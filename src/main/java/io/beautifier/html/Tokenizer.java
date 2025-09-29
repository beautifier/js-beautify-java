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

import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.beautifier.core.Directives;
import io.beautifier.core.InputScannerPattern;
import io.beautifier.core.Options.TemplateLanguage;
import io.beautifier.core.TemplatablePattern;

@NonNullByDefault
public class Tokenizer extends io.beautifier.core.Tokenizer<Tokenizer.TOKEN, Token> {

	enum TOKEN {
		TAG_OPEN,
		TAG_CLOSE,
		CONTROL_FLOW_OPEN,
		CONTROL_FLOW_CLOSE,
		ATTRIBUTE,
		EQUALS,
		VALUE,
		COMMENT,
		TEXT,
		UNKNOWN,
		START,
		RAW,
		EOF,
		CONTENT, /* This doesn't exist in the JavaScript version, it uses a unique string value of "TK_CONTENT" */
	}

	private static final Directives directives_core = new Directives(Pattern.compile("<!--"), Pattern.compile("-->"));


	private final Patterns __patterns;
	private final HTMLOptions _options;

	private class Patterns {
		private TemplatablePattern word;
		private TemplatablePattern word_control_flow_close_excluded;
		private TemplatablePattern single_quote;
		private TemplatablePattern double_quote;
		private TemplatablePattern attribute;
		private TemplatablePattern element_name;
		private InputScannerPattern<?> angular_control_flow_start;
		private InputScannerPattern<?> handlebars_comment;
		private InputScannerPattern<?> handlebars;
		private InputScannerPattern<?> handlebars_open;
		private InputScannerPattern<?> handlebars_raw_close;
		private InputScannerPattern<?> comment;
		private InputScannerPattern<?> cdata;
		private InputScannerPattern<?> conditional_comment;
		private InputScannerPattern<?> processing;
		private InputScannerPattern<?> unformatted_content_delimiter;

		private Patterns() {
			// Words end at whitespace or when a tag starts
			// if we are indenting handlebars, they are considered tags
			var templatable_reader = new TemplatablePattern(_input).read_options(_options);
			var pattern_reader = new InputScannerPattern<>(_input);

			word = templatable_reader.until(Pattern.compile("[\n\r\t <]"));
			word_control_flow_close_excluded = templatable_reader.until(Pattern.compile("[\n\r\t <}]"));
			single_quote = templatable_reader.until_after(Pattern.compile("'"));
			double_quote = templatable_reader.until_after(Pattern.compile("\""));
			attribute = templatable_reader.until(Pattern.compile("[\n\r\t =>]|/>"));
			element_name = templatable_reader.until(Pattern.compile("[\n\r\t >/]"));

			angular_control_flow_start = pattern_reader.matching(Pattern.compile("\\@[a-zA-Z]+[^({]*[({]"));
			handlebars_comment = pattern_reader.starting_with(Pattern.compile("\\{\\{!--")).until_after(Pattern.compile("--\\}\\}"));
			handlebars = pattern_reader.starting_with(Pattern.compile("\\{\\{")).until_after(Pattern.compile("\\}\\}"));
			handlebars_open = pattern_reader.until(Pattern.compile("[\n\r\t }]"));
			handlebars_raw_close = pattern_reader.until(Pattern.compile("\\}\\}"));
			comment = pattern_reader.starting_with(Pattern.compile("<!--")).until_after(Pattern.compile("-->"));
			cdata = pattern_reader.starting_with(Pattern.compile("<!\\[CDATA\\[")).until_after(Pattern.compile("]]>"));
			// https://en.wikipedia.org/wiki/Conditional_comment
			conditional_comment = pattern_reader.starting_with(Pattern.compile("<!\\[")).until_after(Pattern.compile("]>"));
			processing = pattern_reader.starting_with(Pattern.compile("<\\?")).until_after(Pattern.compile("\\?>"));

			if (_options.unformatted_content_delimiter != null && !_options.unformatted_content_delimiter.isEmpty()) {
				var literal_regexp = Pattern.compile(Pattern.quote(_options.unformatted_content_delimiter));
				unformatted_content_delimiter =
					pattern_reader.matching(literal_regexp)
					.until_after(literal_regexp);
			}
		}
	}

