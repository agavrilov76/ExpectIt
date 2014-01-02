package net.sf.expectit;

/*
 * #%L
 * net.sf.expectit
 * %%
 * Copyright (C) 2014 Alexey Gavrilov
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

import net.sf.expectit.matcher.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static net.sf.expectit.matcher.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for basic single operation matchers.
 *
 * @author Alexey Gavrilov
 */
public class MatcherTest {
    private SingleInput input;
    private ExecutorService executor;

    /**
     * Creates a mock input stream which send some data every 100 ms.
     */
    @Before
    public void setup() throws IOException {
        InputStream mock = mock(InputStream.class);
        input = new SingleInput(mock, Charset.defaultCharset(), null, null);
        executor = Executors.newSingleThreadExecutor();
        input.start(executor);
        // overriding read(byte[]) method since it is used in the SingleInput
        when(mock.read(any(byte[].class))).then(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Thread.sleep(100);
                byte[] bytes = "a1b2c3_".getBytes();
                //noinspection SuspiciousSystemArraycopy
                System.arraycopy(bytes, 0, invocation.getArguments()[0], 0, bytes.length);
                return bytes.length;
            }
        });
    }

    @After
    public void cleanup() {
        input.stop();
        executor.shutdown();
    }

    /**
     * Basic expect string tests
     */
    @Test
    public void testExpectString() throws IOException {
        Result result = input.expect(1000, contains("b2"));
        assertTrue(result.isSuccessful());
        assertEquals(result.groupCount(), 0);
        assertEquals(result.end(), 4);
        assertEquals(result.start(), 2);
        assertEquals(result.group(), "b2");
        assertEquals(result.end(0), 4);
        assertEquals(result.start(0), 2);
        assertEquals(result.group(0), "b2");
        assertEquals(result.getBefore(), "a1");
        checkIndexOutOfBound(result, 1);
        StringBuilder buffer = input.getBuffer();
        result = input.expect(200, contains("cx"));
        // no changes in the buffer if matching fails
        assertEquals(buffer.length(), input.getBuffer().length());
        assertFalse(result.isSuccessful());
        checkIndexOutOfBound(result, 1);
        checkIllegalState(result);
        checkIllegalState(result, 0);
        assertEquals(result.groupCount(), 0);
        // expecting that mocking input stream goes circles
        assertTrue(input.expect(1000, contains("c3_a")).isSuccessful());
        buffer = input.getBuffer();
        // check that buffer gets updated
        assertTrue(buffer.toString().startsWith("1b2c3"));
    }

    /**
     * Check for null, empty and invalid inputs.
     */
    @Test
    public void testEmptyStringAndNull() throws IOException {
        Result result = input.expect(1000, contains(""));
        assertTrue(result.isSuccessful());
        try {
            input.expect(1000, contains(null));
            fail();
        } catch (NullPointerException ok) {
        }
        result = input.expect(1000, regexp(""));
        assertTrue(result.isSuccessful());
        result = input.expect(1000, matches(""));
        assertTrue(result.isSuccessful());
        try {
            input.expect(1000, regexp((String) null));
            fail();
        } catch (NullPointerException ok) {
        }
        try {
            input.expect(1000, regexp((Pattern) null));
            fail();
        } catch (NullPointerException ok) {
        }
        try {
            input.expect(1000, regexp("^(^^^"));
            fail();
        } catch (PatternSyntaxException ok) {
        }
    }

    /**
     * Tests for regexp matcher
     */
    @Test
    public void testExpectRegexp() throws IOException {
        Result result = input.expect(1000, regexp(".b.c"));
        assertTrue(result.isSuccessful());
        assertEquals(result.getBefore(), "a");
        assertEquals(result.groupCount(), 0);
        assertEquals(result.end(), 5);
        assertEquals(result.start(), 1);
        assertEquals(result.group(), "1b2c");
        assertEquals(result.end(0), 5);
        assertEquals(result.start(0), 1);
        assertEquals(result.group(0), "1b2c");
        checkIndexOutOfBound(result, 1);
        assertTrue(input.getBuffer().toString().startsWith("3_"));
        result = input.expect(100, regexp("a.z.c"));
        assertFalse(result.isSuccessful());
        checkIllegalState(result);
        result = input.expect(1000, regexp("3_([^_]*)c3"));
        assertEquals(result.group(0), "3_a1b2c3");
        assertEquals(result.group(1), "a1b2");
        result = input.expect(1000, regexp("a(.)b(.)c(.)"));
        assertEquals(result.group(1), "1");
        assertEquals(result.group(3), "3");
        assertEquals(result.groupCount(), 3);
        assertEquals(result.end(), 7);
        // sanity check for Pattern instance
        result = input.expect(1000, regexp(Pattern.compile("a(.*)")));
        assertTrue(result.isSuccessful());
    }

    /**
     * Tests for exact match.
     */
    @Test
    public void testExpectMatch() throws IOException {
        Result result = input.expect(1000, matches(".*a1b2.*"));
        assertTrue(result.isSuccessful());
        assertEquals(result.getBefore(), "");
        checkIndexOutOfBound(result, 1);
        assertEquals(result.groupCount(), 0);
        assertEquals(input.getBuffer().length(), 0);
        //await for some data
        input.expect(1000, contains("_a"));
        assertTrue(input.expect(1000, contains("_a")).isSuccessful());
        result = input.expect(200, matches("a(.)b(.)c(.)"));
        assertFalse(result.isSuccessful());
        checkIllegalState(result);
        checkIndexOutOfBound(result, 1);
        String buffer = input.getBuffer().toString();
        assertTrue(buffer, buffer.startsWith("1b2c3_"));
        result = input.expect(1000, matches("(.*)c(.).*"));
        assertEquals(result.start(), 0);
        assertEquals(result.end(), buffer.length());
        assertEquals(result.group(), buffer);
        assertEquals(result.getBefore(), "");
        assertEquals(result.groupCount(), 2);
        assertEquals(result.group(2), "3");
        checkIndexOutOfBound(result, 3);
        // sanity check for Pattern instance
        result = input.expect(1000, matches(Pattern.compile("^a(.*)")));
        assertTrue(result.isSuccessful());
        assertEquals(result.group(1), "1b2c3_");
    }

    /**
     * Tests for the 'allOf' matcher
     */
    @Test
    public void testAllOf() throws IOException {
        MultiResult result = input.expect(1000, allOf(contains("b")));
        assertTrue(result.isSuccessful());
        assertEquals(result.start(), 2);
        assertEquals(result.end(), 3);

        result = input.expect(1000, allOf(contains("a"), contains("b"), regexp("a1")));
        assertTrue(result.isSuccessful());
        assertEquals(result.start(), 6);
        assertEquals(result.end(), 7);
        for (Result r : result.getResults()) {
            assertTrue(r.isSuccessful());
        }
        assertEquals(result.getResults().size(), 3);
        assertEquals(result.getResults().get(1).start(), 6);
        assertEquals(result.getResults().get(1).end(), 7);
        assertEquals(result.getResults().get(2).start(), 4);
        assertEquals(result.getResults().get(2).end(), 6);

        // one negative
        result = input.expect(200, allOf(contains("a"), contains("b"), regexp("XXX")));
        assertFalse(result.isSuccessful());
        assertEquals(result.getResults().size(), 3);
        assertEquals(result.getResults().get(0).start(), 4);
        assertEquals(result.getResults().get(0).end(), 5);
        assertTrue(result.getResults().get(1).isSuccessful());
        assertFalse(result.getResults().get(2).isSuccessful());

        // all negative
        result = input.expect(200, allOf(matches("vv"), regexp("XXX")));
        for (Result r : result.getResults()) {
            assertFalse(r.isSuccessful());
        }

        // same end position: the first wins
        result = input.expect(1000, allOf(contains("a"), contains("b"), regexp("a(.*)")));
        assertTrue(result.isSuccessful());
        assertEquals(result.groupCount(), 1);

        try {
            input.expect(1000, allOf());
            fail();
        } catch (IllegalArgumentException ok) {
        }
        try {
            input.expect(1000, allOf((Matcher<?>[]) null));
            fail();
        } catch (NullPointerException ok) {
        }
    }

    @Test
    public void testAnyOf() throws IOException {
        try {
            input.expect(1000, anyOf());
            fail();
        } catch (IllegalArgumentException ok) {
        }
        try {
            input.expect(1000, anyOf((Matcher<?>[]) null));
            fail();
        } catch (NullPointerException ok) {
        }

        MultiResult result = input.expect(1000, anyOf(contains("b")));
        assertTrue(result.isSuccessful());
        assertEquals(result.start(), 2);
        assertEquals(result.end(), 3);
        result = input.expect(1000, anyOf(contains("x"), contains("b"), contains("zzz")));
        assertTrue(result.isSuccessful());
        assertEquals(result.start(), 6);
        assertEquals(result.end(), 7);
        assertEquals(result.group(), "b");
        result = input.expect(200, anyOf(contains("ttt"), contains("zzz")));
        assertFalse(result.isSuccessful());

        // test for combination of matchers
        result = input.expect(1000, anyOf(allOf(contains("1"), contains("2")), contains("FFFF")));
        assertTrue(result.isSuccessful());
        assertTrue(result.getResults().get(0) instanceof MultiResult);
        assertEquals(result.getResults().size(), 2);
    }

    @Test
    public void testToString() {
        Matcher<Result> c = contains("xyz");
        assertEquals(c.toString(), "contains('xyz')");
        Matcher<Result> r = regexp("xyz");
        assertEquals(r.toString(), "regexp('xyz')");
        Matcher<Result> m = matches("xyz");
        assertEquals(m.toString(), "matches('xyz')");
        assertEquals(anyOf(c, r, m).toString(), "anyOf(contains('xyz'),regexp('xyz'),matches('xyz'))");
        assertEquals(allOf(c).toString(), "allOf(contains('xyz'))");
    }

    /**
     * Verifies that all the result methods throw IllegalStateException.
     */
    private static void checkIllegalState(Result result) {
        try {
            result.end();
            fail();
        } catch (IllegalStateException ignore) {
        }
        try {
            result.getBefore();
            fail();
        } catch (IllegalStateException ignore) {
        }
        try {
            result.group();
            fail();
        } catch (IllegalStateException ignore) {
        }
        try {
            result.start();
            fail();
        } catch (IllegalStateException ignore) {
        }
    }

    /**
     * Verifies that all the result methods that take the index throws IndexOutOfBoundException for the given index.
     */
    private static void checkIndexOutOfBound(Result result, int index) {
        try {
            result.end(index);
            fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
        try {
            result.group(index);
            fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
        try {
            result.start(index);
            fail();
        } catch (IndexOutOfBoundsException ignore) {
        }
    }

    /**
     * Verifies that all the result methods throw IllegalStateException.
     */
    private static void checkIllegalState(Result result, int index) {
        try {
            result.end(index);
            fail();
        } catch (IllegalStateException ignore) {
        }
        try {
            result.group(index);
            fail();
        } catch (IllegalStateException ignore) {
        }
        try {
            result.start(index);
            fail();
        } catch (IllegalStateException ignore) {
        }
    }
}
