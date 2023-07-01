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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.beautifier.core.Directives;
import io.beautifier.core.InputScanner;
import io.beautifier.core.InputScannerPattern;
import io.beautifier.core.TemplatablePattern;

@NonNullByDefault
class Tokenizer extends io.beautifier.core.Tokenizer<Tokenizer.TOKEN, Token> {

	@SafeVarargs
	static <T> boolean in_array(T what, @NonNull T... arr) {
		for (T a : arr) {
			if (a.equals(what)) {
			return true;
			}
		}
		return false;
	}


	enum TOKEN {
		START,
		RAW,
		EOF,
		START_EXPR,
		END_EXPR,
		START_BLOCK,
		END_BLOCK,
		WORD,
		RESERVED,
		SEMICOLON,
		STRING,
		EQUALS,
		OPERATOR,
		COMMA,
		BLOCK_COMMENT,
		COMMENT,
		DOT,
		UNKNOWN,
		;
	}

	private static final Directives directives_core = new Directives(Pattern.compile("/\\*"), Pattern.compile("\\*/"));

	private static final Pattern number_pattern = Pattern.compile("0[xX][0123456789abcdefABCDEF_]*n?|0[oO][01234567_]*n?|0[bB][01_]*n?|\\d[\\d_]*n|(?:\\.\\d[\\d_]*|\\d[\\d_]*\\.?[\\d_]*)(?:[eE][+-]?[\\d_]+)?");

	private static final Pattern digit = Pattern.compile("[0-9]");

	// Dot "." must be distinguished from "..." and decimal
	private static final Pattern dot_pattern = Pattern.compile("[^\\d\\.]");

	static final @NonNull String[] positionable_operators = 
		(">>> === !== &&= ??= ||= " +
		"<< && >= ** != == <= >> || ?? |> " +
		"< / - + > : & % ? ^ | *").split(" ");

	private static final Pattern punct_pattern;

	static {
		// IMPORTANT: this must be sorted longest to shortest or tokenizing many not work.
		// Also, you must update possitionable operators separately from punct
		String punct =
			">>>= " +
			"... >>= <<= === >>> !== **= &&= ??= ||= " +
			"=> ^= :: /= << <= == && -= >= >> != -- += ** || ?? ++ %= &= *= |= |> " +
			"= ! ? > < : / ^ - + * & % ~ |";
		punct = punct.replaceAll("[-\\[\\]{}()*+?.,^$|#]", "\\\\$0");
		// ?. but not if followed by a number 
		punct = "\\?\\.(?!\\d) " + punct;
		punct = punct.replace(' ', '|');

		punct_pattern = Pattern.compile(punct);
	}

	// words which should always start on new line.
	static final @NonNull String[] line_starters = "continue,try,throw,return,var,let,const,if,switch,case,default,for,while,break,function,import,export".split(",");
	private static final String[] reserved_words;
	
	static {
		List<String> reservedWords = new ArrayList<>(Arrays.asList(line_starters));
		reservedWords.addAll(Arrays.asList("do", "in", "of", "else", "get", "set", "new", "catch", "finally", "typeof", "yield", "async", "await", "from", "as", "class", "extends"));
		reserved_words = reservedWords.toArray(new String[reservedWords.size()]);
	}

	private static final Pattern reserved_word_pattern = Pattern.compile("^(?:" + String.join("|", reserved_words) + ")$");

	// var template_pattern = /(?:(?:<\?php|<\?=)[\s\S]*?\?>)|(?:<%[\s\S]*?%>)/g;

	private boolean in_html_comment;
	private boolean has_char_escapes;

	private final Patterns __patterns;
	private final Options _options;

