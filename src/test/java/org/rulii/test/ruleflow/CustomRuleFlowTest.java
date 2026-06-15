package org.rulii.test.ruleflow;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.bind.Binding;
import org.rulii.context.RuleContextBuilder;
import org.rulii.ruleflow.RuleFlow;
import org.rulii.ruleflow.RuleFlowBuilder;

import java.util.Locale;
import java.util.concurrent.Executors;

import static org.rulii.model.action.Actions.action;
import static org.rulii.model.condition.Conditions.condition;
import static org.rulii.model.function.Functions.function;

public class CustomRuleFlowTest {

    public CustomRuleFlowTest() {
        super();
    }

    @Test
    public void test1() {
        RuleFlow<Integer> flow = RuleFlow.builder()
                .name("test1")
                .description("Test 1")
                .context((RuleContextBuilder builder) -> {
                    builder.executeUsing(Executors.newFixedThreadPool(5));
                    builder.locale(Locale.CANADA);
                })
                .param("value1", String.class)
                .bind("value2", "value is 2")
                .bind(value3 -> 12)
                .when(condition((Integer value3) -> value3 > 10))
                    .then((RuleFlowBuilder builder) -> {
                        builder.execute(action((Binding<Integer> value3) -> value3.setValue(100)));
                    })
                .returning(function((Integer value3) -> value3))
                .build();

        Integer result = flow.run(value1 -> "hello world");
        Assertions.assertEquals(100, result);
    }
}
