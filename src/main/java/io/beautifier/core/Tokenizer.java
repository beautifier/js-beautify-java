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

import java.util.Stack;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class Tokenizer<E extends Enum<?>, T extends Token<E, T>> {

	private final E TOKEN_START;
	private final E TOKEN_RAW;
	private final E TOKEN_EOF;
	private final TokenSupplier<E, T> tokenSupplier;

	protected final InputScanner _input;
	private @Nullable Options _options;
	private @Nullable TokenStream<E, T> __tokens;
	protected final Patterns _patterns;

	protected static class Patterns {

		public WhitespacePattern whitespace;

		Patterns(WhitespacePattern whitespace) {
			this.whitespace = whitespace;
		}

	}

	public Tokenizer(String input_string, TokenSupplier<E, T> tokenSupplier, E TOKEN_START, E TOKEN_RAW, E TOKEN_EOF, @Nullable Options options) {
		this._input = new InputScanner(input_string);
		this._options = options;
		this.tokenSupplier = tokenSupplier;
		this.TOKEN_START = TOKEN_START;
		this.TOKEN_RAW = TOKEN_RAW;
		this.TOKEN_EOF = TOKEN_EOF;
		this.__tokens = null;

		this._patterns = new Patterns(new WhitespacePattern(this._input));
	}

	public TokenStream<E, T> tokenize() {
		this._input.restart();
		this.__tokens = new TokenStream<>();

		this._reset();

		@Nullable T current = null;
		var previous = tokenSupplier.createToken(TOKEN_START, "", 0, null);
		@Nullable T open_token = null;
		final Stack<T> open_stack = new Stack<>();
		var comments = new TokenStream<E, T>();

		while (previous.type != TOKEN_EOF) {
			current = this._get_next_token(previous, open_token);
			while (this._is_comment(current)) {
				comments.add(current);
				current = this._get_next_token(previous, open_token);
			}

			if (!comments.isEmpty()) {
				current.comments_before = comments;
				comments = new TokenStream<E, T>();
			}

			current.parent = open_token;

			if (this._is_opening(current)) {
				open_stack.push(open_token);
				open_token = current;
			} else if (open_token != null && this._is_closing(current, open_token)) {
				current.opened = open_token;
				open_token.closed = current;
				open_token = open_stack.pop();
				current.parent = open_token;
			}

			current.previous = previous;
			previous.next = current;

			this.__tokens.add(current);
			previous = current;
		}

		return this.__tokens;
	}


	protected boolean _is_first_token() {
		return this.__tokens.isEmpty();
	}

	protected void _reset() {

	}

	private static final Pattern GET_NEXT_TOKEN_PATTERN = Pattern.compile(".+");

	protected T _get_next_token(T previous_token, @Nullable T open_token) {
		this._readWhitespace();
		var resulting_string = this._input.read(GET_NEXT_TOKEN_PATTERN);
		if (!resulting_string.isEmpty()) {
			return this._create_token(TOKEN_RAW, resulting_string);
		} else {
			return this._create_token(TOKEN_EOF, "");
		}
	}

	protected boolean _is_comment(T current_token) {
		return false;
	}

	protected boolean _is_opening(T current_token) {
		return false;
	}

	protected boolean _is_closing(T current_token, @Nullable T open_token) {
		return false;
	}

	protected T _create_token(E type, char text) {
		return _create_token(type, Character.toString(text));
	}

	protected T _create_token(E type, String text) {
		var token = tokenSupplier.createToken(type, text,
			this._patterns.whitespace.newline_count,
			this._patterns.whitespace.whitespace_before_token);
		return token;
	}

	protected String _readWhitespace() {
		return this._patterns.whitespace.read();
	}
}
