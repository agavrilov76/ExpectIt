package net.sf.expectit;

import static net.sf.expectit.Utils.toDebugString;
import static org.junit.Assert.assertEquals;

import com.google.common.base.Charsets;
import java.util.Arrays;
import org.junit.Test;

public class UtilsTest {

    @Test
    public void testDebugString() {
        assertEquals(toDebugString(null), "null");
        assertEquals(toDebugString((Object) null), "null");
        assertEquals(toDebugString("ABC"), "ABC");
        final char[] chars = new char[1000];
        Arrays.fill(chars, 'x');
        final int length = toDebugString(String.valueOf(chars)).length();
        assertEquals(length, Utils.MAX_STRING_LENGTH + 22);
        assertEquals(toDebugString("ab\n\r"), "ab\\n\\r");
        final String utfString = toDebugString("abcdef".getBytes(), 4, Charsets.UTF_16);
        assertEquals(utfString.length(), 2);
        final String defaultStirng = toDebugString("abcdef".getBytes(), 3, null);
        assertEquals(defaultStirng, "abc");
    }

}