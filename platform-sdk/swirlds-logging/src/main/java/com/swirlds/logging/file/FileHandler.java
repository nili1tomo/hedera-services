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

package com.swirlds.logging.file;

import com.swirlds.config.api.Configuration;
import com.swirlds.logging.api.Level;
import com.swirlds.logging.api.extensions.event.LogEvent;
import com.swirlds.logging.api.extensions.handler.AbstractSyncedHandler;
import com.swirlds.logging.api.internal.format.LineBasedFormat;
import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * A file handler that writes log events to a file.
 * <p>
 * This handler use a {@link BufferedWriter} to write {@link LogEvent}s to a file.
 * You can configure the following properties:
 * <ul>
 *     <li>{@code file} - the {@link Path} of the file</li>
 *     <li>{@code append} - whether to append to the file or not</li>
 * </ul>
 *
 */
public class FileHandler extends AbstractSyncedHandler {

    private static final String FILE_NAME_PROPERTY = "%s.file";
    private static final String APPEND_PROPERTY = "%s.append";
    private static final String DEFAULT_FILE_NAME = "swirlds-log.log";
    private final BufferedWriter bufferedWriter;

    /**
     * Creates a new file handler.
     *
     * @param configKey     the configuration key
     * @param configuration the configuration
     */
    public FileHandler(@NonNull final String configKey, @NonNull final Configuration configuration) {
        super(configKey, configuration);

        final String propertyPrefix = PROPERTY_HANDLER.formatted(configKey);
        final Path filePath = Objects.requireNonNullElse(
                configuration.getValue(FILE_NAME_PROPERTY.formatted(propertyPrefix), Path.class, null),
                Path.of(DEFAULT_FILE_NAME));
        final boolean append = Objects.requireNonNullElse(
                configuration.getValue(APPEND_PROPERTY.formatted(propertyPrefix), Boolean.class, null), true);

        BufferedWriter bufferedWriter = null;
        try {
            if (!Files.exists(filePath) || Files.isWritable(filePath)) {
                if (append) {
                    bufferedWriter = Files.newBufferedWriter(
                            filePath,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.APPEND,
                            StandardOpenOption.WRITE,
                            StandardOpenOption.DSYNC);
                } else {
                    bufferedWriter = Files.newBufferedWriter(
                            filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.DSYNC);
                }
            } else {
                EMERGENCY_LOGGER.log(Level.ERROR, "Log file could not be created or written to");
            }
        } catch (final Exception exception) {
            EMERGENCY_LOGGER.log(Level.ERROR, "Failed to create FileHandler", exception);
        }
        this.bufferedWriter = bufferedWriter;
    }

    /**
     * Handles a log event by appending it to the file using the {@link LineBasedFormat}.
     *
     * @param event The log event to be printed.
     */
    @Override
    protected void handleEvent(@NonNull final LogEvent event) {
        if (bufferedWriter != null) {
            LineBasedFormat.print(bufferedWriter, event);
        }
    }

    @Override
    protected void handleStopAndFinalize() {
        super.handleStopAndFinalize();
        try {
            if (bufferedWriter != null) {
                bufferedWriter.flush();
                bufferedWriter.close();
            }
        } catch (final Exception exception) {
            EMERGENCY_LOGGER.log(Level.ERROR, "Failed to close file output stream", exception);
        }
    }
}
