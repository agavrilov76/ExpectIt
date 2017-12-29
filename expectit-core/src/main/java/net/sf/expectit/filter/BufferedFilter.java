package net.sf.expectit.filter;

import java.util.Arrays;
import java.util.List;

class BufferedFilter extends FilterAdapter {
    private String lastLine = "";

    private final List<String> lineSeparators;

    BufferedFilter(final String ... lineSeparators) {
        this.lineSeparators = Arrays.asList(lineSeparators);
    }

    @Override
    protected String doBeforeAppend(final String string, final StringBuilder buffer) {
        int pos = -1;
        for (final String lineSeparator : lineSeparators) {
            pos = string.lastIndexOf(lineSeparator);
            if (pos != -1) {
                break;
            }
        }
        final String result;
        if (pos != -1 && pos != string.length() - 1) {
            result = lastLine + string.substring(0, pos + 1);
            lastLine = string.substring(pos + 1);
        } else {
            result = lastLine + string;
            lastLine = "";
        }

        return result;
    }
}
