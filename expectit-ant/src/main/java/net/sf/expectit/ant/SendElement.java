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
import net.sf.expectit.ant.matcher.AbstractTaskElement;
import org.apache.tools.ant.BuildException;

/**
 * An element that corresponds to the {@link net.sf.expectit.Expect}'s send methods.
 */
public class SendElement extends AbstractTaskElement {
    private String line;
    private String string;

    @Override
    public void execute() {
        if (line != null && string != null) {
            throw new IllegalArgumentException("line and string cannot be set together");
        }
        try {
            if (line != null) {
                getExpect().sendLine(getProject().replaceProperties(line));
            } else {
                getExpect().send(getProject().replaceProperties(string));
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    /**
     * Sets the string that will be passed to {@link net.sf.expectit.Expect#sendLine(java.lang
     * .String)}}.
     *
     * @param line the string.
     */
    public void setLine(String line) {
        this.line = line;
    }

    /**
     * Sets the string that will be passed to {@link net.sf.expectit.Expect#send(String)}.
     *
     * @param string the string.
     */
    public void setString(String string) {
        this.string = string;
    }
}
