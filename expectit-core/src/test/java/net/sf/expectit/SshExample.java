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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;

import static net.sf.expectit.filter.Filters.printableOnly;
import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;

/**
 * An example of interacting with SSH server
 */
public class SshExample {
    public static void main(String[] args) throws JSchException, IOException {
        JSch jSch = new JSch();
        Session session = jSch.getSession("new", "sdf.org");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        Channel channel = session.openChannel("shell");

        Expect expect = new ExpectBuilder()
                .withOutput(channel.getOutputStream())
                .withInputs(channel.getInputStream(), channel.getExtInputStream())
                .withEchoOutput(new PrintWriter(System.out))
                .withInputFilters(removeColors(), printableOnly())
                .withErrorOnTimeout(true)
                .build();

        channel.connect();
        expect.expect(contains("[RETURN]"));
        expect.sendLine();
        String ipAddress = expect.expect(regexp("Trying (.*)\\.\\.\\.")).group(1);
        System.out.println("Captured IP: " + ipAddress);
        expect.expect(contains("login:"));
        expect.sendLine("new");
        expect.expect(contains("(Y/N)"));
        expect.send("N");
        expect.expect(regexp(": $"));
        expect.send("\b");
        expect.expect(regexp("\\(y\\/n\\)"));
        expect.sendLine("y");
        expect.expect(contains("Would you like to sign the guestbook?"));
        expect.send("n");
        expect.expect(contains("[RETURN]"));
        expect.sendLine();
        channel.disconnect();
        session.disconnect();
        expect.close();
    }
}
