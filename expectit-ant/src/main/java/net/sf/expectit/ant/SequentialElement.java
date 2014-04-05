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

import net.sf.expectit.Expect;
import net.sf.expectit.ant.matcher.AbstractTaskElement;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.taskdefs.Sequential;

import java.util.ArrayList;
import java.util.List;

/**
 * A container for the expect operations and other Ant tasks.
 */
public class SequentialElement extends Sequential {
    private final List<Task> tasks = new ArrayList<Task>();
    private Expect expect;

    @Override
    public void addTask(Task task) {
        tasks.add(task);
        super.addTask(task);
    }

    /**
     * Sets the expect instance.
     *
     * @param expect the expect
     */
    public void setExpect(Expect expect) {
        this.expect = expect;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Passes the {@code expect} instance to the children tasks that are instanceof
     * {@link net.sf.expectit.ant.matcher.AbstractTaskElement}.
     */
    @Override
    public void execute() {
        for (Task t : tasks) {
            if (t instanceof UnknownElement) {
                t.maybeConfigure();
                Object proxy = ((UnknownElement) t).getWrapper().getProxy();
                if (proxy instanceof AbstractTaskElement) {
                    ((AbstractTaskElement) proxy).setExpect(expect);
                }
            }
        }
        super.execute();
    }

}
