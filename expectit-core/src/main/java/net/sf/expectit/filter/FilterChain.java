package net.sf.expectit.filter;

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
 * The factory for filter chains.
 *
 * @author Alexey Gavrilov
 */
public final class FilterChain {

    private FilterChain() {
    }

    /**
     * Combines the filters in a filter chain.
     * <p/>
     * The given filters are applied one by one in the order that hey appear in the method
     * argument list.
     * <p/>
     * The string returns by the
     * {@link Filter#beforeAppend(String, StringBuilder)} method of one filter is passed a
     * parameter to the next one if it is not {@code null}. If it is {@code null},
     * then the {@code beforeAppend}
     * won't be called any more and the latest non-null result is appended to the expect internal
     * buffer.
     * <p/>
     * If the return value of the {@link Filter#afterAppend(StringBuilder)} method is true,
     * then all the calls
     * of this method on the consequent filters will be suppressed.
     *
     * @param filters the filters, not {@code null}
     * @return the combined filter
     */
    public static Filter chain(final Filter... filters) {
        return new FilterAdapter() {
            @Override
            protected String doBeforeAppend(String string, StringBuilder buffer) {
                String previousResult = null;
                for (Filter filter : filters) {
                    string = filter.beforeAppend(string, buffer);
                    if (string == null) {
                        return previousResult;
                    }
                    previousResult = string;
                }
                return string;
            }

            @Override
            protected boolean doAfterAppend(StringBuilder buffer) {
                for (Filter filter : filters) {
                    if (filter.afterAppend(buffer)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
