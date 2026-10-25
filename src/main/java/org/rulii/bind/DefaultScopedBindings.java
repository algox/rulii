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

import org.rulii.lib.apache.commons.logging.Log;
import org.rulii.lib.apache.commons.logging.LogFactory;
import org.rulii.lib.spring.util.Assert;
import org.rulii.util.RuleUtils;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.rulii.bind.Bindings.builder;

/**
 * Default implementation of the Scoped Bindings.
 *
 * @author Max Arulananthan
 * @since 1.0
 *
 */
public class DefaultScopedBindings implements ScopedBindings, Map<String, Object> {

    private static final Log logger = LogFactory.getLog(DefaultScopedBindings.class);

    private final Stack<NamedScope> scopes = new Stack<>();
    private final List<BindingListener> listeners = new LinkedList<>();
    // Number of scopes inherited from a parent by fork(); zero for a stack of our own. Scopes below
    // the floor are shared with the parent and never popped, listened to or searched for removal here.
    private final int floor;

    DefaultScopedBindings(String name) {
        this(name, builder().standard());
    }

    DefaultScopedBindings(String name, Bindings bindings) {
        super();
        Assert.hasText(name, "name cannot be null/empty.");
        Assert.notNull(bindings, "bindings cannot be null.");
        this.floor = 0;
        this.scopes.push(new NamedScope(name, bindings));
    }

    /**
     * Creates a fork: the inherited scopes (by reference, shared with the parent) at the bottom, the parent's
     * listeners copied, and a fresh scope of the given name on top as the first scope this stack owns.
     *
     * <p>The reserved {@code ruleContext} and {@code bindings} are not inherited: they describe the stack they were
     * bound in (the parent's {@code bindings} is the parent's stack), so a fork rebinds its own. An inherited scope
     * holding them is replaced by a scope of the same name over the same Binding objects minus those two; every other
     * scope is inherited as is.
     *
     * @param inherited the parent's scopes at the time of the fork; must not be empty.
     * @param forkScopeName name of the first owned scope.
     * @param listeners the parent's listeners.
     */
    DefaultScopedBindings(List<NamedScope> inherited, String forkScopeName, List<BindingListener> listeners) {
        super();
        Assert.notNull(inherited, "inherited cannot be null.");
        Assert.isTrue(!inherited.isEmpty(), "inherited cannot be empty.");
        Assert.hasText(forkScopeName, "forkScopeName cannot be null/empty.");
        Assert.notNull(listeners, "listeners cannot be null.");
        this.floor = inherited.size();

        for (NamedScope scope : inherited) {
            this.scopes.push(withoutReservedPair(scope));
        }

        this.listeners.addAll(listeners);
        this.scopes.push(new NamedScope(forkScopeName, createBindings()));
    }

    @Override
    public ScopedBindings fork() {
        return new DefaultScopedBindings(scopes, "fork-scope-" + UUID.randomUUID(), listeners);
    }

    /**
     * The scope itself if it holds neither reserved {@code ruleContext} nor {@code bindings}; otherwise a scope of the
     * same name over the same Binding objects (values stay shared) minus those two.
     */
    private static NamedScope withoutReservedPair(NamedScope scope) {
        Bindings source = scope.getBindings();
        String ruleContext = ReservedBindings.RULE_CONTEXT.getName();
        String bindings = ReservedBindings.BINDINGS.getName();

        if (!source.contains(ruleContext) && !source.contains(bindings)) {
            return scope;
        }

        Bindings result = builder().standard();

        for (Binding<?> binding : source) {
            if (ruleContext.equals(binding.getName()) || bindings.equals(binding.getName())) {
                continue;
            }

            if (result instanceof PromiscuousBinder promiscuous) {
                promiscuous.promiscuousBind(binding);
            } else {
                result.bind(binding);
            }
        }

        return new NamedScope(scope.getName(), result);
    }

    @Override
    public NamedScope addScope() {
        return addScope("anonymous-scope-" + UUID.randomUUID());
    }

    @Override
    public NamedScope addScope(String name) {
        Assert.hasText(name, "name cannot be null/empty.");
        return addScope(name, createBindings());
    }

    @Override
    public NamedScope addScope(String name, Bindings bindings) {
        Assert.hasText(name, "name cannot be empty/null.");
        Assert.notNull(bindings, "bindings cannot be null.");
        Bindings existing = getScopeBindings(name);

        if (existing != null) {
            throw new BindingsAlreadyExistsException(name, existing);
        }

        NamedScope result = new NamedScope(name, bindings);
        scopes.push(result);

        if (logger.isDebugEnabled()) {
            logger.debug("New Scope added [" + name + "]");
        }

        // Fire the Scope listeners
        for (BindingListener listener : listeners) {
            listener.onScopeAdd(result);
        }

        return result;
    }

    @Override
    public NamedScope getScope(String name) {
        Assert.hasText(name, "name cannot be empty/null.");
        NamedScope result = null;

        for (NamedScope scope : scopes) {
            // Compare the reference to make sure we match.
            if (scope.getName().equals(name)) {
                result = scope;
                break;
            }
        }

        return result;
    }

