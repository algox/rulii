/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2026, Algorithmx Inc.
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
package org.rulii.script.el;

import jakarta.el.ExpressionFactory;
import org.rulii.model.UnrulyException;

/**
 * The one {@link ExpressionFactory} the EL language uses, created on first use. Kept apart from the
 * factory so that the factory class loads even when the EL API is absent.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
final class ElSupport {

    private static volatile ExpressionFactory expressionFactory;

    private ElSupport() {
        super();
    }

    /**
     * Whether an EL implementation can be found through the API's discovery.
     */
    static boolean hasImplementation() {
        try {
            return expressionFactory() != null;
        } catch (RuntimeException | LinkageError e) {
            return false;
        }
    }

    /**
     * The shared expression factory; thread-safe, as the specification requires of it.
     *
     * @throws UnrulyException if no EL implementation is available.
     */
    static ExpressionFactory expressionFactory() {
        ExpressionFactory result = expressionFactory;

        if (result == null) {
            synchronized (ElSupport.class) {
                result = expressionFactory;
                if (result == null) {
                    try {
                        result = ExpressionFactory.newInstance();
                    } catch (RuntimeException e) {
                        throw new UnrulyException("No Jakarta Expression Language implementation is available; add one (org.glassfish.expressly:expressly"
                                + " or org.apache.tomcat.embed:tomcat-embed-el) to use the [jakarta-el] scripting language.", e);
                    }
                    expressionFactory = result;
                }
            }
        }

        return result;
    }
}
