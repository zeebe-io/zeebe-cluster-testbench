package io.zeebe.chaos

import io.camunda.zeebe.model.bpmn.Bpmn
import io.camunda.zeebe.model.bpmn.BpmnModelInstance
import io.camunda.zeebe.model.bpmn.builder.AbstractFlowNodeBuilder
import io.camunda.zeebe.model.bpmn.builder.SubProcessBuilder
import java.time.Duration
import java.util.*

private const val ERROR_CODE_FAILED_ACTION = "failedAction"
private const val ID_END_FAILURE = "end_failure"
private const val ID_INTRODUCE_CHAOS = "introduce_chaos"
private const val ID_STEADY_STATE_AFTER = "steady_state_after"
private const val ID_STEADY_STATE_BEFORE = "steady_state_before"
private const val NAME_CHAOS_EXPERIMENT_FAILED = "Chaos Experiment Failed"
private const val NAME_VERIFY_STEADY_STATE = "Verify Steady State"
private const val SUFFIX_ERROR = "_error"

fun toBpmn(
    clusterPlan: String,
    experimentName: String,
    experiment: Experiment
): BpmnModelInstance {
    val processId = "${clusterPlan}-${experimentName}"

    var wip: AbstractFlowNodeBuilder<*, *> =
        Bpmn.createExecutableProcess(processId)
            .startEvent("start").name("Run Chaos Experiment")

    wip = addVerifySteadyStateSubProcess(ID_STEADY_STATE_BEFORE, wip, experiment)

    wip = addIntroduceChaosSubProcess(wip, experiment)

    wip = addVerifySteadyStateSubProcess(ID_STEADY_STATE_AFTER, wip, experiment)

    wip = wip.endEvent("end_success").name("Chaos Experiment Succeeded")

    wip = addBoundaryErrorEvents(wip)

    return wip.done()
}

fun addVerifySteadyStateSubProcess(
    processId: String,
    wip: AbstractFlowNodeBuilder<*, *>,
    experiment: Experiment
): AbstractFlowNodeBuilder<*, *> {

    var result: AbstractFlowNodeBuilder<*, *> =
        wip.subProcess(processId).name(NAME_VERIFY_STEADY_STATE).embeddedSubProcess()
            .startEvent()

    for (probe in experiment.steadyState.probes) {
        val id = "UUID-" + UUID.randomUUID()
            .toString() //must start with a character, or validtion will fail

        result = result.serviceTask(id).name(probe.name).zeebeJobType(probe.provider.path)

        val timeout = probe.provider.timeout
        if (timeout > 0) {
            result =
                result.boundaryEvent().name("Timeout")
                    .timerWithDuration(Duration.ofSeconds(timeout).toString()).endEvent().error(
                        ERROR_CODE_FAILED_ACTION
                    ).name(NAME_CHAOS_EXPERIMENT_FAILED).moveToNode(
                        id
                    )
        }
    }

    return result.endEvent()
        .subProcessDone()
}


fun addIntroduceChaosSubProcess(
    wip: AbstractFlowNodeBuilder<*, *>,
    experiment: Experiment
): AbstractFlowNodeBuilder<*, *> {
    var result: AbstractFlowNodeBuilder<*, *> =
        wip.subProcess(ID_INTRODUCE_CHAOS).name("introduce chaos").embeddedSubProcess()
            .startEvent()

    for (method in experiment.method) {
        val id = "UUID-" + UUID.randomUUID()
            .toString() //must start with a character, or validtion will fail
        result = result.serviceTask(id).name(method.name).zeebeJobType(method.provider.path)

        val timeout = method.provider.timeout
        if (timeout > 0) {
            result =
                result.boundaryEvent().name("Timeout")
                    .timerWithDuration(Duration.ofSeconds(timeout).toString()).endEvent().error(
                        ERROR_CODE_FAILED_ACTION
                    ).name(NAME_CHAOS_EXPERIMENT_FAILED).moveToNode(
                        id
                    )
        }
    }

    return result.endEvent()
        .subProcessDone()
}

fun addBoundaryErrorEvents(wip: AbstractFlowNodeBuilder<*, *>): AbstractFlowNodeBuilder<*, *> {
    var result = wip.moveToNode(ID_STEADY_STATE_AFTER)

    result =
        (result as SubProcessBuilder).boundaryEvent(ID_STEADY_STATE_AFTER + SUFFIX_ERROR)
            .name(NAME_CHAOS_EXPERIMENT_FAILED)
            .error("failedAction").endEvent(ID_END_FAILURE)
            .name("Chaos Experiment tFailed")

    result = result.moveToNode(ID_INTRODUCE_CHAOS)

    result = (result as SubProcessBuilder)
        .boundaryEvent(ID_INTRODUCE_CHAOS + SUFFIX_ERROR).name(NAME_CHAOS_EXPERIMENT_FAILED)
        .error("failedAction").connectTo(ID_END_FAILURE)

    result = result.moveToNode(ID_STEADY_STATE_BEFORE)

    result = (result as SubProcessBuilder)
        .boundaryEvent(ID_STEADY_STATE_BEFORE + SUFFIX_ERROR).name(NAME_CHAOS_EXPERIMENT_FAILED)
        .error("failedAction").connectTo(ID_END_FAILURE)

    return result
}

