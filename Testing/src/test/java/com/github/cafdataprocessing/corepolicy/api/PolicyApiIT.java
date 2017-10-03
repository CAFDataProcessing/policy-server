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
package com.github.cafdataprocessing.corepolicy.api;

import com.cedarsoftware.util.DeepEquals;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cafdataprocessing.corepolicy.common.*;
import com.github.cafdataprocessing.corepolicy.common.dto.*;
import com.github.cafdataprocessing.corepolicy.TestHelper;
import com.github.cafdataprocessing.corepolicy.common.shared.TemporaryEnvChanger;
import com.github.cafdataprocessing.corepolicy.testing.Assume;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.github.cafdataprocessing.corepolicy.TestHelper.getUniqueString;
import static com.github.cafdataprocessing.corepolicy.TestHelper.shouldThrow;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class PolicyApiIT extends PolicyApiTestBase {
    public PolicyApiIT() {
        super();
    }

    @Test
    public void testPolicy() throws Exception {

        Long totalHitsBeforeCreation = 0L;

        {
            PageOfResults<Policy> pageOfResults = sut.retrievePoliciesPage(new PageRequest(1L, 1L));

            totalHitsBeforeCreation = pageOfResults.totalhits;
        }

        Policy createdPolicy = createNewUniqueIndexPolicy();

        createdPolicy.name = getUniqueString("updated");
        createdPolicy.description = "description";
        ObjectMapper mapper = new ObjectMapper();
        createdPolicy.details = mapper.readTree(metadataPolicyJson);
        Policy updatedPolicy = sut.update(createdPolicy);

        assertEquals(createdPolicy.id, updatedPolicy.id);
        assertEquals(createdPolicy.description, updatedPolicy.description);
        assertEquals(createdPolicy.details, updatedPolicy.details);
        assertEquals(createdPolicy.name, updatedPolicy.name);
        assertEquals(createdPolicy.typeId, updatedPolicy.typeId);

        Collection<Policy> policies = sut.retrievePolicies(Arrays.asList(updatedPolicy.id));

        assertEquals(1, policies.size());

        PageRequest pageRequest = new PageRequest(totalHitsBeforeCreation + 1L, 10L);
        PageOfResults<Policy> pageOfResults = sut.retrievePoliciesPage(pageRequest);
        Policy policy = policies.stream().findFirst().get();



        assertEquals(1, pageOfResults.results.size());
        assertEquals(totalHitsBeforeCreation + 1L, (long)pageOfResults.totalhits);
        assertEquals(policy.id, createdPolicy.id );

        sut.deletePolicy(updatedPolicy.id);

        // issue for same page of results, it should be gone.
        pageOfResults = sut.retrievePoliciesPage(pageRequest);

        assertEquals(0, pageOfResults.results.size());
        assertEquals(totalHitsBeforeCreation, (Long)pageOfResults.totalhits);
    }


    @Test
    public void testCreatePolicyType() throws Exception {

        // we can use the simple utility method for this.
        PolicyType createdPolicyType = createNewUniquePolicyType();

        assertNotNull( createdPolicyType );
        assertTrue( createdPolicyType.id > 0 );
    }

    @Test
    public void testCreatePolicyTypeWithConflictMode() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        PolicyType policyType = new PolicyType();
        policyType.name = "test" + randomLetter();
        policyType.description = "description";
        policyType.definition = mapper.readTree(policyTypeJson);
        policyType.shortName = "TestCustomPolicy" + UUID.randomUUID().toString();
        policyType.conflictResolutionMode = ConflictResolutionMode.CUSTOM;

        PolicyType createdPolicyType = sut.create(policyType);

        assertNotNull( createdPolicyType );
        assertEquals(ConflictResolutionMode.CUSTOM, createdPolicyType.conflictResolutionMode);
    }

    @Test
    public void testCreatePolicyTypeWithUTF8Characters() throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        PolicyType policyType = new PolicyType();
        policyType.name = getUniqueString("TestPolicyFunkyChars_Citroên_:");
        policyType.description = "description";
        policyType.definition = mapper.readTree(policyTypeJson);
        policyType.shortName = getUniqueString("TestPolicyFunkyChars_Citroên_:");
        policyType.conflictResolutionMode = ConflictResolutionMode.CUSTOM;

        PolicyType createdPolicyType = sut.create(policyType);

        assertNotNull( createdPolicyType );
        assertNotNull( "Created PolicyType ID can't be null", createdPolicyType.id );
        assertEquals("Create PolicyType name must contain same characters: ", policyType.name, createdPolicyType.name);
        assertEquals( "Create PolicyType internal_name must contain same characters: ", policyType.shortName, createdPolicyType.shortName );

        // ensure the created
    }

    @Test
    public void createExternalPolicyTest() throws Exception {
        Policy policy = new Policy();
        policy.name = getUniqueString("updated");
        policy.description = "description";
        ObjectMapper mapper = new ObjectMapper();
        policy.details = mapper.readTree("{\"externalReference\":\"Test\"}");
        policy.typeId = 2L;

        Policy createdPolicy = sut.create(policy);
        policy.id = createdPolicy.id;

        assertTrue(DeepEquals.deepEquals(policy, createdPolicy));
    }

    @Test
    public void createIncorrectExternalPolicyTest() throws Exception{
        Policy createdPolicy = new Policy();
        createdPolicy.name = getUniqueString("updated");
        createdPolicy.description = "description";
        ObjectMapper mapper = new ObjectMapper();
        createdPolicy.details = mapper.readTree("{}");
        createdPolicy.typeId = 2L;

        shouldThrow(u -> sut.create(createdPolicy));
    }

    @Test
    public void testCreatePolicyType_Failures() throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        {
            // create without internal_name
            PolicyType policy = new PolicyType();
            policy.name = "Test policy name";
            policy.description = "Test policy without internal_name";
            policy.definition = mapper.readTree(policyTypeJson);

            TestHelper.shouldThrow((o) -> {
                sut.create(policy);
            });
        }

        {
            // create without name
            PolicyType policy = new PolicyType();
            policy.definition = mapper.readTree(policyTypeJson);
            policy.description = "Test policy without name";
            policy.shortName = "TestPolicyInternalName" + UUID.randomUUID().toString();

            TestHelper.shouldThrow((o) -> {
                sut.create(policy);
            });
        }

        {
            // create without definition
            PolicyType policy = new PolicyType();
            policy.name = "Test policy name";
            policy.description = "Test policy without definition";
            policy.shortName = "TestPolicyInternalName" + UUID.randomUUID().toString();

            TestHelper.shouldThrow((o) -> {
                sut.create(policy);
            });
        }
    }
    @Test
    public void testCreatePolicyType_Failures_NonUniqueInternalName() throws Exception {

        // we can use the simple utility method for this.
        PolicyType createdPolicyType = createNewUniquePolicyType();

        assertNotNull( createdPolicyType );

        // now we have the policy type, try to create another with the same name.
        PolicyType duplicateWithoutId = new PolicyType();
        duplicateWithoutId.definition = createdPolicyType.definition;
        duplicateWithoutId.name = createdPolicyType.name;
        duplicateWithoutId.description = createdPolicyType.description;

        // important line as this needs to contain same internal name!
        duplicateWithoutId.shortName = createdPolicyType.shortName;

        TestHelper.shouldThrow((o)->{
            PolicyType newPolicy = sut.create(duplicateWithoutId);
        });

    }

    @Test
    public void testUpdatePolicyType() throws Exception {

        // we can use the simple utility method for this.
        PolicyType createdPolicyType = createNewUniquePolicyType();

        assertNotNull( createdPolicyType );

        // Now update this policy and check it gets the updated info.
        PolicyType updatedPolicyType = new PolicyType();
        updatedPolicyType.shortName = createdPolicyType.shortName;
        updatedPolicyType.definition = createdPolicyType.definition;
        updatedPolicyType.name = "Changed Policy Name";
        updatedPolicyType.id = createdPolicyType.id;
        updatedPolicyType.description = "Changed Policy name from: " + createdPolicyType.name + " to: " + updatedPolicyType.name;
        updatedPolicyType.conflictResolutionMode = ConflictResolutionMode.CUSTOM;

        PolicyType newPolicyType = sut.update( updatedPolicyType );

        // ensure our new policy doesn't match the old one, and matches the new definitions
        AssertEquals(newPolicyType, updatedPolicyType);
    }


    @Test
    public void testDeletePolicyType() throws Exception {

        PolicyType createdPolicyType = createNewUniquePolicyType();

        assertTrue(createdPolicyType.id != 0);

        sut.deletePolicyType( createdPolicyType.id );
    }

    @Test
    public void testRetrieveGlobalPolicyType() throws Exception {
        PolicyType comparePolicy = sut.retrievePolicyType(1L);

        assertNotNull(comparePolicy);
        assertEquals(1L, (long) comparePolicy.id);
    }

    @Test
    public void testRetrieveGlobalPolicyTypePage() throws Exception {
        PageOfResults<PolicyType> pageOfResults= sut.retrievePolicyTypesPage(new PageRequest(1L, 10L));

        assertNotNull("Should have page of results", pageOfResults);
        assertTrue("Hit results > 0", (pageOfResults.results.size() > 0));

        // as we now have more than 1 base policy, just look for indexing policy in paged results.
        for ( PolicyType policyType : pageOfResults.results ) {
            if ( policyType.shortName.equalsIgnoreCase("MetadataPolicy")) {
                assertEquals("PolicyID for base data should match.", 1L, (long) policyType.id);
            }
        }
    }

    @Test
    public void testRetrievePolicyTypes() throws Exception {
        // check we can read same policyType back by id!
        PolicyType comparePolicy = sut.retrievePolicyType(1L);

        assertEquals(1L, (long)comparePolicy.id);
    }

    @Test
    public void testRetrievePolicyTypesByName() throws Exception {

        if ( !apiProperties.getMode().equalsIgnoreCase("direct"))
        {
            // only run in direct mysql mode, until removed!
            assertTrue( "Test only runs in direct mode - being deprecated", true);
            return;
        }

        PolicyType createdPolicyType = createNewUniquePolicyType();

        assertTrue(createdPolicyType.id != 0);

        // check we can read same policyType back by id!
        PolicyType comparePolicy = sut.retrievePolicyTypeByName(createdPolicyType.shortName);

        AssertEquals(createdPolicyType, comparePolicy);

    }

    @Test
    public void testRetrievePoliciesByPolicyType() throws Exception {

        // added so we have more than 1 policy type incase its bringing back all for projectid and not restricted by typeid.
        PolicyType dummyPolicyType = createNewUniquePolicyType();
        Policy dummyPolicy = createMetadataPolicyFromTypeId(dummyPolicyType.id);
        sut.create( dummyPolicy );

        PolicyType createdPolicyType = createNewUniquePolicyType();

        // check we can read same policyType back by id!
        PolicyType comparePolicy = sut.retrievePolicyType(createdPolicyType.id);

        AssertEquals(createdPolicyType, comparePolicy);

        // Now create some policies of this type. ( we want to page them so make a few! )
        Collection<Policy> newPolicies = new ArrayList<>();

        int maxPoliciesNeeded = 10;
        for (int index = 0; index < maxPoliciesNeeded; index++) {
            Policy policy = createMetadataPolicyFromTypeId(createdPolicyType.id);

            Policy createdPolicy = sut.create(policy);

            newPolicies.add(createdPolicy);
        }

        PageRequest pageRequest = new PageRequest(1L, 100L);

        Filter filter = Filter.create(ApiStrings.Policy.Arguments.POLICY_TYPE ,createdPolicyType.id);

        // now ensure we got the 10 policies back for this given type.
        PageOfResults<Policy> relatedPolicies = sut.retrievePoliciesPage(pageRequest, filter);

        assertEquals("Should be equal number of related policy results.", newPolicies.size(), relatedPolicies.results.size());

        // check each item exists in our returned policies.
        for (Policy policy : newPolicies) {
            Policy comparison = relatedPolicies.results.stream().filter( u->u.id.equals(policy.id) ).findFirst().get();

            assertEquals( "Policy Ids should be equal", policy.id, comparison.id );
        }
    }

    @Test
    public void testRetrievePolicyTypesPage() throws Exception {

        Collection<PolicyType> newPolicies = new ArrayList<>();

        // add so we have more than 1 policy type.
        newPolicies.add( createNewUniquePolicyType() );
        newPolicies.add(createNewUniquePolicyType());

        // now we have at least 3 items, 2 we have created and one base data - at least.!
        // call to get back first hit.
        PageOfResults<PolicyType> pageOfResults = sut.retrievePolicyTypesPage(new PageRequest(1L, 1L));

        PageOfResults<PolicyType> pageOfResults2 = sut.retrievePolicyTypesPage(new PageRequest(2L, 1L));


        // incase someone runs this test on its own - where we would only get back 1 policy
        //type  i.e. base data. add a few more to ensure its runs correctly.

        assertTrue("Page of results start index 1 - totalHits must be greater than 1.", pageOfResults.totalhits > 1 );
        assertTrue("Page of results start index 2 - totalHits must be greater than 1.", pageOfResults2.totalhits > 1 );

        assertTrue("Results Page must have 1 item.", pageOfResults.results.size() == 1 );
        assertTrue("Results Page2 must have 1 item.", pageOfResults2.results.size() == 1);
        assertTrue("Ids of the 2 policytypes must be different: ",
                (pageOfResults.results.stream().findFirst().get().id != pageOfResults2.results.stream().findFirst().get().id ) );
    }

    @Test
    public void testChangeOfPolicyTypeBaseDataDissallowed(){

        // Skip this test if not in direct mode.
        Assume.assumeTrue(Assume.AssumeReason.BY_DESIGN, "Need to be in API Direct mode / Repository=Hibernate for this test", apiProperties.isInApiMode(ApiProperties.ApiMode.direct) && apiProperties.isInRepository(ApiProperties.ApiDirectRepository.hibernate), genericApplicationContext);

        // check that calling policytype create with projectId of null, fails!
        checkCreatePolicyTypeWithNullProjectThrows();
    }

    @Test
    public void testAdminOfPolicyTypeBaseDataDissallowedByDefault(){

        Assert.assertFalse("Administration of base data should not be allowed by default!", apiProperties.getAdminBaseDataEnabled());
    }

    @Test
    public void testAdminBaseData() throws IOException {
        Assert.assertFalse("Administration of base data should not be allowed by default!", apiProperties.getAdminBaseDataEnabled());

        // we are then going to force the api properties to make this true!
        try (TemporaryEnvChanger adminValue = new TemporaryEnvChanger("api.admin.basedata", "true")) {

            // ensure that our normal policytype create does not allow creation even with this setting.
            checkCreatePolicyTypeWithNullProjectThrows();
        }

    }

    @Test
    public void testCreationOfPolicyTypeWithExistingName() throws IOException {
        Assume.assumeTrue(Assume.AssumeReason.BUG, "PD-868", "testCreationOfPolicyTypeWithExistingName", "Admin Api not supported in anything other than hibernate", apiProperties.isInApiMode(ApiProperties.ApiMode.direct) && apiProperties.isInRepository(ApiProperties.ApiDirectRepository.hibernate), genericApplicationContext);

        String uniqueName = getUniqueString("UniquePolicyTypeName");

        // we are then going to force the api properties to make this true!
        try (TemporaryEnvChanger adminValue = new TemporaryEnvChanger("api.admin.basedata", "true")) {

            // inside this scope we can now use Admin calls.
            AdminApi adminApi = genericApplicationContext.getBean(AdminApi.class);

            PolicyType createdPolicyTypeBase = null;
            PolicyType createdPolicyType = null;

            {
                // Now try to do the same thing, but via our Admin api.
                PolicyType policyType = createNewPolicyType();
                policyType.shortName = uniqueName;

                createdPolicyTypeBase = adminApi.create(policyType);

                assertNotNull(createdPolicyTypeBase.id);
                assertTrue(createdPolicyTypeBase.id != 0);
            }

            // Ensure that if a normal user tries to create one with this name it succeeds!
            {
                PolicyType policyType = createNewPolicyType();
                policyType.shortName = uniqueName;

                createdPolicyType = sut.create(policyType);
                assertNotNull(createdPolicyType.id);
                assertTrue(createdPolicyType.id != 0);
                assertNotEquals("Ensure base data policyType doesn't match user policyType", createdPolicyTypeBase.id, createdPolicyType.id );
            }

            // Finally retrieve it by name, to check it works.
            PolicyType returned = sut.retrievePolicyTypeByName(uniqueName);
            assertNotNull(createdPolicyType.id);
            assertTrue(createdPolicyType.id != 0);
            assertEquals("Ensure returned policyType matches user policyType", createdPolicyType.id, returned.id);
        }
    }




    @Test
    public void testAdminOfPolicyTypeBaseData() throws IOException {
        Assert.assertFalse("Administration of base data should not be allowed by default!", apiProperties.getAdminBaseDataEnabled());

        Assume.assumeTrue(Assume.AssumeReason.DEBT, "testAdminOfPolicyTypeBaseData", "admin api not yet completed for mysql/web.", apiProperties.isInApiMode(ApiProperties.ApiMode.direct) && apiProperties.isInRepository(ApiProperties.ApiDirectRepository.hibernate), genericApplicationContext);

        // we are then going to force the api properties to make this true!
        try (TemporaryEnvChanger adminValue = new TemporaryEnvChanger("api.admin.basedata", "true")) {

            // inside this scope we can now use Admin calls.
            AdminApi adminApi = genericApplicationContext.getBean(AdminApi.class);

            {
                // Now try to do the same thing, but via our Admin api.
                PolicyType policyType = createNewPolicyType();
                PolicyType createdPolicyType = adminApi.create(policyType);

                assertNotNull(createdPolicyType.id);
                assertTrue(createdPolicyType.id != 0);
                assertEquals(createdPolicyType.description, policyType.description);
                assertEquals(createdPolicyType.definition, policyType.definition);
                assertEquals(createdPolicyType.name, policyType.name);
                assertEquals(createdPolicyType.shortName, policyType.shortName);
            }

            // Ensure it also works if the user context has a projectId of NULL which represents,
            // global system startup ( not as a specific user. )
            UserContext userContext = genericApplicationContext.getBean(UserContext.class);

            String origProjId = null;
            try {
                origProjId = userContext.getProjectId();

                userContext.setProjectId(null);

                PolicyType policyType = createNewPolicyType();

                PolicyType createdPolicyType = adminApi.create(policyType);

                assertNotNull(createdPolicyType.id);
                assertTrue(createdPolicyType.id != 0);
                assertEquals(createdPolicyType.description, policyType.description);
                assertEquals(createdPolicyType.definition, policyType.definition);
                assertEquals(createdPolicyType.name, policyType.name);
                assertEquals(createdPolicyType.shortName, policyType.shortName);
            }
            finally
            {
                userContext.setProjectId(origProjId);
            }

        }

    }

    @Test
    public void testAdminApiFailsByDefault() throws IOException {
        // check that without the api.admin.basedata setting the call fails.
        // Now try to do the same thing, but via our Admin api.
        PolicyType policyType = createNewPolicyType();

        AdminApi adminApi = genericApplicationContext.getBean(AdminApi.class);
        shouldThrow((o)-> {
            adminApi.create(policyType);
        });
    }

    private void checkCreatePolicyTypeWithNullProjectThrows() {
        UserContext userContext = genericApplicationContext.getBean(UserContext.class);

        String origProjId = null;
        try {
            origProjId = userContext.getProjectId();

            userContext.setProjectId(null);
            shouldThrow((u) ->
            {
                try {
                    createNewUniquePolicyType();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        finally
        {
            userContext.setProjectId(origProjId);
        }
    }

    public static String metadataPolicyJson = "{\n" +
            "\"title\" : \"Test Metadata Policy\"," +
            "\"description\" : \"Example Metadata Policy Details for unit tests\"" +
            "}";

    public static String externalPolicyJson = "{\n" +
            "\"title\" : \"Test External Policy\"," +
            "\"description\" : \"Example External Policy Details for unit tests\"," +
            "\"externalReference\" : \"Test\"" +
            "}";

    public static String policyTypeJson = "{\n" +
            "    \"title\": \"Metadata Policy Type\",\n" +
            "    \"description\": \"An Metadata policy.\",\n" +
            "    \"type\": \"object\",\n" +
            "    \"properties\": {\n" +
            "        \"fieldActions\": {\n" +
            "            \"type\": \"array\",\n" +
            "            \"items\": {\n" +
            "                \"title\": \"Field Action\",\n" +
            "                \"type\": \"object\",\n" +
            "                \"properties\": {\n" +
            "                    \"name\": {\n" +
            "                        \"description\": \"The name of the field to perform the action on.\",\n" +
            "                        \"type\": \"string\",\n" +
            "                        \"minLength\": 1\n" +
            "                    },\n" +
            "                    \"action\": {\n" +
            "                        \"description\": \"The type of action to perform on the field.\",\n" +
            "                        \"type\": \"string\",\n" +
            "                        \"enum\": [\n" +
            "                            \"ADD_FIELD_VALUE\"\n" +
            "                        ]\n" +
            "                    },\n" +
            "                    \"value\": {\n" +
            "                        \"description\": \"The value to use for the field action.\",\n" +
            "                        \"type\": \"string\"\n" +
            "                    }\n" +
            "                },\n" +
            "                \"required\": [\"name\", \"action\"]\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";



    // utility methods
    private PolicyType createNewUniquePolicyType() throws IOException {

        PolicyType policyType = createNewPolicyType();

        PolicyType createdPolicyType = sut.create(policyType);

        assertNotNull(createdPolicyType.id);
        assertTrue(createdPolicyType.id != 0);
        assertEquals(createdPolicyType.description, policyType.description);
        assertEquals(createdPolicyType.definition, policyType.definition);
        assertEquals(createdPolicyType.name, policyType.name);
        assertEquals(createdPolicyType.shortName, policyType.shortName);

        return createdPolicyType;
    }

    private PolicyType createNewPolicyType() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        PolicyType policyType = new PolicyType();
        policyType.name = "test" + randomLetter();
        policyType.description = LONG_DESCRIPTION_BLOB;
        policyType.definition = mapper.readTree(policyTypeJson);
        policyType.shortName = "TestCustomPolicy" + UUID.randomUUID().toString();
        return policyType;
    }


    private void AssertEquals(PolicyType newPolicyType, PolicyType originalPolicyType) {
        assertEquals(originalPolicyType.description, newPolicyType.description);
        assertEquals(originalPolicyType.definition, newPolicyType.definition);
        assertEquals(originalPolicyType.name, newPolicyType.name);
        assertEquals(originalPolicyType.id, newPolicyType.id);
        assertEquals(originalPolicyType.shortName, newPolicyType.shortName);
    }

    private static Character randomLetter(){
        Random r = new Random();

        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        return alphabet.charAt(r.nextInt(alphabet.length()));
    }

    public static Policy newUniqueIndexPolicy() throws IOException {
        return createMetadataPolicyFromTypeId(1L);
    }

    public static Policy createMetadataPolicyFromTypeId( Long typeId ) throws IOException {
        Policy policy = new Policy();
        policy.name = "Policy " + randomLetter();
        policy.description = "des";
        policy.typeId = typeId;
        ObjectMapper mapper = new ObjectMapper();
        policy.details = mapper.readTree(metadataPolicyJson);

        return policy;
    }

    public static Policy newUniqueExternalPolicy() throws IOException {
        Policy policy = new Policy();
        policy.name = "Policy " + randomLetter();
        policy.description = "des";
        policy.typeId = 2L;
        ObjectMapper mapper = new ObjectMapper();
        policy.details = mapper.readTree(externalPolicyJson);

        return policy;
    }

    private Policy createNewUniqueExternalPolicy() throws IOException {
        Policy policy = newUniqueExternalPolicy();

        Policy createdPolicy = sut.create(policy);

        // ensure its ok so far!
        assertEquals(policy.description, createdPolicy.description);
        assertEquals(policy.details, createdPolicy.details);
        assertEquals(policy.name, createdPolicy.name);
        assertEquals(policy.typeId, createdPolicy.typeId);

        return createdPolicy;
    }

    private Policy createNewUniqueIndexPolicy() throws IOException {
        Policy policy = newUniqueIndexPolicy();

        Policy createdPolicy = sut.create(policy);

        // ensure its ok so far!
        assertEquals(policy.description, createdPolicy.description);
        assertEquals(policy.details, createdPolicy.details);
        assertEquals(policy.name, createdPolicy.name);
        assertEquals(policy.typeId, createdPolicy.typeId);

        return createdPolicy;
    }

    @Test
    public void createPolicyCheckDefaultPriority() throws IOException {
        Policy policy = new Policy();
        policy.name = "Policy";
        policy.description = "des";
        policy.typeId = 1L;
        ObjectMapper mapper = new ObjectMapper();
        policy.details = mapper.readTree(metadataPolicyJson);

        Policy createdPolicy = sut.create(policy);

        // ensure its ok so far!
        assertEquals(policy.description, createdPolicy.description);
        assertEquals(policy.details, createdPolicy.details);
        assertEquals(policy.name, createdPolicy.name);
        assertEquals(policy.typeId, createdPolicy.typeId);
        assertEquals("Default priority not set to 0",(Integer)0, createdPolicy.priority);

        Policy retrievedPolicy = sut.retrievePolicies(Arrays.asList(createdPolicy.id)).stream().findFirst().get();

        assertTrue(DeepEquals.deepEquals(createdPolicy, retrievedPolicy));
    }

    @Test
    public void createPolicyType_InternalName_MustBeUnique() throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        PolicyType policyType = new PolicyType();
        policyType.name = "test";
        policyType.description = "description";
        policyType.definition = mapper.readTree(policyTypeJson);
        policyType.shortName = "TestCustomPolicy" + UUID.randomUUID().toString();

        PolicyType createdPolicyType = sut.create(policyType);

        assertNotNull(createdPolicyType.id);
        assertTrue(createdPolicyType.id != 0);
        assertEquals(createdPolicyType.description, policyType.description);
        assertEquals(createdPolicyType.definition, policyType.definition);
        assertEquals(createdPolicyType.name, policyType.name);
        assertEquals(createdPolicyType.shortName, policyType.shortName);

        // now try to create another with the same internal_name / shortName.
        PolicyType policyTypeCheck = new PolicyType();
        policyTypeCheck.name = policyType.name;
        policyTypeCheck.description = policyType.description;
        policyTypeCheck.definition = policyType.definition;
        policyTypeCheck.shortName = policyType.shortName;

        TestHelper.shouldThrow((o) -> {
            sut.create(policyType);
        });

    }

    @Test
    public void createCustomPolicy() throws IOException {

        PolicyType policyType = new PolicyType();
        policyType.name = "test";
        policyType.description = "description";

        policyType.shortName = "TestCustomPolicy" + UUID.randomUUID().toString();

        // should fail validation, without JSON definition
        TestHelper.shouldThrow((o) -> {
            sut.create(policyType);
        });

        final String customPolicyTypeJson = "{\n" +
                "    \"title\": \"Custom Policy Type\",\n" +
                "    \"description\": \"An custom test policy type.\",\n" +
                "    \"type\": \"object\",\n" +
                "    \"properties\": {\n" +
                "        \"priority\": {\n" +
                "            \"type\": \"string\",\n" +
                "            \"enum\": [\n" +
                "                \"HIGH\",\n" +
                "                \"MIDDLE\",\n" +
                "                \"LOW\"\n" +
                "            ]\n" +
                "        },\n" +
                "        \"notes\": {\n" +
                "            \"type\": \"string\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"required\": [\"priority\"]\n" +
                "}";

        ObjectMapper mapper = new ObjectMapper();
        policyType.definition = mapper.readTree(customPolicyTypeJson);

        PolicyType createdPolicyType = sut.create(policyType);

        assertNotNull(createdPolicyType.id);
        assertTrue(createdPolicyType.id != 0);
        assertEquals(createdPolicyType.description, policyType.description);
        assertEquals(createdPolicyType.definition, policyType.definition);
        assertEquals(createdPolicyType.name, policyType.name);
        assertEquals(createdPolicyType.shortName, policyType.shortName);

        // Now try to create a policy of this type.

        Policy policy = new Policy();
        policy.name = "Custom Policy Test";
        policy.description = "Unit testing custom policies.";
        policy.typeId = createdPolicyType.id;
        policy.details = mapper.readTree("{\n" +
                "\"title\" : \"Test Custom Policy\"" +
                "}");

        TestHelper.shouldThrow((o) -> {
            sut.create(policy);
        });

        policy.details = mapper.readTree("{\n" +
                "\"title\" : \"Test Custom Policy\"," +
                "\"description\" : \"Example Custom Policy of Type: " + policyType.shortName + "\"" +
                "}");

        // Now try again with description in place
        TestHelper.shouldThrow((o) -> {
            sut.create(policy);
        });

        // ensure enum is incorrect!
        policy.details = mapper.readTree("{\n" +
                "\"title\" : \"Test Custom Policy\"," +
                "\"description\" : \"Example Custom Policy of Type: " + policyType.shortName + "\"," +
                "\"priority\" : \"SOMESTRANGEVALUE\"" +
                "}");

        // Now try again with description in place
        TestHelper.shouldThrow((o) -> {
            sut.create(policy);
        });


        policy.details = mapper.readTree("{" +
                "\"title\" : \"Test Custom Policy\", " +
                "\"description\" : \"Example Custom Policy of Type: " + policyType.shortName + "\", " +
                "\"priority\" : \"HIGH\" " +
                "}");

        Policy createdPolicy = sut.create(policy);

        // ensure its ok so far!
        assertEquals(policy.description, createdPolicy.description);
        assertEquals(policy.details, createdPolicy.details);
        assertEquals(policy.name, createdPolicy.name);
        assertEquals(policy.typeId, createdPolicy.typeId);
        assertEquals("Default priority not set to 0",(Integer)0, createdPolicy.priority);

        Policy retrievedPolicy = sut.retrievePolicies(Arrays.asList(createdPolicy.id)).stream().findFirst().get();

        assertTrue(DeepEquals.deepEquals(createdPolicy, retrievedPolicy));
    }

    @Test
     public void createPolicyCheckPriority() throws IOException {
        Policy policy = new Policy();
        policy.name = "Policy";
        policy.description = "des";
        policy.typeId = 1L;
        ObjectMapper mapper = new ObjectMapper();
        policy.details = mapper.readTree(metadataPolicyJson);
        policy.priority = 100;

        Policy createdPolicy = sut.create(policy);

        // ensure its ok so far!
        assertEquals(policy.description, createdPolicy.description);
        assertEquals(policy.details, createdPolicy.details);
        assertEquals(policy.name, createdPolicy.name);
        assertEquals(policy.typeId, createdPolicy.typeId);
        assertEquals("Priority doesnt match", policy.priority, createdPolicy.priority);

        Policy retrievedPolicy = sut.retrievePolicies(Arrays.asList(createdPolicy.id)).stream().findFirst().get();

        assertTrue(DeepEquals.deepEquals(createdPolicy, retrievedPolicy));
    }

    @Test
    public void updatePolicyCheckPriority() throws IOException {
        Policy policy = new Policy();
        policy.name = "Policy";
        policy.description = "des";
        policy.typeId = 1L;
        ObjectMapper mapper = new ObjectMapper();
        policy.details = mapper.readTree(metadataPolicyJson);
        policy.priority = 100;

        Policy createdPolicy = sut.create(policy);

        createdPolicy.priority = 200;

        Policy updatedPolicy = sut.update(createdPolicy);

        assertTrue(DeepEquals.deepEquals(createdPolicy, updatedPolicy));
        assertEquals((Integer)200, updatedPolicy.priority);
    }

    @Test
    public void testRetrieveDeletedPolicies() throws IOException {

        PageRequest pageRequest = new PageRequest(1L,10L);
        Long initialHitCount = sut.retrievePoliciesPage(pageRequest).totalhits;

        Policy createdPolicy1 = createNewUniqueIndexPolicy();
        Policy createdPolicy2 = createNewUniqueIndexPolicy();

        assertEquals(2, sut.retrievePolicies(Arrays.asList(createdPolicy1.id, createdPolicy2.id)).size());

        pageRequest = new PageRequest(1L,10L);
        assertEquals((Long) (initialHitCount + 2L), sut.retrievePoliciesPage(pageRequest).totalhits);

        sut.deletePolicy(createdPolicy1.id);

        // try to get both policies ( so should only be 1 item. )
        shouldThrow(u -> sut.retrievePolicies(Arrays.asList(createdPolicy1.id)).size());

        shouldThrow(u -> sut.retrievePolicies(Arrays.asList(createdPolicy1.id, createdPolicy2.id)).size());

        assertEquals(1, sut.retrievePolicies(Arrays.asList(createdPolicy2.id)).size());

        // Finally total hit count should also drop by one.
        assertEquals((Long)(initialHitCount + 1L), sut.retrievePoliciesPage(pageRequest).totalhits);
    }

    @Test
    public void testCannotDeletePolicyInUse() throws IOException {

        Policy createdPolicy = createNewUniqueIndexPolicy();

        ClassificationApi classificationApi = genericApplicationContext.getBean(ClassificationApi.class);

        DocumentCollection collection = new DocumentCollection();
        collection.name = "Test";
        collection.policyIds = new HashSet<>(Arrays.asList(createdPolicy.id));

        classificationApi.create(collection);

        shouldThrow(u -> sut.deletePolicy(createdPolicy.id));
    }

    @Test
    public void updateDeletedPolicy() throws IOException {
        Policy policy = new Policy();
        policy.name = "Policy";
        policy.description = "des";
        policy.typeId = 1L;
        ObjectMapper mapper = new ObjectMapper();
        policy.details = mapper.readTree(metadataPolicyJson);
        policy.priority = 100;

        Policy createdPolicy = sut.create(policy);

        createdPolicy.priority = 200;

        sut.deletePolicy(createdPolicy.id);

        shouldThrow(u -> sut.update(createdPolicy));
    }

    @Test
    public void testReturnPageOrderPolicy() throws IOException {
        Collection<Policy> newItems = new LinkedList<>();

        {
            newItems.add(createNewUniqueIndexPolicy());
            newItems.add(createNewUniqueIndexPolicy());
            newItems.add(createNewUniqueIndexPolicy());
        }

        checkSortedItems(newItems);
    }


    private void checkSortedItems(Collection<Policy> newItems) {
        List<Policy> sortedItems;
        PageRequest pageRequest = new PageRequest(1L, 10L);
        PageOfResults<Policy> pageOfResults = sut.retrievePoliciesPage (pageRequest);
        List<Policy> allItems = new ArrayList<>();

        // Go through and reqeust all pages, and ensure the sorting matches that expected.
        // Now finally ensure we get all the items in one big page to compare.

        // as the amount of data increases the length of time
        // to get all results increases. As such batch this in smaller amounts.
        long maxPageSize = 50;
        long startIndex = 1;
        while ( allItems.size() < pageOfResults.totalhits )
        {
            pageOfResults = sut.retrievePoliciesPage(new PageRequest(startIndex, maxPageSize));

            if ( pageOfResults.results.size() == 0 ) {
                break;
            }

            allItems.addAll(pageOfResults.results);
            startIndex += maxPageSize;
        }


        sortedItems = allItems.stream().sorted((c1, c2) -> {
            if (c1.name == null ) {
                if(c2.name == null) {
                    return c1.id.compareTo(c2.id);
                }
                return -1; // null comes before any text
            } else if (c2.name == null) {
                return 1; // source has text, dest null, so after
            }
            else{
                return c1.name.compareToIgnoreCase(c2.name);
            }
        }).collect(Collectors.toList());

        assertEquals("Sorted Items size, should match total hits", Long.valueOf(sortedItems.size()), pageOfResults.totalhits);

        // Now go through the default order we got the results back in ( allItems ) and our sorted list. ( sortedItems )
        for (int i = 0; i < sortedItems.size(); i++) {
            assertEquals("Order of id not correct for index: " + String.valueOf(i), sortedItems.get(i).id, allItems.get(i).id);
            assertEquals("Order of Name not correct for index: " + String.valueOf(i), sortedItems.get(i).name, allItems.get(i).name);
        }

    }

    @Test
    public void testReturnPageOrderPolicyType() throws IOException {
        Collection<PolicyType> newItems = new LinkedList<>();

        {
            newItems.add(createNewUniquePolicyType());
            newItems.add(createNewUniquePolicyType());
            newItems.add(createNewUniquePolicyType());
        }

        PageRequest pageRequest = new PageRequest(1L, 10L);
        PageOfResults<PolicyType> pageOfResults = sut.retrievePolicyTypesPage(pageRequest);

        List<PolicyType> sortedItems = pageOfResults.results.stream().sorted((c1, c2) -> c1.name.compareTo(c2.name)).collect(Collectors.toList());

        assertEquals(sortedItems.size(), pageOfResults.results.size());
        for (int i = 0; i < sortedItems.size(); i++) {
            assertEquals("Order not correct", sortedItems.get(i).id, pageOfResults.results.stream().collect(Collectors.toList()).get(i).id);
        }
    }

    // moved to keep this at the bottom of this file instead of scrolling past it every time.
    private final static String LONG_DESCRIPTION_BLOB = "description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description description ";

}
