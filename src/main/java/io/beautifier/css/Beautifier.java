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

package io.beautifier.css;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.beautifier.core.Directives;
import io.beautifier.core.InputScanner;
import io.beautifier.core.Output;
import io.beautifier.css.Options.BraceStyle;

@NonNullByDefault
public class Beautifier {

	private static final Directives directives_core = new Directives(Pattern.compile("/\\*"), Pattern.compile("\\*/"));

	private static final Pattern lineBreak = Pattern.compile("\r\n|[\r\n]");

	// tokenizer
	private static final Pattern whitespaceChar = Pattern.compile("\\s");
	private static final Pattern whitespacePattern = Pattern.compile("(?:\\s|\n)+");
	private static final Pattern block_comment_pattern = Pattern.compile("/\\*(?:[\\s\\S]*?)((?:\\*/)|$)");
	private static final Pattern comment_pattern = Pattern.compile("//(?:[^\n\r\\u2028\\u2029]*)");


	// https://developer.mozilla.org/en-US/docs/Web/CSS/At-rule
	private static final Set<String> NESTED_AT_RULE = new HashSet<>(Arrays.asList(
		"page",
		"font-face",
		"keyframes",
		// also in CONDITIONAL_GROUP_RULE below
		"media",
		"supports",
		"document"
	));
	private static final Set<String> CONDITIONAL_GROUP_RULE = new HashSet<>(Arrays.asList(
		"media",
		"supports",
		"document"
	));
	private static final String[] NON_SEMICOLON_NEWLINE_PROPERTY = new String[] {
		"grid-template-areas",
		"grid-template"
	};

	private String _source_text;
	private final Options _options;

	private @Nullable String _ch;
	private @Nullable InputScanner _input;
	private @Nullable Output _output;
	private int _indentLevel;
	private int _nestedLevel;

	public Beautifier(@Nullable String source_text) {
		this(source_text, null);
	}

	public Beautifier(@Nullable String source_text, @Nullable Options options) {
		this._source_text = source_text != null ? source_text : "";
		// Allow the setting of language/file-type specific options
		// with inheritance of overall settings
		this._options = options != null ? options : new Options();
		this._ch = null;
		this._input = null;

		this._options.prepare();
	}

	private String eatString(String endChars) {
		final StringBuilder result = new StringBuilder();
		this._ch = this._input.next();
		while (this._ch != null) {
			result.append(this._ch);
			if ("\\".equals(this._ch)) {
				result.append(this._input.next());
			} else if (endChars.indexOf(this._ch) != -1 || "\n".equals(this._ch)) {
				break;
			}
			this._ch = this._input.next();
		}
		return result.toString();
	}

	private boolean eatWhitespace() {
		return eatWhitespace(false);
	}

	// Skips any white space in the source text from the current position.
	// When allowAtLeastOneNewLine is true, will output new lines for each
	// newline character found; if the user has preserve_newlines off, only
	// the first newline will be output
	private boolean eatWhitespace(boolean allowAtLeastOneNewLine) {
		var result = whitespaceChar.matcher(Objects.toString(this._input.peek(), "")).find();
		var newline_count = 0;
		while (whitespaceChar.matcher(Objects.toString(this._input.peek(), "")).find()) {
			this._ch = this._input.next();
			if (allowAtLeastOneNewLine && "\n".equals(this._ch)) {
				if (newline_count == 0 || newline_count < this._options.max_preserve_newlines) {
					newline_count++;
					this._output.add_new_line(true);
				}
			}
		}
		return result;
	}

	// Nested pseudo-class if we are insideRule
	// and the next special character found opens
	// a new block
	private boolean foundNestedPseudoClass() {
		var openParen = 0;
		var i = 1;
		var ch = this._input.peek(i);
		while (ch != null) {
			if ("{".equals(ch)) {
				return true;
			} else if ("(".equals(ch)) {
				// pseudoclasses can contain ()
				openParen += 1;
			} else if (")".equals(ch)) {
				if (openParen == 0) {
					return false;
				}
				openParen -= 1;
			} else if (";".equals(ch) || "}".equals(ch)) {
				return false;
			}
			i++;
			ch = this._input.peek(i);
		}
		return false;
	}

	private void print_string(String output_string) {
		this._output.set_indent(this._indentLevel);
		this._output.non_breaking_space = true;
		this._output.add_token(output_string);
	}

	private void preserveSingleSpace(boolean isAfterSpace) {
		if (isAfterSpace) {
			this._output.space_before_token = true;
		}
	}

