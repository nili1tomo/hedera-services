/*
 * Copyright (C) 2023-2024 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.swirlds.platform.state.nexus;

import com.swirlds.common.metrics.RunningAverageMetric;
import com.swirlds.metrics.api.Metrics;
import com.swirlds.platform.config.StateConfig;
import com.swirlds.platform.consensus.ConsensusConstants;
import com.swirlds.platform.state.signed.ReservedSignedState;
import com.swirlds.platform.wiring.ClearTrigger;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.util.Objects;

import static com.swirlds.metrics.api.Metrics.PLATFORM_CATEGORY;

/**
 * A nexus that holds the latest complete signed state.
 */
public class LatestCompleteStateNexus {
    private static final RunningAverageMetric.Config AVG_ROUND_SUPERMAJORITY_CONFIG = new RunningAverageMetric.Config(
            PLATFORM_CATEGORY, "roundSup")
            .withDescription("latest round with state signed by a supermajority")
            .withUnit("round");

    private final StateConfig stateConfig;
    private ReservedSignedState currentState;

    /**
     * Create a new nexus that holds the latest complete signed state.
     *
     * @param stateConfig the state configuration
     * @param metrics     the metrics object to update
     */
    public LatestCompleteStateNexus(@NonNull final StateConfig stateConfig, @NonNull final Metrics metrics) {
        this.stateConfig = Objects.requireNonNull(stateConfig);
        Objects.requireNonNull(metrics);

        final RunningAverageMetric avgRoundSupermajority = metrics.getOrCreate(AVG_ROUND_SUPERMAJORITY_CONFIG);
        metrics.addUpdater(() -> avgRoundSupermajority.update(getRound()));
    }

    /**
     * Replace the current state with the given state if the given state is newer than the current state.
     *
     * @param reservedSignedState the new state
     */
    public synchronized void setStateIfNewer(@NonNull final ReservedSignedState reservedSignedState) {
        if (reservedSignedState.isNotNull()
                && getRound() < reservedSignedState.get().getRound()) {

            if (currentState != null) {
                currentState.close();
            }
            currentState = reservedSignedState;
        } else {
            reservedSignedState.close();
        }
    }

    /**
     * Notify the nexus that a new signed state has been created. This is useful for the nexus to know when it should
     * clear the latest complete state. This is used so that we don't hold the latest complete state forever in case we
     * have trouble gathering signatures.
     *
     * @param newStateRound a new signed state round that is not yet complete
     */
    public synchronized void newIncompleteState(final long newStateRound) {
        // NOTE: This logic is duplicated in SignedStateManager, but will be removed from the signed state manager
        // once its refactor is done

        // Any state older than this is unconditionally removed, even if it is the latest
        final long earliestPermittedRound = newStateRound - stateConfig.roundsToKeepForSigning() + 1;

        // Is the latest complete round older than the earliest permitted round?
        if (getRound() < earliestPermittedRound) {
            // Yes, so remove it
            if (currentState != null) {
                currentState.close();
            }

            currentState = null;
        }
    }

    @Nullable
    public synchronized ReservedSignedState getState(@NonNull final String reason) {
        if (currentState == null) {
            return null;
        }
        return currentState.tryGetAndReserve(reason);
    }

    public synchronized long getRound() {
        if (currentState == null) {
            return ConsensusConstants.ROUND_UNDEFINED;
        }
        return currentState.get().getRound();
    }
}
