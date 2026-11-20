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
package org.rulii.bind;

import org.rulii.lib.spring.util.Assert;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The bindings of one context seen as a scope of another: a live view of them that leaves out their reserved
 * {@code ruleContext} and {@code bindings}.
 *
 * <p>Those two describe the stack they were bound in. A context built over the bindings of another context
 * ({@code RuleContext.builder().build(other.getBindings())}, {@code rule.run(bindings)} from within an action) has a
 * pair of its own in its root scope, below the given bindings; without this view the older pair would be found
 * first, and every {@code RuleContext} or {@code Bindings} parameter would resolve to the other context, which knows
 * nothing of the scopes pushed on the new one.
 *
 * <p>Everything else is the given bindings': lookups, binds and listeners go straight to them, so what is bound
 * through the new context's global scope still lands where the caller reads it.
 *
 * <p>A {@link ContextHider}: a fork does not share this view with the other thread, it takes what is visible in it
 * at the time of the fork.
 *
 * @author Max Arulananthan
 * @since 2.3
 */
public final class HiddenContextBindings implements Bindings, ContextHider {

    private static final String RULE_CONTEXT    = ReservedBindings.RULE_CONTEXT.getName();
    private static final String BINDINGS        = ReservedBindings.BINDINGS.getName();

    private final Bindings delegate;

    private HiddenContextBindings(Bindings delegate) {
        super();
        this.delegate = delegate;
    }

    /**
     * The given bindings as the scope of a new context: the bindings themselves when they carry no reserved
     * {@code ruleContext} or {@code bindings} (the usual case), a view without the two otherwise.
     *
     * @param bindings the bindings a context is being built over; must not be null.
     * @return the bindings to use as the scope.
     */
    public static Bindings of(Bindings bindings) {
        Assert.notNull(bindings, "bindings cannot be null.");
        return bindings.contains(RULE_CONTEXT) || bindings.contains(BINDINGS) ? new HiddenContextBindings(bindings) : bindings;
    }

    /**
     * The bindings this view sits on.
     *
     * @return the delegate.
     */
    public Bindings getDelegate() {
        return delegate;
    }

    private static boolean isHidden(String name) {
        return RULE_CONTEXT.equals(name) || BINDINGS.equals(name);
    }

    // --- lookups: everything but the two ------------------------------------------------------------

    @Override
    public <T> Binding<T> getBinding(String name) {
        return isHidden(name) ? null : delegate.getBinding(name);
    }

    @Override
    public <T> Binding<T> getBinding(String name, Type type) {
        return isHidden(name) ? null : delegate.getBinding(name, type);
    }

    @Override
    public <T> List<Binding<T>> getBindings(Type type) {
        List<Binding<T>> matches = delegate.getBindings(type);
        if (matches.isEmpty()) return matches;

        List<Binding<T>> result = null;

        for (int i = 0; i < matches.size(); i++) {
            Binding<T> match = matches.get(i);

            if (isHidden(match.getName())) {
                // First one to leave out: copy what came before it.
                if (result == null) result = new ArrayList<>(matches.subList(0, i));
            } else if (result != null) {
                result.add(match);
            }
        }

        return result != null ? result : matches;
    }

    @Override
    public int size() {
        int result = delegate.size();
        if (delegate.contains(RULE_CONTEXT)) result--;
        if (delegate.contains(BINDINGS)) result--;
        return result;
    }

    @Override
    public Set<String> getNames() {
        Set<String> result = new LinkedHashSet<>(delegate.getNames());
        result.remove(RULE_CONTEXT);
        result.remove(BINDINGS);
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Iterator<Binding<?>> iterator() {
        List<Binding<?>> result = new ArrayList<>();

        for (Binding<?> binding : delegate) {
            if (!isHidden(binding.getName())) result.add(binding);
        }

        return result.iterator();
    }

    /**
     * The values without the two; a snapshot.
     */
    @Override
    public Map<String, Object> asMap() {
        Map<String, Object> result = new LinkedHashMap<>(delegate.asMap());
        result.remove(RULE_CONTEXT);
        result.remove(BINDINGS);
        return Collections.unmodifiableMap(result);
    }

    // --- everything else is the delegate's ----------------------------------------------------------

    @Override
    public <T> void bind(Binding<T> binding) {
        delegate.bind(binding);
    }

    @Override
    public void addBindingListener(BindingListener listener) {
        delegate.addBindingListener(listener);
    }

    @Override
    public boolean removeBindingListener(BindingListener listener) {
        return delegate.removeBindingListener(listener);
    }

    @Override
    public String prettyPrint(String prefix) {
        return delegate.prettyPrint(prefix);
    }

    @Override
    public String toString() {
        return "HiddenContextBindings(" + delegate + ")";
    }
}
