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

import net.sf.expectit.filter.Filter;
import org.junit.Test;

import static net.sf.expectit.filter.Filters.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Alexey Gavrilov
 */
public class FilterTest {

    @Test
    public void testFilterChain() {
        StringBuilder buffer = new StringBuilder();
        Filter f1 = mock(Filter.class);
        when(f1.filter("x", buffer)).thenReturn("X");
        Filter f2 = mock(Filter.class);
        when(f2.filter("X", buffer)).thenReturn("Y");
        assertEquals(chain(f1, f2).filter("x", buffer), "Y");
        Filter f3 = mock(Filter.class);
        when(f3.filter("x", buffer)).thenReturn(null);
        assertEquals(chain(f1, f3, f2).filter("x", buffer), "X");
    }

    @Test
    public void testNonPrintable() {
        assertTrue(printableOnly().filter("\u0000\u0008", new StringBuilder()).isEmpty());
    }

    @Test
    public void testNoColors() {
        assertEquals(removeColors().filter("\u001b[31m\u001B[7m", new StringBuilder()), "");
    }
}
