package betsy.bpmn.engines.camunda;

import betsy.common.model.ProcessLanguage;
import betsy.common.model.engine.Engine;

import java.time.LocalDate;

public class Camunda740WithBpmnSpectorEngine extends Camunda710Engine {

    @Override
    public Engine getEngineObject() {
        return new Engine(ProcessLanguage.BPMN, "camunda", "7.4.0_BPMNspector", LocalDate.of(2015, 11, 30), "Apache-2.0");
    }

    @Override
    public String getTomcatName() {
        return "apache-tomcat-8.0.24";
    }

    @Override
    public void install() {
        CamundaInstaller camundaInstaller = new CamundaInstaller();
        camundaInstaller.setDestinationDir(getServerPath());
        camundaInstaller.setFileName("camunda-bpm-tomcat-7.4.0-BPMNspector.zip");
        camundaInstaller.setTomcatName(getTomcatName());
        camundaInstaller.install();
    }
}