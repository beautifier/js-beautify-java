package io.beautifier.css;

import org.junit.jupiter.api.Test;

public class OptionsTests {
	
	@Test
	void constructOptions() {
		Options.builder()
			.disabled(false)
			.indent_size(4)
			.newline_between_rules(true)
			;
	}

}