	private void indent() {
		this._indentLevel++;
	}

	private void outdent() {
		if (this._indentLevel > 0) {
			this._indentLevel--;
		}
	}

	/*_____________________--------------------_____________________*/

	public String beautify() {
		if (this._options.disabled) {
			return this._source_text;
		}

		var source_text = this._source_text;
		var eol = this._options.eol;
		if ("auto".equals(eol)) {
			eol = "\n";
			Matcher matcher = lineBreak.matcher(source_text);
			if (matcher.find()) {
				eol = matcher.group();
			}
		}


		// HACK: newline parsing inconsistent. This brute force normalizes the this._input.
		source_text = lineBreak.matcher(source_text).replaceAll("\n");

		// reset
		final Matcher baseIdentStringMatcher = Pattern.compile("^[\t ]*").matcher(source_text);
		baseIdentStringMatcher.find();
		var baseIndentString = baseIdentStringMatcher.group();

		this._output = new Output(this._options, baseIndentString);
		this._input = new InputScanner(source_text);
		this._indentLevel = 0;
		this._nestedLevel = 0;

		this._ch = null;
		var parenLevel = 0;

		var insideRule = false;
		// This is the value side of a property value pair (blue in the following ex)
		// label { content: blue }
		var insidePropertyValue = false;
		var enteringConditionalGroup = false;
		var insideNonNestedAtRule = false;
		var insideScssMap = false;
		var topCharacter = this._ch;
		var insideNonSemiColonValues = false;
		String whitespace;
		boolean isAfterSpace;
		String previous_ch;

		while (true) {
			whitespace = this._input.read(whitespacePattern);
			isAfterSpace = !"".equals(whitespace);
			previous_ch = topCharacter;
			this._ch = this._input.next();
			if ("\\".equals(this._ch) && this._input.hasNext()) {
				this._ch += this._input.next();
			}
			topCharacter = this._ch;

			if (this._ch == null || this._ch.isEmpty()) {
				break;
			} else if ("/".equals(this._ch) && "*".equals(this._input.peek())) {
				// /* css comment */
				// Always start block comments on a new line.
				// This handles scenarios where a block comment immediately
				// follows a property definition on the same line or where
				// minified code is being beautified.
				this._output.add_new_line();
				this._input.back();

				var comment = this._input.read(block_comment_pattern);

				// Handle ignore directive
				var directives = directives_core.get_directives(comment);
				if (directives != null && "start".equals(directives.get("ignore"))) {
					comment += directives_core.readIgnored(this._input);
				}

				this.print_string(comment);

				// Ensures any new lines following the comment are preserved
				this.eatWhitespace(true);

				// Block comments are followed by a new line so they don't
				// share a line with other properties
				this._output.add_new_line();
			} else if ("/".equals(this._ch) && "/".equals(this._input.peek())) {
				// // single line comment
				// Preserves the space before a comment
				// on the same line as a rule
				this._output.space_before_token = true;
				this._input.back();
				this.print_string(this._input.read(comment_pattern));

				// Ensures any new lines following the comment are preserved
				this.eatWhitespace(true);
			} else if ("$".equals(this._ch)) {
				this.preserveSingleSpace(isAfterSpace);

				this.print_string(this._ch);

				// strip trailing space, if present, for hash property checks
				var variable = this._input.peekUntilAfter(Pattern.compile("[: ,;{}()\\[\\]/='\"]"));

				if (Pattern.compile("[ :]$").matcher(variable).find()) {
					// we have a variable or pseudo-class, add it and insert one space before continuing
					variable = this.eatString(": ").replaceFirst("\\s$", "");
					this.print_string(variable);
					this._output.space_before_token = true;
				}

				variable = variable.replaceFirst("\\s$", "");

				// might be sass variable
				if (parenLevel == 0 && variable.indexOf(':') != -1) {
					insidePropertyValue = true;
					this.indent();
				}
			} else if ("@".equals(this._ch)) {
				this.preserveSingleSpace(isAfterSpace);

				// deal with less property mixins @{...}
				if ("{".equals(this._input.peek())) {
					this.print_string(this._ch + this.eatString("}"));
				} else {
					this.print_string(this._ch);

					// strip trailing space, if present, for hash property checks
					var variableOrRule = this._input.peekUntilAfter(Pattern.compile("[: ,;{}()\\[\\]/='\"]"));

					if (Pattern.compile("[ :]$").matcher(variableOrRule).find()) {
						// we have a variable or pseudo-class, add it and insert one space before continuing
						variableOrRule = this.eatString(": ").replaceFirst("\\s$", "");
						this.print_string(variableOrRule);
						this._output.space_before_token = true;
					}

					variableOrRule = variableOrRule.replaceFirst("\\s$", "");

					// might be less variable
					if (parenLevel == 0 && variableOrRule.indexOf(':') != -1) {
						insidePropertyValue = true;
						this.indent();

						// might be a nesting at-rule
					} else if (NESTED_AT_RULE.contains(variableOrRule)) {
						this._nestedLevel += 1;
						if (CONDITIONAL_GROUP_RULE.contains(variableOrRule)) {
							enteringConditionalGroup = true;
						}

						// might be a non-nested at-rule
					} else if (parenLevel == 0 && !insidePropertyValue) {
						insideNonNestedAtRule = true;
					}
				}
			} else if ("#".equals(this._ch) && "{".equals(this._input.peek())) {
				this.preserveSingleSpace(isAfterSpace);
				this.print_string(this._ch + this.eatString("}"));
			} else if ("{".equals(this._ch)) {
				if (insidePropertyValue) {
					insidePropertyValue = false;
					this.outdent();
				}

				// non nested at rule becomes nested
				insideNonNestedAtRule = false;

				// when entering conditional groups, only rulesets are allowed
				if (enteringConditionalGroup) {
					enteringConditionalGroup = false;
					insideRule = (this._indentLevel >= this._nestedLevel);
				} else {
					// otherwise, declarations are also allowed
					insideRule = (this._indentLevel >= this._nestedLevel - 1);
				}
				if (this._options.newline_between_rules && insideRule) {
					if (this._output.previous_line != null && !"{".equals(this._output.previous_line.item(-1))) {
						this._output.ensure_empty_line_above("/", ",");
					}
				}

				this._output.space_before_token = true;

				// The difference in print_string and indent order is necessary to indent the '{' correctly
				if (this._options.brace_style == BraceStyle.expand) {
					this._output.add_new_line();
					this.print_string(this._ch);
					this.indent();
					this._output.set_indent(this._indentLevel);
				} else {
					// inside mixin and first param is object
					if ("(".equals(previous_ch)) {
						this._output.space_before_token = false;
					} else if (!",".equals(previous_ch)) {
						this.indent();
					}
					this.print_string(this._ch);
				}

				this.eatWhitespace(true);
				this._output.add_new_line();
			} else if ("}".equals(this._ch)) {
				this.outdent();
				this._output.add_new_line();
				if ("{".equals(previous_ch)) {
					this._output.trim(true);
				}

				if (insidePropertyValue) {
					this.outdent();
					insidePropertyValue = false;
				}
				this.print_string(this._ch);
				insideRule = false;
				if (this._nestedLevel != 0) {
					this._nestedLevel--;
				}

				this.eatWhitespace(true);
				this._output.add_new_line();

				if (this._options.newline_between_rules && !this._output.just_added_blankline()) {
					if (!"}".equals(this._input.peek())) {
						this._output.add_new_line(true);
					}
				}
				if (")".equals(this._input.peek())) {
					this._output.trim(true);
					if (this._options.brace_style == BraceStyle.expand) {
						this._output.add_new_line(true);
					}
				}
			} else if (":".equals(this._ch)) {

				for (var i = 0; i < NON_SEMICOLON_NEWLINE_PROPERTY.length; i++) {
					if (this._input.lookBack(NON_SEMICOLON_NEWLINE_PROPERTY[i])) {
						insideNonSemiColonValues = true;
						break;
					}
				}

				if ((insideRule || enteringConditionalGroup) && !(this._input.lookBack("&") || this.foundNestedPseudoClass()) && !this._input.lookBack("(") && !insideNonNestedAtRule && parenLevel == 0) {
					// 'property: value' delimiter
					// which could be in a conditional group query

					this.print_string(":");
					if (!insidePropertyValue) {
						insidePropertyValue = true;
						this._output.space_before_token = true;
						this.eatWhitespace(true);
						this.indent();
					}
				} else {
					// sass/less parent reference don't use a space
					// sass nested pseudo-class don't use a space

					// preserve space before pseudoclasses/pseudoelements, as it means "in any child"
					if (this._input.lookBack(" ")) {
						this._output.space_before_token = true;
					}
					if (":".equals(this._input.peek())) {
						// pseudo-element
						this._ch = this._input.next();
						this.print_string("::");
					} else {
						// pseudo-class
						this.print_string(":");
					}
				}
			} else if ("\"".equals(this._ch) || "'".equals(this._ch)) {
				var preserveQuoteSpace = "\"".equals(previous_ch) || "'".equals(previous_ch);
				this.preserveSingleSpace(preserveQuoteSpace || isAfterSpace);
				this.print_string(this._ch + this.eatString(this._ch));
				this.eatWhitespace(true);
			} else if (";".equals(this._ch)) {
				insideNonSemiColonValues = false;
				if (parenLevel == 0) {
					if (insidePropertyValue) {
						this.outdent();
						insidePropertyValue = false;
					}
					insideNonNestedAtRule = false;
					this.print_string(this._ch);
					this.eatWhitespace(true);

					// This maintains single line comments on the same
					// line. Block comments are also affected, but
					// a new line is always output before one inside
					// that section
					if (!"/".equals(this._input.peek())) {
						this._output.add_new_line();
					}
				} else {
					this.print_string(this._ch);
					this.eatWhitespace(true);
					this._output.space_before_token = true;
				}
			} else if ("(".equals(this._ch)) { // may be a url
				if (this._input.lookBack("url")) {
					this.print_string(this._ch);
					this.eatWhitespace();
					parenLevel++;
					this.indent();
					this._ch = this._input.next();
					if (")".equals(this._ch) || "\"".equals(this._ch) || "'".equals(this._ch)) {
						this._input.back();
					} else if (this._ch != null) {
						this.print_string(this._ch + this.eatString(")"));
						if (parenLevel != 0) {
							parenLevel--;
							this.outdent();
						}
					}
				} else {
					var space_needed = false;
					if (this._input.lookBack("with")) {
						// look back is not an accurate solution, we need tokens to confirm without whitespaces
						space_needed = true;
					}
					this.preserveSingleSpace(isAfterSpace || space_needed);
					this.print_string(this._ch);

					// handle scss/sass map
					if (insidePropertyValue && "$".equals(previous_ch) && this._options.selector_separator_newline) {
						this._output.add_new_line();
						insideScssMap = true;
					} else {
						this.eatWhitespace();
						parenLevel++;
						this.indent();
					}
				}
			} else if (")".equals(this._ch)) {
				if (parenLevel != 0) {
					parenLevel--;
					this.outdent();
				}
				if (insideScssMap && ";".equals(this._input.peek()) && this._options.selector_separator_newline) {
					insideScssMap = false;
					this.outdent();
					this._output.add_new_line();
				}
				this.print_string(this._ch);
			} else if (",".equals(this._ch)) {
				this.print_string(this._ch);
				this.eatWhitespace(true);
				if (this._options.selector_separator_newline && (!insidePropertyValue || insideScssMap) && parenLevel == 0 && !insideNonNestedAtRule) {
					this._output.add_new_line();
				} else {
					this._output.space_before_token = true;
				}
			} else if ((">".equals(this._ch) || "+".equals(this._ch) || "~".equals(this._ch)) && !insidePropertyValue && parenLevel == 0) {
				//handle combinator spacing
				if (this._options.space_around_combinator) {
					this._output.space_before_token = true;
					this.print_string(this._ch);
					this._output.space_before_token = true;
				} else {
					this.print_string(this._ch);
					this.eatWhitespace();
					// squash extra whitespace
					if (this._ch != null && whitespaceChar.matcher(this._ch).find()) {
						this._ch = "";
					}
				}
			} else if ("]".equals(this._ch)) {
				this.print_string(this._ch);
			} else if ("[".equals(this._ch)) {
				this.preserveSingleSpace(isAfterSpace);
				this.print_string(this._ch);
			} else if ("=".equals(this._ch)) { // no whitespace before or after
				this.eatWhitespace();
				this.print_string("=");
				if (whitespaceChar.matcher(this._ch).find()) {
					this._ch = "";
				}
			} else if ("!".equals(this._ch) && !this._input.lookBack("\\")) { // !important
				this._output.space_before_token = true;
				this.print_string(this._ch);
			} else {
				var preserveAfterSpace = "\"".equals(previous_ch) || "'".equals(previous_ch);
				this.preserveSingleSpace(preserveAfterSpace || isAfterSpace);
				this.print_string(this._ch);

				if (!this._output.just_added_newline() && "\n".equals(this._input.peek()) && insideNonSemiColonValues) {
					this._output.add_new_line();
				}
			}
		}

		var sweetCode = this._output.get_code(eol);

		return sweetCode;
	}

}
