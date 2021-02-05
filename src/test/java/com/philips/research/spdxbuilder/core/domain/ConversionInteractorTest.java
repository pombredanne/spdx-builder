/*
 * Copyright (c) 2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.spdxbuilder.core.domain;

import com.philips.research.spdxbuilder.core.BomReader;
import com.philips.research.spdxbuilder.core.BomWriter;
import com.philips.research.spdxbuilder.core.ConversionService;
import com.philips.research.spdxbuilder.core.KnowledgeBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ConversionInteractorTest {
    private static final File ORT_FILE = Path.of("src", "main", "resources", "ort_sample.yml").toFile();
    private static final String TYPE = "Type";
    private static final String GROUP = "Namespace";
    private static final String PROJECT = "Project";
    private static final String ORGANIZATION = "Organization";
    private static final String NAME = "Package";
    private static final String VERSION = "Version";
    private static final String LICENSE = "License";
    private static final URI LOCATION = URI.create("www.example.com");
    private static final String COMMENT = "Comment";
    private static final URI NAMESPACE_URI = URI.create("http://example.com");
    private static final URI PURL = URI.create("pkg:/group/name");

    private final BomReader reader = mock(BomReader.class);
    private final BomWriter writer= mock(BomWriter.class);
    private final KnowledgeBase knowledgeBase = mock(KnowledgeBase.class);
    private final BillOfMaterials bom = new BillOfMaterials();
    private final ConversionService interactor = new ConversionInteractor(reader, writer, bom)
            .setKnowledgeBase(knowledgeBase);
    private final Package project = new Package(TYPE, GROUP, PROJECT, VERSION);
    private final Package pkg = new Package(TYPE, GROUP, NAME, VERSION);

    @Test
    void convertsBillOfMaterials() {
        interactor.convert();

        verify(reader).read(bom);
        verify(knowledgeBase).enhance(bom);
        verify(writer).write(bom);
    }

    @Test
    void skipsEnhancement_noKnowledgeBaseConfigured() {
        //noinspection ConstantConditions
        ((ConversionInteractor)interactor).setKnowledgeBase(null);

        interactor.convert();

        verify(knowledgeBase, never()).enhance(bom);
    }

    @Test
    void setsDocumentProperties() {
        interactor.setDocument(PROJECT, ORGANIZATION);
        interactor.setComment(COMMENT);

        assertThat(bom.getTitle()).isEqualTo(PROJECT);
        assertThat(bom.getOrganization()).contains(ORGANIZATION);
        assertThat(bom.getComment()).contains(COMMENT);
    }

    @Test
    void setsDocumentIdentification() {
        interactor.setDocNamespace(NAMESPACE_URI);
        interactor.setDocReference(PROJECT);

        assertThat(bom.getNamespace()).contains(NAMESPACE_URI);
        assertThat(bom.getIdentifier()).contains(PROJECT);
    }

//    @Nested
//    class Curation {
//        private final Package otherPkg = new Package(TYPE, GROUP, NAME, VERSION);
//
//        @BeforeEach
//        void setUp() {
//            pkg.setPurl(PURL);
//            bom.addPackage(pkg).addPackage(otherPkg);
//        }
//
//        @Test
//        void curatesPackageLicense() {
//            interactor.curatePackageLicense(PURL, LICENSE);
//
//            assertThat(pkg.getConcludedLicense()).contains(LICENSE);
//            assertThat(otherPkg.getConcludedLicense()).isEmpty();
//        }
//
//        @Test
//        void curatesPackageSource() {
//            interactor.curatePackageSource(PURL, LOCATION);
//
//            assertThat(pkg.getSourceLocation()).contains(LOCATION);
//            assertThat(otherPkg.getSourceLocation()).isEmpty();
//        }
//    }
}
