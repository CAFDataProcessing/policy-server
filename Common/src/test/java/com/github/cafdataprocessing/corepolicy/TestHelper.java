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
package com.github.cafdataprocessing.corepolicy;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import static com.github.cafdataprocessing.corepolicy.common.shared.DebugHelper.getCurrentThreadPrefix;

/**
 *
 */
public class TestHelper {
    public static void shouldThrow(Consumer<?> consumer, String failureMessage){
        boolean threw = false;
        try{
            consumer.accept(null);
        }
        catch (Exception e){
            threw = true;
        }
        if(!threw){
            throw new RuntimeException(failureMessage);
        }
    }

    public static void shouldThrow(Consumer<?> consumer){
        shouldThrow(consumer, "Expected exception did not happen.");
    }

    public static InputStream getInputStream(String testString) {
        return new ByteArrayInputStream( testString.getBytes( ) );
    }

    public static void RunMultiThreadedTest(Consumer<?> function, int numberOfThreads, final CountDownLatch gate, final Collection<String> errors) throws InterruptedException {

        for (Integer i = 0; i < numberOfThreads; i++) {
            final Integer threadNumber = i;
            new Thread() {
                public void run() {
                    try {
                        System.out.println(getCurrentThreadPrefix() + " Running thread " + threadNumber);

                        function.accept(null);

                        System.out.println(getCurrentThreadPrefix() + " Finished - " + threadNumber);
                    } catch (Exception e) {
                        gate.countDown();
                        errors.add(getCurrentThreadPrefix() + " Failure - " + e);
                        throw new RuntimeException(e);
                    }

                    gate.countDown();
                }
            }.start();
        }

        gate.await();
    }

    public static String getUniqueString( String strPrefix ) {
        return  strPrefix + UUID.randomUUID().toString();
    }

}
