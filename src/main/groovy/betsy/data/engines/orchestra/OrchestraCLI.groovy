package betsy.data.engines.orchestra

import betsy.data.Process
import betsy.data.engines.Engine;


class OrchestraCLI {

    AntBuilder ant = new AntBuilder()
    Engine engine

    void deploy(Process process) {
        ant.exec(executable: "cmd", dir: "${engine.serverPath}/orchestra-cxf-tomcat-4.9.0") {
            arg(value: "/c")
            arg(value: "ant")
            arg(value: "deploy")
            arg(value: "-Dbar=${new File(process.targetPackageFilePath).absolutePath}")
        }
    }

}
