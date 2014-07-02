package betsy;

import betsy.data.BetsyProcess;
import betsy.data.engines.Engine;
import betsy.data.engines.EnginePackageBuilder;
import betsy.executables.Composite;
import configuration.ProcessRepository;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class BetsyTests {
    @Test
    public void simulateATestRun() throws Exception {
        Engine engine = new MockEngine();
        List<BetsyProcess> processes = new ProcessRepository().getByName("ALL");
        Betsy betsy = new Betsy();


        betsy.setEngines(Arrays.asList(engine));
        betsy.setProcesses(processes);
        betsy.setComposite(new MockComposite());
        betsy.execute();
    }

    public class MockEngine extends Engine {
        public void install() {
        }

        public void startup() {
        }

        public void shutdown() {
        }

        public boolean isRunning() {
            return false;
        }

        public void deploy(BetsyProcess process) {
        }

        public void storeLogs(BetsyProcess process) {
        }

        public void buildArchives(BetsyProcess process) {
            new EnginePackageBuilder().createFolderAndCopyProcessFilesToTarget(process);
        }

        public String getEndpointUrl(BetsyProcess process) {
            return "myendpoint";
        }

        public String getName() {
            return "mock";
        }

        public Path getXsltPath() {
            return Paths.get("unused_path");
        }

    }

    public class MockComposite extends Composite {
        @Override
        protected void buildTest(BetsyProcess process) {

        }

        @Override
        protected void test(BetsyProcess process) {

        }

    }
}