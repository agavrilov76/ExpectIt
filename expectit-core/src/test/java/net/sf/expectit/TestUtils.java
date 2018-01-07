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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Constants and method used in all the tests.
 */
public final class TestUtils {
    public static final String EOF = "EOF";

    private TestUtils() {
    }

    /**
     * A small timeout used for the tests that block until an expect operation finishes.
     */
    public static final long SMALL_TIMEOUT
            = Long.getLong(TestUtils.class.getName() + ".smallTimeout", 300);

    /**
     * A long timeout which should cause tests to fail.
     */
    public static final long LONG_TIMEOUT
            = Long.getLong(TestUtils.class.getName() + ".longTimeout", 1500);

    /**
     * Creates a mock stream that pumps the given text every period of milliseconds.
     */
    public static MockInputStream mockInputStream(String text) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        InputStream mock = mock(InputStream.class);
        final BlockingQueue<String> queue = new LinkedBlockingDeque<String>();
        queue.put(text);
        when(mock.read(any(byte[].class))).then(
                new Answer<Object>() {

                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        try {
                            String take = queue.take();
                            if (take.equals(EOF)) {
                                return -1;
                            }
                            byte[] bytes = take.getBytes();
                            //noinspection MismatchedReadAndWriteOfArray
                            byte[] dest = (byte[]) invocation.getArguments()[0];
                            System.arraycopy(bytes, 0, dest, 0, bytes.length);
                            return bytes.length;
                        } finally {
                            latch.countDown();
                        }
                    }
                });
        return new MockInputStream(mock, queue, latch);
    }
}
