/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.rulii.lib.spring.core;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.IdentityHashMap;
import java.util.Map;

import org.rulii.lib.spring.lang.Nullable;
import org.rulii.lib.spring.util.Assert;
import org.rulii.lib.spring.util.ClassUtils;
import org.rulii.lib.spring.util.ConcurrentReferenceHashMap;
import org.rulii.lib.spring.util.ObjectUtils;
import org.rulii.lib.spring.util.StringUtils;

/**
 * Encapsulates a Java {@link Type}, providing access to {@link #getSuperType()
 * supertypes}, {@link #getInterfaces() interfaces}, and {@link #getGeneric(int...) generic
 * parameters} along with the ability to ultimately {@link #resolve() resolve} to a
 * {@link Class}.
 *
 * <p>
 * {@code ResolvableTypes} may be obtained from {@link #forField(Field) fields},
 * {@link #forMethodParameter(Method, int) method parameters}, {@link #forMethodReturnType(Method)
 * method returns} or {@link #forClass(Class) classes}. Most methods on this class will themselves
 * return {@link ResolvableType}s, allowing easy navigation. For example:
 *
 * <pre class="code">
 * private HashMap&lt;Integer, List&lt;String&gt;&gt; myMap;
 *
 * public void example() {
 *   ResolvableType t = ResolvableType.forField(getClass().getDeclaredField("myMap"));
 *   t.getSuperType(); // AbstractMap&lt;Integer, List&lt;String&gt;&gt;
 *   t.asMap(); // Map&lt;Integer, List&lt;String&gt;&gt;
 *   t.getGeneric(0).resolve(); // Integer
 *   t.getGeneric(1).resolve(); // List
 *   t.getGeneric(1); // List&lt;String&gt;
 *   t.resolveGeneric(1, 0); // String
 * }
 * </pre>
 *
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @author Stephane Nicoll
 * @since 4.0
 * @see #forField(Field)
 * @see #forMethodParameter(Method, int)
 * @see #forMethodReturnType(Method)
 * @see #forConstructorParameter(Constructor, int)
 * @see #forClass(Class)
 * @see #forType(Type)
 * @see #forInstance(Object)
 * @see ResolvableTypeProvider
 */

@SuppressWarnings("serial")
public class ResolvableType implements Serializable {

  /**
   * {@code ResolvableType} returned when no value is available. {@code NONE} is used in preference
   * to {@code null} so that multiple method calls can be safely chained.
   */
  public static final ResolvableType NONE = new ResolvableType(EmptyType.INSTANCE, null, null, 0);

  private static final ResolvableType[] EMPTY_TYPES_ARRAY = new ResolvableType[0];

  private static final ConcurrentReferenceHashMap<ResolvableType, ResolvableType> cache = new ConcurrentReferenceHashMap<>(256);

  /**
   * The underlying Java type being managed.
   */
  private final Type type;

  /**
   * Optional provider for the type.
   */
  @Nullable
  private final SerializableTypeWrapper.TypeProvider typeProvider;

  /**
   * The {@code VariableResolver} to use or {@code null} if no resolver is available.
   */
  @Nullable
  private final VariableResolver variableResolver;

  /**
   * The component type for an array or {@code null} if the type should be deduced.
   */
  @Nullable
  private final ResolvableType componentType;

  @Nullable
  private final Integer hash;

  @Nullable
  private Class<?> resolved;

  @Nullable
  private volatile ResolvableType superType;

  @Nullable
  private volatile ResolvableType[] interfaces;

  @Nullable
  private volatile ResolvableType[] generics;

  /**
   * Private constructor used to create a new {@link ResolvableType} for cache key purposes, with no
   * upfront resolution.
   */
  private ResolvableType(
          Type type, @Nullable SerializableTypeWrapper.TypeProvider typeProvider, @Nullable VariableResolver variableResolver) {

    this.type = type;
    this.typeProvider = typeProvider;
    this.variableResolver = variableResolver;
    this.componentType = null;
    this.hash = calculateHashCode();
    this.resolved = null;
  }

