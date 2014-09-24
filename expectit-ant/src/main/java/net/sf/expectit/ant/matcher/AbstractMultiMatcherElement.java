package net.sf.expectit.ant.matcher;

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

import java.util.ArrayList;
import java.util.List;
import net.sf.expectit.MultiResult;
import net.sf.expectit.Result;
import net.sf.expectit.matcher.Matcher;

/**
 * An abstract base element that represents a composite matcher.
 */
abstract class AbstractMultiMatcherElement extends AbstractMatcherElement<MultiResult> {
    private final List<AbstractMatcherElement<Result>> tasks = new
            ArrayList<AbstractMatcherElement<Result>>();

    /**
     * Adds a child matcher element.
     *
     * @param matcherTask the matcher element
     */
    public void add(AbstractMatcherElement<Result> matcherTask) {
        tasks.add(matcherTask);
    }

    /**
     * {@inheritDoc}
     * <p/>
     * In addition, this method exports the children results. They are exported using their
     * {@code resultPrefix}, and
     * via shortcut {@code prefix + "." + &lt;number&gt;}, where the number here is the result
     * index.
     *
     * @param prefix the property prefix
     * @param result the result
     */
    @Override
    protected void exportSuccessfulResult(String prefix, MultiResult result) {
        super.exportSuccessfulResult(prefix, result);
        for (int i = 0; i < tasks.size(); i++) {
            AbstractMatcherElement<Result> t = tasks.get(i);
            t.exportSuccessfulResult(t.getResultPrefix(), result.getResults().get(i));
            t.exportSuccessfulResult(prefix + "." + i, result.getResults().get(i));
        }
    }

    /**
     * Creates and return all the children matchers.
     *
     * @return matcher array
     */
    protected Matcher<?>[] getMatchers() {
        Matcher<?>[] matchers = new Matcher<?>[tasks.size()];
        for (int i = 0; i < matchers.length; i++) {
            matchers[i] = tasks.get(i).createMatcher();
        }
        return matchers;
    }
}