    @Override
    public Bindings getScopeBindings(String name) {
        Assert.hasText(name, "name cannot be empty/null.");
        NamedScope result = getScope(name);
        return result != null ? result.getBindings() : null;
    }

    @Override
    public String getScopeName(Bindings bindings) {
        Assert.notNull(bindings, "bindings cannot be null.");

        String result = null;

        for (NamedScope scope : scopes) {
            // Compare the reference to make sure we match.
            if (scope.getBindings() == bindings) {
                result = scope.getName();
                break;
            }
        }

        return result;
    }

    @Override
    public NamedScope removeScope() {
        // Check to make sure we are not removing the root scope (the fork scope, in a fork)
        if (scopes.size() == floor + 1) {
            throw new CannotRemoveRootScopeException();
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Scope removed [" + scopes.peek().getName() + "]");
        }

        NamedScope result = scopes.pop();

        // Fire the Scope listeners
        for (BindingListener listener : listeners) {
            listener.onScopeRemove(result);
        }

        return result;
    }

    @Override
    public NamedScope removeScope(String name) {
        Assert.hasText(name, "name cannot be empty/null.");
        Bindings bindings = getScopeBindings(name);

        // We couldn't find any such Scope.
        if (bindings == null) throw new NoSuchBindingsException(name);

        return removeScope(bindings);
    }

    @Override
    public NamedScope removeScope(Bindings target) {
        Assert.notNull(target, "target cannot be null.");

        // Check to make sure we are not removing the root scope (the fork scope, in a fork)
        if (scopes.size() == floor + 1) {
            throw new CannotRemoveRootScopeException();
        }

        boolean found = false;
        for (NamedScope scope : getOwnedScopes()) {
            // Compare the reference to make sure we match.
            if (scope.getBindings() == target) {
                found = true;
                break;
            }
        }

        // Could not find the requested Bindings (an inherited scope is not ours to remove)
        if (!found) {
            throw new NoSuchBindingsException(target);
        }

        NamedScope result;

        // We know the Scope exists;
        do {
            // Pop until we find our target
            result = removeScope();
        } while (result.getBindings() != target);

        return result;
    }

    @Override
    public void removeScope(NamedScope target) {
        Assert.notNull(target, "target cannot be null.");

        boolean found = false;
        for (NamedScope scope : getOwnedScopes()) {
            // Compare the reference to make sure we match.
            if (scope == target) {
                found = true;
                break;
            }
        }

        // Could not find the requested Scope (an inherited scope is not ours to remove)
        if (!found) {
            throw new NoSuchScopeException(target);
        }

        NamedScope result;

        // We know the Scope exists;
        do {
            // Pop until we find our target
            result = removeScope();
        } while (result != target);
    }

    @Override
    public <T> void bind(Binding<T> binding) {
        Assert.notNull(binding, "binding cannot be null.");
        List<Binding<T>> bindings = getAllBindings(binding.getName());

        for (Binding<T> b : bindings) {
            if (b.isFinal()) {
                throw new BindingAlreadyExistsException(b, binding, true);
            }
        }

        getCurrentBindings().bind(binding);
    }

    @Override
    public Bindings getCurrentBindings() {
        return getCurrentScope().getBindings();
    }

    @Override
    public NamedScope getCurrentScope() {
        return scopes.peek();
    }

    @Override
    public NamedScope getParentScope() {
        return getScopeSize() > 2 ? scopes.get(getScopeSize() - 2) : null;
    }

    @Override
    public NamedScope getRootScope() {
        return getScopeSize() > 0 ? scopes.get(0) : null;
    }

    @Override
    public NamedScope getGlobalScope() {
        return getScope(GLOBAL_SCOPE);
    }

    @Override
    public <T> Binding<T> getBinding(String name) {
        List<NamedScope> scopes = getScopes();

        if (scopes.isEmpty()) return null;

        Binding<T> result = null;
        int size = scopes.size();
        // Must start at end and come up
        for (int i = size - 1; i >=0; i--) {
            result = scopes.get(i).getBindings().getBinding(name);
            if (result != null) break;
        }

        return result;
    }

    @Override
    public <T> Binding<T> getBinding(String name, Type type) {
        List<NamedScope> scopes = getScopes();

        Binding<T> result = null;
        int size = scopes.size();
        // Must start at end and come up
        for (int i = size - 1; i >=0; i--) {
            result = scopes.get(i).getBindings().getBinding(name, type);
            if (result != null) break;
        }

        return result;
    }

    @Override
    public <T> List<Binding<T>> getBindings(Type type) {
        List<Binding<T>> result = new LinkedList<>();
        List<NamedScope> scopes = getScopes();

        int size = scopes.size();
        // Must start at end and come up
        for (int i = size - 1; i >=0; i--) {
            result.addAll(scopes.get(i).getBindings().getBindings(type));
            // Found something in this scope stop.
            if (!result.isEmpty()) break;
        }

        return result;
    }

    @Override
    public <T> List<Binding<T>> getAllBindings(Type type) {
        List<Binding<T>> result = new LinkedList<>();
        List<NamedScope> scopes = getScopes();

        // Must start at root and keep adding
        for (NamedScope scope : scopes) {
            result.addAll(scope.getBindings().getBindings(type));
        }

        return result;
    }

