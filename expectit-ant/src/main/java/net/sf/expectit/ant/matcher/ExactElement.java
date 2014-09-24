package net.sf.expectit.ant.matcher;

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

import static net.sf.expectit.matcher.Matchers.exact;

import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;

/**
 * An element that corresponds to {@link net.sf.expectit.matcher.Matchers#exact(String)}.
 */
public class ExactElement extends AbstractStringMatcherElement {

    @Override
    protected Matcher<Result> getResultStringMatcher(final String string) {
        return exact(getProject().replaceProperties(string));
    }
}
