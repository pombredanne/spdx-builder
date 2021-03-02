/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2021 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.spdxbuilder.persistence.blackduck;

import java.util.Optional;
import java.util.UUID;

public interface BlackDuckProduct {
    UUID getId();

    String getName();

    Optional<String> getDescription();
}
