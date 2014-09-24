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


import static net.schmizz.sshj.connection.channel.direct.Session.Shell;
import static net.sf.expectit.filter.Filters.removeColors;
import static net.sf.expectit.filter.Filters.removeNonPrintable;
import static net.sf.expectit.matcher.Matchers.contains;
import static net.sf.expectit.matcher.Matchers.regexp;

import java.io.IOException;
import java.security.PublicKey;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

/**
 * A ExpectIt example using sshj library.
 */
public class SshJExample {
    public static void main(String[] args) throws IOException {
        SSHClient ssh = new SSHClient();
        ssh.addHostKeyVerifier(
                new HostKeyVerifier() {
                    @Override
                    public boolean verify(String s, int i, PublicKey publicKey) {
                        return true;
                    }
                });
        ssh.connect("sdf.org");
        ssh.authPassword("new", "");
        Session session = ssh.startSession();
        session.allocateDefaultPTY();
        Shell shell = session.startShell();
        Expect expect = new ExpectBuilder()
                .withOutput(shell.getOutputStream())
                .withInputs(shell.getInputStream(), shell.getErrorStream())
                .withEchoInput(System.out)
                .withEchoOutput(System.err)
                .withInputFilters(removeColors(), removeNonPrintable())
                .withErrorOnTimeout(true)
                .build();
        try {
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
        } finally {
            session.close();
            ssh.close();
            expect.close();
        }
    }
}
