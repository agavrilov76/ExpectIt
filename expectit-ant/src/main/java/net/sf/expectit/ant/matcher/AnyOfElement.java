package net.sf.expectit.ant.matcher;

/*
 * #%L
 * net.sf.expectit
 * %%
 * Copyright (C) 2014 Alexey Gavrilov
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

import net.sf.expectit.MultiResult;
import net.sf.expectit.matcher.Matcher;

import static net.sf.expectit.matcher.Matchers.anyOf;

/**
 * An element that corresponds to {@link net.sf.expectit.matcher.Matchers#anyOf(net.sf.expectit.matcher.Matcher[])}.
 */
public class AnyOfElement extends AbstractMultiMatcherElement {

    @Override
    protected Matcher<MultiResult> createMatcher() {
        return anyOf(getMatchers());
    }


}
