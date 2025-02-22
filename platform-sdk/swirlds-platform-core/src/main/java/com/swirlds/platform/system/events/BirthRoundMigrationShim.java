/*
 * Copyright (C) 2024 Hedera Hashgraph, LLC
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

package com.swirlds.platform.system.events;

import static com.swirlds.logging.legacy.LogMarker.STARTUP;
import static com.swirlds.platform.consensus.ConsensusConstants.ROUND_FIRST;

import com.swirlds.common.context.PlatformContext;
import com.swirlds.common.metrics.SpeedometerMetric;
import com.swirlds.common.utility.CompareTo;
import com.swirlds.platform.event.GossipEvent;
import com.swirlds.platform.system.SoftwareVersion;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Performs special migration on events during the birth round migration pathway.
 */
public class BirthRoundMigrationShim {

    private static final SpeedometerMetric.Config SHIM_ANCIENT_EVENTS = new SpeedometerMetric.Config(
                    "platform", "shimAncientEvents")
            .withDescription("Events that the BirthRoundMigrationShim gave an ancient birth round override")
            .withUnit("hz");

    private final SpeedometerMetric shimAncientEvents;

    private static final SpeedometerMetric.Config SHIM_BARELY_NON_ANCIENT_EVENTS = new SpeedometerMetric.Config(
                    "platform", "shimBarelyNonAncientEvents")
            .withDescription("Events that the BirthRoundMigrationShim gave a barely non-ancient birth round override")
            .withUnit("hz");

    private final SpeedometerMetric shimBarelyNonAncientEvents;

    private static final Logger logger = LogManager.getLogger(BirthRoundMigrationShim.class);

    /**
     * The first software version where the birth round mode is enabled. Events from this software version and later are
     * not modified by this object. Events from earlier software versions have their birth rounds modified by this
     * object.
     */
    private final SoftwareVersion firstVersionInBirthRoundMode;

    /**
     * The last round before the birth round mode was enabled.
     */
    private final long lastRoundBeforeBirthRoundMode;

    /**
     * The lowest judge generation before the birth round mode was enabled.
     */
    private final long lowestJudgeGenerationBeforeBirthRoundMode;

    /**
     * Constructs a new BirthRoundMigrationShim.
     *
     * @param platformContext                           the platform context
     * @param firstVersionInBirthRoundMode              the first software version where the birth round mode is
     *                                                  enabled
     * @param lastRoundBeforeBirthRoundMode             the last round before the birth round mode was enabled
     * @param lowestJudgeGenerationBeforeBirthRoundMode the lowest judge generation before the birth round mode was
     *                                                  enabled
     */
    public BirthRoundMigrationShim(
            @NonNull final PlatformContext platformContext,
            @NonNull final SoftwareVersion firstVersionInBirthRoundMode,
            final long lastRoundBeforeBirthRoundMode,
            final long lowestJudgeGenerationBeforeBirthRoundMode) {

        logger.info(
                STARTUP.getMarker(),
                "BirthRoundMigrationShim initialized with firstVersionInBirthRoundMode={}, "
                        + "lastRoundBeforeBirthRoundMode={}, lowestJudgeGenerationBeforeBirthRoundMode={}",
                firstVersionInBirthRoundMode,
                lastRoundBeforeBirthRoundMode,
                lowestJudgeGenerationBeforeBirthRoundMode);

        this.firstVersionInBirthRoundMode = firstVersionInBirthRoundMode;
        this.lastRoundBeforeBirthRoundMode = lastRoundBeforeBirthRoundMode;
        this.lowestJudgeGenerationBeforeBirthRoundMode = lowestJudgeGenerationBeforeBirthRoundMode;

        shimAncientEvents = platformContext.getMetrics().getOrCreate(SHIM_ANCIENT_EVENTS);
        shimBarelyNonAncientEvents = platformContext.getMetrics().getOrCreate(SHIM_BARELY_NON_ANCIENT_EVENTS);
    }

    /**
     * Migrate an event's birth round, if needed.
     *
     * @param event the event to migrate
     * @return the migrated event
     */
    @NonNull
    public GossipEvent migrateEvent(@NonNull final GossipEvent event) {
        if (CompareTo.isLessThan(event.getHashedData().getSoftwareVersion(), firstVersionInBirthRoundMode)) {
            // The event was created before the birth round mode was enabled.
            // We need to migrate the event's birth round.

            if (event.getGeneration() >= lowestJudgeGenerationBeforeBirthRoundMode) {
                // Any event with a generation greater than or equal to the lowest pre-migration judge generation
                // is given a birth round that will be non-ancient at migration time.
                event.getHashedData().setBirthRoundOverride(lastRoundBeforeBirthRoundMode);
                shimBarelyNonAncientEvents.cycle();
            } else {
                // All other pre-migration events are given a birth round that will
                // cause them to be immediately ancient.
                event.getHashedData().setBirthRoundOverride(ROUND_FIRST);
                shimAncientEvents.cycle();
            }
        }

        return event;
    }
}
