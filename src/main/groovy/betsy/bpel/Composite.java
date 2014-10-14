package betsy.bpel;

import betsy.bpel.engines.Engine;
import betsy.bpel.model.BetsyProcess;
import betsy.bpel.reporting.BPELCsvReport;
import betsy.bpel.reporting.Reporter;
import betsy.bpel.soapui.TestBuilder;
import betsy.bpel.ws.TestPartnerService;
import betsy.bpel.ws.TestPartnerServicePublisherInternal;
import betsy.common.analytics.Analyzer;
import betsy.common.model.TestSuite;
import betsy.common.tasks.FileTasks;
import betsy.common.tasks.WaitTasks;
import betsy.common.util.IOCapture;
import betsy.common.util.LogUtil;
import betsy.common.util.Progress;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import soapui.SoapUiRunner;

import java.nio.file.Path;

public class Composite {
    private static Logger logger = Logger.getLogger(Composite.class);
    private TestPartnerService testPartner = new TestPartnerServicePublisherInternal();
    private TestSuite testSuite;
    private int requestTimeout = 15000;

    protected static void log(String name, Runnable closure) {
        LogUtil.log(name, logger, closure);
    }

    protected static void log(Path path, Runnable closure) {
        LogUtil.log(path, logger, closure);
    }

    public void execute() {
        final Progress progress = new Progress(testSuite.getProcessesCount());
        MDC.put("progress", progress.toString());

        // prepare test suite
        // MUST BE OUTSITE OF LOG -> as it deletes whole file tree
        FileTasks.deleteDirectory(testSuite.getPath());
        FileTasks.mkdirs(testSuite.getPath());

        log(testSuite.getPath(), () -> {

            // fail fast
            for (Engine engine : testSuite.getEngines()) {
                if (engine.isRunning()) {
                    throw new IllegalStateException("Engine " + String.valueOf(engine) + " is running");
                }
            }

            for (Engine engine : testSuite.getEngines()) {
                FileTasks.mkdirs(engine.getPath());

                log(engine.getPath(), () -> {
                    for (BetsyProcess process : engine.getProcesses()) {

                        progress.next();
                        MDC.put("progress", progress.toString());

                        executeProcess(process);
                    }

                });
            }


            createReports();
        });

    }

    protected void createReports() {
        log(testSuite.getReportsPath(), () -> {
            new Reporter(testSuite).createReports();
            new Analyzer(testSuite.getCsvFilePath(), testSuite.getReportsPath()).createAnalytics(new BPELCsvReport());
        });
    }

    protected void executeProcess(final BetsyProcess process) {
        Retry retry = new Retry();
        retry.setProcess(process);
        retry.atMostThreeTimes(() -> log(process.getTargetPath(), () -> {
            try {
                buildPackageAndTest(process);
                installAndStart(process);
                deploy(process);
                test(process);
                collect(process);
            } finally {
                // ensure shutdown
                shutdown(process);
            }

        }));
    }

    protected void shutdown(final BetsyProcess process) {
        log(String.valueOf(process.getTargetPath()) + "/engine_shutdown", () -> process.getEngine().shutdown());
    }

    protected void deploy(final BetsyProcess process) {
        log(String.valueOf(process.getTargetPath()) + "/deploy", () -> process.getEngine().deploy(process));
    }

    protected void installAndStart(final BetsyProcess process) {
        // setup infrastructure
        log(String.valueOf(process.getTargetPath()) + "/engine_install", () -> process.getEngine().install());
        log(String.valueOf(process.getTargetPath()) + "/engine_startup", () -> process.getEngine().startup());
    }

    protected void test(final BetsyProcess process) {
        log(String.valueOf(process.getTargetPath()) + "/test", () -> {
            try {
                try {
                    testPartner.publish();
                } catch (Exception ignore) {
                    testPartner.unpublish();
                    logger.debug("Address already in use - waiting 2 seconds to get available");
                    WaitTasks.sleep(2000);
                    testPartner.publish();
                }
                testSoapUi(process);
            } finally {
                testPartner.unpublish();
            }
        });
    }

    protected void collect(final BetsyProcess process) {
        log(String.valueOf(process.getTargetPath()) + "/collect", () -> process.getEngine().storeLogs(process));
    }

    protected void testSoapUi(final BetsyProcess process) {
        log(String.valueOf(process.getTargetPath()) + "/test_soapui", () -> IOCapture.captureIO(() ->
                new SoapUiRunner(process.getTargetSoapUIFilePath(), process.getTargetReportsPath()).run()));
        WaitTasks.sleep(500);
    }

    protected void buildPackageAndTest(final BetsyProcess process) {
        log(String.valueOf(process.getTargetPath()) + "/build", () -> {
            buildPackage(process);
            buildTest(process);
        });
    }

    protected void buildTest(final BetsyProcess process) {
        log(String.valueOf(process.getTargetPath()) + "/build_test", () ->
                IOCapture.captureIO(() -> new TestBuilder(process, requestTimeout).buildTest()));
    }

    protected void buildPackage(final BetsyProcess process) {
        log(String.valueOf(process.getTargetPath()) + "/build_package",
                () -> IOCapture.captureIO(
                        () -> process.getEngine().buildArchives(process)));
    }

    public TestPartnerService getTestPartner() {
        return testPartner;
    }

    public void setTestPartner(TestPartnerService testPartner) {
        this.testPartner = testPartner;
    }

    public TestSuite getTestSuite() {
        return testSuite;
    }

    public void setTestSuite(TestSuite testSuite) {
        this.testSuite = testSuite;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
    }
}