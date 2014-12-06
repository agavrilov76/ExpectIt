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

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static net.sf.expectit.ExpectBuilder.DEFAULT_BUFFER_SIZE;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.google.common.base.Charsets;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * A copier test.
 */
public class InputStreamCopierTest {
    private ExecutorService executor = Executors.newCachedThreadPool();

    private InputStream input;
    private ByteArrayOutputStream sink;
    private URL resource;
    private WritableByteChannel channel;

    @Before
    public void setup() throws IOException {
        resource = getResource("sample.txt");
        input = resource.openStream();
        sink = new ByteArrayOutputStream();
        channel = Channels.newChannel(sink);
    }

    @After
    public void cleanUp() {
        executor.shutdown();
    }

    @Test
    public void testCopy() throws IOException, ExecutionException, InterruptedException {
        final InputStreamCopier copier =
                new InputStreamCopier(channel, input, DEFAULT_BUFFER_SIZE, null, null);
        executor.submit(copier).get();
        assertArrayEquals(toByteArray(resource), sink.toByteArray());
    }

    @Test
    public void testClosedStream() throws IOException, ExecutionException, InterruptedException {
        input.close();
        try {
            final InputStreamCopier copier =
                    new InputStreamCopier(channel, input, DEFAULT_BUFFER_SIZE, null, null);
            executor.submit(copier).get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testEcho() throws ExecutionException, InterruptedException, IOException {
        final Appendable echo = mock(Appendable.class);
        final InputStreamCopier copier =
                new InputStreamCopier(channel, input, DEFAULT_BUFFER_SIZE, echo, null);
        executor.submit(copier).get();
        final String string = new String(toByteArray(resource));
        verify(echo).append(string);
    }

    @Test
    public void testEcho2() throws ExecutionException, InterruptedException, IOException {
        final Appendable echo = mock(Appendable.class);
        final Charset utf16 = Charsets.UTF_16;
        final InputStreamCopier copier =
                new InputStreamCopier(channel, input, DEFAULT_BUFFER_SIZE, echo, utf16);
        executor.submit(copier).get();
        final String string = new String(toByteArray(resource), utf16);
        verify(echo).append(string);
    }

    @Test
    public void testEcho3() throws ExecutionException, InterruptedException, IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final PrintStream echo = new PrintStream(bytes);
        final InputStreamCopier copier =
                new InputStreamCopier(channel, input, DEFAULT_BUFFER_SIZE, echo, null);
        executor.submit(copier).get();
        assertArrayEquals(toByteArray(resource), bytes.toByteArray());
    }
}
