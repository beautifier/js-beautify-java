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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class TokenStream<E extends Enum<?>, T extends Token<E, T>> {

	private final List<T> __tokens;
	private int __tokens_length;
	private int __position;
	private final @Nullable T __parent_token;
	
	public TokenStream() {
		this(null);
	}

	public TokenStream(@Nullable T parent_token) {
		// private
		this.__tokens = new ArrayList<>();
		this.__tokens_length = this.__tokens.size();
		this.__position = 0;
		this.__parent_token = parent_token;
	}

	public void restart() {
		this.__position = 0;
	}

	public boolean isEmpty() {
		return this.__tokens_length == 0;
	}

	public boolean hasNext() {
		return this.__position < this.__tokens_length;
	}

	public @Nullable T next() {
		if (this.hasNext()) {
			var val = this.__tokens.get(this.__position);
			this.__position += 1;
			return val;
		} else {
			return null;
		}
	}

	public @Nullable T peek() {
		return peek(0);
	}

	public @Nullable T peek(int index) {
		index += this.__position;
		if (index >= 0 && index < this.__tokens_length) {
			return this.__tokens.get(index);
		} else {
			return null;
		}
	}

	public void add(T token) {
		if (this.__parent_token != null) {
			token.parent = this.__parent_token;
		}
		this.__tokens.add(token);
		this.__tokens_length += 1;
	}

}
