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

import static io.beautifier.javascript.Tokenizer.in_array;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.beautifier.core.Output;
import io.beautifier.core.TokenStream;
import io.beautifier.javascript.Options.BraceStyle;
import io.beautifier.javascript.Options.OperatorPosition;
import io.beautifier.javascript.Tokenizer.TOKEN;

@NonNullByDefault
public class Beautifier {

	private static String ltrim(String s) {
		return s.replaceFirst("^\\s+", "");
	}

	private boolean reserved_word(@Nullable Token token, String word) {
		return token != null && token.type == TOKEN.RESERVED && token.text.equals(word);
	}

	private boolean reserved_array(@Nullable Token token, String... words) {
		return token != null && token.type == TOKEN.RESERVED && in_array(token.text, words);
	}

	// Unsure of what they mean, but they work. Worth cleaning up in future.
	private static final String[] special_words = new String[] { "case", "return", "do", "if", "throw", "else", "await", "break", "continue", "async" };

	private static final OperatorPosition[] OPERATOR_POSITION_BEFORE_OR_PRESERVE = new OperatorPosition[] { OperatorPosition.beforeNewline, OperatorPosition.preserveNewline };

	enum MODE {
		BlockStatement, // 'BLOCK'
		Statement, // 'STATEMENT'
		ObjectLiteral, // 'OBJECT',
		ArrayLiteral, //'[EXPRESSION]',
		ForInitializer, //'(FOR-EXPRESSION)',
		Conditional, //'(COND-EXPRESSION)',
		Expression, //'(EXPRESSION)'
	}

	private static void remove_redundant_indentation(Output output, Frame frame) {
		// This implementation is effective but has some issues:
		//     - can cause line wrap to happen too soon due to indent removal
		//           after wrap points are calculated
		// These issues are minor compared to ugly indentation.

		if (frame.multiline_frame ||
			frame.mode == MODE.ForInitializer ||
			frame.mode == MODE.Conditional) {
			return;
		}

		// remove one indent from each line inside this section
		output.remove_indent(frame.start_line_index);
	}

	// we could use just string.split, but
	// IE doesn't like returning empty strings
	private static List<String> split_linebreaks(String s) {
		//return s.split(/\x0d\x0a|\x0a/);

		s = s.replaceAll(Acorn.lineBreak.pattern(), "\n");
		List<String> out = new ArrayList<>();
		int idx = s.indexOf("\n");
		while (idx != -1) {
			out.add(s.substring(0, idx));
			s = s.substring(idx + 1);
			idx = s.indexOf("\n");
		}
		if (!s.isEmpty()) {
			out.add(s);
		}
		return out;
	}

	private static boolean is_array(MODE mode) {
		return mode == MODE.ArrayLiteral;
	}

	private static boolean is_expression(MODE mode) {
		return EnumSet.of(MODE.Expression, MODE.ForInitializer, MODE.Conditional).contains(mode);
	}

	private static boolean all_lines_start_with(List<String> lines, String c) {
		for (String line : lines) {
			if (!line.trim().startsWith(c)) {
				return false;
			}
		}
		return true;
	}

	private static boolean each_line_matches_indent(List<String> lines, String indent) {
		for (String line : lines) {
			// allow empty lines to pass through
			if (!line.isEmpty() && !line.startsWith(indent)) {
				return false;
			}
		}
		return true;
	}

	private String _source_text;
	private @Nullable Output _output;
	private @Nullable TokenStream<TOKEN, Token> _tokens;
	private @Nullable String _last_last_text;
	private @Nullable Frame _flags;
	private @Nullable Frame _previous_flags;
	private @Nullable List<Frame> _flag_store;
	private Options _options;

	public Beautifier(@Nullable String source_text) {
		this(source_text, null);
	}
	
	public Beautifier(@Nullable String source_text, @Nullable Options options) {
		this._source_text = source_text != null ? source_text : "";

		this._output = null;
		this._tokens = null;
		this._last_last_text = null;
		this._flags = null;
		this._previous_flags = null;

		this._flag_store = null;
		this._options = options != null ? options : new Options();
		this._options.prepare();
	}

	static class Frame {
		private MODE mode;
		private @Nullable Frame parent;
		private Token last_token; // last token text
		private String last_word; // last TOKEN.WORD passed
		private boolean declaration_statement;
		private boolean declaration_assignment;
		private boolean multiline_frame;
		private boolean inline_frame;
		private boolean if_block;
		private boolean else_block;
		private boolean class_start_block; // class A { INSIDE HERE } or class B extends C { INSIDE HERE }
		private boolean do_block;
		private boolean do_while;
		private boolean import_block;
		private boolean in_case_statement; // switch(..){ INSIDE HERE }
		private boolean in_case; // we're on the exact line with "case 0:"
		private boolean case_body; // the indented case-action block
		private boolean case_block; // the indented case-action block is wrapped with {}
		private int indentation_level;
		private int alignment;
		private int line_indent_level;
		private int start_line_index;
		private int ternary_depth;

		Frame(@Nullable Frame flags_base, MODE mode, int next_indent_level, int start_line_index) {
			this.mode = mode;
			this.parent = flags_base;
			this.last_token = flags_base != null ? flags_base.last_token : new Token(TOKEN.START_BLOCK, ""); // last token text
			this.last_word = flags_base != null ? flags_base.last_word : ""; // last TOKEN.WORD passed
			this.declaration_statement = false;
			this.declaration_assignment = false;
			this.multiline_frame = false;
			this.inline_frame = false;
			this.if_block = false;
			this.else_block = false;
			this.class_start_block = false; // class A { INSIDE HERE } or class B extends C { INSIDE HERE }
			this.do_block = false;
			this.do_while = false;
			this.import_block = false;
			this.in_case_statement = false; // switch(..){ INSIDE HERE }
			this.in_case = false; // we're on the exact line with "case 0:"
			this.case_body = false; // the indented case-action block
			this.case_block = false; // the indented case-action block is wrapped with {}
			this.indentation_level = next_indent_level;
			this.alignment = 0;
			this.line_indent_level = flags_base != null ? flags_base.line_indent_level : next_indent_level;
			this.start_line_index = start_line_index;
			this.ternary_depth = 0;
		}
	}

	public Frame create_flags(@Nullable Frame flags_base, MODE mode) {
		var next_indent_level = 0;
		if (flags_base != null) {
			next_indent_level = flags_base.indentation_level;
			if (!this._output.just_added_newline() &&
				flags_base.line_indent_level > next_indent_level) {
				next_indent_level = flags_base.line_indent_level;
			}
		}

		return new Frame(flags_base, mode, next_indent_level, this._output.get_line_number());
	}

