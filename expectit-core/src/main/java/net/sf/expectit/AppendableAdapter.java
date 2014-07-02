package net.sf.expectit;

import java.io.IOException;

/**
 * An adapter class making unused methods throw exceptions.
 */
abstract class AppendableAdapter implements Appendable {
    @Override
    public Appendable append(CharSequence csq, int start, int end) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Appendable append(char c) throws IOException {
        throw new UnsupportedOperationException();
    }
}
