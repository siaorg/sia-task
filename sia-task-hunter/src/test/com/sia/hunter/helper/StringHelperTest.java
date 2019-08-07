package com.sia.hunter.helper;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import static org.mockito.Matchers.anyDouble;

@RunWith(PowerMockRunner.class)
public class StringHelperTest {

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void testRemoveEnd() {
    Assert.assertEquals(null, StringHelper.removeEnd(null, null));
    Assert.assertEquals("foo", StringHelper.removeEnd("foobar", "bar"));
    Assert.assertEquals("foobar", StringHelper.removeEnd("foobar", "baz"));
  }

  @Test
  public void testApplyRelativePath() {
    Assert.assertEquals("/", StringHelper.applyRelativePath("/", "/"));
    Assert.assertEquals("/a\'b\'c",
            StringHelper.applyRelativePath("/", "a\'b\'c"));
    Assert.assertEquals("a\'b\'c",
            StringHelper.applyRelativePath("\'", "a\'b\'c"));
  }

  @Test
  public void testCountOccurrencesOf() {
    Assert.assertEquals(1, StringHelper.countOccurrencesOf("a/b/c", "a/b/c"));
    Assert.assertEquals(0, StringHelper.countOccurrencesOf(",", "1a 2b 3c"));
    Assert.assertEquals(0, StringHelper.countOccurrencesOf("????", ""));
    Assert.assertEquals(0, StringHelper.countOccurrencesOf("", ""));
  }

  @Test
  public void testEndsWithIgnoreCase() {
    Assert.assertTrue(StringHelper.endsWithIgnoreCase("a/b/c", "a/b/c"));

    Assert.assertFalse(StringHelper.endsWithIgnoreCase("1234", "a\'b\'c"));
    Assert.assertFalse(StringHelper.endsWithIgnoreCase("a\'b\'c", "\'"));
    Assert.assertFalse(StringHelper.endsWithIgnoreCase(null, null));
  }

  @Test
  public void testGetFilenameExtension() {
    Assert.assertEquals("//", StringHelper.getFilenameExtension("..//"));

    Assert.assertNull(StringHelper.getFilenameExtension(","));
    Assert.assertNull(StringHelper.getFilenameExtension(null));
  }

  @Test
  public void testParseLocalString() {
    Assert.assertEquals(new Locale("en", "", ""),
            StringHelper.parseLocaleString("en"));
    Assert.assertEquals(new Locale("en", "UK", ""),
            StringHelper.parseLocaleString("en_UK_"));
  }

  @Test
  public void testToLanguageTag() {
    Assert.assertEquals("en",
            StringHelper.toLanguageTag(new Locale("en", "", "")));
  }

  @Test
  public void testGetFilename() {
    Assert.assertEquals("3", StringHelper.getFilename("3"));
    Assert.assertEquals("c", StringHelper.getFilename("a/b/c"));

    Assert.assertNull(StringHelper.getFilename(null));
  }

  @PrepareForTest({StringHelper.class})
  @Test
  public void testGetRandomNumber() throws Exception {
    PowerMockito.mockStatic(Math.class);
    PowerMockito.when(Math.random()).thenReturn(4.0);
    PowerMockito.when(Math.ceil(anyDouble())).thenReturn(3.0);

    Assert.assertEquals("3", StringHelper.getRandomNumber(1));
  }

  @Test
  public void testGetSubStrBeforeToken() {
    Assert.assertEquals("a/b/c",
            StringHelper.getSubStrBeforeToken("a/b/c", "3"));
    Assert.assertEquals("1a 2b ",
            StringHelper.getSubStrBeforeToken("1a 2b 3c", "3"));
  }

  @Test
  public void testDotdecod() {
    Assert.assertEquals("/u2efoobar", StringHelper.dotdecod(".foobar"));
  }

  @Test
  public void testHasText() {
    Assert.assertTrue(StringHelper.hasText("foobar"));

    Assert.assertFalse(StringHelper.hasText(""));
    Assert.assertFalse(StringHelper.hasText(" "));
  }

  @Test
  public void testContainsWhitespace() {
    Assert.assertTrue(StringHelper.containsWhitespace(" "));

    Assert.assertFalse(StringHelper.containsWhitespace(""));
    Assert.assertFalse(StringHelper.containsWhitespace("foobar"));
  }

