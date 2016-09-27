package configuration.bpmn;

import betsy.bpmn.model.BPMNTestCaseBuilder;
import pebl.benchmark.test.Test;
import pebl.benchmark.feature.FeatureSet;
import pebl.benchmark.feature.Feature;

import java.util.Arrays;
import java.util.List;

/**
 * This class bundles processes that are somewhat problematic, but engines should behave correctly by ignoring certain attributes or similar behavior
 */
class ErrorProcesses {

    /*
    * ERROR CONSTRUCTS
    */
    public static final FeatureSet CONSTRUCT_PARALLEL_GATEWAY_COND = new FeatureSet(Groups.ERRORS, "ParallelGateway_Conditions",
            "Checking the detection of invalid conditions attached to parallel gateways.");

    public static final FeatureSet CONSTRUCT_INVALID_GATEWAY_COMBINATIONS = new FeatureSet(Groups.ERRORS, "InvalidGatewayCombinations",
            "Checking the detection of invalid combinations of gateway types resulting, e.g., resulting in deadlocks.");

    public static final FeatureSet CONSTRUCT_INVALID_LOOP_COND = new FeatureSet(Groups.ERRORS, "InvalidLoopConditions",
            "Checking the detection of invalid loop conditions.");

    public static final FeatureSet CONSTRUCT_INVALID_TOKEN_QUANTITY = new FeatureSet(Groups.ERRORS, "InvalidTokenQuantity",
            "Checking the detection of invalid token quantities.");


     /*
     * FEATURE TESTS
     */

    public static final Test PARALLEL_GATEWAY_WITH_CONDITIONS = BPMNProcessBuilder.buildErrorProcess(
            "ParallelGateway_Conditions", "A process with four scriptTasks and two parallelGateways. " +
                    "Two of the scriptTasks are surrounded by the parallelGateways and the sequenceFlows pointing to the mergine gateway have conditions. " +
                    "These conditions should be ignored by an engine.",
            new Feature(CONSTRUCT_PARALLEL_GATEWAY_COND, "ParallelGateway_Conditions"),
            new BPMNTestCaseBuilder().inputA().assertTask1().assertTask2().assertTask3(),
            new BPMNTestCaseBuilder().inputB().assertTask1().assertTask2().assertTask3(),
            new BPMNTestCaseBuilder().inputAB().assertTask1().assertTask2().assertTask3(),
            new BPMNTestCaseBuilder().inputC().assertTask1().assertTask2().assertTask3()
    );

    public static final Test EXCLUSIVE_DIVERGING_PARALLEL_CONVERGING = BPMNProcessBuilder.buildErrorProcess(
            "ExclusiveDiverging_ParallelConverging", "A process with four scriptTasks, a diverging exclusiveGateway and a converging parallelGateway. " +
            "Two scriptTasks are enclosed by the gateways and the execution should deadlock, because only one incoming branch of the parallelGateway " +
            "should ever be executed. Hence, the scriptTask following the parallelGateway should never be executed.",
            new Feature(CONSTRUCT_INVALID_GATEWAY_COMBINATIONS, "ExclusiveDiverging_ParallelConverging"),
            new BPMNTestCaseBuilder().inputA().assertTask1(),
            new BPMNTestCaseBuilder().inputB().assertTask2(),
            new BPMNTestCaseBuilder().inputAB().assertTask1()
    );

    public static final Test INCLUSIVE_DIVERGING_PARALLEL_CONVERGING = BPMNProcessBuilder.buildErrorProcess(
            "InclusiveDiverging_ParallelConverging", "A process with four scriptTasks, a diverging inclusiveGateway and a converging parallelGateway. " +
            "Two scriptTasks are enclosed by the gateways and the execution should deadlock if only one incoming branch of the parallelGateway " +
            "is enabled. Hence, the scriptTask following the parallelGateway should only be executed in a single case.",
            new Feature(CONSTRUCT_INVALID_GATEWAY_COMBINATIONS, "InclusiveDiverging_ParallelConverging"),
            new BPMNTestCaseBuilder().inputA().assertTask1(),
            new BPMNTestCaseBuilder().inputB().assertTask2(),
            new BPMNTestCaseBuilder().inputAB().assertTask1().assertTask2().assertTask3()
    );

    public static final Test LOOP_TASK_NEGATIVE_LOOP_MAXIMUM = BPMNProcessBuilder.buildErrorProcess(
            "LoopTask_NegativeLoopMaximum", "A scriptTask with standardLoopCharacteristics and a condition that always evaluates to true. Additionally a loopMaximum is set to minus one.",
            new Feature(CONSTRUCT_INVALID_LOOP_COND, "LoopTask_NegativeLoopMaximum"),
            new BPMNTestCaseBuilder().assertGenericError()
    );

    public static final Test MULTI_INSTANCE_TASK_NEGATIVE_LOOP_CARDINALITY = BPMNProcessBuilder.buildErrorProcess(
            "MultiInstanceTask_NegativeLoopCardinality", "A scriptTask that is marked as a sequential multiInstance task and is enabled minus one times.",
            new Feature(CONSTRUCT_INVALID_LOOP_COND, "MultiInstanceTask_NegativeLoopCardinality"),
            new BPMNTestCaseBuilder().assertGenericError()
    );

    public static final Test TOKEN_START_QUANTITY_TWO = BPMNProcessBuilder.buildErrorProcess(
            "Token_StartQuantity_Two", "A process with a scriptTask with completionQuantity=1 and, immediately afterwards, " +
                    "a scriptTask with startQuantity=2. Since there will never two tokens arrive, the scriptTask must not be executed.",
            new Feature(CONSTRUCT_INVALID_TOKEN_QUANTITY, "Token_StartQuantity_Two"),
            new BPMNTestCaseBuilder().assertTask1()
    );

    public static final Test TOKEN_START_QUANTITY_ZERO = BPMNProcessBuilder.buildErrorProcess(
            "Token_StartQuantity_Zero", "A process with a scriptTask with startQuantity=0. " +
                    "Since startQuantity must not be zero, the process must not be executed.",
            new Feature(CONSTRUCT_INVALID_TOKEN_QUANTITY, "Token_StartQuantity_Zero"),
            new BPMNTestCaseBuilder().assertGenericError()
    );

    public static final Test TOKEN_COMPLETION_QUANTITY_ZERO = BPMNProcessBuilder.buildErrorProcess(
            "Token_CompletionQuantity_Zero", "A process with a scriptTask with completionQuantity=0. " +
            "Since completionQuantity must not be zero, the process must not be executed.",
            new Feature(CONSTRUCT_INVALID_TOKEN_QUANTITY, "Token_CompletionQuantity_Zero"),
            new BPMNTestCaseBuilder().assertGenericError()
    );


    public static final List<Test> ERRORS = Arrays.asList(
            PARALLEL_GATEWAY_WITH_CONDITIONS,
            EXCLUSIVE_DIVERGING_PARALLEL_CONVERGING,
            INCLUSIVE_DIVERGING_PARALLEL_CONVERGING,

            LOOP_TASK_NEGATIVE_LOOP_MAXIMUM,

            MULTI_INSTANCE_TASK_NEGATIVE_LOOP_CARDINALITY,

            TOKEN_START_QUANTITY_TWO,
            TOKEN_START_QUANTITY_ZERO,
            TOKEN_COMPLETION_QUANTITY_ZERO
    );
}
