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

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.eof;
import static net.sf.expectit.matcher.Matchers.regexp;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A process example.
 */
public class ProcessExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("/bin/sh");

        Expect expect = new ExpectBuilder()
                .withInputs(process.getInputStream())
                .withOutput(process.getOutputStream())
                .withTimeout(1, TimeUnit.SECONDS)
                .withEchoInput(System.out)
                .withEchoOutput(System.err)
                .withExceptionOnFailure()
                .build();
        System.out.println("PWD:" + expect.sendLine("pwd").expect(contains("\n")).getBefore());
        // try-with-resources is omitted for simplicity
        expect.sendLine("ls -lh");
        // capture the total
        String total = expect.expect(regexp("^total (.*)")).group(1);
        System.out.println("Size: " + total);
        // capture file list
        String list = expect.expect(regexp("\n$")).getBefore();
        // print the result
        System.out.println("List: " + list);
        expect.sendLine("exit");
        // expect the process to finish
        expect.expect(eof());
        // finally is omitted
        process.waitFor();
        expect.close();
    }

}
