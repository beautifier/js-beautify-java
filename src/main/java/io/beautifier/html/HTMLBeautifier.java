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

package io.beautifier.html;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.beautifier.core.BeautifierFunction;
import io.beautifier.core.Output;
import io.beautifier.core.TokenStream;
import io.beautifier.html.Options.IndentScripts;
import io.beautifier.html.Options.WrapAttributes;
import io.beautifier.html.Tokenizer.TOKEN;

@NonNullByDefault
public class HTMLBeautifier {

	private static final Pattern lineBreak = Pattern.compile("\r\n|[\r\n]");

	private static class Printer {

		private int indent_level;
		private int alignment_size;
		private int max_preserve_newlines;
		private boolean preserve_newlines;
		private Output _output;

		Printer(Options options, String base_indent_string) { //handles input/output and some other printing functions

			this.indent_level = 0;
			this.alignment_size = 0;
			this.max_preserve_newlines = options.max_preserve_newlines;
			this.preserve_newlines = options.preserve_newlines;

			this._output = new Output(options, base_indent_string);

		}

		boolean current_line_has_match(Pattern pattern) {
			return this._output.current_line.has_match(pattern);
		}

		void set_space_before_token(boolean value) {
			set_space_before_token(value, false);
		}

		void set_space_before_token(boolean value, boolean non_breaking) {
			this._output.space_before_token = value;
			this._output.non_breaking_space = non_breaking;
		}

		void set_wrap_point() {
			this._output.set_indent(this.indent_level, this.alignment_size);
			this._output.set_wrap_point();
		}


		void add_raw_token(Token token) {
			this._output.add_raw_token(token);
		}

		boolean print_preserved_newlines(Token raw_token) {
			var newlines = 0;
			if (raw_token.type != TOKEN.TEXT && raw_token.previous.type != TOKEN.TEXT) {
				newlines = raw_token.newlines != 0 ? 1 : 0;
			}

			if (this.preserve_newlines) {
				newlines = raw_token.newlines < this.max_preserve_newlines + 1 ? raw_token.newlines : this.max_preserve_newlines + 1;
			}
			for (var n = 0; n < newlines; n++) {
				this.print_newline(n > 0);
			}

			return newlines != 0;
		}

		boolean traverse_whitespace(Token raw_token) {
			if ((raw_token.whitespace_before != null && !raw_token.whitespace_before.isEmpty()) || raw_token.newlines != 0) {
				if (!this.print_preserved_newlines(raw_token)) {
					this._output.space_before_token = true;
				}
				return true;
			}
			return false;
		}

		boolean previous_token_wrapped() {
			return this._output.previous_token_wrapped;
		}

		void print_newline(boolean force) {
			this._output.add_new_line(force);
		}

		void print_token(Token token) {
			if (token.text != null && !token.text.isEmpty()) {
				this._output.set_indent(this.indent_level, this.alignment_size);
				this._output.add_token(token.text);
			}
		}

		void indent() {
			this.indent_level++;
		}

		String get_full_indent(int level) {
			level = this.indent_level + level;
			if (level < 1) {
				return "";
			}

			return this._output.get_indent_string(level);
		}

	}

	private String get_type_attribute(Token start_token) {
		String result = null;
		var raw_token = start_token.next;

		// Search attributes for a type attribute
		while (raw_token.type != TOKEN.EOF && start_token.closed != raw_token) {
			if (raw_token.type == TOKEN.ATTRIBUTE && "type".equals(raw_token.text)) {
				if (raw_token.next != null && raw_token.next.type == TOKEN.EQUALS &&
					raw_token.next.next != null && raw_token.next.next.type == TOKEN.VALUE) {
					result = raw_token.next.next.text;
				}
				break;
			}
			raw_token = raw_token.next;
		}

		return result;
	}

	private String get_custom_beautifier_name(String tag_check, Token raw_token) {
		String typeAttribute = null;
		String result = null;

		if (raw_token.closed == null) {
			return null;
		}

		if ("script".equals(tag_check)) {
			typeAttribute = "text/javascript";
		} else if ("style".equals(tag_check)) {
			typeAttribute = "text/css";
		}

		typeAttribute = Objects.toString(get_type_attribute(raw_token), typeAttribute);

		// For script and style tags that have a type attribute, only enable custom beautifiers for matching values
		// For those without a type attribute use default;
		if (typeAttribute.contains("text/css")) {
			result = "css";
		} else if (Pattern.compile("module|((text|application|dojo)/(x-)?(javascript|ecmascript|jscript|livescript|(ld\\+)?json|method|aspect))").matcher(typeAttribute).find()) {
			result = "javascript";
		} else if (Pattern.compile("(text|application|dojo)/(x-)?(html)").matcher(typeAttribute).find()) {
			result = "html";
		} else if (typeAttribute.contains("test/null")) {
			// Test only mime-type for testing the beautifier when null is passed as beautifing function
			result = "null";
		}

		return result;
	}

	private class TagFrame {

		@Nullable TagFrame parent;
		String tag;
		int indent_level;
		@Nullable ParserToken parser_token;

