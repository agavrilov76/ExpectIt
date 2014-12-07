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