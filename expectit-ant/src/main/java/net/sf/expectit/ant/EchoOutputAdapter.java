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
import net.sf.expectit.echo.EchoOutput;
import org.apache.tools.ant.Task;

/**
 * A log adapter which passes the captured input and output to the Ant logger for the given task.
 */
class EchoOutputAdapter implements EchoOutput {
    private final Task task;

    /**
     * Creates an instance for the given task.
     *
     * @param task the task instance
     */
    public EchoOutputAdapter(Task task) {
        this.task = task;
    }

    @Override
    public void onReceive(int input, String string) throws IOException {
        task.log("RECEIVE: " + string);
    }

    @Override
    public void onSend(String string) throws IOException {
        task.log("SEND: " + string);
    }
}
