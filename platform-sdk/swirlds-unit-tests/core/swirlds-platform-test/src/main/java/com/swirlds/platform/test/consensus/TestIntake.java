/*
 * Copyright (C) 2022-2024 Hedera Hashgraph, LLC
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

package com.swirlds.platform.test.consensus;

import static com.swirlds.common.wiring.wires.SolderType.INJECT;
import static com.swirlds.platform.consensus.ConsensusConstants.ROUND_FIRST;
import static com.swirlds.platform.event.AncientMode.GENERATION_THRESHOLD;
import static org.mockito.Mockito.mock;

import com.swirlds.base.time.Time;
import com.swirlds.common.context.PlatformContext;
import com.swirlds.common.platform.NodeId;
import com.swirlds.common.test.fixtures.platform.TestPlatformContextBuilder;
import com.swirlds.common.wiring.model.WiringModel;
import com.swirlds.common.wiring.schedulers.TaskScheduler;
import com.swirlds.common.wiring.schedulers.builders.TaskSchedulerType;
import com.swirlds.config.extensions.test.fixtures.TestConfigBuilder;
import com.swirlds.platform.Consensus;
import com.swirlds.platform.ConsensusImpl;
import com.swirlds.platform.components.ConsensusEngine;
import com.swirlds.platform.consensus.ConsensusConfig;
import com.swirlds.platform.consensus.ConsensusSnapshot;
import com.swirlds.platform.consensus.NonAncientEventWindow;
import com.swirlds.platform.event.GossipEvent;
import com.swirlds.platform.event.hashing.EventHasher;
import com.swirlds.platform.event.linking.InOrderLinker;
import com.swirlds.platform.event.orphan.OrphanBuffer;
import com.swirlds.platform.gossip.IntakeEventCounter;
import com.swirlds.platform.gossip.NoOpIntakeEventCounter;
import com.swirlds.platform.gossip.shadowgraph.Shadowgraph;
import com.swirlds.platform.internal.ConsensusRound;
import com.swirlds.platform.internal.EventImpl;
import com.swirlds.platform.state.signed.LoadableFromSignedState;
import com.swirlds.platform.state.signed.SignedState;
import com.swirlds.platform.system.address.AddressBook;
import com.swirlds.platform.test.consensus.framework.ConsensusOutput;
import com.swirlds.platform.test.fixtures.event.IndexedEvent;
import com.swirlds.platform.wiring.ConsensusEngineWiring;
import com.swirlds.platform.wiring.InOrderLinkerWiring;
import com.swirlds.platform.wiring.OrphanBufferWiring;
import com.swirlds.platform.wiring.components.EventHasherWiring;
import com.swirlds.platform.wiring.components.EventWindowManagerWiring;
import com.swirlds.platform.wiring.components.PostHashCollectorWiring;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * Event intake with consensus and shadowgraph, used for testing
 */
public class TestIntake implements LoadableFromSignedState {
    private final ConsensusImpl consensus;
    private final Shadowgraph shadowGraph;
    private final ConsensusOutput output;

    private final EventHasherWiring hasherWiring;
    private final OrphanBufferWiring orphanBufferWiring;
    private final InOrderLinkerWiring linkerWiring;
    private final ConsensusEngineWiring consensusEngineWiring;
    private final WiringModel model;

