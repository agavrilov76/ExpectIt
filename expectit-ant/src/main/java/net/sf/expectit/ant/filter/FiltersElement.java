package net.sf.expectit.ant.filter;

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

import static net.sf.expectit.filter.FilterChain.chain;

import java.util.ArrayList;
import java.util.List;
import net.sf.expectit.filter.Filter;
import org.apache.tools.ant.Task;

/**
 * A container for filter elements.
 */
public class FiltersElement extends Task {
    private List<AbstractFilterElement> filters = new ArrayList<AbstractFilterElement>();

    /**
     * Adds an filter element into the container.
     *
     * @param filter the element instance
     */
    public void add(AbstractFilterElement filter) {
        filters.add(filter);
    }

    /**
     * Creates a filter from the list of the children filter elements.
     *
     * @return the filter instance
     */
    public Filter toFilter() {
        Filter[] result = new Filter[filters.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = filters.get(i).createFilter();
        }
        return chain(result);
    }
}