	private String _reset(String source_text) {
		Matcher matcher = Pattern.compile("^[\t ]*").matcher(source_text);
		matcher.find();
		var baseIndentString = matcher.group();

		this._last_last_text = ""; // pre-last token text
		this._output = new Output(this._options, baseIndentString);

		// If testing the ignore directive, start with output disable set to true
		this._output.raw = this._options.test_output_raw;


		// Stack of parsing/formatting states, including MODE.
		// We tokenize, parse, and output in an almost purely a forward-only stream of token input
		// and formatted output.  This makes the beautifier less accurate than full parsers
		// but also far more tolerant of syntax errors.
		//
		// For example, the default mode is MODE.BlockStatement. If we see a '{' we push a new frame of type
		// MODE.BlockStatement on the the stack, even though it could be object literal.  If we later
		// encounter a ":", we'll switch to to MODE.ObjectLiteral.  If we then see a ";",
		// most full parsers would die, but the beautifier gracefully falls back to
		// MODE.BlockStatement and continues on.
		this._flag_store = new ArrayList<>();
		this.set_mode(MODE.BlockStatement);
		var tokenizer = new Tokenizer(source_text, this._options);
		this._tokens = tokenizer.tokenize();
		return source_text;
	}

	public String beautify() {
		// if disabled, return the input unchanged.
		if (this._options.disabled) {
			return this._source_text;
		}

		String sweet_code;
		var source_text = this._reset(this._source_text);

		var eol = this._options.eol;
		if ("auto".equals(this._options.eol)) {
			eol = "\n";
			if (source_text != null && Acorn.lineBreak.matcher(source_text).find()) {
				Matcher matcher = Acorn.lineBreak.matcher(source_text);
				if (matcher.find()) {
					eol = matcher.group();
				}
			}
		}

		var current_token = this._tokens.next();
		while (current_token != null) {
			this.handle_token(current_token);

			this._last_last_text = this._flags.last_token.text;
			this._flags.last_token = current_token;

			current_token = this._tokens.next();
		}

		sweet_code = this._output.get_code(eol);

		return sweet_code;
	}

	public void handle_token(Token current_token) {
		handle_token(current_token, false);
	}

	public void handle_token(Token current_token, boolean preserve_statement_flags) {
		switch (current_token.type) {
		case START_EXPR:
			this.handle_start_expr(current_token);
			break;
		case END_EXPR:
			this.handle_end_expr(current_token);
			break;
		case START_BLOCK:
			this.handle_start_block(current_token);
			break;
		case END_BLOCK:
			this.handle_end_block(current_token);
			break;
		case WORD:
			this.handle_word(current_token);
			break;
		case RESERVED:
			this.handle_word(current_token);
			break;
		case SEMICOLON:
			this.handle_semicolon(current_token);
			break;
		case STRING:
			this.handle_string(current_token);
			break;
		case EQUALS:
			this.handle_equals(current_token);
			break;
		case OPERATOR:
			this.handle_operator(current_token);
			break;
		case COMMA:
			this.handle_comma(current_token);
			break;
		case BLOCK_COMMENT:
			this.handle_block_comment(current_token, preserve_statement_flags);
			break;
		case COMMENT:
			this.handle_comment(current_token, preserve_statement_flags);
			break;
		case DOT:
			this.handle_dot(current_token);
			break;
		case EOF:
			this.handle_eof(current_token);
			break;
		case UNKNOWN:
			this.handle_unknown(current_token, preserve_statement_flags);
			break;
		default:
			this.handle_unknown(current_token, preserve_statement_flags);
			break;
		}
	}

	private void handle_whitespace_and_comments(Token current_token) {
		handle_whitespace_and_comments(current_token, false);
	}

	private void handle_whitespace_and_comments(Token current_token, boolean preserve_statement_flags) {
		var newlines = current_token.newlines;
		var keep_whitespace = this._options.keep_array_indentation && is_array(this._flags.mode);

		if (current_token.comments_before != null) {
			var comment_token = current_token.comments_before.next();
			while (comment_token != null) {
				// The cleanest handling of inline comments is to treat them as though they aren't there.
				// Just continue formatting and the behavior should be logical.
				// Also ignore unknown tokens.  Again, this should result in better behavior.
				this.handle_whitespace_and_comments(comment_token, preserve_statement_flags);
				this.handle_token(comment_token, preserve_statement_flags);
				comment_token = current_token.comments_before.next();
			}
		}

		if (keep_whitespace) {
			for (var i = 0; i < newlines; i += 1) {
				this.print_newline(i > 0, preserve_statement_flags);
			}
		} else {
			if (this._options.max_preserve_newlines != 0 && newlines > this._options.max_preserve_newlines) {
				newlines = this._options.max_preserve_newlines;
			}

			if (this._options.preserve_newlines) {
				if (newlines > 1) {
					this.print_newline(false, preserve_statement_flags);
					for (var j = 1; j < newlines; j += 1) {
						this.print_newline(true, preserve_statement_flags);
					}
				}
			}
		}

	}

	private static final String[] newline_restricted_tokens = new String[] { "async", "break", "continue", "return", "throw", "yield" };

	public void allow_wrap_or_preserved_newline(Token current_token) {
		allow_wrap_or_preserved_newline(current_token, false);
	}

	public void allow_wrap_or_preserved_newline(Token current_token, boolean force_linewrap) {
		// Never wrap the first token on a line
		if (this._output.just_added_newline()) {
			return;
		}

		var shouldPreserveOrForce = (this._options.preserve_newlines && current_token.newlines != 0) || force_linewrap;
		var operatorLogicApplies = in_array(this._flags.last_token.text, Tokenizer.positionable_operators) ||
			in_array(current_token.text, Tokenizer.positionable_operators);

		if (operatorLogicApplies) {
			var shouldPrintOperatorNewline = (
					in_array(this._flags.last_token.text, Tokenizer.positionable_operators) &&
					in_array(this._options.operator_position, OPERATOR_POSITION_BEFORE_OR_PRESERVE)
				) ||
				in_array(current_token.text, Tokenizer.positionable_operators);
			shouldPreserveOrForce = shouldPreserveOrForce && shouldPrintOperatorNewline;
		}

		if (shouldPreserveOrForce) {
			this.print_newline(false, true);
		} else if (this._options.wrap_line_length != 0) {
			if (reserved_array(this._flags.last_token, newline_restricted_tokens)) {
				// These tokens should never have a newline inserted
				// between them and the following expression.
				return;
			}
			this._output.set_wrap_point();
		}
	}

	public void print_newline() {
		print_newline(false, false);
	}

	public void print_newline(boolean force_newline) {
		print_newline(force_newline, false);
	}

	public void print_newline(boolean force_newline, boolean preserve_statement_flags) {
		if (!preserve_statement_flags) {
			if (!";".equals(this._flags.last_token.text) && !",".equals(this._flags.last_token.text) && !"=".equals(this._flags.last_token.text) && (this._flags.last_token.type != TOKEN.OPERATOR || "--".equals(this._flags.last_token.text) || "++".equals(this._flags.last_token.text))) {
				var next_token = this._tokens.peek();
				while (this._flags.mode == MODE.Statement &&
					!(this._flags.if_block && reserved_word(next_token, "else")) &&
					!this._flags.do_block) {
					this.restore_mode();
				}
			}
		}

		if (this._output.add_new_line(force_newline)) {
			this._flags.multiline_frame = true;
		}
	}

