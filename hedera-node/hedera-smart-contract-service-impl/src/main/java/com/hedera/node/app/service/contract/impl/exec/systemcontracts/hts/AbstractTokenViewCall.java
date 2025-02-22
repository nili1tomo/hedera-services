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

package com.hedera.node.app.service.contract.impl.exec.systemcontracts.hts;

import static com.hedera.hapi.node.base.ResponseCodeEnum.INVALID_TOKEN_ID;
import static com.hedera.hapi.node.base.ResponseCodeEnum.SUCCESS;
import static com.hedera.node.app.service.contract.impl.exec.systemcontracts.hts.HtsCall.PricedResult.gasOnly;

import com.hedera.hapi.node.base.ResponseCodeEnum;
import com.hedera.hapi.node.state.token.Token;
import com.hedera.node.app.service.contract.impl.exec.gas.SystemContractGasCalculator;
import com.hedera.node.app.service.contract.impl.exec.systemcontracts.FullResult;
import com.hedera.node.app.service.contract.impl.hevm.HederaWorldUpdater;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

public abstract class AbstractTokenViewCall extends AbstractHtsCall {
    protected final Token token;

    protected AbstractTokenViewCall(
            @NonNull final SystemContractGasCalculator gasCalculator,
            @NonNull final HederaWorldUpdater.Enhancement enhancement,
            @Nullable final Token token) {
        super(gasCalculator, enhancement, true);
        this.token = token;
    }

    @Override
    public @NonNull PricedResult execute() {
        if (token == null) {
            return externalizeUnsuccessfulResult(INVALID_TOKEN_ID, gasCalculator.viewGasRequirement());
        } else {
            return externalizeSuccessfulResult();
        }
    }

    @Override
    public boolean allowsStaticFrame() {
        return true;
    }

    protected PricedResult externalizeSuccessfulResult() {
        return gasOnly(resultOfViewingToken(token), SUCCESS, true);
    }

    protected PricedResult externalizeUnsuccessfulResult(ResponseCodeEnum responseCode, long gasRequirement) {
        return gasOnly(viewCallResultWith(responseCode, gasRequirement), responseCode, true);
    }

    /**
     * Returns the result of viewing the given {@code token}.
     *
     * @param token the token to view
     * @return the result of viewing the given {@code token}
     */
    @NonNull
    protected abstract FullResult resultOfViewingToken(Token token);

    /**
     * Returns the result of viewing the given {@code token} given the {@code status}.
     * Currently, the only usage for this method is to return an INVALID_TOKEN_ID status
     * if the token is null.
     * @param status - ResponseCodeEnum status
     * @return the results to return to the caller
     */
    @NonNull
    protected abstract FullResult viewCallResultWith(@NonNull ResponseCodeEnum status, long gasRequirement);
}
