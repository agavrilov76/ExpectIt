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

    private static final String COLORS_REGEXP_STRING = "\\x1b\\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]";
    /**
     * The regular expression which matches <a href="http://en.wikipedia
     * .org/wiki/ANSI_escape_code#Colors">ANSI escape
     * sequences for colors</a>.
     */
    public static final Pattern COLORS_PATTERN = Pattern.compile(COLORS_REGEXP_STRING);

    /**
     * The regular expression which matches non printable characters: {@code
     * [\x00\x08\x0B\x0C\x0E-\x1F]}.
     */
    public static final Pattern NON_PRINTABLE_PATTERN = Pattern.compile(
            "[\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]");

    private Filters() {
    }

    /**
     * Creates a filter which removes all the non-printable characters matching {@link
     * #NON_PRINTABLE_PATTERN} in the
     * input string.
     *
     * @return the filter
     */
    public static Filter removeNonPrintable() {
        return replaceInString(NON_PRINTABLE_PATTERN, "");
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
     * Creates a filter which removes <a href="http://en.wikipedia
     * .org/wiki/ANSI_escape_code#Colors">ANSI
     * escape sequences for colors</a> in the input.
     *
     * @return the filter
     */
    public static Filter removeColors() {
        return replaceInString(COLORS_PATTERN, "");
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
        return replaceInBuffer(Pattern.compile(regexp), replacement);
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
        return new FilterAdapter() {
            @Override
            protected boolean doAfterAppend(StringBuilder buffer) {
                Matcher matcher = regexp.matcher(buffer);
                String str = matcher.replaceAll(replacement);
                buffer.replace(0, buffer.length(), str);
                return false;
            }
        };
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
