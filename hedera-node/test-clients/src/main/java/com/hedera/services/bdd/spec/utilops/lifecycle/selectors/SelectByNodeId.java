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

package com.hedera.services.bdd.spec.utilops.lifecycle.selectors;

import com.hedera.services.bdd.junit.HapiTestNode;
import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Selects a node based on it's nodeId, like 0, 1, 2, 3.
 */
public class SelectByNodeId implements NodeSelector {
    private final long nodeId;

    public SelectByNodeId(final long nodeId) {
        if (nodeId < 0) {
            throw new IllegalArgumentException("Node IDs are non-negative. Cannot be " + nodeId);
        }
        this.nodeId = nodeId;
    }

    @Override
    public boolean test(@NonNull final HapiTestNode hapiTestNode) {
        return hapiTestNode.getId() == nodeId;
    }

    @Override
    public String toString() {
        return "by nodeId '" + nodeId + "'";
    }
}
