package com.swirlds.platform.wiring.components;

import com.swirlds.common.wiring.schedulers.TaskScheduler;
import com.swirlds.common.wiring.wires.input.BindableInputWire;
import com.swirlds.common.wiring.wires.input.InputWire;
import com.swirlds.platform.state.nexus.SignedStateNexus;
import com.swirlds.platform.state.signed.ReservedSignedState;
import com.swirlds.platform.wiring.ClearTrigger;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Wiring for a {@link SignedStateNexus} that holds the latest immutable signed state.
 *
 * @param setStateInput the input wire for setting the latest immutable signed state
 * @param clearInput    the input wire for clearing the latest immutable signed state
 */
public record LatestImmutableStateNexusWiring(@NonNull InputWire<ReservedSignedState> setStateInput,
                                              @NonNull InputWire<ClearTrigger> clearInput) {

    /**
     * Create a new instance of this wiring.
     *
     * @param taskScheduler the task scheduler for this wiring object
     * @return the new wiring instance
     */
    public static LatestImmutableStateNexusWiring create(@NonNull final TaskScheduler<Void> taskScheduler) {
        return new LatestImmutableStateNexusWiring(taskScheduler.buildInputWire("setState"),
                taskScheduler.buildInputWire("clear"));
    }

    /**
     * Bind the latest immutable state nexus to this wiring.
     *
     * @param nexus the latest immutable state nexus to bind
     */
    public void bind(@NonNull final SignedStateNexus nexus) {
        ((BindableInputWire<ReservedSignedState, Void>) setStateInput).bind(nexus::setState);
        ((BindableInputWire<ClearTrigger, Void>) clearInput).bind(nexus::clearState);
    }
}
