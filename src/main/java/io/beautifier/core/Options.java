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

import java.util.EnumSet;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.json.JSONObject;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@NonNullByDefault
public abstract class Options<SELF extends Options<SELF>> {

	public enum TemplateLanguage {
		auto,
		none,
		django,
		erb,
		handlebars,
		php,
		smarty,
		angular,
	}

	@Accessors(fluent = true, chain = true)
	@Getter
	@Setter
	public abstract static class Builder<O extends Options<?>, SELF extends Builder<O, SELF>> {

		public @Nullable Boolean disabled;
		public @Nullable String eol;
		public @Nullable Boolean end_with_newline;
		public @Nullable Integer indent_size;
		public @Nullable String indent_char;
		public @Nullable Integer indent_level;
		public @Nullable Boolean preserve_newlines;
		public @Nullable Integer max_preserve_newlines;
		public @Nullable Boolean indent_with_tabs;
		public @Nullable Integer wrap_line_length;
		public @Nullable Boolean indent_empty_lines;
		public @Nullable EnumSet<TemplateLanguage> templating;
		protected io.beautifier.css.CSSOptions. @Nullable Builder css;
		protected io.beautifier.html.HTMLOptions. @Nullable Builder html;
		protected io.beautifier.javascript.JavaScriptOptions. @Nullable Builder js;
		protected final @Nullable Builder<?, ?> parent;

		public Builder() {
			parent = null;
		}

		public Builder(Builder<?, ?> parent) {
			this.parent = parent;
		}
		
		public abstract O build();

		/**
		 * Resolve all of the properties in this builder into the target builder. Only sets properties
		 * that are {@code non-null} in this object. Defers to parent first so child properties override
		 * parent ones.
		 * @param target
		 */
		protected void resolveTo(Builder<?, ?> target) {
			final Builder<?, ?> parent = this.parent;
			if (parent != null) {
				parent.resolveTo(target);
			}

			resolveCoreTo(target);
		}

		protected void resolveCoreTo(Builder<?, ?> target) {
			if (disabled != null) {
				target.disabled = disabled;
			}
			if (eol != null) {
				target.eol = eol;
			}
			if (end_with_newline != null) {
				target.end_with_newline = end_with_newline;
			}
			if (indent_size != null) {
				target.indent_size = indent_size;
			}
			if (indent_char != null) {
				target.indent_char = indent_char;
			}
			if (indent_level != null) {
				target.indent_level = indent_level;
			}
			if (preserve_newlines != null) {
				target.preserve_newlines = preserve_newlines;
			}
			if (max_preserve_newlines != null) {
				target.max_preserve_newlines = max_preserve_newlines;
			}
			if (indent_with_tabs != null) {
				target.indent_with_tabs = indent_with_tabs;
			}
			if (wrap_line_length != null) {
				target.wrap_line_length = wrap_line_length;
			}
			if (indent_empty_lines != null) {
				target.indent_empty_lines = indent_empty_lines;
			}
			if (templating != null) {
				target.templating = templating;
			}
			if (css != null) {
				target.css = css;
			}
			if (html != null) {
				target.html = html;
			}
			if (js != null) {
				target.js = js;
			}
		}

		public io.beautifier.css.CSSOptions.Builder css() {
			if (this.css == null) {
				this.css = new io.beautifier.css.CSSOptions.Builder(this);
			}
			return this.css;
		}

		public io.beautifier.javascript.JavaScriptOptions.Builder js() {
			if (this.js == null) {
				this.js = new io.beautifier.javascript.JavaScriptOptions.Builder(this);
			}
			return this.js;
		}

		public io.beautifier.html.HTMLOptions.Builder html() {
			if (this.html == null) {
				this.html = new io.beautifier.html.HTMLOptions.Builder(this);
			}
			return this.html;
		}

		public void apply(JSONObject data) {
			final Iterator<String> it = data.keys();
			while (it.hasNext()) {
				final String key = it.next();
				if (!apply(key, data)) {
					throw new IllegalArgumentException("Unsupported options key: " + key);
				}
			}
		}

		protected boolean apply(String key, JSONObject data) {
			switch (key) {
				case "indent_size":
					indent_size = data.getInt(key);
					return true;
				case "disabled":
					disabled = data.getBoolean(key);
					return true;
				case "html":
					html().apply(data.getJSONObject(key));
					return true;
				case "css":
					css().apply(data.getJSONObject(key));
					return true;
				case "js":
					js().apply(data.getJSONObject(key));
					return true;
			}
			return false;
		}