    /**
     * @param addressBook the address book used by this intake
     */
    public TestIntake(@NonNull final AddressBook addressBook, @NonNull final ConsensusConfig consensusConfig) {
        final NodeId selfId = new NodeId(0);

        final Time time = Time.getCurrent();
        output = new ConsensusOutput(time);

        // FUTURE WORK: Broaden this test sweet to include testing ancient threshold via birth round.
        consensus = new ConsensusImpl(
                consensusConfig, ConsensusUtils.NOOP_CONSENSUS_METRICS, addressBook, GENERATION_THRESHOLD);

        final PlatformContext platformContext = TestPlatformContextBuilder.create()
                .withConfiguration(new TestConfigBuilder().getOrCreateConfig())
                .build();

        shadowGraph = new Shadowgraph(platformContext, mock(AddressBook.class));

        model = WiringModel.create(platformContext, time, mock(ForkJoinPool.class));

        final EventHasher eventHasher = new EventHasher(platformContext);
        hasherWiring = EventHasherWiring.create(directScheduler("eventHasher"));
        hasherWiring.bind(eventHasher);

        final PostHashCollectorWiring postHashCollectorWiring =
                PostHashCollectorWiring.create(directScheduler("postHashCollector"));

        final IntakeEventCounter intakeEventCounter = new NoOpIntakeEventCounter();
        final OrphanBuffer orphanBuffer = new OrphanBuffer(platformContext, intakeEventCounter);
        orphanBufferWiring = OrphanBufferWiring.create(directScheduler("orphanBuffer"));
        orphanBufferWiring.bind(orphanBuffer);

        final InOrderLinker linker = new InOrderLinker(platformContext, time, intakeEventCounter);
        linkerWiring = InOrderLinkerWiring.create(directScheduler("linker"));
        linkerWiring.bind(linker);

        final ConsensusEngine consensusEngine = new ConsensusEngine(
                platformContext, selfId, () -> consensus, shadowGraph, intakeEventCounter, output::staleEvent);

        consensusEngineWiring = ConsensusEngineWiring.create(directScheduler("consensusEngine"));
        consensusEngineWiring.bind(consensusEngine);

        final EventWindowManagerWiring eventWindowManagerWiring = EventWindowManagerWiring.create(model);

        hasherWiring.eventOutput().solderTo(postHashCollectorWiring.eventInput());
        postHashCollectorWiring.eventOutput().solderTo(orphanBufferWiring.eventInput());
        orphanBufferWiring.eventOutput().solderTo(linkerWiring.eventInput());
        linkerWiring.eventOutput().solderTo("shadowgraph", "addEvent", shadowGraph::addEvent);
        linkerWiring.eventOutput().solderTo("output", "eventAdded", output::eventAdded);
        linkerWiring.eventOutput().solderTo(consensusEngineWiring.eventInput());

        consensusEngineWiring.consensusRoundOutput().solderTo(eventWindowManagerWiring.consensusRoundInput());
        consensusEngineWiring
                .consensusRoundOutput()
                .solderTo("consensusOutputTestTool", "round output", output::consensusRound);

        eventWindowManagerWiring
                .nonAncientEventWindowOutput()
                .solderTo(orphanBufferWiring.nonAncientEventWindowInput(), INJECT);
        eventWindowManagerWiring
                .nonAncientEventWindowOutput()
                .solderTo(linkerWiring.nonAncientEventWindowInput(), INJECT);

        model.start();
    }

    /**
     * Link an event to its parents and add it to consensus and shadowgraph
     *
     * @param event the event to add
     */
    public void addEvent(@NonNull final GossipEvent event) {
        hasherWiring.eventInput().put(event);
    }

    /**
     * Same as {@link #addEvent(GossipEvent)} but for a list of events
     */
    public void addEvents(@NonNull final List<IndexedEvent> events) {
        for (final IndexedEvent event : events) {
            addEvent(event.getBaseEvent());
        }
    }

    /**
     * Same as {@link #addEvent(GossipEvent)} but skips the linking and inserts this instance
     */
    public void addLinkedEvent(@NonNull final EventImpl event) {
        output.eventAdded(event);
        if (!consensus.isExpired(event.getBaseEvent())) {
            shadowGraph.addEvent(event);
        }
        consensusEngineWiring.eventInput().put(event);
    }

    /**
     * @return the consensus used by this intake
     */
    public @NonNull Consensus getConsensus() {
        return consensus;
    }

    /**
     * @return the shadowgraph used by this intake
     */
    public @NonNull Shadowgraph getShadowGraph() {
        return shadowGraph;
    }

    /**
     * @return a queue of all rounds that have reached consensus
     */
    public @NonNull Deque<ConsensusRound> getConsensusRounds() {
        return output.getConsensusRounds();
    }

    public @Nullable ConsensusRound getLatestRound() {
        return output.getConsensusRounds().pollLast();
    }

    @Override
    public void loadFromSignedState(@NonNull final SignedState signedState) {
        consensus.loadFromSignedState(signedState);
        shadowGraph.clear();
    }

    public void loadSnapshot(@NonNull final ConsensusSnapshot snapshot) {
        consensus.loadSnapshot(snapshot);

        // FUTURE WORK: remove the fourth variable setting useBirthRound to false when we switch from comparing
        // minGenNonAncient to comparing birthRound to minRoundNonAncient.  Until then, it is always false in
        // production.
        orphanBufferWiring
                .nonAncientEventWindowInput()
                .put(new NonAncientEventWindow(
                        consensus.getLastRoundDecided(),
                        consensus.getMinGenerationNonAncient(),
                        consensus.getMinRoundGeneration(),
                        GENERATION_THRESHOLD));
        linkerWiring
                .nonAncientEventWindowInput()
                .put(new NonAncientEventWindow(
                        consensus.getLastRoundDecided(),
                        consensus.getMinGenerationNonAncient(),
                        consensus.getMinRoundGeneration(),
                        GENERATION_THRESHOLD));

        shadowGraph.clear();

        final NonAncientEventWindow eventWindow = new NonAncientEventWindow(
                ROUND_FIRST,
                consensus.getMinGenerationNonAncient(),
                consensus.getMinGenerationNonAncient(),
                GENERATION_THRESHOLD);

        shadowGraph.startWithEventWindow(eventWindow);
    }

    public @NonNull ConsensusOutput getOutput() {
        return output;
    }

    public void reset() {
        consensus.reset();
        shadowGraph.clear();
        output.clear();
    }

    public <X> TaskScheduler<X> directScheduler(final String name) {
        return model.schedulerBuilder(name)
                .withType(TaskSchedulerType.DIRECT)
                .build()
                .cast();
    }
}
