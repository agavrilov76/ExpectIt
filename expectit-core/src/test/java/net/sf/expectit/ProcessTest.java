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

import com.google.common.io.Files;
import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.Utils.LONG_TIMEOUT;
import static net.sf.expectit.Utils.SMALL_TIMEOUT;
import static net.sf.expectit.matcher.Matchers.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Convert to integration test
 */
public class ProcessTest {

    public static final String BIN_SH = "/bin/sh";
    private Expect expect;
    private Process process;

    @BeforeClass
    public static void ignoreOnWindows() {
        assumeTrue(new File(BIN_SH).canExecute());
    }

    @Before
    public void setup() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(BIN_SH);
        try {
            process = builder.start();
        } catch (IOException e) {
            Assume.assumeNoException("Unable to start shell", e);
        }
        expect = new ExpectBuilder()
                .withTimeout(LONG_TIMEOUT, TimeUnit.MILLISECONDS)
                .withInputs(process.getInputStream(), process.getErrorStream())
                .withOutput(process.getOutputStream())
                .build();
    }

    @After
    public void cleanup() throws IOException, InterruptedException {
        if (process != null) {
            process.destroy();
            process.waitFor();
        }
        if (expect != null) {
            expect.close();
        }
    }

    @Test
    public void testBasicOperations() throws IOException, InterruptedException {
        expect.send("echo xyz\n");
        expect.send("echo test-123\n");
        expect.expect(contains("y")).start();
        Result expect1 = expect.expect(regexp("(?m)^(.*)-123"));

        try {
            expect.expect(SMALL_TIMEOUT, allOf(contains("G"), regexp("g"))).getBefore();
            fail();
        } catch (IllegalStateException ignore) {
        }

        String output = expect1.getBefore();
        assertEquals(output, "z\n");
    }

    @Test
    public void testHugeInput() throws IOException {
        File f = File.createTempFile("test", ".txt");
        StringBuilder builder = new StringBuilder();
        int size = 10000;
        for (int i = 0; i < size; i++) {
            builder.append(UUID.randomUUID().toString());
        }
        Files.write(builder.toString().getBytes(), f);
        f.deleteOnExit();
        expect.sendLine("cat " + f.getAbsolutePath());
        expect.sendLine("echo TEST_STRING");
        int len = expect.expect(30000, contains("TEST_STRING")).getBefore().length();
        assertEquals(size * 36, len);
    }

    @Test
    public void testErrorStream() throws IOException {
        String string = UUID.randomUUID().toString();
        expect.sendLine("echo " + string + " >&2");
        System.out.println(expect.expectIn(1, contains(string)).group());
    }

    @Test
    public void testEof() throws IOException {
        expect.sendLine("echo Line1");
        expect.sendLine("echo Line2");
        expect.sendLine("sleep " + LONG_TIMEOUT / 1000 + "; echo Line3; exit");
        Result result = expect.expect(5 * LONG_TIMEOUT, eof());
        assertEquals(result.getBefore(),"Line1\nLine2\nLine3\n", result.getBefore());
    }
}