		@SuppressWarnings("unchecked")
		private SELF self() {
			return (SELF) this;
		}

		public SELF disabled(Boolean disabled) {
			this.disabled = disabled;
			return self();
		}

		public SELF eol(String eol) {
			this.eol = eol;
			return self();
		}

		public SELF end_with_newline(Boolean end_with_newline) {
			this.end_with_newline = end_with_newline;
			return self();
		}

		public SELF indent_size(Integer indent_size) {
			this.indent_size = indent_size;
			return self();
		}

		public SELF indent_char(String indent_char) {
			this.indent_char = indent_char;
			return self();
		}

		public SELF indent_level(Integer indent_level) {
			this.indent_level = indent_level;
			return self();
		}

		public SELF preserve_newlines(Boolean preserve_newlines) {
			this.preserve_newlines = preserve_newlines;
			return self();
		}

		public SELF max_preserve_newlines(Integer max_preserve_newlines) {
			this.max_preserve_newlines = max_preserve_newlines;
			return self();
		}

		public SELF indent_with_tabs(Boolean indent_with_tabs) {
			this.indent_with_tabs = indent_with_tabs;
			return self();
		}

		public SELF wrap_line_length(Integer wrap_line_length) {
			this.wrap_line_length = wrap_line_length;
			return self();
		}

		public SELF indent_empty_lines(Boolean indent_empty_lines) {
			this.indent_empty_lines = indent_empty_lines;
			return self();
		}

		public SELF templating(EnumSet<TemplateLanguage> templating) {
			this.templating = templating;
			return self();
		}

	}

	public boolean disabled;
	public String eol;
	public boolean end_with_newline;
	public int indent_size;
	public String indent_char;
	public int indent_level;
	public boolean preserve_newlines;
	public int max_preserve_newlines;
	public boolean indent_with_tabs;
	public int wrap_line_length;
	public boolean indent_empty_lines;
	public EnumSet<TemplateLanguage> templating;

	protected io.beautifier.javascript.JavaScriptOptions.Builder js;
	protected io.beautifier.css.CSSOptions.Builder css;
	protected io.beautifier.html.HTMLOptions.Builder html;

	protected Options(Builder<?, ?> builder) {
		disabled = resolve(builder.disabled);
		eol = resolve(builder.eol, "auto");
		end_with_newline = resolve(builder.end_with_newline);
		int indent_size = resolve(builder.indent_size, 4);
		String indent_char = resolve(builder.indent_char, " ");
		indent_level = resolve(builder.indent_level);
		preserve_newlines = resolve(builder.preserve_newlines, true);
		int max_preserve_newlines = resolve(builder.max_preserve_newlines, 32786);
		if (!preserve_newlines) {
			max_preserve_newlines = 0;
		}
		this.max_preserve_newlines = max_preserve_newlines;
		indent_with_tabs = resolve(builder.indent_with_tabs);
		if (indent_with_tabs) {
			indent_char = "\t";

			// indent_size behavior changed after 1.8.6
			// It used to be that indent_size would be
			// set to 1 for indent_with_tabs. That is no longer needed and
			// actually doesn't make sense - why not use spaces? Further,
			// that might produce unexpected behavior - tabs being used
			// for single-column alignment. So, when indent_with_tabs is true
			// and indent_size is 1, reset indent_size to 4.
			if (indent_size == 1) {
				indent_size = 4;
			}
		}

		this.indent_size = indent_size;
		this.indent_char = indent_char;
		
		wrap_line_length = resolve(builder.wrap_line_length);
		indent_empty_lines = resolve(builder.indent_empty_lines);
		templating = resolve(builder.templating, EnumSet.of(TemplateLanguage.auto));
	}

	public io.beautifier.javascript.JavaScriptOptions.Builder js() {
		return js;
	}

	public io.beautifier.css.CSSOptions.Builder css() {
		return css;
	}

	public io.beautifier.html.HTMLOptions.Builder html() {
		return html;
	}

	protected boolean resolve(@Nullable Boolean value) {
		return resolve(value, false);
	}

	protected boolean resolve(@Nullable Boolean value, boolean defaultValue) {
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

	protected int resolve(@Nullable Integer value) {
		return resolve(value, 0);
	}

	protected int resolve(@Nullable Integer value, int defaultValue) {
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}
	
	protected <T> T resolve(@Nullable T value, T defaultValue) {
		if (value != null) {
			return value;
		} else {
			return defaultValue;
		}
	}

}
