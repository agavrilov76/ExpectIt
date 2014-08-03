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

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

/**
 * A mock object which provides control over the input stream.
 */
public class MockInputStream {
    private final InputStream stream;
    private final BlockingQueue<String> queue;
    private final CountDownLatch latch;

    MockInputStream(InputStream stream, BlockingQueue<String> queue, CountDownLatch latch) {
        this.stream = stream;
        this.queue = queue;
        this.latch = latch;
    }

    public InputStream getStream() {
        return stream;
    }

    public void push(String data) throws InterruptedException {
        queue.put(data);
    }

    public void waitUntilReady() throws InterruptedException {
        latch.await();
    }
}
