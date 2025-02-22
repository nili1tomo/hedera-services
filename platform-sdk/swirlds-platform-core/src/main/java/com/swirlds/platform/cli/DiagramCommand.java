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

package com.swirlds.platform.cli;

import static com.swirlds.common.threading.manager.AdHocThreadManager.getStaticThreadManager;

import com.swirlds.base.time.Time;
import com.swirlds.cli.PlatformCli;
import com.swirlds.cli.utility.AbstractCommand;
import com.swirlds.cli.utility.SubcommandOf;
import com.swirlds.common.context.DefaultPlatformContext;
import com.swirlds.common.context.PlatformContext;
import com.swirlds.common.crypto.CryptographyHolder;
import com.swirlds.common.metrics.noop.NoOpMetrics;
import com.swirlds.common.notification.NotificationEngine;
import com.swirlds.common.threading.manager.ThreadManager;
import com.swirlds.common.wiring.model.ModelEdgeSubstitution;
import com.swirlds.common.wiring.model.ModelGroup;
import com.swirlds.common.wiring.model.ModelManualLink;
import com.swirlds.config.api.Configuration;
import com.swirlds.platform.config.DefaultConfiguration;
import com.swirlds.platform.config.StateConfig;
import com.swirlds.platform.eventhandling.TransactionPool;
import com.swirlds.platform.state.nexus.LatestCompleteStateNexus;
import com.swirlds.platform.system.status.PlatformStatusManager;
import com.swirlds.platform.wiring.PlatformWiring;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import picocli.CommandLine;

@CommandLine.Command(
        name = "diagram",
        mixinStandardHelpOptions = true,
        description = "Generate a mermaid style diagram of platform wiring.")
@SubcommandOf(PlatformCli.class)
public final class DiagramCommand extends AbstractCommand {

    private List<String> groupStrings = List.of();
    private Set<String> collapsedGroups = Set.of();
    private List<String> substitutionStrings = List.of();
    private List<String> manualLinks = List.of();

    private DiagramCommand() {}

    @CommandLine.Option(
            names = {"-g", "--group"},
            description = "Specify an un-collapsed grouping. Format is 'GROUP_NAME:COMPONENT_NAME[,COMPONENT_NAME]*'.")
    private void setGroupStrings(@NonNull final List<String> groupStrings) {
        this.groupStrings = groupStrings;
    }

    @CommandLine.Option(
            names = {"-c", "--collapse"},
            description = "Specify the name of a group that should be collapsed.")
    private void setCollapsedGroups(@NonNull final Set<String> collapsedGroups) {
        this.collapsedGroups = collapsedGroups;
    }

    @CommandLine.Option(
            names = {"-s", "--substitute"},
            description = "Substitute a type of edge with a symbol. Useful for spammy edges. "
                    + "Format: SOURCE_COMPONENT:EDGE_DESCRIPTION:SYMBOL")
    private void setCollapsedGroups(@NonNull final List<String> substitutionStrings) {
        this.substitutionStrings = substitutionStrings;
    }

    @CommandLine.Option(
            names = {"-l", "--link"},
            description = "Manually link two components in the diagram. "
                    + "Format: SOURCE_COMPONENT:EDGE_LABEL:TARGET_COMPONENT")
    private void setManualLinks(@NonNull final List<String> manualLinks) {
        this.manualLinks = manualLinks;
    }

    /**
     * Entry point.
     */
    @Override
    public Integer call() throws IOException {
        final Configuration configuration = DefaultConfiguration.buildBasicConfiguration();
        final PlatformContext platformContext = new DefaultPlatformContext(
                configuration, new NoOpMetrics(), CryptographyHolder.get(), Time.getCurrent());

        final PlatformWiring platformWiring = new PlatformWiring(platformContext);

        final ThreadManager threadManager = getStaticThreadManager();
        final NotificationEngine notificationEngine = NotificationEngine.buildEngine(threadManager);
        platformWiring.wireExternalComponents(
                new PlatformStatusManager(platformContext, platformContext.getTime(), threadManager, a -> {}),
                new TransactionPool(platformContext),
                new LatestCompleteStateNexus(
                        platformContext.getConfiguration().getConfigData(StateConfig.class),
                        platformContext.getMetrics()),
                notificationEngine);

        final String diagramString = platformWiring
                .getModel()
                .generateWiringDiagram(parseGroups(), parseSubstitutions(), parseManualLinks());
        final String encodedDiagramString = Base64.getEncoder().encodeToString(diagramString.getBytes());

        final String editorUrl = "https://mermaid.ink/svg/" + encodedDiagramString + "?bgColor=e8e8e8";

        System.out.println(diagramString);
        System.out.println();
        System.out.println(editorUrl);
        return 0;
    }

    /**
     * Parse groups from the command line arguments.
     *
     * @return a list of zero or more groups
     */
    @NonNull
    private List<ModelGroup> parseGroups() {
        final List<ModelGroup> groups = new ArrayList<>();

        for (final String group : groupStrings) {
            final String[] parts = group.split(":");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid group string: " + group);
            }
            final String groupName = parts[0];
            final String[] elements = parts[1].split(",");
            groups.add(new ModelGroup(groupName, Set.of(elements), collapsedGroups.contains(groupName)));
        }

        return groups;
    }

    /**
     * Parse substitutions from the command line arguments.
     *
     * @return a list of zero or more substitutions
     */
    @NonNull
    private List<ModelEdgeSubstitution> parseSubstitutions() {
        final List<ModelEdgeSubstitution> substitutions = new ArrayList<>();
        for (final String substitutionString : substitutionStrings) {
            final String[] parts = substitutionString.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid substitution string: " + substitutionString);
            }
            final String sourceComponent = parts[0];
            final String edgeDescription = parts[1];
            final String symbol = parts[2];
            substitutions.add(new ModelEdgeSubstitution(sourceComponent, edgeDescription, symbol));
        }

        return substitutions;
    }

    /**
     * Parse manual links from the command line arguments.
     *
     * @return a list of zero or more manual links
     */
    @NonNull
    private List<ModelManualLink> parseManualLinks() {
        final List<ModelManualLink> links = new ArrayList<>();
        for (final String linkString : manualLinks) {
            final String[] parts = linkString.split(":");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid link string: " + linkString);
            }
            final String sourceComponent = parts[0];
            final String edgeLabel = parts[1];
            final String targetComponent = parts[2];
            links.add(new ModelManualLink(sourceComponent, edgeLabel, targetComponent));
        }

        return links;
    }
}
