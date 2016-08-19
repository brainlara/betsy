package betsy.bpmn.engines.camunda;

import betsy.bpmn.engines.*;
import betsy.bpmn.model.BPMNAssertions;
import betsy.bpmn.model.BPMNTestCase;
import betsy.bpmn.model.Variable;
import betsy.common.tasks.FileTasks;
import betsy.common.tasks.WaitTasks;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.nio.file.Path;

public class CamundaTester {

    private static final Logger LOGGER = Logger.getLogger(CamundaTester.class);

    private BPMNTestCase testCase;

    private String key;

    private Path logDir;

    private BPMNTester bpmnTester;

    /**
     * runs a single test
     */
    public void runTest() {

        Path logFile = FileTasks.findFirstMatchInFolder(logDir, "catalina*");

        BPMNProcessOutcomeChecker.ProcessOutcome outcomeBeforeTest = new CamundaLogBasedProcessOutcomeChecker(logFile).checkProcessOutcome(key);
        if(outcomeBeforeTest == BPMNProcessOutcomeChecker.ProcessOutcome.UNDEPLOYED) {
            BPMNAssertions.appendToFile(getFileName(), BPMNAssertions.ERROR_DEPLOYMENT);
        }

        try {
            if(testCase.hasParallelProcess()){
                new CamundaProcessStarter().start(BPMNTestCase.PARALLEL_PROCESS_KEY);
            }

            new CamundaProcessStarter().start(key, testCase.getVariables());

            // Wait and check for Errors only if instantiation was successful
            WaitTasks.sleep(testCase.getDelay().orElse(0));

            BPMNProcessOutcomeChecker.ProcessOutcome outcomeAfterTest = new CamundaLogBasedProcessOutcomeChecker(logFile).checkProcessOutcome(key);
            if(outcomeAfterTest == BPMNProcessOutcomeChecker.ProcessOutcome.RUNTIME) {
                BPMNAssertions.appendToFile(getFileName(), BPMNAssertions.ERROR_RUNTIME);
            } else if(outcomeAfterTest == BPMNProcessOutcomeChecker.ProcessOutcome.PROCESS_ABORTED_BECAUSE_ERROR_EVENT_THROWN) {
                BPMNAssertions.appendToFile(getFileName(), BPMNAssertions.ERROR_THROWN_ERROR_EVENT);
            }

            // Check on parallel execution
            BPMNEnginesUtil.checkParallelExecution(testCase, getFileName());

            // Check whether MARKER file exists
            BPMNEnginesUtil.checkMarkerFileExists(testCase, getFileName());

            // Check data type
            BPMNEnginesUtil.checkDataLog(testCase, getFileName());
        } catch (Exception e) {
            LOGGER.info("Could not start process", e);
            BPMNAssertions.appendToFile(getFileName(), BPMNAssertions.ERROR_RUNTIME);
        }

        BPMNEnginesUtil.substituteSpecificErrorsForGenericError(testCase, getFileName());

        LOGGER.info("contents of log file " + getFileName() + ": " + FileTasks.readAllLines(getFileName()));

        bpmnTester.test();
    }

    public void setBpmnTester(BPMNTester bpmnTester) {
        this.bpmnTester = bpmnTester;
    }

    private Path getFileName() {
        return logDir.resolve("..").normalize().resolve("bin").resolve("log" + testCase.getNumber() + ".txt");
    }

    public BPMNTestCase getTestCase() {
        return testCase;
    }

    public void setTestCase(BPMNTestCase testCase) {
        this.testCase = testCase;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setLogDir(Path logDir) {
        this.logDir = logDir;
    }

}
