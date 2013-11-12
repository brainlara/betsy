package betsy.data.engines.ode

import betsy.data.BetsyProcess
import betsy.data.engines.LocalEngine
import betsy.data.engines.tomcat.Tomcat

class OdeEngine extends LocalEngine {

    @Override
    String getName() {
        "ode"
    }

    @Override
    String getEndpointUrl(BetsyProcess process) {
        "${tomcat.tomcatUrl}/ode/processes/${process.name}TestInterface"
    }

    String getDeploymentDir() {
        "${tomcat.tomcatDir}/webapps/ode/WEB-INF/processes"
    }

    Tomcat getTomcat() {
        new Tomcat(engineDir: serverPath)
    }

    @Override
    void startup() {
        tomcat.startup()
    }

    @Override
    void shutdown() {
        tomcat.shutdown()
    }

    @Override
    void storeLogs(BetsyProcess process) {
        ant.mkdir(dir: "${process.targetPath}/logs")
        ant.copy(todir: "${process.targetPath}/logs") {
            ant.fileset(dir: "${tomcat.tomcatDir}/logs/")
        }
    }

    @Override
    void install() {
        new OdeInstaller().install()
    }

    @Override
    void deploy(BetsyProcess process) {
        new OdeDeployer(processName: process.name,
                logFilePath: "${tomcat.tomcatDir}/logs/ode.log",
                deploymentDirPath: getDeploymentDir(),
                packageFilePath: process.targetPackageFilePath
        ).deploy()
    }

    @Override
    void buildArchives(BetsyProcess process) {
        packageBuilder.createFolderAndCopyProcessFilesToTarget(process)

        // engine specific steps
        ant.xslt(in: process.bpelFilePath, out: "${process.targetBpelPath}/deploy.xml", style: "$xsltPath/bpel_to_ode_deploy_xml.xsl")
        ant.replace(file: "${process.targetBpelPath}/TestInterface.wsdl", token: "TestInterfaceService", value: "${process.name}TestInterfaceService")
        ant.replace(file: "${process.targetBpelPath}/deploy.xml", token: "TestInterfaceService", value: "${process.name}TestInterfaceService")

        packageBuilder.replaceEndpointTokenWithValue(process)
        packageBuilder.replacePartnerTokenWithValue(process)

        packageBuilder.bpelFolderToZipFile(process)
    }

    @Override
    void failIfRunning() {
        tomcat.checkIfIsRunning()
    }

}
