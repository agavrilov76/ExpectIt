package net.sf.expectit;

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

import java.io.Flushable;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * A class to hold static utility methods.
 */
final class Utils {

    static final int MAX_STRING_LENGTH = 200;

    private Utils() {
    }

    static String toDebugString(final byte[] bytes, final int len, final Charset charset) {
        if (charset == null) {
            return toDebugString(new String(bytes, 0, len, Charset.defaultCharset()));
        }
        return toDebugString(new String(bytes, 0, len, charset));
    }

    static String toDebugString(final String string) {
        if (string == null) {
            return "null";
        }
        final StringBuilder builder = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            final char c = string.charAt(i);
            switch (c) {
                case '\n': builder.append("\\n"); break;
                case '\r': builder.append("\\r"); break;
                default:
                    builder.append(c);
            }
            if (i > MAX_STRING_LENGTH) {
                builder.append(" ... (");
                builder.append(string.length() - i);
                builder.append(" char more)");
                break;
            }
        }
        return builder.toString();
    }

    static String toDebugString(final Object object) {
        return toDebugString(String.valueOf(object));
    }

    static void flushAppendable(final Appendable appendable) throws IOException {
        if (appendable instanceof Flushable) {
            ((Flushable) appendable).flush();
        }
    }
}
