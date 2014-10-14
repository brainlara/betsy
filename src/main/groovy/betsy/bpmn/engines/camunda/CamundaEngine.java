package betsy.bpmn.engines.camunda;

import betsy.bpmn.engines.BPMNEngine;
import betsy.bpmn.model.BPMNProcess;
import betsy.bpmn.model.BPMNTestBuilder;
import betsy.bpmn.model.BPMNTestCase;
import betsy.bpmn.reporting.BPMNTestcaseMerger;
import betsy.common.config.Configuration;
import betsy.common.tasks.*;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class CamundaEngine extends BPMNEngine {
    @Override
    public String getName() {
        return "camunda";
    }

    public String getCamundaUrl() {
        return "http://localhost:8080";
    }

    public String getTomcatName() {
        return "apache-tomcat-7.0.33";
    }

    public Path getTomcatDir() {
        return getServerPath().resolve("server").resolve(getTomcatName());
    }

    @Override
    public void deploy(final BPMNProcess process) {
        FileTasks.copyFileIntoFolder(process.getTargetPath().resolve("war").resolve(process.getName() + ".war"), getTomcatDir().resolve("webapps"));

        //wait until it is deployed
        final Path logFile = FileTasks.findFirstMatchInFolder(getTomcatDir().resolve("logs"), "catalina*");
        if (logFile == null) {
            throw new IllegalStateException("Could not find catalina log file in " + getTomcatDir().resolve("logs"));
        }

        WaitTasks.waitFor(15000, 500, () ->
                FileTasks.hasFileSpecificSubstring(logFile, "Process Application " + process.getName() + " Application successfully deployed.") ||
                        FileTasks.hasFileSpecificSubstring(logFile, "Context [/" + process.getName() + "] startup failed due to previous errors"));
    }

    @Override
    public void buildArchives(final BPMNProcess process) {
        XSLTTasks.transform(getXsltPath().resolve("../scriptTask.xsl"),
                process.getResourcePath().resolve(process.getName() + ".bpmn"),
                process.getTargetPath().resolve("war/WEB-INF/classes/" + process.getName() + ".bpmn-temp"));

        XSLTTasks.transform(getXsltPath().resolve("camunda.xsl"),
                process.getTargetPath().resolve("war/WEB-INF/classes/" + process.getName() + ".bpmn-temp"),
                process.getTargetPath().resolve("war/WEB-INF/classes/" + process.getName() + ".bpmn"));

        FileTasks.deleteFile(process.getTargetPath().resolve("war/WEB-INF/classes/" + process.getName() + ".bpmn-temp"));

        CamundaResourcesGenerator generator = new CamundaResourcesGenerator();
        generator.setGroupId(process.getGroupId());
        generator.setProcessName(process.getName());
        generator.setSrcDir(process.getResourcePath());
        generator.setDestDir(process.getTargetPath().resolve("war"));
        generator.setVersion(process.getVersion());
        generator.generateWar();
    }

    @Override
    public void buildTest(final BPMNProcess process) {
        BPMNTestBuilder builder = new BPMNTestBuilder();
        builder.setPackageString(getName() + "." + process.getGroup());
        builder.setLogDir(getTomcatDir().resolve("bin"));
        builder.setProcess(process);
        builder.buildTests();
    }

    @Override
    public String getEndpointUrl(BPMNProcess process) {
        return "http://localhost:8080/engine-rest/engine/default";
    }

    @Override
    public void storeLogs(BPMNProcess process) {
        FileTasks.mkdirs(process.getTargetLogsPath());

        // TODO only copy log files from tomcat, the other files are files for the test
        FileTasks.copyFilesInFolderIntoOtherFolder(getTomcatDir().resolve("logs"), process.getTargetLogsPath());

        for (BPMNTestCase tc : process.getTestCases()) {
            FileTasks.copyFileIntoFolder(getTomcatDir().resolve("bin").resolve("log" + tc.getNumber() + ".txt"), process.getTargetLogsPath());
        }

    }

    @Override
    public void install() {
        CamundaInstaller installer = new CamundaInstaller();
        installer.setDestinationDir(getServerPath());
        installer.setTomcatName(getTomcatName());
        installer.install();
    }

    @Override
    public void startup() {
        Path pathToJava7 = Configuration.getJava7Home();
        FileTasks.assertDirectory(pathToJava7);

        Path pathToJre7 = Configuration.getJre7Home();
        FileTasks.assertDirectory(pathToJre7);

        Map<String, String> map = new LinkedHashMap<>(2);
        map.put("JAVA_HOME", pathToJava7.toString());
        map.put("JRE_HOME", pathToJre7.toString());
        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build(getServerPath(), "camunda_startup.bat"), map);

        Map<String, String> map1 = new LinkedHashMap<>(2);
        map1.put("JAVA_HOME", pathToJava7.toString());
        map1.put("JRE_HOME", pathToJre7.toString());
        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getServerPath().resolve("camunda_startup.sh")), map1);

        WaitTasks.waitForAvailabilityOfUrl(30_000, 500, getCamundaUrl());
    }

    @Override
    public void shutdown() {
        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build("taskkill").values("/FI", "WINDOWTITLE eq Tomcat"));
        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getServerPath().resolve("camunda_shutdown.sh")));
    }

    @Override
    public boolean isRunning() {
        return URLTasks.isUrlAvailable(getCamundaUrl());
    }

    @Override
    public void testProcess(BPMNProcess process) {
        for (BPMNTestCase testCase : process.getTestCases()) {
            CamundaTester tester = new CamundaTester();
            tester.setTestCase(testCase);
            tester.setTestSrc(process.getTargetTestSrcPathWithCase(testCase.getNumber()));
            tester.setRestURL(getEndpointUrl(process));
            tester.setReportPath(process.getTargetReportsPathWithCase(testCase.getNumber()));
            tester.setTestBin(process.getTargetTestBinPathWithCase(testCase.getNumber()));
            tester.setKey(process.getName());
            tester.setLogDir(getTomcatDir().resolve("logs"));
            tester.runTest();
        }


        new BPMNTestcaseMerger(process.getTargetReportsPath()).mergeTestCases();
    }

}