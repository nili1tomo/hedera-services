/*
 * Copyright (C) 2020-2024 Hedera Hashgraph, LLC
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

package com.hedera.services.bdd.suites.freeze;

import static com.hedera.services.bdd.spec.HapiSpec.customHapiSpec;
import static com.hedera.services.bdd.spec.utilops.UtilVerbs.freezeOnly;

import com.hedera.services.bdd.spec.HapiSpec;
import com.hedera.services.bdd.suites.HapiSuite;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FreezeDockerNetwork extends HapiSuite {
    private static final Logger log = LogManager.getLogger(FreezeDockerNetwork.class);

    public static void main(String... args) {
        new FreezeDockerNetwork().runSuiteSync();
    }

    @Override
    public List<HapiSpec> getSpecsInSuite() {
        return List.of(new HapiSpec[] {
            justFreeze(),
        });
    }

    final HapiSpec justFreeze() {
        return customHapiSpec("JustFreeze")
                .withProperties(Map.of("nodes", "127.0.0.1:50213:0.0.3,127.0.0.1:50214:0.0.4,127.0.0.1:50215:0.0.5"))
                .given()
                .when()
                .then(freezeOnly().startingIn(60).seconds());
    }

    @Override
    protected Logger getResultsLogger() {
        return log;
    }
}
