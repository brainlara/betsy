package betsy.bpmn.engines;

import java.util.Collections;
import java.util.List;

import pebl.benchmark.test.steps.vars.Variable;

public interface BPMNProcessStarter {

    /**
     * Start a new process instance for a given process id. The list of variables is sent there as well.
     *
     * @param processID
     * @param variables
     * @throws RuntimeException in case any failures are detected
     */
    void start(String processID, List<Variable> variables) throws RuntimeException;

    /**
     * Starts a new process instance but without any variables.
     *
     * @param processID
     */
    default void start(String processID) {
        start(processID, Collections.emptyList());
    }

}