	private class Patterns {
		private TemplatablePattern template;
		private InputScannerPattern identifier;
		private InputScannerPattern number;
		private InputScannerPattern punct;
		private InputScannerPattern comment;
		private InputScannerPattern block_comment;
		private InputScannerPattern html_comment_start;
		private InputScannerPattern html_comment_end;
		private InputScannerPattern include;
		private InputScannerPattern shebang;
		private InputScannerPattern xml;
		private InputScannerPattern single_quote;
		private InputScannerPattern double_quote;
		private InputScannerPattern template_text;
		private InputScannerPattern template_expression;

		private Patterns() {
			var pattern_reader = new InputScannerPattern(_input);
			var templatable = new TemplatablePattern(_input)
				.read_options(_options);
				
			this.template = templatable;
			this.identifier = templatable.starting_with(Acorn.identifier).matching(Acorn.identifierMatch);
			this.number = pattern_reader.matching(number_pattern);
			this.punct = pattern_reader.matching(punct_pattern);
			// comment ends just before nearest linefeed or end of file
			this.comment = pattern_reader.starting_with(Pattern.compile("\\/\\/")).until(Pattern.compile("[\n\r\u2028\u2029]"));
			//  /* ... */ comment ends with nearest */ or end of file
			this.block_comment = pattern_reader.starting_with(Pattern.compile("/\\*")).until_after(Pattern.compile("\\*/"));
			this.html_comment_start = pattern_reader.matching(Pattern.compile("<!--"));
			this.html_comment_end = pattern_reader.matching(Pattern.compile("-->"));
			this.include = pattern_reader.starting_with(Pattern.compile("#include")).until_after(Acorn.lineBreak);
			this.shebang = pattern_reader.starting_with(Pattern.compile("#!")).until_after(Acorn.lineBreak);
			this.xml = pattern_reader.matching(Pattern.compile("[\\s\\S]*?<(/?)([-a-zA-Z:0-9_.]+|\\{[^}]+?}|!\\[CDATA\\[[^\\]]*?\\]\\]|)(\\s*\\{[^}]+?}|\\s+[-a-zA-Z:0-9_.]+|\\s+[-a-zA-Z:0-9_.]+\\s*=\\s*('[^']*'|\"[^\"]*\"|\\{([^{}]|\\{[^}]+?})+?}))*\\s*(/?)\\s*>"));
			this.single_quote = templatable.until(Pattern.compile("['\\\\\n\r\u2028\u2029]"));
			this.double_quote = templatable.until(Pattern.compile("[\"\\\\\n\r\u2028\u2029]"));
			this.template_text = templatable.until(Pattern.compile("[`\\$]"));
			this.template_expression = templatable.until(Pattern.compile("[`}\\\\]"));
		}
	}

	public Tokenizer(String input_string, Options options) {
		super(input_string, Token::createToken, TOKEN.START, TOKEN.RAW, TOKEN.EOF, options);

		this._options = options;
		this._patterns.whitespace = this._patterns.whitespace.matching(
			"\u00A0\u1680\u180e\u2000-\u200a\u202f\u205f\u3000\ufeff",
			"\u2028\u2029");

		this.__patterns = new Patterns();
	}

	@Override
	protected boolean _is_comment(Token current_token) {
		return current_token.type == TOKEN.COMMENT || current_token.type == TOKEN.BLOCK_COMMENT || current_token.type == TOKEN.UNKNOWN;
	}

	@Override
	protected boolean _is_opening(Token current_token) {
		return current_token.type == TOKEN.START_BLOCK || current_token.type == TOKEN.START_EXPR;
	}

	@Override
	protected boolean _is_closing(Token current_token, @Nullable Token open_token) {
		return (current_token.type == TOKEN.END_BLOCK || current_token.type == TOKEN.END_EXPR) &&
			(open_token != null && (
			(current_token.text.equals("]") && open_token.text.equals("[")) ||
			(current_token.text.equals(")") && open_token.text.equals("(")) ||
			(current_token.text.equals("}") && open_token.text.equals("{"))));
	}

