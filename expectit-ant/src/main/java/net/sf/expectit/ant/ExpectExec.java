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
import java.io.InputStream;
import java.io.OutputStream;
import net.sf.expectit.ant.filter.FiltersElement;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.taskdefs.Execute;
import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
import org.apache.tools.ant.types.RedirectorElement;

/**
 * An expect implementation of the exec task.
 */
public class ExpectExec extends ExecTask implements ExecuteStreamHandler, ExpectSupport {
    private ExpectSupportImpl expectSupport;

    private boolean destroyProcess;

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
    public void add(FiltersElement filter) {
        expectSupport.add(filter);
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
     * Creates an instance of {@link net.sf.expectit.ant.ExpectSupport}.
     */
    @Override
    public void init() {
        expectSupport = new ExpectSupportImpl(this);
    }

    /**
     * Always throw an {@link java.lang.UnsupportedOperationException} exception.
     *
     * @param redirectorElement the element
     */
    @Override
    public void addConfiguredRedirector(RedirectorElement redirectorElement) {
        throw new UnsupportedOperationException("Redirector element is not supported");
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Upon creation, configure the input and output streams for the {@link ExpectSupportImpl}
     * instance and
     * call the {@link ExpectSupportImpl#execute()} method.
     *
     * @return an execute instance
     */
    @Override
    protected Execute prepareExec() {
        Execute original = super.prepareExec();
        Execute exec = new Execute(this, createWatchdog()) {
            @Override
            protected void waitFor(Process process) {
                try {
                    expectSupport.execute();
                    if (destroyProcess) {
                        process.destroy();
                    }
                } catch (Exception e) {
                    process.destroy();
                    throw new BuildException(e);
                } finally {
                    super.waitFor(process);
                }
            }
        };
        exec.setNewenvironment(newEnvironment);
        exec.setAntRun(getProject());
        exec.setWorkingDirectory(original.getWorkingDirectory());
        exec.setEnvironment(original.getEnvironment());
        return exec;
    }

    @Override
    public void setProcessInputStream(OutputStream os) throws IOException {
        expectSupport.setOutput(os);
    }

    @Override
    public void setProcessErrorStream(InputStream is) throws IOException {
        expectSupport.setInput(1, is);
    }

    @Override
    public void setProcessOutputStream(InputStream is) throws IOException {
        expectSupport.setInput(0, is);
    }

    @Override
    public void start() throws IOException {
    }

    /**
     * Closes the underlying {@link net.sf.expectit.ant.ExpectSupportImpl} instance.
     */
    @Override
    public void stop() {
        try {
            expectSupport.close();
        } catch (IOException e) {
            log(e, Project.MSG_DEBUG);
        }
    }

    /**
     * Set the flag indicating if the underlying process should be destroyed after the task is
     * finished.
     * <p/>
     * By default is {@code false}, meaning that the task will block until the process finishes.
     *
     * @param destroyProcess the destroy process flag
     */
    public void setDestroyProcess(boolean destroyProcess) {
        this.destroyProcess = destroyProcess;
    }

    @Override
    public void setEchoOutput(boolean echoOutput) {
        expectSupport.setEchoOutput(echoOutput);
    }
}
