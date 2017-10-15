package net.sf.expectit.filter;

class BufferedFilter extends FilterAdapter {
    private String lastLine = "";

    private final String lineSeparator;

    BufferedFilter(final String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }

    @Override
    protected String doBeforeAppend(final String string, final StringBuilder buffer) {
        int pos = string.lastIndexOf(lineSeparator);
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
