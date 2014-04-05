package net.sf.expectit.ant.filter;

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

/**
 * A base element for replace filters.
 */
public abstract class AbstractReplaceElement extends AbstractFilterElement {
    private String replacement;
    private String regexp;

    /**
     * Sets the replacement.
     *
     * @param replacement the replacement
     */
    public void setReplacement(String replacement) {
        this.replacement = replacement;
    }

    /**
     * Sets the regexp string.
     *
     * @param regexp the regexp string
     */
    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    String getReplacement() {
        if (replacement == null) {
            throw new IllegalArgumentException("replacement value is required");
        }
        return getProject().replaceProperties(replacement);
    }

    String getRegexp() {
        if (regexp == null) {
            throw new IllegalArgumentException("regexp value is required");
        }
        return getProject().replaceProperties(regexp);
    }
}
