package net.sf.expectit;

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
