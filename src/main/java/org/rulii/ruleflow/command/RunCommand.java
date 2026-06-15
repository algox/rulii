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
package org.rulii.ruleflow.command;

import org.rulii.bind.BindingDeclaration;
import org.rulii.bind.Bindings;
import org.rulii.bind.NamedScope;
import org.rulii.context.RuleContext;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.Runnable;
import org.rulii.model.UnrulyException;
import org.rulii.registry.RuleRegistry;
import org.rulii.ruleflow.RuleFlowExecutionContext;

import java.util.Map;

/**
 * Pipeline command that executes a {@link Runnable} (Rule, RuleSet, or RuleFlow) and
 * optionally binds the result into the current scope.
 *
 * <p>The runnable is resolved at runtime — either from the instance supplied at build time
 * or by looking it up in the {@link RuleRegistry} by name or class.
 *
 * <p>When {@code stepParams} is provided (via {@code .with(...)} on the builder), a temporary
 * anonymous scope is pushed before the runnable executes and removed in a {@code finally} block,
 * so the injected bindings never outlive this step.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RunCommand implements RuleFlowCommand {

    private final Runnable<?> runnable;
    private final String registryName;
    private final Class<?> registryClass;
    private final String bindingName;
    private final String bindingScopeName;
    private final OnExceptionCommand stepHandler;
    private final Object stepParams;

    private RunCommand(Runnable<?> runnable, String registryName, Class<?> registryClass,
                       String bindingName, String bindingScopeName, OnExceptionCommand stepHandler,
                       Object stepParams) {
        super();
        this.runnable = runnable;
        this.registryName = registryName;
        this.registryClass = registryClass;
        this.bindingName = bindingName;
        this.bindingScopeName = bindingScopeName;
        this.stepHandler = stepHandler;
        this.stepParams = stepParams;
    }

    /**
     * Creates a {@code RunCommand} for a pre-built runnable instance.
     *
     * @param runnable the runnable to execute; must not be null.
     * @param bindingName name to bind the result under, or null.
     * @param bindingScopeName scope to bind the result into, or null for the current scope.
     * @param stepHandler step-level exception handler, or null.
     * @param stepParams step-scoped parameters ({@code BindingDeclaration<?>[]},{@code Map<String,Object>}, or a JavaBean POJO), or null.
     * @return a new RunCommand.
     */
    public static RunCommand of(Runnable<?> runnable, String bindingName, String bindingScopeName,
                                OnExceptionCommand stepHandler, Object stepParams) {
        Assert.notNull(runnable, "runnable cannot be null.");
        return new RunCommand(runnable, null, null, bindingName, bindingScopeName, stepHandler, stepParams);
    }

    /**
     * Creates a {@code RunCommand} that looks up the runnable by name from the registry at runtime.
     *
     * @param registryName name to look up; must not be null or empty.
     * @param bindingName name to bind the result under, or null.
     * @param bindingScopeName scope to bind the result into, or null for the current scope.
     * @param stepHandler step-level exception handler, or null.
     * @param stepParams step-scoped parameters, or null.
     * @return a new RunCommand.
     */
    public static RunCommand ofName(String registryName, String bindingName, String bindingScopeName,
                                    OnExceptionCommand stepHandler, Object stepParams) {
        Assert.hasText(registryName, "registryName cannot be empty/null.");
        return new RunCommand(null, registryName, null, bindingName, bindingScopeName, stepHandler, stepParams);
    }

    /**
     * Creates a {@code RunCommand} that looks up the rule by class from the registry at runtime.
     *
     * @param registryClass class to look up; must not be null.
     * @param bindingName name to bind the result under, or null.
     * @param bindingScopeName scope to bind the result into, or null for the current scope.
     * @param stepHandler step-level exception handler, or null.
     * @param stepParams step-scoped parameters, or null.
     * @return a new RunCommand.
     */
    public static RunCommand ofClass(Class<?> registryClass, String bindingName, String bindingScopeName,
                                     OnExceptionCommand stepHandler, Object stepParams) {
        Assert.notNull(registryClass, "registryClass cannot be null.");
        return new RunCommand(null, null, registryClass, bindingName, bindingScopeName, stepHandler, stepParams);
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        NamedScope paramScope = null;

        try {
            if (stepParams != null) paramScope = pushParamScope(ctx.getRuleContext());
            Runnable<?> target = resolveRunnable(ctx.getRuleContext());
            Object result = target.run(ctx.getRuleContext());
            if (bindingName != null) bindResult(ctx.getRuleContext(), result);
        } catch (UnrulyException e) {
            if (!handleException(e, ctx)) throw e;
        } finally {
            if (paramScope != null) ctx.getRuleContext().getBindings().removeScope(paramScope);
        }
    }

    @SuppressWarnings("unchecked")
    private NamedScope pushParamScope(RuleContext ruleContext) {
        NamedScope scope = ruleContext.getBindings().addScope();

        if (stepParams instanceof BindingDeclaration<?> declaration) {
            ruleContext.getBindings().bind(declaration);
        } else if (stepParams instanceof BindingDeclaration<?>[] declarations) {
            ruleContext.getBindings().bind(declarations);
        } else if (stepParams instanceof Map<?, ?> map) {
            ruleContext.getBindings().loadMap((Map<String, Object>) map);
        } else {
            ruleContext.getBindings().loadProperties(stepParams);
        }

        return scope;
    }

    private Runnable<?> resolveRunnable(RuleContext ruleContext) {
        if (runnable != null) return runnable;

        RuleRegistry registry = ruleContext.getRuleRegistry();

        if (registry == null) throw new UnrulyException("No RuleRegistry configured on RuleContext. Cannot look up [" + (registryName != null ? registryName : registryClass) + "]");

        if (registryName != null) {
            Runnable<?> result = registry.get(registryName);
            if (result == null) throw new UnrulyException("No Runnable found in RuleRegistry for name [" + registryName + "]");
            return result;
        }

        Runnable<?> result = registry.getRule(registryClass);
        if (result == null) throw new UnrulyException("No Rule found in RuleRegistry for class [" + registryClass + "]");

        return result;
    }

    private void bindResult(RuleContext ruleContext, Object result) {
        Bindings bindings = bindingScopeName != null
                ? ruleContext.getBindings().getScopeBindings(bindingName)
                : ruleContext.getBindings();
        bindings.bind(bindingName, result);
    }

    private boolean handleException(UnrulyException e, RuleFlowExecutionContext ctx) {

        if (stepHandler != null && stepHandler.canHandle(e)) {
            stepHandler.handleException(e, ctx);
            ctx.getRuleContext().getTracer().fireOnRuleFlowExceptionHandled(ctx.getRuleFlow(), e, true);
            return true;
        }

        if (ctx.getGlobalHandler() != null && ctx.getGlobalHandler().canHandle(e)) {
            ctx.getGlobalHandler().handleException(e, ctx);
            ctx.getRuleContext().getTracer().fireOnRuleFlowExceptionHandled(ctx.getRuleFlow(), e, false);
            return true;
        }

        return false;
    }

    public Runnable<?> getRunnable() { return runnable; }

    public String getRegistryName() { return registryName; }

    public Class<?> getRegistryClass() { return registryClass; }

}