		TagFrame(@Nullable TagFrame parent, TagOpenParserToken parser_token, int indent_level) {
			this.parent = parent;
			this.tag = parser_token != null ? parser_token.tag_name : "";
			this.indent_level = indent_level;
			this.parser_token = parser_token != null ? parser_token : null;
		}

	}

	private class TagStack {

		Printer _printer;
		@Nullable TagFrame _current_frame;

		TagStack(Printer printer) {
			this._printer = printer;
			this._current_frame = null;
		}

		@Nullable
		ParserToken get_parser_token() {
			return this._current_frame != null ? this._current_frame.parser_token : null;
		}

		void record_tag(TagOpenParserToken parser_token) { //function to record a tag and its parent in this.tags Object
			var new_frame = new TagFrame(this._current_frame, parser_token, this._printer.indent_level);
			this._current_frame = new_frame;
		}

		@Nullable
		private ParserToken _try_pop_frame(@Nullable TagFrame frame) { //function to retrieve the opening tag to the corresponding closer
			ParserToken parser_token = null;

			if (frame != null) {
				parser_token = frame.parser_token;
				this._printer.indent_level = frame.indent_level;
				this._current_frame = frame.parent;
			}

			return parser_token;
		}

		@Nullable
		private TagFrame _get_frame(Set<String> tag_list) {
			return _get_frame(tag_list, null);
		}

		@Nullable
		private TagFrame _get_frame(Set<String> tag_list, @Nullable Set<String> stop_list) { //function to retrieve the opening tag to the corresponding closer
			var frame = this._current_frame;

			while (frame != null) { //till we reach '' (the initial value);
				if (tag_list.contains(frame.tag)) { //if this is it use it
					break;
				} else if (stop_list != null && stop_list.contains(frame.tag)) {
					frame = null;
					break;
				}
				frame = frame.parent;
			}

			return frame;
		}

		@Nullable
		ParserToken try_pop(String tag) {
			return try_pop(tag, (Set<String>)null);
		}

		@Nullable
		ParserToken try_pop(String tag, String[] stop_list) {
			return try_pop(tag, new HashSet<>(Arrays.asList(stop_list)));
		}

		@Nullable
		ParserToken try_pop(String tag, @Nullable Set<String> stop_list) { //function to retrieve the opening tag to the corresponding closer
			var frame = this._get_frame(Collections.singleton(tag), stop_list);
			return this._try_pop_frame(frame);
		}

		void indent_to_tag(String... tag_list) {
			indent_to_tag(new HashSet<>(Arrays.asList(tag_list)));
		}

		void indent_to_tag(Set<String> tag_list) {
			var frame = this._get_frame(tag_list);
			if (frame != null) {
				this._printer.indent_level = frame.indent_level;
			}
		}

	}

	private String _source_text;
	private Options _options;
	private @Nullable BeautifierFunction _js_beautify;
	private @Nullable BeautifierFunction _css_beautify;
	private @Nullable TagStack _tag_stack;

	private final boolean _is_wrap_attributes_force;
	private final boolean _is_wrap_attributes_force_expand_multiline;
	private final boolean _is_wrap_attributes_force_aligned;
	private final boolean _is_wrap_attributes_aligned_multiple;
	private final boolean _is_wrap_attributes_preserve;
	private final boolean _is_wrap_attributes_preserve_aligned;

	public static String beautify(@Nullable String source_text, io.beautifier.core.Options<?> options) {
		return new HTMLBeautifier(source_text, options.html().build()).beautify();
	}

	public static Options.Builder options() {
		return Options.builder();
	}

	public HTMLBeautifier(@Nullable String source_text) {
		this(source_text, null);
	}
	
	public HTMLBeautifier(@Nullable String source_text, @Nullable Options options) {
		this(source_text, options, null, null);
	}

	public HTMLBeautifier(@Nullable String source_text, @Nullable Options options, @Nullable BeautifierFunction js_beautify, @Nullable BeautifierFunction css_beautify) {
	// function Beautifier(source_text, options, js_beautify, css_beautify) {
		//Wrapper function to invoke all the necessary constructors and deal with the output.
		this._source_text = Objects.toString(source_text, "");;
		this._js_beautify = js_beautify;
		this._css_beautify = css_beautify;
		this._tag_stack = null;

		// Allow the setting of language/file-type specific options
		// with inheritance of overall settings
		var optionHtml = options != null ? options : Options.builder().build();

		this._options = optionHtml;

		this._is_wrap_attributes_force = this._options.wrap_attributes.isForce();
		this._is_wrap_attributes_force_expand_multiline = (this._options.wrap_attributes == WrapAttributes.forceExpandMultiline);
		this._is_wrap_attributes_force_aligned = (this._options.wrap_attributes == WrapAttributes.forceAligned);
		this._is_wrap_attributes_aligned_multiple = (this._options.wrap_attributes == WrapAttributes.alignedMultiple);
		this._is_wrap_attributes_preserve = this._options.wrap_attributes.isPreserve();
		this._is_wrap_attributes_preserve_aligned = (this._options.wrap_attributes == WrapAttributes.preserveAligned);
	}

	private class ParserToken {

		@Nullable ParserToken parent;
		@Nullable String tag_name;
		String text;
		@Nullable TOKEN type;
		boolean is_inline_element;
		boolean multiline_content;