  /**
   * Private constructor used to create a new {@link ResolvableType} for cache value purposes, with
   * upfront resolution and a pre-calculated hash.
   *
   * @since 4.2
   */
  private ResolvableType(Type type, @Nullable SerializableTypeWrapper.TypeProvider typeProvider,
      @Nullable VariableResolver variableResolver, @Nullable Integer hash) {

    this.type = type;
    this.typeProvider = typeProvider;
    this.variableResolver = variableResolver;
    this.componentType = null;
    this.hash = hash;
    this.resolved = resolveClass();
  }

  /**
   * Private constructor used to create a new {@link ResolvableType} for uncached purposes, with
   * upfront resolution but lazily calculated hash.
   */
  private ResolvableType(Type type, @Nullable SerializableTypeWrapper.TypeProvider typeProvider,
      @Nullable VariableResolver variableResolver, @Nullable ResolvableType componentType) {

    this.type = type;
    this.typeProvider = typeProvider;
    this.variableResolver = variableResolver;
    this.componentType = componentType;
    this.hash = null;
    this.resolved = resolveClass();
  }

  /**
   * Private constructor used to create a new {@link ResolvableType} on a {@link Class} basis.
   * Avoids all {@code instanceof} checks in order to create a straight {@link Class} wrapper.
   *
   * @since 4.2
   */
  private ResolvableType(@Nullable Class<?> clazz) {
    this.resolved = (clazz != null ? clazz : Object.class);
    this.type = this.resolved;
    this.typeProvider = null;
    this.variableResolver = null;
    this.componentType = null;
    this.hash = null;
  }

  /**
   * Return the underling Java {@link Type} being managed.
   */
  public Type getType() {
    return SerializableTypeWrapper.unwrap(this.type);
  }

  /**
   * Return the underlying Java {@link Class} being managed, if available; otherwise {@code null}.
   */
  @Nullable
  public Class<?> getRawClass() {
    if (this.type == this.resolved) {
      return this.resolved;
    }
    Type rawType = this.type;
    if (rawType instanceof ParameterizedType) {
      rawType = ((ParameterizedType) rawType).getRawType();
    }
    return (rawType instanceof Class ? (Class<?>) rawType : null);
  }

  /**
   * Return the underlying source of the resolvable type. Will return a {@link Field},
   * {@link MethodParameter} or {@link Type} depending on how the {@link ResolvableType} was
   * constructed. With the exception of the {@link #NONE} constant, this method will never return
   * {@code null}. This method is primarily to provide access to additional type information or
   * meta-data that alternative JVM languages may provide.
   */
  public Object getSource() {
    Object source = (this.typeProvider != null ? this.typeProvider.getSource() : null);
    return (source != null ? source : this.type);
  }

  /**
   * Determine whether this {@code ResolvableType} is assignable from the specified other type.
   * <p>
   * Attempts to follow the same rules as the Java compiler, considering whether both the
   * {@link #resolve() resolved} {@code Class} is {@link Class#isAssignableFrom(Class) assignable
   * from} the given type as well as whether all {@link #getGenerics() generics} are assignable.
   *
   * @param other the type to be checked against (as a {@code ResolvableType})
   * @return {@code true} if the specified other type can be assigned to this
   *         {@code ResolvableType}; {@code false} otherwise
   */
  public boolean isAssignableFrom(ResolvableType other) {
    return isAssignableFrom(other, null);
  }

