/*
 * This software is licensed under the Apache 2 license, quoted below.
 *
 * Copyright (c) 1999-2024, Algorithmx Inc.
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

package org.rulii.trace;

import org.rulii.lang.Runnable;
import org.rulii.lib.spring.util.Assert;

import java.util.LinkedList;
import java.util.List;

public class DefaultEventProcessor implements EventProcessor {

    private final List<ExecutionListener> listeners = new LinkedList<>();
    private boolean eventsEnabled = true;

    public DefaultEventProcessor() {
        super();
    }

    public boolean isEventsEnabled() {
        return eventsEnabled;
    }

    public void setEventsEnabled(boolean eventsEnabled) {
        this.eventsEnabled = eventsEnabled;
    }

    public synchronized void addEventListener(ExecutionListener listener) {
        this.listeners.add(listener);
    }

    public synchronized void removeEventListener(ExecutionListener listener) {
        this.listeners.remove(listener);
    }

    public synchronized <T extends Runnable<?>> void fireListeners(ExecutionEvent<T> event) {
        Assert.notNull(event, "event cannot be null.");
        // Events turned off
        if (!isEventsEnabled()) return;
        // Fire all the listeners
        //listeners.forEach(l -> l.onEvent(event));
    }

}
