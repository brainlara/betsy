package configuration.bpmn

import betsy.bpmn.model.BPMNProcess
import betsy.bpmn.model.BPMNTestCase

class GatewayProcesses {
    static BPMNProcessBuilder builder = new BPMNProcessBuilder()

    public static final BPMNProcess EXCLUSIVE_GATEWAY = builder.buildGatewayProcess(
            "ExclusiveGateway", "A process with four scriptTasks and exclusiveGateways. " +
            "The execution of two of the tasks is controlled by the exclusiveGateways and only one of the tasks is actually executed.",
            [
                    new BPMNTestCase(2).inputA().assertTask1().assertTask3(),
                    new BPMNTestCase(1).inputB().assertTask2().assertTask3(),
                    new BPMNTestCase(3).inputAB().assertTask1().assertTask3(),
                    new BPMNTestCase(4).inputC().assertRuntimeException()
            ]
    )

    public static final BPMNProcess EXCLUSIVE_GATEWAY_WITH_DEFAULT = builder.buildGatewayProcess(
            "ExclusiveGatewayWithDefault", "A process with five scriptTasks and exclusiveGateways. " +
            "The execution of three of the tasks is controlled by the exclusiveGateways based on the input and only one of the tasks is actually executed." +
            "Two tasks are triggered through sequenceFlows with conditionExpressions and one is triggered through a sequenceFlow which is marked as default.",
            [
                    new BPMNTestCase(1).inputB().assertTask2().assertTask4(),
                    new BPMNTestCase(2).inputA().assertTask1().assertTask4(),
                    new BPMNTestCase(3).inputAB().assertTask1().assertTask4(),
                    new BPMNTestCase(4).inputC().assertTask3().assertTask4()
            ]
    )

    public static final BPMNProcess EXCLUSIVE_DIVERING_INCLUSIVE_CONVERGING = builder.buildGatewayProcess(
            "ExclusiveDivergingInclusiveConverging", "A process with four scriptTasks, a diverging exclusiveGateway and a converging inclusiveGateway. " +
            "Two of the tasks are enclosed between the gateways and only one of them is triggered depending on input data. " +
            "The inclusiveGateway should merge the incoming branches.",
            [
                    new BPMNTestCase(2).inputA().assertTask1().assertTask3(),
                    new BPMNTestCase(1).inputB().assertTask2().assertTask3(),
                    new BPMNTestCase(3).inputAB().assertTask1().assertTask3(),
            ]
    )

    public static final BPMNProcess PARALLEL_GATEWAY = builder.buildGatewayProcess(
            "ParallelGateway", "A process with four scriptTasks and two parallelGateways. " +
            "Two of the scriptTasks are surrounded by the parallelGateways.",
            [
                    new BPMNTestCase(1).assertTask1().assertTask2().assertTask3()
            ]
    )

    public static final BPMNProcess INCLUSIVE_GATEWAY = builder.buildGatewayProcess(
            "InclusiveGateway", "A process with four scriptTasks, two of which are encapsulated by inclusiveGateways. " +
            "Either one, none, or both of the scriptTasks are enabled based on input data.",
            [
                    new BPMNTestCase(1).inputAB().assertTask1().assertTask2().assertTask3(),
                    new BPMNTestCase(2).inputA().assertTask1().assertTask3(),
                    new BPMNTestCase(3).inputB().assertTask2().assertTask3(),
                    new BPMNTestCase(4).inputC().assertRuntimeException()
            ]
    )

    public static final BPMNProcess INCLUSIVE_GATEWAY_WITH_DEFAULT = builder.buildGatewayProcess(
            "InclusiveGatewayWithDefault", "A process with five scriptTasks, three of which are encapsulated by inclusiveGateways. " +
            "One of the scriptTasks acts as a default task. Either one, both of the others or the default task are executed based on input data.",
            [
                    new BPMNTestCase(1).inputAB().assertTask1().assertTask2().assertTask4(),
                    new BPMNTestCase(2).inputA().assertTask1().assertTask4(),
                    new BPMNTestCase(3).inputB().assertTask2().assertTask4(),
                    new BPMNTestCase(4).inputC().assertTask3().assertTask4()
            ]
    )

