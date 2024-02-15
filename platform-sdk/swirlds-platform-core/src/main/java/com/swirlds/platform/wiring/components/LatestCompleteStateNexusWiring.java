package com.swirlds.platform.wiring.components;

import com.swirlds.common.wiring.schedulers.TaskScheduler;
import com.swirlds.common.wiring.wires.input.BindableInputWire;
import com.swirlds.common.wiring.wires.input.InputWire;
import com.swirlds.platform.state.nexus.LatestCompleteStateNexus;
import com.swirlds.platform.state.signed.ReservedSignedState;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Wiring for a {@link LatestCompleteStateNexus} that holds the latest complete signed state.
 *
 * @param reservedSignedStateInput        the input wire for setting the latest complete signed state
 * @param incompleteStateRoundNumberInput the input wire for the round number of newly created incomplete states
 */
public record LatestCompleteStateNexusWiring(@NonNull InputWire<ReservedSignedState> reservedSignedStateInput,
                                             @NonNull InputWire<Long> incompleteStateRoundNumberInput) {
    /**
     * Create a new instance of this wiring.
     *
     * @param taskScheduler the task scheduler for this wiring object
     * @return the new wiring instance
     */
    public static LatestCompleteStateNexusWiring create(@NonNull final TaskScheduler<Void> taskScheduler) {
        return new LatestCompleteStateNexusWiring(
                taskScheduler.buildInputWire("reservedSignedState"),
                taskScheduler.buildInputWire("incompleteStateRoundNumber"));
    }

    /**
     * Bind the latest complete state nexus to this wiring.
     *
     * @param latestCompleteStateNexus the latest complete state nexus to bind
     */
    public void bind(@NonNull final LatestCompleteStateNexus latestCompleteStateNexus) {
        ((BindableInputWire<ReservedSignedState, Void>) reservedSignedStateInput).bind(latestCompleteStateNexus::setStateIfNewer);
        ((BindableInputWire<Long, Void>) incompleteStateRoundNumberInput).bind(latestCompleteStateNexus::newIncompleteState);
    }
}
