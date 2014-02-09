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

import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;

import static net.sf.expectit.matcher.Matchers.contains;

/**
 * An element that corresponds to {@link net.sf.expectit.matcher.Matchers#contains(String)}.
 */
public class ContainsElement extends AbstractMatcherElement<Result> {
    private String string;

    @Override
    protected Matcher<Result> createMatcher() {
        if (string == null) {
            throw new IllegalArgumentException("string value is required");
        }
        return contains(getProject().replaceProperties(string));
    }

    /**
     * Sets the string for the matcher.
     *
     * @param string the string
     */
    public void setString(String string) {
        this.string = string;
    }

}
