/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.license;

import com.philips.research.spdxbuilder.core.BusinessException;
import com.philips.research.spdxbuilder.core.domain.Package;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.JsonBody;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

class LicenseScannerClientTest {
    private static final int PORT = 1080;
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "My/#?Namespace";
    private static final String NAME = "My/#?Name";
    private static final String VERSION = "My/#?Version";
    private static final URI LOCATION = URI.create("http://example.com");
    private static final String LICENSE = "Apache-2.0";
    private static final String SCAN_ID = "ScanId";

    private static ClientAndServer mockServer;

    private final LicenseScannerClient client = new LicenseScannerClient(URI.create("http://localhost:" + PORT));
    private final Package pkg = new Package(TYPE, NAMESPACE, NAME, VERSION);

    @BeforeAll
    static void beforeAll() {
        mockServer = startClientAndServer(PORT);
    }

    @AfterAll
    static void afterAll() {
        mockServer.stop();
    }

    @AfterEach
    void afterEach() {
        mockServer.reset();
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void queriesLicense() {
        pkg.setSourceLocation(LOCATION);
        mockServer.when(request().withMethod("POST")
                .withPath("/packages")
                .withBody(JsonBody.json(new JSONObject().put("purl", pkg.getPurl()).put("location", LOCATION).toString())))
                .respond(response().withStatusCode(200).withBody(new JSONObject()
                        .put("license", LICENSE)
                        .put("confirmed", true).toString()));

        final var license = client.scanLicense(pkg).get();

        assertThat(license.getLicense()).contains(LICENSE);
        assertThat(license.isConfirmed()).isTrue();
    }

    @Test
    void ignoresEmptyLicense() {
        mockServer.when(request().withMethod("POST")
                .withPath("/packages")
                .withBody(JsonBody.json(new JSONObject().put("purl", pkg.getPurl()).toString())))
                .respond(response().withStatusCode(200).withBody("{}"));

        final var license = client.scanLicense(pkg);

        assertThat(license).isEmpty();
    }

    @Test
    void contestsScan_differentFromDeclaredLicense() {
        pkg.setDeclaredLicense("Other");
        mockServer.when(request().withMethod("POST")
                .withPath("/packages")
                .withBody(JsonBody.json(new JSONObject().put("purl", pkg.getPurl()).toString())))
                .respond(response().withStatusCode(200)
                        .withBody(new JSONObject()
                                .put("id", SCAN_ID)
                                .put("license", LICENSE).toString()));

        client.scanLicense(pkg);

        System.out.println(mockServer.retrieveLogMessages(request().withMethod("POST")
                .withPath("/packages")
                .withBody(new JSONObject().put("purl", pkg.getPurl()).toString())));

        final var body = new JSONObject().put("license", "Other");
        mockServer.verify(request().withMethod("POST")
                .withPath(String.format("/scans/%s/contest", SCAN_ID))
                .withBody(new JsonBody(body.toString())));
    }

    @Test
    void ignores_serverNotReachable() {
        var serverlessClient = new LicenseScannerClient(URI.create("http://localhost:1234"));

        assertThatThrownBy(() -> serverlessClient.scanLicense(pkg))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("not reachable");
    }

    @Test
    void throws_unexpectedResponseFromServer() {
        // Default not-found response
        assertThatThrownBy(() -> client.scanLicense(pkg))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("status 404");
    }
}
