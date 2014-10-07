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

import static net.sf.expectit.matcher.SimpleResult.failure;
import static net.sf.expectit.matcher.SimpleResult.success;

import java.util.Arrays;
import java.util.regex.Pattern;
import net.sf.expectit.MultiResult;
import net.sf.expectit.Result;


/**
 * The matcher factory.
 *
 * @author Alexey Gavrilov
 */
public final class Matchers {

    /**
     * Don't expect to create an instance of this class.
     */
    private Matchers() {
    }

    /**
     * Creates a matcher of {@code String} that matches when examined input fully <b>matches</b>
     * the given regular
     * expression.
     * <p/>
     * This method simply compiles the given pattern to a {@link java.util.regex.Pattern} and
     * passes it to the
     * {@link #matches(java.util.regex.Pattern)} method.
     *
     * @param pattern the pattern that represents regular expression
     * @return the match result
     * @see #regexp(java.util.regex.Pattern)
     */
    public static Matcher<Result> matches(String pattern) {
        return matches(Pattern.compile(pattern));
    }

    /**
     * Creates a matcher of {@code Pattern} that matches when examined input fully <b>matches</b>
     * the given regular
     * expression. The operation is equivalent to the {@link java.util.regex.Matcher#matches()}
     * method.
     * <p/>
     * The returning {@code Result} implements the {@link java.util.regex.MatchResult} methods to
     * query the results
     * of the regular expression match.
     * <p/>
     * If the match succeeded, the input buffer is cleared and {@link net.sf.expectit
     * .Result#getBefore()} returns
     * an empty string.
     *
     * @param pattern the representation of a regular expression
     * @return the match result
     */
    public static Matcher<Result> matches(Pattern pattern) {
        return new RegexpMatcher(pattern, false);
    }

    /**
     * Creates a matcher of {@code String} that matches when examined input <b>contains</b> the
     * given regular
     * expression.
     * <p/>
     * This method simply compiles the given string to a {@link java.util.regex.Pattern} and
     * passes it to the
     * {@link #regexp(java.util.regex.Pattern)} method.
     *
     * @param pattern the string that represents regular expression
     * @return the match result
     * @see #regexp(java.util.regex.Pattern)
     */
    public static Matcher<Result> regexp(String pattern) {
        return regexp(Pattern.compile(pattern));
    }

    /**
     * Creates a matcher of {@code Pattern} that matches when examined input <b>contains</b> the
     * given regular
     * expression. The operation is equivalent to the {@link java.util.regex.Matcher#find()} method.
     * <p/>
     * The returning {@code Result} implements the {@link java.util.regex.MatchResult} methods to
     * query the results
     * of the regular expression match.
     * <p/>
     * If the match succeeded, the input buffer is updated: the input part from the beginning
     * until the end
     * position of the match is removed.
     *
     * @param pattern the representation of a regular expression
     * @return the match result
     */
    public static Matcher<Result> regexp(Pattern pattern) {
        return new RegexpMatcher(pattern, true);
    }

    /**
     * Creates a matcher of {@code String} that matches when examined input contains the given
     * substring.
     * <p/>
     * The returning {@code Result} has no groups except the one with {@code 0} index which
     * represents the exact
     * string match.
     * <p/>
     * If the match succeeded, the input buffer is updated: the input part from the beginning
     * until the end
     * position of the match is removed.
     *
     * @param string the string to search for
     * @return the match result
     */
    public static Matcher<Result> contains(final String string) {
        return new Matcher<Result>() {
            @Override
            public Result matches(String input, boolean isEof) {
                int pos = input.indexOf(string);
                return pos != -1 ? success(input, input.substring(0, pos), string) : failure(input);
            }

            @Override
            public String toString() {
                return generateToString("contains", string);
            }
        };
    }

    /**
     * Creates a matcher that matches if the examined input matches <b>all</b> of the specified
     * matchers. This method
     * evaluates all the matchers regardless intermediate results.
     * <p/>
     * The match result represents a combination of all match operations. If succeeded,
     * the match result with the
     * greatest end position is selected to implement the result {@link Result} instance returned
     * by this method.
     * If the result is negative, then the one which fails first is returned.
     * <p/>
     * If several matchers the have same end position, then the result from the one with the
     * smaller argument index is
     * returned.
     *
     * @param matchers the vararg array of the matchers
     * @return the multi match result
     */
    public static Matcher<MultiResult> allOf(final Matcher<?>... matchers) {
        checkNotEmpty(matchers);
        return new MultiMatcher(true, matchers);
    }

