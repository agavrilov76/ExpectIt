package net.sf.expectit;

import java.io.InputStream;
import java.util.concurrent.BlockingQueue;

/**
 * Created by agavrilov on 02/07/14.
 */
public class MockInputStream {
    private final InputStream stream;
    private final BlockingQueue<String> queue;

    public MockInputStream(InputStream stream, BlockingQueue<String> queue) {
        this.stream = stream;
        this.queue = queue;
    }

    public InputStream getStream() {
        return stream;
    }

    public void push(String data) throws InterruptedException {
        queue.put(data);
    }
}
