package net.sf.expectit.matcher;

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

import net.sf.expectit.Result;

/**
 * A result of string matching..
 */
class SimpleResult implements Result {
    public static final Result NEGATIVE = new SimpleResult(false, null, null);

    private final boolean succeeded;
    private final String before;
    private final String group;

    public SimpleResult(boolean succeeded, String before, String group) {
        this.succeeded = succeeded;
        this.before = before;
        this.group = group;
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
}
