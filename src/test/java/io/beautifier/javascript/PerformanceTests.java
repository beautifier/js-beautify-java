package io.beautifier.javascript;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

public class PerformanceTests {
	
	@Test
	void underscore() throws IOException {
		String input = readString(getClass().getResourceAsStream("underscore.js"));
		String result = new JavaScriptBeautifier(input, Options.builder().wrap_line_length(80).build()).beautify();
		assertNotEquals(input, result);
	}
	
	@Test
	void underscoreMin() throws IOException {
		String input = readString(getClass().getResourceAsStream("underscore-min.js"));
		String result = new JavaScriptBeautifier(input, Options.builder().wrap_line_length(80).build()).beautify();
		assertNotEquals(input, result);
	}
	
	@Test
	void github() throws IOException {
		String input = readString(getClass().getResourceAsStream("github-min.js"));
		String result = new JavaScriptBeautifier(input, Options.builder().wrap_line_length(80).build()).beautify();
		assertNotEquals(input, result);
	}

	private String readString(InputStream in) throws IOException {
		StringBuilder result = new StringBuilder();
		byte[] buf = new byte[65536];
		int read = in.read(buf);
		while (read != -1) {
			result.append(new String(buf, 0, read, StandardCharsets.UTF_8));
			read = in.read(buf);
		}
		return result.toString();
	}
	
}
