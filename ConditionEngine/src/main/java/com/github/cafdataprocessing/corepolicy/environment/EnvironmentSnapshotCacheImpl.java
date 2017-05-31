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

import com.github.cafdataprocessing.corepolicy.EnvironmentSnapshotCache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.github.cafdataprocessing.corepolicy.common.EngineProperties;
import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.exceptions.InvalidFieldValueCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.TransitoryBackEndFailureCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.InvalidFieldValueErrors;
import com.github.cafdataprocessing.corepolicy.common.shared.CorePolicyLogger;
import org.joda.time.Period;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class EnvironmentSnapshotCacheImpl implements EnvironmentSnapshotCache {

    private LoadingCache<Long, EnvironmentSnapshot> conditionEngineRepositoryCache;
    private EnvironmentInitializer environmentInitializer;

    @Autowired
    public EnvironmentSnapshotCacheImpl(ApplicationContext applicationContext, EngineProperties engineProperties) {
        Integer maxSequenceCacheSize = engineProperties.getEnvironmentCacheMaxsize();
        Period period = new Period(engineProperties.getEnvironmentCacheVerifyPeriod());
        environmentInitializer = applicationContext.getBean("Pipeline", EnvironmentInitializer.class);

        conditionEngineRepositoryCache = CacheBuilder.newBuilder()
                .maximumSize(maxSequenceCacheSize)
                .expireAfterWrite(period.toStandardDuration().getMillis(), TimeUnit.MILLISECONDS)
                .build(
                        new CacheLoader<Long, EnvironmentSnapshot>() {
                            public EnvironmentSnapshot load(Long sequenceId) throws Exception {
                                try(CorePolicyLogger traceInfo = new CorePolicyLogger("EnvironmentSnapshot:load")) {
                                    EnvironmentSnapshotImpl environmentSnapshot = new EnvironmentSnapshotImpl();
                                    environmentSnapshot.setCollectionSequenceId(sequenceId);
                                    environmentSnapshot = environmentInitializer.initialize(environmentSnapshot);
                                    return environmentSnapshot;
                                }
                            }
                        }
                );
    }


    @Override
    public EnvironmentSnapshot get(Long sequenceId) throws Exception {

        try(CorePolicyLogger traceInfo = new CorePolicyLogger("EnvironmentSnapshot:get")) {
            EnvironmentSnapshot cachedEntry = conditionEngineRepositoryCache.get(sequenceId);
            return cachedEntry;
        } catch (UncheckedExecutionException e) {
            if(e.getCause() instanceof TransitoryBackEndFailureCpeException) {
                BackEndRequestFailedErrors error = (BackEndRequestFailedErrors)((TransitoryBackEndFailureCpeException) e.getCause()).getError();

                throw new TransitoryBackEndFailureCpeException(error, e);
            }
            if(e.getCause() instanceof InvalidFieldValueCpeException) {
                InvalidFieldValueErrors error = (InvalidFieldValueErrors)((InvalidFieldValueCpeException) e.getCause()).getError();

                throw new InvalidFieldValueCpeException(error, e);
            }
            throw e;
        }
    }

    @Override
    public void invalidate(Long sequenceId) {
        try (CorePolicyLogger traceInfo = new CorePolicyLogger("EnvironmentSnapshot:invalidate")) {

            // Invalidate makes this unsafe - by discarding what is presently there.
            // As such we want to call remove, if we have a snapshot already loaded!

            // Get the cached version - only if present - dont force a load at this point!
            EnvironmentSnapshot cachedEntry = conditionEngineRepositoryCache.getIfPresent(sequenceId);

            // Now take this version, and delete any persisted items, whereever they may be...
            if (cachedEntry != null && cachedEntry instanceof EnvironmentSnapshotImpl) {
                environmentInitializer.remove((EnvironmentSnapshotImpl) cachedEntry);
            }

            // Take the current cache, and reload whatever it currently holds.
            conditionEngineRepositoryCache.refresh(sequenceId);
        }
    }
}