    @Override
    public <T> List<Binding<T>> getAllBindings(String name) {
        List<Binding<T>> result = new LinkedList<>();
        List<NamedScope> scopes = getScopes();

        // Must start at root and keep adding
        for (NamedScope scope : scopes) {
            Binding<T> match = scope.getBindings().getBinding(name);
            if (match != null) result.add(match);
        }

        return result;
    }

    @Override
    public int getScopeSize() {
        return scopes.size();
    }

    @Override
    public int size() {
        List<NamedScope> scopes = getScopes();
        int result = 0;

        for (NamedScope scope : scopes) {
            result += scope.getBindings().size();
        }

        return result;
    }

    @Override
    public Set<String> getNames() {
        return asMap().keySet();
    }

    /**
     * Iterator of all the Bindings starting with working scope and going up the Stack.
     *
     * @return all bindings (reverse order).
     */
    @Override
    public Iterator<Binding<?>> iterator() {
        List<NamedScope> scopes = getScopes();
        Set<Binding<?>> result = new LinkedHashSet<>();

        // Must start at root and keep adding
        for (NamedScope scope : scopes) {
            for (Binding<?> binding : scope.getBindings()) {
                result.add(binding);
            }
        }

        return result.iterator();
    }

    @Override
    public void addBindingListener(BindingListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        this.listeners.add(listener);
        // Add listener to all the scopes we own (inherited scopes belong to the parent)
        getOwnedScopes().forEach((NamedScope scope) -> scope.getBindings().addBindingListener(listener));
    }

    @Override
    public boolean removeBindingListener(BindingListener listener) {
        Assert.notNull(listener, "listener cannot be null.");
        AtomicBoolean result = new AtomicBoolean(false);

        getOwnedScopes().forEach((NamedScope scope) -> {
            if (scope.getBindings().removeBindingListener(listener)) {
                result.set(true);
            }
        });

        return result.get();
    }

    /**
     * Creates a new scope.
     *
     * @return newly created Bindings.
     */
    protected Bindings createBindings() {
        Bindings result = builder().standard();

        for (BindingListener listener : listeners) {
            result.addBindingListener(listener);
        }

        return result;
    }

    private List<NamedScope> getScopes() {
        return scopes;
    }

    /**
     * The scopes this stack owns: everything above the inherited floor (all of them for a stack of our own).
     *
     * @return owned scopes, bottom first.
     */
    private List<NamedScope> getOwnedScopes() {
        return floor == 0 ? scopes : scopes.subList(floor, scopes.size());
    }

    @Override
    public String prettyPrint(String prefix) {
        StringBuilder result = new StringBuilder();
        int scopeIndex = 0;

        for (NamedScope scope : scopes) {
            result.append("Scope (index = ").append(scopeIndex++).append(")");
            result.append(prefix).append(scope.getBindings().prettyPrint(getTabs(scopeIndex + 1)));
            result.append(prefix).append(getTabs(scopeIndex));
        }

        return result.toString();
    }

    @Override
    public Map<String, Object> asMap() {
        return this;
    }

    @Override
    public boolean containsKey(Object key) {
        Assert.notNull(key, "key cannot be null.");
        return contains(key.toString());
    }

    @Override
    public boolean containsValue(Object value) {
        for (Binding<?> binding : this) {
            if (Objects.equals(binding.getValue(), value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Object get(Object key) {
        Assert.notNull(key, "key cannot be null.");
        Binding<Object> result = getBinding(key.toString());
        return result != null ? result.getValue() : null;
    }

    @Override
    public Object put(String key, Object value) {
        return setValueOrBind(key, value);
    }

    @Override
    public Object remove(Object key) {
        throw new UnsupportedOperationException("Bindings does not support removal of values. Use setValue() instead.");
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        for (Entry<? extends String, ?> entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Bindings cannot be cleared.");
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Set<String> keySet() {
        Map<String, Binding<?>> result = new LinkedHashMap<>();

        // iterator() walks root -> current; put() (not putIfAbsent()) lets the innermost scope's
        // binding overwrite an outer one for a shadowed name, matching getBinding()'s own
        // current-scope-wins lookup precedence.
        for (Binding<?> binding : this) {
            result.put(binding.getName(), binding);
        }

        return result.keySet();
    }

    @Override
    public Collection<Object> values() {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Binding<?> binding : this) {
            result.put(binding.getName(), binding.getValue());
        }

        return result.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        Map<String, Entry<String, Object>> result = new LinkedHashMap<>();

        for (Binding<?> binding : this) {
            result.put(binding.getName(), new AbstractMap.SimpleEntry<>(binding.getName(), binding.getValue()));
        }

        return new LinkedHashSet<>(result.values());
    }

    @Override
    public int uniqueSize() {
        return keySet().size();
    }

    private String getTabs(int count) {
        return RuleUtils.TAB.repeat(Math.max(0, count));
    }

    @Override
    public String toString() {
        return "ScopedBindings(" + getScopeSize() + ")";
    }
}