	private String _current_tag_name;
	private String _unformatted_content_delimiter;

	public Tokenizer(String input_string, HTMLOptions options) {
		super(input_string, Token::createToken, TOKEN.START, TOKEN.RAW, TOKEN.EOF, options);

		this._options = options;

		this._current_tag_name = "";


		this.__patterns = new Patterns();

		if (this._options.indent_handlebars) {
			this.__patterns.word = this.__patterns.word.exclude(TemplateLanguage.handlebars);
			this.__patterns.word_control_flow_close_excluded = this.__patterns.word_control_flow_close_excluded.exclude(TemplateLanguage.handlebars);
		}

		this._unformatted_content_delimiter = null;
	}

	@Override
	protected boolean _is_comment(Token current_token) {
		return false; //current_token.type === TOKEN.COMMENT || current_token.type === TOKEN.UNKNOWN;
	}

	@Override
	protected boolean _is_opening(Token current_token) {
		return current_token.type == TOKEN.TAG_OPEN || current_token.type == TOKEN.CONTROL_FLOW_OPEN;
	}

	@Override
	protected boolean _is_closing(Token current_token, @Nullable Token open_token) {
		return (current_token.type == TOKEN.TAG_CLOSE &&
			(open_token != null && (
				((">".equals(current_token.text) || "/>".equals(current_token.text)) && open_token.text.startsWith("<")) ||
				("}}".equals(current_token.text) && open_token.text.startsWith("{") && open_token.text.substring(1, 2).equals("{"))))
			) || (current_token.type == TOKEN.CONTROL_FLOW_CLOSE &&
			("}".equals(current_token.text) && open_token.text.endsWith("{")));
	}

	@Override
	protected void _reset() {
		this._current_tag_name = "";
	}

	@Override
	protected Token _get_next_token(Token previous_token, @Nullable Token open_token) {
		Token token = null;
		this._readWhitespace();
		var c = this._input.peek();

		if (c == null) {
			return this._create_token(TOKEN.EOF, "");
		}

		if (token == null) {
			token = this._read_open_handlebars(c, open_token);
		}
		if (token == null) {
			token = this._read_attribute(c, previous_token, open_token);
		}
		if (token == null) {
			token = this._read_close(c, open_token);
		}
		if (token == null) {
			token = this._read_script_and_style(c, previous_token);
		}
		if (token == null) {
			token = this._read_control_flows(c, open_token);
		}
		if (token == null) {
			token = this._read_raw_content(c, previous_token, open_token);
		}
		if (token == null) {
			token = this._read_content_word(c, open_token);
		}
		if (token == null) {
			token = this._read_comment_or_cdata(c);
		}
		if (token == null) {
			token = this._read_processing(c);
		}
		if (token == null) {
			token = this._read_open(c, open_token);
		}
		if (token == null) {
			token = this._create_token(TOKEN.UNKNOWN, this._input.next());
		}

		return token;
	}

	@Nullable
	private Token _read_comment_or_cdata(String c) {
		Token token = null;
		String resulting_string = null;
		Map<String, String> directives = null;

		if ("<".equals(c)) {
			var peek1 = this._input.peek(1);
			// We treat all comments as literals, even more than preformatted tags
			// we only look for the appropriate closing marker
			if ("!".equals(peek1)) {
				resulting_string = this.__patterns.comment.read();

				// only process directive on html comments
				if (resulting_string != null && !resulting_string.isEmpty()) {
					directives = directives_core.get_directives(resulting_string);
					if (directives != null && "start".equals(directives.get("ignore"))) {
						resulting_string += directives_core.readIgnored(this._input);
					}
				} else {
					resulting_string = this.__patterns.cdata.read();
				}
			}

			if (resulting_string != null && !resulting_string.isEmpty()) {
				token = this._create_token(TOKEN.COMMENT, resulting_string);
				token.directives = directives;
			}
		}

		return token;
	}

