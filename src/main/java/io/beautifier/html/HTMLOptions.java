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

package io.beautifier.html;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@NonNullByDefault
public class HTMLOptions extends io.beautifier.core.Options<HTMLOptions> {

	public static Builder builder() {
		return new Builder();
	}

	@Accessors(fluent = true, chain = true)
	@Getter
	@Setter
	public static class Builder extends io.beautifier.core.Options.Builder<HTMLOptions, Builder> {

		public @Nullable Boolean indent_inner_html;
		public @Nullable Boolean indent_body_inner_html;
		public @Nullable Boolean indent_head_inner_html;
		public @Nullable Boolean indent_handlebars;
		public @Nullable WrapAttributes wrap_attributes;
		public @Nullable Integer wrap_attributes_min_attrs;
		public @Nullable Integer wrap_attributes_indent_size;
		public @Nullable String unformatted_content_delimiter;
		public @Nullable IndentScripts indent_scripts;
		public @Nullable Set<String> extra_liners;
		public @Nullable Set<String> inline;
		public @Nullable Set<String> void_elements;
		public @Nullable Set<String> unformatted;
		public @Nullable Set<String> content_unformatted;

		public Builder() {

		}

		public Builder(io.beautifier.core.Options.Builder<?, ?> parent) {
			super(parent);
		}

		@Override
		public HTMLOptions build() {
			Builder target = new Builder();
			resolveTo(target);

			HTMLOptions result = new HTMLOptions(target);
			result.css = target.css();
			result.js = target.js();
			result.html = this;
			return result;
		}

		@Override
		protected void resolveTo(io.beautifier.core.Options.Builder<?, ?> target) {
			super.resolveTo(target);

			if (target instanceof Builder) {
				resolveHtmlTo((Builder)target);
				
				if (html != null) {
					html.resolveCoreTo(target);
					html.resolveHtmlTo((Builder)target);
				}
			}
		}

		private void resolveHtmlTo(Builder target) {
			if (indent_inner_html != null) {
				target.indent_inner_html = indent_inner_html;
			}
			if (indent_body_inner_html != null) {
				target.indent_body_inner_html = indent_body_inner_html;
			}
			if (indent_head_inner_html != null) {
				target.indent_head_inner_html = indent_head_inner_html;
			}
			if (indent_handlebars != null) {
				target.indent_handlebars = indent_handlebars;
			}
			if (wrap_attributes != null) {
				target.wrap_attributes = wrap_attributes;
			}
			if (wrap_attributes_min_attrs != null) {
				target.wrap_attributes_min_attrs = wrap_attributes_min_attrs;
			}
			if (wrap_attributes_indent_size != null) {
				target.wrap_attributes_indent_size = wrap_attributes_indent_size;
			}
			if (unformatted_content_delimiter != null) {
				target.unformatted_content_delimiter = unformatted_content_delimiter;
			}
			if (indent_scripts != null) {
				target.indent_scripts = indent_scripts;
			}
			if (extra_liners != null) {
				target.extra_liners = extra_liners;
			}
			if (inline != null) {
				target.inline = inline;
			}
			if (void_elements != null) {
				target.void_elements = void_elements;
			}
			if (unformatted != null) {
				target.unformatted = unformatted;
			}
			if (content_unformatted != null) {
				target.content_unformatted = content_unformatted;
			}
		}

		public Builder indent_inner_html(Boolean indent_inner_html) {
			this.indent_inner_html = indent_inner_html;
			return self();
		}

		public Builder indent_body_inner_html(Boolean indent_body_inner_html) {
			this.indent_body_inner_html = indent_body_inner_html;
			return self();
		}

		public Builder indent_head_inner_html(Boolean indent_head_inner_html) {
			this.indent_head_inner_html = indent_head_inner_html;
			return self();
		}

		public Builder indent_handlebars(Boolean indent_handlebars) {
			this.indent_handlebars = indent_handlebars;
			return self();
		}

		public Builder wrap_attributes(WrapAttributes wrap_attributes) {
			this.wrap_attributes = wrap_attributes;
			return self();
		}

		public Builder wrap_attributes_min_attrs(Integer wrap_attributes_min_attrs) {
			this.wrap_attributes_min_attrs = wrap_attributes_min_attrs;
			return self();
		}

		public Builder wrap_attributes_indent_size(Integer wrap_attributes_indent_size) {
			this.wrap_attributes_indent_size = wrap_attributes_indent_size;
			return self();
		}

		public Builder unformatted_content_delimiter(String unformatted_content_delimiter) {
			this.unformatted_content_delimiter = unformatted_content_delimiter;
			return self();
		}

		public Builder indent_scripts(IndentScripts indent_scripts) {
			this.indent_scripts = indent_scripts;
			return self();
		}

