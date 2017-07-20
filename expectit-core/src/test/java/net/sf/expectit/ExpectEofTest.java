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

import static net.sf.expectit.matcher.Matchers.regexp;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Test;

/**
 * Tests for various expect builder parameters
 */
public class ExpectEofTest {
    private Expect expect;

    @After
    public void cleanup() throws IOException {
        if (expect != null) {
            expect.close();
            expect = null;
        }
    }

    @Test (timeout = 1000, expected = EOFException.class)
    public void testEOF() throws IOException, InterruptedException {
        final ServerSocket serverSocket = new ServerSocket(0);
        final Thread serverThread = new Thread() {
            @Override
            public void run() {
                try {
                    final Socket socket = serverSocket.accept();
                    socket.getOutputStream().close();
                    serverSocket.close();
                } catch (final IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };
        serverThread.start();
        final Socket client = new Socket("localhost", serverSocket.getLocalPort());
        final InputStream inputStream = client.getInputStream();
        expect = new ExpectBuilder()
                .withInputs(inputStream)
                .build();
        try {
            expect.withTimeout(3000, TimeUnit.MILLISECONDS).expect(regexp("abc"));
        } finally {
            serverThread.join();
        }
    }
}
