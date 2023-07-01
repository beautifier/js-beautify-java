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

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
public class Token<E extends Enum<?>, SELF extends Token<E, SELF>> {
	
	public E type;
	public final String text;
	// comments_before are
	// comments that have a new line before them
	// and may or may not have a newline after
	// this is a set of comments before
	public @Nullable TokenStream<E, SELF> comments_before; /* inline comment*/
	public int newlines;
	public String whitespace_before;
	public @Nullable SELF parent;
	public @Nullable SELF next;
	public @Nullable SELF previous;
	public @Nullable SELF opened;
	public @Nullable SELF closed;
	public @Nullable Map<String, String> directives;

	public Token(E type, String text) {
		this(type, text, 0, null);
	}

	public Token(E type, String text, int newlines, @Nullable String whitespace_before) {
		this.type = type;
		this.text = text;
		this.newlines = newlines;

		this.whitespace_before = whitespace_before != null ? whitespace_before : "";
	}

}