		public Builder extra_liners(Set<String> extra_liners) {
			this.extra_liners = extra_liners;
			return self();
		}

		public Builder inline(Set<String> inline) {
			this.inline = inline;
			return self();
		}

		public Builder void_elements(Set<String> void_elements) {
			this.void_elements = void_elements;
			return self();
		}

		public Builder unformatted(Set<String> unformatted) {
			this.unformatted = unformatted;
			return self();
		}

		public Builder content_unformatted(Set<String> content_unformatted) {
			this.content_unformatted = content_unformatted;
			return self();
		}
	}

	public enum WrapAttributes {
		auto,
		force,
		forceAligned,
		forceExpandMultiline,
		alignedMultiple,
		preserve,
		preserveAligned,
		;

		public boolean isForce() {
			return this == WrapAttributes.force || this == WrapAttributes.forceAligned || this == WrapAttributes.forceExpandMultiline;
		}

		public boolean isPreserve() {
			return this == WrapAttributes.preserve || this == WrapAttributes.preserveAligned;
		}
		
	}

	public enum IndentScripts {
		normal,
		keep,
		separate,
	}

	final boolean indent_inner_html;
	final boolean indent_body_inner_html;
	final boolean indent_head_inner_html;
	final boolean indent_handlebars;
	final WrapAttributes wrap_attributes;
	final int wrap_attributes_min_attrs;
	final int wrap_attributes_indent_size;
	final @Nullable String unformatted_content_delimiter;
	final IndentScripts indent_scripts;

	final Set<String> extra_liners;
	final Set<String> inline;
	final Set<String> void_elements;
	final Set<String> unformatted;
	final Set<String> content_unformatted;

	protected HTMLOptions(Builder builder) {
		super(builder);

		if (templating.size() == 1 && templating.contains(TemplateLanguage.auto)) {
			templating = EnumSet.of(TemplateLanguage.django, TemplateLanguage.erb, TemplateLanguage.handlebars, TemplateLanguage.php);
		}

		indent_inner_html = resolve(builder.indent_inner_html);
		indent_body_inner_html = resolve(builder.indent_body_inner_html, true);
		indent_head_inner_html = resolve(builder.indent_head_inner_html, true);
		indent_handlebars = resolve(builder.indent_handlebars, true);
		wrap_attributes = resolve(builder.wrap_attributes, WrapAttributes.auto);
		wrap_attributes_min_attrs = resolve(builder.wrap_attributes_min_attrs, 2);
		wrap_attributes_indent_size = resolve(builder.wrap_attributes_indent_size, indent_size);
		unformatted_content_delimiter = resolve(builder.unformatted_content_delimiter, null);
		indent_scripts = resolve(builder.indent_scripts, IndentScripts.normal);
		extra_liners = resolve(builder.extra_liners, new HashSet<>(Arrays.asList("head", "body", "/html")));

		// Block vs inline elements
		// https://developer.mozilla.org/en-US/docs/Web/HTML/Block-level_elements
		// https://developer.mozilla.org/en-US/docs/Web/HTML/Inline_elements
		// https://www.w3.org/TR/html5/dom.html#phrasing-content
		inline = resolve(builder.inline, new HashSet<>(Arrays.asList(
			"a", "abbr", "area", "audio", "b", "bdi", "bdo", "br", "button", "canvas", "cite",
			"code", "data", "datalist", "del", "dfn", "em", "embed", "i", "iframe", "img",
			"input", "ins", "kbd", "keygen", "label", "map", "mark", "math", "meter", "noscript",
			"object", "output", "progress", "q", "ruby", "s", "samp", /* "script", */ "select", "small",
			"span", "strong", "sub", "sup", "svg", "template", "textarea", "time", "u", "var",
			"video", "wbr", "text",
			// obsolete inline tags
			"acronym", "big", "strike", "tt"
		)));

		void_elements = resolve(builder.void_elements, new HashSet<>(Arrays.asList(
			// HTLM void elements - aka self-closing tags - aka singletons
			// https://www.w3.org/html/wg/drafts/html/master/syntax.html#void-elements
			"area", "base", "br", "col", "embed", "hr", "img", "input", "keygen",
			"link", "menuitem", "meta", "param", "source", "track", "wbr",
			// NOTE: Optional tags are too complex for a simple list
			// they are hard coded in _do_optional_end_element

			// Doctype and xml elements
			"!doctype", "?xml",

			// obsolete tags
			// basefont: https://www.computerhope.com/jargon/h/html-basefont-tag.htm
			// isndex: https://developer.mozilla.org/en-US/docs/Web/HTML/Element/isindex
			"basefont", "isindex"
		)));
		unformatted = resolve(builder.unformatted, Collections.emptySet());
		content_unformatted = resolve(builder.content_unformatted, new HashSet<>(Arrays.asList(
			"pre", "textarea"
		)));
	}

}
