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
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import io.beautifier.core.Options.TemplateLanguage;

@NonNullByDefault
public class TemplatablePattern extends InputScannerPattern {

	private static class Patterns {
		private InputScannerPattern handlebars_comment;
		private InputScannerPattern handlebars_unescaped;
		private InputScannerPattern handlebars;
		private InputScannerPattern php;
		private InputScannerPattern erb;
		private InputScannerPattern django;
		private InputScannerPattern django_value;
		private InputScannerPattern django_comment;
		private InputScannerPattern smarty;
		private InputScannerPattern smarty_comment;
		private InputScannerPattern smarty_literal;
	}

	private @Nullable Pattern __template_pattern;
	private EnumSet<TemplateLanguage> _disabled;
	private EnumSet<TemplateLanguage> _excluded;
	private final Patterns __patterns;

	public TemplatablePattern(InputScanner input_scanner) {
		this(input_scanner, null);
	}

	// This lets templates appear anywhere we would do a readUntil
	// The cost is higher but it is pay to play.
	public TemplatablePattern(InputScanner input_scanner, @Nullable TemplatablePattern parent) {
		super(input_scanner, parent);
		this.__template_pattern = null;
		this._disabled = EnumSet.noneOf(TemplateLanguage.class);
		this._excluded = EnumSet.noneOf(TemplateLanguage.class);

		if (parent != null) {
			this.__template_pattern = parent.__template_pattern;
			this._excluded.addAll(parent._excluded);
			this._disabled.addAll(parent._disabled);
		}

		var pattern = new InputScannerPattern(input_scanner);
		this.__patterns = new Patterns();
		this.__patterns.handlebars_comment = pattern.starting_with(Pattern.compile("\\{\\{!--")).until_after(Pattern.compile("--}}"));
		this.__patterns.handlebars_unescaped = pattern.starting_with(Pattern.compile("\\{\\{\\{")).until_after(Pattern.compile("\\)}}}"));
		this.__patterns.handlebars = pattern.starting_with(Pattern.compile("\\{\\{")).until_after(Pattern.compile("\\)}}"));
		this.__patterns.php = pattern.starting_with(Pattern.compile("<\\?(?:[= ]|php)")).until_after(Pattern.compile("\\?>"));
		this.__patterns.erb = pattern.starting_with(Pattern.compile("<%[^%]")).until_after(Pattern.compile("[^%]%>"));
		// django coflicts with handlebars a bit.
		this.__patterns.django = pattern.starting_with(Pattern.compile("\\{%")).until_after(Pattern.compile("%}"));
		this.__patterns.django_value = pattern.starting_with(Pattern.compile("\\{\\{")).until_after(Pattern.compile("}}"));
		this.__patterns.django_comment = pattern.starting_with(Pattern.compile("\\{#")).until_after(Pattern.compile("#}"));
		this.__patterns.smarty = pattern.starting_with(Pattern.compile("\\{(?=[^}{\\s\n])")).until_after(Pattern.compile("[^\\s\n]}"));
		this.__patterns.smarty_comment = pattern.starting_with(Pattern.compile("\\{\\*")).until_after(Pattern.compile("\\*}"));
		this.__patterns.smarty_literal = pattern.starting_with(Pattern.compile("\\{literal}")).until_after(Pattern.compile("\\{/literal}"));
	}

	protected TemplatablePattern _create() {
		return new TemplatablePattern(this._input, this);
	}

	protected void _update() {
		this.__set_templated_pattern();
	}

	public TemplatablePattern disable(TemplateLanguage language) {
		var result = this._create();
		result._disabled.add(language);
		result._update();
		return result;
	}

	public TemplatablePattern read_options(Options options) {
		var result = this._create();
		for (var language : TemplateLanguage.values()) {
			if (!options.templating.contains(language)) {
				result._disabled.add(language);
			}
		}
		result._update();
		return result;
	}

	public TemplatablePattern exclude(TemplateLanguage language) {
		var result = this._create();
		result._excluded.add(language);
		result._update();
		return result;
	}

