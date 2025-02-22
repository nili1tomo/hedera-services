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

package com.hedera.node.app.service.mono.state.virtual.utils;

/**
 * A functional interface that represents an action that accepts two arguments and
 * returns no result. It is similar to {@link java.util.function.BiConsumer}, but
 * may throw a checked exception.
 *
 * @param <T> first argument type
 * @param <U> second argument type
 * @param <E> checked exception type
 */
public interface ThrowingBiConsumer<T, U, E extends Exception> {

    /**
     * Performs an action on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @throws E if an error occurred
     */
    void accept(T t, U u) throws E;
}
