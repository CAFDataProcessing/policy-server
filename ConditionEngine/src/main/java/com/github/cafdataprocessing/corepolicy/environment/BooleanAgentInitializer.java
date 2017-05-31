/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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
package com.github.cafdataprocessing.corepolicy.environment;

import com.github.cafdataprocessing.corepolicy.booleanagent.BooleanAgentServices;
import com.github.cafdataprocessing.corepolicy.booleanagent.ConditionToBooleanAgentConverter;
import com.github.cafdataprocessing.corepolicy.common.ElasticsearchProperties;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.dto.Lexicon;
import com.github.cafdataprocessing.corepolicy.common.dto.LexiconExpression;
import com.github.cafdataprocessing.corepolicy.common.dto.LexiconExpressionType;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.Condition;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.TextCondition;
import com.github.cafdataprocessing.corepolicy.domainModels.BooleanAgentDocuments;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 *
 */
public class BooleanAgentInitializer implements EnvironmentInitializer {
    private final ElasticsearchProperties elasticsearchProperties;
    private final BooleanAgentServices booleanAgentServices;
    private final ConditionToBooleanAgentConverter conditionToBooleanAgentConverter;

    @Autowired
    public BooleanAgentInitializer(ElasticsearchProperties elasticsearchProperties,
                                   BooleanAgentServices booleanAgentServices,
                                   ConditionToBooleanAgentConverter conditionToBooleanAgentConverter){

        this.elasticsearchProperties = elasticsearchProperties;
        this.booleanAgentServices = booleanAgentServices;
        this.conditionToBooleanAgentConverter = conditionToBooleanAgentConverter;
    }

    @Override
    public EnvironmentSnapshotImpl initialize(EnvironmentSnapshotImpl environmentSnapshot) {
        if(!booleanAgentServices.getAvailable()){
            return environmentSnapshot;
        }

        //Check the age of the snapshot and if it close to the time the agents would have expired.
        //If the agents are close to expiry then recreate them.
        boolean agentsMightHaveExpired = environmentSnapshot.getCreateDate().toDateTime().minusHours(1).getMillis()
                < DateTime.now().getMillis() - elasticsearchProperties.getAgentExpiry().toStandardDuration().getMillis();

        if(booleanAgentServices.existForInstanceId(environmentSnapshot.getInstanceId()) && !(agentsMightHaveExpired)){
            return environmentSnapshot;
        }

        Collection<TextCondition> contentExpressionConditions = new ArrayList<>();
        Collection<LexiconExpression> lexiconExpressions = new ArrayList<>();

        Collection<Condition> conditions = new LinkedList<>(environmentSnapshot.getConditions().values());

        for(Condition condition: conditions){
            if (condition instanceof TextCondition) {
                TextCondition contentExpressionCondition = (TextCondition) condition;
                contentExpressionConditions.add(contentExpressionCondition);
            }
        }

        for(Lexicon lexiconToConvert: environmentSnapshot.getLexicons().values()){
            lexiconExpressions.addAll(lexiconToConvert.lexiconExpressions.stream().
                    filter(lexiconExpression -> lexiconExpression.type == LexiconExpressionType.TEXT).
                    collect(Collectors.toList()));
        }

        BooleanAgentDocuments booleanAgentDocuments = conditionToBooleanAgentConverter.convert(contentExpressionConditions,
                lexiconExpressions);

        booleanAgentServices.create(environmentSnapshot.getInstanceId(), booleanAgentDocuments);

        return environmentSnapshot;
    }

    @Override
    public EnvironmentSnapshotImpl remove(EnvironmentSnapshotImpl environmentSnapshot){
        return environmentSnapshot;
    }
}
