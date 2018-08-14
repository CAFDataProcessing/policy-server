#!/bin/bash
#
# Copyright 2015-2018 Micro Focus or one of its affiliates.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

CONNECTION_URL=$(env | grep hibernate.connectionstring | awk -F'=' '{print $2}')
DB_USERNAME=$(env | grep hibernate.user | awk -F'=' '{print $2}')
DB_NAME=$(env | grep hibernate.databasename | awk -F'=' '{print $2}')
DB_PASSWORD=$(env | grep hibernate.password | awk -F'=' '{print $2}')

java    -Dapi.mode=direct \
        -Dapi.direct.repository=hibernate \
        -Dhibernate.connectionstring=$CONNECTION_URL \
        -Dhibernate.user=$DB_USERNAME \
        -Dhibernate.password=$DB_PASSWORD \
        -Dhibernate.databasename=$DB_NAME \
        -jar /database/corepolicy-database.jar -c
