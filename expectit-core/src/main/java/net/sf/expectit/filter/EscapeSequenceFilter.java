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

class EscapeSequenceFilter extends FilterAdapter {
    private String previousPart = "";

    private final char startingWith;
    private final String endingWith;
    private final Pattern pattern;

    EscapeSequenceFilter(
            final char startingWith,
            final String endingWith,
            final Pattern pattern) {
        this.startingWith = startingWith;
        this.endingWith = endingWith;
        this.pattern = pattern;
    }

    @Override
    protected String doBeforeAppend(final String string, final StringBuilder buffer) {
        final StringBuilder result = new StringBuilder(previousPart + string);

        int pos1 = 0;
        while (pos1 != -1) {
            pos1 = result.indexOf(String.valueOf(startingWith), pos1);
            if (pos1 != -1) {
                final int pos2 = result.indexOf(endingWith, pos1);
                if (pos2 != -1) {
                    previousPart = "";
                    final String substring = result.substring(pos1, pos2 + endingWith.length());
                    if (pattern.matcher(substring).matches()) {
                        result.delete(pos1, pos2 + endingWith.length());
                    } else {
                        pos1++;
                    }
                } else {
                    final String oldPreviousPart = previousPart;
                    previousPart = startingWith + result.substring(pos1 + 1);
                    result.delete(pos1, result.length()).insert(0, oldPreviousPart);
                    break;
                }
            }
        }

        return result.toString();
    }
}
