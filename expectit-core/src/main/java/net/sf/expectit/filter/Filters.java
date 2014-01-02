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
     * Creates a composite filter which applies the given filters one by one. The sequence stops when the
     * {@link Filter#filter(String, StringBuilder)} returns {@code null}. Then the latest non-null result is returned.
     *
     * @param filters the filter chain
     * @return the composite filter
     */
    public static Filter chain(final Filter... filters) {
        return new Filter() {
            @Override
            public String filter(String string, StringBuilder buffer) {
                String previousResult = null;
                for (Filter filter : filters) {
                    string = filter.filter(string, buffer);
                    if (string == null) {
                        return previousResult;
                    }
                    previousResult = string;
                }
                return string;
            }
        };
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
