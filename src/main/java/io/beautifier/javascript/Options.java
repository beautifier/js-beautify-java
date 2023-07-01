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

package io.beautifier.javascript;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class Options extends io.beautifier.core.Options {

	/**
	 * For the {@code preserve-inline} style, see {@link #brace_preserve_inline}.
	 */
	public enum BraceStyle {
		collapse,
		expand,
		endExpand,
		none,
	}

	public enum OperatorPosition {
		beforeNewline,
		afterNewline,
		preserveNewline,
	}

	BraceStyle brace_style = BraceStyle.collapse;
	boolean brace_preserve_inline;
	boolean unindent_chained_methods;
	boolean break_chained_methods;
	boolean space_in_paren;
	boolean space_in_empty_paren;
	boolean jslint_happy;
	boolean space_after_anon_function;
	boolean space_after_named_function;
	boolean keep_array_indentation;
	boolean space_before_conditional;
	boolean unescape_strings;
	boolean e4x;
	boolean comma_first;
	OperatorPosition operator_position = OperatorPosition.beforeNewline;
	// For testing of beautify preserve:start directive
	boolean test_output_raw;

	@Override
	public void prepare() {
		super.prepare();

		// force this._options.space_after_anon_function to true if this._options.jslint_happy
		if (this.jslint_happy) {
			this.space_after_anon_function = true;
		}
	}
	
}
