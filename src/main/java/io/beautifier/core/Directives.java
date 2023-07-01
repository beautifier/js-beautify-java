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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class Directives {
	
	private Pattern __directives_block_pattern;
	private Pattern __directive_pattern;
	private Pattern __directives_end_ignore_pattern;

	public Directives(String start_block_pattern, String end_block_pattern) {
		this.__directives_block_pattern = Pattern.compile(start_block_pattern + " beautify( \\w+[:]\\w+)+ " + end_block_pattern);
		this.__directive_pattern = Pattern.compile(" (\\w+)[:](\\w+)");
		this.__directives_end_ignore_pattern = Pattern.compile(start_block_pattern + "\\sbeautify\\signore:end\\s" + end_block_pattern);
	}

	public Directives(Pattern start_block_pattern, Pattern end_block_pattern) {
		this(start_block_pattern.pattern(), end_block_pattern.pattern());
	}

	public @Nullable Map<String, String> get_directives(String text) {
		Matcher matcher = this.__directives_block_pattern.matcher(text);
		if (!matcher.matches()) {
			return null;
		}

		final Map<String, String> directives = new HashMap<>();

		matcher = __directive_pattern.matcher(text);
		while (matcher.find()) {
			directives.put(matcher.group(1), matcher.group(2));
		}

		return directives;
	}

	public String readIgnored(InputScanner input) {
		return input.readUntilAfter(this.__directives_end_ignore_pattern);
	}
	
}
