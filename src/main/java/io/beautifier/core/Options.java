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

import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class Options {

	public enum TemplateLanguage {
		auto,
		none,
		django,
		erb,
		handlebars,
		php,
		smarty,
	}

	public boolean disabled;
	public String eol = "auto";
	public boolean end_with_newline;
	public int indent_size = 4;
	public String indent_char = " ";
	public int indent_level;
	public boolean preserve_newlines = true;
	public int max_preserve_newlines = 32768;
	public boolean indent_with_tabs;
	public int wrap_line_length;
	public boolean indent_empty_lines;
	public EnumSet<TemplateLanguage> templating = EnumSet.of(TemplateLanguage.auto);

	public void prepare() {
		if (!this.preserve_newlines) {
			this.max_preserve_newlines = 0;
		}

		if (this.indent_with_tabs) {
			this.indent_char = "\t";

			// indent_size behavior changed after 1.8.6
			// It used to be that indent_size would be
			// set to 1 for indent_with_tabs. That is no longer needed and
			// actually doesn't make sense - why not use spaces? Further,
			// that might produce unexpected behavior - tabs being used
			// for single-column alignment. So, when indent_with_tabs is true
			// and indent_size is 1, reset indent_size to 4.
			if (this.indent_size == 1) {
				this.indent_size = 4;
			}
		}
	}

}
