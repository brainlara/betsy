package peal.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.xml.namespace.QName;

import betsy.common.tasks.FileTasks;
import betsy.common.tasks.WaitTasks;
import betsy.common.util.IOCapture;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import peal.DeploymentException;
import peal.InstanceService;
import peal.ProcessLanguage;
import peal.ProcessModelService;
import peal.helper.ZipFileHelper;
import peal.identifier.EngineId;
import peal.identifier.InstanceId;
import peal.identifier.ProcessModelId;
import peal.observer.EngineState;
import peal.observer.ProcessModelState;
import peal.packages.DeploymentPackage;
import peal.packages.LogPackage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class EngineServiceImplBpmnEnginesWithoutUndeployTest extends AbstractEngineServiceCleanup{

    public static final Path SEQUENCE_FOLDER = Paths.get("src/test/resources/SequenceFlow");

    private final EngineId engineId;
    private final ProcessModelId processModelId;

    EngineServiceImpl engineService = new EngineServiceImpl();
    ProcessModelService processModelService = new ProcessModelServiceImpl(engineService);
    InstanceService instanceService = new InstanceServiceImpl(engineService);

    public EngineServiceImplBpmnEnginesWithoutUndeployTest(EngineId engineId) {
        this.engineId = Objects.requireNonNull(engineId);
        this.processModelId = new ProcessModelId(engineId.getEngineId(),
                new QName("http://dsg.wiai.uniba.de/betsy/activities/bpel/sequence", "SequenceFlow"));
    }

    @Test
    public void testAll() throws IOException, DeploymentException {
        if (engineService.getSupportedLanguage(engineId) != ProcessLanguage.BPMN) {
            return;
        }

        assertState(EngineState.NOT_INSTALLED);

        engineService.install(engineId);
        assertState(EngineState.INSTALLED);

        engineService.start(engineId);

        WaitTasks.sleep(2000);

        assertState(EngineState.STARTED);

        DeploymentPackage deployableBpelPackage = processModelService.makeDeployable(engineId,
                ZipFileHelper.zipToProcessModelPackage(ZipFileHelper.buildFromFolder(SEQUENCE_FOLDER)));
        System.out.println("FILE EXTENSION: " + deployableBpelPackage.fileExtension);
        assertState(EngineState.STARTED);

        ProcessModelId processModelId = processModelService.deploy(engineId, deployableBpelPackage);
        assertState(EngineState.STARTED);

        WaitTasks.sleep(2000);

        List<peal.Variable> variables = Arrays.asList(new peal.Variable("testCaseNumber", "Integer", new Integer(1)),
                new peal.Variable("integerVariable", "Integer", new Integer(0)));
        InstanceId instanceId = instanceService.start(processModelId, variables);
        assertState(EngineState.STARTED);

        WaitTasks.sleep(2000);
        LogPackage instanceLogs = instanceService.getLogs(instanceId);
        Path instanceLogFile = ZipFileHelper.extractIntoTemporaryFolder(instanceLogs).resolve("log1.txt");
        assertEquals(Collections.singletonList("SCRIPT_task1"), Files.readAllLines(instanceLogFile));

        engineService.stop(engineId);
        WaitTasks.sleep(5000);
        assertState(EngineState.INSTALLED);

        engineService.uninstall(engineId);
        assertState(EngineState.NOT_INSTALLED);
    }

    private void assertState(EngineState engineState) {
        assertEquals(engineState, engineService.getState(engineId));
    }

    private void assertState(ProcessModelState engineState) {
        assertEquals(engineState, processModelService.getState(processModelId));
    }

    @Parameterized.Parameters(name = "{index} {0}")
    public static Iterable<Object[]> data() {
        return new EngineServiceImpl().getSupportedEngines().stream()
                .filter(p -> new EngineServiceImpl().getSupportedLanguage(p).equals(ProcessLanguage.BPMN))
                // full: camunda activiti
                .filter(p -> !p.toString().startsWith("jbpm"))
                .map(p -> new Object[] {p})
                .collect(Collectors.toList());
    }

    @Override public EngineId getEngineId() {
        return engineId;
    }
}