	public void print_token_line_indentation(Token current_token) {
		if (this._output.just_added_newline()) {
			if (this._options.keep_array_indentation &&
				current_token.newlines != 0 &&
				("[".equals(current_token.text) || is_array(this._flags.mode))) {
				this._output.current_line.set_indent(-1);
				this._output.current_line.push(current_token.whitespace_before);
				this._output.space_before_token = false;
			} else if (this._output.set_indent(this._flags.indentation_level, this._flags.alignment)) {
				this._flags.line_indent_level = this._flags.indentation_level;
			}
		}
	}

	public void print_token(Token current_token) {
		if (this._output.raw) {
			this._output.add_raw_token(current_token);
			return;
		}

		if (this._options.comma_first && current_token.previous != null && current_token.previous.type == TOKEN.COMMA &&
			this._output.just_added_newline()) {
			if (",".equals(this._output.previous_line.last())) {
				var popped = this._output.previous_line.pop();
				// if the comma was already at the start of the line,
				// pull back onto that line and reprint the indentation
				if (this._output.previous_line.is_empty()) {
					this._output.previous_line.push(popped);
					this._output.trim(true);
					this._output.current_line.pop();
					this._output.trim();
				}

				// add the comma in front of the next token
				this.print_token_line_indentation(current_token);
				this._output.add_token(",");
				this._output.space_before_token = true;
			}
		}

		this.print_token_line_indentation(current_token);
		this._output.non_breaking_space = true;
		this._output.add_token(current_token.text);
		if (this._output.previous_token_wrapped) {
			this._flags.multiline_frame = true;
		}
	}

	public void indent() {
		this._flags.indentation_level += 1;
		this._output.set_indent(this._flags.indentation_level, this._flags.alignment);
	}

	public void deindent() {
		if (this._flags.indentation_level > 0 &&
			((this._flags.parent == null) || this._flags.indentation_level > this._flags.parent.indentation_level)) {
			this._flags.indentation_level -= 1;
			this._output.set_indent(this._flags.indentation_level, this._flags.alignment);
		}
	}

	public void set_mode(MODE mode) {
		if (this._flags != null) {
			this._flag_store.add(this._flags);
			this._previous_flags = this._flags;
		} else {
			this._previous_flags = this.create_flags(null, mode);
		}

		this._flags = this.create_flags(this._previous_flags, mode);
		this._output.set_indent(this._flags.indentation_level, this._flags.alignment);
	}


	public void restore_mode() {
		if (this._flag_store != null && !this._flag_store.isEmpty()) {
			this._previous_flags = this._flags;
			this._flags = this._flag_store.remove(this._flag_store.size() - 1);
			if (this._previous_flags.mode == MODE.Statement) {
				remove_redundant_indentation(this._output, this._previous_flags);
			}
			this._output.set_indent(this._flags.indentation_level, this._flags.alignment);
		}
	}

	public boolean start_of_object_property() {
		return this._flags.parent.mode == MODE.ObjectLiteral && this._flags.mode == MODE.Statement && (
			(":".equals(this._flags.last_token.text) && this._flags.ternary_depth == 0) || (reserved_array(this._flags.last_token, "get", "set")));
	}

	public boolean start_of_statement(Token current_token) {
		var start = false;
		start = start || reserved_array(this._flags.last_token, "var", "let", "const") && current_token.type == TOKEN.WORD;
		start = start || reserved_word(this._flags.last_token, "do");
		start = start || (!(this._flags.parent.mode == MODE.ObjectLiteral && this._flags.mode == MODE.Statement)) && reserved_array(this._flags.last_token, newline_restricted_tokens) && current_token.newlines == 0;
		start = start || reserved_word(this._flags.last_token, "else") &&
			!(reserved_word(current_token, "if") && current_token.comments_before == null);
		start = start || (this._flags.last_token.type == TOKEN.END_EXPR && (this._previous_flags.mode == MODE.ForInitializer || this._previous_flags.mode == MODE.Conditional));
		start = start || (this._flags.last_token.type == TOKEN.WORD && this._flags.mode == MODE.BlockStatement &&
			!this._flags.in_case &&
			!("--".equals(current_token.text) || "++".equals(current_token.text)) &&
			!"function".equals(this._last_last_text) &&
			current_token.type != TOKEN.WORD && current_token.type != TOKEN.RESERVED);
		start = start || (this._flags.mode == MODE.ObjectLiteral && (
			(":".equals(this._flags.last_token.text) && this._flags.ternary_depth == 0) || reserved_array(this._flags.last_token, "get", "set")));

		if (start) {
			this.set_mode(MODE.Statement);
			this.indent();

			this.handle_whitespace_and_comments(current_token, true);

			// Issue #276:
			// If starting a new statement with [if, for, while, do], push to a new line.
			// if (a) if (b) if(c) d(); else e(); else f();
			if (!this.start_of_object_property()) {
				this.allow_wrap_or_preserved_newline(current_token,
					reserved_array(current_token, "do", "for", "if", "while"));
			}
			return true;
		}
		return false;
	}