    /**
     * Creates a matcher that matches if the examined input matches <b>any</b> of the specified
     * matchers.
     * <p/>
     * The match result represents a combination of any match operations. If succeeded,
     * the match result with the
     * greatest end position is selected to implement the result {@link Result} instance returned
     * by this method.
     * <p/>
     * If several matchers have the same end position, then the result from the one with the
     * smaller argument index is
     * returned.
     *
     * @param matchers the vararg array of the matchers
     * @return the multi match result
     */
    public static Matcher<MultiResult> anyOf(final Matcher<?>... matchers) {
        checkNotEmpty(matchers);
        return new MultiMatcher(false, matchers);
    }

    /**
     * Creates a matcher that matches if input reaches the end of stream.
     * <p/>
     * If succeeded, the {@link net.sf.expectit.Result#getBefore()} will return the entire input
     * buffer, and
     * the {@link net.sf.expectit.Result#group()} returns an empty string.
     *
     * @return the matcher
     */
    public static Matcher<Result> eof() {
        return new Matcher<Result>() {
            @Override
            public Result matches(String input, boolean isEof) {
                return isEof ? success(input, input, "") : failure(input);
            }

            @Override
            public String toString() {
                return "eof";
            }
        };
    }

    /**
     * Creates a matcher that matches if the given {@code matcher} matches the {@code number} of
     * times.
     * <p/>
     * The match result represents a combination of all the performed match operations. If
     * succeeded, the result
     * with the greatest end position is returned.
     * <p/>
     *
     * @param number  the number of times which the given {@code matcher} must match the input
     * @param matcher the matcher
     * @return the result
     */
    public static Matcher<MultiResult> times(final int number, final Matcher<?> matcher) {
        Matcher<?>[] matchers = new Matcher[number];
        Arrays.fill(matchers, matcher);
        return sequence(matchers);
    }

    /**
     * Matches the given matchers one by one. Every successful matches updates the internal
     * buffer. The consequent
     * match operation is performed after the previous match has succeeded.
     *
     * @param matchers the collection of matchers.
     * @return the matcher.
     */
    public static Matcher<MultiResult> sequence(final Matcher<?>... matchers) {
        checkNotEmpty(matchers);
        return new Matcher<MultiResult>() {
            @Override
            public MultiResult matches(String input, boolean isEof) {
                int matchCount = 0;
                Result[] results = new Result[matchers.length];
                Arrays.fill(results, failure(input));
                int beginIndex = 0;
                for (int i = 0; i < matchers.length; i++) {
                    Result result = matchers[i].matches(input.substring(beginIndex), isEof);
                    if (result.isSuccessful()) {
                        beginIndex += result.end();
                        results[i] = result;
                        if (++matchCount == matchers.length) {
                            String group = result.group();
                            Result finalResult = new SimpleResult(
                                    true,
                                    input,
                                    input.substring(0, beginIndex - group.length()),
                                    group);
                            return new MultiResultImpl(finalResult, Arrays.asList(results));
                        }
                    } else {
                        break;
                    }
                }
                return new MultiResultImpl(failure(input), Arrays.asList(results));
            }

            @Override
            public String toString() {
                return String.format("sequence(%s)", MultiMatcher.matchersToString(matchers));
            }
        };
    }

    /**
     * Creates a matcher that matches when at least one character exists in the input buffer.
     * <p/>
     * If the result is successful, the {@link net.sf.expectit.Result#getBefore()} returns an
     * empty string, the
     * {@link net.sf.expectit.Result#group()} returns the entire input buffer.
     *
     * @return the result
     */
    public static Matcher<Result> anyString() {
        return new Matcher<Result>() {
            @Override
            public Result matches(String input, boolean isEof) {
                return input.length() > 0 ? success(input, "", input) : failure(input);
            }

            @Override
            public String toString() {
                return "anyString";
            }
        };
    }


    private static void checkNotEmpty(Matcher<?>[] matchers) {
        if (matchers.length == 0) {
            throw new IllegalArgumentException("Matchers cannot be empty");
        }
    }

    static String generateToString(String name, Object parameter) {
        return String.format("%s('%s')", name, parameter);
    }

    /**
     * Creates a matcher that matches when the given string is equal to the
     * input buffer contents.
     * <p/>
     * If the result is successful, the {@link net.sf.expectit.Result#getBefore()} returns an
     * empty string, the {@link net.sf.expectit.Result#group()} returns the entire input buffer.
     *
     * @param exact the string to match.
     * @return the result.
     */
    public static Matcher<Result> exact(final String exact) {
        return new Matcher<Result>() {
            @Override
            public Result matches(String input, boolean isEof) {
                return exact.equals(input) ? success(input, "", input) : failure(input);
            }

            @Override
            public String toString() {
                return "exact";
            }
        };
    }

}
