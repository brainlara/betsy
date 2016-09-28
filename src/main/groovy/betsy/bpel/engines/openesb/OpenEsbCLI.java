package betsy.bpel.engines.openesb;

import java.nio.file.Path;

import betsy.common.tasks.ConsoleTasks;
import betsy.common.tasks.FileTasks;
import betsy.common.util.StringUtils;
import org.apache.log4j.Logger;

public class OpenEsbCLI {

    private static final Logger LOGGER = Logger.getLogger(OpenEsbCLI.class);
    private final Path glassfishHome;

    public OpenEsbCLI(Path glassfishHome) {
        this.glassfishHome = glassfishHome;
    }

    public void stopDomain() {
        FileTasks.assertDirectory(glassfishHome);

        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build(getAsAdminWindows()).values("stop-domain", "domain1"));

        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getBinFolder(), "chmod").values("+x", "asadmin"));
        ConsoleTasks.executeOnUnix(ConsoleTasks.CliCommand.build("sync"));
        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getAsAdminUnix()).values("stop-domain", "domain1"));
    }

    public void startDomain() {
        FileTasks.assertDirectory(glassfishHome);

        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build(getAsAdminWindows()).values("start-domain", "domain1"));

        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getBinFolder(), "chmod").values("+x", "asadmin"));
        ConsoleTasks.executeOnUnix(ConsoleTasks.CliCommand.build("sync"));
        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getAsAdminUnix()).values("start-domain", "domain1"));
    }

    private Path getAsAdminWindows() {
        return getBinFolder().resolve("asadmin.bat").toAbsolutePath();
    }

    private Path getAsAdminUnix() {
        return getBinFolder().resolve("asadmin").toAbsolutePath();
    }

    protected Path getBinFolder() {
        return glassfishHome.resolve("bin");
    }

    public void forceRedeploy(String processName, Path packageFilePath, Path tmpFolder) {
        FileTasks.assertDirectory(glassfishHome);

        LOGGER.info("Deploying " + processName + " from " + packageFilePath);

        Path deployCommands = tmpFolder.resolve("deploy_commands.txt");

        // QUIRK path must always be in unix style, otherwise it is not correctly deployed
        String packageFilePathUnixStyle = StringUtils.toUnixStyle(packageFilePath);

        String scriptContent = "deploy-jbi-service-assembly " + packageFilePathUnixStyle + "\n";
        scriptContent += "start-jbi-service-assembly " + processName + "Application\n";

        FileTasks.createFile(deployCommands, scriptContent);

        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build(getAsAdminWindows()).
                values("multimode", "--file", deployCommands.toAbsolutePath().toString()));

        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getBinFolder(), "chmod").values("+x", "asadmin"));
        ConsoleTasks.executeOnUnix(ConsoleTasks.CliCommand.build("sync"));
        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getAsAdminUnix()).
                values("multimode", "--file", deployCommands.toAbsolutePath().toString()));
    }

    public void undeploy(String processName, Path tmpFolder) {
        FileTasks.assertDirectory(glassfishHome);

        LOGGER.info("Undeploying " + processName);

        Path deployCommands = tmpFolder.resolve("undeploy_commands.txt");


        String scriptContent = "stop-jbi-service-assembly " + processName + "Application\n";
        scriptContent += "shut-down-jbi-service-assembly " + processName + "Application\n";
        scriptContent += "undeploy-jbi-service-assembly " + processName + "Application\n";

        FileTasks.createFile(deployCommands, scriptContent);

        ConsoleTasks.executeOnWindowsAndIgnoreError(ConsoleTasks.CliCommand.build(getAsAdminWindows()).
                values("multimode", "--file", deployCommands.toAbsolutePath().toString()));

        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getBinFolder(), "chmod").values("+x", "asadmin"));
        ConsoleTasks.executeOnUnix(ConsoleTasks.CliCommand.build("sync"));
        ConsoleTasks.executeOnUnixAndIgnoreError(ConsoleTasks.CliCommand.build(getAsAdminUnix()).
                values("multimode", "--file", deployCommands.toAbsolutePath().toString()));
    }

}
