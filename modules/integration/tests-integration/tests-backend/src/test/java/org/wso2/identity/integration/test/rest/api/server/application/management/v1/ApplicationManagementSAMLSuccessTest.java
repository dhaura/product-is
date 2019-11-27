/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.identity.integration.test.rest.api.server.application.management.v1;

import io.restassured.response.Response;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.engine.context.TestUserMode;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

public class ApplicationManagementSAMLSuccessTest extends ApplicationManagementBaseTest {

    private static final String SAML_APP_NAME = "My SAML App";
    private static final String ISSUER = "https://sp.example.com/shibboleth";

    private String samlAppPostRequest;
    private String createdAppId;

    @Factory(dataProvider = "restAPIUserConfigProvider")
    public ApplicationManagementSAMLSuccessTest(TestUserMode userMode, String samlAppPostRequest) throws Exception {

        super(userMode);
        this.samlAppPostRequest = samlAppPostRequest;
    }

    @DataProvider(name = "restAPIUserConfigProvider")
    public static Object[][] restAPIUserConfigProvider() {

        return new Object[][]{
                {TestUserMode.SUPER_TENANT_ADMIN, "create-saml-app-with-metadata-file.json"},
                {TestUserMode.TENANT_ADMIN, "create-saml-app-with-metadata-file.json"},

                {TestUserMode.SUPER_TENANT_ADMIN, "create-saml-app-with-manual-config.json"},
                {TestUserMode.TENANT_ADMIN, "create-saml-app-with-manual-config.json"}
        };
    }

    @Test
    public void testCreateSAMLAppWithMetadataFile() throws Exception {

        String body = readResource(samlAppPostRequest);
        Response responseOfPost = getResponseOfPost(APPLICATION_MANAGEMENT_API_BASE_PATH, body);
        responseOfPost.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_CREATED)
                .header(HttpHeaders.LOCATION, notNullValue())
                .body(notNullValue())
                .body("name", equalTo(SAML_APP_NAME));

        String location = responseOfPost.getHeader(HttpHeaders.LOCATION);
        createdAppId = location.substring(location.lastIndexOf("/") + 1);
    }

    @Test(dependsOnMethods = "testCreateSAMLAppWithMetadataFile")
    public void testGetSAMLInboundDetails() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + "/inbound-protocols/saml";

        Response responseOfGet = getResponseOfGet(path);
        responseOfGet.then()
                .log().ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .body("issuer", equalTo(ISSUER));
    }

    @Test(dependsOnMethods = "testGetSAMLInboundDetails")
    public void testDeleteSAMLInbound() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId + "/inbound-protocols/saml";

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Make sure we don't have the SAML inbound details.
        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }

    @Test(dependsOnMethods = "testDeleteSAMLInbound")
    public void testPutSAMLInbound() throws Exception {

        // Create SAML Inbound with a PUT
        // GET and assert inbound details exists

        // Update SAML Inbound with a PUT
        // GET and assert inbound details exists.

    }

    @Test(dependsOnMethods = "testPutSAMLInbound")
    public void testDeleteApplication() throws Exception {

        String path = APPLICATION_MANAGEMENT_API_BASE_PATH + "/" + createdAppId;

        Response responseOfDelete = getResponseOfDelete(path);
        responseOfDelete.then()
                .log()
                .ifValidationFails()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        // Make sure we don't have the SAML inbound details.
        getResponseOfGet(path).then().assertThat().statusCode(HttpStatus.SC_NOT_FOUND);
    }
}
