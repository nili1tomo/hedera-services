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

package com.swirlds.platform.gui.internal;

import com.swirlds.platform.gossip.shadowgraph.Shadowgraph;
import com.swirlds.platform.gui.GuiPlatformAccessor;
import com.swirlds.platform.gui.hashgraph.internal.ShadowgraphGuiSource;
import com.swirlds.platform.system.Platform;
import com.swirlds.platform.system.address.AddressBook;

/**
 * A {@link ShadowgraphGuiSource} that retrieves the {@link Shadowgraph} from the platform that is being displayed by the
 * browser
 */
public class PlatformHashgraphGuiSource implements ShadowgraphGuiSource {

    @Override
    public AddressBook getAddressBook() {
        return getPlatform().getAddressBook();
    }

    @Override
    public boolean isReady() {
        return getShadowGraph() != null;
    }

    @Override
    public Shadowgraph getShadowGraph() {
        final Platform platform = getPlatform();
        if (platform == null) {
            return null;
        }
        return GuiPlatformAccessor.getInstance().getShadowGraph(platform.getSelfId());
    }

    private Platform getPlatform() {
        return WinBrowser.memberDisplayed.getPlatform();
    }
}
