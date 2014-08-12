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

import net.sf.expectit.filter.Filter;
import org.junit.Test;

import java.util.regex.Pattern;

import static net.sf.expectit.filter.Filters.*;
import static org.junit.Assert.*;

/**
 * Filter tests.
 */
public class FilterTest {

    @Test
    public void testNonPrintable() {
        assertTrue(removeNonPrintable().beforeAppend("\u0000\u0008", new StringBuilder()).isEmpty());
    }

    @Test
    public void testNoColors() {
        String str = "abc\u001b[31m\u001B[7mdef\u001B[01;31m\u001B[KX\u001B[m\u001B[K";
        StringBuilder buffer = new StringBuilder(str);
        assertFalse(removeColors().afterAppend(buffer));
        assertEquals(buffer.toString(), str);
        assertEquals("abcdefX", removeColors().beforeAppend(str, new StringBuilder()));
    }

    @Test
    public void testReplaceInString() {
        Filter filter = replaceInString("Z(.)f", "X$1x");
        assertEquals(filter.beforeAppend("aaZdfhhh", null), "aaXdxhhh");
        filter = replaceInString(Pattern.compile("[a]"), "");
        assertEquals(filter.beforeAppend("aad", null), "d");
    }

    @Test
    public void testReplaceInBuffer() {
        Filter filter = replaceInBuffer("ab(.)d(.)f", "3$2$1E");
        StringBuilder b = new StringBuilder("dabedzf");
        filter.afterAppend(b);
        assertEquals(b.toString(), "d3zeE");
        StringBuilder b2 = new StringBuilder();
        filter.afterAppend(b2);
        assertTrue(b2.length() == 0);

        b2.append("12345");
        filter.afterAppend(b2);
        assertEquals(b2.toString(), "12345");
        Filter filter2 = replaceInBuffer(Pattern.compile("12.4"), "");
        filter2.beforeAppend("", b2);
        assertEquals(b2.toString(), "12345");

        filter2.afterAppend(b2);
        assertEquals(b2.toString(), "5");
    }

    @Test
    public void testFilterSwitcher() {
        Filter filter = replaceInString("a", "b");
        filter.setEnabled(false);
        assertEquals(filter.beforeAppend("abcd", null), "abcd");

        Filter filter2 = replaceInBuffer("_", "!");
        filter2.setEnabled(false);
        StringBuilder abb = new StringBuilder("_abb");
        assertFalse(filter2.afterAppend(abb));
        assertEquals(abb.toString(), "_abb");

        assertFalse(filter.isEnabled());
        filter.setEnabled(true);
        filter2.setEnabled(true);
        assertTrue(filter.isEnabled());

        Filter chain = chain(filter, filter2);
        assertEquals(chain.beforeAppend("abcd", null), "bbcd");
        abb = new StringBuilder("_abb");
        assertFalse(chain.afterAppend(abb));
        assertEquals(abb.toString(), "!abb");

        chain.setEnabled(false);
        assertEquals(chain.beforeAppend("abcd", null), "abcd");

    }
}