  @Test
  public void testTrimWhitespace() {
    Assert.assertEquals("", StringHelper.trimWhitespace(""));
    Assert.assertEquals("foo bar", StringHelper.trimWhitespace(" foo bar "));
  }

  @Test
  public void testTrimAllWhitespace() {
    Assert.assertEquals("", StringHelper.trimAllWhitespace(""));
    Assert.assertEquals("foobar", StringHelper.trimAllWhitespace(" foo bar "));
  }

  @Test
  public void testTrimLeadingWhitespace() {
    Assert.assertEquals("", StringHelper.trimLeadingWhitespace(""));
    Assert.assertEquals("foobar",
            StringHelper.trimLeadingWhitespace("    foobar"));
  }

  @Test
  public void testTrimTrailingWhitespace() {
    Assert.assertEquals("", StringHelper.trimTrailingWhitespace(""));
    Assert.assertEquals("foobar",
            StringHelper.trimTrailingWhitespace("foobar    "));
  }

  @Test
  public void testTrimLeadingCharacter() {
    Assert.assertEquals("", StringHelper.trimLeadingCharacter("", 'f'));
    Assert.assertEquals("oobar",
            StringHelper.trimLeadingCharacter("foobar", 'f'));
  }

  @Test
  public void testTrimTrailingCharacter() {
    Assert.assertEquals("", StringHelper.trimTrailingCharacter("", 'f'));
    Assert.assertEquals("fooba",
            StringHelper.trimTrailingCharacter("foobar", 'r'));
  }

  @Test
  public void testReplace() {
    Assert.assertEquals("", StringHelper.replace("", "", null));
    Assert.assertEquals("twixbar",
            StringHelper.replace("marsbar", "mars", "twix"));
  }

  @Test
  public void testCapitalize() {
    Assert.assertNull(StringHelper.capitalize(null));

    Assert.assertEquals("", StringHelper.capitalize(""));
    Assert.assertEquals("Foobar", StringHelper.capitalize("foobar"));
  }

  @Test
  public void testUncapitalize() {
    Assert.assertNull(StringHelper.uncapitalize(null));

    Assert.assertEquals("", StringHelper.uncapitalize(""));
    Assert.assertEquals("foobar", StringHelper.uncapitalize("Foobar"));
  }

  @Test
  public void testDelete() {
    Assert.assertEquals("bar", StringHelper.delete("marsbar", "mars"));
  }

  @Test
  public void testDeleteAny() {
    Assert.assertEquals("", StringHelper.deleteAny("", ""));
    Assert.assertEquals("lie", StringHelper.deleteAny("linear", "nar"));
  }


  @Test
  public void testHasLength() {
    Assert.assertTrue(StringHelper.hasLength("?"));

    Assert.assertFalse(StringHelper.hasLength(""));
    Assert.assertFalse(StringHelper.hasLength(null));
  }

  @Test
  public void testIsEmpty() {
    Assert.assertTrue(StringHelper.isEmpty(""));

    Assert.assertFalse(StringHelper.isEmpty("/"));
  }

  @Test
  public void testIsNumeric() {
    Assert.assertFalse(StringHelper.isNumeric(""));
    Assert.assertFalse(StringHelper.isNumeric(null));
    Assert.assertFalse(StringHelper.isNumeric("foo"));
  }

  @Test
  public void testJoin() {
    Collection<String> collection = new ArrayList<>(Arrays.asList("foo!bar"));

    Assert.assertEquals("foo!bar", StringHelper.join(collection, "!"));
    Assert.assertEquals("", StringHelper.join(new String[]{}, ","));
    Assert.assertEquals("", StringHelper.join(new String[]{null}, "3"));
    Assert.assertEquals("\'", StringHelper.join(new String[]{"\'"}, "3"));
    Assert.assertEquals("?", StringHelper.join(new String[]{"?"}, null));
    Assert.assertEquals("foo",
            StringHelper.join(new String[]{null, null}, "foo"));

    Assert.assertNull(StringHelper.join((String[]) null, ","));
    Assert.assertNull(StringHelper.join((Collection<String>) null, "3"));
  }

  @Test
  public void testQuoteIfString() {
    Assert.assertNull(StringHelper.quoteIfString(null));
  }

