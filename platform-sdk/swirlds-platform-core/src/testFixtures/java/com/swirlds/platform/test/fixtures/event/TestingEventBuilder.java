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

package com.swirlds.platform.test.fixtures.event;

import com.swirlds.common.crypto.SignatureType;
import com.swirlds.common.platform.NodeId;
import com.swirlds.common.test.fixtures.RandomUtils;
import com.swirlds.platform.event.GossipEvent;
import com.swirlds.platform.internal.EventImpl;
import com.swirlds.platform.system.BasicSoftwareVersion;
import com.swirlds.platform.system.events.BaseEventHashedData;
import com.swirlds.platform.system.events.BaseEventUnhashedData;
import com.swirlds.platform.system.events.EventConstants;
import com.swirlds.platform.system.events.EventDescriptor;
import com.swirlds.platform.system.transaction.ConsensusTransactionImpl;
import com.swirlds.platform.system.transaction.SwirldTransaction;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.time.Instant;
import java.util.Collections;
import java.util.Random;

/**
 * A builder for creating event instances for testing purposes.
 */
public class TestingEventBuilder {
    /** default timestamp to use when none is set */
    private static final Instant DEFAULT_TIMESTAMP = Instant.ofEpochMilli(1588771316678L);
    /** source of randomness */
    private Random random;
    /** creator ID to use */
    private NodeId creatorId;
    /** the time created of an event */
    private Instant timeCreated;
    /** the number of transactions an event should contain */
    private int numberOfTransactions;
    /** the transaction size */
    private int transactionSize;
    /** the transactions of an event */
    private ConsensusTransactionImpl[] transactions;
    /** the event's self-parent, could be a GossipEvent or EventImpl */
    private Object selfParent;
    /** the event's other-parent, could be a GossipEvent or EventImpl */
    private Object otherParent;
    /** a fake generation to set for an event */
    private Long fakeGeneration;
    /** the event's consensus status */
    private boolean consensus;

    private TestingEventBuilder() {}

    /**
     * @return a new instance of the builder with default settings
     */
    public static @NonNull TestingEventBuilder builder() {
        return new TestingEventBuilder().setDefaults();
    }

    /**
     * Set the builder to its default settings.
     *
     * @return this instance
     */
    public @NonNull TestingEventBuilder setDefaults() {
        random = new Random();
        creatorId = new NodeId(0);
        timeCreated = null;
        numberOfTransactions = 2;
        transactionSize = 4;
        transactions = null;
        selfParent = null;
        otherParent = null;
        fakeGeneration = null;
        consensus = false;
        return this;
    }

    /**
     * Same as {@link #setDefaults()}.
     */
    public @NonNull TestingEventBuilder reset() {
        return setDefaults();
    }

    /**
     * Set the random number generator to use. NOTE: this will override any seed set with {@link #setSeed(long)}.
     *
     * @param random the random number generator
     * @return this instance
     */
    public @NonNull TestingEventBuilder setRandom(@NonNull final Random random) {
        this.random = random;
        return this;
    }

    /**
     * Set the seed to use for the random number generator. NOTE: this will be overridden if a random number generator
     * is set with {@link #setRandom(Random)}.
     *
     * @param seed the seed
     * @return this instance
     */
    public @NonNull TestingEventBuilder setSeed(final long seed) {
        this.random = new Random(seed);
        return this;
    }

    /**
     * Set the creator ID to use.
     *
     * @param creatorId the creator ID
     * @return this instance
     */
    public @NonNull TestingEventBuilder setCreatorId(final long creatorId) {
        this.creatorId = new NodeId(creatorId);
        return this;
    }

    /**
     * Set the creator ID to use. If set to null, the default creator ID will be used.
     *
     * @param creatorId the creator ID
     * @return this instance
     */
    public @NonNull TestingEventBuilder setCreatorId(@Nullable final NodeId creatorId) {
        this.creatorId = creatorId;
        return this;
    }

    /**
     * Set the time created of an event. If set to null, the default time created will be used.
     *
     * @param timeCreated the time created
     * @return this instance
     */
    public @NonNull TestingEventBuilder setTimeCreated(@Nullable final Instant timeCreated) {
        this.timeCreated = timeCreated;
        return this;
    }

