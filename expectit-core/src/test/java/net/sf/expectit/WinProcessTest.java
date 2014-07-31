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

import org.junit.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.Utils.LONG_TIMEOUT;
import static net.sf.expectit.Utils.SMALL_TIMEOUT;
import static net.sf.expectit.echo.EchoAdapters.adapt;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.eof;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * A test for interacting with Windows OS shell.
 */
public class WinProcessTest {

    public static final String WIN_CMD = System.getenv("COMSPEC");
    private Expect expect;
    private Process process;

    @BeforeClass
    public static void ignoreOnWindows() {
        assumeTrue(WIN_CMD != null && new File(WIN_CMD).canExecute());
    }

    @Before
    public void setup() throws IOException {
        ProcessBuilder builder = new ProcessBuilder(WIN_CMD, "/Q");
        process = builder.start();
        expect = new ExpectBuilder()
                .withTimeout(LONG_TIMEOUT, TimeUnit.MILLISECONDS)
                .withInputs(process.getInputStream(), process.getErrorStream())
                .withOutput(process.getOutputStream())
                .withInputFilters(removeNonPrintable())
                .withEchoInput(System.out)
                .withEchoOutput(System.err)
                 // sets cyrillic DOS encoding to verify that
                .withCharset(Charset.forName("cp866"))
                .build();
    }

    @After
    public void cleanup() throws IOException, InterruptedException {
        process.destroy();
        process.waitFor();
        expect.close();
    }

    @Test
    public void test() throws IOException, InterruptedException {
        Thread.sleep(SMALL_TIMEOUT);
        try {
            Assume.assumeTrue(process.exitValue() != Integer.MAX_VALUE);
        } catch (IllegalThreadStateException ignore) {
        }
        System.out.println(expect.expect(contains(">")).getBefore());
        expect.sendLine("echo test-123");
        Result res = expect.expect(contains("test-123"));
        assertTrue(res.isSuccessful());
        assertFalse(res.getBefore().contains("echo"));
        assertTrue(expect.expect(contains(">")).isSuccessful());
        expect.sendLine("echo %cd%");
        System.err.println("pwd: " + expect.expect(contains("\n")).getBefore());
        expect.sendLine("exit");
        assertTrue(expect.expect(eof()).isSuccessful());
    }

}