  @Test
  public void testQuote() {
    Assert.assertEquals("\'1234\'", StringHelper.quote("1234"));
  }

  @Test
  public void testStartsWithIgnoreCase() {
    Assert.assertTrue(StringHelper.startsWithIgnoreCase("1", "1"));

    Assert.assertFalse(StringHelper.startsWithIgnoreCase("1", "3"));
    Assert.assertFalse(StringHelper.startsWithIgnoreCase("1", "a\'b\'c"));
    Assert.assertFalse(StringHelper.startsWithIgnoreCase(null, null));
  }

  @Test
  public void testStripFilenameExtension() {
    Assert.assertNull(StringHelper.stripFilenameExtension(null));

    Assert.assertEquals(",,", StringHelper.stripFilenameExtension(",,./"));
    Assert.assertEquals("a\'b\'c",
            StringHelper.stripFilenameExtension("a\'b\'c"));
  }

  @Test
  public void testSubstringMatchThrowsException1() {
    thrown.expect(StringIndexOutOfBoundsException.class);
    StringHelper.substringMatch("?", -22, "\u0000\u0000");
  }

  @Test
  public void testSubstringMatchThrowsException2() {
    thrown.expect(NullPointerException.class);
    StringHelper.substringMatch("?", -22, null);
  }

  @Test
  public void testSubstringMatch() {
    Assert.assertTrue(StringHelper.substringMatch(null, -22, ""));

    Assert.assertFalse(StringHelper.substringMatch("\f\f", 4, "\u0000\u0000"));
    Assert.assertFalse(StringHelper.substringMatch("\f\f", 21, "\u0000"));
  }

  @Test
  public void testTokenizeToStringArray() {
    Assert.assertArrayEquals(new String[] {"foo", "bar"},
            StringHelper.tokenizeToStringArray("foo bar", " "));

    Assert.assertNull(
            StringHelper.tokenizeToStringArray(null, "?", true, true));
  }

  @Test
  public void testToLanguageTagThrowsException() {
    thrown.expect(NullPointerException.class);
    StringHelper.toLanguageTag(null);
  }

  @Test
  public void testToStringArray() {
    Enumeration<String> enumeration = Collections.enumeration(
            new ArrayList<String>(Arrays.asList("foo")));

    Assert.assertArrayEquals(new String[] {"Bar"},
            StringHelper.toStringArray(new ArrayList<>(Arrays.asList("Bar"))));
    Assert.assertArrayEquals(new String[] {"foo"},
            StringHelper.toStringArray(enumeration));

    Assert.assertNull(StringHelper.toStringArray((Enumeration<String>) null));
    Assert.assertNull(StringHelper.toStringArray((Collection<String>) null));
  }

  @Test
  public void testSplit() {
    Assert.assertArrayEquals(new String[] {"foo", "bar"},
            StringHelper.split("foo bar", " "));

    Assert.assertNull(StringHelper.split("foobar", ""));
    Assert.assertNull(StringHelper.split("foobar", "baz"));
  }

  @Test
  public void testDelimitedListToStringArray() {
    Assert.assertArrayEquals(new String[]{"foo", "bar"},
            StringHelper.delimitedListToStringArray("foo bar", " "));
    Assert.assertArrayEquals(new String[0],
            StringHelper.delimitedListToStringArray(null, ""));
    Assert.assertArrayEquals(new String[]{"f"},
            StringHelper.delimitedListToStringArray("f", ""));
    Assert.assertArrayEquals(new String[]{""},
            StringHelper.delimitedListToStringArray("", null));
  }

  @Test
  public void testCommaDelimitedListToStringArray() {
    Assert.assertArrayEquals(new String[]{"foo", "bar"},
            StringHelper.commaDelimitedListToStringArray("foo,bar"));
  }

  @Test
  public void testCommaDelimitedListToSet() {
    Set<String> set = new TreeSet<>();
    set.add("foo");
    set.add("bar");

    Assert.assertEquals(set, StringHelper.commaDelimitedListToSet("foo,bar"));
  }

  @Test
  public void testUnqualify() {
    Assert.assertEquals(",", StringHelper.unqualify(","));
    Assert.assertEquals(",", StringHelper.unqualify(",", '\''));
  }
}
