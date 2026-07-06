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
package org.rulii.script.janino;

import org.codehaus.commons.compiler.CompileException;
import org.codehaus.janino.Java;
import org.codehaus.janino.util.DeepCopier;
import org.rulii.bind.Binding;
import org.rulii.bind.Bindings;
import org.rulii.lib.spring.util.Assert;
import org.rulii.model.UnrulyException;
import org.rulii.script.BuildScriptException;
import org.rulii.util.reflect.ReflectionUtils;

import java.lang.reflect.*;

/**
 * Janino {@link DeepCopier} that rewrites {@code ctx.<name>} binding accesses into
 * explicit {@link Bindings} API calls as it walks the AST.
 *
 * <p>Three kinds of rewriting are performed:
 * <ol>
 *   <li><b>Read</b> — {@code ctx.foo} → {@code ((WrapperType) ctx.getValue("foo"))}<br>
 *       The cast uses the wrapper class of the binding's raw type so that Janino can
 *       resolve overloaded operators (e.g. {@code >=} on {@code Integer}).</li>
 *   <li><b>Write</b> — {@code ctx.foo = expr} → {@code ctx.setValueOrBind("foo", expr)}</li>
 *   <li><b>Increment / decrement</b> — {@code ctx.foo++} / {@code ctx.foo--} → throws
 *       {@link UnrulyException}; use {@code ctx.setValueOrBind} explicitly instead.</li>
 * </ol>
 *
 * <p>In addition, any local variable declaration that shadows the bindings parameter name
 * (default {@code "ctx"}) is rejected at compile time.
 *
 * @author Max Arulananthan
 * @since 1.2
 * @see JaninoScriptCompiler
 */
public class ASTMutator extends DeepCopier {

    private final String bindingsName;
    private final String script;
    private final Bindings bindings;

    /**
     * Creates a new {@code ASTMutator}.
     *
     * @param bindingsName the name of the bindings variable inside the script (e.g. {@code "ctx"});
     *                     must not be null or empty.
     * @param script       the original script source, used only for error messages; must not be null.
     * @param bindings     the live bindings snapshot used to resolve binding types at compile time;
     *                     must not be null.
     */
    public ASTMutator(String bindingsName, String script, Bindings bindings) {
        super();
        Assert.hasText(bindingsName, "bindingsName cannot be empty.");
        this.bindingsName = bindingsName;
        this.script = script;
        this.bindings = bindings;
    }

    /**
     * Deep-copies and rewrites a single block statement.
     *
     * @param stmt the statement to rewrite; must not be null.
     * @return the rewritten statement; never null.
     * @throws UnrulyException if a {@link CompileException} is raised during the deep copy.
     */
    public Java.BlockStatement translate(Java.BlockStatement stmt) {
        try {
            return copyBlockStatement(stmt);
        } catch (CompileException e) {
            throw new UnrulyException("Error rewriting block statement", e);
        }
    }

    /**
     * Rejects any local variable declaration whose name collides with the bindings parameter name.
     *
     * @throws UnrulyException if a declarator name equals {@link #getBindingsName()}.
     */
    @Override
    public Java.BlockStatement copyLocalVariableDeclarationStatement(Java.LocalVariableDeclarationStatement subject) throws CompileException {

        for (Java.VariableDeclarator declarator : subject.variableDeclarators) {
            if (declarator.name.equals(getBindingsName())) throw new UnrulyException("Cannot use " + getBindingsName()
                    + " as a variable name. It is reserved for the bindings.");
        }

        return super.copyLocalVariableDeclarationStatement(subject);
    }

    /**
     * Intercepts rvalue nodes to rewrite binding reads, writes, and forbidden increments.
     *
     * <ul>
     *   <li>{@code ctx.foo} → {@code ((WrapperType) ctx.getValue("foo"))}</li>
     *   <li>{@code ctx.foo = expr} → {@code ctx.setValueOrBind("foo", expr)}</li>
     *   <li>{@code ctx.foo++} / {@code ctx.foo--} → {@link UnrulyException}</li>
     * </ul>
     */
    @Override
    public Java.Rvalue copyRvalue(Java.Rvalue subject) throws CompileException {

        if (subject instanceof Java.AmbiguousName an) {
            if (getBindingsName().equals(an.identifiers[0]) && an.n == 2) return translateName(an);
        }

        if (subject instanceof Java.Assignment fa) {
            if (fa.lhs instanceof Java.AmbiguousName an) {
                if (getBindingsName().equals(an.identifiers[0]) && an.n == 2) {
                    return translateAssignment(fa, an, super.copyRvalue(fa.rhs));
                }
            }
        }

        if (subject instanceof Java.Crement crement) {
            if (crement.operand instanceof Java.AmbiguousName an) {
                if (getBindingsName().equals(an.identifiers[0]) && an.n == 2) {
                    throw new UnrulyException("Cannot increment/decrement a binding. Use ctx.setValue() instead.");
                }
            }
        }

        return super.copyRvalue(subject);
    }

