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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class WhitespacePattern extends InputScannerPattern<WhitespacePattern> {

	int newline_count;
	String whitespace_before_token;
	private @Nullable Pattern _line_regexp;
	private @Nullable Pattern _newline_regexp;

	public WhitespacePattern(InputScanner input_scanner) {
		this(input_scanner, null);
	}

	public WhitespacePattern(InputScanner input_scanner, @Nullable WhitespacePattern parent) {
		super(input_scanner, parent);

		if (parent != null) {
			this._line_regexp = parent._line_regexp;
		} else {
			this.__set_whitespace_patterns("", "");
		}

		this.newline_count = 0;
		this.whitespace_before_token = "";
	}

	private void __set_whitespace_patterns(String whitespace_chars, String newline_chars) {
		whitespace_chars += "\\t ";
		newline_chars += "\\n\\r";

		this._match_pattern = Pattern.compile(
			'[' + whitespace_chars + newline_chars + "]+");
		this._newline_regexp = Pattern.compile(
			"\\r\\n|[" + newline_chars + "]");
	}

	public String read() {
		this.newline_count = 0;
		this.whitespace_before_token = "";

		var resulting_string = this._input.read(this._match_pattern);
		if (" ".equals(resulting_string)) {
			this.whitespace_before_token = " ";
		} else if (!resulting_string.isEmpty()) {
			var matches = this.__split(this._newline_regexp, resulting_string);
			this.newline_count = matches.size() - 1;
			this.whitespace_before_token = matches.get(this.newline_count);
		}

		return resulting_string;
	}

	public WhitespacePattern matching(String whitespace_chars, String newline_chars) {
		var result = this._create();
		result.__set_whitespace_patterns(whitespace_chars, newline_chars);
		result._update();
		return result;
	}

	@Override
	protected WhitespacePattern _create() {
		return new WhitespacePattern(this._input, this);
	}

	private List<String> __split(Pattern regexp, String input_string) {
		Matcher matcher = regexp.matcher(input_string);

		var start_index = 0;
		List<String> result = new ArrayList<>();
		while (matcher.find()) {
			result.add(input_string.substring(start_index, matcher.start()));
			start_index = matcher.start() + matcher.group().length();
		}

		if (start_index < input_string.length()) {
			result.add(input_string.substring(start_index, input_string.length()));
		} else {
			result.add("");
		}

		return result;
	}

}