    public static final BPMNProcess INCLUSIVE_DIVERGING_EXCLUSIVE_CONVERGING = builder.buildGatewayProcess(
            "InclusiveDivergingExclusiveConverging", "A process with four scriptTasks, a diverging inclusiveGateway and a converging exclusiveGateway. " +
            "Two of the tasks are encapsulated by the gateways. " +
            "Either one, none, or both of the scriptTasks are enabled based on input data and as a result the exclusiveGateway should either fire once or twice.",
            [
                    new BPMNTestCase(1).inputAB().assertTask1().assertTask3().assertTask2().assertTask3(),
                    new BPMNTestCase(2).inputA().assertTask1().assertTask3(),
                    new BPMNTestCase(3).inputB().assertTask2().assertTask3()
            ]
    )

    public static final BPMNProcess PARALLEL_DIVERGING_EXCLUSIVE_CONVERGING = builder.buildGatewayProcess(
            "ParallelDivergingExclusiveConverging", "A process with four tasks, a diverging parallelGateway and a converging exclusiveGateway. " +
            "Two of the tasks are executed in parallel and then merged by the exclusiveGateway. " +
            "As a result, the task following the exclusiveGateway should be followed twice.",
            [
                    new BPMNTestCase(1).assertTask1().assertTask3().assertTask2().assertTask3()
            ]
    )

    public static final BPMNProcess PARALLEL_DIVERGING_INCLUSIVE_CONVERGING = builder.buildGatewayProcess(
            "ParallelDivergingInclusiveConverging", "A process with four tasks, a diverging parallelGateway and a converging inclusiveGateway. " +
            "Two of the tasks are executed in parallel and merged by the inclusiveGateway.",
            [
                    new BPMNTestCase(1).assertTask1().assertTask2().assertTask3()
            ]
    )

    public static final BPMNProcess EXCLUSIVE_GATEWAY_MIXED = builder.buildGatewayProcess(
            "ExclusiveGatewayMixed", "A process with six scriptTasks and three exclusiveGateways." +
            "One of the gateways acts as a mixed gateway. Each pair of exclusiveGateways encapsulates two script tasks." +
            "The enabling of these scriptTasks depends on input data",
            [
                    new BPMNTestCase(1).inputA().assertTask2().assertTask4().assertTask5(),
                    new BPMNTestCase(2).inputB().assertTask1().assertTask3().assertTask5(),
                    new BPMNTestCase(3).inputAB().assertTask1().assertTask3().assertTask5(),
            ]
    )

    public static final BPMNProcess COMPLEX = builder.buildGatewayProcess(
            "ComplexGateway", "Tests for the complex gateway",
            [
                    new BPMNTestCase(1).inputAB().assertTask1().assertTask2().assertSuccess()
                    //this gateway is actually not supported by camunda and jbpm for that reason there are no more test cases
            ]
    )

    public static final BPMNProcess EVENT_BASED = builder.buildGatewayProcess(
            "EventBasedGateway", "Tests for the complex gateway",
            [
                    new BPMNTestCase(1).assertTask1().assertSuccess()
            ]
    )

    public static final List<BPMNProcess> GATEWAYS = [
            EXCLUSIVE_GATEWAY,
            EXCLUSIVE_GATEWAY_WITH_DEFAULT,
            EXCLUSIVE_GATEWAY_MIXED,
            EXCLUSIVE_DIVERING_INCLUSIVE_CONVERGING,

            INCLUSIVE_GATEWAY,
            INCLUSIVE_GATEWAY_WITH_DEFAULT,
            INCLUSIVE_DIVERGING_EXCLUSIVE_CONVERGING,

            PARALLEL_GATEWAY,
            PARALLEL_DIVERGING_EXCLUSIVE_CONVERGING,
            PARALLEL_DIVERGING_INCLUSIVE_CONVERGING,

            COMPLEX,

            EVENT_BASED
    ].flatten() as List<BPMNProcess>
}