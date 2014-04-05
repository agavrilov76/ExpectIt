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

import net.sf.expectit.MultiResult;
import net.sf.expectit.matcher.Matcher;

import static net.sf.expectit.matcher.Matchers.times;

/**
 * An element that corresponds to {@link net.sf.expectit.matcher.Matchers#times(int, net.sf.expectit.matcher.Matcher)}.
 */
public class TimesElement extends AbstractMatcherElement<MultiResult> {

    private int number;
    private AbstractMatcherElement<?> element;

    /**
     * Adds the child matcher element which will be matcher the number of times.
     *
     * @param element the matcher element
     */
    public void add(AbstractMatcherElement<?> element) {
        this.element = element;
    }

    @Override
    protected Matcher<MultiResult> createMatcher() {
        if (element == null) {
            throw new IllegalArgumentException("Child element is missing");
        }
        return times(number, element.createMatcher());
    }

    /**
     * Sets the number of time the child matcher element should match.
     *
     * @param number the number
     */
    public void setNumber(int number) {
        this.number = number;
    }

}
