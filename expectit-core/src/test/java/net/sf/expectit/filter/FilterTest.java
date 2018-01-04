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

import static net.sf.expectit.TestUtils.mockInputStream;
import static net.sf.expectit.filter.FilterChain.chain;
import static net.sf.expectit.filter.Filters.replaceInBuffer;
import static net.sf.expectit.filter.Filters.replaceInString;
import static net.sf.expectit.matcher.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.regex.Pattern;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import org.junit.Test;

/**
 * Filter tests.
 */
public class FilterTest {

    @Test
    public void testNonPrintable() throws Exception {
        Expect expect = new ExpectBuilder()
                .withInputs(mockInputStream("\u0000\u0008x").getStream())
                .withInputFilters(BuiltinFilters.removeNonPrintable())
                .build();
        assertEquals(expect.expect(contains("x")).getBefore(), "");
        expect.close();
    }

    @Test
    public void testNoColors() throws Exception {
        String str = "abc\u001b[31m\u001B[7mdef\u001B[01;31m\u001B[KX\u001B[m\u001B[K";
        Expect expect = new ExpectBuilder()
                .withInputs(mockInputStream(str + "x").getStream())
                .withInputFilters(BuiltinFilters.removeColors())
                .build();
        assertEquals(expect.expect(contains("x")).getBefore(), "abcdefX");
        expect.close();
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
    public void testReplaceInBufferWithOverlap() {
        final Filter filter = replaceInBuffer(Pattern.compile("xy"), "", 2);
        StringBuilder b = new StringBuilder("abx");
        filter.beforeAppend("abx", b);
        filter.afterAppend(b);

        StringBuilder b2 = new StringBuilder("abxyc");
        filter.beforeAppend("yc", b2);
        filter.afterAppend(b2);

        assertEquals(b2.toString(), "abc");
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
