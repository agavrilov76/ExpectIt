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
 * A telnet process example.
 */
public class TelnetProcessExample {
    public static void main(String[] args) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec("telnet rainmaker.wunderground.com");

        StringBuilder wholeBuffer = new StringBuilder();
        Expect expect = new ExpectBuilder()
                .withOutput(process.getOutputStream())
                .withInputs(process.getInputStream())
                .withEchoOutput(wholeBuffer)
                .withEchoInput(wholeBuffer)
                .withExceptionOnFailure()
                .build();

        expect.expect(contains("Press Return to continue"));
        expect.sendLine();
        expect.expect(contains("forecast city code--"));
        expect.sendLine("SAN");
        expect.expect(contains("X to exit:"));
        expect.sendLine();

        String response = wholeBuffer.toString();
        System.out.println(response);

        expect.close();
    }

}