	public void handle_start_expr(Token current_token) {
		// The conditional starts the statement if appropriate.
		if (!this.start_of_statement(current_token)) {
			this.handle_whitespace_and_comments(current_token);
		}

		var next_mode = MODE.Expression;
		if ("[".equals(current_token.text)) {

			if (this._flags.last_token.type == TOKEN.WORD || ")".equals(this._flags.last_token.text)) {
				// this is array index specifier, break immediately
				// a[x], fn()[x]
				if (reserved_array(this._flags.last_token, Tokenizer.line_starters)) {
					this._output.space_before_token = true;
				}
				this.print_token(current_token);
				this.set_mode(next_mode);
				this.indent();
				if (this._options.space_in_paren) {
					this._output.space_before_token = true;
				}
				return;
			}

			next_mode = MODE.ArrayLiteral;
			if (is_array(this._flags.mode)) {
				if ("[".equals(this._flags.last_token.text) ||
					(",".equals(this._flags.last_token.text) && ("]".equals(this._last_last_text) || "}".equals(this._last_last_text)))) {
					// ], [ goes to new line
					// }, [ goes to new line
					if (!this._options.keep_array_indentation) {
						this.print_newline();
					}
				}
			}

			if (!in_array(this._flags.last_token.type, TOKEN.START_EXPR, TOKEN.END_EXPR, TOKEN.WORD, TOKEN.OPERATOR, TOKEN.DOT)) {
				this._output.space_before_token = true;
			}
		} else {
			if (this._flags.last_token.type == TOKEN.RESERVED) {
				if ("for".equals(this._flags.last_token.text)) {
					this._output.space_before_token = this._options.space_before_conditional;
					next_mode = MODE.ForInitializer;
				} else if (in_array(this._flags.last_token.text, "if", "while", "switch")) {
					this._output.space_before_token = this._options.space_before_conditional;
					next_mode = MODE.Conditional;
				} else if (in_array(this._flags.last_word, "await", "async")) {
					// Should be a space between await and an IIFE, or async and an arrow function
					this._output.space_before_token = true;
				} else if ("import".equals(this._flags.last_token.text) && "".equals(current_token.whitespace_before)) {
					this._output.space_before_token = false;
				} else if (in_array(this._flags.last_token.text, Tokenizer.line_starters) || "catch".equals(this._flags.last_token.text)) {
					this._output.space_before_token = true;
				}
			} else if (this._flags.last_token.type == TOKEN.EQUALS || this._flags.last_token.type == TOKEN.OPERATOR) {
				// Support of this kind of newline preservation.
				// a = (b &&
				//     (c || d));
				if (!this.start_of_object_property()) {
					this.allow_wrap_or_preserved_newline(current_token);
				}
			} else if (this._flags.last_token.type == TOKEN.WORD) {
				this._output.space_before_token = false;

				// function name() vs function name ()
				// function* name() vs function* name ()
				// async name() vs async name ()
				// In ES6, you can also define the method properties of an object
				// var obj = {a: function() {}}
				// It can be abbreviated
				// var obj = {a() {}}
				// var obj = { a() {}} vs var obj = { a () {}}
				// var obj = { * a() {}} vs var obj = { * a () {}}
				var peek_back_two = this._tokens.peek(-3);
				if (this._options.space_after_named_function && peek_back_two != null) {
					// peek starts at next character so -1 is current token
					var peek_back_three = this._tokens.peek(-4);
					if (reserved_array(peek_back_two, "async", "function") ||
						("*".equals(peek_back_two.text) && reserved_array(peek_back_three, "async", "function"))) {
						this._output.space_before_token = true;
					} else if (this._flags.mode == MODE.ObjectLiteral) {
						if (("{".equals(peek_back_two.text) || ",".equals(peek_back_two.text)) ||
							("*".equals(peek_back_two.text) && ("{".equals(peek_back_three.text) || ",".equals(peek_back_three.text)))) {
							this._output.space_before_token = true;
						}
					} else if (this._flags.parent != null && this._flags.parent.class_start_block) {
						this._output.space_before_token = true;
					}
				}
			} else {
				// Support preserving wrapped arrow function expressions
				// a.b('c',
				//     () => d.e
				// )
				this.allow_wrap_or_preserved_newline(current_token);
			}

			// function() vs function ()
			// yield*() vs yield* ()
			// function*() vs function* ()
			if ((this._flags.last_token.type == TOKEN.RESERVED && ("function".equals(this._flags.last_word) || "typeof".equals(this._flags.last_word))) ||
				("*".equals(this._flags.last_token.text) &&
					(in_array(this._last_last_text, "function", "yield") ||
						(this._flags.mode == MODE.ObjectLiteral && in_array(this._last_last_text, "{", ","))))) {
				this._output.space_before_token = this._options.space_after_anon_function;
			}
		}

		if (";".equals(this._flags.last_token.text) || this._flags.last_token.type == TOKEN.START_BLOCK) {
			this.print_newline();
		} else if (this._flags.last_token.type == TOKEN.END_EXPR || this._flags.last_token.type == TOKEN.START_EXPR || this._flags.last_token.type == TOKEN.END_BLOCK || ".".equals(this._flags.last_token.text) || this._flags.last_token.type == TOKEN.COMMA) {
			// do nothing on (( and )( and ][ and ]( and .(
			// TODO: Consider whether forcing this is required.  Review failing tests when removed.
			this.allow_wrap_or_preserved_newline(current_token, current_token.newlines != 0);
		}

		this.print_token(current_token);
		this.set_mode(next_mode);
		if (this._options.space_in_paren) {
			this._output.space_before_token = true;
		}

		// In all cases, if we newline while inside an expression it should be indented.
		this.indent();
	}

	public void handle_end_expr(Token current_token) {
		// statements inside expressions are not valid syntax, but...
		// statements must all be closed when their container closes
		while (this._flags.mode == MODE.Statement) {
			this.restore_mode();
		}

		this.handle_whitespace_and_comments(current_token);

		if (this._flags.multiline_frame) {
			this.allow_wrap_or_preserved_newline(current_token,
				"]".equals(current_token.text) && is_array(this._flags.mode) && !this._options.keep_array_indentation);
		}

		if (this._options.space_in_paren) {
			if (this._flags.last_token.type == TOKEN.START_EXPR && !this._options.space_in_empty_paren) {
				// () [] no inner space in empty parens like these, ever, ref #320
				this._output.trim();
				this._output.space_before_token = false;
			} else {
				this._output.space_before_token = true;
			}
		}
		this.deindent();
		this.print_token(current_token);
		this.restore_mode();

		remove_redundant_indentation(this._output, this._previous_flags);

		// do {} while () // no statement required after
		if (this._flags.do_while && this._previous_flags.mode == MODE.Conditional) {
			this._previous_flags.mode = MODE.Expression;
			this._flags.do_block = false;
			this._flags.do_while = false;

		}
	}

	public void handle_start_block(Token current_token) {
		this.handle_whitespace_and_comments(current_token);

		// Check if this is should be treated as a ObjectLiteral
		var next_token = this._tokens.peek();
		var second_token = this._tokens.peek(1);
		if ("switch".equals(this._flags.last_word) && this._flags.last_token.type == TOKEN.END_EXPR) {
			this.set_mode(MODE.BlockStatement);
			this._flags.in_case_statement = true;
		} else if (this._flags.case_body) {
			this.set_mode(MODE.BlockStatement);
		} else if (second_token != null && (
				(in_array(second_token.text, ":", ",") && in_array(next_token.type, TOKEN.STRING, TOKEN.WORD, TOKEN.RESERVED)) ||
				(in_array(next_token.text, "get", "set", "...") && in_array(second_token.type, TOKEN.WORD, TOKEN.RESERVED))
			)) {
			// We don't support TypeScript,but we didn't break it for a very long time.
			// We'll try to keep not breaking it.
			if (in_array(this._last_last_text, "class", "interface") && !in_array(second_token.text, ":", ",")) {
				this.set_mode(MODE.BlockStatement);
			} else {
				this.set_mode(MODE.ObjectLiteral);
			}
		} else if (this._flags.last_token.type == TOKEN.OPERATOR && "=>".equals(this._flags.last_token.text)) {
			// arrow function: (param1, paramN) => { statements }
			this.set_mode(MODE.BlockStatement);
		} else if (in_array(this._flags.last_token.type, TOKEN.EQUALS, TOKEN.START_EXPR, TOKEN.COMMA, TOKEN.OPERATOR) ||
			reserved_array(this._flags.last_token, "return", "throw", "import", "default")
		) {
			// Detecting shorthand function syntax is difficult by scanning forward,
			//     so check the surrounding context.
			// If the block is being returned, imported, export default, passed as arg,
			//     assigned with = or assigned in a nested object, treat as an ObjectLiteral.
			this.set_mode(MODE.ObjectLiteral);
		} else {
			this.set_mode(MODE.BlockStatement);
		}

		if (this._flags.last_token != null) {
			if (reserved_array(this._flags.last_token.previous, "class", "extends")) {
				this._flags.class_start_block = true;
			}
		}

		var empty_braces = next_token.comments_before == null && "}".equals(next_token.text);
		var empty_anonymous_function = empty_braces && "function".equals(this._flags.last_word) &&
			this._flags.last_token.type == TOKEN.END_EXPR;

		if (this._options.brace_preserve_inline) // check for inline, set inline_frame if so
		{
			// search forward for a newline wanted inside this block
			var index = 0;
			Token check_token = null;
			this._flags.inline_frame = true;
			do {
				index += 1;
				check_token = this._tokens.peek(index - 1);
				if (check_token.newlines != 0) {
					this._flags.inline_frame = false;
					break;
				}
			} while (check_token.type != TOKEN.EOF &&
				!(check_token.type == TOKEN.END_BLOCK && check_token.opened == current_token));
		}

		if ((this._options.brace_style == BraceStyle.expand ||
				(this._options.brace_style == BraceStyle.none && current_token.newlines != 0)) &&
			!this._flags.inline_frame) {
			if (this._flags.last_token.type != TOKEN.OPERATOR &&
				(empty_anonymous_function ||
					this._flags.last_token.type == TOKEN.EQUALS ||
					(reserved_array(this._flags.last_token, special_words) && !"else".equals(this._flags.last_token.text)))) {
				this._output.space_before_token = true;
			} else {
				this.print_newline(false, true);
			}
		} else { // collapse || inline_frame
			if (is_array(this._previous_flags.mode) && (this._flags.last_token.type == TOKEN.START_EXPR || this._flags.last_token.type == TOKEN.COMMA)) {
				if (this._flags.last_token.type == TOKEN.COMMA || this._options.space_in_paren) {
					this._output.space_before_token = true;
				}

				if (this._flags.last_token.type == TOKEN.COMMA || (this._flags.last_token.type == TOKEN.START_EXPR && this._flags.inline_frame)) {
					this.allow_wrap_or_preserved_newline(current_token);
					this._previous_flags.multiline_frame = this._previous_flags.multiline_frame || this._flags.multiline_frame;
					this._flags.multiline_frame = false;
				}
			}
			if (this._flags.last_token.type != TOKEN.OPERATOR && this._flags.last_token.type != TOKEN.START_EXPR) {
				if (in_array(this._flags.last_token.type, TOKEN.START_BLOCK, TOKEN.SEMICOLON) && !this._flags.inline_frame) {
					this.print_newline();
				} else {
					this._output.space_before_token = true;
				}
			}
		}
		this.print_token(current_token);
		this.indent();

		// Except for specific cases, open braces are followed by a new line.
		if (!empty_braces && !(this._options.brace_preserve_inline && this._flags.inline_frame)) {
			this.print_newline();
		}
	}