		ParserToken(String text, @Nullable TOKEN type) {
			this.text = text;
			this.type = type;
		}
	}

	public String beautify() {

		// if disabled, return the input unchanged.
		if (this._options.disabled) {
			return this._source_text;
		}

		var source_text = this._source_text;
		var eol = this._options.eol;
		if ("auto".equals(this._options.eol)) {
			eol = "\n";
			Matcher matcher = lineBreak.matcher(source_text);
			if (matcher.find()) {
				eol = matcher.group();
			}
		}

		// HACK: newline parsing inconsistent. This brute force normalizes the input.
		source_text = lineBreak.matcher(source_text).replaceAll("\n");

		final Matcher baseIdentStringMatcher = Pattern.compile("^[\t ]*").matcher(source_text);
		baseIdentStringMatcher.find();
		var baseIndentString = baseIdentStringMatcher.group();

		var last_token = new ParserToken("", null);

		TagOpenParserToken last_tag_token = new TagOpenParserToken();

		var printer = new Printer(this._options, baseIndentString);
		var tokens = new Tokenizer(source_text, this._options).tokenize();

		this._tag_stack = new TagStack(printer);

		ParserToken parser_token = null;
		var raw_token = tokens.next();
		while (raw_token.type != TOKEN.EOF) {

			if (raw_token.type == TOKEN.TAG_OPEN || raw_token.type == TOKEN.COMMENT) {
				last_tag_token = this._handle_tag_open(printer, raw_token, last_tag_token, last_token, tokens);
				parser_token = last_tag_token;
			} else if ((raw_token.type == TOKEN.ATTRIBUTE || raw_token.type == TOKEN.EQUALS || raw_token.type == TOKEN.VALUE) ||
				(raw_token.type == TOKEN.TEXT && !last_tag_token.tag_complete)) {
				parser_token = this._handle_inside_tag(printer, raw_token, last_tag_token, last_token);
			} else if (raw_token.type == TOKEN.TAG_CLOSE) {
				parser_token = this._handle_tag_close(printer, raw_token, last_tag_token);
			} else if (raw_token.type == TOKEN.TEXT) {
				parser_token = this._handle_text(printer, raw_token, last_tag_token);
			} else {
				// This should never happen, but if it does. Print the raw token
				printer.add_raw_token(raw_token);
			}

			last_token = parser_token;

			raw_token = tokens.next();
		}
		var sweet_code = printer._output.get_code(eol);

		return sweet_code;
	}

	private ParserToken _handle_tag_close(Printer printer, Token raw_token, TagOpenParserToken last_tag_token) {
		var parser_token = new ParserToken(raw_token.text, raw_token.type);
		printer.alignment_size = 0;
		last_tag_token.tag_complete = true;

		printer.set_space_before_token(raw_token.newlines != 0 || !"".equals(raw_token.whitespace_before), true);
		if (last_tag_token.is_unformatted) {
			printer.add_raw_token(raw_token);
		} else {
			if ("<".equals(last_tag_token.tag_start_char)) {
				printer.set_space_before_token(raw_token.text.startsWith("/"), true); // space before />, no space before >
				if (this._is_wrap_attributes_force_expand_multiline && last_tag_token.has_wrapped_attrs) {
					printer.print_newline(false);
				}
			}
			printer.print_token(raw_token);

		}

		if (last_tag_token.indent_content &&
			!(last_tag_token.is_unformatted || last_tag_token.is_content_unformatted)) {
			printer.indent();

			// only indent once per opened tag
			last_tag_token.indent_content = false;
		}

		if (!last_tag_token.is_inline_element &&
			!(last_tag_token.is_unformatted || last_tag_token.is_content_unformatted)) {
			printer.set_wrap_point();
		}

		return parser_token;
	}

	private ParserToken _handle_inside_tag(Printer printer, Token raw_token, TagOpenParserToken last_tag_token, ParserToken last_token) {
		var wrapped = last_tag_token.has_wrapped_attrs;
		var parser_token = new ParserToken(raw_token.text, raw_token.type);

		printer.set_space_before_token(raw_token.newlines != 0 || !raw_token.whitespace_before.isEmpty(), true);
		if (last_tag_token.is_unformatted) {
			printer.add_raw_token(raw_token);
		} else if ("{".equals(last_tag_token.tag_start_char) && raw_token.type == TOKEN.TEXT) {
			// For the insides of handlebars allow newlines or a single space between open and contents
			if (printer.print_preserved_newlines(raw_token)) {
				raw_token.newlines = 0;
				printer.add_raw_token(raw_token);
			} else {
				printer.print_token(raw_token);
			}
		} else {
			if (raw_token.type == TOKEN.ATTRIBUTE) {
				printer.set_space_before_token(true);
			} else if (raw_token.type == TOKEN.EQUALS) { //no space before =
				printer.set_space_before_token(false);
			} else if (raw_token.type == TOKEN.VALUE && raw_token.previous.type == TOKEN.EQUALS) { //no space before value
				printer.set_space_before_token(false);
			}

			if (raw_token.type == TOKEN.ATTRIBUTE && "<".equals(last_tag_token.tag_start_char)) {
				if (this._is_wrap_attributes_preserve || this._is_wrap_attributes_preserve_aligned) {
					printer.traverse_whitespace(raw_token);
					wrapped = wrapped || raw_token.newlines != 0;
				}

				// Wrap for 'force' options, and if the number of attributes is at least that specified in 'wrap_attributes_min_attrs':
				// 1. always wrap the second and beyond attributes
				// 2. wrap the first attribute only if 'force-expand-multiline' is specified
				if (this._is_wrap_attributes_force &&
					last_tag_token.attr_count >= this._options.wrap_attributes_min_attrs &&
					(last_token.type != TOKEN.TAG_OPEN || // ie. second attribute and beyond
						this._is_wrap_attributes_force_expand_multiline)) {
					printer.print_newline(false);
					wrapped = true;
				}
			}
			printer.print_token(raw_token);
			wrapped = wrapped || printer.previous_token_wrapped();
			last_tag_token.has_wrapped_attrs = wrapped;
		}
		return parser_token;
	}

