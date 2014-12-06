package net.sf.expectit;

import java.nio.charset.Charset;

/**
 * A class to hold static utility methods.
 */
final class Utils {

    private static final int MAX_STRING_LENGTH = 200;

    private Utils() {
    }

    static String toDebugString(final byte[] bytes, final int len, final Charset charset) {
        if (charset == null) {
            return toDebugString(new String(bytes, 0, len, Charset.defaultCharset()));
        }
        return toDebugString(new String(bytes, 0, len, charset));
    }

    static String toDebugString(final String string) {
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
        if (object == null) {
            return "null";
        }
        return toDebugString(object.toString());
    }
}