	public void handle_end_block(Token current_token) {
		// statements must all be closed when their container closes
		this.handle_whitespace_and_comments(current_token);

		while (this._flags.mode == MODE.Statement) {
			this.restore_mode();
		}

		var empty_braces = this._flags.last_token.type == TOKEN.START_BLOCK;

		if (this._flags.inline_frame && !empty_braces) { // try inline_frame (only set if this._options.braces-preserve-inline) first
			this._output.space_before_token = true;
		} else if (this._options.brace_style == BraceStyle.expand) {
			if (!empty_braces) {
				this.print_newline();
			}
		} else {
			// skip {}
			if (!empty_braces) {
				if (is_array(this._flags.mode) && this._options.keep_array_indentation) {
					// we REALLY need a newline here, but newliner would skip that
					this._options.keep_array_indentation = false;
					this.print_newline();
					this._options.keep_array_indentation = true;

				} else {
					this.print_newline();
				}
			}
		}
		this.restore_mode();
		this.print_token(current_token);
	}

	private enum Prefix {
		NONE,
		SPACE,
		NEWLINE,
	}

	private void handle_word(Token current_token) {
		if (current_token.type == TOKEN.RESERVED) {
			if (in_array(current_token.text, "set", "get") && this._flags.mode != MODE.ObjectLiteral) {
				current_token.type = TOKEN.WORD;
			} else if ("import".equals(current_token.text) && in_array(this._tokens.peek().text, "(", ".")) {
				current_token.type = TOKEN.WORD;
			} else if (in_array(current_token.text, "as", "from") && !this._flags.import_block) {
				current_token.type = TOKEN.WORD;
			} else if (this._flags.mode == MODE.ObjectLiteral) {
				var next_token = this._tokens.peek();
				if (":".equals(next_token.text)) {
					current_token.type = TOKEN.WORD;
				}
			}
		}

		if (this.start_of_statement(current_token)) {
			// The conditional starts the statement if appropriate.
			if (reserved_array(this._flags.last_token, "var", "let", "const") && current_token.type == TOKEN.WORD) {
				this._flags.declaration_statement = true;
			}
		} else if (current_token.newlines != 0 && !is_expression(this._flags.mode) &&
			(this._flags.last_token.type != TOKEN.OPERATOR || ("--".equals(this._flags.last_token.text) || "++".equals(this._flags.last_token.text))) &&
			this._flags.last_token.type != TOKEN.EQUALS &&
			(this._options.preserve_newlines || !reserved_array(this._flags.last_token, "var", "let", "const", "set", "get"))) {
			this.handle_whitespace_and_comments(current_token);
			this.print_newline();
		} else {
			this.handle_whitespace_and_comments(current_token);
		}

		if (this._flags.do_block && !this._flags.do_while) {
			if (reserved_word(current_token, "while")) {
				// do {} ## while ()
				this._output.space_before_token = true;
				this.print_token(current_token);
				this._output.space_before_token = true;
				this._flags.do_while = true;
				return;
			} else {
				// do {} should always have while as the next word.
				// if we don't see the expected while, recover
				this.print_newline();
				this._flags.do_block = false;
			}
		}

		// if may be followed by else, or not
		// Bare/inline ifs are tricky
		// Need to unwind the modes correctly: if (a) if (b) c(); else d(); else e();
		if (this._flags.if_block) {
			if (!this._flags.else_block && reserved_word(current_token, "else")) {
				this._flags.else_block = true;
			} else {
				while (this._flags.mode == MODE.Statement) {
					this.restore_mode();
				}
				this._flags.if_block = false;
				this._flags.else_block = false;
			}
		}

		if (this._flags.in_case_statement && reserved_array(current_token, "case", "default")) {
			this.print_newline();
			if (!this._flags.case_block && (this._flags.case_body || this._options.jslint_happy)) {
				// switch cases following one another
				this.deindent();
			}
			this._flags.case_body = false;

			this.print_token(current_token);
			this._flags.in_case = true;
			return;
		}

		if (this._flags.last_token.type == TOKEN.COMMA || this._flags.last_token.type == TOKEN.START_EXPR || this._flags.last_token.type == TOKEN.EQUALS || this._flags.last_token.type == TOKEN.OPERATOR) {
			if (!this.start_of_object_property()) {
				this.allow_wrap_or_preserved_newline(current_token);
			}
		}

		if (reserved_word(current_token, "function")) {
			if (in_array(this._flags.last_token.text, "}", ";") ||
				(this._output.just_added_newline() && !(in_array(this._flags.last_token.text, "(", "[", "{", ":", "=", ",") || this._flags.last_token.type == TOKEN.OPERATOR))) {
				// make sure there is a nice clean space of at least one blank line
				// before a new function definition
				if (!this._output.just_added_blankline() && current_token.comments_before == null) {
					this.print_newline();
					this.print_newline(true);
				}
			}
			if (this._flags.last_token.type == TOKEN.RESERVED || this._flags.last_token.type == TOKEN.WORD) {
				if (reserved_array(this._flags.last_token, "get", "set", "new", "export") ||
					reserved_array(this._flags.last_token, newline_restricted_tokens)) {
					this._output.space_before_token = true;
				} else if (reserved_word(this._flags.last_token, "default") && "export".equals(this._last_last_text)) {
					this._output.space_before_token = true;
				} else if ("declare".equals(this._flags.last_token.text)) {
					// accomodates Typescript declare function formatting
					this._output.space_before_token = true;
				} else {
					this.print_newline();
				}
			} else if (this._flags.last_token.type == TOKEN.OPERATOR || "=".equals(this._flags.last_token.text)) {
				// foo = function
				this._output.space_before_token = true;
			} else if (!this._flags.multiline_frame && (is_expression(this._flags.mode) || is_array(this._flags.mode))) {
				// (function
			} else {
				this.print_newline();
			}

			this.print_token(current_token);
			this._flags.last_word = current_token.text;
			return;
		}

		var prefix = Prefix.NONE;

		if (this._flags.last_token.type == TOKEN.END_BLOCK) {

			if (this._previous_flags.inline_frame) {
				prefix = Prefix.SPACE;
			} else if (!reserved_array(current_token, "else", "catch", "finally", "from")) {
				prefix = Prefix.NEWLINE;
			} else {
				if (this._options.brace_style == BraceStyle.expand ||
					this._options.brace_style == BraceStyle.endExpand ||
					(this._options.brace_style == BraceStyle.none && current_token.newlines != 0)) {
					prefix = Prefix.NEWLINE;
				} else {
					prefix = Prefix.SPACE;
					this._output.space_before_token = true;
				}
			}
		} else if (this._flags.last_token.type == TOKEN.SEMICOLON && this._flags.mode == MODE.BlockStatement) {
			// TODO: Should this be for STATEMENT as well?
			prefix = Prefix.NEWLINE;
		} else if (this._flags.last_token.type == TOKEN.SEMICOLON && is_expression(this._flags.mode)) {
			prefix = Prefix.SPACE;
		} else if (this._flags.last_token.type == TOKEN.STRING) {
			prefix = Prefix.NEWLINE;
		} else if (this._flags.last_token.type == TOKEN.RESERVED || this._flags.last_token.type == TOKEN.WORD ||
			("*".equals(this._flags.last_token.text) &&
				(in_array(this._last_last_text, "function", "yield") ||
					(this._flags.mode == MODE.ObjectLiteral && in_array(this._last_last_text, "{", ","))))) {
			prefix = Prefix.SPACE;
		} else if (this._flags.last_token.type == TOKEN.START_BLOCK) {
			if (this._flags.inline_frame) {
				prefix = Prefix.SPACE;
			} else {
				prefix = Prefix.NEWLINE;
			}
		} else if (this._flags.last_token.type == TOKEN.END_EXPR) {
			this._output.space_before_token = true;
			prefix = Prefix.NEWLINE;
		}

		if (reserved_array(current_token, Tokenizer.line_starters) && !")".equals(this._flags.last_token.text)) {
			if (this._flags.inline_frame || "else".equals(this._flags.last_token.text) || "export".equals(this._flags.last_token.text)) {
				prefix = Prefix.SPACE;
			} else {
				prefix = Prefix.NEWLINE;
			}

		}

		if (reserved_array(current_token, "else", "catch", "finally")) {
			if ((!(this._flags.last_token.type == TOKEN.END_BLOCK && this._previous_flags.mode == MODE.BlockStatement) ||
					this._options.brace_style == BraceStyle.expand ||
					this._options.brace_style == BraceStyle.endExpand ||
					(this._options.brace_style == BraceStyle.none && current_token.newlines != 0)) &&
				!this._flags.inline_frame) {
				this.print_newline();
			} else {
				this._output.trim(true);
				var line = this._output.current_line;
				// If we trimmed and there's something other than a close block before us
				// put a newline back in.  Handles '} // comment' scenario.
				if (!"}".equals(line.last())) {
					this.print_newline();
				}
				this._output.space_before_token = true;
			}
		} else if (prefix == Prefix.NEWLINE) {
			if (reserved_array(this._flags.last_token, special_words)) {
				// no newline between 'return nnn'
				this._output.space_before_token = true;
			} else if ("declare".equals(this._flags.last_token.text) && reserved_array(current_token, "var", "let", "const")) {
				// accomodates Typescript declare formatting
				this._output.space_before_token = true;
			} else if (this._flags.last_token.type != TOKEN.END_EXPR) {
				if ((this._flags.last_token.type != TOKEN.START_EXPR || !reserved_array(current_token, "var", "let", "const")) && !":".equals(this._flags.last_token.text)) {
					// no need to force newline on 'var': for (var x = 0...)
					if (reserved_word(current_token, "if") && reserved_word(current_token.previous, "else")) {
						// no newline for } else if {
						this._output.space_before_token = true;
					} else {
						this.print_newline();
					}
				}
			} else if (reserved_array(current_token, Tokenizer.line_starters) && !")".equals(this._flags.last_token.text)) {
				this.print_newline();
			}
		} else if (this._flags.multiline_frame && is_array(this._flags.mode) && ",".equals(this._flags.last_token.text) && "}".equals(this._last_last_text)) {
			this.print_newline(); // }, in lists get a newline treatment
		} else if (prefix == Prefix.SPACE) {
			this._output.space_before_token = true;
		}
		if (current_token.previous != null && (current_token.previous.type == TOKEN.WORD || current_token.previous.type == TOKEN.RESERVED)) {
			this._output.space_before_token = true;
		}
		this.print_token(current_token);
		this._flags.last_word = current_token.text;

		if (current_token.type == TOKEN.RESERVED) {
			if ("do".equals(current_token.text)) {
				this._flags.do_block = true;
			} else if ("if".equals(current_token.text)) {
				this._flags.if_block = true;
			} else if ("import".equals(current_token.text)) {
				this._flags.import_block = true;
			} else if (this._flags.import_block && reserved_word(current_token, "from")) {
				this._flags.import_block = false;
			}
		}
	}

