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
package com.github.cafdataprocessing.corepolicy.hibernate.repositories;

import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.UserContext;
import com.github.cafdataprocessing.corepolicy.common.dto.LexiconExpression;
import com.github.cafdataprocessing.corepolicy.repositories.v2.ExecutionContext;
import com.github.cafdataprocessing.corepolicy.repositories.v2.LexiconExpressionRepository;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 *
 */
@Component
public class LexiconExpressionRepositoryImpl extends HibernateBaseRepositoryImpl<LexiconExpression> implements LexiconExpressionRepository {

    @Autowired
    public LexiconExpressionRepositoryImpl(UserContext userContext, ApplicationContext context ) {
        super(userContext, LexiconExpression.class, "LexiconExpression", context);
    }

    @Override
    public Collection<LexiconExpression> retrieve(ExecutionContext executionContext, Long lexiconId) {
        Session session = getSession(executionContext);

        Criteria criteria = session.createCriteria(LexiconExpression.class)
                .add(Restrictions.eq("lexiconId", lexiconId))
                .add(Restrictions.or(
                                Restrictions.eq("projectId", userContext.getProjectId()),
                                Restrictions.isNull("projectId"))
                );

        List list = criteria.list();

        for (Object item: list) {
            session.evict(item);
        }
        return list;
    }

    final static String batchCreateSQL = "INSERT INTO tbl_lexicon_expression (expression,lexicon_type,lexicon_id,project_id) VALUES %s;";
    final static String valueFormat = "('%s','%s','%s','%s')";

    @Override
    public Collection<LexiconExpression> createAll(ExecutionContext executionContext, Collection<LexiconExpression> lexiconExpressions) {
        Session session = getSession(executionContext);
        EngineProperties engineProperties = applicationContext.getBean(EngineProperties.class);
        Integer maxBatchSize = engineProperties.getDefaultBatchSize();
        int expressionCount = 0;
        StringBuilder stringBuilder = new StringBuilder();
        Long lexiconId = lexiconExpressions.stream().findFirst().get().lexiconId;

        for (LexiconExpression expression : lexiconExpressions) {
            expressionCount++;

            expression.id = null;

            expression = preSave(expression, session);

            if (expression.id != null) {
                throw new RuntimeException("Id should be null");
            }


            String values = String.format(valueFormat, expression.expression, expression.type, expression.lexiconId, userContext.getProjectId());

            //if first value don't prefix with a comma
            if(stringBuilder.length() == 0){
                stringBuilder.append(values);
            } else {
                stringBuilder.append("," + values);
            }

            if (expressionCount % maxBatchSize == 0) {
                executeBatchInsert(stringBuilder.toString(), session);
                stringBuilder.setLength(0);
            }
        }
        //Save any remaining expressions
        if (stringBuilder.length() > 0) {
            executeBatchInsert(stringBuilder.toString(), session);
        }
        return retrieve(executionContext, lexiconId);
    }

    private final void executeBatchInsert(String valuesToInsert, Session session) {
        String insertStatement = String.format(batchCreateSQL, valuesToInsert);
        session.createSQLQuery(insertStatement).executeUpdate();
    }

    @Override
    protected void forceLazyInitializedPropsToNull( Collection<LexiconExpression> items ) {

        // nothing to do here, as it has no lazy initialized props!
        return;
    }

    @Override
    protected Collection<Order> getSortFields(boolean ascending) {
        final Collection<Order> ascSortFields = Arrays.asList(Order.asc("id"));
        final Collection<Order> descSortFields = Arrays.asList(Order.desc("id"));

        return ascending ? ascSortFields : descSortFields;
    }

    @Override
    protected RuntimeException handleCreateException(Exception exception) {
        return new RuntimeException(exception);
    }

    @Override
    protected RuntimeException handleUpdateException(Exception exception) {
        return new RuntimeException(exception);
    }
}
