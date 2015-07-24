package net.sf.expectit;

/*
 * #%L
 * ExpectIt
 * %%
 * Copyright (C) 2014 - 2015 Alexey Gavrilov and contributors
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

import static net.sf.expectit.matcher.Matchers.anyOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import net.sf.expectit.interact.Action;
import net.sf.expectit.interact.InteractBuilder;
import net.sf.expectit.interact.OngoingResult;
import net.sf.expectit.matcher.Matcher;
import net.sf.expectit.matcher.SimpleResult;

class InteractBuilderImpl implements InteractBuilder {
    private static final Logger LOG = Logger.getLogger(SingleInputExpect.class.getName());

    private final AbstractExpectImpl expect;
    private final int input;

    private final List<Matcher<?>> matchers = new ArrayList<Matcher<?>>();
    private final List<Action> actions = new ArrayList<Action>();

    public InteractBuilderImpl(final AbstractExpectImpl expect, final int input) {
        this.expect = expect;
        this.input = input;
    }

    @Override
    public <R extends Result> OngoingResult<R> when(final Matcher<R> matcher) {
        return new OngoingResult<R>() {
            @Override
            public InteractBuilder then(final Action action) {
                if (action == null) {
                    throw new NullPointerException("Action cannot be null");
                }
                matchers.add(matcher);
                actions.add(action);
                return InteractBuilderImpl.this;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends Result> R until(final Matcher<R> matcher) throws IOException {
        final Matcher<?>[] array = new Matcher[matchers.size() + 1];
        for (int i = 0; i < matchers.size(); i++) {
            array[i + 1] = matchers.get(i);
        }
        array[0] = new ResultMatcher((Matcher<Result>) matcher);

        while (true) {
            final long time = System.currentTimeMillis();
            final MultiResult multiResult = expect.expectIn(input, anyOf(array));
            final String inputBuffer = expect.getInputs()[this.input].getBuffer().toString();
            if (System.currentTimeMillis() - time > expect.getTimeout()) {
                LOG.fine("Until matching operation timeout");
                return (R) SimpleResult.failure(inputBuffer, true);
            }
            final InternalResult untilResult = (InternalResult) multiResult.getResults().get(0);
            boolean matched = false;
            for (int i = 0; i < multiResult.getResults().size() - 1; i++) {
                final Result result = multiResult.getResults().get(i + 1);
                if (result.isSuccessful() && untilResult.getResult().end() > result.end()) {
                    matched = true;
                    LOG.fine("Condition #" + i + " matched: " + result);
                    actions.get(i).apply(result);
                }
            }
            if (!matched && untilResult.isSuccessful()) {
                LOG.fine("Until condition is successful");
                return (R) untilResult.getResult();
            }
        }
    }

    private static class InternalResult extends SimpleResult {
        private final Result result;

        public InternalResult(final Result result, final String input) {
            super(
                    result.isSuccessful(),
                    input,
                    "",
                    "",
                    result.canStopMatching());
            this.result = result;
        }

        public Result getResult() { return result; }
    }

    private static class ResultMatcher implements Matcher<Result> {
        private final Matcher<Result> matcher;

        public ResultMatcher(final Matcher<Result> matcher) {
            this.matcher = matcher;
        }

        @Override
        public Result matches(final String input, final boolean isEof) {
            final Result result = matcher.matches(input, isEof);
            return new InternalResult(result, input);
        }

        @Override
        public String toString() {
            return matcher.toString();
        }
    }
}
