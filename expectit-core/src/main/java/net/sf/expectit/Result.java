package net.sf.expectit;

/*
 * #%L
 * ExpectIt
 * %%
 * Copyright (C) 2014 Alexey Gavrilov and contributors
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

import java.util.regex.MatchResult;

/**
 * A representation of the result of a single match operation.
 * <p/>
 * The interface contains methods to query the results of the match against an expression.
 */
public interface Result extends MatchResult {
    /**
     * Gets latest input string passed to the matcher before it returns.
     * @return the input string.
     */
    String getInput();
    /**
     * Indicates whether the match was successful or not.
     *
     * @return {@code true} if the match succeeded, or {@code false} otherwise.
     */
    boolean isSuccessful();

    /**
     * Returns a part of the input string from the beginning until the starting position of the
     * match.
     * Never returns {@code null}.
     *
     * @return the string before the match
     * @throws java.lang.IllegalStateException if the match operation failed
     */
    String getBefore();
}
