#!/bin/bash
#
# Copyright 2015-2017 Hewlett Packard Enterprise Development LP.
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


####################################################
####################################################
# Utility functions
####################################################
####################################################

####################################################
# Get access to environment 
# vars which include dots or non-alphas in the name.
####################################################
function get_env_var() {
   local variableName="${1:-}" ; shift

   # helpful debug.
   # echo 'requesting environment variable name:' $variableName   
   # perl -le 'print $ENV{'$variableName'}';
   
   result=$(perl -le 'print $ENV{"'$variableName'"};')
}

####################################################
# Returns 1 - start Elasticsearch service
#             (default, or if POLICY_ELASTICSEARCH_DISABLED=false)
#		  0 - do not start Elasticsearch service
#			  (e.g. if POLICY_ELASTICSEARCH_DISABLED=true)
####################################################
should_start_elasticsearch() {

	get_env_var "POLICY_ELASTICSEARCH_DISABLED"
	# "POLICY_ELASTICSEARCH_DISABLED"

	[[ -n "$result" ]] && {

        if [ "$result" == "true" ]; then

			echo 'POLICY_ELASTICSEARCH_DISABLED is set to true - NOT enabling Elasticsearch service';
			return 0;

		fi;

        if [ "$result" == "false" ]; then

            echo 'POLICY_ELASTICSEARCH_DISABLED found as: false - enabling Elasticsearch service';
            return 1;

		fi;

		echo 'POLICY_ELASTICSEARCH_DISABLED found as:' $result ' - enabling Elasticsearch service';
		return 1;
	}

	echo 'POLICY_ELASTICSEARCH_ENABLED is not declared - enabling Elasticsearch service';
	return 1;
}

start_elasticsearch() {
    /opt/elasticsearchConfig/configureElasticsearch.sh
    echo "Attempting to start Elasticsearch ..."
    /etc/init.d/elasticsearch start
    if [ -n "$POLICY_ELASTICSEARCH_VERIFY_ATTEMPTS" ];
    then
      remainingChecks=$POLICY_ELASTICSEARCH_VERIFY_ATTEMPTS
    else
      remainingChecks=10
    fi
    while [ "$remainingChecks" -ne "0" ]
    do
        if service elasticsearch status | grep -q "elasticsearch is running" && curl --silent http://localhost:9200/_cluster/health | grep -q 'cluster_name';
        then
            echo "Elasticsearch started."
            remainingChecks=0
        else
            remainingChecks=$[remainingChecks-1]
            if [ "$remainingChecks" -ne "0" ];
            then
                echo "Elasticsearch not started yet, re-verifying after a short delay ..."
                sleep 5s
            else
                echo "Elasticsearch failed to start in a reasonable time. Exiting..."
                exit 1
            fi
        fi
    done
}

####################################################
####################################################
# Start of actual execution.
####################################################
####################################################

#Attempt to Start Elasticsearch if configured on.
should_start_elasticsearch

#Only start Elasticsearch if required to do so.
if (($?==1)); then
	start_elasticsearch
fi;

#Launch the policy server
sh /opt/tomcat/bin/catalina.sh run

