package net.sf.expectit.ant;

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
import net.sf.expectit.ant.filter.FiltersElement;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * An expect task implementation for a socket connection.
 */
public class ExpectSocket extends Task implements ExpectSupport {
    private ExpectSupportImpl expectSupport;
    private Integer port;
    private String host;

    /**
     * Creates an instance of {@link net.sf.expectit.ant.ExpectSupport}.
     */
    @Override
    public void init() {
        expectSupport = new ExpectSupportImpl(this);
    }

    @Override
    public void add(SequentialElement sequential) {
        expectSupport.add(sequential);
    }

    @Override
    public void setExpectTimeout(long ms) {
        expectSupport.setExpectTimeout(ms);
    }

    @Override
    public void setCharset(String charset) {
        expectSupport.setCharset(charset);
    }

    @Override
    public void add(FiltersElement filters) {
        expectSupport.add(filters);
    }

    @Override
    public void setErrorOnTimeout(boolean errorOnTimeout) {
        expectSupport.setErrorOnTimeout(errorOnTimeout);
    }

    @Override
    public void setExceptionOnFailure(final boolean exceptionOnFailure) {
        expectSupport.setExceptionOnFailure(exceptionOnFailure);
    }

    @Override
    public void setLineSeparator(String lineSeparator) {
        expectSupport.setLineSeparator(lineSeparator);
    }

    /**
     * Sets the socket port.
     *
     * @param port the socket port.
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Sets the socket host name.
     *
     * @param host the host name.
     */
    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public void setEchoOutput(boolean echoOutput) {
        expectSupport.setEchoOutput(echoOutput);
    }

    @Override
    public void execute() {
        if (host == null) {
            throw new IllegalArgumentException("host is not set");
        }
        if (port == null) {
            throw new IllegalArgumentException("port is not set");
        }
        try {
            Socket socket = new Socket(host, port);
            try {
                expectSupport.setInput(0, socket.getInputStream());
                expectSupport.setOutput(socket.getOutputStream());
                expectSupport.execute();
            } finally {
                expectSupport.close();
                socket.close();
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
}
