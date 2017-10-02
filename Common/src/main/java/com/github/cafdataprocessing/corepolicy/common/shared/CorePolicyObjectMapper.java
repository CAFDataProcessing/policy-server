/*
 * Copyright 2015-2017 EntIT Software LLC, a Micro Focus company.
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
package com.github.cafdataprocessing.corepolicy.common.shared;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.common.dto.DtoBase;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;

/**
 *
 */
public class CorePolicyObjectMapper extends ObjectMapper {
    public CorePolicyObjectMapper(){
        super();
        SimpleModule module = new SimpleModule("EnvironmentObjectMapperModule", new Version(0, 1, 0, ""));
//        module.addDeserializer(Condition.class, new ConditionDeserializer(this));
        module.addSerializer(Document.class, new DocumentSerializer());
        module.addDeserializer(Document.class, new DocumentDeserializer());
//        module.addDeserializer(PageOfResults.class, new PageOfResultsDeserializer());
        module.addDeserializer(EnvironmentSnapshot.class, new EnvironmentSnapshotDeserializer());
        module.addDeserializer(Condition.class, new AbstractConditionDeserializer());
        module.addDeserializer(DtoBase.class, new DtoDeserializer());
        module.addSerializer(DtoBase.class, new DtoSerializer());
        module.addSerializer(EnvironmentSnapshot.class, new EnvironmentSnapshotSerializer());
        super.registerModule(module);
        super.registerModule(new JodaModule());
        super.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        super.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        super.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
        super.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING,true);
        //super.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true); //this was causing a unit test failure
        super.disable(SerializationFeature.INDENT_OUTPUT);
    }
}
