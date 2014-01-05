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
import java.util.Properties;

import static net.sf.expectit.filter.Filters.printableOnly;
import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.eof;
import static net.sf.expectit.matcher.Matchers.regexp;

/**
 * An example to interacting with the Karaf shell.
 */
public class KarafExample {
    public static void main(String[] args) throws JSchException, IOException {
        JSch jSch = new JSch();
        Session session = jSch.getSession("karaf", "localhost", 8101);
        session.setPassword("karaf");
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        Channel channel = session.openChannel("shell");
        Expect expect = new ExpectBuilder()
                .withOutput(channel.getOutputStream())
                .withInputs(channel.getInputStream(), channel.getExtInputStream())
                .withEchoOutput(System.out)
                .withInputFilters(removeColors(), printableOnly())
                .withErrorOnTimeout(true)
                .build();
        channel.connect();
        expect.expect(regexp("karaf@root\\(\\)> "));
        expect.send("\t");
        expect.expect(contains("y or n)"));
        expect.send("y");
        String list = expect.expect(regexp("karaf@root\\(\\)> ")).getBefore();
        System.err.println(list);
        expect.sendLine("list");
        System.err.println(expect.expect(regexp("karaf@root\\(\\)> ")).getBefore());
        expect.sendLine("logout");
        expect.expect(eof());
        channel.disconnect();
        session.disconnect();
        expect.close();
    }
}
