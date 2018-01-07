package net.sf.expectit.interact;

/*
 * #%L
 * ExpectIt
 * %%
 * Copyright (C) 2014 - 2015 Alexey Gavrilov and contributors
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

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static net.sf.expectit.TestUtils.SMALL_TIMEOUT;
import static net.sf.expectit.TestUtils.mockInputStream;
import static net.sf.expectit.matcher.Matchers.allOf;
import static net.sf.expectit.matcher.Matchers.anyOf;
import static net.sf.expectit.matcher.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import net.sf.expectit.MockInputStream;
import net.sf.expectit.MultiResult;
import net.sf.expectit.Result;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

public class InteractTest {

    private Expect expect;

    @After
    public void cleanup() throws IOException {
        if (expect != null) {
            expect.close();
        }
    }

    @Test
    public void simpleTest() throws Exception {
        final MockInputStream input = mockInputStream("AaBbAaBbcdefF");
        expect = new ExpectBuilder()
                .withExceptionOnFailure()
                .withTimeout(SMALL_TIMEOUT, TimeUnit.MILLISECONDS)
                .withInputs(input.getStream())
                .build();
        input.waitUntilReady();
        final Action<Result> action1 = mock(Action.class);
        final Action<MultiResult> action2 = mock(Action.class);
        final Action<Result> action3 = mock(Action.class);
        final ArgumentCaptor<Result> result1 = ArgumentCaptor.forClass(Result.class);
        final ArgumentCaptor<MultiResult> result2 = ArgumentCaptor.forClass(MultiResult.class);
        final ArgumentCaptor<Result> result3 = ArgumentCaptor.forClass(Result.class);
        doNothing().when(action1).apply(result1.capture());
        doNothing().when(action2).apply(result2.capture());
        doNothing().when(action3).apply(result3.capture());
        final Result result = expect.interact()
                .when(contains("a")).then(action1)
                .when(allOf(contains("b"))).then(action2)
                .when(contains("XXX")).then(action3)
                .until(contains("ef"));
        assertEquals(result.getBefore(), "cd");
        assertEquals(result1.getValue().getBefore(), "A");
        assertEquals(result2.getValue().getBefore(), "AaB");
        verify(action1, Mockito.times(2)).apply(any(Result.class));
        verify(action2, Mockito.times(2)).apply(any(MultiResult.class));
        verifyNoMoreInteractions(action3);
    }

    @Test (timeout = 5000)
    public void negativeTest() throws Exception {
        final MockInputStream input = mockInputStream("abcd");
        expect = new ExpectBuilder()
                .withInfiniteTimeout()
                .withInputs(input.getStream())
                .build();
        input.waitUntilReady();
        final Action<Result> action1 = mock(Action.class);
        final Result result = expect
                .withTimeout(SMALL_TIMEOUT, TimeUnit.MILLISECONDS)
                .interact()
                .when(contains("X")).then(action1)
                .until(contains("Y"));
        assertFalse(result.isSuccessful());
        assertEquals(result.getInput(), "abcd");
        verifyNoMoreInteractions(action1);
    }

    @Test
    public void testException() throws Exception {
        expect = new ExpectBuilder()
                .withInputs(mock(InputStream.class))
                .build();
        try {
            expect.interactWith(1);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {
        }
        try {
            expect.interactWith(-1);
            Assert.fail();
        } catch (IllegalArgumentException ignore) {
        }
        try {
            expect.interact().when(contains("X")).then(null);
            fail();
        } catch (NullPointerException ignore) {
        }
    }

    @Test
    public void testCornerCases() throws Exception {
        final MockInputStream input = mockInputStream("xyz");
        final MockInputStream input2 = mockInputStream("abcd");
        expect = new ExpectBuilder()
                .withTimeout(SMALL_TIMEOUT, TimeUnit.MILLISECONDS)
                .withExceptionOnFailure()
                .withInputs(input.getStream(), input2.getStream())
                .build();
        input.waitUntilReady();
        input2.waitUntilReady();
        final Result result = expect.interactWith(1).until(contains("ab"));
        assertTrue(result.isSuccessful());
        final Action<Result> action = mock(Action.class);
        final Result r = expect.interact().when(contains("y")).then(action).until(contains("x"));
        assertTrue(r.isSuccessful());
        verifyNoMoreInteractions(action);
    }

    @Test
    public void testRealWorld() throws Exception {
        final MockInputStream input = mockInputStream("password");
        expect = new ExpectBuilder()
                .withTimeout(SMALL_TIMEOUT, TimeUnit.MILLISECONDS)
                .withExceptionOnFailure()
                .withInputs(input.getStream())
                .build();
        input.waitUntilReady();
        final Action<Result> action1 = new Action<Result>() {
            @Override
            public void apply(final Result result) {
                try {
                    input.push("exit");
                } catch (final InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        final Action<Result> action2 = new Action<Result>() {
            @Override
            public void apply(final Result result) {
                try {
                    input.push("#");
                } catch (final InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
        };
        expect.interact()
                .when(contains("#")).then(action1)
                .when(contains("assword")).then(action2)
                .until(contains("exit"));
    }
}
