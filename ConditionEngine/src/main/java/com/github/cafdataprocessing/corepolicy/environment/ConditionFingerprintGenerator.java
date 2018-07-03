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
package com.github.cafdataprocessing.corepolicy.environment;

import com.github.cafdataprocessing.corepolicy.common.EnvironmentSnapshot;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequence;
import com.github.cafdataprocessing.corepolicy.common.dto.CollectionSequenceEntry;
import com.github.cafdataprocessing.corepolicy.common.dto.conditions.*;
import com.github.cafdataprocessing.corepolicy.common.exceptions.BackEndRequestFailedCpeException;
import com.github.cafdataprocessing.corepolicy.common.exceptions.excpetionErrors.BackEndRequestFailedErrors;
import com.github.cafdataprocessing.corepolicy.common.shared.FingerprintGenerator;
import com.github.cafdataprocessing.corepolicy.common.shared.Sha1FingerprintGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * This should really be in the main snapshot fingerprint generator, but I've moved it here for now because it's long!
 * This assumes that the other fingerprints that conditions are dependant on (lexicons etc) have already
 * been generated.
 */
public class ConditionFingerprintGenerator {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentSnapshotFingerprintGenerator.class);
    private EnvironmentSnapshot snapshot;
    private FingerprintGenerator fingerprintGenerator;


    ConditionFingerprintGenerator(EnvironmentSnapshot snapshot) {

        this.snapshot = snapshot;
        //interesting point on the initial value, 16 is too small so want to set the bar a little higher!
        //http://stackoverflow.com/a/8352426
        //this.conditionIdFingerprintMap = new HashMap<>(64);
        this.fingerprintGenerator = new Sha1FingerprintGenerator();
    }

    public static void populate(EnvironmentSnapshot snapshot){
        ConditionFingerprintGenerator generator = new ConditionFingerprintGenerator(snapshot);
        generator.populateConditions();
    }

    private void populateConditions() {
        CollectionSequence sequence = this.snapshot.getCollectionSequences().get(this.snapshot.getCollectionSequenceId());
        Collection<CollectionSequenceEntry> entries = sequence.collectionSequenceEntries;

        if(entries != null) {
            for (CollectionSequenceEntry entry : entries) {
                for (Long collectionId : entry.collectionIds) {
                    Condition c = this.snapshot.getCollection(collectionId).condition;
                    addFingerprint(c);
                }
            }
        }

        if(sequence.defaultCollectionId != null){
            Condition c = this.snapshot.getCollection(sequence.defaultCollectionId).condition;
            addFingerprint(c);
        }
    }

    void addFingerprint(Condition condition) {

        if(condition == null)
            return;

        //if we don't need to make this fingerprint again, then dont
        if(condition.fingerprint != null)
            return;

        String fingerprintSource;

        switch (condition.conditionType) {
            case NUMBER:
                fingerprintSource = getNumberFingerprintSource((NumberCondition) condition);
                break;
            case NOT:
                //first, need to make the child fp
                addFingerprint(((NotCondition) condition).condition);

                fingerprintSource = getNotFingerprintSource((NotCondition) condition);
                break;
            case LEXICON:
                fingerprintSource = getLexiconFingerprintSource((LexiconCondition) condition);
                break;
            case EXISTS:
                fingerprintSource = getExistsFingerprintSource((ExistsCondition) condition);
                break;
            case DATE:
                fingerprintSource = getDateFingerprintSource((DateCondition) condition);
                break;
            case BOOLEAN:

                for(Condition child : ((BooleanCondition) condition).children) {
                    addFingerprint(child);
                }

                fingerprintSource = getBooleanFingerprintSource((BooleanCondition) condition);
                break;
            case TEXT:
                fingerprintSource = getTextFingerprintSource((TextCondition) condition);
                break;
            case REGEX:
                fingerprintSource = getRegexFingerprintSource((RegexCondition) condition);
                break;
            case STRING:
                fingerprintSource = getStringFingerprintSource((StringCondition) condition);
                break;
            case FRAGMENT:
                //first, need to make the child fp
                addFingerprint(this.snapshot.getCondition(((FragmentCondition) condition).value));

                fingerprintSource = getFragmentFingerprintSource((FragmentCondition) condition);
                break;
            default:
                throw new RuntimeException("Unknown condition type detected!");
        }

        condition.fingerprint = this.fingerprintGenerator.generate(fingerprintSource);
    }

    /**
     * Generates a string of condition properties to be combined with specific implementation props then hashed.
     * *DOES NOT HASH ITSELF*
     */
    String getConditionFingerprintInput(Condition c){
        StringBuilder sb = new StringBuilder();
        //ConditionKey props
        sb
                .append(c.id)
                .append(c.name == null ? "" : c.name)
                .append(c.conditionType);

        //Condition props
        sb
                .append(c.isFragment)
                .append(c.order)
                .append(c.target)
                .append(c.includeDescendants);
                //.append(c.notes);

        //we can ignore the parent_condition_id property here, because we don't
        //not returning a hash here because it will be hashed again and it's a waste of CPU.
        return sb.toString();
    }

    /**
     * Generates a string of field condition properties to be combined with specific implementation props then hashed.
     * *DOES NOT HASH ITSELF*
     */
    String getFieldConditionFingerprintSource(FieldCondition condition){
        /*
        Need to understand how field values are looked up to determine the correct fingerprint
        condition 'field'
            -> FieldLabel (fingerprint contains the following)
                -> Field
            -> Field

            I don't have to has them in this order, but I am just for consistency.
         */
        StringBuilder fieldHashSource = new StringBuilder();

        if(this.snapshot.getFieldLabels().containsKey(condition.field)){
            fieldHashSource.append(this.snapshot.getFieldLabel(condition.field).fingerprint);
        }
        fieldHashSource.append(condition.field);
        return getConditionFingerprintInput(condition) + fieldHashSource.toString();
    }

    String getNumberFingerprintSource(NumberCondition condition){
        return getFieldConditionFingerprintSource(condition) + condition.operator + condition.value;
    }

    String getNotFingerprintSource(NotCondition condition){

        if ( condition.condition == null ) {
            return getConditionFingerprintInput(condition);
        }

        if(condition.condition.fingerprint == null)
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.InvalidDataDetected, new Exception("Not condition is missing child fingerprint."));

        return getConditionFingerprintInput(condition) + condition.condition.fingerprint;
    }

    String getLexiconFingerprintSource(LexiconCondition condition){
        String lexiconFp = this.snapshot.getLexicon(condition.value).fingerprint;
        if(lexiconFp == null)
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.InvalidDataDetected, new Exception("Lexicon is missing fingerprint."));
        return getFieldConditionFingerprintSource(condition) + condition.language + lexiconFp;
        //todo do we need to include lexicon fingerprints here??
    }

    String getExistsFingerprintSource(ExistsCondition condition){
        return getFieldConditionFingerprintSource(condition);
    }

    String getDateFingerprintSource(DateCondition condition){
        return getFieldConditionFingerprintSource(condition) + condition.value + condition.operator;
    }

    String getBooleanFingerprintSource(BooleanCondition condition){
        //boolean fingerprint source can be the concatenation of all the children's fingerprints, because using the
        //source would be huge!
        StringBuilder sb = new StringBuilder(getConditionFingerprintInput(condition));
        sb.append(condition.operator);
        for(Condition c : condition.children){
            if(c.fingerprint == null){
                throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.InvalidDataDetected, new Exception("Boolean condition is missing child fingerprint."));
            }
            sb.append(c.fingerprint);
        }
        return sb.toString();
    }

    String getTextFingerprintSource(TextCondition condition){
        return getFieldConditionFingerprintSource(condition) + condition.value;
    }

    String getRegexFingerprintSource(RegexCondition condition){
        return getFieldConditionFingerprintSource(condition) + condition.value;
    }

    String getStringFingerprintSource(StringCondition condition){
        return getFieldConditionFingerprintSource(condition) + condition.value + condition.operator;
    }

    String getFragmentFingerprintSource(FragmentCondition condition){
        //fragments use the pointed fingerprint just like booleans
        Condition childCondition = this.snapshot.getCondition(condition.value);
        if(childCondition.fingerprint == null)
            throw new BackEndRequestFailedCpeException(BackEndRequestFailedErrors.InvalidDataDetected, new Exception("Fragment condition is missing fingerprint."));
        return getConditionFingerprintInput(condition) + this.snapshot.getCondition(condition.value).fingerprint;
    }
}