	private void handle_semicolon(Token current_token) {
		if (this.start_of_statement(current_token)) {
			// The conditional starts the statement if appropriate.
			// Semicolon can be the start (and end) of a statement
			this._output.space_before_token = false;
		} else {
			this.handle_whitespace_and_comments(current_token);
		}

		var next_token = this._tokens.peek();
		while (this._flags.mode == MODE.Statement &&
			!(this._flags.if_block && reserved_word(next_token, "else")) &&
			!this._flags.do_block) {
			this.restore_mode();
		}

		// hacky but effective for the moment
		if (this._flags.import_block) {
			this._flags.import_block = false;
		}
		this.print_token(current_token);
	}

	private void handle_string(Token current_token) {
		if (current_token.text.startsWith("`") && current_token.newlines == 0 && "".equals(current_token.whitespace_before) && (")".equals(current_token.previous.text) || this._flags.last_token.type == TOKEN.WORD)) {
			//Conditional for detectign backtick strings
		} else if (this.start_of_statement(current_token)) {
			// The conditional starts the statement if appropriate.
			// One difference - strings want at least a space before
			this._output.space_before_token = true;
		} else {
			this.handle_whitespace_and_comments(current_token);
			if (this._flags.last_token.type == TOKEN.RESERVED || this._flags.last_token.type == TOKEN.WORD || this._flags.inline_frame) {
				this._output.space_before_token = true;
			} else if (this._flags.last_token.type == TOKEN.COMMA || this._flags.last_token.type == TOKEN.START_EXPR || this._flags.last_token.type == TOKEN.EQUALS || this._flags.last_token.type == TOKEN.OPERATOR) {
				if (!this.start_of_object_property()) {
					this.allow_wrap_or_preserved_newline(current_token);
				}
			} else if ((current_token.text.startsWith("`") && this._flags.last_token.type == TOKEN.END_EXPR && ("]".equals(current_token.previous.text) || ")".equals(current_token.previous.text)) && current_token.newlines == 0)) {
				this._output.space_before_token = true;
			} else {
				this.print_newline();
			}
		}
		this.print_token(current_token);
	}

