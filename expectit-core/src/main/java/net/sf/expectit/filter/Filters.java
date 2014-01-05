package net.sf.expectit.filter;

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

/**
 * Filter factory.
 *
 * @author Alexey Gavrilov
 */
public final class Filters {

    private Filters() {
    }

    /**
     * Removes non-printable characters. Check the method source code for details.
     *
     * @return the filter
     */
    public static Filter printableOnly() {
        return new Filter() {
            @Override
            public String filter(String string, StringBuilder result) {
                return string.replaceAll("[\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
            }
        };
    }

    /**
     * Removes <a href="http://en.wikipedia.org/wiki/ANSI_escape_code#Colors">ANSI escape sequences for colors</a>.
     *
     * @return the filter
     */
    public static Filter removeColors() {
        return new Filter() {
            @Override
            public String filter(String string, StringBuilder result) {
                return string.replaceAll("\\x1b[^m]*m", "");
            }
        };
    }
}
