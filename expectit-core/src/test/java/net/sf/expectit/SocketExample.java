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

import java.io.IOException;
import java.net.Socket;

import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;

/**
 * Socket example.
 */
public class SocketExample {
    public static void main(String[] args) throws IOException {
        // try-with-resources omitted
        Socket socket = new Socket("google.com", 80);
        Expect expect = new ExpectBuilder()
                .withInputs(socket.getInputStream())
                .withOutput(socket.getOutputStream())
                .withEchoInput(System.out)
                .withEchoOutput(System.err)
                .build();
        expect.sendLine("GET");
        String result = expect.expect(contains("\n")).getBefore();
        System.out.println("Result: " + result);
        expect.expect(contains("<H1>302 Moved</H1>"));
        String url = expect.expect(regexp("<A HREF=\"([^\"]*)")).group(1);
        System.out.println("Redirect url from html: " + url);
        // finally omitted
        expect.close();
        socket.close();
    }
}
