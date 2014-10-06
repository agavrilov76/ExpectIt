package net.sf.expectit.matcher;

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

import static net.sf.expectit.matcher.Matchers.generateToString;

import java.util.regex.Pattern;
import net.sf.expectit.Result;

/**
 * A result type that represents the match of a regular expression based on {@link java.util
 * .regex.Matcher}.
 *
 * @author Alexey Gavrilov
 */
class RegexpMatcher implements Matcher<Result> {
    private final Pattern pattern;
    private final boolean useFind;

    /**
     * The constructor.
     *
     * @param pattern the pattern
     * @param useFind when <tt>true</tt> the {@link java.util.regex.Matcher#find()} method is
     *                used to create a result,
     *                otherwise the {@link java.util.regex.Matcher#matches()} method is used.
     */
    public RegexpMatcher(Pattern pattern, boolean useFind) {
        this.pattern = pattern;
        this.useFind = useFind;
    }

    @Override
    public Result matches(String input, boolean isEof) {
        java.util.regex.Matcher matcher = pattern.matcher(input);
        boolean result;
        if (useFind) {
            result = matcher.find();
        } else {
            result = matcher.matches();
        }
        if (result) {
            return new RegexpResult(true, input, input.substring(0, matcher.start()), matcher);
        } else {
            return SimpleResult.failure(input);
        }
    }

    @Override
    public String toString() {
        if (useFind) {
            return generateToString("regexp", pattern);
        }
        return generateToString("matches", pattern);
    }
}
