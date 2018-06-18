/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
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
package com.github.cafdataprocessing.corepolicy.common.dto;

import com.github.cafdataprocessing.corepolicy.common.dto.conditions.ConditionType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Encapsulates a condition name and a corresponding condition type.
 */
public class NameTypePair{
        public String name;
        public Enum conditionType;

        public NameTypePair(String name) {
            this.name = name;
            this.conditionType = null;
        }

        public NameTypePair(String name, ConditionType conditionType) {
            this.name = name;
            this.conditionType = conditionType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;

            if (o == null || getClass() != o.getClass()) return false;

            NameTypePair that = (NameTypePair) o;

            return new EqualsBuilder()
                    .append(name, that.name)
                    .append(conditionType, that.conditionType)
                    .isEquals();
        }

        @Override
        public int hashCode() {
            return new HashCodeBuilder(17, 37)
                    .append(name)
                    .append(conditionType)
                    .toHashCode();
        }
    }
