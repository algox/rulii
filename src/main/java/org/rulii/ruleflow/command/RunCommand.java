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
import org.rulii.model.Runnable;
import org.rulii.model.UnrulyException;
import org.rulii.registry.RuleRegistry;
import org.rulii.ruleflow.RuleFlowExceptionHandler;
import org.rulii.ruleflow.RuleFlowExecutionContext;

import java.util.Map;
import java.util.UUID;

/**
 * Immutable pipeline command that invokes a {@link Runnable} (Rule, RuleSet, RuleFlow, or
 * Function) and optionally binds the result into the current scope.
 *
 * <p>Exactly one of {@code runnable}, {@code registryName}, or {@code registryClass} is
 * non-null. All configuration is fixed at construction time via
 * {@link org.rulii.ruleflow.RunConstruct}. Use {@code as()} to bind the result and
 * {@code with()} to supply step-scoped parameters.
 *
 * @author Max Arulananthan
 * @since 2.0
 */
public class RunCommand implements RuleFlowCommand {

    private final Runnable<?> runnable;
    private final String nameInRegistry;
    private final Class<?> classInRegistry;
    private final String bindingName;
    private final String bindingScopeName;
    private final Object params;
    private final RuleFlowExceptionHandler exceptionHandler;

    public RunCommand(Runnable<?> runnable, String nameInRegistry, Class<?> classInRegistry,
                      String bindingName, String bindingScopeName, Object params,
                      RuleFlowExceptionHandler exceptionHandler) {
        super();
        this.runnable = runnable;
        this.nameInRegistry = nameInRegistry;
        this.classInRegistry = classInRegistry;
        this.bindingName = bindingName;
        this.bindingScopeName = bindingScopeName;
        this.params = params;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void execute(RuleFlowExecutionContext ctx) {
        RuleContext ruleContext = ctx.getRuleContext();

        // Capture the result destination BEFORE pushing the transient param scope: the
        // result belongs to the scope that is current when the command starts, never to
        // the step-scoped param scope (which is discarded after the step).
        Bindings resultTarget = bindingName != null ? resolveResultTarget(ruleContext) : null;
        NamedScope paramScope = null;

        try {
            if (params != null) paramScope = pushParamScope(ruleContext);
            Object result = resolveRunnable(ruleContext).run(ruleContext);
            if (resultTarget != null) resultTarget.bind(bindingName, result);
        } catch (UnrulyException e) {
            if (!handleException(e, ctx)) throw e;
        } finally {
            if (paramScope != null) ruleContext.getBindings().removeScope(paramScope);
        }
    }

    @SuppressWarnings("unchecked")
    private NamedScope pushParamScope(RuleContext ruleContext) {
        NamedScope scope = ruleContext.getBindings().addScope(getScopeName());

        if (params instanceof BindingDeclaration<?> declaration) {
            ruleContext.getBindings().bind(declaration);
        } else if (params instanceof BindingDeclaration<?>[] declarations) {
            ruleContext.getBindings().bind(declarations);
        } else if (params instanceof Map<?, ?> map) {
            ruleContext.getBindings().loadMap((Map<String, Object>) map);
        } else {
            ruleContext.getBindings().loadProperties(params);
        }

        return scope;
    }

    private Runnable<?> resolveRunnable(RuleContext ruleContext) {
        if (runnable != null) return runnable;

        RuleRegistry registry = ruleContext.getRuleRegistry();
        if (registry == null) throw new UnrulyException("No RuleRegistry configured on RuleContext. Cannot look up ["
                + (nameInRegistry != null ? nameInRegistry : classInRegistry) + "]");

        if (nameInRegistry != null) {
            Runnable<?> result = registry.get(nameInRegistry);
            if (result == null) throw new UnrulyException("No Runnable found in RuleRegistry for name [" + nameInRegistry + "]");
            return result;
        }

        Runnable<?> result = registry.getRule(classInRegistry);
        if (result == null) throw new UnrulyException("No Runnable found in RuleRegistry for class [" + classInRegistry + "]");
        return result;
    }

    /**
     * Resolves the Bindings the step result will be bound into: the named scope when
     * {@code as(scope, binding)} was used, otherwise the currently active scope.
     *
     * @param ruleContext the rule context.
     * @return the destination bindings.
     */
    private Bindings resolveResultTarget(RuleContext ruleContext) {
        return bindingScopeName != null
                ? ruleContext.getBindings().getScopeBindings(bindingScopeName)
                : ruleContext.getBindings().getCurrentScope().getBindings();
    }

    private boolean handleException(UnrulyException e, RuleFlowExecutionContext ctx) {

        if (exceptionHandler != null && exceptionHandler.canHandle(e)) {
            exceptionHandler.handleException(e, ctx);
            ctx.getRuleContext().getTracer().fireOnRuleFlowExceptionHandled(ctx.getRuleFlow(), e, true);
            return true;
        }

        return false;
    }

    public Runnable<?> getRunnable() { return runnable; }

    public String getNameInRegistry() {
        return nameInRegistry;
    }

    public Class<?> getClassInRegistry() {
        return classInRegistry;
    }

    public String getBindingName() {
        return bindingName;
    }

    public String getBindingScopeName() {
        return bindingScopeName;
    }

    public Object getParams() {
        return params;
    }

    public RuleFlowExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    protected String getScopeName() {
        return "run-scope-" + UUID.randomUUID().toString().substring(0, 8);
    }
}