	@Nullable
	private Token _read_processing(String c) {
		Token token = null;
		String resulting_string = null;
		Map<String, String> directives = null;

		if ("<".equals(c)) {
			var peek1 = this._input.peek(1);
			if ("!".equals(peek1) || "?".equals(peek1)) {
				resulting_string = this.__patterns.conditional_comment.read();
				if (resulting_string == null || resulting_string.isEmpty()) {
					resulting_string = this.__patterns.processing.read();
				}
			}

			if (resulting_string != null && !resulting_string.isEmpty()) {
				token = this._create_token(TOKEN.COMMENT, resulting_string);
				token.directives = directives;
			}
		}

		return token;
	}

	@Nullable
	private Token _read_open(String c, @Nullable Token open_token) {
		String resulting_string = null;
		Token token = null;
		if (open_token == null || open_token.type == TOKEN.CONTROL_FLOW_OPEN) {
			if ("<".equals(c)) {

				resulting_string = this._input.next();
				if ("/".equals(this._input.peek())) {
					resulting_string += this._input.next();
				}
				resulting_string += this.__patterns.element_name.read();
				token = this._create_token(TOKEN.TAG_OPEN, resulting_string);
			}
		}
		return token;
	}

	@Nullable
	private Token _read_open_handlebars(String c, @Nullable Token open_token) {
		String resulting_string = null;
		Token token = null;
		if (open_token == null || open_token.type == TOKEN.CONTROL_FLOW_OPEN) {
			if ((this._options.templating.contains(TemplateLanguage.angular) || this._options.indent_handlebars) && "{".equals(c) && "{".equals(this._input.peek(1))) {
				if (this._options.indent_handlebars && "!".equals(this._input.peek(2))) {
					resulting_string = this.__patterns.handlebars_comment.read();
					if (resulting_string == null || resulting_string.isEmpty()) {
						resulting_string = this.__patterns.handlebars.read();
					}
					token = this._create_token(TOKEN.COMMENT, resulting_string);
				} else {
					resulting_string = this.__patterns.handlebars_open.read();
					token = this._create_token(TOKEN.TAG_OPEN, resulting_string);
				}
			}
		}
		return token;
	}

	private Token _read_control_flows(String c, @Nullable Token open_token) {
		var resulting_string = "";
		Token token = null;
		// Only check for control flows if angular templating is set
		if (!this._options.templating.contains(TemplateLanguage.angular)) {
			return token;
		}

		if ("@".equals(c)) {
			resulting_string = this.__patterns.angular_control_flow_start.read();
			if ("".equals(resulting_string)) {
				return token;
			}

			var opening_parentheses_count = resulting_string.endsWith("(") ? 1 : 0;
			var closing_parentheses_count = 0;
			// The opening brace of the control flow is where the number of opening and closing parentheses equal
			// e.g. @if({value: true} !== null) { 
			while (!(resulting_string.endsWith("{") && opening_parentheses_count == closing_parentheses_count)) {
				var next_char = this._input.next();
				if (next_char == null) {
					break;
				} else if ("(".equals(next_char)) {
					opening_parentheses_count++;
				} else if (")".equals(next_char)) {
					closing_parentheses_count++;
				}
				resulting_string += next_char;
			}
			token = this._create_token(TOKEN.CONTROL_FLOW_OPEN, resulting_string);
		} else if ("}".equals(c) && open_token != null && open_token.type == TOKEN.CONTROL_FLOW_OPEN) {
			resulting_string = this._input.next();
			token = this._create_token(TOKEN.CONTROL_FLOW_CLOSE, resulting_string);
		}
		return token;
	}

	@Nullable
	private Token _read_close(String c, @Nullable Token open_token) {
		String resulting_string = null;
		Token token = null;
		if (open_token != null && open_token.type == TOKEN.TAG_OPEN) {
			if (open_token.text.startsWith("<") && (">".equals(c) || ("/".equals(c) && ">".equals(this._input.peek(1))))) {
				resulting_string = this._input.next();
				if ("/".equals(c)) { //  for close tag "/>"
					resulting_string += this._input.next();
				}
				token = this._create_token(TOKEN.TAG_CLOSE, resulting_string);
			} else if (open_token.text.startsWith("{") && "}".equals(c) && "}".equals(this._input.peek(1))) {
				this._input.next();
				this._input.next();
				token = this._create_token(TOKEN.TAG_CLOSE, "}}");
			}
		}

		return token;
	}