  private boolean isAssignableFrom(ResolvableType other, @Nullable Map<Type, Type> matchedBefore) {
    Assert.notNull(other, "ResolvableType must not be null");

    // If we cannot resolve types, we are not assignable
    if (this == NONE || other == NONE) {
      return false;
    }

    // Deal with array by delegating to the component type
    if (isArray()) {
      return (other.isArray() && getComponentType().isAssignableFrom(other.getComponentType()));
    }

    if (matchedBefore != null && matchedBefore.get(this.type) == other.type) {
      return true;
    }

    // Deal with wildcard bounds
    WildcardBounds ourBounds = WildcardBounds.get(this);
    WildcardBounds typeBounds = WildcardBounds.get(other);

    // In the form X is assignable to <? extends Number>
    if (typeBounds != null) {
      return (ourBounds != null && ourBounds.isSameKind(typeBounds) &&
          ourBounds.isAssignableFrom(typeBounds.getBounds()));
    }

    // In the form <? extends Number> is assignable to X...
    if (ourBounds != null) {
      return ourBounds.isAssignableFrom(other);
    }

    // Main assignability check about to follow
    boolean exactMatch = (matchedBefore != null);  // We're checking nested generic variables now...
    boolean checkGenerics = true;
    Class<?> ourResolved = null;
    if (this.type instanceof TypeVariable) {
      TypeVariable<?> variable = (TypeVariable<?>) this.type;
      // Try default variable resolution
      if (this.variableResolver != null) {
        ResolvableType resolved = this.variableResolver.resolveVariable(variable);
        if (resolved != null) {
          ourResolved = resolved.resolve();
        }
      }
      if (ourResolved == null) {
        // Try variable resolution against target type
        if (other.variableResolver != null) {
          ResolvableType resolved = other.variableResolver.resolveVariable(variable);
          if (resolved != null) {
            ourResolved = resolved.resolve();
            checkGenerics = false;
          }
        }
      }
      if (ourResolved == null) {
        // Unresolved type variable, potentially nested -> never insist on exact match
        exactMatch = false;
      }
    }
    if (ourResolved == null) {
      ourResolved = resolve(Object.class);
    }
    Class<?> otherResolved = other.resolve(Object.class);

    // We need an exact type match for generics
    // List<CharSequence> is not assignable from List<String>
    if (exactMatch ? !ourResolved.equals(otherResolved) : !ClassUtils.isAssignable(ourResolved, otherResolved)) {
      return false;
    }

    if (checkGenerics) {
      // Recursively check each generic
      ResolvableType[] ourGenerics = getGenerics();
      ResolvableType[] typeGenerics = other.as(ourResolved).getGenerics();
      if (ourGenerics.length != typeGenerics.length) {
        return false;
      }
      if (matchedBefore == null) {
        matchedBefore = new IdentityHashMap<>(1);
      }
      matchedBefore.put(this.type, other.type);
      for (int i = 0; i < ourGenerics.length; i++) {
        if (!ourGenerics[i].isAssignableFrom(typeGenerics[i], matchedBefore)) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Return {@code true} if this type resolves to a Class that represents an array.
   *
   * @see #getComponentType()
   */
  public boolean isArray() {
    if (this == NONE) {
      return false;
    }
    return ((this.type instanceof Class && ((Class<?>) this.type).isArray()) ||
        this.type instanceof GenericArrayType || resolveType().isArray());
  }

  /**
   * Return the ResolvableType representing the component type of the array or {@link #NONE} if this
   * type does not represent an array.
   *
   * @see #isArray()
   */
  public ResolvableType getComponentType() {
    if (this == NONE) {
      return NONE;
    }
    if (this.componentType != null) {
      return this.componentType;
    }
    if (this.type instanceof Class) {
      Class<?> componentType = ((Class<?>) this.type).getComponentType();
      return forType(componentType, this.variableResolver);
    }
    if (this.type instanceof GenericArrayType) {
      return forType(((GenericArrayType) this.type).getGenericComponentType(), this.variableResolver);
    }
    return resolveType().getComponentType();
  }

  /**
   * Return this type as a {@link ResolvableType} of the specified class. Searches
   * {@link #getSuperType() supertype} and {@link #getInterfaces() interface} hierarchies to find a
   * match, returning {@link #NONE} if this type does not implement or extend the specified class.
   *
   * @param type the required type (typically narrowed)
   * @return a {@link ResolvableType} representing this object as the specified type, or
   *         {@link #NONE} if not resolvable as that type
   * @see #asCollection()
   * @see #asMap()
   * @see #getSuperType()
   * @see #getInterfaces()
   */
  public ResolvableType as(Class<?> type) {
    if (this == NONE) {
      return NONE;
    }
    if (ObjectUtils.nullSafeEquals(resolve(), type)) {
      return this;
    }
    for (ResolvableType interfaceType : getInterfaces()) {
      ResolvableType interfaceAsType = interfaceType.as(type);
      if (interfaceAsType != NONE) {
        return interfaceAsType;
      }
    }
    return getSuperType().as(type);
  }

  /**
   * Return a {@link ResolvableType} representing the direct supertype of this type. If no supertype
   * is available this method returns {@link #NONE}.
   *
   * @see #getInterfaces()
   */
  public ResolvableType getSuperType() {
    Class<?> resolved = resolve();
    if (resolved == null || resolved.getGenericSuperclass() == null) {
      return NONE;
    }
    ResolvableType superType = this.superType;
    if (superType == null) {
      superType = forType(SerializableTypeWrapper.forGenericSuperclass(resolved), asVariableResolver());
      this.superType = superType;
    }
    return superType;
  }

  /**
   * Return a {@link ResolvableType} array representing the direct interfaces implemented by this
   * type. If this type does not implement any interfaces an empty array is returned.
   *
   * @see #getSuperType()
   */
  public ResolvableType[] getInterfaces() {
    Class<?> resolved = resolve();
    if (resolved == null || ObjectUtils.isEmpty(resolved.getGenericInterfaces())) {
      return EMPTY_TYPES_ARRAY;
    }
    ResolvableType[] interfaces = this.interfaces;
    if (interfaces == null) {
      interfaces = forTypes(SerializableTypeWrapper.forGenericInterfaces(resolved), asVariableResolver());
      this.interfaces = interfaces;
    }
    return interfaces;
  }

  /**
   * Return {@code true} if this type contains generic parameters.
   *
   * @see #getGeneric(int...)
   * @see #getGenerics()
   */
  public boolean hasGenerics() {
    return (getGenerics().length > 0);
  }

  /**
   * Return a {@link ResolvableType} for the specified nesting level. The nesting level refers to
   * the specific generic parameter that should be returned. A nesting level of 1 indicates this
   * type; 2 indicates the first nested generic; 3 the second; and so on. For example, given
   * {@code List<Set<Integer>>} level 1 refers to the {@code List}, level 2 the {@code Set}, and
   * level 3 the {@code Integer}.
   * <p>
   * The {@code typeIndexesPerLevel} map can be used to reference a specific generic for the given
   * level. For example, an index of 0 would refer to a {@code Map} key; whereas, 1 would refer to
   * the value. If the map does not contain a value for a specific level the last generic will be
   * used (e.g. a {@code Map} value).
   * <p>
   * Nesting levels may also apply to array types; for example given {@code String[]}, a nesting
   * level of 2 refers to {@code String}.
   * <p>
   * If a type does not {@link #hasGenerics() contain} generics the {@link #getSuperType()
   * supertype} hierarchy will be considered.
   *
   * @param nestingLevel the required nesting level, indexed from 1 for the current type, 2 for the
   *          first nested generic, 3 for the second and so on
   * @param typeIndexesPerLevel a map containing the generic index for a given nesting level (may be
   *          {@code null})
   * @return a {@link ResolvableType} for the nested level or {@link #NONE}
   */
  public ResolvableType getNested(int nestingLevel, @Nullable Map<Integer, Integer> typeIndexesPerLevel) {
    ResolvableType result = this;
    for (int i = 2; i <= nestingLevel; i++) {
      if (result.isArray()) {
        result = result.getComponentType();
      } else {
        // Handle derived types
        while (result != ResolvableType.NONE && !result.hasGenerics()) {
          result = result.getSuperType();
        }
        Integer index = (typeIndexesPerLevel != null ? typeIndexesPerLevel.get(i) : null);
        index = (index == null ? result.getGenerics().length - 1 : index);
        result = result.getGeneric(index);
      }
    }
    return result;
  }

  /**
   * Return a {@link ResolvableType} representing the generic parameter for the given indexes.
   * Indexes are zero based; for example given the type {@code Map<Integer, List<String>>},
   * {@code getGeneric(0)} will access the {@code Integer}. Nested generics can be accessed by
   * specifying multiple indexes; for example {@code getGeneric(1, 0)} will access the
   * {@code String} from the nested {@code List}. For convenience, if no indexes are specified the
   * first generic is returned.
   * <p>
   * If no generic is available at the specified indexes {@link #NONE} is returned.
   *
   * @param indexes the indexes that refer to the generic parameter (may be omitted to return the
   *          first generic)
   * @return a {@link ResolvableType} for the specified generic or {@link #NONE}
   * @see #hasGenerics()
   * @see #getGenerics()
   * @see #resolveGeneric(int...)
   * @see #resolveGenerics()
   */
  public ResolvableType getGeneric(@Nullable int... indexes) {
    ResolvableType[] generics = getGenerics();
    if (indexes == null || indexes.length == 0) {
      return (generics.length == 0 ? NONE : generics[0]);
    }
    ResolvableType generic = this;
    for (int index : indexes) {
      generics = generic.getGenerics();
      if (index < 0 || index >= generics.length) {
        return NONE;
      }
      generic = generics[index];
    }
    return generic;
  }

  /**
   * Return an array of {@link ResolvableType}s representing the generic parameters of this type. If
   * no generics are available an empty array is returned. If you need to access a specific generic
   * consider using the {@link #getGeneric(int...)} method as it allows access to nested generics
   * and protects against {@code IndexOutOfBoundsExceptions}.
   *
   * @return an array of {@link ResolvableType}s representing the generic parameters (never
   *         {@code null})
   * @see #hasGenerics()
   * @see #getGeneric(int...)
   * @see #resolveGeneric(int...)
   * @see #resolveGenerics()
   */
  public ResolvableType[] getGenerics() {
    if (this == NONE) {
      return EMPTY_TYPES_ARRAY;
    }
    ResolvableType[] generics = this.generics;
    if (generics == null) {
      if (this.type instanceof Class) {
        Class<?> typeClass = (Class<?>) this.type;
        generics = forTypes(SerializableTypeWrapper.forTypeParameters(typeClass), this.variableResolver);
      } else if (this.type instanceof ParameterizedType) {
        Type[] actualTypeArguments = ((ParameterizedType) this.type).getActualTypeArguments();
        generics = new ResolvableType[actualTypeArguments.length];
        for (int i = 0; i < actualTypeArguments.length; i++) {
          generics[i] = forType(actualTypeArguments[i], this.variableResolver);
        }
      } else {
        generics = resolveType().getGenerics();
      }
      this.generics = generics;
    }
    return generics;
  }

  /**
   * Resolve this type to a {@link Class}, returning {@code null} if the type cannot be
   * resolved. This method will consider bounds of {@link TypeVariable}s and {@link WildcardType}s
   * if direct resolution fails; however, bounds of {@code Object.class} will be ignored.
   *
   * @return the resolved {@link Class}, or {@code null} if not resolvable
   * @see #resolve(Class)
   * @see #resolveGeneric(int...)
   * @see #resolveGenerics()
   */
  @Nullable
  public Class<?> resolve() {
    return this.resolved;
  }

  /**
   * Resolve this type to a {@link Class}, returning the specified {@code fallback} if the
   * type cannot be resolved. This method will consider bounds of {@link TypeVariable}s and
   * {@link WildcardType}s if direct resolution fails; however, bounds of {@code Object.class} will
   * be ignored.
   *
   * @param fallback the fallback class to use if resolution fails
   * @return the resolved {@link Class} or the {@code fallback}
   * @see #resolve()
   * @see #resolveGeneric(int...)
   * @see #resolveGenerics()
   */
  public Class<?> resolve(Class<?> fallback) {
    return (this.resolved != null ? this.resolved : fallback);
  }

  @Nullable
  private Class<?> resolveClass() {
    if (this.type == EmptyType.INSTANCE) {
      return null;
    }
    if (this.type instanceof Class) {
      return (Class<?>) this.type;
    }
    if (this.type instanceof GenericArrayType) {
      Class<?> resolvedComponent = getComponentType().resolve();
      return (resolvedComponent != null ? Array.newInstance(resolvedComponent, 0).getClass() : null);
    }
    return resolveType().resolve();
  }

  /**
   * Resolve this type by a single level, returning the resolved value or {@link #NONE}.
   * <p>
   * Note: The returned {@link ResolvableType} should only be used as an intermediary as it cannot
   * be serialized.
   */
  ResolvableType resolveType() {
    if (this.type instanceof ParameterizedType) {
      return forType(((ParameterizedType) this.type).getRawType(), this.variableResolver);
    }
    if (this.type instanceof WildcardType) {
      Type resolved = resolveBounds(((WildcardType) this.type).getUpperBounds());
      if (resolved == null) {
        resolved = resolveBounds(((WildcardType) this.type).getLowerBounds());
      }
      return forType(resolved, this.variableResolver);
    }
    if (this.type instanceof TypeVariable) {
      TypeVariable<?> variable = (TypeVariable<?>) this.type;
      // Try default variable resolution
      if (this.variableResolver != null) {
        ResolvableType resolved = this.variableResolver.resolveVariable(variable);
        if (resolved != null) {
          return resolved;
        }
      }
      // Fallback to bounds
      return forType(resolveBounds(variable.getBounds()), this.variableResolver);
    }
    return NONE;
  }

  @Nullable
  private Type resolveBounds(Type[] bounds) {
    if (ObjectUtils.isEmpty(bounds) || Object.class == bounds[0]) {
      return null;
    }
    return bounds[0];
  }

  @Nullable
  private ResolvableType resolveVariable(TypeVariable<?> variable) {
    if (this.type instanceof TypeVariable) {
      return resolveType().resolveVariable(variable);
    }
    if (this.type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) this.type;
      Class<?> resolved = resolve();
      if (resolved == null) {
        return null;
      }
      TypeVariable<?>[] variables = resolved.getTypeParameters();
      for (int i = 0; i < variables.length; i++) {
        if (ObjectUtils.nullSafeEquals(variables[i].getName(), variable.getName())) {
          Type actualType = parameterizedType.getActualTypeArguments()[i];
          return forType(actualType, this.variableResolver);
        }
      }
      Type ownerType = parameterizedType.getOwnerType();
      if (ownerType != null) {
        return forType(ownerType, this.variableResolver).resolveVariable(variable);
      }
    }
    if (this.variableResolver != null) {
      return this.variableResolver.resolveVariable(variable);
    }
    return null;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof ResolvableType)) {
      return false;
    }

    ResolvableType otherType = (ResolvableType) other;
    if (!ObjectUtils.nullSafeEquals(this.type, otherType.type)) {
      return false;
    }
    if (this.typeProvider != otherType.typeProvider &&
        (this.typeProvider == null || otherType.typeProvider == null ||
            !ObjectUtils.nullSafeEquals(this.typeProvider.getType(), otherType.typeProvider.getType()))) {
      return false;
    }
    if (this.variableResolver != otherType.variableResolver &&
        (this.variableResolver == null || otherType.variableResolver == null ||
            !ObjectUtils.nullSafeEquals(this.variableResolver.getSource(), otherType.variableResolver.getSource()))) {
      return false;
    }
    if (!ObjectUtils.nullSafeEquals(this.componentType, otherType.componentType)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    return (this.hash != null ? this.hash : calculateHashCode());
  }

  private int calculateHashCode() {
    int hashCode = ObjectUtils.nullSafeHashCode(this.type);
    if (this.typeProvider != null) {
      hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.typeProvider.getType());
    }
    if (this.variableResolver != null) {
      hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.variableResolver.getSource());
    }
    if (this.componentType != null) {
      hashCode = 31 * hashCode + ObjectUtils.nullSafeHashCode(this.componentType);
    }
    return hashCode;
  }

  /**
   * Adapts this {@link ResolvableType} to a {@link VariableResolver}.
   */
  @Nullable
  VariableResolver asVariableResolver() {
    if (this == NONE) {
      return null;
    }
    return new DefaultVariableResolver();
  }

  /**
   * Custom serialization support for {@link #NONE}.
   */
  private Object readResolve() {
    return (this.type == EmptyType.INSTANCE ? NONE : this);
  }

  /**
   * Return a String representation of this type in its fully resolved form (including any generic
   * parameters).
   */
  @Override
  public String toString() {
    if (isArray()) {
      return getComponentType() + "[]";
    }
    if (this.resolved == null) {
      return "?";
    }
    if (this.type instanceof TypeVariable) {
      TypeVariable<?> variable = (TypeVariable<?>) this.type;
      if (this.variableResolver == null || this.variableResolver.resolveVariable(variable) == null) {
        // Don't bother with variable boundaries for toString()...
        // Can cause infinite recursions in case of self-references
        return "?";
      }
    }
    StringBuilder result = new StringBuilder(this.resolved.getName());
    if (hasGenerics()) {
      result.append('<');
      result.append(StringUtils.arrayToDelimitedString(getGenerics(), ", "));
      result.append('>');
    }
    return result.toString();
  }

  // Factory methods

  /**
   * Return a {@link ResolvableType} for the specified {@link Method} parameter with a given
   * implementation. Use this variant when the class that declares the method includes generic
   * parameter variables that are satisfied by the implementation class.
   *
   * @param method the source method (must not be {@code null})
   * @param parameterIndex the parameter index
   * @param implementationClass the implementation class
   * @return a {@link ResolvableType} for the specified method parameter
   * @see #forMethodParameter(Method, int, Class)
   * @see #forMethodParameter(MethodParameter)
   */
  public static ResolvableType forMethodParameter(Method method, int parameterIndex, Class<?> implementationClass) {
    Assert.notNull(method, "Method must not be null");
    MethodParameter methodParameter = new MethodParameter(method, parameterIndex);
    methodParameter.setContainingClass(implementationClass);
    return forMethodParameter(methodParameter);
  }

  /**
   * Return a {@link ResolvableType} for the specified {@link MethodParameter}.
   *
   * @param methodParameter the source method parameter (must not be {@code null})
   * @return a {@link ResolvableType} for the specified method parameter
   * @see #forMethodParameter(Method, int)
   */
  public static ResolvableType forMethodParameter(MethodParameter methodParameter) {
    return forMethodParameter(methodParameter, (Type) null);
  }

  /**
   * Return a {@link ResolvableType} for the specified {@link MethodParameter}, overriding the
   * target type to resolve with a specific given type.
   *
   * @param methodParameter the source method parameter (must not be {@code null})
   * @param targetType the type to resolve (a part of the method parameter's type)
   * @return a {@link ResolvableType} for the specified method parameter
   * @see #forMethodParameter(Method, int)
   */
  public static ResolvableType forMethodParameter(MethodParameter methodParameter, @Nullable Type targetType) {
    Assert.notNull(methodParameter, "MethodParameter must not be null");
    ResolvableType owner = forType(methodParameter.getContainingClass()).as(methodParameter.getDeclaringClass());
    return forType(targetType, new SerializableTypeWrapper.MethodParameterTypeProvider(methodParameter), owner.asVariableResolver())
        .getNested(methodParameter.getNestingLevel(), methodParameter.typeIndexesPerLevel);
  }

  private static ResolvableType[] forTypes(Type[] types, @Nullable VariableResolver owner) {
    ResolvableType[] result = new ResolvableType[types.length];
    for (int i = 0; i < types.length; i++) {
      result[i] = forType(types[i], owner);
    }
    return result;
  }

  /**
   * Return a {@link ResolvableType} for the specified {@link Type}. Note: The resulting
   * {@link ResolvableType} may not be {@link Serializable}.
   *
   * @param type the source type (potentially {@code null})
   * @return a {@link ResolvableType} for the specified {@link Type}
   * @see #forType(Type, ResolvableType)
   */
  public static ResolvableType forType(@Nullable Type type) {
    return forType(type, null, null);
  }

  /**
   * Return a {@link ResolvableType} for the specified {@link Type} backed by a given
   * {@link VariableResolver}.
   *
   * @param type the source type or {@code null}
   * @param variableResolver the variable resolver or {@code null}
   * @return a {@link ResolvableType} for the specified {@link Type} and {@link VariableResolver}
   */
  static ResolvableType forType(@Nullable Type type, @Nullable VariableResolver variableResolver) {
    return forType(type, null, variableResolver);
  }

  /**
   * Return a {@link ResolvableType} for the specified {@link Type} backed by a given
   * {@link VariableResolver}.
   *
   * @param type the source type or {@code null}
   * @param typeProvider the type provider or {@code null}
   * @param variableResolver the variable resolver or {@code null}
   * @return a {@link ResolvableType} for the specified {@link Type} and {@link VariableResolver}
   */
  static ResolvableType forType(
          @Nullable Type type, @Nullable SerializableTypeWrapper.TypeProvider typeProvider, @Nullable VariableResolver variableResolver) {

    if (type == null && typeProvider != null) {
      type = SerializableTypeWrapper.forTypeProvider(typeProvider);
    }
    if (type == null) {
      return NONE;
    }

    // For simple Class references, build the wrapper right away -
    // no expensive resolution necessary, so not worth caching...
    if (type instanceof Class) {
      return new ResolvableType(type, typeProvider, variableResolver, (ResolvableType) null);
    }

    // Purge empty entries on access since we don't have a clean-up thread or the like.
    cache.purgeUnreferencedEntries();

    // Check the cache - we may have a ResolvableType which has been resolved before...
    ResolvableType resultType = new ResolvableType(type, typeProvider, variableResolver);
    ResolvableType cachedType = cache.get(resultType);
    if (cachedType == null) {
      cachedType = new ResolvableType(type, typeProvider, variableResolver, resultType.hash);
      cache.put(cachedType, cachedType);
    }
    resultType.resolved = cachedType.resolved;
    return resultType;
  }

  /**
   * Strategy interface used to resolve {@link TypeVariable}s.
   */
  interface VariableResolver extends Serializable {

    /**
     * Return the source of the resolver (used for hashCode and equals).
     */
    Object getSource();

    /**
     * Resolve the specified variable.
     *
     * @param variable the variable to resolve
     * @return the resolved variable, or {@code null} if not found
     */
    @Nullable
    ResolvableType resolveVariable(TypeVariable<?> variable);
  }

  private class DefaultVariableResolver implements VariableResolver {

    @Override
    @Nullable
    public ResolvableType resolveVariable(TypeVariable<?> variable) {
      return ResolvableType.this.resolveVariable(variable);
    }

    @Override
    public Object getSource() {
      return ResolvableType.this;
    }
  }

  /**
   * Internal helper to handle bounds from {@link WildcardType}s.
   */
  private static class WildcardBounds {

    private final Kind kind;

    private final ResolvableType[] bounds;

    /**
     * Internal constructor to create a new {@link WildcardBounds} instance.
     *
     * @param kind the kind of bounds
     * @param bounds the bounds
     * @see #get(ResolvableType)
     */
    public WildcardBounds(Kind kind, ResolvableType[] bounds) {
      this.kind = kind;
      this.bounds = bounds;
    }

    /**
     * Return {@code true} if this bounds is the same kind as the specified bounds.
     */
    public boolean isSameKind(WildcardBounds bounds) {
      return this.kind == bounds.kind;
    }

    /**
     * Return {@code true} if this bounds is assignable to all the specified types.
     *
     * @param types the types to test against
     * @return {@code true} if this bounds is assignable to all types
     */
    public boolean isAssignableFrom(ResolvableType... types) {
      for (ResolvableType bound : this.bounds) {
        for (ResolvableType type : types) {
          if (!isAssignable(bound, type)) {
            return false;
          }
        }
      }
      return true;
    }

    private boolean isAssignable(ResolvableType source, ResolvableType from) {
      return (this.kind == Kind.UPPER ? source.isAssignableFrom(from) : from.isAssignableFrom(source));
    }

    /**
     * Return the underlying bounds.
     */
    public ResolvableType[] getBounds() {
      return this.bounds;
    }

    /**
     * Get a {@link WildcardBounds} instance for the specified type, returning {@code null} if the
     * specified type cannot be resolved to a {@link WildcardType}.
     *
     * @param type the source type
     * @return a {@link WildcardBounds} instance or {@code null}
     */
    @Nullable
    public static WildcardBounds get(ResolvableType type) {
      ResolvableType resolveToWildcard = type;
      while (!(resolveToWildcard.getType() instanceof WildcardType)) {
        if (resolveToWildcard == NONE) {
          return null;
        }
        resolveToWildcard = resolveToWildcard.resolveType();
      }
      WildcardType wildcardType = (WildcardType) resolveToWildcard.type;
      Kind boundsType = (wildcardType.getLowerBounds().length > 0 ? Kind.LOWER : Kind.UPPER);
      Type[] bounds = (boundsType == Kind.UPPER ? wildcardType.getUpperBounds() : wildcardType.getLowerBounds());
      ResolvableType[] resolvableBounds = new ResolvableType[bounds.length];
      for (int i = 0; i < bounds.length; i++) {
        resolvableBounds[i] = ResolvableType.forType(bounds[i], type.variableResolver);
      }
      return new WildcardBounds(boundsType, resolvableBounds);
    }

    /**
     * The various kinds of bounds.
     */
    enum Kind {
      UPPER,
      LOWER
    }
  }

  @SuppressWarnings("serial")
  static class EmptyType implements Type, Serializable {

    static final Type INSTANCE = new EmptyType();

    Object readResolve() {
      return INSTANCE;
    }
  }

}
