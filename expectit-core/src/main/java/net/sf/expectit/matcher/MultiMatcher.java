package net.sf.expectit.matcher;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.sf.expectit.MultiResult;
import net.sf.expectit.Result;

/**
 * A class to accommodate results of the multiple matches.
 *
 * @author Alexey Gavrilov
 */
class MultiMatcher implements Matcher<MultiResult> {
    private final Matcher<?>[] matchers;
    private final boolean allOperation;

    public MultiMatcher(boolean allOperation, Matcher<?>... matchers) {
        this.matchers = matchers;
        this.allOperation = allOperation;
    }

    @Override
    public MultiResult matches(String input, boolean isEof) {
        List<Result> results = new ArrayList<Result>();
        List<Result> successResults = new ArrayList<Result>();
        Result firstFailResult = null;
        for (Matcher<?> matcher : matchers) {
            Result result = matcher.matches(input, isEof);
            if (result.isSuccessful()) {
                successResults.add(result);
            } else if (firstFailResult == null) {
                firstFailResult = result;
            }
            results.add(result);
        }

        Result delegate;
        if (allOperation) {
            delegate = getAllOfResult(successResults, firstFailResult);
        } else {
            delegate = getAnyOfResult(successResults, results);
        }
        return new MultiResultImpl(delegate, results);
    }

    private Result getAllOfResult(List<Result> successResults, Result firstFailResult) {
        if (firstFailResult == null) {
            return findResultWithMaxEnd(successResults);
        }
        return firstFailResult;
    }

    private Result getAnyOfResult(List<Result> successResults, List<Result> results) {
        if (successResults.size() > 0) {
            return findResultWithMaxEnd(successResults);
        } else {
            return results.get(0);
        }
    }

    /**
     * Find the result with the maximum end position and use it as delegate.
     */
    private static Result findResultWithMaxEnd(List<Result> successResults) {
        return Collections.max(
                successResults, new Comparator<Result>() {
                    @Override
                    public int compare(Result o1, Result o2) {
                        return Integer.valueOf(o1.end()).compareTo(o2.end());
                    }
                });
    }

    @Override
    public String toString() {
        StringBuilder matchersString = new StringBuilder();
        for (Matcher<?> matcher : matchers) {
            if (matchersString.length() > 0) {
                matchersString.append(',');
            }
            matchersString.append(matcher.toString());
        }
        if (allOperation) {
            return String.format("allOf(%s)", matchersString);
        }
        return String.format("anyOf(%s)", matchersString);
    }
}
