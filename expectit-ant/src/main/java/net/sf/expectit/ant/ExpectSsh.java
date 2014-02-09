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

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import net.sf.expectit.ant.filter.FiltersElement;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.optional.ssh.SSHBase;

import java.io.IOException;

/**
 * An expect task implementation for a ssh connection.
 */
public class ExpectSsh extends SSHBase implements ExpectSupport {
    private ExpectSupportImpl expectSupport;

    @Override
    public void add(SequentialElement sequential) {
        expectSupport.add(sequential);
    }

    @Override
    public void add(FiltersElement filters) {
        expectSupport.add(filters);
    }

    @Override
    public void setLineSeparator(String lineSeparator) {
        expectSupport.setLineSeparator(lineSeparator);
    }

    @Override
    public void setCharset(String charset) {
        expectSupport.setCharset(charset);
    }

    @Override
    public void setEchoOutput(boolean echoOutput) {
        expectSupport.setEchoOutput(echoOutput);
    }

    @Override
    public void setExpectTimeout(long ms) {
        expectSupport.setExpectTimeout(ms);
    }

    @Override
    public void setErrorOnTimeout(boolean errorOnTimeout) {
        expectSupport.setErrorOnTimeout(errorOnTimeout);
    }

    @Override
    public void init() {
        super.init();
        expectSupport = new ExpectSupportImpl(this);
    }

    @Override
    public void execute() {
        try {
            Session session = openSession();
            Channel channel = session.openChannel("shell");
            expectSupport.setOutput(channel.getOutputStream());
            expectSupport.setInput(0, channel.getInputStream());
            expectSupport.setInput(1, channel.getExtInputStream());
            channel.connect();
            try {
                expectSupport.execute();
            } finally {
                channel.disconnect();
                session.disconnect();
                expectSupport.close();
            }
        } catch (IOException e) {
            throw new BuildException(e);
        } catch (JSchException e) {
            throw new BuildException(e);
        }
    }
}
