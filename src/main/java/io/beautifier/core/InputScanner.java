/*

  The MIT License (MIT)

  Copyright (c) 2007-2018 Einar Lielmanis, Liam Newman, and contributors.

  Permission is hereby granted, free of charge, to any person
  obtaining a copy of this software and associated documentation files
  (the "Software"), to deal in the Software without restriction,
  including without limitation the rights to use, copy, modify, merge,
  publish, distribute, sublicense, and/or sell copies of the Software,
  and to permit persons to whom the Software is furnished to do so,
  subject to the following conditions:

  The above copyright notice and this permission notice shall be
  included in all copies or substantial portions of the Software.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
  BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
  ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  SOFTWARE.
*/

package io.beautifier.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class InputScanner {

	private String __input;
	private int __input_length;
	private int __position = 0;

	public InputScanner() {
		this(null);
	}
	
	public InputScanner(String input_string) {
		this.__input = input_string != null ? input_string : "";
		this.__input_length = this.__input.length();
		this.__position = 0;
	}

	public void restart() {
		this.__position = 0;
	}

	public void back() {
		if (this.__position > 0) {
			this.__position -= 1;
		}
	}

	public boolean hasNext() {
		return this.__position < this.__input_length;
	}

	public @Nullable String next() {
		if (this.hasNext()) {
			Character val = this.__input.charAt(this.__position);
			this.__position += 1;
			return Character.toString(val);
		} else {
			return null;
		}
	}

	public @Nullable String peek() {
		return peek(0);
	}

	public @Nullable String peek(int index) {
		Character val = null;
		index += this.__position;
		if (index >= 0 && index < this.__input_length) {
			return Character.toString(this.__input.charAt(index));
		} else {
			return null;
		}
	}

	// This is a JavaScript only helper function (not in python)
	// Javascript doesn't have a match method
	// and not all implementation support "sticky" flag.
	// If they do not support sticky then both this.match() and this.test() method
	// must get the match and check the index of the match.
	// If sticky is supported and set, this method will use it.
	// Otherwise it will check that global is set, and fall back to the slower method.
	private @Nullable Matcher __match(Pattern pattern, int index) {
		Matcher matcher = pattern.matcher(__input);
		matcher.region(index, __input_length);

		if (matcher.lookingAt()) {
			return matcher;
		} else {
			return null;
		}
	}

	public boolean test(Pattern pattern, int index) {
		index += this.__position;

		if (index >= 0 && index < this.__input_length) {
			return this.__match(pattern, index) != null;
		} else {
			return false;
		}
	}

	public boolean testChar(Pattern pattern) {
		return testChar(pattern, 0);
	}

	public boolean testChar(Pattern pattern, int index) {
		// test one character regex match
		String val = this.peek(index);
		return val != null && pattern.matcher(val).matches();
	}

	public @Nullable Matcher match(Pattern pattern) {
		Matcher pattern_match = this.__match(pattern, this.__position);
		if (pattern_match != null) {
			this.__position += pattern_match.group().length();
			return pattern_match;
		} else {
			return null;
		}
	}

	public String read(@Nullable Pattern starting_pattern) {
		return read(starting_pattern, null, false);
	}

	public String read(@Nullable Pattern starting_pattern, @Nullable Pattern until_pattern) {
		return read(starting_pattern, until_pattern, false);
	}

	public String read(@Nullable Pattern starting_pattern, @Nullable Pattern until_pattern, boolean until_after) {
		final StringBuilder val = new StringBuilder();
		Matcher match = null;
		if (starting_pattern != null) {
			match = this.match(starting_pattern);
			if (match != null) {
				val.append(match.group());
			}
		}
		if (until_pattern != null && (match != null || starting_pattern == null)) {
			val.append(this.readUntil(until_pattern, until_after));
		}
		return val.toString();
	}

	public String readUntil(Pattern pattern) {
		return readUntil(pattern, false);
	}

	public String readUntil(Pattern pattern, boolean until_after) {
		var match_index = this.__position;

		var pattern_match = pattern.matcher(this.__input);
		if (pattern_match.find(this.__position)) {
			match_index = pattern_match.start();
			if (until_after) {
				match_index += pattern_match.group().length();
			}
		} else {
			match_index = this.__input_length;
		}

		var val = this.__input.substring(this.__position, match_index);
		this.__position = match_index;
		return val;
	}

	public String readUntilAfter(Pattern pattern) {
		return this.readUntil(pattern, true);
	}

	/* css beautifier legacy helpers */
	public String peekUntilAfter(Pattern pattern) {
		var start = this.__position;
		var val = this.readUntilAfter(pattern);
		this.__position = start;
		return val;
	}

	public boolean lookBack(String testVal) {
		var start = this.__position - 1;
		return start >= testVal.length() && this.__input.substring(start - testVal.length(), start)
			.toLowerCase().equals(testVal);
	}
}
