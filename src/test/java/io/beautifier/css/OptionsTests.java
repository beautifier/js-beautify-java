package io.beautifier.css;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.beautifier.css.CSSOptions.BraceStyle;

public class OptionsTests {
	
	@Test
	void simpleOptions() {
		CSSOptions options = CSSOptions.builder()
			.disabled(false)
			.indent_size(4)
			.newline_between_rules(true)
			.build();
		assertFalse(options.disabled);
		assertEquals(4, options.indent_size);
		assertTrue(options.newline_between_rules);
		assertEquals(BraceStyle.collapse, options.brace_style); /* The default */
	}

	@Test
	void cssSpecificOptions() {
		CSSOptions.Builder builder = CSSOptions.builder()
			.indent_size(5)
			;
		builder.css()
			.indent_size(6)
			;
		builder.js()
			.indent_size(7)
			;
		builder.html()
			.indent_size(8)
			;
		CSSOptions options = builder.build();
		assertEquals(6, options.indent_size);
	}

}