    /**
     * Set the number of transactions an event should contain. If {@link #setTransactions(ConsensusTransactionImpl[])}
     * is called with a non-null value, this setting will be ignored.
     *
     * @param numberOfTransactions the number of transactions
     * @return this instance
     */
    public @NonNull TestingEventBuilder setNumberOfTransactions(final int numberOfTransactions) {
        this.numberOfTransactions = numberOfTransactions;
        return this;
    }

    /**
     * Set the transaction size. If {@link #setTransactions(ConsensusTransactionImpl[])} is called with a non-null
     * value, this setting will be ignored.
     *
     * @param transactionSize the transaction size
     * @return this instance
     */
    public @NonNull TestingEventBuilder setTransactionSize(final int transactionSize) {
        this.transactionSize = transactionSize;
        return this;
    }

    /**
     * Set the transactions of an event. If set to null, transactions will be generated based on setting set with
     * {@link #setNumberOfTransactions(int)} and {@link #setTransactionSize(int)}.
     *
     * @param transactions the transactions
     * @return this instance
     */
    public @NonNull TestingEventBuilder setTransactions(@Nullable final ConsensusTransactionImpl[] transactions) {
        this.transactions = transactions;
        return this;
    }

    /**
     * Set the self-parent of an event. If set to null, no self-parent will be used unless a generation is set, in which
     * case, a self-parent descriptor will be created with the given generation.
     *
     * @param selfParent the self-parent
     * @return  this instance
     */
    public @NonNull TestingEventBuilder setSelfParent(@Nullable final GossipEvent selfParent) {
        this.selfParent = selfParent;
        return this;
    }

    /**
     * Set the other-parent of an event. If set to null, no other-parent will be used unless a generation is set, in which
     * case, a other-parent descriptor will be created with the given generation.
     *
     * @param otherParent the other-parent
     * @return  this instance
     */
    public @NonNull TestingEventBuilder setOtherParent(@Nullable final GossipEvent otherParent) {
        this.otherParent = otherParent;
        return this;
    }

    /**
     * Same as {@link #setSelfParent(GossipEvent)} but with a different event type.
     */
    public @NonNull TestingEventBuilder setSelfParent(@Nullable final EventImpl selfParent) {
        this.selfParent = selfParent;
        return this;
    }

    /**
     * Same as {@link #setOtherParent(GossipEvent)} but with a different event type.
     */
    public @NonNull TestingEventBuilder setOtherParent(@Nullable final EventImpl otherParent) {
        this.otherParent = otherParent;
        return this;
    }

    /**
     * Set a generation to set for an event. Since the generation is based on the parents generation, this functionality
     * creates parent descriptors for an event to get the desired generation. It does not work in conjunction with
     * setting the parents for an event. If parents are set, then the generation will be based on the parents provided.
     *
     * @param generation the generation
     * @return this instance
     */
    public @NonNull TestingEventBuilder setGeneration(final Long generation) {
        fakeGeneration = generation;
        return this;
    }

    /**
     * Set the consensus status of an event. This is only applicable when building an EventImpl.
     *
     * @param consensus the consensus status
     * @return this instance
     */
    public @NonNull TestingEventBuilder setConsensus(final boolean consensus) {
        this.consensus = consensus;
        return this;
    }

    private @Nullable EventImpl getSelfParentImpl() {
        return selfParent instanceof final EventImpl ei ? ei : null;
    }

    private @Nullable EventImpl getOtherParentImpl() {
        return otherParent instanceof final EventImpl ei ? ei : null;
    }

    private @Nullable GossipEvent getSelfParentGossip() {
        if (selfParent instanceof final GossipEvent ge) {
            return ge;
        }
        return selfParent instanceof final EventImpl ei ? ei.getBaseEvent() : null;
    }

    private @Nullable GossipEvent getOtherParentGossip() {
        if (otherParent instanceof final GossipEvent ge) {
            return ge;
        }
        return otherParent instanceof final EventImpl ei ? ei.getBaseEvent() : null;
    }

