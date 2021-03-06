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
package com.github.cafdataprocessing.corepolicy.common;

import java.util.concurrent.*;
import java.util.regex.Matcher;

/**
 *
 */
public class MatcherWithTimeout {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final Callable<Boolean> task;
    private final Matcher matcher;
    private final int timeoutSeconds;

    public MatcherWithTimeout(final Matcher matcher, int timeoutSeconds){
        this.timeoutSeconds = timeoutSeconds;
        task = () -> matcher.find();
        this.matcher = matcher;
    }

    public boolean find(){
        Future<Boolean> future = executor.submit(task);
        try {
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String group(){
        return matcher.group();
    }

    public Matcher getMatcher(){
        return matcher;
    }
}
