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

import java.util.regex.Pattern;

/**
 * The factory for built-in filters.
 *
 * @author Alexey Gavrilov
 */
public final class BuiltinFilters {

    /**
     * The regular expression and pattern which matches
     * <a href="http://en.wikipedia.org/wiki/ANSI_escape_code#Colors">ANSI escape
     * sequences for colors</a>.
     */
    private static final String COLORS_REGEXP_STRING = "\\e\\[([0-9]{1,2}(;[0-9]{1,2})?)?[m|K]";
    public static final Pattern COLORS_PATTERN = Pattern.compile(COLORS_REGEXP_STRING);

    /**
     * This regular expression and patter that match VT100 sequences.
     * <p>
     * This will also match ANSI colors.<br>
     * See {@link BuiltinFilters#COLORS_PATTERN}
     */
    private static final String VT100_REGEXP_STRING = "" +
            "\\e\\[(\\d+;)*(\\d+)?[ABCDHJKLMfmnr]|" +
            "\\e\\[\\?\\d*[hlm]|" +
            "\\e[78EDHM=]";
    public static final Pattern VT100_PATTERN = Pattern.compile(VT100_REGEXP_STRING);

    /**
     * The regular expression which matches non printable characters: {@code
     * [\x00\x08\x0B\x0C\x0E-\x1F]}.
     */
    public static final Pattern NON_PRINTABLE_PATTERN = Pattern.compile(
            "[\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]");

    private BuiltinFilters() {
    }

    /**
     * Creates a filter which removes
     * <a href="http://en.wikipedia.org/wiki/ANSI_escape_code#Colors">ANSI
     * escape sequences for colors</a> in the input.
     *
     * @return the filter
     */
    public static Filter removeColors() {
        return Filters.replaceInBuffer(COLORS_PATTERN, "");
    }

    /**
     * Creates a filter which removes all VT100 control sequences in the input.
     * <p>
     * This will also remove ANSI colors.<br>
     * See {@link BuiltinFilters#removeColors()}
     *
     * @return the filter
     */
    public static Filter removeVt100Sequences() {
        return Filters.replaceInBuffer(VT100_PATTERN, "");
    }

    /**
     * Creates a filter which removes all the non-printable characters matching {@link
     * #NON_PRINTABLE_PATTERN} in the
     * input string.
     *
     * @return the filter
     */
    public static Filter removeNonPrintable() {
        return Filters.replaceInString(NON_PRINTABLE_PATTERN, "");
    }

}