	private ParserToken _handle_text(Printer printer, Token raw_token, TagOpenParserToken last_tag_token) {
		var parser_token = new ParserToken(
			raw_token.text,
			TOKEN.CONTENT
		);
		if (last_tag_token.custom_beautifier_name != null) { //check if we need to format javascript
			this._print_custom_beatifier_text(printer, raw_token, last_tag_token);
		} else if (last_tag_token.is_unformatted || last_tag_token.is_content_unformatted) {
			printer.add_raw_token(raw_token);
		} else {
			printer.traverse_whitespace(raw_token);
			printer.print_token(raw_token);
		}
		return parser_token;
	}

	private void _print_custom_beatifier_text(Printer printer, Token raw_token, TagOpenParserToken last_tag_token) {
		var local = this;
		if (!raw_token.text.isEmpty()) {

			var text = raw_token.text;
			BeautifierFunction _beautifier;
			var script_indent_level = 1;
			var pre = "";
			var post = "";
			if ("javascript".equals(last_tag_token.custom_beautifier_name) && this._js_beautify != null) {
				_beautifier = (String source_text, io.beautifier.core.Options<?> options) -> {
					final String saveEol = options.js().eol;
					try {
						options.js().eol = "\n";
						return _js_beautify.beautify(source_text, options);
					} finally {
						options.js().eol = saveEol;
					}
				};
			} else if ("css".equals(last_tag_token.custom_beautifier_name) && this._css_beautify != null) {
				_beautifier = (String source_text, io.beautifier.core.Options<?> options) -> {
					final String saveEol = options.css().eol;
					try {
						options.css().eol = "\n";
						return _css_beautify.beautify(source_text, options);
					} finally {
						options.css().eol = saveEol;
					}
				};
			} else if ("html".equals(last_tag_token.custom_beautifier_name)) {
				_beautifier = (String html_source, io.beautifier.core.Options<?> options) -> {
					final String saveEol = options.html().eol;
					try {
						options.html().eol = "\n";
						var beautifier = new HTMLBeautifier(html_source, options.html().build(), local._js_beautify, local._css_beautify);
						return beautifier.beautify();
					} finally {
						options.html().eol = saveEol;
					}
				};
			} else {
				_beautifier = null;
			}

			if (this._options.indent_scripts == IndentScripts.keep) {
				script_indent_level = 0;
			} else if (this._options.indent_scripts == IndentScripts.separate) {
				script_indent_level = -printer.indent_level;
			}

			var indentation = printer.get_full_indent(script_indent_level);

			// if there is at least one empty line at the end of this text, strip it
			// we'll be adding one back after the text but before the containing tag.
			text = Pattern.compile("\n[ \t]*$").matcher(text).replaceFirst("");

			// Handle the case where content is wrapped in a comment or cdata.
			if (!"html".equals(last_tag_token.custom_beautifier_name) &&
				text.startsWith("<") && Pattern.compile("^(<!--|<!\\[CDATA\\[)").matcher(text).find()) {
				var matched = Pattern.compile("^(<!--[^\n]*|<!\\[CDATA\\[)(\n?)([ \t\n]*)([\\s\\S]*)(-->|]]>)$").matcher(text);

				// if we start to wrap but don't finish, print raw
				if (!matched.find()) {
					printer.add_raw_token(raw_token);
					return;
				}

				pre = indentation + matched.group(1) + '\n';
				text = matched.group(4);
				if (!matched.group(5).isEmpty()) {
					post = indentation + matched.group(5);
				}

				// if there is at least one empty line at the end of this text, strip it
				// we'll be adding one back after the text but before the containing tag.
				text = Pattern.compile("\n[ \t]*$").matcher(text).replaceFirst("");

				if (!matched.group(2).isEmpty() || matched.group(3).indexOf('\n') != -1) {
					// if the first line of the non-comment text has spaces
					// use that as the basis for indenting in null case.
					matched = Pattern.compile("[ \t]+$").matcher(matched.group(3));
					if (matched.find()) {
						raw_token.whitespace_before = matched.group();
					}
				}
			}

			if (text != null && !text.isEmpty()) {
				if (_beautifier != null) {

					// call the Beautifier if avaliable
					text = _beautifier.beautify(indentation + text, this._options);
				} else {
					// simply indent the string otherwise
					var white = raw_token.whitespace_before;
					if (white != null && !white.isEmpty()) {
						text = Pattern.compile("\n(" + white + ")?").matcher(text).replaceAll("\n");
					}

					text = indentation + Pattern.compile("\n").matcher(text).replaceAll("\n" + indentation);
				}
			}

			if (!pre.isEmpty()) {
				if (text == null || text.isEmpty()) {
					text = pre + post;
				} else {
					text = pre + text + '\n' + post;
				}
			}

			printer.print_newline(false);
			if (text != null && !text.isEmpty()) {
				raw_token.text = text;
				raw_token.whitespace_before = "";
				raw_token.newlines = 0;
				printer.add_raw_token(raw_token);
				printer.print_newline(true);
			}
		}
	}