    private @NonNull Instant getParentTime() {
        final Instant sp = getSelfParentGossip() == null
                ? DEFAULT_TIMESTAMP
                : getSelfParentGossip().getHashedData().getTimeCreated();
        final Instant op = getOtherParentGossip() == null
                ? DEFAULT_TIMESTAMP
                : getOtherParentGossip().getHashedData().getTimeCreated();
        return sp.isAfter(op) ? sp : op;
    }

    /**
     * Same as {@link #buildGossipEvent()}
     */
    public @NonNull GossipEvent buildEvent() {
        return buildGossipEvent();
    }

    /**
     * Build a GossipEvent instance.
     *
     * @return the GossipEvent instance
     */
    public @NonNull GossipEvent buildGossipEvent() {
        final ConsensusTransactionImpl[] tr;
        if (transactions == null) {
            tr = new ConsensusTransactionImpl[numberOfTransactions];
            for (int i = 0; i < tr.length; ++i) {
                final byte[] bytes = new byte[transactionSize];
                random.nextBytes(bytes);
                tr[i] = new SwirldTransaction(bytes);
            }
        } else {
            tr = transactions;
        }

        final long selfParentGen = fakeGeneration != null
                ? fakeGeneration - 1
                : getSelfParentGossip() != null
                        ? getSelfParentGossip().getGeneration()
                        : EventConstants.GENERATION_UNDEFINED;
        final long otherParentGen = fakeGeneration != null
                ? fakeGeneration - 1
                : getOtherParentGossip() != null
                        ? getOtherParentGossip().getGeneration()
                        : EventConstants.GENERATION_UNDEFINED;

        final EventDescriptor selfParent = getSelfParentGossip() != null
                ? new EventDescriptor(
                        getSelfParentGossip().getHashedData().getHash(),
                        creatorId,
                        selfParentGen,
                        EventConstants.BIRTH_ROUND_UNDEFINED)
                : selfParentGen > EventConstants.GENERATION_UNDEFINED
                        ? new EventDescriptor(
                                RandomUtils.randomHash(random),
                                creatorId,
                                selfParentGen,
                                EventConstants.BIRTH_ROUND_UNDEFINED)
                        : null;
        final EventDescriptor otherParent = getOtherParentGossip() != null
                ? new EventDescriptor(
                        getOtherParentGossip().getHashedData().getHash(),
                        getOtherParentGossip().getHashedData().getCreatorId(),
                        otherParentGen,
                        EventConstants.BIRTH_ROUND_UNDEFINED)
                : otherParentGen > EventConstants.GENERATION_UNDEFINED
                        ? new EventDescriptor(
                                RandomUtils.randomHash(random),
                                creatorId,
                                otherParentGen,
                                EventConstants.BIRTH_ROUND_UNDEFINED)
                        : null;
        final BaseEventHashedData hashedData = new BaseEventHashedData(
                new BasicSoftwareVersion(1),
                creatorId,
                selfParent,
                otherParent == null ? Collections.emptyList() : Collections.singletonList(otherParent),
                EventConstants.BIRTH_ROUND_UNDEFINED,
                timeCreated == null ? getParentTime().plusMillis(1 + creatorId.id()) : timeCreated,
                tr);

        hashedData.setHash(RandomUtils.randomHash(random));

        final byte[] sig = new byte[SignatureType.RSA.signatureLength()];
        random.nextBytes(sig);

        final BaseEventUnhashedData unhashedData = new BaseEventUnhashedData(
                getOtherParentGossip() == null
                        ? null
                        : getOtherParentGossip().getHashedData().getCreatorId(),
                sig);
        final GossipEvent gossipEvent = new GossipEvent(hashedData, unhashedData);
        gossipEvent.buildDescriptor();
        return gossipEvent;
    }

    /**
     * Build an EventImpl instance.
     *
     * @return the EventImpl instance
     */
    public @NonNull EventImpl buildEventImpl() {
        final EventImpl event = new EventImpl(buildGossipEvent(), getSelfParentImpl(), getOtherParentImpl());
        event.setConsensus(consensus);
        return event;
    }
}
