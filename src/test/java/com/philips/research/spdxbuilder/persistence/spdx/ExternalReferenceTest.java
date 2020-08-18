/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.persistence.spdx;

import com.philips.research.spdxbuilder.core.bom.Package;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExternalReferenceTest {
    private static final String TYPE = "Type";
    private static final String NAMESPACE = "Namespace";
    private static final String NAME = "Name";
    private static final String VERSION = "Version";

    @Test
    void createsPurlFromPackage() {
        final var pkg = new Package(TYPE, NAMESPACE, NAME, VERSION);

        final var ref = ExternalReference.purl(pkg);

        assertThat(ref.toString()).isEqualTo("PACKAGE-MANAGER purl pkg:" + TYPE + "/" + NAMESPACE + "/" + NAME + "@" + VERSION);
    }
}
