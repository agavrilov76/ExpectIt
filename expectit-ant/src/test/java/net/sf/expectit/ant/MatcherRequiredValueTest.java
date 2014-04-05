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

import net.sf.expectit.ant.matcher.*;
import org.apache.tools.ant.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;

/**
 * Tests that elements throw illegal argument exception.
 */
@RunWith(Parameterized.class)
public class MatcherRequiredValueTest {
    private final Task task;

    public MatcherRequiredValueTest(Task task) {
        this.task = task;
    }

    @Parameterized.Parameters(name = "{0}")
    public static Iterable<Object[]> data() throws IOException {
        return Arrays.asList(new Object[][]{
                {new ContainsElement()},
                {new AllOfElement()},
                {new RegexpElement()},
                {new MatchesElement()},
                {new AnyOfElement()},
                {new TimesElement()}
        });
    }

    @Test(expected = IllegalArgumentException.class)
    public void test() {
        task.execute();
    }

}
