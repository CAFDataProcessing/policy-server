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
package com.github.cafdataprocessing.corepolicy;

import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Helper class for tests.
 */
public class Helper {
    private static AtomicLong number = new AtomicLong(0);

    /**
     * Gets an id which is unique within the application instance
     * @return an id
     */
    public static long getId(){
        long id = number.getAndIncrement();
        return id;
    }

    public static String getTempFolder(){
        try{
            //create a temp file
            File temp = File.createTempFile("temp-file-name", ".tmp");
            String absolutePath = temp.getAbsolutePath();
            String tempFilePath = absolutePath.
                    substring(0,absolutePath.lastIndexOf(File.separator));

            return tempFilePath;
        }catch(IOException e){
            throw new BackEndRequestFailedCpeException(e);
        }
    }
}
