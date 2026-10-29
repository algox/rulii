/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rulii.validation.rules.email;

import org.rulii.lib.apache.validation.DomainValidator;
import org.rulii.lib.apache.validation.EmailValidator;
import org.rulii.lib.apache.validation.InetAddressValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An email validator that judges the domain part by shape alone: a dot-separated sequence of
 * host name labels (letters, digits and hyphens, a hyphen neither first nor last, at most 63
 * characters each, 253 in all), or an IP address in brackets. No top-level domain registry is
 * consulted, so a local or single-label host ({@code user@localhost}, {@code bob@intranet}), a
 * reserved or newly delegated top-level domain all pass, as the Bean Validation reference
 * implementation accepts them. The local part is validated as the standard validator does.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
final class AnyDomainEmailValidator extends EmailValidator {

    private static final long serialVersionUID = 1L;

    private static final Pattern IP_DOMAIN = Pattern.compile("^\\[(.*)\\]$");
    private static final Pattern LABEL = Pattern.compile("[\\p{L}\\p{N}](?:[\\p{L}\\p{N}-]{0,61}[\\p{L}\\p{N}])?");
    private static final int MAX_DOMAIN_LENGTH = 253;

    AnyDomainEmailValidator() {
        super(true, true, DomainValidator.getInstance(true));
    }

    @Override
    protected boolean isValidDomain(String domain) {
        Matcher ip = IP_DOMAIN.matcher(domain);

        if (ip.matches()) return InetAddressValidator.getInstance().isValid(ip.group(1));
        if (domain.isEmpty() || domain.length() > MAX_DOMAIN_LENGTH) return false;

        for (String label : domain.split("\\.", -1)) {
            if (!LABEL.matcher(label).matches()) return false;
        }

        return true;
    }
}
