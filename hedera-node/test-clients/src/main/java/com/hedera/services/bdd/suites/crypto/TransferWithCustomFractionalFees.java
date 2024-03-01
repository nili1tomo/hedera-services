/*
 * Copyright (C) 2021-2024 Hedera Hashgraph, LLC
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

package com.hedera.services.bdd.suites.crypto;

import static com.hedera.services.bdd.junit.TestTags.CRYPTO;
import static com.hedera.services.bdd.spec.HapiSpec.defaultHapiSpec;
import static com.hedera.services.bdd.spec.assertions.AccountDetailsAsserts.accountDetailsWith;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getAccountBalance;
import static com.hedera.services.bdd.spec.queries.QueryVerbs.getAccountDetails;
import static com.hedera.services.bdd.spec.transactions.TxnVerbs.*;
import static com.hedera.services.bdd.spec.transactions.token.CustomFeeSpecs.*;
import static com.hedera.services.bdd.spec.transactions.token.TokenMovement.*;

import com.hedera.services.bdd.junit.HapiTest;
import com.hedera.services.bdd.junit.HapiTestSuite;
import com.hedera.services.bdd.spec.HapiSpec;
import com.hedera.services.bdd.suites.HapiSuite;
import com.hederahashgraph.api.proto.java.ResponseCodeEnum;
import java.util.List;
import java.util.OptionalLong;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Tag;

@HapiTestSuite
@Tag(CRYPTO)
public class TransferWithCustomFractionalFees extends HapiSuite {
    private static final Logger log = LogManager.getLogger(TransferWithCustomFractionalFees.class);
    private final long tokenTotal = 1_000L;
    private final long numerator = 1L;
    private final long denominator = 10L;
    private final long minHtsFee = 2L;
    private final long maxHtsFee = 10L;

    private final String token = "withCustomFees";
    private final String hbarCollector = "hbarFee";
    private final String htsCollector = "denomFee";
    private final String tokenReceiver = "receiver";
    private final String tokenTreasury = "tokenTreasury";
    private final String spender = "spender";
    private final String tokenOwner = "tokenOwner";
    private final String alice = "alice";
    private final String bob = "bob";
    private final String carol = "carol";
    private final String adminKey = "admin";
    private final String feeScheduleKey = "feeScheduleKey";

    public static void main(String... args) {
        new TransferWithCustomFractionalFees().runSuiteAsync();
    }

    @Override
    public List<HapiSpec> getSpecsInSuite() {
        return List.of(new HapiSpec[] {
            transferWithFractionalCustomFeeNegativeMoreThenTen(),
            transferWithFractionalCustomFee(),
            transferWithFractionalCustomFeeBellowMinimumAmount(),
            transferWithFractionalCustomFeeAboveMaximumAmount(),
            transferWithFractionalCustomFeeNetOfTransfers(),
            transferWithFractionalCustomFeeNetOfTransfersBellowMinimumAmount(),
            transferWithFractionalCustomFeeNetOfTransfersAboveMaximumAmount(),
            transferWithFractionalCustomFeeAllowance(),
            transferWithFractionalCustomFeeNegativeNotEnoughAllowance(),
        });
    }

    @HapiTest
    public HapiSpec transferWithFractionalCustomFeeNegativeMoreThenTen() {
        return defaultHapiSpec("transferWithFractionalCustomFeeNegativeMoreThenTen")
                .given(
                        cryptoCreate(htsCollector).balance(ONE_HUNDRED_HBARS),
                        cryptoCreate(hbarCollector).balance(0L),
                        cryptoCreate(tokenReceiver),
                        cryptoCreate(tokenTreasury),
                        cryptoCreate(tokenOwner).balance(ONE_MILLION_HBARS))
                .when(tokenCreate(token)
                        .treasury(tokenTreasury)
                        .initialSupply(tokenTotal)
                        .payingWith(htsCollector)
                        .withCustom(fractionalFee(
                                numerator, denominator, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .withCustom(fractionalFee(
                                numerator, denominator + 1, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .withCustom(fractionalFee(
                                numerator, denominator + 2, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .withCustom(fractionalFee(
                                numerator, denominator + 3, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .withCustom(fractionalFee(
                                numerator, denominator + 4, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .withCustom(fractionalFee(
                                numerator, denominator + 5, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .withCustom(fractionalFee(
                                numerator, denominator + 6, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .withCustom(fractionalFee(
                                numerator, denominator + 7, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .withCustom(fractionalFee(
                                numerator, denominator + 8, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .withCustom(fractionalFee(
                                numerator, denominator + 9, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .withCustom(fractionalFee(
                                numerator, denominator + 10, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector))
                        .hasKnownStatus(ResponseCodeEnum.CUSTOM_FEES_LIST_TOO_LONG))
                .then();
    }

    @HapiTest
    public HapiSpec transferWithFractionalCustomFee() {
        return defaultHapiSpec("transferWithFractionalCustomFee")
                .given(
                        cryptoCreate(htsCollector).balance(ONE_HUNDRED_HBARS),
                        cryptoCreate(hbarCollector).balance(0L),
                        cryptoCreate(tokenReceiver),
                        cryptoCreate(tokenTreasury),
                        cryptoCreate(tokenOwner).balance(ONE_MILLION_HBARS),
                        tokenCreate(token)
                                .treasury(tokenTreasury)
                                .initialSupply(tokenTotal)
                                .payingWith(htsCollector)
                                .withCustom(fractionalFee(
                                        numerator, denominator, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector)),
                        tokenAssociate(tokenReceiver, token),
                        tokenAssociate(tokenOwner, token),
                        cryptoTransfer(moving(tokenTotal, token).between(tokenTreasury, tokenOwner)))
                .when(cryptoTransfer(moving(100L, token).between(tokenOwner, tokenReceiver))
                        .fee(ONE_HUNDRED_HBARS)
                        .payingWith(tokenOwner))
                .then(
                        getAccountBalance(tokenOwner).hasTokenBalance(token, 900L),
                        getAccountBalance(tokenReceiver).hasTokenBalance(token, 90L),
                        getAccountBalance(htsCollector).hasTokenBalance(token, 10L));
    }

    @HapiTest
    public HapiSpec transferWithFractionalCustomFeeBellowMinimumAmount() {
        return defaultHapiSpec("transferWithFractionalCustomFeeBellowMinimumAmount")
                .given(
                        cryptoCreate(htsCollector).balance(ONE_HUNDRED_HBARS),
                        cryptoCreate(hbarCollector).balance(0L),
                        cryptoCreate(tokenReceiver),
                        cryptoCreate(tokenTreasury),
                        cryptoCreate(tokenOwner).balance(ONE_MILLION_HBARS),
                        tokenCreate(token)
                                .treasury(tokenTreasury)
                                .initialSupply(tokenTotal)
                                .payingWith(htsCollector)
                                .withCustom(
                                        fractionalFee(numerator, denominator, 20L, OptionalLong.of(30L), htsCollector)),
                        tokenAssociate(tokenReceiver, token),
                        tokenAssociate(tokenOwner, token),
                        cryptoTransfer(moving(tokenTotal, token).between(tokenTreasury, tokenOwner)))
                .when(cryptoTransfer(moving(100L, token).between(tokenOwner, tokenReceiver))
                        .fee(ONE_HUNDRED_HBARS)
                        .payingWith(tokenOwner))
                .then(
                        getAccountBalance(tokenOwner).hasTokenBalance(token, 900L),
                        getAccountBalance(tokenReceiver).hasTokenBalance(token, 80L),
                        getAccountBalance(htsCollector).hasTokenBalance(token, 20L));
    }

    @HapiTest
    public HapiSpec transferWithFractionalCustomFeeAboveMaximumAmount() {
        return defaultHapiSpec("transferWithFractionalCustomFeeAboveMaximumAmount")
                .given(
                        cryptoCreate(htsCollector).balance(ONE_HUNDRED_HBARS),
                        cryptoCreate(hbarCollector).balance(0L),
                        cryptoCreate(tokenReceiver),
                        cryptoCreate(tokenTreasury),
                        cryptoCreate(tokenOwner).balance(ONE_MILLION_HBARS),
                        tokenCreate(token)
                                .treasury(tokenTreasury)
                                .initialSupply(tokenTotal)
                                .payingWith(htsCollector)
                                .withCustom(fractionalFee(
                                        numerator, denominator, minHtsFee, OptionalLong.of(9L), htsCollector)),
                        tokenAssociate(tokenReceiver, token),
                        tokenAssociate(tokenOwner, token),
                        cryptoTransfer(moving(tokenTotal, token).between(tokenTreasury, tokenOwner)))
                .when(cryptoTransfer(moving(100L, token).between(tokenOwner, tokenReceiver))
                        .fee(ONE_HUNDRED_HBARS)
                        .payingWith(tokenOwner))
                .then(
                        getAccountBalance(tokenOwner).hasTokenBalance(token, 900),
                        getAccountBalance(tokenReceiver).hasTokenBalance(token, 91),
                        getAccountBalance(htsCollector).hasTokenBalance(token, 9L));
    }

    @HapiTest
    public HapiSpec transferWithFractionalCustomFeeNetOfTransfers() {
        return defaultHapiSpec("transferWithFractionalCustomFeeNetOfTransfers")
                .given(
                        cryptoCreate(htsCollector).balance(ONE_HUNDRED_HBARS),
                        cryptoCreate(hbarCollector).balance(0L),
                        cryptoCreate(tokenReceiver),
                        cryptoCreate(tokenTreasury),
                        cryptoCreate(tokenOwner).balance(ONE_MILLION_HBARS),
                        tokenCreate(token)
                                .treasury(tokenTreasury)
                                .initialSupply(tokenTotal)
                                .payingWith(htsCollector)
                                .withCustom(fractionalFeeNetOfTransfers(
                                        numerator, denominator, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector)),
                        tokenAssociate(tokenReceiver, token),
                        tokenAssociate(tokenOwner, token),
                        cryptoTransfer(moving(tokenTotal, token).between(tokenTreasury, tokenOwner)))
                .when(cryptoTransfer(moving(100L, token).between(tokenOwner, tokenReceiver))
                        .fee(ONE_HUNDRED_HBARS)
                        .payingWith(tokenOwner))
                .then(
                        getAccountBalance(tokenOwner).hasTokenBalance(token, 890L),
                        getAccountBalance(tokenReceiver).hasTokenBalance(token, 100L),
                        getAccountBalance(htsCollector).hasTokenBalance(token, 10L));
    }

    @HapiTest
    public HapiSpec transferWithFractionalCustomFeeNetOfTransfersBellowMinimumAmount() {
        return defaultHapiSpec("transferWithFractionalCustomFeeNetOfTransfersBellowMinimumAmount")
                .given(
                        cryptoCreate(htsCollector).balance(ONE_HUNDRED_HBARS),
                        cryptoCreate(hbarCollector).balance(0L),
                        cryptoCreate(tokenReceiver),
                        cryptoCreate(tokenTreasury),
                        cryptoCreate(tokenOwner).balance(ONE_MILLION_HBARS),
                        tokenCreate(token)
                                .treasury(tokenTreasury)
                                .initialSupply(tokenTotal)
                                .payingWith(htsCollector)
                                .withCustom(fractionalFeeNetOfTransfers(
                                        numerator, denominator, 20L, OptionalLong.of(30), htsCollector)),
                        tokenAssociate(tokenReceiver, token),
                        tokenAssociate(tokenOwner, token),
                        cryptoTransfer(moving(tokenTotal, token).between(tokenTreasury, tokenOwner)))
                .when(cryptoTransfer(moving(100L, token).between(tokenOwner, tokenReceiver))
                        .fee(ONE_HUNDRED_HBARS)
                        .payingWith(tokenOwner))
                .then(
                        getAccountBalance(tokenOwner).hasTokenBalance(token, 880L),
                        getAccountBalance(tokenReceiver).hasTokenBalance(token, 100L),
                        getAccountBalance(htsCollector).hasTokenBalance(token, 20L));
    }

    @HapiTest
    public HapiSpec transferWithFractionalCustomFeeNetOfTransfersAboveMaximumAmount() {
        return defaultHapiSpec("transferWithFractionalCustomFeeNetOfTransfersAboveMaximumAmount")
                .given(
                        cryptoCreate(htsCollector).balance(ONE_HUNDRED_HBARS),
                        cryptoCreate(hbarCollector).balance(0L),
                        cryptoCreate(tokenReceiver),
                        cryptoCreate(tokenTreasury),
                        cryptoCreate(tokenOwner).balance(ONE_MILLION_HBARS),
                        tokenCreate(token)
                                .treasury(tokenTreasury)
                                .initialSupply(tokenTotal)
                                .payingWith(htsCollector)
                                .withCustom(fractionalFeeNetOfTransfers(
                                        numerator, denominator, minHtsFee, OptionalLong.of(9L), htsCollector)),
                        tokenAssociate(tokenReceiver, token),
                        tokenAssociate(tokenOwner, token),
                        cryptoTransfer(moving(tokenTotal, token).between(tokenTreasury, tokenOwner)))
                .when(cryptoTransfer(moving(100L, token).between(tokenOwner, tokenReceiver))
                        .fee(ONE_HUNDRED_HBARS)
                        .payingWith(tokenOwner))
                .then(
                        getAccountBalance(tokenOwner).hasTokenBalance(token, 891L),
                        getAccountBalance(tokenReceiver).hasTokenBalance(token, 100L),
                        getAccountBalance(htsCollector).hasTokenBalance(token, 9L));
    }

    @HapiTest
    public HapiSpec transferWithFractionalCustomFeeAllowance() {
        return defaultHapiSpec("transferWithFractionalCustomFeeAllowance")
                .given(
                        cryptoCreate(htsCollector).balance(ONE_HUNDRED_HBARS),
                        cryptoCreate(tokenReceiver),
                        cryptoCreate(spender).balance(ONE_MILLION_HBARS),
                        cryptoCreate(tokenTreasury),
                        cryptoCreate(tokenOwner).balance(ONE_MILLION_HBARS),
                        tokenCreate(token)
                                .treasury(tokenTreasury)
                                .initialSupply(tokenTotal)
                                .payingWith(htsCollector)
                                .withCustom(fractionalFee(
                                        numerator, denominator, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector)),
                        tokenAssociate(tokenReceiver, token),
                        tokenAssociate(tokenOwner, token),
                        tokenAssociate(spender, token),
                        cryptoTransfer(moving(tokenTotal, token).between(tokenTreasury, tokenOwner)))
                .when(
                        cryptoApproveAllowance()
                                .payingWith(tokenOwner)
                                .addTokenAllowance(tokenOwner, token, spender, 120L)
                                .signedBy(tokenOwner)
                                .fee(ONE_HUNDRED_HBARS),
                        getAccountDetails(tokenOwner)
                                .has(accountDetailsWith().tokenAllowancesContaining(token, spender, 120L)),
                        cryptoTransfer(movingWithAllowance(100L, token).between(tokenOwner, tokenReceiver))
                                .fee(ONE_HUNDRED_HBARS)
                                .payingWith(spender)
                                .signedBy(spender))
                .then(
                        getAccountBalance(tokenOwner).hasTokenBalance(token, tokenTotal - 100L),
                        getAccountBalance(tokenReceiver).hasTokenBalance(token, 100L - 100L * numerator / denominator),
                        getAccountBalance(htsCollector).hasTokenBalance(token, 100L * numerator / denominator),
                        getAccountDetails(tokenOwner)
                                .has(accountDetailsWith().tokenAllowancesContaining(token, spender, 20L)));
    }

    @HapiTest
    public HapiSpec transferWithFractionalCustomFeeNegativeNotEnoughAllowance() {
        return defaultHapiSpec("transferWithFractionalCustomFeeNegativeNotEnoughAllowance")
                .given(
                        cryptoCreate(htsCollector).balance(ONE_HUNDRED_HBARS),
                        cryptoCreate(hbarCollector).balance(0L),
                        cryptoCreate(tokenReceiver),
                        cryptoCreate(spender).balance(ONE_MILLION_HBARS),
                        cryptoCreate(tokenTreasury),
                        cryptoCreate(tokenOwner).balance(ONE_MILLION_HBARS),
                        tokenCreate(token)
                                .treasury(tokenTreasury)
                                .initialSupply(tokenTotal)
                                .payingWith(htsCollector)
                                .withCustom(fractionalFee(
                                        numerator, denominator, minHtsFee, OptionalLong.of(maxHtsFee), htsCollector)),
                        tokenAssociate(tokenReceiver, token),
                        tokenAssociate(tokenOwner, token),
                        tokenAssociate(spender, token),
                        cryptoTransfer(moving(tokenTotal, token).between(tokenTreasury, tokenOwner)))
                .when(
                        cryptoApproveAllowance()
                                .payingWith(tokenOwner)
                                .addTokenAllowance(tokenOwner, token, spender, 1)
                                .fee(ONE_HUNDRED_HBARS),
                        cryptoTransfer(moving(100L, token).between(spender, tokenReceiver))
                                .fee(ONE_HUNDRED_HBARS)
                                .payingWith(spender)
                                .hasKnownStatus(ResponseCodeEnum.INSUFFICIENT_TOKEN_BALANCE))
                .then(
                        getAccountBalance(tokenOwner).hasTokenBalance(token, 1000L),
                        getAccountBalance(tokenReceiver).hasTokenBalance(token, 0L),
                        getAccountBalance(htsCollector).hasTokenBalance(token, 0L));
    }

    @HapiTest
    public HapiSpec transferWithFractionalCustomFeesThreeCollectors() {
        return defaultHapiSpec("transferWithFractionalCustomFeesThreeCollectors")
                .given(
                        cryptoCreate(alice),
                        cryptoCreate(bob),
                        cryptoCreate(carol),
                        cryptoCreate(tokenTreasury).balance(ONE_MILLION_HBARS),
                        tokenCreate(token)
                                .treasury(tokenTreasury)
                                .initialSupply(tokenTotal)
                                .withCustom(fractionalFee(numerator, 2L, minHtsFee, OptionalLong.of(maxHtsFee), alice))
                                .withCustom(fractionalFee(numerator, 4L, minHtsFee, OptionalLong.of(maxHtsFee), bob))
                                .withCustom(fractionalFee(numerator, 8L, minHtsFee, OptionalLong.of(maxHtsFee), carol)))
                .when(
                        cryptoTransfer(moving(tokenTotal, token).between(tokenTreasury, alice)),
                        cryptoTransfer(moving(tokenTotal / 2, token).between(alice, bob)),
                        cryptoTransfer(moving(tokenTotal / 4, token).between(bob, carol)))
                .then(
                        getAccountBalance(alice).hasTokenBalance(token, tokenTotal / 2),
                        getAccountBalance(bob).hasTokenBalance(token, tokenTotal / 2 - tokenTotal / 4),
                        getAccountBalance(carol).hasTokenBalance(token, tokenTotal / 4));
    }

    @Override
    protected Logger getResultsLogger() {
        return log;
    }
}
