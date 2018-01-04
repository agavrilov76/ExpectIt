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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The filter factory.
 *
 * @author Alexey Gavrilov
 */
public final class Filters {

    private static final int DEFAULT_FILTER_OVERLAP = 80;

    private Filters() {
    }

    /**
     * Creates a filter which replaces every substring in the input string that matches the given
     * regular expression
     * and replaces it with given replacement.
     * <p/>
     * The method just calls {@link String#replaceAll(String, String)} for the input string.
     *
     * @param regexp      the regular expression
     * @param replacement the string to be substituted for each match
     * @return the filter
     */
    public static Filter replaceInString(final Pattern regexp, final String replacement) {
        return new FilterAdapter() {
            @Override
            protected String doBeforeAppend(String string, StringBuilder buffer) {
                return regexp.matcher(string).replaceAll(replacement);
            }
        };
    }

    /**
     * Equivalent to {@link #replaceInString(java.util.regex.Pattern,
     * String)} but takes the regular expression
     * as string.
     *
     * @param regexp      the regular expression
     * @param replacement the string to be substituted for each match
     * @return the filter
     */
    public static Filter replaceInString(final String regexp, final String replacement) {
        return replaceInString(Pattern.compile(regexp), replacement);
    }

    /**
     * Equivalent to {@link #replaceInBuffer(java.util.regex.Pattern,
     * String)} but takes the regular expression
     * as string.
     *
     * @param regexp      the regular expression
     * @param replacement the string to be substituted for each match
     * @return the filter
     */
    public static Filter replaceInBuffer(final String regexp, final String replacement) {
        return replaceInBuffer(Pattern.compile(regexp), replacement, DEFAULT_FILTER_OVERLAP);
    }

    /**
     * Creates a filter which replaces every substring in the input buffer that matches the given
     * regular expression
     * and replaces it with given replacement.
     * <p/>
     * The method just calls {@link String#replaceAll(String, String)} for the entire buffer
     * contents every time new
     * data arrives,
     *
     * @param regexp      the regular expression
     * @param replacement the string to be substituted for each match
     * @param overlap     the number of characters prepended to the matching string from the
     *                    previous data chunk before match
     * @return the filter
     */
    static Filter replaceInBuffer(
            final Pattern regexp,
            final String replacement,
            final int overlap) {
        return new FilterAdapter() {
            private String string;

            @Override
            protected String doBeforeAppend(String string, StringBuilder buffer) {
                this.string = string;
                return string;
            }

            @Override
            protected boolean doAfterAppend(StringBuilder buffer) {
                int pos = string == null
                        ? 0
                        : Math.max(0, buffer.length() - string.length() - overlap);

                final Matcher matcher = regexp.matcher(buffer.substring(pos));
                final String str = matcher.replaceAll(replacement);
                buffer.replace(pos, buffer.length(), str);

                return false;
            }
        };
    }

    /**
     * Creates a filter which replaces every substring in the input buffer that matches the given
     * regular expression
     * and replaces it with given replacement.
     * <p/>
     * The method just calls {@link String#replaceAll(String, String)} for the entire buffer
     * contents every time new
     * data arrives,
     *
     * @param regexp      the regular expression
     * @param replacement the string to be substituted for each match
     * @return the filter
     */
    public static Filter replaceInBuffer(final Pattern regexp, final String replacement) {
        return replaceInBuffer(regexp, replacement, DEFAULT_FILTER_OVERLAP);
    }
}
