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
package org.rulii.test.core;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.model.MethodDefinition;
import org.rulii.model.ResultType;
import org.rulii.model.SourceDefinition;
import org.rulii.model.function.Function;
import org.rulii.script.Script;
import org.rulii.script.janino.JaninoScriptProcessorFactory;
import org.rulii.util.TypeReference;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import static org.rulii.model.function.Functions.function;

/**
 * Tests {@link ResultType}: the factories, the derivation from a function, assignability and the description.
 *
 * @author Max Arulananthan
 * @since 2.2
 */
public class ResultTypeTest {

    private static final Type LIST_OF_STRING = new TypeReference<List<String>>() {}.getType();

    @SuppressWarnings("unused")
    public static class Names {
        public List<String> names() {
            return new ArrayList<>();
        }

        public void nothing() {}

        public int count() {
            return 0;
        }
    }

    @Test
    public void factoriesRecordTheTypeAndTheOrigin() {
        Assertions.assertEquals(ResultType.Origin.DECLARED, ResultType.declared(String.class).getOrigin());
        Assertions.assertEquals(String.class, ResultType.declared(String.class).getType());
        Assertions.assertEquals(ResultType.Origin.DERIVED, ResultType.derived(Integer.class).getOrigin());
        Assertions.assertEquals(ResultType.Origin.DEFAULT, ResultType.byDefault(Integer.class).getOrigin());
        Assertions.assertTrue(ResultType.declared(String.class).isKnown());

        Assertions.assertEquals(ResultType.Origin.UNKNOWN, ResultType.unknown().getOrigin());
        Assertions.assertNull(ResultType.unknown().getType());
        Assertions.assertFalse(ResultType.unknown().isKnown());
        Assertions.assertEquals(ResultType.Origin.NONE, ResultType.none().getOrigin());
        Assertions.assertNull(ResultType.none().getType());
        Assertions.assertFalse(ResultType.none().isKnown());

        Assertions.assertEquals(ResultType.declared(Object.class), ResultType.declared(Object.class), "Object is kept when the author says so");
        Assertions.assertEquals(Object.class, ResultType.declared(Object.class).getType());
        Assertions.assertThrows(IllegalArgumentException.class, () -> ResultType.declared(null));
    }

    @Test
    public void concreteReturnTypeOfFunctions() {
        Function<Integer> typed = function(() -> 7);
        Assertions.assertEquals(Integer.class, ResultType.concreteReturnType(typed));

        Function<?> generic = (org.rulii.context.RuleContext ctx) -> ctx.getBindings().getValue("x");
        Assertions.assertNull(ResultType.concreteReturnType(generic), "a generic lambda reports Object");

        Function<Object> script = Function.builder().build(Script.builder().build(JaninoScriptProcessorFactory.LANGUAGE_NAME, "7"));
        Assertions.assertNull(ResultType.concreteReturnType(script), "a script function reports Object");

        Assertions.assertNull(ResultType.concreteReturnType((Function<?>) null));
        Assertions.assertNull(ResultType.concreteReturnType((MethodDefinition) null));
    }

    @Test
    public void concreteReturnTypeOfMethods() throws Exception {
        MethodDefinition names = MethodDefinition.load(Names.class.getMethod("names"), true, SourceDefinition.build());
        Type type = ResultType.concreteReturnType(names);
        Assertions.assertInstanceOf(ParameterizedType.class, type, "a parameterized return type is concrete");
        Assertions.assertEquals(List.class, ((ParameterizedType) type).getRawType());

        MethodDefinition nothing = MethodDefinition.load(Names.class.getMethod("nothing"), true, SourceDefinition.build());
        Assertions.assertNull(ResultType.concreteReturnType(nothing), "void is not a result");

        MethodDefinition count = MethodDefinition.load(Names.class.getMethod("count"), true, SourceDefinition.build());
        Assertions.assertEquals(int.class, ResultType.concreteReturnType(count));
    }

    @Test
    public void resolveAndDerive() {
        Function<Integer> typed = function(() -> 7);
        Function<?> generic = (org.rulii.context.RuleContext ctx) -> ctx.getBindings().getValue("x");

        Assertions.assertEquals(ResultType.declared(Number.class), ResultType.resolve(Number.class, typed), "explicit wins");
        Assertions.assertEquals(ResultType.derived(Integer.class), ResultType.resolve(null, typed));
        Assertions.assertEquals(ResultType.unknown(), ResultType.resolve(null, generic));
        Assertions.assertEquals(ResultType.unknown(), ResultType.resolve(null, null));
        Assertions.assertEquals(ResultType.unknown(), ResultType.derive(null));
        Assertions.assertEquals(ResultType.derived(Integer.class), ResultType.derive(typed.getDefinition()));
    }

    @Test
    public void acceptsComparesRawClassesAndBoxes() {
        ResultType number = ResultType.declared(Number.class);
        Assertions.assertTrue(number.accepts(Integer.class));
        Assertions.assertTrue(number.accepts(int.class), "primitives are boxed");
        Assertions.assertFalse(number.accepts(String.class));

        ResultType list = ResultType.declared(LIST_OF_STRING);
        Assertions.assertTrue(list.accepts(ArrayList.class), "a parameterized type accepts by its raw type");
        Assertions.assertTrue(list.accepts(LIST_OF_STRING));
        Assertions.assertFalse(list.accepts(String.class));

        Assertions.assertTrue(ResultType.declared(Object.class).accepts(String.class));
        Assertions.assertFalse(ResultType.unknown().accepts(String.class), "an unknown type accepts nothing");
        Assertions.assertFalse(ResultType.none().accepts(String.class));
        Assertions.assertThrows(IllegalArgumentException.class, () -> number.accepts(null));
    }

    @Test
    public void descriptionsAndNames() {
        Assertions.assertEquals("Integer (derived)", ResultType.derived(Integer.class).getDescription());
        Assertions.assertEquals("List<String> (declared)", ResultType.declared(LIST_OF_STRING).getDescription());
        Assertions.assertEquals("unknown", ResultType.unknown().getDescription());
        Assertions.assertEquals("none", ResultType.none().getDescription());
        Assertions.assertEquals("int", ResultType.simpleName(int.class));
        Assertions.assertEquals("List<String>", ResultType.simpleName(LIST_OF_STRING));

        Assertions.assertEquals(ResultType.derived(Integer.class), ResultType.derived(Integer.class));
        Assertions.assertNotEquals(ResultType.derived(Integer.class), ResultType.declared(Integer.class), "the origin is part of the value");
        Assertions.assertEquals(ResultType.derived(Integer.class).hashCode(), ResultType.derived(Integer.class).hashCode());
        Assertions.assertTrue(ResultType.derived(Integer.class).toString().contains("DERIVED"));
    }
}
