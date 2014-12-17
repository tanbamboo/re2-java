package com.logentries.re2_test;

import com.logentries.re2.*;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.regex.MatchResult;

import static org.junit.Assert.*;


public class TestMatcherFind {


    @Test
    public void testFindSimple() throws Exception {

        RE2 regex = new RE2("(www\\.)?dandelion\\.eu");

        RE2Matcher matcher = regex.matcher("https://dandelion.eu/datatxt");
        assertTrue(matcher.find());
        assertEquals(8, matcher.start());
        assertEquals(20, matcher.end());
        assertEquals("dandelion.eu", matcher.group());
        assertEquals(8, matcher.start(0));
        assertEquals(20, matcher.end(0));
        assertEquals("dandelion.eu", matcher.group(0));
        assertEquals(-1, matcher.start(1));
        assertEquals(-1, matcher.end(1));
        assertNull(matcher.group(1));

    }
    @Test
    public void testFindNoGroups() throws Exception {

        RE2 regex = new RE2("(www\\.)?dandelion\\.eu");

        RE2Matcher matcher = regex.matcher("https://www.dandelion.eu/datatxt", false);
        assertTrue(matcher.find());
        assertEquals("www.dandelion.eu", matcher.group());
        assertEquals(1, matcher.groupCount());
    }

    @Test
    public void testMatchGroups() throws Exception {
        RE2 regex = new RE2("(www\\.)?dandelion\\.(eu)");
        RE2Matcher matcher = regex.matcher("€€ https://dandelion.eu/datatxt - www.dandelion.eu/datatxt");
        assertTrue(matcher.findNext());
        assertEquals("dandelion.eu", matcher.group());
        assertNull(matcher.group(1));
        assertEquals("eu", matcher.group(2));

        assertTrue(matcher.findNext());
        assertEquals("www.dandelion.eu", matcher.group());
        assertEquals("www.",matcher.group(1));
        assertEquals("eu", matcher.group(2));

        assertFalse(matcher.findNext());
    }
    @Test
    public void testFindNext() throws Exception {
        RE2 regex = new RE2("(www\\.)?dandelion\\.(eu)");
        RE2Matcher matcher = regex.matcher("€€ https://dandelion.euwww.dandelion.eu");
        assertTrue(matcher.findNext());
        assertEquals("dandelion.eu", matcher.group());
        assertNull(matcher.group(1));
        assertEquals("eu", matcher.group(2));

        assertTrue(matcher.findNext());
        assertEquals("www.dandelion.eu", matcher.group());
        assertEquals("www.",matcher.group(1));
        assertEquals("eu", matcher.group(2));

        assertFalse(matcher.findNext());
    }

    @Test(expected = IllegalStateException.class)
    public void testFindGroupOverflow() throws Exception {

        RE2 regex = new RE2("(www\\.)?dandelion\\.eu");

        RE2Matcher matcher = regex.matcher("https://dandelion.eu/datatxt");
        assertTrue(matcher.find());
        matcher.group(2);

    }

    @Test
    public void testFindStart() throws Exception {

        RE2 regex = new RE2("(www\\.)?dandelion\\.eu");

        RE2Matcher matcher = regex.matcher("Datatxt: https://dandelion.eu/datatxt - the named entity extraction tool by Spaziodati");
        assertTrue(matcher.find());
        assertTrue(matcher.find(17));
        assertFalse(matcher.find(18));
        assertFalse(matcher.find(40));
    }

    @Test
    public void testFindEnd() throws Exception {

        RE2 regex = new RE2("(www\\.)?dandelion\\.eu");

        RE2Matcher matcher = regex.matcher("Datatxt: https://dandelion.eu/datatxt -");
        assertTrue(matcher.find());
        assertTrue(matcher.find(17,29));
        assertFalse(matcher.find(18,29));
        assertTrue(matcher.find(0, 39));
    }