	public String read() {
		StringBuilder result = new StringBuilder();
		if (this._match_pattern != null) {
			result.append(this._input.read(this._starting_pattern));
		} else {
			result.append(this._input.read(this._starting_pattern, this.__template_pattern));
		}
		var next = this._read_template();
		while (!next.isEmpty()) {
			result.append(next);
			if (this._match_pattern != null) {
				result.append(this._input.read(this._match_pattern));
			} else {
				result.append(this._input.readUntil(this.__template_pattern));
			}
			next = this._read_template();
		}

		if (this._until_after) {
			result.append(this._input.readUntilAfter(this._until_pattern));
		}
		return result.toString();
	}

	private void __set_templated_pattern() {
		final List<String> items = new ArrayList<>();

		if (!this._disabled.contains(TemplateLanguage.php)) {
			items.add(this.__patterns.php._starting_pattern.pattern());
		}
		if (!this._disabled.contains(TemplateLanguage.handlebars)) {
			items.add(this.__patterns.handlebars._starting_pattern.pattern());
		}
		if (!this._disabled.contains(TemplateLanguage.erb)) {
			items.add(this.__patterns.erb._starting_pattern.pattern());
		}
		if (!this._disabled.contains(TemplateLanguage.django)) {
			items.add(this.__patterns.django._starting_pattern.pattern());
			// The starting pattern for django is more complex because it has different
			// patterns for value, comment, and other sections
			items.add(this.__patterns.django_value._starting_pattern.pattern());
			items.add(this.__patterns.django_comment._starting_pattern.pattern());
		}
		if (!this._disabled.contains(TemplateLanguage.smarty)) {
			items.add(this.__patterns.smarty._starting_pattern.pattern());
		}

		if (this._until_pattern != null) {
			items.add(this._until_pattern.pattern());
		}
		this.__template_pattern = Pattern.compile("(?:" + String.join("|", items) + ")");
	}

	protected String _read_template() {
		String resulting_string = "";
		var c = this._input.peek();
		if ("<".equals(c)) {
			var peek1 = this._input.peek(1);
			//if we're in a comment, do something special
			// We treat all comments as literals, even more than preformatted tags
			// we just look for the appropriate close tag
			if (!this._disabled.contains(TemplateLanguage.php) && !this._excluded.contains(TemplateLanguage.php) && "?".equals(peek1)) {
				if (resulting_string.isEmpty()) {
					resulting_string = this.__patterns.php.read();
				}
			}
			if (!this._disabled.contains(TemplateLanguage.erb) && !this._excluded.contains(TemplateLanguage.erb) && "%".equals(peek1)) {
				if (resulting_string.isEmpty()) {
					resulting_string = this.__patterns.erb.read();
				}
			}
		} else if ("{".equals(c)) {
			if (!this._disabled.contains(TemplateLanguage.handlebars) && !this._excluded.contains((TemplateLanguage.handlebars))) {
				if (resulting_string.isEmpty()) {
					resulting_string = this.__patterns.handlebars_comment.read();
				}
				if (resulting_string.isEmpty()) {
					resulting_string = this.__patterns.handlebars_unescaped.read();
				}
				if (resulting_string.isEmpty()) {
					resulting_string = this.__patterns.handlebars.read();
				}
			}

			if (!this._disabled.contains(TemplateLanguage.django)) {
				// django coflicts with handlebars a bit.
				if (!this._excluded.contains(TemplateLanguage.django) && !this._excluded.contains(TemplateLanguage.handlebars)) {
					if (resulting_string.isEmpty()) {
						resulting_string = this.__patterns.django_value.read();
					}
				}
				if (!this._excluded.contains(TemplateLanguage.django)) {
					if (resulting_string.isEmpty()) {
						resulting_string = this.__patterns.django_comment.read();
					}
					if (resulting_string.isEmpty()) {
						resulting_string = this.__patterns.django.read();
					}
				}
			}

			if (!this._disabled.contains(TemplateLanguage.smarty)) {
				// smarty cannot be enabled with django or handlebars enabled
				if (this._disabled.contains(TemplateLanguage.django) && this._disabled.contains(TemplateLanguage.handlebars)) {
					if (resulting_string.isEmpty()) {
						resulting_string = this.__patterns.smarty_comment.read();
					}
					if (resulting_string.isEmpty()) {
						resulting_string = this.__patterns.smarty_literal.read();
					}
					if (resulting_string.isEmpty()) {
						resulting_string = this.__patterns.smarty.read();
					}
				}
			}
		}
		return resulting_string;
	}
}