	@Override
	protected void _reset() {
		in_html_comment = false;
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
			token = this._read_non_javascript(c);
		}
		if (token == null) {
			token = this._read_string(c);
		}
		if (token == null) {
			token = this._read_pair(c, this._input.peek(1)); // Issue #2062 hack for record type '#{'
		}
		if (token == null) {
			token = this._read_word(previous_token);
		}
		if (token == null) {
			token = this._read_singles(c);
		}
		if (token == null) {
			token = this._read_comment(c);
		}
		if (token == null) {
			token = this._read_regexp(c, previous_token);
		}
		if (token == null) {
			token = this._read_xml(c, previous_token);
		}
		if (token == null) {
			token = this._read_punctuation();
		}
		if (token == null) {
			token = this._create_token(TOKEN.UNKNOWN, this._input.next());
		}
		return token;
	}

	@Nullable
	protected Token _read_word(Token previous_token) {
		String resulting_string;
		resulting_string = this.__patterns.identifier.read();
		if (!resulting_string.isEmpty()) {
			resulting_string = resulting_string.replaceAll(Acorn.lineBreak.pattern(), "\n");
			if (!(previous_token.type == TOKEN.DOT ||
				(previous_token.type == TOKEN.RESERVED && (previous_token.text.equals("set") || previous_token.text.equals("get")))) &&
				reserved_word_pattern.matcher(resulting_string).find())
			{
				if ((resulting_string.equals("in") || resulting_string.equals("of")) &&
					(previous_token.type == TOKEN.WORD || previous_token.type == TOKEN.STRING)) { // hack for 'in' and 'of' operators
					return this._create_token(TOKEN.OPERATOR, resulting_string);
				}
				return this._create_token(TOKEN.RESERVED, resulting_string);
			}
			return this._create_token(TOKEN.WORD, resulting_string);
		}

		resulting_string = this.__patterns.number.read();
		if (!resulting_string.isEmpty()) {
			return this._create_token(TOKEN.WORD, resulting_string);
		} else {
			return null;
		}
	}

	@Nullable
	protected Token _read_singles(String c) {
		Token token = null;
		if ("(".equals(c) || "[".equals(c)) {
			token = this._create_token(TOKEN.START_EXPR, c);
		} else if (")".equals(c) || "]".equals(c)) {
			token = this._create_token(TOKEN.END_EXPR, c);
		} else if ("{".equals(c)) {
			token = this._create_token(TOKEN.START_BLOCK, c);
		} else if ("}".equals(c)) {
			token = this._create_token(TOKEN.END_BLOCK, c);
		} else if (";".equals(c)) {
			token = this._create_token(TOKEN.SEMICOLON, c);
		} else if (".".equals(c) && dot_pattern.matcher(Objects.toString(this._input.peek(1), "")).find()) {
			token = this._create_token(TOKEN.DOT, c);
		} else if (",".equals(c)) {
			token = this._create_token(TOKEN.COMMA, c);
		}

		if (token != null) {
			this._input.next();
		}
		return token;
	}

	@Nullable
	protected Token _read_pair(String c, String d) {
		Token token = null;
		if ("#".equals(c) && "{".equals(d)) {
			token = this._create_token(TOKEN.START_BLOCK, c + d);
		}

		if (token != null) {
			this._input.next();
			this._input.next();
		}
		return token;
	}

	@Nullable
	protected Token _read_punctuation() {
		var resulting_string = this.__patterns.punct.read();

		if (!resulting_string.isEmpty()) {
			if (resulting_string.equals("=")) {
				return this._create_token(TOKEN.EQUALS, resulting_string);
			} else if (resulting_string.equals("?.")) {
				return this._create_token(TOKEN.DOT, resulting_string);
			} else {
				return this._create_token(TOKEN.OPERATOR, resulting_string);
			}
		}

		return null;
	}

	@Nullable
	protected Token _read_non_javascript(String c) {
		var resulting_string = "";

		if ("#".equals(c)) {
			if (this._is_first_token()) {
				resulting_string = this.__patterns.shebang.read();

				if (!resulting_string.isEmpty()) {
					return this._create_token(TOKEN.UNKNOWN, resulting_string.trim() + '\n');
				}
			}

			// handles extendscript #includes
			resulting_string = this.__patterns.include.read();

			if (!resulting_string.isEmpty()) {
				return this._create_token(TOKEN.UNKNOWN, resulting_string.trim() + '\n');
			}

			c = this._input.next();

			// Spidermonkey-specific sharp variables for circular references. Considered obsolete.
			final StringBuilder sharp = new StringBuilder("#");
			if (this._input.hasNext() && this._input.testChar(digit)) {
				do {
					c = this._input.next();
					sharp.append(c);
				} while (this._input.hasNext() && !"#".equals(c) && !"=".equals(c));
				if ("#".equals(c)) {
					//
				} else if ("[".equals(this._input.peek()) && "]".equals(this._input.peek(1))) {
					sharp.append("[]");
					this._input.next();
					this._input.next();
				} else if ("{".equals(this._input.peek()) && "}".equals(this._input.peek(1))) {
					sharp.append("{}");
					this._input.next();
					this._input.next();
				}
				return this._create_token(TOKEN.WORD, sharp.toString());
			}

			this._input.back();

		} else if ("<".equals(c) && this._is_first_token()) {
			resulting_string = this.__patterns.html_comment_start.read();
			if (!resulting_string.isEmpty()) {
				while (this._input.hasNext() && !this._input.testChar(Acorn.newline)) {
					resulting_string += this._input.next();
				}
				in_html_comment = true;
				return this._create_token(TOKEN.COMMENT, resulting_string);
			}
		} else if (in_html_comment && "-".equals(c)) {
			resulting_string = this.__patterns.html_comment_end.read();
			if (!resulting_string.isEmpty()) {
				in_html_comment = false;
				return this._create_token(TOKEN.COMMENT, resulting_string);
			}
		}

		return null;
	}

	@Nullable
	protected Token _read_comment(String c) {
		Token token = null;
		if ("/".equals(c)) {
			var comment = "";
			if ("*".equals(this._input.peek(1))) {
				// peek for comment /* ... */
				comment = this.__patterns.block_comment.read();
				var directives = directives_core.get_directives(comment);
				if (directives != null && "start".equals(directives.get("ignore"))) {
					comment += directives_core.readIgnored(this._input);
				}
				comment = comment.replaceAll(Acorn.lineBreak.pattern(), "\n");
				token = this._create_token(TOKEN.BLOCK_COMMENT, comment);
				token.directives = directives;
			} else if ("/".equals(this._input.peek(1))) {
				// peek for comment // ...
				comment = this.__patterns.comment.read();
				token = this._create_token(TOKEN.COMMENT, comment);
			}
		}
		return token;
	}

	@Nullable
	protected Token _read_string(String c) {
		if ("`".equals(c) || "'".equals(c) || "\"".equals(c)) {
			String resulting_string = this._input.next();
			this.has_char_escapes = false;

			if ("`".equals(c)) {
				resulting_string += this._read_string_recursive("`", true, "${");
			} else {
				resulting_string += this._read_string_recursive(c);
			}

			if (this.has_char_escapes && this._options.unescape_strings) {
				resulting_string = unescape_string(resulting_string);
			}

			if (c.equals(this._input.peek())) {
				resulting_string += this._input.next();
			}

			resulting_string = resulting_string.replaceAll(Acorn.lineBreak.pattern(), "\n");

			return this._create_token(TOKEN.STRING, resulting_string);
		}

		return null;
	}

	protected boolean _allow_regexp_or_xml(Token previous_token) {
		// regex and xml can only appear in specific locations during parsing
		return (previous_token.type == TOKEN.RESERVED && in_array(previous_token.text, new String @NonNull[] { "return", "case", "throw", "else", "do", "typeof", "yield" })) ||
			(previous_token.type == TOKEN.END_EXPR && ")".equals(previous_token.text) &&
			previous_token.opened.previous.type == TOKEN.RESERVED && in_array(previous_token.opened.previous.text, "if", "while", "for")) ||
			(EnumSet.of(TOKEN.COMMENT, TOKEN.START_EXPR, TOKEN.START_BLOCK, TOKEN.START,
			TOKEN.END_BLOCK, TOKEN.OPERATOR, TOKEN.EQUALS, TOKEN.EOF, TOKEN.SEMICOLON, TOKEN.COMMA).contains(previous_token.type)
			);
	}

	@Nullable
	protected Token _read_regexp(String c, Token previous_token) {
		if ("/".equals(c) && this._allow_regexp_or_xml(previous_token)) {
			// handle regexp
			//
			final StringBuilder resulting_string = new StringBuilder(this._input.next());
			var esc = false;

			var in_char_class = false;
			while (this._input.hasNext() &&
			((esc || in_char_class || !c.equals(this._input.peek())) &&
				!this._input.testChar(Acorn.newline))) {
				resulting_string.append(this._input.peek());
				if (!esc) {
					esc = "\\".equals(this._input.peek());
					if ("[".equals(this._input.peek())) {
						in_char_class = true;
					} else if ("]".equals(this._input.peek())) {
						in_char_class = false;
					}
				} else {
					esc = false;
				}
				this._input.next();
			}

			if (c.equals(this._input.peek())) {
				resulting_string.append(this._input.next());

				// regexps may have modifiers /regexp/MOD , so fetch those, too
				// Only [gim] are valid, but if the user puts in garbage, do what we can to take it.
				resulting_string.append(this._input.read(Acorn.identifier));
			}
			return this._create_token(TOKEN.STRING, resulting_string.toString());
		}
		return null;
	}

	@Nullable
	protected Token _read_xml(String c, Token previous_token) {
		if (this._options.e4x && "<".equals(c) && this._allow_regexp_or_xml(previous_token)) {
			final StringBuilder xmlStr = new StringBuilder();
			var match = this.__patterns.xml.read_match();
			// handle e4x xml literals
			//
			if (match != null) {
				// Trim root tag to attempt to
				var rootTag = match.group(2).replaceFirst("^\\{\\s+", "{").replaceFirst("\\s+}$", "}");
				var isCurlyRoot = rootTag.indexOf('{') == 0;
				var depth = 0;
				while (match != null) {
					var isEndTag = match.group(1) != null && !match.group(1).isEmpty();
					var tagName = match.group(2);
					var isSingletonTag = (match.group(match.groupCount()) != null && !match.group(match.groupCount()).isEmpty()) || (tagName.length() >= 8 && tagName.substring(0, 8).equals("![CDATA["));
					if (!isSingletonTag &&
					(tagName.equals(rootTag) || (isCurlyRoot && !tagName.replaceFirst("^\\{\s+", "{").replaceFirst("\\s+}$", "}").isEmpty()))) {
						if (isEndTag) {
							--depth;
						} else {
							++depth;
						}
					}
					xmlStr.append(match.group());
					if (depth <= 0) {
						break;
					}
					match = this.__patterns.xml.read_match();
				}
				// if we didn't close correctly, keep unformatted.
				if (match == null) {
					xmlStr.append(this._input.match(Pattern.compile("[\\s\\S]*")).group());
				}
				return this._create_token(TOKEN.STRING, xmlStr.toString().replaceAll(Acorn.lineBreak.pattern(), "\n"));
			}
		}

		return null;
	}

	public String unescape_string(String s) {
		// You think that a regex would work for this
		// return s.replace(/\\x([0-9a-f]{2})/gi, function(match, val) {
		//         return String.fromCharCode(parseInt(val, 16));
		//     })
		// However, dealing with '\xff', '\\xff', '\\\xff' makes this more fun.
		final StringBuilder out = new StringBuilder();
		var escaped = 0;

		var input_scan = new InputScanner(s);
		Matcher matched = null;

		while (input_scan.hasNext()) {
			// Keep any whitespace, non-slash characters
			// also keep slash pairs.
			matched = input_scan.match(Pattern.compile("([\\s]|[^\\\\]|\\\\\\\\)+"));

			if (matched != null) {
				out.append(matched.group());
			}

			if ("\\".equals(input_scan.peek())) {
				input_scan.next();
				if ("x".equals(input_scan.peek())) {
					matched = input_scan.match(Pattern.compile("x([0-9A-Fa-f]{2})"));
				} else if ("u".equals(input_scan.peek())) {
					matched = input_scan.match(Pattern.compile("u([0-9A-Fa-f]{4})"));
				} else {
					out.append('\\');
					if (input_scan.hasNext()) {
						out.append(input_scan.next());
					}
					continue;
				}

				// If there's some error decoding, return the original string
				if (matched == null) {
					return s;
				}

				escaped = Integer.parseInt(matched.group(1), 16);

				if (escaped > 0x7e && escaped <= 0xff && matched.group().indexOf('x') == 0) {
					// we bail out on \x7f..\xff,
					// leaving whole string escaped,
					// as it's probably completely binary
					return s;
				} else if (escaped >= 0x00 && escaped < 0x20) {
					// leave 0x00...0x1f escaped
					out.append('\\');
					out.append(matched.group());
					continue;
				} else if (escaped == 0x22 || escaped == 0x27 || escaped == 0x5c) {
					// single-quote, apostrophe, backslash - escape these
					out.append('\\');
					out.append((char) escaped);
				} else {
					out.append((char) escaped);
				}
			}
		}

		return out.toString();
	}

	private String _read_string_recursive(String delimiter) {
		return _read_string_recursive(delimiter, false, null);
	}

	// handle string
	//
	private String _read_string_recursive(String delimiter, boolean allow_unescaped_newlines, @Nullable String start_sub) {
		String current_char;
		InputScannerPattern pattern;
		if ("'".equals(delimiter)) {
			pattern = this.__patterns.single_quote;
		} else if ("\"".equals(delimiter)) {
			pattern = this.__patterns.double_quote;
		} else if ("`".equals(delimiter)) {
			pattern = this.__patterns.template_text;
		} else if ("}".equals(delimiter)) {
			pattern = this.__patterns.template_expression;
		} else {
			throw new IllegalArgumentException("Unsupported delimiter: " + delimiter);
		}

		var resulting_string = pattern.read();
		while (this._input.hasNext()) {
			String next = this._input.next();
			if (delimiter.equals(next) ||
				(!allow_unescaped_newlines && Acorn.newline.matcher(next).find()))
			{
				this._input.back();
				break;
			} else if ("\\".equals(next) && this._input.hasNext()) {
				current_char = this._input.peek();

				if ("x".equals(current_char) || "u".equals(current_char)) {
					this.has_char_escapes = true;
				} else if ("\r".equals(current_char) && "\n".equals(this._input.peek(1))) {
					this._input.next();
				}

				next += this._input.next();
			} else if (start_sub != null) {
				if (start_sub.equals("${") && "$".equals(next) && "{".equals(this._input.peek())) {
					next += this._input.next();
				}

				if (start_sub.equals(next)) {
					if ("`".equals(delimiter)) {
						next += this._read_string_recursive("}", allow_unescaped_newlines, "`");
					} else {
						next += this._read_string_recursive("`", allow_unescaped_newlines, "${");
					}
					if (this._input.hasNext()) {
						next += this._input.next();
					}
				}
			}
			next += pattern.read();
			resulting_string += next;
		}

		return resulting_string;
	}
}
