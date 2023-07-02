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

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@NonNullByDefault
public class Options extends io.beautifier.core.Options<Options> {

	public static Builder builder() {
		return new Builder();
	}

	@Accessors(fluent = true, chain = true)
	@Getter
	@Setter
	public static class Builder extends io.beautifier.core.Options.Builder<Options, Builder> {

		public @Deprecated @Nullable String selector_separator;
		public @Nullable Boolean selector_separator_newline;
		public @Nullable Boolean newline_between_rules;
		public @Nullable Boolean space_around_selector_separator;
		public @Nullable Boolean space_around_combinator;
		public @Nullable BraceStyle brace_style;
		
		public Builder() {

		}

		public Builder(io.beautifier.core.Options.Builder<?, ?> parent) {
			super(parent);
		}

		@Override
		public Options build() {
			Builder target = new Builder();
			resolveTo(target);

			Options result = new Options(target);
			result.css = this;
			result.js = target.js();
			result.html = target.html();
			return result;
		}

		@Override
		protected void resolveTo(io.beautifier.core.Options.Builder<?, ?> target) {
			super.resolveTo(target);

			if (target instanceof Builder) {
				resolveCssTo((Builder) target);

				if (css != null) {
					css.resolveCoreTo(target);
					css.resolveCssTo((Builder) target);
				}
			}
		}

		private void resolveCssTo(Builder target) {
			if (selector_separator_newline != null) {
				target.selector_separator_newline = selector_separator_newline;
			}
			if (newline_between_rules != null) {
				target.newline_between_rules = newline_between_rules;
			}
			if (space_around_selector_separator != null) {
				target.space_around_selector_separator = space_around_selector_separator;
			}
			if (space_around_combinator != null) {
				target.space_around_combinator = space_around_combinator;
			}
			if (brace_style != null) {
				target.brace_style = brace_style;
			}
		}

	}

	/**
	 * For the {@code preserve-inline} style, see {@link #brace_preserve_inline}.
	 */
	public enum BraceStyle {
		collapse,
		expand,
		endExpand,
		none,
	}

	final boolean selector_separator_newline;
	final boolean newline_between_rules;
	final boolean space_around_combinator;
	final BraceStyle brace_style;

	public Options(Builder builder) {
		super(builder);

		selector_separator_newline = resolve(builder.selector_separator_newline, true);
		newline_between_rules = resolve(builder.newline_between_rules, true);
		space_around_combinator = resolve(builder.space_around_combinator) || resolve(builder.space_around_selector_separator);
		BraceStyle brace_style = resolve(builder.brace_style, BraceStyle.collapse);
		if (brace_style != BraceStyle.collapse && brace_style != BraceStyle.expand) {
			// default to collapse, as only collapse|expand is implemented for now
			brace_style = BraceStyle.collapse;
		}
		this.brace_style = brace_style;
	}

}