	private void handle_equals(Token current_token) {
		if (this.start_of_statement(current_token)) {
			// The conditional starts the statement if appropriate.
		} else {
			this.handle_whitespace_and_comments(current_token);
		}

		if (this._flags.declaration_statement) {
			// just got an '=' in a var-line, different formatting/line-breaking, etc will now be done
			this._flags.declaration_assignment = true;
		}
		this._output.space_before_token = true;
		this.print_token(current_token);
		this._output.space_before_token = true;
	}

	private void handle_comma(Token current_token) {
		this.handle_whitespace_and_comments(current_token, true);

		this.print_token(current_token);
		this._output.space_before_token = true;
		if (this._flags.declaration_statement) {
			if (is_expression(this._flags.parent.mode)) {
				// do not break on comma, for(var a = 1, b = 2)
				this._flags.declaration_assignment = false;
			}

			if (this._flags.declaration_assignment) {
				this._flags.declaration_assignment = false;
				this.print_newline(false, true);
			} else if (this._options.comma_first) {
				// for comma-first, we want to allow a newline before the comma
				// to turn into a newline after the comma, which we will fixup later
				this.allow_wrap_or_preserved_newline(current_token);
			}
		} else if (this._flags.mode == MODE.ObjectLiteral ||
			(this._flags.mode == MODE.Statement && this._flags.parent.mode == MODE.ObjectLiteral)) {
			if (this._flags.mode == MODE.Statement) {
				this.restore_mode();
			}

			if (!this._flags.inline_frame) {
				this.print_newline();
			}
		} else if (this._options.comma_first) {
			// EXPR or DO_BLOCK
			// for comma-first, we want to allow a newline before the comma
			// to turn into a newline after the comma, which we will fixup later
			this.allow_wrap_or_preserved_newline(current_token);
		}
	}

	private void handle_operator(Token current_token) {
		var isGeneratorAsterisk = "*".equals(current_token.text) &&
			(reserved_array(this._flags.last_token, "function", "yield") ||
				(in_array(this._flags.last_token.type, TOKEN.START_BLOCK, TOKEN.COMMA, TOKEN.END_BLOCK, TOKEN.SEMICOLON))
			);
		var isUnary = in_array(current_token.text, "-", "+") && (
			in_array(this._flags.last_token.type, TOKEN.START_BLOCK, TOKEN.START_EXPR, TOKEN.EQUALS, TOKEN.OPERATOR) ||
			in_array(this._flags.last_token.text, Tokenizer.line_starters) ||
			",".equals(this._flags.last_token.text)
		);

		if (this.start_of_statement(current_token)) {
			// The conditional starts the statement if appropriate.
		} else {
			var preserve_statement_flags = !isGeneratorAsterisk;
			this.handle_whitespace_and_comments(current_token, preserve_statement_flags);
		}

		// hack for actionscript's import .*;
		if ("*".equals(current_token.text) && this._flags.last_token.type == TOKEN.DOT) {
			this.print_token(current_token);
			return;
		}

		if ("::".equals(current_token.text)) {
			// no spaces around exotic namespacing syntax operator
			this.print_token(current_token);
			return;
		}

		// Allow line wrapping between operators when operator_position is
		//   set to before or preserve
		if (this._flags.last_token.type == TOKEN.OPERATOR && in_array(this._options.operator_position, OPERATOR_POSITION_BEFORE_OR_PRESERVE)) {
			this.allow_wrap_or_preserved_newline(current_token);
		}

		if (":".equals(current_token.text) && this._flags.in_case) {
			this.print_token(current_token);

			this._flags.in_case = false;
			this._flags.case_body = true;
			if (this._tokens.peek().type != TOKEN.START_BLOCK) {
				this.indent();
				this.print_newline();
				this._flags.case_block = false;
			} else {
				this._flags.case_block = true;
				this._output.space_before_token = true;
			}
			return;
		}

		var space_before = true;
		var space_after = true;
		var in_ternary = false;
		if (":".equals(current_token.text)) {
			if (this._flags.ternary_depth == 0) {
				// Colon is invalid javascript outside of ternary and object, but do our best to guess what was meant.
				space_before = false;
			} else {
				this._flags.ternary_depth -= 1;
				in_ternary = true;
			}
		} else if ("?".equals(current_token.text)) {
			this._flags.ternary_depth += 1;
		}

		// let's handle the operator_position option prior to any conflicting logic
		if (!isUnary && !isGeneratorAsterisk && this._options.preserve_newlines && in_array(current_token.text, Tokenizer.positionable_operators)) {
			var isColon = ":".equals(current_token.text);
			var isTernaryColon = (isColon && in_ternary);
			var isOtherColon = (isColon && !in_ternary);

			switch (this._options.operator_position) {
				case beforeNewline:
					// if the current token is : and it's not a ternary statement then we set space_before to false
					this._output.space_before_token = !isOtherColon;

					this.print_token(current_token);

					if (!isColon || isTernaryColon) {
						this.allow_wrap_or_preserved_newline(current_token);
					}

					this._output.space_before_token = true;
					return;

				case afterNewline:
					// if the current token is anything but colon, or (via deduction) it's a colon and in a ternary statement,
					//   then print a newline.

					this._output.space_before_token = true;

					if (!isColon || isTernaryColon) {
						if (this._tokens.peek().newlines != 0) {
							this.print_newline(false, true);
						} else {
							this.allow_wrap_or_preserved_newline(current_token);
						}
					} else {
						this._output.space_before_token = false;
					}

					this.print_token(current_token);

					this._output.space_before_token = true;
					return;

				case preserveNewline:
					if (!isOtherColon) {
						this.allow_wrap_or_preserved_newline(current_token);
					}

					// if we just added a newline, or the current token is : and it's not a ternary statement,
					//   then we set space_before to false
					space_before = !(this._output.just_added_newline() || isOtherColon);

					this._output.space_before_token = space_before;
					this.print_token(current_token);
					this._output.space_before_token = true;
					return;
			}
		}

		if (isGeneratorAsterisk) {
			this.allow_wrap_or_preserved_newline(current_token);
			space_before = false;
			var next_token = this._tokens.peek();
			space_after = next_token != null && in_array(next_token.type, TOKEN.WORD, TOKEN.RESERVED);
		} else if ("...".equals(current_token.text)) {
			this.allow_wrap_or_preserved_newline(current_token);
			space_before = this._flags.last_token.type == TOKEN.START_BLOCK;
			space_after = false;
		} else if (in_array(current_token.text, "--", "++", "!", "~") || isUnary) {
			// unary operators (and binary +/- pretending to be unary) special cases
			if (this._flags.last_token.type == TOKEN.COMMA || this._flags.last_token.type == TOKEN.START_EXPR) {
				this.allow_wrap_or_preserved_newline(current_token);
			}

			space_before = false;
			space_after = false;

			// http://www.ecma-international.org/ecma-262/5.1/#sec-7.9.1
			// if there is a newline between -- or ++ and anything else we should preserve it.
			if (current_token.newlines != 0 && ("--".equals(current_token.text) || "++".equals(current_token.text) || "~".equals(current_token.text))) {
				var new_line_needed = reserved_array(this._flags.last_token, special_words) && current_token.newlines != 0;
				if (new_line_needed && (this._previous_flags.if_block || this._previous_flags.else_block)) {
					this.restore_mode();
				}
				this.print_newline(new_line_needed, true);
			}

			if (";".equals(this._flags.last_token.text) && is_expression(this._flags.mode)) {
				// for (;; ++i)
				//        ^^^
				space_before = true;
			}

			if (this._flags.last_token.type == TOKEN.RESERVED) {
				space_before = true;
			} else if (this._flags.last_token.type == TOKEN.END_EXPR) {
				space_before = !("]".equals(this._flags.last_token.text) && ("--".equals(current_token.text) || "++".equals(current_token.text)));
			} else if (this._flags.last_token.type == TOKEN.OPERATOR) {
				// a++ + ++b;
				// a - -b
				space_before = in_array(current_token.text, "--", "-", "++", "+") && in_array(this._flags.last_token.text, "--", "-", "++", "+");
				// + and - are not unary when preceeded by -- or ++ operator
				// a-- + b
				// a * +b
				// a - -b
				if (in_array(current_token.text, "+", "-") && in_array(this._flags.last_token.text, "--", "++")) {
					space_after = true;
				}
			}


			if (((this._flags.mode == MODE.BlockStatement && !this._flags.inline_frame) || this._flags.mode == MODE.Statement) &&
				("{".equals(this._flags.last_token.text) || ";".equals(this._flags.last_token.text))) {
				// { foo; --i }
				// foo(); --bar;
				this.print_newline();
			}
		}

		this._output.space_before_token = this._output.space_before_token || space_before;
		this.print_token(current_token);
		this._output.space_before_token = space_after;
	}

