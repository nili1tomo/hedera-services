/*
 * Copyright (C) 2016-2024 Hedera Hashgraph, LLC
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

package com.swirlds.platform.components.common.query;

import com.swirlds.platform.system.transaction.SystemTransaction;

/**
 * An object or method that submits a non-priority system transaction to the transaction pool for inclusion in an event.
 * Non-priority system transactions are included in events only if there is room after priority events have been
 * included.
 */
@FunctionalInterface
public interface SystemTransactionSubmitter {

    /**
     * Submits a system transaction for inclusion in an event.
     *
     * @param systemTransaction
     * 		the system transaction to submit
     * @return {@code true}	if the transaction was successfully submitted, {@code false} otherwise
     */
    boolean submit(SystemTransaction systemTransaction);
}
