package io.beautifier.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class InputScannerTest {
	
	@Test
	void newInputScanner() {
		assertFalse(new InputScanner().hasNext(), "new should return empty scanner when input is not present");
	}

	@Test
	void next() {
		{
			var value = "howdy";
			var inputText = new InputScanner(value);
			assertEquals(value.substring(0, 1), inputText.next(), "next should return the value at current index and increments the index");
			assertEquals(value.substring(1, 2), inputText.next(), "next should return the value at current index and increments the index");
		}

		{
			var value = "howdy";
			var inputText = new InputScanner(value);
			inputText.readUntilAfter(Pattern.compile("howdy"));
			assertNull(inputText.next(), "next should return null if index is at then end of the value");
		}
	}

	@Test
	void peek() {
		{
			var value = "howdy";
			var inputText = new InputScanner(value);
			assertEquals(value.substring(3, 4), inputText.peek(3), "peek should return value at index passed as parameter");
			inputText.next();
			assertEquals(value.substring(4, 5), inputText.peek(3), "peek should return value at index passed as parameter");
		}

		{
			var value = "howdy";
			var inputText = new InputScanner(value);
			assertNull(inputText.peek(-2), "peek should return null if index is less than 0 or greater than text length");
			assertNull(inputText.peek(5), "peek should return null if index is less than 0 or greater than text length");
		}
	}

	@Test
	void peekWithoutParameters() {
		var value = "howdy";
		var inputText = new InputScanner(value);
		assertEquals(value.substring(0, 1), inputText.peek(), "peek without parameters should return value at index 0 if parameter is not present");
		inputText.next();
		assertEquals(value.substring(4, 5), inputText.peek(3), "should return value at index 0 if parameter is not present");
	}

	@Test
	void test() {
		var value = "howdy";
		var pattern = Pattern.compile("how");
		var index = 0;
		var inputText = new InputScanner(value);
		assertTrue(inputText.test(pattern, index), "test should return whether the pattern is matched or not");
		inputText.next();
		assertFalse(inputText.test(pattern, index));
	}

	@Test
	void testChar() {
		var value = "howdy";
		var pattern = Pattern.compile("o");
		var index = 1;
		var inputText = new InputScanner(value);
		assertTrue(inputText.testChar(pattern, index));
	}

	@Test
	void restart() {
		var value = "howdy";
		var inputText = new InputScanner(value);
		inputText.next();
		assertEquals(value.substring(1, 2), inputText.peek(), "restart should reset index to 0");
		inputText.restart();
		assertEquals(value.substring(0, 1), inputText.peek(), "restart should reset index to 0");
	}

	@Test
	void back() {
		{
			var value = "howdy";
			var inputText = new InputScanner(value);
			inputText.next();
			assertEquals(value.substring(1, 2), inputText.peek(), "back should move the index one place back if current position is not 0");
			inputText.back();
			assertEquals(value.substring(0, 1), inputText.peek(), "back should move the index one place back if current position is not 0");
		}

		{
			var value = "howdy";
			var inputText = new InputScanner(value);
			assertEquals(value.substring(0, 1), inputText.peek(), "back should not move the index back if current position is 0");
			inputText.back();
			assertEquals(value.substring(0, 1), inputText.peek(), "back should not move the index back if current position is 0");
		}
	}

	@Test
	void hasNext() {
		{
			var value = "howdy";
			var inputText = new InputScanner(value);
			inputText.readUntilAfter(Pattern.compile("howd"));
			assertTrue(inputText.hasNext(), "hasNext should return true if index is not at the last position");
		}

		{
			var value = "howdy";
			var inputText = new InputScanner(value);
			inputText.readUntilAfter(Pattern.compile("howdy"));
			assertFalse(inputText.hasNext(), "hasNext should return false if index is at the last position");
		}
	}

	@Test
	void match() {
		{
			var value = "howdy";
			var inputText = new InputScanner(value);
			var patternmatch = inputText.match(Pattern.compile("how"));
			assertEquals(inputText.peek(), value.substring(3, 4));
			assertNotNull(patternmatch, "match should return details of pattern match and move index to next position");
			assertEquals("how", patternmatch.group(0), "match should return details of pattern match and move index to next position");
		}

		{
			var value = "howdy";
			var inputText = new InputScanner(value);
			var patternmatch = inputText.match(Pattern.compile("test"));
			assertEquals(value.substring(0, 1), inputText.peek(), "match should return null and not move index if there is no match");
			assertNull(patternmatch, "match should return null and not move index if there is no match");
		}
	}

	@Test
	void read() {
		{
			var inputText = new InputScanner("howdy");
			var patternmatch = inputText.read(Pattern.compile("how"));
			assertEquals("how", patternmatch, "read should return the matched substring");
		}

		{
			var inputText = new InputScanner("howdy");
			var patternmatch = inputText.read(Pattern.compile("ow"));
			assertEquals("", patternmatch, "read should return the empty string if there is no match");
		}

		{
			var inputText = new InputScanner("howdy");
			var startPattern = Pattern.compile("how");
			var untilPattern = Pattern.compile("dy");
			var untilAfter = true;
			var patternmatch = inputText.read(startPattern, untilPattern, untilAfter);
			assertEquals("howdy", patternmatch, "read should return substring from start to until pattern when unitilAfter is true");
		}

		{
			var inputText = new InputScanner("howdy");
			var startPattern = Pattern.compile("how");
			var untilPattern = Pattern.compile("dy");
			var untilAfter = false;
			var patternmatch = inputText.read(startPattern, untilPattern, untilAfter);
			assertEquals("how", patternmatch, "read should return the substring matched for startPattern when untilPattern is given but unitilAfter is false");
		}

		{
			var inputText = new InputScanner("howdy");
			Pattern startPattern = null;
			var untilPattern = Pattern.compile("how");
			var untilAfter = true;
			var patternmatch = inputText.read(startPattern, untilPattern, untilAfter);
			assertEquals("how", patternmatch, "read should return substring matched for untilPattern when startPattern is null");
		}

		{
			var inputText = new InputScanner("howdy");
			Pattern startPattern = null;
			var untilPattern = Pattern.compile("how");
			var untilAfter = false;
			var patternmatch = inputText.read(startPattern, untilPattern, untilAfter);
			assertEquals("", patternmatch, "read should return substring matched for untilPattern when startPattern is null and untilAfter is false");
		}
	}

	@Test
	void readUntil() {
		{
			var inputText = new InputScanner("howdy");
			var pattern = Pattern.compile("how");
			var untilAfter = true;
			var patternmatch = inputText.readUntil(pattern, untilAfter);
			assertEquals("how", patternmatch, "readUntil should return substring matched for pattern when untilAfter is true");
		}

		{
			var inputText = new InputScanner("howdy");
			var pattern = Pattern.compile("wd");
			var untilAfter = false;
			var patternmatch = inputText.readUntil(pattern, untilAfter);
			assertEquals("ho", patternmatch, "readUntil should return substring from index 0 to start index of matched substring when untilAfter is false");
		}

		{
			var inputText = new InputScanner("howdy");
			var pattern = Pattern.compile("how");
			var untilAfter = false;
			var patternmatch = inputText.readUntil(pattern, untilAfter);
			assertEquals("", patternmatch, "readUntil should return empty string when start index of matched substring is 0 and untilAfter is false");
		}
	}

	@Test
	void readUntilAfter() {
		var inputText = new InputScanner("howdy");
		var pattern = Pattern.compile("how");
		var patternmatch = inputText.readUntilAfter(pattern);
		assertEquals("how", patternmatch, "readUntilAfter should return matched substring");
	}

	@Test
	void peekUntilAfter() {
		var value = "howdy";
		var inputText = new InputScanner(value);
		var pattern = Pattern.compile("how");
		assertEquals(value.substring(0, 1), inputText.peek(), "peekUntilAfter should return matched substring and retain index position");
		assertEquals("how", inputText.peekUntilAfter(pattern), "peekUntilAfter should return matched substring and retain index position");
		assertEquals(value.substring(0, 1), inputText.peek(), "peekUntilAfter should return matched substring and retain index position");
	}

	@Test
	void lookBack() {
		var inputText = new InputScanner("howdy");
		var testVal = "how";
		inputText.readUntilAfter(Pattern.compile("howd"));
		assertTrue(inputText.lookBack(testVal), "lookBack should return whether testVal is obtained by shifting index to the left");
		testVal = "ho";
		assertFalse(inputText.lookBack(testVal), "lookBack should return whether testVal is obtained by shifting index to the left");
	}

}
