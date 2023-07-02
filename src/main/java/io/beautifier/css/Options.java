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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true, chain = true)
@EqualsAndHashCode(callSuper = true)
@NonNullByDefault
public class Options extends io.beautifier.core.Options<Options> {

	/**
	 * For the {@code preserve-inline} style, see {@link #brace_preserve_inline}.
	 */
	public enum BraceStyle {
		collapse,
		expand,
		endExpand,
		none,
	}

	@Deprecated @Nullable String selector_separator;
	boolean selector_separator_newline = true;
	boolean newline_between_rules = true;
	boolean space_around_selector_separator;
	boolean space_around_combinator;
	BraceStyle brace_style = BraceStyle.collapse;
	
	public Options() {
		
	}

	public void apply(String json) {
		final JSONObject data = new JSONObject(json);
		for (String key : data.keySet()) {
			switch (key) {
				case "indent_size":
					this.indent_size = data.getInt("indent_size");
					break;
				default:
					throw new IllegalArgumentException("Unsupported options key: " + key);
			}
		}
	}

	@Override
	protected void prepare() {
		super.prepare();

		space_around_combinator = space_around_combinator || space_around_selector_separator;

		if (brace_style != BraceStyle.collapse && brace_style != BraceStyle.expand) {
			// default to collapse, as only collapse|expand is implemented for now
			brace_style = BraceStyle.collapse;
		}
	}

}
