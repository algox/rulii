package org.rulii.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.model.UnrulyException;
import org.rulii.util.reflect.MethodExecutor;
import org.rulii.util.reflect.MethodHandleMethodExecutor;

/**
 * MethodHandleMethodExecutorTest is a test class for validating the functionality of MethodHandleMethodExecutor.
 *
 * This class includes test cases for:
 * - Executing an instance method on a target object.
 * - Handling exceptions related to an invalid number of arguments.
 * - Executing a static method without a target object.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class MethodHandleMethodExecutorTest {

    public MethodHandleMethodExecutorTest() {
        super();
    }

    @Test
    public void testMethodExecution() throws Throwable {
        class TestObject {
            public String runTest(String param) {
                return "Hello " + param;
            }
        }

        TestObject obj = new TestObject();
        MethodExecutor executor = new MethodHandleMethodExecutor(TestObject.class.getMethod("runTest", String.class));
        String ret = executor.execute(obj, "World");
        Assertions.assertEquals("Hello World", ret);
    }

    @Test
    public void testInvalidNumberOfArgsException() {
        class TestObject {
            public String runTest(String param) {
                return "Hello " + param;
            }
        }

        Assertions.assertThrows(UnrulyException.class, () -> {
            TestObject obj = new TestObject();
            MethodExecutor executor = new MethodHandleMethodExecutor(TestObject.class.getMethod("runTest", String.class));
            executor.execute(obj, "World", "Extra arg");
        });
    }

    @Test
    public void testStaticMethodExecution() throws Throwable {
        class TestObject {
            public static String staticRunTest(String param) {
                return "Hello " + param;
            }
        }

        MethodExecutor executor = new MethodHandleMethodExecutor(TestObject.class.getMethod("staticRunTest", String.class));
        String ret = executor.execute(null, "World");
        Assertions.assertEquals("Hello World", ret);
    }
}
