package peal.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import javax.xml.namespace.QName;

import betsy.common.tasks.WaitTasks;
import org.junit.Test;
import peal.DeploymentException;
import peal.ProcessLanguage;
import peal.ProcessModelService;
import peal.helper.ZipFileHelper;
import peal.identifier.EngineId;
import peal.identifier.ProcessModelId;
import peal.impl.engine.EngineServiceImpl;
import peal.impl.processmodel.ProcessModelServiceImpl;
import peal.observer.EngineState;
import peal.observer.ProcessModelState;
import peal.packages.DeploymentPackage;

import static org.junit.Assert.assertEquals;

public class AbstractEngineServiceImplBpelEnginesTest extends AbstractEngineServiceCleanup {

    public static final Path SEQUENCE_FOLDER = Paths.get("src/test/resources/Sequence");

    private final EngineId engineId;
    private final ProcessModelId processModelId;

    EngineServiceImpl engineService = new EngineServiceImpl();
    ProcessModelService processModelService = new ProcessModelServiceImpl(engineService);

    public AbstractEngineServiceImplBpelEnginesTest(EngineId engineId) {
        this.engineId = Objects.requireNonNull(engineId);
        this.processModelId = new ProcessModelId(engineId.getEngineId(),
                new QName("http://dsg.wiai.uniba.de/betsy/activities/bpel/sequence", "Sequence"));
    }

    @Test
    public void testAll() throws IOException, DeploymentException {
        if (engineService.getSupportedLanguage(engineId) != ProcessLanguage.BPEL) {
            return;
        }

        assertState(ProcessModelState.NOT_DEPLOYED);
        assertState(EngineState.NOT_INSTALLED);

        engineService.install(engineId);
        assertState(ProcessModelState.NOT_DEPLOYED);
        assertState(EngineState.INSTALLED);

        engineService.start(engineId);

        WaitTasks.sleep(2000);

        assertState(ProcessModelState.NOT_DEPLOYED);
        assertState(EngineState.STARTED);

        DeploymentPackage deployableBpelPackage = processModelService.makeDeployable(engineId,
                ZipFileHelper.zipToProcessModelPackage(ZipFileHelper.buildFromFolder(SEQUENCE_FOLDER)));
        System.out.println("FILE EXTENSION: " + deployableBpelPackage.fileExtension);
        assertState(ProcessModelState.NOT_DEPLOYED);
        assertState(EngineState.STARTED);

        ProcessModelId processModelId = processModelService.deploy(engineId, deployableBpelPackage);
        assertState(ProcessModelState.DEPLOYED);
        assertState(EngineState.STARTED);

        WaitTasks.sleep(5000);

        WSTester.assertCorrectWorkingProcess(getUrlForProcessId(processModelId));
        assertState(ProcessModelState.DEPLOYED);
        assertState(EngineState.STARTED);

        processModelService.undeploy(processModelId);
        assertState(ProcessModelState.NOT_DEPLOYED);
        assertState(EngineState.STARTED);

        engineService.stop(engineId);
        WaitTasks.sleep(5000);
        assertState(EngineState.INSTALLED);
        assertState(ProcessModelState.NOT_DEPLOYED);

        engineService.uninstall(engineId);
        assertState(EngineState.NOT_INSTALLED);
        assertState(ProcessModelState.NOT_DEPLOYED);
    }

    private void assertState(EngineState engineState) {
        assertEquals(engineState, engineService.getState(engineId));
    }

    private void assertState(ProcessModelState engineState) {
        assertEquals(engineState, processModelService.getState(processModelId));
    }

    private URL getUrlForProcessId(ProcessModelId processModelId) throws MalformedURLException {
        return new URL(new EngineServiceImpl().getEngineByID(processModelId.toEngineId()).getEndpointUrl(processModelId.getProcessId().getLocalPart()));
    }

    @Override public EngineId getEngineId() {
        return engineId;
    }
}