	private void handle_block_comment(Token current_token, boolean preserve_statement_flags) {
		if (this._output.raw) {
			this._output.add_raw_token(current_token);
			if (current_token.directives != null && "end".equals(current_token.directives.get("preserve"))) {
				// If we're testing the raw output behavior, do not allow a directive to turn it off.
				this._output.raw = this._options.test_output_raw;
			}
			return;
		}

		if (current_token.directives != null) {
			this.print_newline(false, preserve_statement_flags);
			this.print_token(current_token);
			if ("start".equals(current_token.directives.get("preserve"))) {
				this._output.raw = true;
			}
			this.print_newline(false, true);
			return;
		}

		// inline block
		if (!Acorn.newline.matcher(current_token.text).find() && current_token.newlines == 0) {
			this._output.space_before_token = true;
			this.print_token(current_token);
			this._output.space_before_token = true;
			return;
		} else {
			this.print_block_commment(current_token, preserve_statement_flags);
		}
	}

	private void print_block_commment(Token current_token, boolean preserve_statement_flags) {
		var lines = split_linebreaks(current_token.text);
		var javadoc = false;
		var starless = false;
		var lastIndent = current_token.whitespace_before;
		var lastIndentLength = lastIndent.length();

		// block comment starts with a new line
		this.print_newline(false, preserve_statement_flags);

		// first line always indented
		this.print_token_line_indentation(current_token);
		this._output.add_token(lines.get(0));
		this.print_newline(false, preserve_statement_flags);


		if (lines.size() > 1) {
			lines.remove(0);
			javadoc = all_lines_start_with(lines, "*");
			starless = each_line_matches_indent(lines, lastIndent);

			if (javadoc) {
				this._flags.alignment = 1;
			}

			for (int j = 0; j < lines.size(); j++) {
				if (javadoc) {
					// javadoc: reformat and re-indent
					this.print_token_line_indentation(current_token);
					this._output.add_token(ltrim(lines.get(j)));
				} else if (starless && !lines.get(j).isEmpty()) {
					// starless: re-indent non-empty content, avoiding trim
					this.print_token_line_indentation(current_token);
					this._output.add_token(lines.get(j).substring(lastIndentLength));
				} else {
					// normal comments output raw
					this._output.current_line.set_indent(-1);
					this._output.add_token(lines.get(j));
				}

				// for comments on their own line or  more than one line, make sure there's a new line after
				this.print_newline(false, preserve_statement_flags);
			}

			this._flags.alignment = 0;
		}
	}


	private void handle_comment(Token current_token, boolean preserve_statement_flags) {
		if (current_token.newlines != 0) {
			this.print_newline(false, preserve_statement_flags);
		} else {
			this._output.trim(true);
		}

		this._output.space_before_token = true;
		this.print_token(current_token);
		this.print_newline(false, preserve_statement_flags);
	}

	private void handle_dot(Token current_token) {
		if (this.start_of_statement(current_token)) {
			// The conditional starts the statement if appropriate.
		} else {
			this.handle_whitespace_and_comments(current_token, true);
		}

		if (Pattern.compile("^[0-9]+$").matcher(this._flags.last_token.text).find()) {
			this._output.space_before_token = true;
		}

		if (reserved_array(this._flags.last_token, special_words)) {
			this._output.space_before_token = false;
		} else {
			// allow preserved newlines before dots in general
			// force newlines on dots after close paren when break_chained - for bar().baz()
			this.allow_wrap_or_preserved_newline(current_token,
				")".equals(this._flags.last_token.text) && this._options.break_chained_methods);
		}

		// Only unindent chained method dot if this dot starts a new line.
		// Otherwise the automatic extra indentation removal will handle the over indent
		if (this._options.unindent_chained_methods && this._output.just_added_newline()) {
			this.deindent();
		}

		this.print_token(current_token);
	}

	private void handle_unknown(Token current_token, boolean preserve_statement_flags) {
		this.print_token(current_token);

		if (current_token.text.endsWith("\n")) {
			this.print_newline(false, preserve_statement_flags);
		}
	}

	private void handle_eof(Token current_token) {
		// Unwind any open statements
		while (this._flags.mode == MODE.Statement) {
			this.restore_mode();
		}
		this.handle_whitespace_and_comments(current_token);
	}

}
