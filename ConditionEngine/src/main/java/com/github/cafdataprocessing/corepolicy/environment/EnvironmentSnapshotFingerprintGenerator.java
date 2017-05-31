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

import com.github.cafdataprocessing.corepolicy.common.domainModels.EnvironmentSnapshotImpl;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.common.shared.FingerprintGenerator;
import com.github.cafdataprocessing.corepolicy.common.shared.Sha1FingerprintGenerator;

/**
 * Created during dev to hold the logic for populating the fingerprint fields for the environment snapshot objects.
 * I've split the condition fp generator into another class, because it's long is not as clear, and for now I think it
 * will be easier to see what's going on if the class is smaller! Consider it a partial class
 */
public class EnvironmentSnapshotFingerprintGenerator {

    private EnvironmentSnapshotImpl snapshot;
    private FingerprintGenerator fingerprintGenerator;


    EnvironmentSnapshotFingerprintGenerator(EnvironmentSnapshotImpl snapshot) {

        this.snapshot = snapshot;
        //interesting point on the initial value, 16 is too small so want to set the bar a little higher!
        //http://stackoverflow.com/a/8352426
        //this.conditionIdFingerprintMap = new HashMap<>(64);
        this.fingerprintGenerator = new Sha1FingerprintGenerator();
    }

    public static void populate(EnvironmentSnapshotImpl snapshot){
        EnvironmentSnapshotFingerprintGenerator generator = new EnvironmentSnapshotFingerprintGenerator(snapshot);
        generator.populateFingerprints();
    }

    /**
     * This is the method that will organise the population of the fingerprint fields.
     */
    private void populateFingerprints() {
        /*
        The order has to be:
        Field Labels
        Lexicon Expressions
        Lexicons
        Conditions
        Policies //types not needed, because policy details are key
        Collections
        Collection Sequences
            CollectionSequenceEntries
        The snapshot itself

        Need to understand how field values are looked up to determine the correct fingerprint.
        condition 'field'
            -> FieldLabel
                -> Field
            -> Field
         */
        populateFieldLabels();
        populateLexicons();
        populateConditions();
        populatePolicies();
        populateCollections();
        populateCollectionSequences();

        //now, the snapshot fingerprint itself will be sourced from the combination of all collection sequences
        //although in practice, it's one sequence per snapshot.

        if(this.snapshot.getCollectionSequences().size() != 1)
            throw new RuntimeException("A snapshot can only be fingerprinted if it contains a single collection sequence.");

        this.snapshot.fingerprint = this.snapshot.getCollectionSequences().get(this.snapshot.getCollectionSequenceId()).fingerprint;
    }

    private void populatePolicies() {
        for(Policy p : this.snapshot.getPolicies().values()){
            StringBuilder sb = new StringBuilder();
            sb
                    .append(p.id)
                    .append(p.name)
                    //.append(p.description == null ? "" : p.description)
                    .append(p.typeId)
                    .append(p.priority)
                    .append(p.details.textValue());

            p.fingerprint = this.fingerprintGenerator.generate(sb.toString());
        }
    }

    private void populateConditions() {
        ConditionFingerprintGenerator.populate(this.snapshot);
    }

    private void populateCollectionSequences() {
        for(CollectionSequence cs : this.snapshot.getCollectionSequences().values()){
            StringBuilder sb = new StringBuilder();
            sb
                    .append(cs.id)
                    .append(cs.name == null ? "" : cs.name)
                    //.append(cs.description)
                    .append(cs.defaultCollectionId) //todo should this be a fingerprint?!?
                    .append(cs.excludedDocumentFragmentConditionId) //todo should this be a fingerprint?!?
                    .append(cs.fullConditionEvaluation);

            for(CollectionSequenceEntry e : cs.collectionSequenceEntries){

                sb
                        .append("entry")
                        .append(e.order)
                        .append(e.stopOnMatch);
                for(Long id : e.collectionIds){
                    sb.append(this.snapshot.getCollection(id).fingerprint);
                }
            }

            cs.fingerprint = this.fingerprintGenerator.generate(sb.toString());

            //now we need to generate fingerprints for the entries (this is to ensure a PK value when saving).
            //it requires the parent fingerprint :( It is NOT included in the sequence fp (obviously!)
            for(CollectionSequenceEntry e : cs.collectionSequenceEntries){
                StringBuilder entryBuilder = new StringBuilder();
                entryBuilder
                        .append(cs.fingerprint)
                        .append(e.order)
                        .append(e.stopOnMatch);
                for(Long id : e.collectionIds){
                    entryBuilder.append(this.snapshot.getCollection(id).fingerprint);
                }
                e.fingerprint = this.fingerprintGenerator.generate(entryBuilder.toString());
            }
        }
    }

    private void populateCollections() {
        for(DocumentCollection c : this.snapshot.getCollections().values()){
            StringBuilder sb = new StringBuilder();
            sb
                    .append(c.id)
                    .append(c.name == null ? "" : c.name)
                    //.append(c.description) //this is deliberately ignored
                    .append(c.condition == null ? "" : c.condition.fingerprint);

            for(Long policyId : c.policyIds){
                sb.append(this.snapshot.getPolicy(policyId).fingerprint);
            }

            c.fingerprint = this.fingerprintGenerator.generate(sb.toString());
        }
    }

    /**
     * This will have to do both lexicons and expressions.
     */
    private void populateLexicons() {
        for(Lexicon l : this.snapshot.getLexicons().values()){
            StringBuilder sb = new StringBuilder();
            sb
                    .append(l.id)
                    .append(l.name == null ? "" : l.name);
                    //.append(l.description);

            //the lexicon fp will consist of all of the fp's of it's expressions
            for(LexiconExpression le : l.lexiconExpressions){
                StringBuilder sb2 = new StringBuilder();
                sb2
                        .append(le.id)
                        .append(le.type)
                        .append(le.expression);
                le.fingerprint = this.fingerprintGenerator.generate(sb2.toString());
                sb.append(le.fingerprint);
            }

            l.fingerprint = this.fingerprintGenerator.generate(sb.toString());
        }
    }

    private void populateFieldLabels() {
        for(FieldLabel fl : this.snapshot.getFieldLabels().values()){
            StringBuilder sb = new StringBuilder();
            sb
                    .append(fl.id)
                    .append(fl.name == null ? "" : fl.name)
                    .append(fl.fieldType);

            fl.fingerprint = this.fingerprintGenerator.generate(sb.toString());
        }
    }
}
