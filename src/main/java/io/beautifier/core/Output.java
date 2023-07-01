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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class Output {

	public static class OutputLine {

		private Output __parent;
		private int __character_count;
		private int __indent_count;
		private int __alignment_count;
		private int __wrap_point_index;
		private int __wrap_point_character_count;
		private int __wrap_point_indent_count;
		private int __wrap_point_alignment_count;
		private List<String> __items;
		
		OutputLine(Output parent) {
			this.__parent = parent;
			this.__character_count = 0;
			// use indent_count as a marker for this.__lines that have preserved indentation
			this.__indent_count = -1;
			this.__alignment_count = 0;
			this.__wrap_point_index = 0;
			this.__wrap_point_character_count = 0;
			this.__wrap_point_indent_count = -1;
			this.__wrap_point_alignment_count = 0;

			this.__items = new ArrayList<>();
		}

		public OutputLine clone_empty() {
			var line = new OutputLine(this.__parent);
			line.set_indent(this.__indent_count, this.__alignment_count);
			return line;
		}

		public String item(int index) {
			if (index < 0) {
				return this.__items.get(this.__items.size() + index);
			} else {
				return this.__items.get(index);
			}
		}

		public boolean has_match(Pattern pattern) {
			for (var lastCheckedOutput = this.__items.size() - 1; lastCheckedOutput >= 0; lastCheckedOutput--) {
				if (pattern.matcher(this.__items.get(lastCheckedOutput)).find()) {
					return true;
				}
			}
			return false;
		}

		public void set_indent() {
			set_indent(0, 0);
		}

		public void set_indent(int indent) {
			set_indent(indent, 0);
		}

		public void set_indent(int indent, int alignment) {
			if (this.is_empty()) {
				this.__indent_count = indent;
				this.__alignment_count = alignment;
				this.__character_count = this.__parent.get_indent_size(this.__indent_count, this.__alignment_count);
			}
		}

		void _set_wrap_point() {
			if (this.__parent.wrap_line_length != 0) {
				this.__wrap_point_index = this.__items.size();
				this.__wrap_point_character_count = this.__character_count;
				this.__wrap_point_indent_count = this.__parent.next_line.__indent_count;
				this.__wrap_point_alignment_count = this.__parent.next_line.__alignment_count;
			}
		}

		private boolean _should_wrap() {
			return this.__wrap_point_index != 0 &&
				this.__character_count > this.__parent.wrap_line_length &&
				this.__wrap_point_character_count > this.__parent.next_line.__character_count;
		}

		boolean _allow_wrap() {
			if (this._should_wrap()) {
				this.__parent.add_new_line();
				var next = this.__parent.current_line;
				next.set_indent(this.__wrap_point_indent_count, this.__wrap_point_alignment_count);
				next.__items = new ArrayList<>(this.__items.subList(this.__wrap_point_index, this.__items.size()));
				this.__items = new ArrayList<>(this.__items.subList(0, this.__wrap_point_index));

				next.__character_count += this.__character_count - this.__wrap_point_character_count;
				this.__character_count = this.__wrap_point_character_count;

				if (" ".equals(next.__items.get(0))) {
					next.__items.remove(0);
					next.__character_count -= 1;
				}
				return true;
			}
			return false;
		}

		public boolean is_empty() {
			return this.__items.size() == 0;
		}

		public @Nullable String last() {
			if (!this.is_empty()) {
				return this.__items.get(this.__items.size() - 1);
			} else {
				return null;
			}
		}

		public void push(String item) {
			this.__items.add(item);
			var last_newline_index = item.lastIndexOf('\n');
			if (last_newline_index != -1) {
				this.__character_count = item.length() - last_newline_index;
			} else {
				this.__character_count += item.length();
			}
		}

		@Nullable
		public String pop() {
			String item = null;
			if (!this.is_empty()) {
				item = this.__items.remove(this.__items.size() - 1);
				this.__character_count -= item.length();
			}
			return item;
		}


		void _remove_indent() {
			if (this.__indent_count > 0) {
				this.__indent_count -= 1;
				this.__character_count -= this.__parent.indent_size;
			}
		}

		void _remove_wrap_indent() {
			if (this.__wrap_point_indent_count > 0) {
				this.__wrap_point_indent_count -= 1;
			}
		}
		public void trim() {
			while (" ".equals(this.last())) {
				this.__items.remove(this.__items.size() - 1);
				this.__character_count -= 1;
			}
		}

		public String toString() {
			var result = "";
			if (this.is_empty()) {
				if (this.__parent.indent_empty_lines) {
				result = this.__parent.get_indent_string(this.__indent_count);
				}
			} else {
				result = this.__parent.get_indent_string(this.__indent_count, this.__alignment_count);
				result += String.join("", this.__items);
			}
			return result;
		}
	}

	public static class IndentStringCache {

		private final String __base_string;
		private final int __base_string_length;
		private final int __indent_size;
		private final String __indent_string;
		private final List<String> __cache;

		private static String repeat(int count, String str) {
			StringBuilder result = new StringBuilder();
			for (int i = 0; i < count; i++) {
				result.append(str);
			}
			return result.toString();
		}

		public IndentStringCache(Options options) {
			this(options, "");
		}

		public IndentStringCache(Options options, String baseIndentString) {
			this.__cache = new ArrayList<>();
			this.__cache.add("");
			this.__indent_size = options.indent_size;
			
			if (!options.indent_with_tabs) {
				this.__indent_string = repeat(options.indent_size, options.indent_char);
			} else {
				this.__indent_string = options.indent_char;
			}

			// Set to null to continue support for auto detection of base indent
			if (options.indent_level > 0) {
				baseIndentString = repeat(options.indent_level, this.__indent_string);
			}

			this.__base_string = baseIndentString;
			this.__base_string_length = baseIndentString.length();
		}

		public int get_indent_size(int indent) {
			return get_indent_size(indent, 0);
		}

		public int get_indent_size(int indent, int column) {
			var result = this.__base_string_length;
			if (indent < 0) {
				result = 0;
			}
			result += indent * this.__indent_size;
			result += column;
			return result;
		}

		public String get_indent_string(int indent_level) {
			return get_indent_string(indent_level, 0);
		}

		public String get_indent_string(int indent_level, int column) {
			var result = this.__base_string;
			if (indent_level < 0) {
				indent_level = 0;
				result = "";
			}
			column += indent_level * this.__indent_size;
			this.__ensure_cache(column);
			result += this.__cache.get(column);
			return result;
		}

		private void __ensure_cache(int column) {
			while (column >= this.__cache.size()) {
				this.__add_column();
			}
		}

		private void __add_column() {
			var column = this.__cache.size();
			var indent = 0;
			var result = "";
			if (this.__indent_size != 0 && column >= this.__indent_size) {
				indent = (int) Math.floor((double) column / this.__indent_size);
				column -= indent * this.__indent_size;
				result = repeat(indent, this.__indent_string);
			}
			if (column != 0) {
				result += repeat(column, " ");
			}

			this.__cache.add(result);
		}
	}

	private final IndentStringCache __indent_cache;
	public boolean raw;
	private boolean _end_with_newline;
	private int indent_size;
	private int wrap_line_length;
	private boolean indent_empty_lines;
	private List<OutputLine> __lines;
	public @Nullable OutputLine previous_line;
	public @Nullable OutputLine current_line;
	private OutputLine next_line;
	public boolean space_before_token;
	public boolean non_breaking_space;
	public boolean previous_token_wrapped;

	public Output(Options options, String baseIndentString) {
		this.__indent_cache = new IndentStringCache(options, baseIndentString);
		this.raw = false;
		this._end_with_newline = options.end_with_newline;
		this.indent_size = options.indent_size;
		this.wrap_line_length = options.wrap_line_length;
		this.indent_empty_lines = options.indent_empty_lines;
		this.__lines = new ArrayList<>();
		this.previous_line = null;
		this.current_line = null;
		this.next_line = new OutputLine(this);
		this.space_before_token = false;
		this.non_breaking_space = false;
		this.previous_token_wrapped = false;
		// initialize
		this.__add_outputline();
	}

	private void __add_outputline() {
		this.previous_line = this.current_line;
		this.current_line = this.next_line.clone_empty();
		this.__lines.add(this.current_line);
	}

	public int get_line_number() {
		return this.__lines.size();
	}

	public String get_indent_string(int indent) {
		return get_indent_string(indent, 0);
	}

	public String get_indent_string(int indent, int column) {
		return this.__indent_cache.get_indent_string(indent, column);
	}

	public int get_indent_size(int indent, int column) {
		return this.__indent_cache.get_indent_size(indent, column);
	}

	public boolean is_empty() {
		return this.previous_line == null && this.current_line.is_empty();
	}

	public boolean add_new_line() {
		return add_new_line(false);
	}

	public boolean add_new_line(boolean force_newline) {
		// never newline at the start of file
		// otherwise, newline only if we didn't just add one or we're forced
		if (this.is_empty() ||
			(!force_newline && this.just_added_newline())) {
			return false;
		}

		// if raw output is enabled, don't print additional newlines,
		// but still return True as though you had
		if (!this.raw) {
			this.__add_outputline();
		}
		return true;
	}

	public String get_code(String eol) {
		this.trim(true);

		// handle some edge cases where the last tokens
		// has text that ends with newline(s)
		var last_item = this.current_line.pop();
		if (last_item != null) {
			if (last_item.endsWith("\n")) {
				last_item = last_item.replaceFirst("\n+$", "");
			}
			this.current_line.push(last_item);
		}

		if (this._end_with_newline) {
			this.__add_outputline();
		}

		var sweet_code = this.__lines.stream().map(OutputLine::toString).collect(Collectors.joining("\n"));

		if (!"\n".equals(eol)) {
			sweet_code = sweet_code.replaceAll("\n", eol);
		}
		return sweet_code;
	}

	public void set_wrap_point() {
		this.current_line._set_wrap_point();
	}

	public boolean set_indent() {
		return set_indent(0, 0);
	}

	public boolean set_indent(int indent, int alignment) {
		// Next line stores alignment values
		this.next_line.set_indent(indent, alignment);

		// Never indent your first output indent at the start of the file
		if (this.__lines.size() > 1) {
			this.current_line.set_indent(indent, alignment);
			return true;
		}

		this.current_line.set_indent();
		return false;
	}

	public void add_raw_token(Token token) {
		for (var x = 0; x < token.newlines; x++) {
			this.__add_outputline();
		}
		this.current_line.set_indent(-1);
		this.current_line.push(token.whitespace_before);
		this.current_line.push(token.text);
		this.space_before_token = false;
		this.non_breaking_space = false;
		this.previous_token_wrapped = false;
	}

	public void add_token(String printable_token) {
		this.__add_space_before_token();
		this.current_line.push(printable_token);
		this.space_before_token = false;
		this.non_breaking_space = false;
		this.previous_token_wrapped = this.current_line._allow_wrap();
	}

	private void __add_space_before_token() {
		if (this.space_before_token && !this.just_added_newline()) {
			if (!this.non_breaking_space) {
			this.set_wrap_point();
			}
			this.current_line.push(" ");
		}
	}

	public void remove_indent(int index) {
		var output_length = this.__lines.size();
		while (index < output_length) {
			this.__lines.get(index)._remove_indent();
			index++;
		}
		this.current_line._remove_wrap_indent();
	}

	public void trim() {
		trim(false);
	}

	public void trim(boolean eat_newlines) {
		this.current_line.trim();

		while (eat_newlines && this.__lines.size() > 1 &&
			this.current_line.is_empty()) {
			this.__lines.remove(this.__lines.size() - 1);
			this.current_line = this.__lines.get(this.__lines.size() - 1);
			this.current_line.trim();
		}

		this.previous_line = this.__lines.size() > 1 ?
			this.__lines.get(this.__lines.size() - 2) : null;
	}

	public boolean just_added_newline() {
		return this.current_line.is_empty();
	}

	public boolean just_added_blankline() {
		return this.is_empty() ||
			(this.current_line.is_empty() && this.previous_line.is_empty());
	}

	public void ensure_empty_line_above(String starts_with, String ends_with) {
		var index = this.__lines.size() - 2;
		while (index >= 0) {
			var potentialEmptyLine = this.__lines.get(index);
			if (potentialEmptyLine.is_empty()) {
				break;
			} else if (potentialEmptyLine.item(0).indexOf(starts_with) != 0 &&
				!potentialEmptyLine.item(-1).equals(ends_with)) {
				this.__lines.add(index + 1, new OutputLine(this));
				this.previous_line = this.__lines.get(this.__lines.size() - 2);
				break;
			}
			index--;
		}
	}

}