	private TagOpenParserToken _handle_tag_open(Printer printer, Token raw_token, TagOpenParserToken last_tag_token, ParserToken last_token, TokenStream<TOKEN, Token> tokens) {
		var parser_token = this._get_tag_open_token(raw_token);

		if ((last_tag_token.is_unformatted || last_tag_token.is_content_unformatted) &&
			!last_tag_token.is_empty_element &&
			raw_token.type == TOKEN.TAG_OPEN && !parser_token.is_start_tag) {
			// End element tags for unformatted or content_unformatted elements
			// are printed raw to keep any newlines inside them exactly the same.
			printer.add_raw_token(raw_token);
			parser_token.start_tag_token = this._tag_stack.try_pop(parser_token.tag_name);
		} else {
			printer.traverse_whitespace(raw_token);
			this._set_tag_position(printer, raw_token, parser_token, last_tag_token, last_token);
			if (!parser_token.is_inline_element) {
				printer.set_wrap_point();
			}
			printer.print_token(raw_token);
		}

		// count the number of attributes
		if (parser_token.is_start_tag && this._is_wrap_attributes_force) {
			var peek_index = 0;
			Token peek_token;
			do {
				peek_token = tokens.peek(peek_index);
				if (peek_token.type == TOKEN.ATTRIBUTE) {
					parser_token.attr_count += 1;
				}
				peek_index += 1;
			} while (peek_token.type != TOKEN.EOF && peek_token.type != TOKEN.TAG_CLOSE);
		}

		//indent attributes an auto, forced, aligned or forced-align line-wrap
		if (this._is_wrap_attributes_force_aligned || this._is_wrap_attributes_aligned_multiple || this._is_wrap_attributes_preserve_aligned) {
			parser_token.alignment_size = raw_token.text.length() + 1;
		}

		if (!parser_token.tag_complete && !parser_token.is_unformatted) {
			printer.alignment_size = parser_token.alignment_size;
		}

		return parser_token;
	}

	private class TagOpenParserToken extends ParserToken {

		boolean is_unformatted;
		boolean is_content_unformatted;
		boolean is_empty_element;
		boolean is_start_tag;
		boolean is_end_tag;
		boolean indent_content;
		@Nullable String custom_beautifier_name;
		@Nullable ParserToken start_tag_token;
		int attr_count;
		boolean has_wrapped_attrs;
		int alignment_size;
		boolean tag_complete;
		String tag_start_char;
		String tag_check;

		TagOpenParserToken() {
			this(null, null);
		}

		TagOpenParserToken(@Nullable ParserToken parent, @Nullable Token raw_token) {
			super("", TOKEN.TAG_OPEN);
			this.parent = parent;
			this.tag_name = "";
			this.is_inline_element = false;
			this.is_unformatted = false;
			this.is_content_unformatted = false;
			this.is_empty_element = false;
			this.is_start_tag = false;
			this.is_end_tag = false;
			this.indent_content = false;
			this.multiline_content = false;
			this.custom_beautifier_name = null;
			this.start_tag_token = null;
			this.attr_count = 0;
			this.has_wrapped_attrs = false;
			this.alignment_size = 0;
			this.tag_complete = false;
			this.tag_start_char = "";
			this.tag_check = "";

			if (raw_token == null) {
				this.tag_complete = true;
			} else {
				Matcher tag_check_match;

				this.tag_start_char = raw_token.text.substring(0, 1);
				this.text = raw_token.text;

				if ("<".equals(this.tag_start_char)) {
					tag_check_match = Pattern.compile("^<([^\\s>]*)").matcher(raw_token.text);
					this.tag_check = tag_check_match.find() ? tag_check_match.group(1) : "";
				} else {
					tag_check_match = Pattern.compile("^\\{\\{~?(?:[\\^]|#\\*?)?([^\\s}]+)").matcher(raw_token.text);
					this.tag_check = tag_check_match.find() ? tag_check_match.group(1) : "";

					// handle "{{#> myPartial}}" or "{{~#> myPartial}}"
					if ((raw_token.text.startsWith("{{#>") || raw_token.text.startsWith("{{~#>")) && this.tag_check.startsWith(">")) {
						if (">".equals(this.tag_check) && raw_token.next != null) {
							this.tag_check = raw_token.next.text.split(" ")[0];
						} else {
							this.tag_check = raw_token.text.split(">")[1];
						}
					}
				}

				this.tag_check = this.tag_check.toLowerCase();

				if (raw_token.type == TOKEN.COMMENT) {
					this.tag_complete = true;
				}

				this.is_start_tag = this.tag_check.isEmpty() || this.tag_check.charAt(0) != '/';
				this.tag_name = !this.is_start_tag ? this.tag_check.substring(1, this.tag_check.length()) : this.tag_check;
				this.is_end_tag = !this.is_start_tag ||
					(raw_token.closed != null && "/>".equals(raw_token.closed.text));

				// if whitespace handler ~ included (i.e. {{~#if true}}), handlebars tags start at pos 3 not pos 2
				var handlebar_starts = 2;
				if ("{".equals(this.tag_start_char) && this.text.length() >= 3) {
					if (this.text.charAt(2) == '~') {
						handlebar_starts = 3;
					}
				}

				// handlebars tags that don't start with # or ^ are single_tags, and so also start and end.
				this.is_end_tag = this.is_end_tag ||
					("{".equals(this.tag_start_char) && (this.text.length() < 3 || (Pattern.compile("[^#\\^]").matcher(Character.toString(this.text.charAt(handlebar_starts))).find())));
			}
		}

	}

