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
package com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors;

import com.github.cafdataprocessing.corepolicy.common.dto.ItemType;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Object to encapsulate information about the failure of an operation
 */
public class DataOperationFailureErrors implements ExceptionErrors {

    private FailureOperation failureOperation;
    private ItemType itemType;
    private String info;

    public enum FailureOperation {
        CREATE("create"),
        DELETE("delete"),
        RETRIEVE("retrieve"),
        UPDATE("update");

        private final String key;

        FailureOperation(String key) {
            this.key = key;
        }

        public String getKey(){
            return key;
        }
    }

    public DataOperationFailureErrors(FailureOperation failureOperation, ItemType itemType) {
        this.failureOperation = failureOperation;
        this.itemType = itemType;
    }

    public DataOperationFailureErrors(FailureOperation failureOperation, ItemType itemType, String info) {
        this.failureOperation = failureOperation;
        this.itemType = itemType;
        this.info = info;
    }

    @Override
    public String getMessage(Locale locale) {
        ResourceBundle bundle = ResourceBundle.getBundle("localisation.exceptions.exceptionErrors." + getResourceName(), locale);
        MessageFormat format = new MessageFormat(bundle.getString(StringUtils.isBlank(info)?"messageFormat":"messageFormatInfoIncluded"));
        String[] args = {bundle.getString(failureOperation.getKey()), bundle.getString(itemType.toValue()), info};
        return format.format(args);
    }

    @Override
    public String getResourceName() {
        return "backEndDatabaseFailureErrors";
    }
}