	@Nullable
	private Token _read_attribute(String c, @Nullable Token previous_token, @Nullable Token open_token) {
		Token token = null;
		var resulting_string = "";
		if (open_token != null && open_token.text.startsWith("<")) {

			if ("=".equals(c)) {
				token = this._create_token(TOKEN.EQUALS, this._input.next());
			} else if ("\"".equals(c) || "'".equals(c)) {
				var content = this._input.next();
				if ("\"".equals(c)) {
					content += this.__patterns.double_quote.read();
				} else {
					content += this.__patterns.single_quote.read();
				}
				token = this._create_token(TOKEN.VALUE, content);
			} else {
				resulting_string = this.__patterns.attribute.read();

				if (resulting_string != null && !resulting_string.isEmpty()) {
					if (previous_token.type == TOKEN.EQUALS) {
						token = this._create_token(TOKEN.VALUE, resulting_string);
					} else {
						token = this._create_token(TOKEN.ATTRIBUTE, resulting_string);
					}
				}
			}
		}
		return token;
	}

	private boolean _is_content_unformatted(String tag_name) {
		// void_elements have no content and so cannot have unformatted content
		// script and style tags should always be read as unformatted content
		// finally content_unformatted and unformatted element contents are unformatted
		return !this._options.void_elements.contains(tag_name) &&
			(this._options.content_unformatted.contains(tag_name) ||
				this._options.unformatted.contains(tag_name));
	}

	@Nullable
	private Token _read_raw_content(String c, Token previous_token, @Nullable Token open_token) {
		var resulting_string = "";
		if (open_token != null && open_token.text.startsWith("{")) {
			resulting_string = this.__patterns.handlebars_raw_close.read();
		} else if (previous_token.type == TOKEN.TAG_CLOSE &&
			previous_token.opened.text.startsWith("<") && !previous_token.text.startsWith("/")) {
			// ^^ empty tag has no content 
			var tag_name = previous_token.opened.text.substring(1, previous_token.opened.text.length()).toLowerCase();
			if (this._is_content_unformatted(tag_name)) {
				resulting_string = this._input.readUntil(Pattern.compile("</" + tag_name + "[\\n\\r\\t ]*?>", Pattern.CASE_INSENSITIVE));
			}
		}

		if (resulting_string != null && !resulting_string.isEmpty()) {
			return this._create_token(TOKEN.TEXT, resulting_string);
		}

		return null;
	}

	private Token _read_script_and_style(String c, Token previous_token) {
		if (previous_token.type == TOKEN.TAG_CLOSE && previous_token.opened.text.startsWith("<") && !previous_token.text.startsWith("/")) {
			var tag_name = previous_token.opened.text.substring(1).toLowerCase();

			if ("script".equals(tag_name) || "style".equals(tag_name)) {
				// Script and style tags are allowed to have comments wrapping their content
				// or just have regular content.
				var token = this._read_comment_or_cdata(c);
				if (token != null) {
					token.type = TOKEN.TEXT;
					return token;
				}
				var resulting_string = this._input.readUntil(Pattern.compile("</" + tag_name + "[\n\r\t ]*?>", Pattern.CASE_INSENSITIVE));
				if (resulting_string != null && !resulting_string.isEmpty()) {
					return this._create_token(TOKEN.TEXT, resulting_string);
				}
			}
		}

		return null;
	}

	@Nullable
	private Token _read_content_word(String c, @Nullable Token open_token) {
		var resulting_string = "";
		if (this._options.unformatted_content_delimiter != null && !this._options.unformatted_content_delimiter.isEmpty()) {
			if (this._options.unformatted_content_delimiter.substring(0, 1).equals(c)) {
				resulting_string = this.__patterns.unformatted_content_delimiter.read();
			}
		}

		if (resulting_string == null || resulting_string.isEmpty()) {
			resulting_string = (open_token != null && open_token.type == TOKEN.CONTROL_FLOW_OPEN) ? this.__patterns.word_control_flow_close_excluded.read() : this.__patterns.word.read();
		}
		if (resulting_string != null && !resulting_string.isEmpty()) {
			return this._create_token(TOKEN.TEXT, resulting_string);
		}
		return null;
	}

}
