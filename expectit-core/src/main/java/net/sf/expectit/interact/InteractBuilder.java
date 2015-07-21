package net.sf.expectit.interact;

/*
 * #%L
 * ExpectIt
 * %%
 * Copyright (C) 2014 - 2015 Alexey Gavrilov and contributors
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;

/**
 * The interactive loop builder.
 */
public interface InteractBuilder {
    /**
     * Sets the matching predicate for the action condition.
     *
     * @param matcher the matcher.
     * @param <R> the result type.
     * @return the result builder.
     */
    <R extends Result> OngoingResult<R> when(Matcher<R> matcher);

    /**
     * Enters the interact loop and sets the matching predicate for the exit condition.
     *
     * @param matcher the matcher.
     * @param <R> the result type.
     * @return the result builder.
     * @throws IOException if I/O error occurs during matching.
     */
    <R extends Result> R until(Matcher<R> matcher) throws IOException;
}
