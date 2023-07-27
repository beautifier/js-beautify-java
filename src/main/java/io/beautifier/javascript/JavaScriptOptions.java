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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@NonNullByDefault
public class JavaScriptOptions extends io.beautifier.core.Options<JavaScriptOptions> {

	public static Builder builder() {
		return new Builder();
	}

	@Accessors(fluent = true, chain = true)
	@Getter
	@Setter
	public static class Builder extends io.beautifier.core.Options.Builder<JavaScriptOptions, Builder> {

		public @Nullable BraceStyle brace_style;
		public @Nullable Boolean brace_preserve_inline;
		public @Nullable Boolean unindent_chained_methods;
		public @Nullable Boolean break_chained_methods;
		public @Nullable Boolean space_in_paren;
		public @Nullable Boolean space_in_empty_paren;
		public @Nullable Boolean jslint_happy;
		public @Nullable Boolean space_after_anon_function;
		public @Nullable Boolean space_after_named_function;
		public @Nullable Boolean keep_array_indentation;
		public @Nullable Boolean space_before_conditional;
		public @Nullable Boolean unescape_strings;
		public @Nullable Boolean e4x;
		public @Nullable Boolean comma_first;
		public @Nullable OperatorPosition operator_position;
		// For testing of beautify preserve:start directive
		public @Nullable Boolean test_output_raw;

		public Builder() {
			
		}

		public Builder(io.beautifier.core.Options.Builder<?, ?> parent) {
			super(parent);
		}

		@Override
		public JavaScriptOptions build() {
			Builder target = new Builder();
			resolveTo(target);

			JavaScriptOptions result = new JavaScriptOptions(target);
			result.css = target.css();
			result.js = this;
			result.html = target.html();
			return result;
		}

		@Override
		protected void resolveTo(io.beautifier.core.Options.Builder<?, ?> target) {
			super.resolveTo(target);

			if (target instanceof Builder) {
				resolveJsTo((Builder) target);

				if (js != null) {
					js.resolveCoreTo(target);
					js.resolveJsTo((Builder)target);
				}
			}
		}

		private void resolveJsTo(Builder target) {
			if (brace_style != null) {
				target.brace_style = brace_style;
			}
			if (brace_preserve_inline != null) {
				target.brace_preserve_inline = brace_preserve_inline;
			}
			if (unindent_chained_methods != null) {
				target.unindent_chained_methods = unindent_chained_methods;
			}
			if (break_chained_methods != null) {
				target.break_chained_methods = break_chained_methods;
			}
			if (space_in_paren != null) {
				target.space_in_paren = space_in_paren;
			}
			if (space_in_empty_paren != null) {
				target.space_in_empty_paren = space_in_empty_paren;
			}
			if (jslint_happy != null) {
				target.jslint_happy = jslint_happy;
			}
			if (space_after_anon_function != null) {
				target.space_after_anon_function = space_after_anon_function;
			}
			if (space_after_named_function != null) {
				target.space_after_named_function = space_after_named_function;
			}
			if (keep_array_indentation != null) {
				target.keep_array_indentation = keep_array_indentation;
			}
			if (space_before_conditional != null) {
				target.space_before_conditional = space_before_conditional;
			}
			if (unescape_strings != null) {
				target.unescape_strings = unescape_strings;
			}
			if (e4x != null) {
				target.e4x = e4x;
			}
			if (comma_first != null) {
				target.comma_first = comma_first;
			}
			if (operator_position != null) {
				target.operator_position = operator_position;
			}
			if (test_output_raw != null) {
				target.test_output_raw = test_output_raw;
			}
		}

		public Builder brace_style(BraceStyle brace_style) {
			this.brace_style = brace_style;
			return self();
		}

		public Builder brace_preserve_inline(Boolean brace_preserve_inline) {
			this.brace_preserve_inline = brace_preserve_inline;
			return self();
		}

		public Builder unindent_chained_methods(Boolean unindent_chained_methods) {
			this.unindent_chained_methods = unindent_chained_methods;
			return self();
		}

		public Builder break_chained_methods(Boolean break_chained_methods) {
			this.break_chained_methods = break_chained_methods;
			return self();
		}

		public Builder space_in_paren(Boolean space_in_paren) {
			this.space_in_paren = space_in_paren;
			return self();
		}

		public Builder space_in_empty_paren(Boolean space_in_empty_paren) {
			this.space_in_empty_paren = space_in_empty_paren;
			return self();
		}

		public Builder jslint_happy(Boolean jslint_happy) {
			this.jslint_happy = jslint_happy;
			return self();
		}

		public Builder space_after_anon_function(Boolean space_after_anon_function) {
			this.space_after_anon_function = space_after_anon_function;
			return self();
		}

		public Builder space_after_named_function(Boolean space_after_named_function) {
			this.space_after_named_function = space_after_named_function;
			return self();
		}

		public Builder keep_array_indentation(Boolean keep_array_indentation) {
			this.keep_array_indentation = keep_array_indentation;
			return self();
		}

		public Builder space_before_conditional(Boolean space_before_conditional) {
			this.space_before_conditional = space_before_conditional;
			return self();
		}

		public Builder unescape_strings(Boolean unescape_strings) {
			this.unescape_strings = unescape_strings;
			return self();
		}

		public Builder e4x(Boolean e4x) {
			this.e4x = e4x;
			return self();
		}

		public Builder comma_first(Boolean comma_first) {
			this.comma_first = comma_first;
			return self();
		}

		public Builder operator_position(OperatorPosition operator_position) {
			this.operator_position = operator_position;
			return self();
		}

		public Builder test_output_raw(Boolean test_output_raw) {
			this.test_output_raw = test_output_raw;
			return self();
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

	public enum OperatorPosition {
		beforeNewline,
		afterNewline,
		preserveNewline,
	}

	final BraceStyle brace_style;
	final boolean brace_preserve_inline;
	final boolean unindent_chained_methods;
	final boolean break_chained_methods;
	final boolean space_in_paren;
	final boolean space_in_empty_paren;
	final boolean jslint_happy;
	final boolean space_after_anon_function;
	final boolean space_after_named_function;
	boolean keep_array_indentation;
	final boolean space_before_conditional;
	final boolean unescape_strings;
	final boolean e4x;
	final boolean comma_first;
	final OperatorPosition operator_position;
	// For testing of beautify preserve:start directive
	final boolean test_output_raw;

	public JavaScriptOptions(Builder builder) {
		super(builder);

		//todo
		brace_style = resolve(builder.brace_style, BraceStyle.collapse);
		brace_preserve_inline = resolve(builder.brace_preserve_inline);
		unindent_chained_methods = resolve(builder.unindent_chained_methods);
		break_chained_methods = resolve(builder.break_chained_methods);
		space_in_paren = resolve(builder.space_in_paren);
		space_in_empty_paren = resolve(builder.space_in_empty_paren);
		jslint_happy = resolve(builder.jslint_happy);
		// force this._options.space_after_anon_function to true if this._options.jslint_happy
		if (jslint_happy) {
			space_after_anon_function = true;
		} else {
			space_after_anon_function = resolve(builder.space_after_anon_function);
		}
		space_after_named_function = resolve(builder.space_after_named_function);
		keep_array_indentation = resolve(builder.keep_array_indentation);
		space_before_conditional = resolve(builder.space_before_conditional, true);
		unescape_strings = resolve(builder.unescape_strings);
		e4x = resolve(builder.e4x);
		comma_first = resolve(builder.comma_first);
		operator_position = resolve(builder.operator_position, OperatorPosition.beforeNewline);
		test_output_raw = resolve(builder.test_output_raw);
	}
	
}