	private TagOpenParserToken _get_tag_open_token(Token raw_token) { //function to get a full tag and parse its type
		var parser_token = new TagOpenParserToken(this._tag_stack.get_parser_token(), raw_token);

		parser_token.alignment_size = this._options.wrap_attributes_indent_size;

		parser_token.is_end_tag = parser_token.is_end_tag ||
			this._options.void_elements.contains(parser_token.tag_check);

		parser_token.is_empty_element = parser_token.tag_complete ||
			(parser_token.is_start_tag && parser_token.is_end_tag);

		parser_token.is_unformatted = !parser_token.tag_complete && this._options.unformatted.contains(parser_token.tag_check);
		parser_token.is_content_unformatted = !parser_token.is_empty_element && this._options.content_unformatted.contains(parser_token.tag_check);
		parser_token.is_inline_element = this._options.inline.contains(parser_token.tag_name) || parser_token.tag_name.indexOf("-") != -1 || "{".equals(parser_token.tag_start_char);

		return parser_token;
	}

	void _set_tag_position(Printer printer, Token raw_token, TagOpenParserToken parser_token, TagOpenParserToken last_tag_token, ParserToken last_token) {

		if (!parser_token.is_empty_element) {
			if (parser_token.is_end_tag) { //this tag is a double tag so check for tag-ending
				parser_token.start_tag_token = this._tag_stack.try_pop(parser_token.tag_name); //remove it and all ancestors
			} else { // it's a start-tag
				// check if this tag is starting an element that has optional end element
				// and do an ending needed
				if (this._do_optional_end_element(parser_token) != null) {
					if (!parser_token.is_inline_element) {
						printer.print_newline(false);
					}
				}

				this._tag_stack.record_tag(parser_token); //push it on the tag stack

				if (("script".equals(parser_token.tag_name) || "style".equals(parser_token.tag_name)) &&
					!(parser_token.is_unformatted || parser_token.is_content_unformatted)) {
					parser_token.custom_beautifier_name = get_custom_beautifier_name(parser_token.tag_check, raw_token);
				}
			}
		}

		if (this._options.extra_liners.contains(parser_token.tag_check)) { //check if this double needs an extra line
			printer.print_newline(false);
			if (!printer._output.just_added_blankline()) {
				printer.print_newline(true);
			}
		}

		if (parser_token.is_empty_element) { //if this tag name is a single tag type (either in the list or has a closing /)

			// if you hit an else case, reset the indent level if you are inside an:
			// 'if', 'unless', or 'each' block.
			if ("{".equals(parser_token.tag_start_char) && "else".equals(parser_token.tag_check)) {
				this._tag_stack.indent_to_tag("if", "unless", "each");
				parser_token.indent_content = true;
				// Don't add a newline if opening {{#if}} tag is on the current line
				var foundIfOnCurrentLine = printer.current_line_has_match(Pattern.compile("\\{\\{#if"));
				if (!foundIfOnCurrentLine) {
					printer.print_newline(false);
				}
			}

			// Don't add a newline before elements that should remain where they are.
			if ("!--".equals(parser_token.tag_name) && last_token.type == TOKEN.TAG_CLOSE &&
				last_tag_token.is_end_tag && parser_token.text.indexOf('\n') == -1) {
				//Do nothing. Leave comments on same line.
			} else {
				if (!(parser_token.is_inline_element || parser_token.is_unformatted)) {
					printer.print_newline(false);
				}
				this._calcluate_parent_multiline(printer, parser_token);
			}
		} else if (parser_token.is_end_tag) { //this tag is a double tag so check for tag-ending
			var do_end_expand = false;

			// deciding whether a block is multiline should not be this hard
			do_end_expand = parser_token.start_tag_token != null && parser_token.start_tag_token.multiline_content;
			do_end_expand = do_end_expand || (!parser_token.is_inline_element &&
				!(last_tag_token.is_inline_element || last_tag_token.is_unformatted) &&
				!(last_token.type == TOKEN.TAG_CLOSE && parser_token.start_tag_token == last_tag_token) &&
				last_token.type != TOKEN.CONTENT
			);

			if (parser_token.is_content_unformatted || parser_token.is_unformatted) {
				do_end_expand = false;
			}

			if (do_end_expand) {
				printer.print_newline(false);
			}
		} else { // it's a start-tag
			parser_token.indent_content = parser_token.custom_beautifier_name == null;

			if ("<".equals(parser_token.tag_start_char)) {
				if ("html".equals(parser_token.tag_name)) {
					parser_token.indent_content = this._options.indent_inner_html;
				} else if ("head".equals(parser_token.tag_name)) {
					parser_token.indent_content = this._options.indent_head_inner_html;
				} else if ("body".equals(parser_token.tag_name)) {
					parser_token.indent_content = this._options.indent_body_inner_html;
				}
			}

			if (!(parser_token.is_inline_element || parser_token.is_unformatted) &&
				(last_token.type != TOKEN.CONTENT || parser_token.is_content_unformatted)) {
				printer.print_newline(false);
			}

			this._calcluate_parent_multiline(printer, parser_token);
		}
	}

