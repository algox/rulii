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
package org.rulii.test.bind;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.model.UnrulyException;

/**
 * Tests for {@link BindingDeclaration}, in particular the explicit-name factory
 * {@link BindingDeclaration#of(String, Object)} used when binding names are only
 * known at runtime (e.g. XML-declared rule flows).
 *
 * @author Max Arulananthan
 */
public class BindingDeclarationTest {

    public BindingDeclarationTest() {
        super();
    }

    @Test
    public void testOfReportsExplicitNameAndValue() {
        BindingDeclaration<Integer> declaration = BindingDeclaration.of("age", 25);

        Assertions.assertEquals("age", declaration.name());
        Assertions.assertEquals(25, declaration.value());
    }

    @Test
    public void testOfSupportsNullValue() {
        BindingDeclaration<String> declaration = BindingDeclaration.of("empty", null);

        Assertions.assertEquals("empty", declaration.name());
        Assertions.assertNull(declaration.value());
    }

    @Test
    public void testOfRejectsInvalidName() {
        Assertions.assertThrows(UnrulyException.class, () -> BindingDeclaration.of("not.a.valid.name", 1));
        Assertions.assertThrows(UnrulyException.class, () -> BindingDeclaration.of(null, 1));
    }

    @Test
    public void testOfDeclarationBindsLikeALambda() {
        Bindings bindings = Bindings.builder().standard();
        bindings.bind(BindingDeclaration.of("score", 42));

        Assertions.assertTrue(bindings.contains("score"));
        Assertions.assertEquals(42, (Integer) bindings.getValue("score"));
    }

    @Test
    public void testLambdaFormStillDerivesNameFromParameter() {
        BindingDeclaration<Integer> declaration = total -> 100;

        Assertions.assertEquals("total", declaration.name());
        Assertions.assertEquals(100, declaration.value());
    }
}
