package net.sf.expectit.ant;

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

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.*;

/**
* Created by alexey on 2/9/14.
*/
class SshEchoCommandFactory implements Factory<Command>, Runnable {
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private OutputStream out;
    private InputStream in;

    @Override
    public void run() {
        byte[] buffer = new byte[4096];
        try {
            int len = in.read(buffer);
            if (len > 0) {
                out.write(buffer, 0, len);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Command create() {
        return new Command() {
            @Override
            public void setInputStream(InputStream in) {
                SshEchoCommandFactory.this.in = in;
            }

            @Override
            public void setOutputStream(OutputStream out) {
                SshEchoCommandFactory.this.out = out;
            }

            @Override
            public void setErrorStream(OutputStream err) {

            }

            @Override
            public void setExitCallback(ExitCallback callback) {

            }

            @Override
            public void start(Environment env) throws IOException {
                executor.scheduleWithFixedDelay(SshEchoCommandFactory.this, 0, 100, TimeUnit.MILLISECONDS);
            }

            @Override
            public void destroy() {
                executor.shutdownNow();
            }
        };
    }
}
