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
package org.rulii.test.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.rulii.annotation.Description;
import org.rulii.annotation.Given;
import org.rulii.annotation.Rule;
import org.rulii.annotation.Then;
import org.rulii.bind.match.BindingMatchingStrategy;
import org.rulii.bind.match.MatchByNameAndTypeMatchingStrategy;
import org.rulii.bind.match.MatchByNameMatchingStrategy;
import org.rulii.bind.match.MatchByNameThenTypeMatchingStrategy;
import org.rulii.bind.match.MatchByTypeMatchingStrategy;
import org.rulii.bind.match.SmartMatchByTypeMatchingStrategy;
import org.rulii.util.reflect.DefaultObjectFactory;
import org.rulii.util.reflect.ObjectFactory;

import java.util.List;

/**
 * Binding ParameterDefinition tests.
 *
 * @author Max Arulananthan
 */
public class ObjectFactoryTest {

    public ObjectFactoryTest() {
        super();
    }

    @Test
    public void strategyCreationTest() {
        ObjectFactory factory = ObjectFactory.builder().build();
        Assertions.assertInstanceOf(MatchByNameMatchingStrategy.class, factory.createBindingMatchingStrategy(MatchByNameMatchingStrategy.class));
        Assertions.assertInstanceOf(MatchByNameAndTypeMatchingStrategy.class, factory.createBindingMatchingStrategy(MatchByNameAndTypeMatchingStrategy.class));
        Assertions.assertInstanceOf(MatchByTypeMatchingStrategy.class, factory.createBindingMatchingStrategy(MatchByTypeMatchingStrategy.class));
        Assertions.assertInstanceOf(MatchByTypeMatchingStrategy.class, factory.createBindingMatchingStrategy(MatchByTypeMatchingStrategy.class));
        Assertions.assertInstanceOf(TestRule.class, factory.createRule(TestRule.class));
    }

    @Rule @Description("This is test rule")
    public static final class TestRule {

        public TestRule() {
            super();
        }

        @Given
        public boolean when() {
            return true;
        }

        @Then
        public void then() {

        }
    }

    @Test
    public void testCreateBindingMatchingStrategy_validClassType() {
        // Creating an instance of the ObjectFactoryBuilder
        ObjectFactory objectFactory = ObjectFactory.builder().build();
        // Creating a BindingMatchingStrategy instance from the ObjectFactory
        BindingMatchingStrategy bindingMatchingStrategy =
                objectFactory.createBindingMatchingStrategy(MatchByNameAndTypeMatchingStrategy.class);
        // Asserting the BindingMatchingStrategy instance is not null
        Assertions.assertNotNull(bindingMatchingStrategy);
    }

    @Test
    public void testObjectCache_isNotSharedAcrossDifferentFactoryInstances() {
        // Two independently-built, caching-enabled factories (e.g. for two unrelated
        // RuleContexts) must never hand out each other's cached instances.
        ObjectFactory factory1 = ObjectFactory.builder().useCache(true).build();
        ObjectFactory factory2 = ObjectFactory.builder().useCache(true).build();

        CacheableThing instance1 = factory1.createAction(CacheableThing.class);
        CacheableThing instance2 = factory2.createAction(CacheableThing.class);

        Assertions.assertNotSame(instance1, instance2);
    }

    public static class CacheableThing {

        public CacheableThing() {
            super();
        }
    }

    @Test
    public void testCreate_packagePrivateConstructor_isCreatable() {
        // This codebase's own convention (e.g. the 34 built-in ValueValidationRule subclasses)
        // is a package-private no-arg constructor plus a separate builder entry point - the
        // object factory must be able to instantiate those, not just public-constructor classes.
        ObjectFactory objectFactory = ObjectFactory.builder().build();
        PackagePrivateCtor instance = objectFactory.create(PackagePrivateCtor.class, false);
        Assertions.assertNotNull(instance);
    }

    public static class PackagePrivateCtor {

        PackagePrivateCtor() {
            super();
        }
    }

    /** A strategy from outside the engine. */
    public static class OwnStrategy extends MatchByNameMatchingStrategy {

        public OwnStrategy() {
            super();
        }
    }

    /** A factory of one's own: counts what it is asked to create. */
    private static class CountingObjectFactory extends DefaultObjectFactory {

        private int created;

        CountingObjectFactory() {
            super();
        }

        @Override
        public <T> T create(Class<T> type, boolean isUseCache) {
            created++;
            return super.create(type, isUseCache);
        }
    }

    @Test
    public void strategyIsCreatedOncePerFactory() {
        ObjectFactory factory = ObjectFactory.builder().build();
        ObjectFactory another = ObjectFactory.builder().build();

        for (Class<? extends BindingMatchingStrategy> type : List.of(MatchByNameMatchingStrategy.class,
                MatchByNameAndTypeMatchingStrategy.class, MatchByTypeMatchingStrategy.class,
                SmartMatchByTypeMatchingStrategy.class, MatchByNameThenTypeMatchingStrategy.class,
                OwnStrategy.class)) {
            BindingMatchingStrategy strategy = factory.createBindingMatchingStrategy(type);
            // Exactly the class asked for; the same instance from this factory, never another factory's.
            Assertions.assertEquals(type, strategy.getClass());
            Assertions.assertSame(strategy, factory.createBindingMatchingStrategy(type));
            Assertions.assertNotSame(strategy, another.createBindingMatchingStrategy(type));
        }
    }

    @Test
    public void onlyStrategiesAreCachedWhenTheCacheIsOff() {
        ObjectFactory factory = ObjectFactory.builder().build();

        Assertions.assertNotSame(factory.createAction(CacheableThing.class), factory.createAction(CacheableThing.class));
    }

    @Test
    public void factoryOfOnesOwnIsAskedForEveryStrategy() {
        // A subclass creates its objects its own way (rulii-spring asks the container): it sees every request.
        CountingObjectFactory factory = new CountingObjectFactory();
        BindingMatchingStrategy first = factory.createBindingMatchingStrategy(MatchByNameThenTypeMatchingStrategy.class);
        BindingMatchingStrategy second = factory.createBindingMatchingStrategy(MatchByNameThenTypeMatchingStrategy.class);

        Assertions.assertEquals(2, factory.created);
        Assertions.assertSame(first, second);
        Assertions.assertThrows(IllegalArgumentException.class, () -> factory.createBindingMatchingStrategy(null));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> ObjectFactory.builder().build().createBindingMatchingStrategy(null));
    }
}
