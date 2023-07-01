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
public class InputScannerPattern {

	protected InputScanner _input;
	protected @Nullable Pattern _starting_pattern;
	protected @Nullable Pattern _match_pattern;
	protected @Nullable Pattern _until_pattern;
	protected boolean _until_after;

	public InputScannerPattern(InputScanner input_scanner) {
		this(input_scanner, null);
	}
	
	public InputScannerPattern(InputScanner input_scanner, @Nullable InputScannerPattern parent) {
		this._input = input_scanner;
		this._starting_pattern = null;
		this._match_pattern = null;
		this._until_pattern = null;
		this._until_after = false;

		if (parent != null) {
			this._starting_pattern = parent._starting_pattern;
			this._match_pattern = parent._match_pattern;
			this._until_pattern = parent._until_pattern;
			this._until_after = parent._until_after;
		}
	}

	public String read() {
		var result = this._input.read(this._starting_pattern);
		if (this._starting_pattern == null || !result.isEmpty()) {
			result += this._input.read(this._match_pattern, this._until_pattern, this._until_after);
		}
		return result;
	}

	public @Nullable Matcher read_match() {
		final Pattern matchPattern = this._match_pattern;
		if (matchPattern != null) {
			return this._input.match(matchPattern);
		} else {
			return null;
		}
	}

	public InputScannerPattern until_after(Pattern pattern) {
		var result = this._create();
		result._until_after = true;
		result._until_pattern = pattern;
		result._update();
		return result;
	}

	public InputScannerPattern until(Pattern pattern) {
		var result = this._create();
		result._until_after = false;
		result._until_pattern = pattern;
		result._update();
		return result;
	}

	public InputScannerPattern starting_with(Pattern pattern) {
		var result = this._create();
		result._starting_pattern = pattern;
		result._update();
		return result;
	}

	public InputScannerPattern matching(Pattern pattern) {
		var result = this._create();
		result._match_pattern = pattern;
		result._update();
		return result;
	}

	protected InputScannerPattern _create() {
		return new InputScannerPattern(this._input, this);
	}

	protected void _update() {

	}
}