    /**
     * Rewrites a binding read ({@code ctx.foo}) into a type-cast {@code getValue} call.
     *
     * @param an the ambiguous name node representing {@code ctx.foo}.
     * @return a {@code ((WrapperType) ctx.getValue("foo"))} expression node.
     */
    private Java.Rvalue translateName(Java.AmbiguousName an) {
        String bindingName = an.identifiers[1];
        Class<?> bindingType = getBindingType(bindingName);

        Java.Atom ctxTarget = new Java.AmbiguousName(an.getLocation(), new String[] {getBindingsName()}, 1);
        Java.Rvalue[] args = {new Java.StringLiteral(an.getLocation(), "\"" + bindingName + "\"")};
        Java.MethodInvocation methodInvocation = new Java.MethodInvocation(an.getLocation(), ctxTarget, "getValue", args);
        return new Java.ParenthesizedExpression(an.getLocation(),
                new Java.Cast(an.getLocation(), new Java.ReferenceType(an.getLocation(), new Java.Annotation[0],
                        new String[] {bindingType.getName()}, null), methodInvocation));
    }

    /**
     * Rewrites a binding assignment ({@code ctx.foo = expr}) into a {@code setValueOrBind} call.
     *
     * @param assignment the original assignment node.
     * @param an         the left-hand side ambiguous name ({@code ctx.foo}).
     * @param rvalue     the already-copied right-hand side expression.
     * @return a {@code ctx.setValueOrBind("foo", expr)} method-invocation node.
     */
    private Java.Rvalue translateAssignment(Java.Assignment assignment, Java.AmbiguousName an, Java.Rvalue rvalue) {
        String bindingName = an.identifiers[1];
        Java.Atom ctxTarget = new Java.AmbiguousName(assignment.getLocation(), new String[] {getBindingsName()}, 1);
        Java.Rvalue[] args = {new Java.StringLiteral(assignment.getLocation(), "\"" + bindingName + "\""), rvalue};
        return new Java.MethodInvocation(assignment.getLocation(), ctxTarget, "setValueOrBind", args);
    }

    /**
     * Looks up the binding by name and returns its raw wrapper class for use as a cast target.
     *
     * @param bindingName the binding name to look up.
     * @return the wrapper class of the binding's raw type (e.g. {@code Integer.class} for {@code int}).
     * @throws BuildScriptException if no binding with the given name exists in the current bindings.
     */
    private Class<?> getBindingType(String bindingName) {
        Binding<?> binding = bindings.getBinding(bindingName);
        if (binding == null) throw new BuildScriptException(script, "Unable to JIT compile as [" + bindingName + "]  is not defined in the bindings.");
        if (binding.getType() == null) throw new BuildScriptException(script, "Unable to JIT compile as [" + bindingName + "]  has no type. " +
                "Define [" + bindingName + "] with a type in the bindings.");
        return ReflectionUtils.getWrapperClass(getRawType(binding.getType()));
    }

    /**
     * Returns the name of the bindings parameter as exposed inside the script (e.g. {@code "ctx"}).
     *
     * @return the bindings variable name; never null or empty.
     */
    public String getBindingsName() {
        return bindingsName;
    }

    /**
     * Extracts the raw {@link Class} from any {@link Type}, stripping generic parameters,
     * resolving array component types, and following type-variable bounds.
     *
     * <table border="1">
     *   <tr><th>Input</th><th>Returns</th></tr>
     *   <tr><td>{@code String.class}</td><td>{@code String.class}</td></tr>
     *   <tr><td>{@code List<String>}</td><td>{@code List.class}</td></tr>
     *   <tr><td>{@code T extends Foo}</td><td>{@code Foo.class}</td></tr>
     *   <tr><td>{@code ? extends Foo}</td><td>{@code Foo.class}</td></tr>
     *   <tr><td>{@code T[]}</td><td>{@code Object[].class} (component's raw array)</td></tr>
     * </table>
     *
     * @param type the type to resolve; must not be null.
     * @return the raw class; never null.
     * @throws UnrulyException if the type cannot be resolved.
     */
    private static Class<?> getRawType(Type type) {
        if (type instanceof Class<?> c) return c;
        if (type instanceof ParameterizedType pt) return getRawType(pt.getRawType());
        if (type instanceof GenericArrayType gat) {
            Class<?> component = getRawType(gat.getGenericComponentType());
            return Array.newInstance(component, 0).getClass();
        }
        if (type instanceof TypeVariable<?> tv) return getRawType(tv.getBounds()[0]);
        if (type instanceof WildcardType wt) return getRawType(wt.getUpperBounds()[0]);
        throw new UnrulyException("Cannot determine raw type for: " + type);
    }
}
