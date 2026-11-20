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
package org.rulii.script.graaljs;

/**
 * What the context pool of a {@link GraalJsScriptProcessorFactory} has done so far: counts since the factory was
 * created, and the number of contexts idle at the moment of the call.
 *
 * @param created   contexts created.
 * @param reused    evaluations that found a context ready instead of creating one.
 * @param retired   contexts closed because they reached their age, their number of uses or their idle time, because
 *                  enough were idle already, or because {@code discardContexts()} or {@code close()} was called.
 * @param cleaned   times the globals a script left behind were removed.
 * @param tarnished contexts closed because what a script left behind could not be removed.
 * @param failed    contexts closed because the script failed.
 * @param detached  contexts handed over to a result that is still a JavaScript object and never used again.
 * @param idle      contexts waiting for an evaluation right now.
 *
 * @author Max Arulananthan
 * @since 2.3
 * @see GraalJsScriptProcessorFactory#getPoolStatistics()
 */
public record GraalJsPoolStatistics(long created, long reused, long retired, long cleaned, long tarnished, long failed,
                                    long detached, int idle) {
}
