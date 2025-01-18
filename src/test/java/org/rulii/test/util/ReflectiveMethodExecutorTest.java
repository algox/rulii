package org.rulii.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.model.UnrulyException;
import org.rulii.util.reflect.ReflectiveMethodExecutor;

import java.lang.reflect.Method;

/**
 * This class contains test cases for the ReflectiveMethodExecutor class.
 *
 * @author Max Arulananthan
 * @since 1.0
 */
public class ReflectiveMethodExecutorTest {

    public ReflectiveMethodExecutorTest() {
        super();
    }

    public String getSuccessTestMessage() {
        return "ReflectiveMethodExecutor test succeeded!";
    }

    @Test
    public void whenExecuteWithParameters_thenTheMethodMustExecuteSuccessfully() {
        try {
            Method method = ReflectiveMethodExecutorTest.class.getDeclaredMethod("getSuccessTestMessage");
            ReflectiveMethodExecutor executor = new ReflectiveMethodExecutor(method);
            String result = executor.execute(this);
            Assertions.assertTrue(result.contains("succeeded"));
        } catch (Exception e) {
            // Print the stack trace to aid debugging.
            e.printStackTrace();
        }
    }

    @Test
    public void whenExecuteWithStaticMethod_thenTheMethodMustExecuteSuccessfully() {
        try {
            Method method = ReflectiveMethodExecutorTest.class.getMethod("getSuccessTestMessage");
            ReflectiveMethodExecutor executor = new ReflectiveMethodExecutor(method);
            String result = executor.execute(this);
            Assertions.assertTrue(result.contains("succeeded"));
        } catch (Exception e) {
            // Print the stack trace to aid debugging.
            e.printStackTrace();
        }
    }

    @Test
    public void whenExecuteWithInvalidParameters_thenUnrulyExceptionIsThrown() {
        try {
            Method method = ReflectiveMethodExecutorTest.class.getMethod("getSuccessTestMessage");
            ReflectiveMethodExecutor executor = new ReflectiveMethodExecutor(method);
            Assertions.assertThrows(UnrulyException.class, () -> executor.execute(this, "invalidParameter"));
        } catch (Exception e) {
            // Print the stack trace to aid debugging.
            e.printStackTrace();
        }
    }
}
