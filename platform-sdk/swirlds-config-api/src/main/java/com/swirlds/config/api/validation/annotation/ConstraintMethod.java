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

package com.swirlds.config.api.validation.annotation;

import com.swirlds.config.api.ConfigurationBuilder;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A constraint annotation that can be used define how the value for a config data property (see
 * {@link com.swirlds.config.api.ConfigProperty}) must be validated. The value of the annotation must name a public
 * method that is part of the config data record (see {@link com.swirlds.config.api.ConfigData}) that contains the
 * annotated property. The method must follow the given pattern:
 * {@code public ConfigViolation methodName(Configuration configuration)}. If the validation is successful the method
 * must return null. If the validation fails a ConfigViolation must be returned. The validation of the annotation is
 * automatically executed at the initialization of the configuration (see {@link ConfigurationBuilder#build()})
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.RECORD_COMPONENT)
public @interface ConstraintMethod {

    /**
     * Defines the name of the method that will be executed to validate the annotated property.
     *
     * @return name of the method that will be executed to validate the annotated property
     */
    String value();
}