	private void _calcluate_parent_multiline(Printer printer, TagOpenParserToken parser_token) {
		if (parser_token.parent != null && printer._output.just_added_newline() &&
			!((parser_token.is_inline_element || parser_token.is_unformatted) && parser_token.parent.is_inline_element)) {
			parser_token.parent.multiline_content = true;
		}
	}

	//To be used for <p> tag special case:
	private static final Set<String> p_closers = new HashSet<>(Arrays.asList("address", "article", "aside", "blockquote", "details", "div", "dl", "fieldset", "figcaption", "figure", "footer", "form", "h1", "h2", "h3", "h4", "h5", "h6", "header", "hr", "main", "menu", "nav", "ol", "p", "pre", "section", "table", "ul"));
	private static final Set<String> p_parent_excludes = new HashSet<>(Arrays.asList("a", "audio", "del", "ins", "map", "noscript", "video"));

	@Nullable
	private ParserToken _do_optional_end_element(TagOpenParserToken parser_token) {
		ParserToken result = null;
		// NOTE: cases of "if there is no more content in the parent element"
		// are handled automatically by the beautifier.
		// It assumes parent or ancestor close tag closes all children.
		// https://www.w3.org/TR/html5/syntax.html#optional-tags
		if (parser_token.is_empty_element || !parser_token.is_start_tag || parser_token.parent == null) {
			return null;

		}

		if ("body".equals(parser_token.tag_name)) {
			// A head element’s end tag may be omitted if the head element is not immediately followed by a space character or a comment.
			if (result == null) {
				result = this._tag_stack.try_pop("head");
			}

			//} else if (parser_token.tag_name === 'body') {
			// DONE: A body element’s end tag may be omitted if the body element is not immediately followed by a comment.

		} else if ("li".equals(parser_token.tag_name)) {
			// An li element’s end tag may be omitted if the li element is immediately followed by another li element or if there is no more content in the parent element.
			if (result == null) {
				result = this._tag_stack.try_pop("li", new String[] { "ol", "ul", "menu" });
			}

		} else if ("dd".equals(parser_token.tag_name) || "dt".equals(parser_token.tag_name)) {
			// A dd element’s end tag may be omitted if the dd element is immediately followed by another dd element or a dt element, or if there is no more content in the parent element.
			// A dt element’s end tag may be omitted if the dt element is immediately followed by another dt element or a dd element.
			if (result == null) {
				result = this._tag_stack.try_pop("dt", new String[] { "dl" });
			}
			if (result == null) {
				result = this._tag_stack.try_pop("dd", new String[] { "dl" });
			}

		} else if ("p".equals(parser_token.parent.tag_name) && p_closers.contains(parser_token.tag_name)) {
			// IMPORTANT: this else-if works because p_closers has no overlap with any other element we look for in this method
			// check for the parent element is an HTML element that is not an <a>, <audio>, <del>, <ins>, <map>, <noscript>, or <video> element,  or an autonomous custom element.
			// To do this right, this needs to be coded as an inclusion of the inverse of the exclusion above.
			// But to start with (if we ignore "autonomous custom elements") the exclusion would be fine.
			var p_parent = parser_token.parent.parent;
			if (p_parent == null || !p_parent_excludes.contains(p_parent.tag_name)) {
				if (result == null) {
					result = this._tag_stack.try_pop("p");
				}
			}
		} else if ("rp".equals(parser_token.tag_name) || "rt".equals(parser_token.tag_name)) {
			// An rt element’s end tag may be omitted if the rt element is immediately followed by an rt or rp element, or if there is no more content in the parent element.
			// An rp element’s end tag may be omitted if the rp element is immediately followed by an rt or rp element, or if there is no more content in the parent element.
			if (result == null) {
				result = this._tag_stack.try_pop("rt", new String[] { "ruby", "rtc" });
			}
			if (result == null) {
				result = this._tag_stack.try_pop("rp", new String[] { "ruby", "rtc" });
			}

		} else if ("optgroup".equals(parser_token.tag_name)) {
			// An optgroup element’s end tag may be omitted if the optgroup element is immediately followed by another optgroup element, or if there is no more content in the parent element.
			// An option element’s end tag may be omitted if the option element is immediately followed by another option element, or if it is immediately followed by an optgroup element, or if there is no more content in the parent element.
			if (result == null) {
				result = this._tag_stack.try_pop("optgroup", new String[] { "select" });
			}
			//result = result || this._tag_stack.try_pop('option', ['select']);

		} else if ("option".equals(parser_token.tag_name)) {
			// An option element’s end tag may be omitted if the option element is immediately followed by another option element, or if it is immediately followed by an optgroup element, or if there is no more content in the parent element.
			if (result == null) {
				result = this._tag_stack.try_pop("option", new String[] { "select", "datalist", "optgroup" });
			}

		} else if ("colgroup".equals(parser_token.tag_name)) {
			// DONE: A colgroup element’s end tag may be omitted if the colgroup element is not immediately followed by a space character or a comment.
			// A caption element's end tag may be ommitted if a colgroup, thead, tfoot, tbody, or tr element is started.
			if (result == null) {
				result = this._tag_stack.try_pop("caption", new String[] { "table" });
			}

		} else if ("thead".equals(parser_token.tag_name)) {
			// A colgroup element's end tag may be ommitted if a thead, tfoot, tbody, or tr element is started.
			// A caption element's end tag may be ommitted if a colgroup, thead, tfoot, tbody, or tr element is started.
			if (result == null) {
				result = this._tag_stack.try_pop("caption", new String[] { "table" });
			}
			if (result == null) {
				result = this._tag_stack.try_pop("colgroup", new String[] { "table" });
			}

			//} else if (parser_token.tag_name === 'caption') {
			// DONE: A caption element’s end tag may be omitted if the caption element is not immediately followed by a space character or a comment.

		} else if ("tbody".equals(parser_token.tag_name) || "tfoot".equals(parser_token.tag_name)) {
			// A thead element’s end tag may be omitted if the thead element is immediately followed by a tbody or tfoot element.
			// A tbody element’s end tag may be omitted if the tbody element is immediately followed by a tbody or tfoot element, or if there is no more content in the parent element.
			// A colgroup element's end tag may be ommitted if a thead, tfoot, tbody, or tr element is started.
			// A caption element's end tag may be ommitted if a colgroup, thead, tfoot, tbody, or tr element is started.
			if (result == null) {
				result = this._tag_stack.try_pop("caption", new String[] { "table" });
			}
			if (result == null) {
				result = this._tag_stack.try_pop("colgroup", new String[] { "table" });
			}
			if (result == null) {
				result = this._tag_stack.try_pop("thead", new String[] { "table" });
			}
			if (result == null) {
				result = this._tag_stack.try_pop("tbody", new String[] { "table" });
			}

			//} else if (parser_token.tag_name === 'tfoot') {
			// DONE: A tfoot element’s end tag may be omitted if there is no more content in the parent element.

		} else if ("tr".equals(parser_token.tag_name)) {
			// A tr element’s end tag may be omitted if the tr element is immediately followed by another tr element, or if there is no more content in the parent element.
			// A colgroup element's end tag may be ommitted if a thead, tfoot, tbody, or tr element is started.
			// A caption element's end tag may be ommitted if a colgroup, thead, tfoot, tbody, or tr element is started.
			if (result == null) {
				result = this._tag_stack.try_pop("caption", new String[] { "table" });
			}
			if (result == null) {
				result = this._tag_stack.try_pop("colgroup", new String[] { "table" });
			}
			if (result == null) {
				result = this._tag_stack.try_pop("tr", new String[] { "table", "thead", "tbody", "tfoot" });
			}

		} else if ("th".equals(parser_token.tag_name) || "td".equals(parser_token.tag_name)) {
			// A td element’s end tag may be omitted if the td element is immediately followed by a td or th element, or if there is no more content in the parent element.
			// A th element’s end tag may be omitted if the th element is immediately followed by a td or th element, or if there is no more content in the parent element.
			if (result == null) {
				result = this._tag_stack.try_pop("td", new String[] { "table", "thead", "tbody", "tfoot", "tr" });
			}
			if (result == null) {
				result = this._tag_stack.try_pop("th", new String[] { "table", "thead", "tbody", "tfoot", "tr" });
			}
		}

		// Start element omission not handled currently
		// A head element’s start tag may be omitted if the element is empty, or if the first thing inside the head element is an element.
		// A tbody element’s start tag may be omitted if the first thing inside the tbody element is a tr element, and if the element is not immediately preceded by a tbody, thead, or tfoot element whose end tag has been omitted. (It can’t be omitted if the element is empty.)
		// A colgroup element’s start tag may be omitted if the first thing inside the colgroup element is a col element, and if the element is not immediately preceded by another colgroup element whose end tag has been omitted. (It can’t be omitted if the element is empty.)

		// Fix up the parent of the parser token
		parser_token.parent = this._tag_stack.get_parser_token();

		return result;
	}

}
