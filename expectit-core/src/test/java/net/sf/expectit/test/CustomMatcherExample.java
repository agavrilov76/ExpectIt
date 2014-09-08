package net.sf.expectit.test;

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

import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.SimpleResult;

/**
 * An example of a custom matcher in a third-party package.
 */
public class CustomMatcherExample {
    public static class ExactMatcher implements Matcher<Result> {
        private final String exact;

        public ExactMatcher(final String exact) {
            this.exact = exact;
        }

        @Override
        public Result matches(final String input, final boolean isEof) {
            return SimpleResult.valueOf(input.equals(exact), "", input);
        }
    }

    public static void main(String[] args) {
        new ExactMatcher("thing");
    }
}
