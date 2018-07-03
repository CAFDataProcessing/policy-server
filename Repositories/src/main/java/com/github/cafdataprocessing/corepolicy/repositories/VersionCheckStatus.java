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
package com.github.cafdataprocessing.corepolicy.repositories;

/**
 * Class to check version status
 */
public class VersionCheckStatus {

    public VersionCheckStatus( RepositoryType repositoryType )
    {
        this.isVersionSupported = true;
        this.repositoryType = repositoryType;
    }

    public VersionCheckStatus( RepositoryType repositoryType, Exception e)
    {
        this.setVersionSupported(false);
        this.setRepositoryType(repositoryType);
        this.setExceptionInfo( e );
    }

    private boolean isVersionSupported;
    private RepositoryType repositoryType;

    // optional exception information, if isn't supported.
    // this is so we can rethrow original error again and again.
    private Exception  exceptionInfo;

    public boolean isVersionSupported() {
        return isVersionSupported;
    }

    public void setVersionSupported(boolean isVersionSupported) {
        this.isVersionSupported = isVersionSupported;
    }

    public RepositoryType getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(RepositoryType repositoryType) {
        this.repositoryType = repositoryType;
    }

    public Exception  getExceptionInfo() {
        return exceptionInfo;
    }

    public void setExceptionInfo(Exception exceptionInfo) {
        this.exceptionInfo = exceptionInfo;
    }
}
