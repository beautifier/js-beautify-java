package io.beautifier.css;

import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

public class PerformanceTests {
	
	@Test
	void github() throws IOException {
		String input = readString(getClass().getResourceAsStream("github.css"));
		String result = new Beautifier(input, new Options().indent_size(4)).beautify();
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
