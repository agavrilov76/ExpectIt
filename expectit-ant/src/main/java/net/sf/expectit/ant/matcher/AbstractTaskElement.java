package net.sf.expectit.ant.matcher;

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

import net.sf.expectit.Expect;
import org.apache.tools.ant.Task;

/**
 * An abstract expect task element.
 */
public abstract class AbstractTaskElement extends Task {
     private Expect expect;

    /**
     * Sets the expect instance.
     *
     * @param expect the instance
     */
    public void setExpect(Expect expect) {
        this.expect = expect;
    }

    /**
     * Gets an expect instance.
     *
     * @return the instance
     */
    public Expect getExpect() {
        return expect;
    }
}
