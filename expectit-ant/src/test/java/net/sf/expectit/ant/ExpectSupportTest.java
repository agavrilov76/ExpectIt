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

import net.sf.expectit.ExpectBuilder;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Tests delegation from ExpectSupport instance to ExpectBuilder.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(ExpectBuilder.class)
public class ExpectSupportTest {
    private Task task = mock(Task.class);
    private ExpectBuilder builder = mock(ExpectBuilder.class);
    private ExpectSupportImpl expectSupport = new ExpectSupportImpl(task, builder);

    @Test
    public void testDelegation() throws IOException {
        InputStream i1 = mock(InputStream.class);
        InputStream i2 = mock(InputStream.class);
        InputStream i3 = mock(InputStream.class);

        expectSupport.setInput(0, i1);
        expectSupport.setInput(2, i3);
        expectSupport.setInput(1, i2);

        try {
            expectSupport.execute();
            fail();
        } catch (IllegalArgumentException e) {
        }

        expectSupport.setCharset("UTF-8");
        expectSupport.setEchoOutput(true);
        expectSupport.setExpectTimeout(400);
        expectSupport.setLineSeparator("ABC");
        expectSupport.setErrorOnTimeout(true);
        OutputStream output = mock(OutputStream.class);
        expectSupport.setOutput(output);

        SequentialElement sequential = new SequentialElement();
        sequential.setProject(mock(Project.class));
        expectSupport.add(sequential);
        expectSupport.execute();

        verify(builder).withInputs(i1, i2, i3);
        verify(builder).withCharset(Charset.forName("UTF-8"));
        verify(builder).withEchoOutput(any(EchoOutputAdapter.class));
        verify(builder).withLineSeparator("ABC");
        verify(builder).withErrorOnTimeout(true);
        verify(builder).withTimeout(400, TimeUnit.MILLISECONDS);
        //expectSupport.add
        //verify(builder).
    }
}
