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
package org.rulii.test.core.function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.model.Definable;
import org.rulii.model.MethodDefinition;
import org.rulii.model.function.*;

import java.math.BigDecimal;

/**
 * Class to test the FunctionBuilder functionality.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class FunctionBuilderTest {

    public FunctionBuilderTest() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNoArg() {
        Function<Boolean> function = Function.builder()
                .with(() -> true)
                .name("function0")
                .build();

        Assertions.assertEquals("function0", ((Definable<MethodDefinition>) function).getDefinition().getName());
        Assertions.assertEquals(0, ((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().size());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test2Args() {
        Function<BigDecimal> function = Function.builder()
                .with((String x, BigDecimal value) -> value)
                .build();

        Assertions.assertEquals(2, ((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().size());
        Assertions.assertEquals("value", ((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().get(1).getName());
        Assertions.assertEquals(((Definable<MethodDefinition>) function).getDefinition().getParameterDefinitions().get(1).getType(), BigDecimal.class);

        Assertions.assertEquals(new BigDecimal("10.00"), function.apply(x -> "123", value -> new BigDecimal("10.00")));
    }

    @Test
    public void testApply() {
        Function<String> function = Function.builder()
                .with((String x) -> x)
                .build();
        Assertions.assertEquals("test", function.apply(x -> "test"));
    }

    @Test
    public void testNoArgFunction() {
        Function<Integer> function = Functions.function(() -> 42);
        Assertions.assertEquals(42, function.apply());
    }

    @Test
    public void testUnaryFunction() {
        Function<Integer> function = Functions.function((Integer a) -> a * 2);
        Assertions.assertEquals(42, function.apply(a -> 21));
    }

    @Test
    public void testBiFunction() {
        Function<Integer> function = Functions.function((Integer a, Integer b) -> a * b);
        Assertions.assertEquals(42, function.apply(a -> 21, b ->2));
    }

    @Test
    public void testTriFunction() {
        Function<Integer> function = Functions.function((Integer a, Integer b, Integer c) -> a * b + c);
        Assertions.assertEquals(42, function.apply(a -> 10, b -> 4, c -> 2));
    }

    @Test
    public void testFunction_QuadFunction() {
        Function<String> function = Functions.function((String val1, String val2, String val3, String val4) -> val1 + ", " + val2 + ", " + val3 + " and " + val4);
        Assertions.assertEquals("a, b, c and d", function.apply(val1 -> "a", val2 -> "b", val3 -> "c", val4 -> "d"));
    }

    @Test
    public void testQuadFunction() {
        Function<String> quadFunction = Functions.function((String val1, Boolean val2, Double val3, Byte val4) -> "Hello, World! " + val1 + ", " + val2 + ", " + val3 + ", " + val4);
        Assertions.assertEquals("Hello, World! a, true, 100.0, 1", quadFunction.apply(val1 -> "a", val2 -> true, val3 -> 100.0, val4 -> (byte) 1));
    }

    @Test
    public void testQuinFunction() {
        Function<String> quinFunction = Functions.function((String val1, Boolean val2, Double val3, Byte val4, Short val5) -> "Hello, World! " + val1 + ", " + val2 + ", "
                + val3 + ", " + val4 + ", " + val5);
        Assertions.assertEquals("Hello, World! a, true, 100.0, 1, 29", quinFunction.apply(val1 -> "a", val2 -> true, val3 -> 100.0, val4 -> (byte) 1, val5 -> (short) 29));
    }

    @Test
    void testSexFunction() {
        // Initialize a function to concatenate six strings
        Function<String> function = Functions.function((String a, String b, String c, String d, String e, String f) -> a + b + c + d + e + f);
        // Use the function
        String result = function.apply(a -> "A", b -> " ", c -> "B", d ->" ", e -> "C", f -> "");
        // Validate the result
        Assertions.assertEquals("A B C", result, "Result is not as expected");
    }

    @Test
    void testSeptFunction() {
        // Test case 1: Check simple addition of Integer values
        SeptFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> add = (argA, argB, argC, argD, argE, argF, argG) -> argA + argB + argC + argD + argE + argF + argG;
        Assertions.assertEquals(28, add.apply(1, 2, 3, 4, 5, 6, 7));
        // Test case 2: Check string concatenation
        SeptFunction<String, String, String, String, String, String, String, String> concatenate = (argA, argB, argC, argD, argE, argF, argG) -> argA + argB + argC + argD + argE + argF + argG;
        Assertions.assertEquals("ABCDEFG", concatenate.apply("A", "B", "C", "D", "E", "F", "G"));
    }

    private final OctFunction<String, String, String, String, String, String, String, String, String> dummyOctFunction =
            (arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7) ->
                    arg0.concat(arg1).concat(arg2).concat(arg3)
                            .concat(arg4).concat(arg5).concat(arg6).concat(arg7);

    @Test
    public void testApply_withEightParams_concatenatesAll() {
        // Arrange
        String arg0 = "Hello";
        String arg1 = ", ";
        String arg2 = "this";
        String arg3 = " ";
        String arg4 = "is";
        String arg5 = " ";
        String arg6 = "a";
        String arg7 = " test";
        String expected = "Hello, this is a test";
        // Act
        String actual = dummyOctFunction.apply(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
        // Assert
        Assertions.assertEquals(expected, actual, "Result should be the concatenation of all inputs");
    }

    @Test
    public void testApply_withEmptyParams_returnsEmptyString() {
        // Arrange
        String arg0 = "";
        String arg1 = "";
        String arg2 = "";
        String arg3 = "";
        String arg4 = "";
        String arg5 = "";
        String arg6 = "";
        String arg7 = "";

        String expected = "";
        // Act
        String actual = dummyOctFunction.apply(arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7);
        // Assert
        Assertions.assertEquals(expected, actual, "Result should be an empty string as all inputs were empty");
    }

    @Test
    public void testNovFunction() {
        NovFunction<Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> sumFunction =
                (Integer a, Integer b, Integer c, Integer d, Integer e, Integer f, Integer g, Integer h, Integer i) -> a + b + c + d + e + f + g + h + i;
        Integer sum = sumFunction.apply(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Assertions.assertEquals(45, sum);
    }

    @Test
    public void testDecFunction() {
        DecFunction<String, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer> decFunction = (arg0, arg1, arg2, arg3, arg4, arg5, arg6, arg7, arg8, arg9) -> "OK";
        String result = decFunction.apply(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Assertions.assertEquals("OK", result);
    }

    public static class DoublerFunction implements UnaryFunction<Integer, Integer> {
        @Override
        public Integer apply(Integer a) {
            return a * 2;
        }
    }

    /**
     * MethodDefinition/ParameterDefinition.load(Method,...) cache their result in a static
     * IdentityHashMap keyed by the reflective Method - but java.lang.Class#getMethod() returns a
     * fresh Method object on every call, so two independent lookups never actually collide on
     * the same cache entry. The cache only produces a real hit when the exact same Method
     * reference is deliberately reused (e.g. iterating one Method[] array, or the candidates[0]
     * bug fixed elsewhere in this review) - so this test captures a Method reference once and
     * reuses it, exactly like those real call sites do.
     */
    @Test
    public void testMethodDefinitionCopy_mutatingCopyDoesNotAffectCachedOriginal() throws NoSuchMethodException {
        java.lang.reflect.Method method = DoublerFunction.class.getMethod("apply", Integer.class);

        MethodDefinition cached = MethodDefinition.load(method, true, org.rulii.model.SourceDefinition.build());
        MethodDefinition sameCached = MethodDefinition.load(method, true, org.rulii.model.SourceDefinition.build());
        // Same Method reference reused -> same cache entry (the premise this test guards against).
        Assertions.assertSame(cached, sameCached);

        MethodDefinition copy = cached.copy();
        Assertions.assertNotSame(cached, copy);
        // Each parameter is independently copied too, not just the top-level wrapper.
        Assertions.assertNotSame(cached.getParameterDefinitions().get(0), copy.getParameterDefinitions().get(0));

        copy.setName("customName");

        Assertions.assertEquals("customName", copy.getName());
        Assertions.assertNotEquals("customName", cached.getName());
    }

    public static class MultiFunctionTarget {
        @org.rulii.annotation.Function
        public Integer doubleIt(Integer value) {
            return value * 2;
        }

        @org.rulii.annotation.Function
        public Integer tripleIt(Integer value) {
            return value * 3;
        }
    }

    @Test
    public void testFunctionBuilderBuilder_multipleAnnotatedMethods_eachWrapsDistinctMethod() {
        Function<?>[] functions = Function.builder().build(new MultiFunctionTarget(), org.rulii.annotation.Function.class);
        Assertions.assertEquals(2, functions.length);

        // doubleIt(5) -> 10, tripleIt(5) -> 15. If both entries wrapped the same (first)
        // candidate method, these would be equal instead of 10 and 15.
        Object r0 = functions[0].apply(value -> 5);
        Object r1 = functions[1].apply(value -> 5);
        Assertions.assertNotEquals(r0, r1);
    }

    @Test
    public void testFunctionBuilderBuilder_nullTarget_throwsIllegalArgumentException() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> Function.builder().build(null, org.rulii.annotation.Function.class));
    }
}
