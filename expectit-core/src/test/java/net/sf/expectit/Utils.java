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

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.io.InputStream;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Constants and method used in all the tests.
 */
public final class Utils {
    private Utils() {
    }

    /**
     * A small timeout used for the tests that block until an expect operation finishes.
     */
    public static final long SMALL_TIMEOUT = Long.getLong(Utils.class.getName(), 200);

    /**
     * Creates a mock stream that pumps the given text every period of milliseconds.
     */
    public static InputStream mockInputStream(final long period, final String text) throws IOException {
        InputStream mock = mock(InputStream.class);
        when(mock.read(any(byte[].class))).then(new Answer<Object>() {
            private boolean firstTime = true;

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                if (!firstTime) {
                    Thread.sleep(period);
                }
                firstTime = false;
                byte[] bytes = text.getBytes();
                //noinspection MismatchedReadAndWriteOfArray
                byte[] dest = (byte[]) invocation.getArguments()[0];
                if (dest == null) {
                    return -1;
                }
                System.arraycopy(bytes, 0, dest, 0, bytes.length);
                return bytes.length;
            }
        });
        return mock;
    }

    /**
     * A long timeout which should cause tests to fail.
     */
    public static final long LONG_TIMEOUT = Long.getLong(Utils.class.getName(), 1000);

}
