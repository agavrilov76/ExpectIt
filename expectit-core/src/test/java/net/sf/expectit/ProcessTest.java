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

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

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
        assumeTrue(Files.isExecutable(Paths.get(BIN_SH)));
    }

    @Before
    public void setup() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(BIN_SH);
        process = builder.start();
        expect = new ExpectBuilder()
                .withTimeout(1000)
                .withInputs(process.getInputStream(), process.getErrorStream())
                .withOutput(process.getOutputStream())
                .build();
    }

    @After
    public void cleanup() {
        process.destroy();
    }

    @Test
    public void testBasicOperations() throws IOException, InterruptedException {
        expect.send("echo xyz\n");
        expect.send("echo test-123\n");
        expect.expect(contains("y")).start();
        Result expect1 = expect.expect(regexp("(?m)^(.*)-123"));

        try {
            expect.expect(100, allOf(contains("G"), regexp("g"))).getBefore();
            fail();
        } catch (IllegalStateException ignore) {
        }

        String output = expect1.getBefore();
        assertEquals(output, "z\n");
    }

    @Test
    public void testIOException() throws IOException, InterruptedException {
        expect.send("exit\n");
        process.waitFor();
        try {
            expect.expect(200, contains("test"));
            fail();
        } catch (IOException ok) {
        }
    }

    @Test
    public void testHugeInput() throws IOException {
        File f = File.createTempFile("test", ".txt");
        StringBuilder builder = new StringBuilder();
        int size = 40000;
        for (int i = 0; i < size; i++) {
            builder.append(UUID.randomUUID().toString());
        }
        Files.write(f.toPath(), builder.toString().getBytes());
        f.deleteOnExit();
        expect.sendLine("cat " + f.getAbsolutePath());
        expect.sendLine("echo TEST_STRING");
        int len = expect.expect(60000, contains("TEST_STRING")).getBefore().length();
        assertEquals(size * 36, len);
    }

    @Test
    public void testErrorStream() throws IOException {
        String string = UUID.randomUUID().toString();
        expect.sendLine("echo " + string + " >&2");
        System.out.println(expect.expectIn(1, contains(string)).group());
    }


    // for README
    public static void main(String[] args) throws IOException {
        ProcessBuilder builder = new ProcessBuilder("/bin/sh");
        Process process = builder.start();
        Expect expect = new ExpectBuilder()
                .withTimeout(1000)
                .withInputs(process.getInputStream(), process.getErrorStream())
                .withOutput(process.getOutputStream())
                .build();
        expect.sendLine("echo Hello World!");
        Result result = expect.expect(regexp("Wor.."));
        System.out.println("Before: " + result.getBefore());
        System.out.println("Match: " + result.group());
        expect.sendLine("exit");
        expect.close();
    }
}
