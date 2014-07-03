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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static net.sf.expectit.ExpectBuilder.DEFAULT_BUFFER_SIZE;
import static org.junit.Assert.*;

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
        executor.submit(new InputStreamCopier(channel, input, DEFAULT_BUFFER_SIZE)).get();
        assertArrayEquals(toByteArray(resource), sink.toByteArray());
    }

    @Test
    public void testClosedStream() throws IOException, ExecutionException, InterruptedException {
        input.close();
        try {
            executor.submit(new InputStreamCopier(channel, input, DEFAULT_BUFFER_SIZE)).get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

}
