package org.rulii.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.util.reflect.MethodResolver;

import java.lang.reflect.Method;

/**
 * A test class for MethodResolver class to test the functionality of method resolution.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class MethodResolverTest {

    public MethodResolverTest() {
        super();
    }

    @Test
    public void testGetImplementationMethodValidCase() throws NoSuchMethodException {
        Class<?> clazz = MethodResolverTest.class;
        Method method = clazz.getMethod("testGetImplementationMethodValidCase");
        MethodResolver methodResolver = MethodResolver.builder().build();
        Method result = methodResolver.getImplementationMethod(clazz, method);
        Assertions.assertEquals(method, result, "Expected to get correct implementation method");
    }

    @Test
    public void testGetImplementationMethodWithNullInput() {
        MethodResolver methodResolver = MethodResolver.builder().build();
        Assertions.assertThrows(NullPointerException.class, () -> methodResolver.getImplementationMethod(null, null),
                "Expected to throw NullPointerException for null inputs");
    }
}
