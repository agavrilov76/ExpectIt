package net.sf.expectit.filter;


import static org.junit.Assert.assertEquals;

import java.util.regex.Pattern;
import org.junit.Test;

public class EscapeSequenceFilterTest {

    private static final Pattern PATTERN = Pattern.compile("\\x1b.*m");

    @Test
    public void doBeforeAppend() throws Exception {
        final EscapeSequenceFilter filter = new EscapeSequenceFilter('\u001b', "m",
                PATTERN);

        assertEquals(filter.doBeforeAppend("ABC", null), "ABC");
        assertEquals(filter.doBeforeAppend("\u001b\\[m", null), "");
        assertEquals(filter.doBeforeAppend("AB\u001b\\[XYZmC", null), "ABC");

        assertEquals(filter.doBeforeAppend("\u001b\\[mA\u001b\\[m", null), "A");
        assertEquals(filter.doBeforeAppend("AB\u001b\\[XYZmC\u001b\\[XmD", null), "ABCD");
    }

    @Test
    public void doBeforeAppend2() throws Exception {
        final EscapeSequenceFilter filter =
                new EscapeSequenceFilter('r', "ve", Pattern.compile("remove"));
        //assertEquals(filter.doBeforeAppend("removeSome text hereremove", null), "Some text here");

        assertEquals(filter.doBeforeAppend("re", null), "");
        assertEquals(filter.doBeforeAppend("moveSome text here", null), "Some text he");
        assertEquals(filter.doBeforeAppend("", null), "re");
    }


    @Test
    public void doBeforeAppendPartial() throws Exception {
        final EscapeSequenceFilter filter = new EscapeSequenceFilter('\u001b', "m", PATTERN);

        assertEquals(filter.doBeforeAppend("X\u001b\\[Y", null), "X");
        assertEquals(filter.doBeforeAppend("ABCmZ", null), "Z");
        assertEquals(filter.doBeforeAppend("ABCmZ", null), "ABCmZ");
    }
}