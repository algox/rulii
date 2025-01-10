package org.rulii.validation;

import org.rulii.annotation.Given;
import org.rulii.annotation.Otherwise;
import org.rulii.annotation.Param;
import org.rulii.annotation.Rule;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.bind.match.ParameterMatch;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.condition.Condition;
import org.rulii.util.RuleUtils;

import java.util.List;
import java.util.Map;

@Rule
public class SuppliedValidationRule extends ValidationRule {

    private final Condition condition;

    public SuppliedValidationRule(Condition condition, String errorCode, Severity severity, String defaultMessage) {
        super(errorCode, severity, defaultMessage);
        Assert.notNull(condition, "condition must not be null.");
        this.condition = condition;
    }

    @Given
    public boolean isValid(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext) {
        return condition.isTrue(ruleContext);
    }

    @Otherwise
    public void otherwise(@Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleContext ruleContext,
                          @Param(matchUsing = MatchByTypeMatchingStrategy.class) RuleViolations ruleViolations) {
        List<ParameterMatch> matches = ruleContext.getParameterResolver().match(condition.getDefinition(),
                ruleContext.getBindings(), ruleContext.getMatchingStrategy(), ruleContext.getObjectFactory());
        List<Object> values = ruleContext.getParameterResolver().resolve(matches, condition.getDefinition(),
                ruleContext.getBindings(), ruleContext.getMatchingStrategy(), ruleContext.getConverterRegistry(),
                ruleContext.getObjectFactory());
        Map<String, Object> params = RuleUtils.convert(matches, values);
        RuleViolationBuilder builder = createRuleViolationBuilder();

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            builder.param(entry.getKey(), entry.getValue());
        }

        ruleViolations.add(builder.build(ruleContext.getMessageResolver(), ruleContext.getMessageFormatter(), ruleContext.getLocale()));
    }

}
