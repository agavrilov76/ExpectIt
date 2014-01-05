package net.sf.expectit;

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

import org.junit.Test;

import static net.sf.expectit.filter.Filters.printableOnly;
import static net.sf.expectit.filter.Filters.removeColors;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Filter tests.
 */
public class FilterTest {

    @Test
    public void testNonPrintable() {
        assertTrue(printableOnly().filter("\u0000\u0008", new StringBuilder()).isEmpty());
    }

    @Test
    public void testNoColors() {
        assertEquals(removeColors().filter("\u001b[31m\u001B[7m", new StringBuilder()), "");
    }
}