    @Test
    public void testOffsetSpecialChars() throws Exception {

        RE2 regex = new RE2("dandelion\\.eu");

        String[] input = {
            "Dàtàtxt: https://dandelion.eu/datatxt - ", //offset 2
            "D€t€t€€: https://dandelion.eu/datatxt - ", //offset 3
            "€€€€€€€: https://dandelion.eu/datatxt €€€", //offset 3
        };

        for (String i : input) {
            RE2Matcher matcher = regex.matcher(i);
            assertTrue(i, matcher.find());
            assertEquals(i, "dandelion.eu", matcher.group());
            assertTrue(i, matcher.find(17));
            assertEquals(i, "dandelion.eu", matcher.group());
            assertFalse(i, matcher.find(18));
        }

    }
    @Test
    public void testSurrogateChars() throws Exception {

        RE2 regex = new RE2("(www\\.)?dandelion\\.eu");

        String[] input = {
            "D\uD801\uDC28t\uD801\uDC28t\uD801\uDC28€: https://dandelion.eu/datatxt - ", //surrogate
            "D\uD83D\uDC3Et\uD83D\uDC3Et\uD83D\uDC3E€: https://dandelion.eu/datatxt - ", //surrogate
        };

        for (String i : input) {
            RE2Matcher matcher = regex.matcher(i);
            assertTrue(i, matcher.find());
            assertEquals(i, "dandelion.eu", matcher.group());
            assertTrue(i, matcher.find(20));
            assertEquals(i, "dandelion.eu", matcher.group());
            assertFalse(i, matcher.find(21));
        }

    }


    @Test
    public void testEmptyStrings() throws Exception {
        RE2 regex = new RE2("(www\\.)?dandelion\\.eu");
        assertFalse(regex.matcher("").find());
        assertFalse(regex.matcher("a").find());
        assertFalse(regex.matcher("€").find());
    }

    @Test()
    public void testIterator() throws Exception {
        int c = 0;
        for (MatchResult mr : new RE2("t").matcher("input text")) c++;
        assertEquals(3, c);
    }



    @Test(expected = IllegalStateException.class)
    public void testClosed() throws Exception {
        RE2Matcher m = new RE2("test").matcher("input text");
        m.close();
        m.find();
    }

    @Test()
    public void testTryWith() throws Exception {
        RE2 r = new RE2("t");
        try (RE2Matcher m = r.matcher("input text")) {
            assertTrue(m.findNext());
            assertTrue(m.findNext());
            assertTrue(m.findNext());
            assertFalse(m.findNext());
        }
    }


    @Test(expected = IllegalStateException.class)
    public void testReClosed() throws Exception {
        RE2 regex = new RE2("test");
        RE2Matcher m = regex.matcher("input text");
        regex.close();
        m.find();
    }

    @Test
    public void testOptionsList() throws Exception {
        RE2 regex = new RE2("TGIF?",
            Options.CASE_INSENSITIVE,
            Options.ENCODING(Encoding.UTF8),
            Options.PERL_CLASSES(false)
        );
    }

    @Test
    public void testMoreGroups() throws Exception {
        String pattern = "";
        char c = 'a';
        for (int i=0; i<25; i++) {
            char cnext = (char)(c+i);
            if (i>0) pattern += "|("+cnext+")";
            else pattern += "("+ cnext +")";
        }

        RE2Matcher matcher = new RE2(pattern).matcher("a very beatiful string");
        assertTrue(matcher.findNext()); //a
        assertTrue(matcher.findNext()); //v
        assertEquals("v", matcher.group());
        assertEquals("v", matcher.group('v' - 'a' + 1));
    }

    static String rnd(int len) {
        Random r = new Random();
        String s = new String();
        for (int i=0; i<len; i++)
            s += (char)('a' + r.nextInt('z'-'a'));
        return s;
    }
    @Test
    public void testString() {
        Random r = new Random();
        for (int i=0; i<1000; i++) {
            String s = rnd(20+r.nextInt(1000));
            int l = 3 + r.nextInt(5);
            String regex = s.substring(s.length()-l)+"\\b";
            RE2 re = RE2.compile(regex);
            Assert.assertTrue("i:"+i+" len:"+s.length() + " re:"+regex,
                    re.matcher(s).find(s.length()-l-5, s.length()));
        }

    }
}
