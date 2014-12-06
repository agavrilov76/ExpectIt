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

import net.sf.expectit.Result;

/**
 * A result of string matching.
 */
public class SimpleResult implements Result {
    private final boolean succeeded;
    private final String before;
    private final String group;
    private final String input;
    private final boolean canStopMatching;

    /**
     * Creates an instance with the initial field values.
     *  @param succeeded the success flag.
     * @param input the matcher`s input string.
     * @param before    the string before match, not {@code null}.
     * @param group     the string group, not {@code null}.
     * @param canStopMatching
     */
    protected SimpleResult(
            final boolean succeeded,
            final String input,
            final String before,
            final String group,
            final boolean canStopMatching) {
        this.succeeded = succeeded;
        this.input = input;
        this.before = before;
        this.group = group;
        this.canStopMatching = canStopMatching;
    }

    @Override
    public String getInput() {
        return input;
    }

    @Override
    public boolean isSuccessful() {
        return succeeded;
    }

    @Override
    public String getBefore() {
        checkSucceeded();
        return before;
    }

    @Override
    public boolean canStopMatching() {
        return canStopMatching;
    }

    @Override
    public int start() {
        return getBefore().length();
    }

    @Override
    public int start(int group) {
        checkGroup(group);
        return start();
    }

    @Override
    public int end() {
        return start() + group().length();
    }

    @Override
    public int end(int group) {
        checkGroup(group);
        return end();
    }

    @Override
    public String group() {
        checkSucceeded();
        return group;
    }

    @Override
    public String group(int group) {
        checkGroup(group);
        return group();
    }

    @Override
    public int groupCount() {
        return 0;
    }

    void checkGroup(int group) {
        if (group != 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkSucceeded() {
        if (!succeeded) {
            throw new IllegalStateException();
        }
    }

    /**
     * Creates an instance of a successful result type.
     * The {@link net.sf.expectit.Result#canStopMatching()} is always set to {@code true}.
     * @param input the matcher`s input string.
     * @param before the string before match, not {@code null}.
     * @param group the string group, not {@code null}.
     * @return the result object.
     */
    public static Result success(String input, String before, String group) {
        return new SimpleResult(true, input, before, group, true);
    }

    /**
     * Creates an instance of an unsuccessful match.
     * @param input the input string.
     * @param canStopMatching indicates whether matching operation can be stopped.
     * @return the result object.
     */
    public static Result failure(String input, boolean canStopMatching) {
        return new SimpleResult(false, input, null, null, canStopMatching);
    }

    @Override
    public String toString() {
        return "SimpleResult{"
                + "succeeded=" + succeeded
                + ", before='" + before + '\''
                + ", group='" + group + '\''
                + ", input='" + input + '\''
                + ", canStopMatching=" + canStopMatching + '}';
    }
}
