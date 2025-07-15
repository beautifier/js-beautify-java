/*
    AUTO-GENERATED. DO NOT MODIFY.
    Script: generate-tests.js
    Template: data/html/java.mustache
    Data: data/html/tests.js

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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.beautifier.core.Options.TemplateLanguage;
import io.beautifier.css.CSSBeautifier;
import io.beautifier.html.HTMLOptions.IndentScripts;
import io.beautifier.html.HTMLOptions.WrapAttributes;
import io.beautifier.javascript.JavaScriptBeautifier;

public class GeneratedTests {

	private HTMLOptions.Builder opts;
	
	@BeforeEach
	void reset_options() {
		opts = HTMLOptions.builder();
		opts.indent_size = 4;
		opts.indent_char = " ";
		opts.preserve_newlines = true;
		opts.end_with_newline = false;

		opts.indent_size = 4;
		opts.indent_char = " ";
		opts.indent_with_tabs = false;
		opts.preserve_newlines = true;
		opts.extra_liners = new java.util.HashSet<>(java.util.Arrays.asList("html", "head", "/html"));
	}

	private String test_beautifier(String input)
	{
		return new HTMLBeautifier(input, opts.build(), JavaScriptBeautifier::beautify, CSSBeautifier::beautify).beautify();
	}

	private void test_fragment(String input) {
		test_fragment(input, null);
	}

	// test the input on beautifier with the current flag settings
	// does not check the indentation / surroundings as bt() does
	private void test_fragment(String input, String expected)
	{
		if (expected == null) {
			expected = input;
		}

		assertEquals(expected, test_beautifier(input));

		// if the expected is different from input, run it again
		// expected output should be unchanged when run twice.
		if (!Objects.equals(input, expected)) {
			assertEquals(expected, test_beautifier(expected));
		}

		// Everywhere we do newlines, they should be replaced with opts.eol
		opts.eol = "\r\n";
		expected = expected.replaceAll("\n", "\r\n");
		opts.disabled = true;
		assertEquals(input != null ? input : "", test_beautifier(input));
		assertEquals("\n\n" + expected, test_beautifier("\n\n" + expected));
		opts.disabled = false;
		assertEquals(expected, test_beautifier(input));
		if (input != null && input.indexOf('\n') != -1) {
			input = input.replaceAll("\n", "\r\n");
			assertEquals(expected, test_beautifier(input));
			// Ensure support for auto eol detection
			opts.eol = "auto";
			assertEquals(expected, test_beautifier(input));
		}
		opts.eol = "\n";
	}

	private void bth(String input) {
		bth(input, null);
	}

	// test html
	private void bth(String input, String expectation)
	{
		String wrapped_input, wrapped_expectation;

		if (expectation == null) {
			expectation = input;
		}
		test_fragment(input, expectation);

		if (opts.build().indent_size == 4 && input != null) {
			var indent_string = opts.indent_with_tabs ? "\t" : "    ";
			wrapped_input = "<div>\n" + Pattern.compile("^(.+)$", Pattern.MULTILINE).matcher(input).replaceAll(indent_string + "$1") + '\n' + indent_string + "<span>inline</span>\n</div>";
			wrapped_expectation = "<div>\n" + Pattern.compile("^(.+)$", Pattern.MULTILINE).matcher(expectation).replaceAll(indent_string + "$1") + '\n' + indent_string + "<span>inline</span>\n</div>";
			test_fragment(wrapped_input, wrapped_expectation);
		}
	}

	private String unicode_char(int value) {
		return String.copyValueOf(Character.toChars(value));
	}

	@Test
	void beautifier_tests()
	{
		bth("");
	}


	@Test
	@DisplayName("Unicode Support")
	void Unicode_Support() {
		bth("<p>Hello" + unicode_char(160) + unicode_char(3232) + "_" + unicode_char(3232) + "world!</p>");
	}


	@Test
	@DisplayName("Handle inline and block elements differently - ()")
	void Handle_inline_and_block_elements_differently_() {
		test_fragment(
            "<body><h1>Block</h1></body>",
            //  -- output --
            "<body>\n" +
            "    <h1>Block</h1>\n" +
            "</body>");
		test_fragment("<body><i>Inline</i></body>");
		bth(
            "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" preserveAspectRatio=\"none\" x=\"0\" y=\"0\" viewBox=\"0 0 900 710\" width=\"100%\" height=\"100%\">\n" +
            "<circle id=\"mycircle\" \n" +
            "cx=\"182.901\" cy=\"91.4841\" \n" +
            "style=\"fill:rosybrown;stroke:black;stroke-width:1px;\" r=\"48\" /></svg>",
            //  -- output --
            "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:svg=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" preserveAspectRatio=\"none\" x=\"0\" y=\"0\" viewBox=\"0 0 900 710\" width=\"100%\" height=\"100%\">\n" +
            "    <circle id=\"mycircle\" cx=\"182.901\" cy=\"91.4841\" style=\"fill:rosybrown;stroke:black;stroke-width:1px;\" r=\"48\" />\n" +
            "</svg>");
		bth(
            "<div class=\"col-xs-2\">\n" +
            "<input type=\"radio\" class=\"control-label\" ng-disabled=\"!col\" ng-model=\"col\" value=\"2\" class=\"form-control\" id=\"coli\" name=\"coli\" />\n" +
            "<label for=\"coli\" class=\"control-label\">Collision</label></div>",
            //  -- output --
            "<div class=\"col-xs-2\">\n" +
            "    <input type=\"radio\" class=\"control-label\" ng-disabled=\"!col\" ng-model=\"col\" value=\"2\" class=\"form-control\" id=\"coli\" name=\"coli\" />\n" +
            "    <label for=\"coli\" class=\"control-label\">Collision</label>\n" +
            "</div>");
		bth(
            "<label class=\"col-xs-2\">Collision\n" +
            "<input type=\"radio\" class=\"control-label\" ng-disabled=\"!col\" ng-model=\"col\" value=\"2\" class=\"form-control\" id=\"coli\" name=\"coli\" /></label>",
            //  -- output --
            "<label class=\"col-xs-2\">Collision\n" +
            "    <input type=\"radio\" class=\"control-label\" ng-disabled=\"!col\" ng-model=\"col\" value=\"2\" class=\"form-control\" id=\"coli\" name=\"coli\" /></label>");
		bth(
            "<div class=\"col-xs-2\">Collision\n" +
            "<input type=\"radio\" class=\"control-label\" ng-disabled=\"!col\" ng-model=\"col\" value=\"2\" class=\"form-control\" id=\"coli\" name=\"coli\" /></div>",
            //  -- output --
            "<div class=\"col-xs-2\">Collision\n" +
            "    <input type=\"radio\" class=\"control-label\" ng-disabled=\"!col\" ng-model=\"col\" value=\"2\" class=\"form-control\" id=\"coli\" name=\"coli\" />\n" +
            "</div>");
	}


	@Test
	@DisplayName("End With Newline - (end_with_newline = \"true\")")
	void End_With_Newline_end_with_newline_true_() {
		opts.end_with_newline = true;
		test_fragment("", "\n");
		test_fragment("<div></div>", "<div></div>\n");
		test_fragment("\n");
	}

	@Test
	@DisplayName("End With Newline - (end_with_newline = \"false\")")
	void End_With_Newline_end_with_newline_false_() {
		opts.end_with_newline = false;
		test_fragment("");
		test_fragment("<div></div>");
		test_fragment("\n", "");
	}


	@Test
	@DisplayName("Support Indent Level Options and Base Indent Autodetection - ()")
	void Support_Indent_Level_Options_and_Base_Indent_Autodetection_() {
		test_fragment("   a");
		test_fragment(
            "   <div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "   <div>\n" +
            "       <p>This is my sentence.</p>\n" +
            "   </div>");
		test_fragment(
            "   // This is a random comment\n" +
            "<div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "   // This is a random comment\n" +
            "   <div>\n" +
            "       <p>This is my sentence.</p>\n" +
            "   </div>");
	}

	@Test
	@DisplayName("Support Indent Level Options and Base Indent Autodetection - (indent_level = \"0\")")
	void Support_Indent_Level_Options_and_Base_Indent_Autodetection_indent_level_0_() {
		opts.indent_level = 0;
		test_fragment("   a");
		test_fragment(
            "   <div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "   <div>\n" +
            "       <p>This is my sentence.</p>\n" +
            "   </div>");
		test_fragment(
            "   // This is a random comment\n" +
            "<div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "   // This is a random comment\n" +
            "   <div>\n" +
            "       <p>This is my sentence.</p>\n" +
            "   </div>");
	}

	@Test
	@DisplayName("Support Indent Level Options and Base Indent Autodetection - (indent_level = \"1\")")
	void Support_Indent_Level_Options_and_Base_Indent_Autodetection_indent_level_1_() {
		opts.indent_level = 1;
		test_fragment("   a", "    a");
		test_fragment(
            "   <div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "    <div>\n" +
            "        <p>This is my sentence.</p>\n" +
            "    </div>");
		test_fragment(
            "   // This is a random comment\n" +
            "<div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "    // This is a random comment\n" +
            "    <div>\n" +
            "        <p>This is my sentence.</p>\n" +
            "    </div>");
	}

	@Test
	@DisplayName("Support Indent Level Options and Base Indent Autodetection - (indent_level = \"2\")")
	void Support_Indent_Level_Options_and_Base_Indent_Autodetection_indent_level_2_() {
		opts.indent_level = 2;
		test_fragment("a", "        a");
		test_fragment(
            "<div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "        <div>\n" +
            "            <p>This is my sentence.</p>\n" +
            "        </div>");
		test_fragment(
            "// This is a random comment\n" +
            "<div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "        // This is a random comment\n" +
            "        <div>\n" +
            "            <p>This is my sentence.</p>\n" +
            "        </div>");
	}

	@Test
	@DisplayName("Support Indent Level Options and Base Indent Autodetection - (indent_with_tabs = \"true\", indent_level = \"2\")")
	void Support_Indent_Level_Options_and_Base_Indent_Autodetection_indent_with_tabs_true_indent_level_2_() {
		opts.indent_with_tabs = true;
		opts.indent_level = 2;
		test_fragment("a", "\t\ta");
		test_fragment(
            "<div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "\t\t<div>\n" +
            "\t\t\t<p>This is my sentence.</p>\n" +
            "\t\t</div>");
		test_fragment(
            "// This is a random comment\n" +
            "<div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "\t\t// This is a random comment\n" +
            "\t\t<div>\n" +
            "\t\t\t<p>This is my sentence.</p>\n" +
            "\t\t</div>");
	}

	@Test
	@DisplayName("Support Indent Level Options and Base Indent Autodetection - (indent_level = \"0\")")
	void Support_Indent_Level_Options_and_Base_Indent_Autodetection_indent_level_0_1() {
		opts.indent_level = 0;
		test_fragment("\t   a");
		test_fragment(
            "\t   <div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "\t   <div>\n" +
            "\t       <p>This is my sentence.</p>\n" +
            "\t   </div>");
		test_fragment(
            "\t   // This is a random comment\n" +
            "<div>\n" +
            "  <p>This is my sentence.</p>\n" +
            "</div>",
            //  -- output --
            "\t   // This is a random comment\n" +
            "\t   <div>\n" +
            "\t       <p>This is my sentence.</p>\n" +
            "\t   </div>");
	}


	@Test
	@DisplayName("Custom Extra Liners (empty) - (extra_liners = \"[]\")")
	void Custom_Extra_Liners_empty_extra_liners_() {
		opts.extra_liners = new java.util.HashSet<>(java.util.Arrays.asList());
		test_fragment(
            "<html><head><meta></head><body><div><p>x</p></div></body></html>",
            //  -- output --
            "<html>\n" +
            "<head>\n" +
            "    <meta>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div>\n" +
            "        <p>x</p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>");
	}


	@Test
	@DisplayName("Custom Extra Liners (default) - (extra_liners = \"null\")")
	void Custom_Extra_Liners_default_extra_liners_null_() {
		opts.extra_liners = null;
		test_fragment(
            "<html><head></head><body></body></html>",
            //  -- output --
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
	}


	@Test
	@DisplayName("Custom Extra Liners (p, string) - (extra_liners = \"\"p,/p\"\")")
	void Custom_Extra_Liners_p_string_extra_liners_p_p_() {
		opts.extra_liners = new java.util.HashSet<>(java.util.Arrays.asList("p", "/p"));
		test_fragment(
            "<html><head><meta></head><body><div><p>x</p></div></body></html>",
            //  -- output --
            "<html>\n" +
            "<head>\n" +
            "    <meta>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div>\n" +
            "\n" +
            "        <p>x\n" +
            "\n" +
            "        </p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>");
	}


	@Test
	@DisplayName("Custom Extra Liners (p) - (extra_liners = \"[\"p\", \"/p\"]\")")
	void Custom_Extra_Liners_p_extra_liners_p_p_() {
		opts.extra_liners = new java.util.HashSet<>(java.util.Arrays.asList("p", "/p"));
		test_fragment(
            "<html><head><meta></head><body><div><p>x</p></div></body></html>",
            //  -- output --
            "<html>\n" +
            "<head>\n" +
            "    <meta>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div>\n" +
            "\n" +
            "        <p>x\n" +
            "\n" +
            "        </p>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>");
	}


	@Test
	@DisplayName("Tests for script and style Commented and cdata wapping (#1641)")
	void Tests_for_script_and_style_Commented_and_cdata_wapping_1641_() {
		opts.templating = EnumSet.of(TemplateLanguage.valueOf("php"));
		bth(
            "<style><!----></style>",
            //  -- output --
            "<style>\n" +
            "    <!--\n" +
            "    -->\n" +
            "</style>");
		bth(
            "<style><!--\n" +
            "--></style>",
            //  -- output --
            "<style>\n" +
            "    <!--\n" +
            "    -->\n" +
            "</style>");
		bth(
            "<style><!-- the rest of this   line is   ignored\n" +
            "\n" +
            "\n" +
            "\n" +
            "--></style>",
            //  -- output --
            "<style>\n" +
            "    <!-- the rest of this   line is   ignored\n" +
            "    -->\n" +
            "</style>");
		bth(
            "<style type=\"test/null\"><!--\n" +
            "\n" +
            "\t  \n" +
            "\n" +
            "--></style>",
            //  -- output --
            "<style type=\"test/null\">\n" +
            "    <!--\n" +
            "    -->\n" +
            "</style>");
		bth(
            "<script><!--\n" +
            "console.log(\"</script>\" + \"</style>\");\n" +
            "--></script>",
            //  -- output --
            "<script>\n" +
            "    <!--\n" +
            "    console.log(\"</script>\" + \"</style>\");\n" +
            "    -->\n" +
            "</script>");
		
        // If wrapping is incomplete, print remaining unchanged.
        test_fragment(
            "<div>\n" +
            "<script><!--\n" +
            "console.log(\"</script>\" + \"</style>\");\n" +
            " </script>\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <script><!--\n" +
            "console.log(\"</script>\" + \"</style>\");\n" +
            " </script>\n" +
            "</div>");
		bth(
            "<style><!--\n" +
            ".selector {\n" +
            "    font-family: \"</script></style>\";\n" +
            "    }\n" +
            "--></style>",
            //  -- output --
            "<style>\n" +
            "    <!--\n" +
            "    .selector {\n" +
            "        font-family: \"</script></style>\";\n" +
            "    }\n" +
            "    -->\n" +
            "</style>");
		bth(
            "<script type=\"test/null\">\n" +
            "    <!--\n" +
            "   console.log(\"</script>\" + \"</style>\");\n" +
            "    console.log(\"</script>\" + \"</style>\");\n" +
            "--></script>",
            //  -- output --
            "<script type=\"test/null\">\n" +
            "    <!--\n" +
            "    console.log(\"</script>\" + \"</style>\");\n" +
            "     console.log(\"</script>\" + \"</style>\");\n" +
            "    -->\n" +
            "</script>");
		bth(
            "<script type=\"test/null\"><!--\n" +
            " console.log(\"</script>\" + \"</style>\");\n" +
            "      console.log(\"</script>\" + \"</style>\");\n" +
            "--></script>",
            //  -- output --
            "<script type=\"test/null\">\n" +
            "    <!--\n" +
            "    console.log(\"</script>\" + \"</style>\");\n" +
            "         console.log(\"</script>\" + \"</style>\");\n" +
            "    -->\n" +
            "</script>");
		bth(
            "<script><![CDATA[\n" +
            "console.log(\"</script>\" + \"</style>\");\n" +
            "]]></script>",
            //  -- output --
            "<script>\n" +
            "    <![CDATA[\n" +
            "    console.log(\"</script>\" + \"</style>\");\n" +
            "    ]]>\n" +
            "</script>");
		bth(
            "<style><![CDATA[\n" +
            ".selector {\n" +
            "    font-family: \"</script></style>\";\n" +
            "    }\n" +
            "]]></style>",
            //  -- output --
            "<style>\n" +
            "    <![CDATA[\n" +
            "    .selector {\n" +
            "        font-family: \"</script></style>\";\n" +
            "    }\n" +
            "    ]]>\n" +
            "</style>");
		bth(
            "<script type=\"test/null\">\n" +
            "    <![CDATA[\n" +
            "   console.log(\"</script>\" + \"</style>\");\n" +
            "    console.log(\"</script>\" + \"</style>\");\n" +
            "]]></script>",
            //  -- output --
            "<script type=\"test/null\">\n" +
            "    <![CDATA[\n" +
            "    console.log(\"</script>\" + \"</style>\");\n" +
            "     console.log(\"</script>\" + \"</style>\");\n" +
            "    ]]>\n" +
            "</script>");
		bth(
            "<script type=\"test/null\"><![CDATA[\n" +
            " console.log(\"</script>\" + \"</style>\");\n" +
            "      console.log(\"</script>\" + \"</style>\");\n" +
            "]]></script>",
            //  -- output --
            "<script type=\"test/null\">\n" +
            "    <![CDATA[\n" +
            "    console.log(\"</script>\" + \"</style>\");\n" +
            "         console.log(\"</script>\" + \"</style>\");\n" +
            "    ]]>\n" +
            "</script>");
		
        // Issue #1687 - start with <?php ?>
        bth(
            "<script>\n" +
            "<?php ?>\n" +
            "var b={\n" +
            "a:function _test( ){\n" +
            "return 1;\n" +
            "}\n" +
            "}\n" +
            "</script>",
            //  -- output --
            "<script>\n" +
            "    <?php ?>\n" +
            "    var b = {\n" +
            "        a: function _test() {\n" +
            "            return 1;\n" +
            "        }\n" +
            "    }\n" +
            "</script>");
	}


	@Test
	@DisplayName("Tests for script and style types (issue 453, 821)")
	void Tests_for_script_and_style_types_issue_453_821_() {
		bth("<script type=\"text/unknown\"><div></div></script>");
		bth("<script type=\"text/unknown\">Blah Blah Blah</script>");
		bth("<script type=\"text/unknown\">    Blah Blah Blah   </script>", "<script type=\"text/unknown\"> Blah Blah Blah   </script>");
		bth(
            "<script type=\"text/javascript\"><div></div></script>",
            //  -- output --
            "<script type=\"text/javascript\">\n" +
            "    < div > < /div>\n" +
            "</script>");
		bth(
            "<script><div></div></script>",
            //  -- output --
            "<script>\n" +
            "    < div > < /div>\n" +
            "</script>");
		
        // text/html should beautify as html
        bth(
            "<script type=\"text/html\">\n" +
            "<div>\n" +
            "<div></div><div></div></div></script>",
            //  -- output --
            "<script type=\"text/html\">\n" +
            "    <div>\n" +
            "        <div></div>\n" +
            "        <div></div>\n" +
            "    </div>\n" +
            "</script>");
		
        // null beatifier behavior - should still indent
        test_fragment(
            "<script type=\"test/null\">\n" +
            "    <div>\n" +
            "  <div></div><div></div></div></script>",
            //  -- output --
            "<script type=\"test/null\">\n" +
            "    <div>\n" +
            "      <div></div><div></div></div>\n" +
            "</script>");
		bth(
            "<script type=\"test/null\">\n" +
            "   <div>\n" +
            "     <div></div><div></div></div></script>",
            //  -- output --
            "<script type=\"test/null\">\n" +
            "    <div>\n" +
            "      <div></div><div></div></div>\n" +
            "</script>");
		bth(
            "<script type=\"test/null\">\n" +
            "<div>\n" +
            "<div></div><div></div></div></script>",
            //  -- output --
            "<script type=\"test/null\">\n" +
            "    <div>\n" +
            "    <div></div><div></div></div>\n" +
            "</script>");
		bth(
            "<script>var foo = \"bar\";</script>",
            //  -- output --
            "<script>\n" +
            "    var foo = \"bar\";\n" +
            "</script>");
		
        // Issue #1606 - type attribute on other element
        bth(
            "<script>\n" +
            "console.log(1  +  1);\n" +
            "</script>\n" +
            "\n" +
            "<input type=\"submit\"></input>",
            //  -- output --
            "<script>\n" +
            "    console.log(1 + 1);\n" +
            "</script>\n" +
            "\n" +
            "<input type=\"submit\"></input>");
		bth(
            "<script type=\"text/javascript\">console.log(1  +  1);</script>",
            //  -- output --
            "<script type=\"text/javascript\">\n" +
            "    console.log(1 + 1);\n" +
            "</script>");
		
        // Issue #1706 - es script module
        bth(
            "<script type=\"module\">console.log(1  +  1);</script>",
            //  -- output --
            "<script type=\"module\">\n" +
            "    console.log(1 + 1);\n" +
            "</script>");
		bth(
            "<script type=\"application/javascript\">var foo = \"bar\";</script>",
            //  -- output --
            "<script type=\"application/javascript\">\n" +
            "    var foo = \"bar\";\n" +
            "</script>");
		bth(
            "<script type=\"application/javascript;version=1.8\">var foo = \"bar\";</script>",
            //  -- output --
            "<script type=\"application/javascript;version=1.8\">\n" +
            "    var foo = \"bar\";\n" +
            "</script>");
		bth(
            "<script type=\"application/x-javascript\">var foo = \"bar\";</script>",
            //  -- output --
            "<script type=\"application/x-javascript\">\n" +
            "    var foo = \"bar\";\n" +
            "</script>");
		bth(
            "<script type=\"application/ecmascript\">var foo = \"bar\";</script>",
            //  -- output --
            "<script type=\"application/ecmascript\">\n" +
            "    var foo = \"bar\";\n" +
            "</script>");
		bth(
            "<script type=\"dojo/aspect\">this.domNode.style.display=\"none\";</script>",
            //  -- output --
            "<script type=\"dojo/aspect\">\n" +
            "    this.domNode.style.display = \"none\";\n" +
            "</script>");
		bth(
            "<script type=\"dojo/method\">this.domNode.style.display=\"none\";</script>",
            //  -- output --
            "<script type=\"dojo/method\">\n" +
            "    this.domNode.style.display = \"none\";\n" +
            "</script>");
		bth(
            "<script type=\"text/javascript1.5\">var foo = \"bar\";</script>",
            //  -- output --
            "<script type=\"text/javascript1.5\">\n" +
            "    var foo = \"bar\";\n" +
            "</script>");
		bth(
            "<script type=\"application/json\">{\"foo\":\"bar\"}</script>",
            //  -- output --
            "<script type=\"application/json\">\n" +
            "    {\n" +
            "        \"foo\": \"bar\"\n" +
            "    }\n" +
            "</script>");
		bth(
            "<script type=\"application/ld+json\">{\"foo\":\"bar\"}</script>",
            //  -- output --
            "<script type=\"application/ld+json\">\n" +
            "    {\n" +
            "        \"foo\": \"bar\"\n" +
            "    }\n" +
            "</script>");
		bth("<style type=\"text/unknown\"><tag></tag></style>");
		bth(
            "<style type=\"text/css\"><tag></tag></style>",
            //  -- output --
            "<style type=\"text/css\">\n" +
            "    <tag></tag>\n" +
            "</style>");
		bth(
            "<style><tag></tag></style>",
            //  -- output --
            "<style>\n" +
            "    <tag></tag>\n" +
            "</style>");
		bth(
            "<style>.selector {font-size:12px;}</style>",
            //  -- output --
            "<style>\n" +
            "    .selector {\n" +
            "        font-size: 12px;\n" +
            "    }\n" +
            "</style>");
		bth(
            "<style type=\"text/css\">.selector {font-size:12px;}</style>",
            //  -- output --
            "<style type=\"text/css\">\n" +
            "    .selector {\n" +
            "        font-size: 12px;\n" +
            "    }\n" +
            "</style>");
	}


	@Test
	@DisplayName("Attribute Wrap alignment with spaces and tabs - (wrap_attributes = \"\"force-aligned\"\", indent_with_tabs = \"true\")")
	void Attribute_Wrap_alignment_with_spaces_and_tabs_wrap_attributes_force_aligned_indent_with_tabs_true_() {
		opts.wrap_attributes = WrapAttributes.forceAligned;
		opts.indent_with_tabs = true;
		test_fragment(
            "<div><div a=\"1\" b=\"2\"><div>test</div></div></div>",
            //  -- output --
            "<div>\n" +
            "\t<div a=\"1\"\n" +
            "\t\t b=\"2\">\n" +
            "\t\t<div>test</div>\n" +
            "\t</div>\n" +
            "</div>");
		test_fragment(
            "<input type=\"radio\"\n" +
            "\t   name=\"garage\"\n" +
            "\t   id=\"garage-02\"\n" +
            "\t   class=\"ns-e-togg__radio ns-js-form-binding\"\n" +
            "\t   value=\"02\"\n" +
            "\t   {{#ifCond data.antragsart \"05\"}}\n" +
            "\t   checked=\"checked\"\n" +
            "\t   {{/ifCond}}>");
		test_fragment(
            "<div>\n" +
            "\t<input type=\"radio\"\n" +
            "\t\t   name=\"garage\"\n" +
            "\t\t   id=\"garage-02\"\n" +
            "\t\t   class=\"ns-e-togg__radio ns-js-form-binding\"\n" +
            "\t\t   value=\"02\"\n" +
            "\t\t   {{#ifCond data.antragsart \"05\"}}\n" +
            "\t\t   checked=\"checked\"\n" +
            "\t\t   {{/ifCond}}>\n" +
            "</div>");
		test_fragment(
            "---\n" +
            "layout: mainLayout.html\n" +
            "page: default.html\n" +
            "---\n" +
            "\n" +
            "<div>\n" +
            "\t{{> componentXYZ my.data.key}}\n" +
            "\t{{> componentABC my.other.data.key}}\n" +
            "\t<span>Hello World</span>\n" +
            "\t<p>Your paragraph</p>\n" +
            "</div>");
	}

	@Test
	@DisplayName("Attribute Wrap alignment with spaces and tabs - (wrap_attributes = \"\"force\"\", indent_with_tabs = \"true\")")
	void Attribute_Wrap_alignment_with_spaces_and_tabs_wrap_attributes_force_indent_with_tabs_true_() {
		opts.wrap_attributes = WrapAttributes.force;
		opts.indent_with_tabs = true;
		test_fragment(
            "<div><div a=\"1\" b=\"2\"><div>test</div></div></div>",
            //  -- output --
            "<div>\n" +
            "\t<div a=\"1\"\n" +
            "\t\tb=\"2\">\n" +
            "\t\t<div>test</div>\n" +
            "\t</div>\n" +
            "</div>");
		test_fragment(
            "<input type=\"radio\"\n" +
            "\tname=\"garage\"\n" +
            "\tid=\"garage-02\"\n" +
            "\tclass=\"ns-e-togg__radio ns-js-form-binding\"\n" +
            "\tvalue=\"02\"\n" +
            "\t{{#ifCond data.antragsart \"05\"}}\n" +
            "\tchecked=\"checked\"\n" +
            "\t{{/ifCond}}>");
		test_fragment(
            "<div>\n" +
            "\t<input type=\"radio\"\n" +
            "\t\tname=\"garage\"\n" +
            "\t\tid=\"garage-02\"\n" +
            "\t\tclass=\"ns-e-togg__radio ns-js-form-binding\"\n" +
            "\t\tvalue=\"02\"\n" +
            "\t\t{{#ifCond data.antragsart \"05\"}}\n" +
            "\t\tchecked=\"checked\"\n" +
            "\t\t{{/ifCond}}>\n" +
            "</div>");
		test_fragment(
            "---\n" +
            "layout: mainLayout.html\n" +
            "page: default.html\n" +
            "---\n" +
            "\n" +
            "<div>\n" +
            "\t{{> componentXYZ my.data.key}}\n" +
            "\t{{> componentABC my.other.data.key}}\n" +
            "\t<span>Hello World</span>\n" +
            "\t<p>Your paragraph</p>\n" +
            "</div>");
	}


	@Test
	@DisplayName("Attribute Wrap de-indent - (wrap_attributes = \"\"force-aligned\"\", indent_with_tabs = \"false\")")
	void Attribute_Wrap_de_indent_wrap_attributes_force_aligned_indent_with_tabs_false_() {
		opts.wrap_attributes = WrapAttributes.forceAligned;
		opts.indent_with_tabs = false;
		bth(
            "<div a=\"1\" b=\"2\"><div>test</div></div>",
            //  -- output --
            "<div a=\"1\"\n" +
            "     b=\"2\">\n" +
            "    <div>test</div>\n" +
            "</div>");
		bth(
            "<p>\n" +
            "    <a href=\"/test/\" target=\"_blank\"><img src=\"test.jpg\" /></a><a href=\"/test/\" target=\"_blank\"><img src=\"test.jpg\" /></a>\n" +
            "</p>",
            //  -- output --
            "<p>\n" +
            "    <a href=\"/test/\"\n" +
            "       target=\"_blank\"><img src=\"test.jpg\" /></a><a href=\"/test/\"\n" +
            "       target=\"_blank\"><img src=\"test.jpg\" /></a>\n" +
            "</p>");
		bth(
            "<p>\n" +
            "    <span data-not-a-href=\"/test/\" data-totally-not-a-target=\"_blank\"><img src=\"test.jpg\" /></span><span data-not-a-href=\"/test/\" data-totally-not-a-target=\"_blank\"><img src=\"test.jpg\" /></span>\n" +
            "</p>",
            //  -- output --
            "<p>\n" +
            "    <span data-not-a-href=\"/test/\"\n" +
            "          data-totally-not-a-target=\"_blank\"><img src=\"test.jpg\" /></span><span data-not-a-href=\"/test/\"\n" +
            "          data-totally-not-a-target=\"_blank\"><img src=\"test.jpg\" /></span>\n" +
            "</p>");
	}


	@Test
	@DisplayName("Issue #1403 -- no extra newlines in force-aligned wrap_attributes - (wrap_attributes = \"\"force-aligned\"\")")
	void Issue_1403_no_extra_newlines_in_force_aligned_wrap_attributes_wrap_attributes_force_aligned_() {
		opts.wrap_attributes = WrapAttributes.forceAligned;
		test_fragment(
            "<button class=\"btn btn-primary\" ng-click=\"shipment.editSendDate = false;sampleTracking.updateShipmentDates({shipment_id: shipment.shipment_id, sent_timestamp: shipment.sending_date})\" type=\"button\">Save</button>",
            //  -- output --
            "<button class=\"btn btn-primary\"\n" +
            "        ng-click=\"shipment.editSendDate = false;sampleTracking.updateShipmentDates({shipment_id: shipment.shipment_id, sent_timestamp: shipment.sending_date})\"\n" +
            "        type=\"button\">Save</button>");
	}


	@Test
	@DisplayName("unformatted_content_delimiter ^^")
	void unformatted_content_delimiter_() {
		opts.wrap_line_length = 80;
		opts.unformatted_content_delimiter = "^^";
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 ^^09 0010 0011 0012 0013 0014 0015 ^^16 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008\n" +
            "    ^^09 0010 0011 0012 0013 0014 0015 ^^16 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   ^^10   0011   0012   0013   0014   0015   0016   0^^7   0018   0019   0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009\n" +
            "    ^^10   0011   0012   0013   0014   0015   0016   0^^7 0018 0019 0020</span>");
		test_fragment(
            "<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0^^0   0011   0012   0013   0014   0015   0016   0^^7   0018   0019   0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0^^0 0011 0012 0013 0014\n" +
            "    0015 0016 0^^7 0018 0019 0020</span>");
	}


	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force\"\")")
	void Attribute_Wrap_wrap_attributes_force_() {
		opts.wrap_attributes = WrapAttributes.force;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment("<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "    attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "    heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\"\n" +
            "    attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "    attr0\n" +
            "    attr1=12345\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "    heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12\n" +
            "    attr2=\"bar\" />");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "    rel=\"stylesheet\"\n" +
            "    type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force\"\", wrap_line_length = \"80\")")
	void Attribute_Wrap_wrap_attributes_force_wrap_line_length_80_() {
		opts.wrap_attributes = WrapAttributes.force;
		opts.wrap_line_length = 80;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013\n" +
            "    0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010\n" +
            "    <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013\n" +
            "    0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic\n" +
            "    <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment(
            "<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>",
            //  -- output --
            "<p>Our forms for collecting address-related information follow a standard\n" +
            "    design. Specific input elements willl vary according to the form’s audience\n" +
            "    and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "    attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "    heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\"\n" +
            "    attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "    attr0\n" +
            "    attr1=12345\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "    heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12\n" +
            "    attr2=\"bar\" />");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link\n" +
            "    href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "    rel=\"stylesheet\"\n" +
            "    type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force\"\", wrap_attributes_indent_size = \"8\")")
	void Attribute_Wrap_wrap_attributes_force_wrap_attributes_indent_size_8_() {
		opts.wrap_attributes = WrapAttributes.force;
		opts.wrap_attributes_indent_size = 8;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment("<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div attr0\n" +
            "        attr1=\"123\"\n" +
            "        data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "        attr0\n" +
            "        attr1=\"123\"\n" +
            "        data-attr2=\"hello    t here\"\n" +
            "        heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img attr0\n" +
            "        attr1=\"123\"\n" +
            "        data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\"\n" +
            "        attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "        attr0\n" +
            "        attr1=12345\n" +
            "        data-attr2=\"hello    t here\"\n" +
            "        heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12\n" +
            "        attr2=\"bar\" />");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "        rel=\"stylesheet\"\n" +
            "        type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"auto\"\", wrap_line_length = \"80\", wrap_attributes_indent_size = \"0\")")
	void Attribute_Wrap_wrap_attributes_auto_wrap_line_length_80_wrap_attributes_indent_size_0_() {
		opts.wrap_attributes = WrapAttributes.auto;
		opts.wrap_line_length = 80;
		opts.wrap_attributes_indent_size = 0;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013\n" +
            "    0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010\n" +
            "    <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013\n" +
            "    0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic\n" +
            "    <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment(
            "<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>",
            //  -- output --
            "<p>Our forms for collecting address-related information follow a standard\n" +
            "    design. Specific input elements willl vary according to the form’s audience\n" +
            "    and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment("<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\"\n" +
            "data-attr2=\"hello    t here\"\n" +
            "heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment("<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>", "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\" attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=12345\n" +
            "data-attr2=\"hello    t here\"\n" +
            "heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12 attr2=\"bar\" />");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link\n" +
            "href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "rel=\"stylesheet\" type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"auto\"\", wrap_line_length = \"80\", wrap_attributes_indent_size = \"4\")")
	void Attribute_Wrap_wrap_attributes_auto_wrap_line_length_80_wrap_attributes_indent_size_4_() {
		opts.wrap_attributes = WrapAttributes.auto;
		opts.wrap_line_length = 80;
		opts.wrap_attributes_indent_size = 4;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013\n" +
            "    0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010\n" +
            "    <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013\n" +
            "    0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic\n" +
            "    <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment(
            "<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>",
            //  -- output --
            "<p>Our forms for collecting address-related information follow a standard\n" +
            "    design. Specific input elements willl vary according to the form’s audience\n" +
            "    and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment("<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "    heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment("<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>", "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\" attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=12345\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "    heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12 attr2=\"bar\" />");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link\n" +
            "    href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "    rel=\"stylesheet\" type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"auto\"\", wrap_line_length = \"0\")")
	void Attribute_Wrap_wrap_attributes_auto_wrap_line_length_0_() {
		opts.wrap_attributes = WrapAttributes.auto;
		opts.wrap_line_length = 0;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment("<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment("<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment("<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment("<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>", "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\" attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment("<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>", "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=12345 data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12 attr2=\"bar\" />");
		bth("<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force-aligned\"\")")
	void Attribute_Wrap_wrap_attributes_force_aligned_() {
		opts.wrap_attributes = WrapAttributes.forceAligned;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment("<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div attr0\n" +
            "     attr1=\"123\"\n" +
            "     data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "     attr0\n" +
            "     attr1=\"123\"\n" +
            "     data-attr2=\"hello    t here\"\n" +
            "     heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img attr0\n" +
            "     attr1=\"123\"\n" +
            "     data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\"\n" +
            "      attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "     attr0\n" +
            "     attr1=12345\n" +
            "     data-attr2=\"hello    t here\"\n" +
            "     heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12\n" +
            "      attr2=\"bar\" />");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "      rel=\"stylesheet\"\n" +
            "      type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force-aligned\"\", wrap_line_length = \"80\")")
	void Attribute_Wrap_wrap_attributes_force_aligned_wrap_line_length_80_() {
		opts.wrap_attributes = WrapAttributes.forceAligned;
		opts.wrap_line_length = 80;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013\n" +
            "    0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010\n" +
            "    <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013\n" +
            "    0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic\n" +
            "    <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment(
            "<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>",
            //  -- output --
            "<p>Our forms for collecting address-related information follow a standard\n" +
            "    design. Specific input elements willl vary according to the form’s audience\n" +
            "    and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div attr0\n" +
            "     attr1=\"123\"\n" +
            "     data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "     attr0\n" +
            "     attr1=\"123\"\n" +
            "     data-attr2=\"hello    t here\"\n" +
            "     heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img attr0\n" +
            "     attr1=\"123\"\n" +
            "     data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\"\n" +
            "      attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "     attr0\n" +
            "     attr1=12345\n" +
            "     data-attr2=\"hello    t here\"\n" +
            "     heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12\n" +
            "      attr2=\"bar\" />");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "      rel=\"stylesheet\"\n" +
            "      type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"aligned-multiple\"\", wrap_line_length = \"80\")")
	void Attribute_Wrap_wrap_attributes_aligned_multiple_wrap_line_length_80_() {
		opts.wrap_attributes = WrapAttributes.alignedMultiple;
		opts.wrap_line_length = 80;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013\n" +
            "    0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010\n" +
            "    <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013\n" +
            "    0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic\n" +
            "    <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment(
            "<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>",
            //  -- output --
            "<p>Our forms for collecting address-related information follow a standard\n" +
            "    design. Specific input elements willl vary according to the form’s audience\n" +
            "    and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment("<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\"\n" +
            "     data-attr2=\"hello    t here\"\n" +
            "     heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment("<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>", "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\" attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=12345\n" +
            "     data-attr2=\"hello    t here\"\n" +
            "     heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12 attr2=\"bar\" />");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "      rel=\"stylesheet\" type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"aligned-multiple\"\")")
	void Attribute_Wrap_wrap_attributes_aligned_multiple_() {
		opts.wrap_attributes = WrapAttributes.alignedMultiple;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment("<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment("<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment("<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment("<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>", "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\" attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment("<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>", "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=12345 data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12 attr2=\"bar\" />");
		bth("<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force-aligned\"\", wrap_attributes_indent_size = \"8\")")
	void Attribute_Wrap_wrap_attributes_force_aligned_wrap_attributes_indent_size_8_() {
		opts.wrap_attributes = WrapAttributes.forceAligned;
		opts.wrap_attributes_indent_size = 8;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment("<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div attr0\n" +
            "     attr1=\"123\"\n" +
            "     data-attr2=\"hello    t here\">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "     attr0\n" +
            "     attr1=\"123\"\n" +
            "     data-attr2=\"hello    t here\"\n" +
            "     heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img attr0\n" +
            "     attr1=\"123\"\n" +
            "     data-attr2=\"hello    t here\" />");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=\"foo\"\n" +
            "      attr2=\"bar\" />");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "     attr0\n" +
            "     attr1=12345\n" +
            "     data-attr2=\"hello    t here\"\n" +
            "     heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root attr1=foo12\n" +
            "      attr2=\"bar\" />");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "      rel=\"stylesheet\"\n" +
            "      type=\"text/css\">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force-expand-multiline\"\", wrap_attributes_indent_size = \"4\")")
	void Attribute_Wrap_wrap_attributes_force_expand_multiline_wrap_attributes_indent_size_4_() {
		opts.wrap_attributes = WrapAttributes.forceExpandMultiline;
		opts.wrap_attributes_indent_size = 4;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment("<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div\n" +
            "    attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\"\n" +
            ">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "    lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "    attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "    heymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img\n" +
            "    attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "/>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "    attr1=\"foo\"\n" +
            "    attr2=\"bar\"\n" +
            "/>");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "    lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "    attr0\n" +
            "    attr1=12345\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "    heymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "    attr1=foo12\n" +
            "    attr2=\"bar\"\n" +
            "/>");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link\n" +
            "    href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "    rel=\"stylesheet\"\n" +
            "    type=\"text/css\"\n" +
            ">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force-expand-multiline\"\", wrap_attributes_indent_size = \"4\", wrap_line_length = \"80\")")
	void Attribute_Wrap_wrap_attributes_force_expand_multiline_wrap_attributes_indent_size_4_wrap_line_length_80_() {
		opts.wrap_attributes = WrapAttributes.forceExpandMultiline;
		opts.wrap_attributes_indent_size = 4;
		opts.wrap_line_length = 80;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013\n" +
            "    0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010\n" +
            "    <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013\n" +
            "    0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic\n" +
            "    <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment(
            "<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>",
            //  -- output --
            "<p>Our forms for collecting address-related information follow a standard\n" +
            "    design. Specific input elements willl vary according to the form’s audience\n" +
            "    and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div\n" +
            "    attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\"\n" +
            ">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "    lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "    attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "    heymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img\n" +
            "    attr0\n" +
            "    attr1=\"123\"\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "/>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "    attr1=\"foo\"\n" +
            "    attr2=\"bar\"\n" +
            "/>");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "    lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "    attr0\n" +
            "    attr1=12345\n" +
            "    data-attr2=\"hello    t here\"\n" +
            "    heymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "    attr1=foo12\n" +
            "    attr2=\"bar\"\n" +
            "/>");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link\n" +
            "    href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "    rel=\"stylesheet\"\n" +
            "    type=\"text/css\"\n" +
            ">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force-expand-multiline\"\", wrap_attributes_indent_size = \"8\")")
	void Attribute_Wrap_wrap_attributes_force_expand_multiline_wrap_attributes_indent_size_8_() {
		opts.wrap_attributes = WrapAttributes.forceExpandMultiline;
		opts.wrap_attributes_indent_size = 8;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment("<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div\n" +
            "        attr0\n" +
            "        attr1=\"123\"\n" +
            "        data-attr2=\"hello    t here\"\n" +
            ">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "        lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "        attr0\n" +
            "        attr1=\"123\"\n" +
            "        data-attr2=\"hello    t here\"\n" +
            "        heymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img\n" +
            "        attr0\n" +
            "        attr1=\"123\"\n" +
            "        data-attr2=\"hello    t here\"\n" +
            "/>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "        attr1=\"foo\"\n" +
            "        attr2=\"bar\"\n" +
            "/>");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "        lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "        attr0\n" +
            "        attr1=12345\n" +
            "        data-attr2=\"hello    t here\"\n" +
            "        heymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "        attr1=foo12\n" +
            "        attr2=\"bar\"\n" +
            "/>");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link\n" +
            "        href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "        rel=\"stylesheet\"\n" +
            "        type=\"text/css\"\n" +
            ">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force-expand-multiline\"\", wrap_attributes_indent_size = \"4\", indent_with_tabs = \"true\")")
	void Attribute_Wrap_wrap_attributes_force_expand_multiline_wrap_attributes_indent_size_4_indent_with_tabs_true_() {
		opts.wrap_attributes = WrapAttributes.forceExpandMultiline;
		opts.wrap_attributes_indent_size = 4;
		opts.indent_with_tabs = true;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment("<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div\n" +
            "\tattr0\n" +
            "\tattr1=\"123\"\n" +
            "\tdata-attr2=\"hello    t here\"\n" +
            ">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "\tlookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "\tattr0\n" +
            "\tattr1=\"123\"\n" +
            "\tdata-attr2=\"hello    t here\"\n" +
            "\theymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img\n" +
            "\tattr0\n" +
            "\tattr1=\"123\"\n" +
            "\tdata-attr2=\"hello    t here\"\n" +
            "/>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "\tattr1=\"foo\"\n" +
            "\tattr2=\"bar\"\n" +
            "/>");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "\tlookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "\tattr0\n" +
            "\tattr1=12345\n" +
            "\tdata-attr2=\"hello    t here\"\n" +
            "\theymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "\tattr1=foo12\n" +
            "\tattr2=\"bar\"\n" +
            "/>");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link\n" +
            "\thref=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "\trel=\"stylesheet\"\n" +
            "\ttype=\"text/css\"\n" +
            ">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force-expand-multiline\"\", wrap_attributes_indent_size = \"7\", indent_with_tabs = \"true\")")
	void Attribute_Wrap_wrap_attributes_force_expand_multiline_wrap_attributes_indent_size_7_indent_with_tabs_true_() {
		opts.wrap_attributes = WrapAttributes.forceExpandMultiline;
		opts.wrap_attributes_indent_size = 7;
		opts.indent_with_tabs = true;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment("<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>", "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment("<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div\n" +
            "\t   attr0\n" +
            "\t   attr1=\"123\"\n" +
            "\t   data-attr2=\"hello    t here\"\n" +
            ">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "\t   lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "\t   attr0\n" +
            "\t   attr1=\"123\"\n" +
            "\t   data-attr2=\"hello    t here\"\n" +
            "\t   heymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img\n" +
            "\t   attr0\n" +
            "\t   attr1=\"123\"\n" +
            "\t   data-attr2=\"hello    t here\"\n" +
            "/>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "\t   attr1=\"foo\"\n" +
            "\t   attr2=\"bar\"\n" +
            "/>");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "\t   lookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "\t   attr0\n" +
            "\t   attr1=12345\n" +
            "\t   data-attr2=\"hello    t here\"\n" +
            "\t   heymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "\t   attr1=foo12\n" +
            "\t   attr2=\"bar\"\n" +
            "/>");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link\n" +
            "\t   href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "\t   rel=\"stylesheet\"\n" +
            "\t   type=\"text/css\"\n" +
            ">");
	}

	@Test
	@DisplayName("Attribute Wrap - (wrap_attributes = \"\"force-expand-multiline\"\", wrap_line_length = \"80\", indent_with_tabs = \"true\")")
	void Attribute_Wrap_wrap_attributes_force_expand_multiline_wrap_line_length_80_indent_with_tabs_true_() {
		opts.wrap_attributes = WrapAttributes.forceExpandMultiline;
		opts.wrap_line_length = 80;
		opts.indent_with_tabs = true;
		bth("<div  >This is some text</div>", "<div>This is some text</div>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "\t0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0   0001   0002   0003   0004   0005   0006   0007   0008   0009   0010   0011   0012   0013   0014   0015   0016   0017   0018   0019   0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "\t0015 0016 0017 0018 0019 0020</span>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\t0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "\t0015 0016 0017 0018 0019 0020</span>");
		
        // issue #869
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013\n" +
            "\t0014&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1324
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009  0010 <span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010\n" +
            "\t<span>&nbsp;</span>&nbsp;0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic 0013 0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic 0013\n" +
            "\t0014" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // issue #1496 and #1324 - respect unicode non-breaking space
        test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011  unic <span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 unic\n" +
            "\t<span>" + unicode_char(160) + "</span>" + unicode_char(160) + "0015 0016 0017 0018 0019 0020</span>");
		
        // Issue 1222 -- P tags are formatting correctly
        test_fragment(
            "<p>Our forms for collecting address-related information follow a standard design. Specific input elements willl vary according to the form’s audience and purpose.</p>",
            //  -- output --
            "<p>Our forms for collecting address-related information follow a standard\n" +
            "\tdesign. Specific input elements willl vary according to the form’s audience\n" +
            "\tand purpose.</p>");
		bth("<div attr=\"123\"  >This is some text</div>", "<div attr=\"123\">This is some text</div>");
		test_fragment(
            "<div attr0 attr1=\"123\" data-attr2=\"hello    t here\">This is some text</div>",
            //  -- output --
            "<div\n" +
            "\tattr0\n" +
            "\tattr1=\"123\"\n" +
            "\tdata-attr2=\"hello    t here\"\n" +
            ">This is some text</div>");
		test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0=\"true\" attr0 attr1=\"123\" data-attr2=\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "\tlookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "\tattr0\n" +
            "\tattr1=\"123\"\n" +
            "\tdata-attr2=\"hello    t here\"\n" +
            "\theymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<img attr0 attr1=\"123\" data-attr2=\"hello    t here\"/>",
            //  -- output --
            "<img\n" +
            "\tattr0\n" +
            "\tattr1=\"123\"\n" +
            "\tdata-attr2=\"hello    t here\"\n" +
            "/>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1=\"foo\" attr2=\"bar\"/>",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "\tattr1=\"foo\"\n" +
            "\tattr2=\"bar\"\n" +
            "/>");
		
        // Issue #1094 - Beautify correctly without quotes and with extra spaces
        test_fragment(
            "<div lookatthissuperduperlongattributenamewhoahcrazy0 =    \"true\" attr0 attr1=  12345 data-attr2   =\"hello    t here\" heymanimreallylongtoowhocomesupwiththesenames=\"false\">This is text</div>",
            //  -- output --
            "<div\n" +
            "\tlookatthissuperduperlongattributenamewhoahcrazy0=\"true\"\n" +
            "\tattr0\n" +
            "\tattr1=12345\n" +
            "\tdata-attr2=\"hello    t here\"\n" +
            "\theymanimreallylongtoowhocomesupwiththesenames=\"false\"\n" +
            ">This is text</div>");
		test_fragment(
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><root attr1   =   foo12   attr2  =\"bar\"    />",
            //  -- output --
            "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
            "<root\n" +
            "\tattr1=foo12\n" +
            "\tattr2=\"bar\"\n" +
            "/>");
		bth(
            "<link href=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\" rel=\"stylesheet\" type=\"text/css\">",
            //  -- output --
            "<link\n" +
            "\thref=\"//fonts.googleapis.com/css?family=Open+Sans:300italic,400italic,600italic,700italic,400,600,700,300&amp;subset=latin\"\n" +
            "\trel=\"stylesheet\"\n" +
            "\ttype=\"text/css\"\n" +
            ">");
	}


	@Test
	@DisplayName("Issue #1335 -- <button> Bug with force-expand-multiline formatting")
	void Issue_1335_button_Bug_with_force_expand_multiline_formatting() {
		opts.wrap_attributes = WrapAttributes.forceExpandMultiline;
		test_fragment(
            "<button\n" +
            "    class=\"my-class\"\n" +
            "    id=\"id1\"\n" +
            ">\n" +
            "    Button 1\n" +
            "</button>\n" +
            "\n" +
            "<button\n" +
            "    class=\"my-class\"\n" +
            "    id=\"id2\"\n" +
            ">\n" +
            "    Button 2\n" +
            "</button>");
		bth(
            "<button>\n" +
            "    <span>foo</span>\n" +
            "<p>bar</p>\n" +
            "</button>",
            //  -- output --
            "<button>\n" +
            "    <span>foo</span>\n" +
            "    <p>bar</p>\n" +
            "</button>");
	}


	@Test
	@DisplayName("Issue #1125 -- Add preserve and preserve_aligned attribute options - (wrap_attributes = \"\"preserve-aligned\"\")")
	void Issue_1125_Add_preserve_and_preserve_aligned_attribute_options_wrap_attributes_preserve_aligned_() {
		opts.wrap_attributes = WrapAttributes.preserveAligned;
		bth(
            "<input type=\"text\"     class=\"form-control\"  autocomplete=\"off\"\n" +
            "[(ngModel)]=\"myValue\"          [disabled]=\"isDisabled\" [placeholder]=\"placeholder\"\n" +
            "[typeahead]=\"suggestionsSource\" [typeaheadOptionField]=\"suggestionValueField\" [typeaheadItemTemplate]=\"suggestionTemplate\"   [typeaheadWaitMs]=\"300\"\n" +
            "(typeaheadOnSelect)=\"onSuggestionSelected($event)\" />",
            //  -- output --
            "<input type=\"text\" class=\"form-control\" autocomplete=\"off\"\n" +
            "       [(ngModel)]=\"myValue\" [disabled]=\"isDisabled\" [placeholder]=\"placeholder\"\n" +
            "       [typeahead]=\"suggestionsSource\" [typeaheadOptionField]=\"suggestionValueField\" [typeaheadItemTemplate]=\"suggestionTemplate\" [typeaheadWaitMs]=\"300\"\n" +
            "       (typeaheadOnSelect)=\"onSuggestionSelected($event)\" />");
	}

	@Test
	@DisplayName("Issue #1125 -- Add preserve and preserve_aligned attribute options - (wrap_attributes = \"\"preserve\"\")")
	void Issue_1125_Add_preserve_and_preserve_aligned_attribute_options_wrap_attributes_preserve_() {
		opts.wrap_attributes = WrapAttributes.preserve;
		bth(
            "<input type=\"text\"     class=\"form-control\"  autocomplete=\"off\"\n" +
            "[(ngModel)]=\"myValue\"          [disabled]=\"isDisabled\" [placeholder]=\"placeholder\"\n" +
            "[typeahead]=\"suggestionsSource\" [typeaheadOptionField]=\"suggestionValueField\" [typeaheadItemTemplate]=\"suggestionTemplate\"   [typeaheadWaitMs]=\"300\"\n" +
            "(typeaheadOnSelect)=\"onSuggestionSelected($event)\" />",
            //  -- output --
            "<input type=\"text\" class=\"form-control\" autocomplete=\"off\"\n" +
            "    [(ngModel)]=\"myValue\" [disabled]=\"isDisabled\" [placeholder]=\"placeholder\"\n" +
            "    [typeahead]=\"suggestionsSource\" [typeaheadOptionField]=\"suggestionValueField\" [typeaheadItemTemplate]=\"suggestionTemplate\" [typeaheadWaitMs]=\"300\"\n" +
            "    (typeaheadOnSelect)=\"onSuggestionSelected($event)\" />");
	}


	@Test
	@DisplayName("Test wrap_attributes_min_attrs with force/force-xx options - (wrap_attributes = \"\"force\"\", wrap_attributes_min_attrs = \"4\")")
	void Test_wrap_attributes_min_attrs_with_force_force_xx_options_wrap_attributes_force_wrap_attributes_min_attrs_4_() {
		opts.wrap_attributes = WrapAttributes.force;
		opts.wrap_attributes_min_attrs = 4;
		bth(
            "<input type=\"four attributes should wrap\"     class=\"form-control\"  autocomplete=\"off\"\n" +
            "[(ngModel)]=\"myValue\" />",
            //  -- output --
            "<input type=\"four attributes should wrap\"\n" +
            "    class=\"form-control\"\n" +
            "    autocomplete=\"off\"\n" +
            "    [(ngModel)]=\"myValue\" />");
		bth(
            "<input type=\"three attributes should not wrap\"    autocomplete=\"off\"\n" +
            "[(ngModel)]=\"myValue\" />",
            //  -- output --
            "<input type=\"three attributes should not wrap\" autocomplete=\"off\" [(ngModel)]=\"myValue\" />");
		bth(
            "<cmpnt v-bind:xx=\"four attributes with valueless attribute should wrap\"  @someevent=\"dosomething\"  someprop\n" +
            "class=\"xx-button\">\n" +
            "<div class=\"alert alert-info\" style=\"margin-left: 1px;\" role=\"alert\">lorem ipsum</div>\n" +
            "</cmpnt>",
            //  -- output --
            "<cmpnt v-bind:xx=\"four attributes with valueless attribute should wrap\"\n" +
            "    @someevent=\"dosomething\"\n" +
            "    someprop\n" +
            "    class=\"xx-button\">\n" +
            "    <div class=\"alert alert-info\" style=\"margin-left: 1px;\" role=\"alert\">lorem ipsum</div>\n" +
            "</cmpnt>");
	}

	@Test
	@DisplayName("Test wrap_attributes_min_attrs with force/force-xx options - (wrap_attributes = \"\"force-aligned\"\", wrap_attributes_min_attrs = \"4\")")
	void Test_wrap_attributes_min_attrs_with_force_force_xx_options_wrap_attributes_force_aligned_wrap_attributes_min_attrs_4_() {
		opts.wrap_attributes = WrapAttributes.forceAligned;
		opts.wrap_attributes_min_attrs = 4;
		bth(
            "<input type=\"four attributes should wrap\"     class=\"form-control\"  autocomplete=\"off\"\n" +
            "[(ngModel)]=\"myValue\" />",
            //  -- output --
            "<input type=\"four attributes should wrap\"\n" +
            "       class=\"form-control\"\n" +
            "       autocomplete=\"off\"\n" +
            "       [(ngModel)]=\"myValue\" />");
		bth(
            "<input type=\"three attributes should not wrap\"    autocomplete=\"off\"\n" +
            "[(ngModel)]=\"myValue\" />",
            //  -- output --
            "<input type=\"three attributes should not wrap\" autocomplete=\"off\" [(ngModel)]=\"myValue\" />");
		bth(
            "<cmpnt v-bind:xx=\"four attributes with valueless attribute should wrap\"  @someevent=\"dosomething\"  someprop\n" +
            "class=\"xx-button\">\n" +
            "<div class=\"alert alert-info\" style=\"margin-left: 1px;\" role=\"alert\">lorem ipsum</div>\n" +
            "</cmpnt>",
            //  -- output --
            "<cmpnt v-bind:xx=\"four attributes with valueless attribute should wrap\"\n" +
            "       @someevent=\"dosomething\"\n" +
            "       someprop\n" +
            "       class=\"xx-button\">\n" +
            "    <div class=\"alert alert-info\" style=\"margin-left: 1px;\" role=\"alert\">lorem ipsum</div>\n" +
            "</cmpnt>");
	}

	@Test
	@DisplayName("Test wrap_attributes_min_attrs with force/force-xx options - (wrap_attributes = \"\"force-expand-multiline\"\", wrap_attributes_min_attrs = \"4\")")
	void Test_wrap_attributes_min_attrs_with_force_force_xx_options_wrap_attributes_force_expand_multiline_wrap_attributes_min_attrs_4_() {
		opts.wrap_attributes = WrapAttributes.forceExpandMultiline;
		opts.wrap_attributes_min_attrs = 4;
		bth(
            "<input type=\"four attributes should wrap\"     class=\"form-control\"  autocomplete=\"off\"\n" +
            "[(ngModel)]=\"myValue\" />",
            //  -- output --
            "<input\n" +
            "    type=\"four attributes should wrap\"\n" +
            "    class=\"form-control\"\n" +
            "    autocomplete=\"off\"\n" +
            "    [(ngModel)]=\"myValue\"\n" +
            "/>");
		bth(
            "<input type=\"three attributes should not wrap\"    autocomplete=\"off\"\n" +
            "[(ngModel)]=\"myValue\" />",
            //  -- output --
            "<input type=\"three attributes should not wrap\" autocomplete=\"off\" [(ngModel)]=\"myValue\" />");
		bth(
            "<cmpnt v-bind:xx=\"four attributes with valueless attribute should wrap\"  @someevent=\"dosomething\"  someprop\n" +
            "class=\"xx-button\">\n" +
            "<div class=\"alert alert-info\" style=\"margin-left: 1px;\" role=\"alert\">lorem ipsum</div>\n" +
            "</cmpnt>",
            //  -- output --
            "<cmpnt\n" +
            "    v-bind:xx=\"four attributes with valueless attribute should wrap\"\n" +
            "    @someevent=\"dosomething\"\n" +
            "    someprop\n" +
            "    class=\"xx-button\"\n" +
            ">\n" +
            "    <div class=\"alert alert-info\" style=\"margin-left: 1px;\" role=\"alert\">lorem ipsum</div>\n" +
            "</cmpnt>");
	}


	@Test
	@DisplayName("Test wrap_attributes_min_attrs = 1 with force/force-xx options - (wrap_attributes = \"\"force\"\", wrap_attributes_min_attrs = \"1\")")
	void Test_wrap_attributes_min_attrs_1_with_force_force_xx_options_wrap_attributes_force_wrap_attributes_min_attrs_1_() {
		opts.wrap_attributes = WrapAttributes.force;
		opts.wrap_attributes_min_attrs = 1;
		bth("<input type=\"one attribute\"/>", "<input type=\"one attribute\" />");
	}

	@Test
	@DisplayName("Test wrap_attributes_min_attrs = 1 with force/force-xx options - (wrap_attributes = \"\"force-aligned\"\", wrap_attributes_min_attrs = \"1\")")
	void Test_wrap_attributes_min_attrs_1_with_force_force_xx_options_wrap_attributes_force_aligned_wrap_attributes_min_attrs_1_() {
		opts.wrap_attributes = WrapAttributes.forceAligned;
		opts.wrap_attributes_min_attrs = 1;
		bth("<input type=\"one attribute\"/>", "<input type=\"one attribute\" />");
	}

	@Test
	@DisplayName("Test wrap_attributes_min_attrs = 1 with force/force-xx options - (wrap_attributes = \"\"force-expand-multiline\"\", wrap_attributes_min_attrs = \"1\")")
	void Test_wrap_attributes_min_attrs_1_with_force_force_xx_options_wrap_attributes_force_expand_multiline_wrap_attributes_min_attrs_1_() {
		opts.wrap_attributes = WrapAttributes.forceExpandMultiline;
		opts.wrap_attributes_min_attrs = 1;
		bth(
            "<input type=\"one attribute\"/>",
            //  -- output --
            "<input\n" +
            "    type=\"one attribute\"\n" +
            "/>");
	}


	@Test
	@DisplayName("Handlebars Indenting Off")
	void Handlebars_Indenting_Off() {
		opts.indent_handlebars = false;
		test_fragment(
            "{{#if 0}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 0}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}");
		test_fragment(
            "<div>\n" +
            "{{#each thing}}\n" +
            "    {{name}}\n" +
            "{{/each}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#each thing}}\n" +
            "    {{name}}\n" +
            "    {{/each}}\n" +
            "</div>");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "   {{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. Just treated as text here.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "<div>\n" +
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "    {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "    {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}\n" +
            "</div>");
		bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {{translate \"onText\"}}\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{{translate \"offText\"}}\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "{{translate \"onText\"}}\n" +
            "{{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/if}}");
	}


	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\")")
	void Handlebars_Indenting_On_indent_handlebars_true_() {
		opts.indent_handlebars = true;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{field}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{field}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{field}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{field}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{field}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{field}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{field}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{field}}");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "{{field}}\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{field}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{field}}\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{field}}\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth("{{#if 0}}{{field}}{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}{{field}}{{/if}}",
            //  -- output --
            "{{#if words}}{{field}}{{/if}}");
		bth(
            "{{#if     words}}{{field}}{{/if}}",
            //  -- output --
            "{{#if words}}{{field}}{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "{{field}}\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "{{field}}\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            {{field}}\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            {{field}}\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    {{field}}\n" +
            "    {{else}}\n" +
            "    {{field}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    {{field}}\n" +
            "{{else}}\n" +
            "    {{field}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    {{field}}\n" +
            "    {{else}}\n" +
            "{{field}}\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "{{field}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        {{field}}\n" +
            "    {{else}}\n" +
            "        {{field}}\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    {{field}}\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {{field}}\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{{field}}\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        {{field}}\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    {{field}}\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>{{field}}</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>{{field}}</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>{{field}}</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\" {{else}}class=\"{{class2}}\" {{/if}}>{{field}}</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>{{field}}</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>{{field}}</span>");
		bth("<{{ele}} unformatted=\"{{#if}}{{field}}{{/if}}\">{{field}}</{{ele}}>");
		bth("<div unformatted=\"{{#if}}{{field}}{{/if}}\">{{field}}</div>");
		bth("<div unformatted=\"{{#if  }}    {{field}}{{/if}}\">{{field}}</div>");
		bth("<div class=\"{{#if thingIs \"value\"}}{{field}}{{/if}}\"></div>");
		bth("<div class=\"{{#if thingIs \'value\'}}{{field}}{{/if}}\"></div>");
		bth("<div class=\'{{#if thingIs \"value\"}}{{field}}{{/if}}\'></div>");
		bth("<div class=\'{{#if thingIs \'value\'}}{{field}}{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}

	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\")")
	void Handlebars_Indenting_On_indent_handlebars_true_1() {
		opts.indent_handlebars = true;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth("{{#if 0}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}",
            //  -- output --
            "{{#if words}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}");
		bth(
            "{{#if     words}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}",
            //  -- output --
            "{{#if words}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "    {{else}}\n" +
            "    {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{else}}\n" +
            "    {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "    {{else}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "    {{else}}\n" +
            "        {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\" {{else}}class=\"{{class2}}\" {{/if}}>{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}</span>");
		bth("<{{ele}} unformatted=\"{{#if}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}\">{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}</{{ele}}>");
		bth("<div unformatted=\"{{#if}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}\">{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}</div>");
		bth("<div unformatted=\"{{#if  }}    {{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}\">{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}</div>");
		bth("<div class=\"{{#if thingIs \"value\"}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}\"></div>");
		bth("<div class=\"{{#if thingIs \'value\'}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}\"></div>");
		bth("<div class=\'{{#if thingIs \"value\"}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}\'></div>");
		bth("<div class=\'{{#if thingIs \'value\'}}{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}

	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\")")
	void Handlebars_Indenting_On_indent_handlebars_true_2() {
		opts.indent_handlebars = true;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{! comment}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{! comment}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{! comment}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{! comment}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{! comment}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{! comment}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{! comment}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{! comment}}");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "{{! comment}}\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{! comment}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{! comment}}\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{! comment}}\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth("{{#if 0}}{{! comment}}{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}{{! comment}}{{/if}}",
            //  -- output --
            "{{#if words}}{{! comment}}{{/if}}");
		bth(
            "{{#if     words}}{{! comment}}{{/if}}",
            //  -- output --
            "{{#if words}}{{! comment}}{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "{{! comment}}\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "{{! comment}}\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            {{! comment}}\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            {{! comment}}\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    {{! comment}}\n" +
            "    {{else}}\n" +
            "    {{! comment}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    {{! comment}}\n" +
            "{{else}}\n" +
            "    {{! comment}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    {{! comment}}\n" +
            "    {{else}}\n" +
            "{{! comment}}\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "{{! comment}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        {{! comment}}\n" +
            "    {{else}}\n" +
            "        {{! comment}}\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    {{! comment}}\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {{! comment}}\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{{! comment}}\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        {{! comment}}\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    {{! comment}}\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>{{! comment}}</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>{{! comment}}</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>{{! comment}}</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\" {{else}}class=\"{{class2}}\" {{/if}}>{{! comment}}</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>{{! comment}}</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>{{! comment}}</span>");
		bth("<{{ele}} unformatted=\"{{#if}}{{! comment}}{{/if}}\">{{! comment}}</{{ele}}>");
		bth("<div unformatted=\"{{#if}}{{! comment}}{{/if}}\">{{! comment}}</div>");
		bth("<div unformatted=\"{{#if  }}    {{! comment}}{{/if}}\">{{! comment}}</div>");
		bth("<div class=\"{{#if thingIs \"value\"}}{{! comment}}{{/if}}\"></div>");
		bth("<div class=\"{{#if thingIs \'value\'}}{{! comment}}{{/if}}\"></div>");
		bth("<div class=\'{{#if thingIs \"value\"}}{{! comment}}{{/if}}\'></div>");
		bth("<div class=\'{{#if thingIs \'value\'}}{{! comment}}{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}

	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\")")
	void Handlebars_Indenting_On_indent_handlebars_true_3() {
		opts.indent_handlebars = true;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- comment--}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- comment--}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- comment--}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- comment--}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- comment--}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- comment--}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- comment--}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- comment--}}");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "{{!-- comment--}}\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{!-- comment--}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{!-- comment--}}\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{!-- comment--}}\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth("{{#if 0}}{{!-- comment--}}{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}{{!-- comment--}}{{/if}}",
            //  -- output --
            "{{#if words}}{{!-- comment--}}{{/if}}");
		bth(
            "{{#if     words}}{{!-- comment--}}{{/if}}",
            //  -- output --
            "{{#if words}}{{!-- comment--}}{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "{{!-- comment--}}\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "{{!-- comment--}}\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            {{!-- comment--}}\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            {{!-- comment--}}\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    {{!-- comment--}}\n" +
            "    {{else}}\n" +
            "    {{!-- comment--}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    {{!-- comment--}}\n" +
            "{{else}}\n" +
            "    {{!-- comment--}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    {{!-- comment--}}\n" +
            "    {{else}}\n" +
            "{{!-- comment--}}\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "{{!-- comment--}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        {{!-- comment--}}\n" +
            "    {{else}}\n" +
            "        {{!-- comment--}}\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    {{!-- comment--}}\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {{!-- comment--}}\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{{!-- comment--}}\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        {{!-- comment--}}\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    {{!-- comment--}}\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>{{!-- comment--}}</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>{{!-- comment--}}</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>{{!-- comment--}}</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\" {{else}}class=\"{{class2}}\" {{/if}}>{{!-- comment--}}</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>{{!-- comment--}}</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>{{!-- comment--}}</span>");
		bth("<{{ele}} unformatted=\"{{#if}}{{!-- comment--}}{{/if}}\">{{!-- comment--}}</{{ele}}>");
		bth("<div unformatted=\"{{#if}}{{!-- comment--}}{{/if}}\">{{!-- comment--}}</div>");
		bth("<div unformatted=\"{{#if  }}    {{!-- comment--}}{{/if}}\">{{!-- comment--}}</div>");
		bth("<div class=\"{{#if thingIs \"value\"}}{{!-- comment--}}{{/if}}\"></div>");
		bth("<div class=\"{{#if thingIs \'value\'}}{{!-- comment--}}{{/if}}\"></div>");
		bth("<div class=\'{{#if thingIs \"value\"}}{{!-- comment--}}{{/if}}\'></div>");
		bth("<div class=\'{{#if thingIs \'value\'}}{{!-- comment--}}{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}

	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\")")
	void Handlebars_Indenting_On_indent_handlebars_true_4() {
		opts.indent_handlebars = true;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{{unescaped_variable}}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{{unescaped_variable}}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{{unescaped_variable}}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{{unescaped_variable}}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{{unescaped_variable}}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{{unescaped_variable}}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{{unescaped_variable}}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{{unescaped_variable}}}");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "{{{unescaped_variable}}}\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{{unescaped_variable}}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{{unescaped_variable}}}\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{{unescaped_variable}}}\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth("{{#if 0}}{{{unescaped_variable}}}{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}{{{unescaped_variable}}}{{/if}}",
            //  -- output --
            "{{#if words}}{{{unescaped_variable}}}{{/if}}");
		bth(
            "{{#if     words}}{{{unescaped_variable}}}{{/if}}",
            //  -- output --
            "{{#if words}}{{{unescaped_variable}}}{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "{{{unescaped_variable}}}\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "{{{unescaped_variable}}}\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            {{{unescaped_variable}}}\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            {{{unescaped_variable}}}\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    {{{unescaped_variable}}}\n" +
            "    {{else}}\n" +
            "    {{{unescaped_variable}}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    {{{unescaped_variable}}}\n" +
            "{{else}}\n" +
            "    {{{unescaped_variable}}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    {{{unescaped_variable}}}\n" +
            "    {{else}}\n" +
            "{{{unescaped_variable}}}\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "{{{unescaped_variable}}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        {{{unescaped_variable}}}\n" +
            "    {{else}}\n" +
            "        {{{unescaped_variable}}}\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    {{{unescaped_variable}}}\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {{{unescaped_variable}}}\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{{{unescaped_variable}}}\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        {{{unescaped_variable}}}\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    {{{unescaped_variable}}}\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>{{{unescaped_variable}}}</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>{{{unescaped_variable}}}</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>{{{unescaped_variable}}}</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\" {{else}}class=\"{{class2}}\" {{/if}}>{{{unescaped_variable}}}</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>{{{unescaped_variable}}}</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>{{{unescaped_variable}}}</span>");
		bth("<{{ele}} unformatted=\"{{#if}}{{{unescaped_variable}}}{{/if}}\">{{{unescaped_variable}}}</{{ele}}>");
		bth("<div unformatted=\"{{#if}}{{{unescaped_variable}}}{{/if}}\">{{{unescaped_variable}}}</div>");
		bth("<div unformatted=\"{{#if  }}    {{{unescaped_variable}}}{{/if}}\">{{{unescaped_variable}}}</div>");
		bth("<div class=\"{{#if thingIs \"value\"}}{{{unescaped_variable}}}{{/if}}\"></div>");
		bth("<div class=\"{{#if thingIs \'value\'}}{{{unescaped_variable}}}{{/if}}\"></div>");
		bth("<div class=\'{{#if thingIs \"value\"}}{{{unescaped_variable}}}{{/if}}\'></div>");
		bth("<div class=\'{{#if thingIs \'value\'}}{{{unescaped_variable}}}{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}

	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\")")
	void Handlebars_Indenting_On_indent_handlebars_true_5() {
		opts.indent_handlebars = true;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth("{{#if 0}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}",
            //  -- output --
            "{{#if words}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}");
		bth(
            "{{#if     words}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}",
            //  -- output --
            "{{#if words}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "    {{else}}\n" +
            "    {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{else}}\n" +
            "    {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "    {{else}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "    {{else}}\n" +
            "        {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\" {{else}}class=\"{{class2}}\" {{/if}}>{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}</span>");
		bth("<{{ele}} unformatted=\"{{#if}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}\">{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}</{{ele}}>");
		bth("<div unformatted=\"{{#if}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}\">{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}</div>");
		bth("<div unformatted=\"{{#if  }}    {{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}\">{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}</div>");
		bth("<div class=\"{{#if thingIs \"value\"}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}\"></div>");
		bth("<div class=\"{{#if thingIs \'value\'}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}\"></div>");
		bth("<div class=\'{{#if thingIs \"value\"}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}\'></div>");
		bth("<div class=\'{{#if thingIs \'value\'}}{{Hello \"woRld\"}} {{!-- comment--}} {{heLloWorlD}}{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}

	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\")")
	void Handlebars_Indenting_On_indent_handlebars_true_6() {
		opts.indent_handlebars = true;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth("{{#if 0}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}",
            //  -- output --
            "{{#if words}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}");
		bth(
            "{{#if     words}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}",
            //  -- output --
            "{{#if words}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            {pre{{field1}} {{field2}} {{field3}}post\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            {pre{{field1}} {{field2}} {{field3}}post\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    {pre{{field1}} {{field2}} {{field3}}post\n" +
            "    {{else}}\n" +
            "    {pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    {pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{else}}\n" +
            "    {pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    {pre{{field1}} {{field2}} {{field3}}post\n" +
            "    {{else}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        {pre{{field1}} {{field2}} {{field3}}post\n" +
            "    {{else}}\n" +
            "        {pre{{field1}} {{field2}} {{field3}}post\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    {pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {pre{{field1}} {{field2}} {{field3}}post\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{pre{{field1}} {{field2}} {{field3}}post\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        {pre{{field1}} {{field2}} {{field3}}post\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    {pre{{field1}} {{field2}} {{field3}}post\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>{pre{{field1}} {{field2}} {{field3}}post</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>{pre{{field1}} {{field2}} {{field3}}post</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>{pre{{field1}} {{field2}} {{field3}}post</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\" {{else}}class=\"{{class2}}\" {{/if}}>{pre{{field1}} {{field2}} {{field3}}post</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>{pre{{field1}} {{field2}} {{field3}}post</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>{pre{{field1}} {{field2}} {{field3}}post</span>");
		bth("<{{ele}} unformatted=\"{{#if}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}\">{pre{{field1}} {{field2}} {{field3}}post</{{ele}}>");
		bth("<div unformatted=\"{{#if}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}\">{pre{{field1}} {{field2}} {{field3}}post</div>");
		bth("<div unformatted=\"{{#if  }}    {pre{{field1}} {{field2}} {{field3}}post{{/if}}\">{pre{{field1}} {{field2}} {{field3}}post</div>");
		bth("<div class=\"{{#if thingIs \"value\"}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}\"></div>");
		bth("<div class=\"{{#if thingIs \'value\'}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}\"></div>");
		bth("<div class=\'{{#if thingIs \"value\"}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}\'></div>");
		bth("<div class=\'{{#if thingIs \'value\'}}{pre{{field1}} {{field2}} {{field3}}post{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}

	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\")")
	void Handlebars_Indenting_On_indent_handlebars_true_7() {
		opts.indent_handlebars = true;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth(
            "{{#if 0}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}",
            //  -- output --
            "{{#if words}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}");
		bth(
            "{{#if     words}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}",
            //  -- output --
            "{{#if words}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "    {{else}}\n" +
            "    {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{else}}\n" +
            "    {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "    {{else}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "    {{else}}\n" +
            "        {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\" {{else}}class=\"{{class2}}\" {{/if}}>{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}</span>");
		bth(
            "<{{ele}} unformatted=\"{{#if}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}\">{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}</{{ele}}>");
		bth(
            "<div unformatted=\"{{#if}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}\">{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}</div>");
		bth(
            "<div unformatted=\"{{#if  }}    {{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}\">{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}</div>");
		bth(
            "<div class=\"{{#if thingIs \"value\"}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}\"></div>");
		bth(
            "<div class=\"{{#if thingIs \'value\'}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}\"></div>");
		bth(
            "<div class=\'{{#if thingIs \"value\"}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}\'></div>");
		bth(
            "<div class=\'{{#if thingIs \'value\'}}{{! \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "}}{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}

	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\")")
	void Handlebars_Indenting_On_indent_handlebars_true_8() {
		opts.indent_handlebars = true;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth(
            "{{#if 0}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}",
            //  -- output --
            "{{#if words}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}");
		bth(
            "{{#if     words}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}",
            //  -- output --
            "{{#if words}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "    {{else}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{else}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "    {{else}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "    {{else}}\n" +
            "        {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\" {{else}}class=\"{{class2}}\" {{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}</span>");
		bth(
            "<{{ele}} unformatted=\"{{#if}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}\">{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}</{{ele}}>");
		bth(
            "<div unformatted=\"{{#if}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}\">{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}</div>");
		bth(
            "<div unformatted=\"{{#if  }}    {{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}\">{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}</div>");
		bth(
            "<div class=\"{{#if thingIs \"value\"}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}\"></div>");
		bth(
            "<div class=\"{{#if thingIs \'value\'}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}\"></div>");
		bth(
            "<div class=\'{{#if thingIs \"value\"}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}\'></div>");
		bth(
            "<div class=\'{{#if thingIs \'value\'}}{{!-- \n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            "--}}{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}

	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\")")
	void Handlebars_Indenting_On_indent_handlebars_true_9() {
		opts.indent_handlebars = true;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth(
            "{{#if 0}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}",
            //  -- output --
            "{{#if words}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}");
		bth(
            "{{#if     words}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}",
            //  -- output --
            "{{#if words}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "    {{else}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{else}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "    {{else}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "    {{else}}\n" +
            "        {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\" {{else}}class=\"{{class2}}\" {{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}</span>");
		bth(
            "<{{ele}} unformatted=\"{{#if}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}\">{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}</{{ele}}>");
		bth(
            "<div unformatted=\"{{#if}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}\">{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}</div>");
		bth(
            "<div unformatted=\"{{#if  }}    {{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}\">{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}</div>");
		bth(
            "<div class=\"{{#if thingIs \"value\"}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}\"></div>");
		bth(
            "<div class=\"{{#if thingIs \'value\'}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}\"></div>");
		bth(
            "<div class=\'{{#if thingIs \"value\"}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}\'></div>");
		bth(
            "<div class=\'{{#if thingIs \'value\'}}{{!-- \n" +
            " mult-line\n" +
            "comment \n" +
            "{{#> component}}\n" +
            " mult-line\n" +
            "comment  \n" +
            "     with spacing\n" +
            " {{/ component}}--}}{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}

	@Test
	@DisplayName("Handlebars Indenting On - (indent_handlebars = \"true\", wrap_line_length = \"80\")")
	void Handlebars_Indenting_On_indent_handlebars_true_wrap_line_length_80_() {
		opts.indent_handlebars = true;
		opts.wrap_line_length = 80;
		bth("{{page-title}}");
		bth(
            "{{page-title}}\n" +
            "{{a}}\n" +
            "{{value-title}}");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "content\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "content",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "content\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>\n" +
            "    </div>\n" +
            "{{/if}}\n" +
            "content");
		bth(
            "{{textarea value=someContent}}\n" +
            "\n" +
            "content\n" +
            "{{#if condition}}\n" +
            "    <div  class=\"some-class-detail\">{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong></div>\n" +
            "{{/if}}\n" +
            "content",
            //  -- output --
            "{{textarea value=someContent}}\n" +
            "\n" +
            "content\n" +
            "{{#if condition}}\n" +
            "    <div class=\"some-class-detail\">\n" +
            "        {{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>{{helper \"hello\"}}<strong>{{helper \"world\"}}</strong>\n" +
            "    </div>\n" +
            "{{/if}}\n" +
            "content");
		
        // error case
        bth(
            "{{page-title}}\n" +
            "{{ myHelper someValue}}\n" +
            "content\n" +
            "{{value-title}}");
		
        // Issue #1469 - preserve newlines inside handlebars, including first one. BUG: does not fix indenting inside handlebars.
        bth(
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "content\n" +
            "   {{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "       {{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}",
            //  -- output --
            "{{em-input\n" +
            "  label=\"Some Labe\" property=\"amt\"\n" +
            "  type=\"text\" placeholder=\"\"}}\n" +
            "content\n" +
            "{{em-input label=\"Type*\"\n" +
            "property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth(
            "{{em-input label=\"Some Labe\" property=\"amt\" type=\"text\" placeholder=\"\"}}\n" +
            "content\n" +
            "{{em-input label=\"Type*\" property=\"type\" type=\"text\" placeholder=\"(LTD)\"}}\n" +
            "{{em-input label=\"Place*\" property=\"place\" type=\"text\" placeholder=\"\"}}");
		bth("{{#if 0}}{{/if}}");
		bth("{{#if 0}}content{{/if}}");
		bth(
            "{{#if 0}}\n" +
            "{{/if}}");
		bth(
            "{{#if     words}}{{/if}}",
            //  -- output --
            "{{#if words}}{{/if}}");
		bth(
            "{{#if     words}}content{{/if}}",
            //  -- output --
            "{{#if words}}content{{/if}}");
		bth(
            "{{#if     words}}content{{/if}}",
            //  -- output --
            "{{#if words}}content{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "<div>\n" +
            "</div>\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "{{#if 1}}\n" +
            "{{/if}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    {{#if 1}}\n" +
            "    {{/if}}\n" +
            "</div>");
		bth(
            "{{#if}}\n" +
            "{{#each}}\n" +
            "{{#if}}\n" +
            "content\n" +
            "{{/if}}\n" +
            "{{#if}}\n" +
            "content\n" +
            "{{/if}}\n" +
            "{{/each}}\n" +
            "{{/if}}",
            //  -- output --
            "{{#if}}\n" +
            "    {{#each}}\n" +
            "        {{#if}}\n" +
            "            content\n" +
            "        {{/if}}\n" +
            "        {{#if}}\n" +
            "            content\n" +
            "        {{/if}}\n" +
            "    {{/each}}\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    <div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "    <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "<div>\n" +
            "    <small>SMALL TEXT</small>\n" +
            "    <span>\n" +
            "        {{#if isOwner}}\n" +
            "            <span><i class=\"fa fa-close\"></i></span>\n" +
            "        {{else}}\n" +
            "            <span><i class=\"fa fa-bolt\"></i></span>\n" +
            "        {{/if}}\n" +
            "    </span>\n" +
            "    <strong>{{userName}}:&nbsp;</strong>{{text}}\n" +
            "</div>");
		bth(
            "{{#if `this.customerSegment == \"Active\"`}}\n" +
            "    ...\n" +
            "{{/if}}");
		bth(
            "{{#isDealLink}}\n" +
            "&nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}",
            //  -- output --
            "{{#isDealLink}}\n" +
            "    &nbsp;&nbsp;<a target=\"_blank\" href=\"{{dealLink}}\" class=\"weak\">See</a>\n" +
            "{{/isDealLink}}");
		bth(
            "{{#if 1}}\n" +
            "    content\n" +
            "    {{else}}\n" +
            "    content\n" +
            "{{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "    content\n" +
            "{{else}}\n" +
            "    content\n" +
            "{{/if}}");
		bth(
            "{{#if 1}}\n" +
            "    {{else}}\n" +
            "    {{/if}}",
            //  -- output --
            "{{#if 1}}\n" +
            "{{else}}\n" +
            "{{/if}}");
		bth(
            "{{#if thing}}\n" +
            "{{#if otherthing}}\n" +
            "    content\n" +
            "    {{else}}\n" +
            "content\n" +
            "    {{/if}}\n" +
            "       {{else}}\n" +
            "content\n" +
            "{{/if}}",
            //  -- output --
            "{{#if thing}}\n" +
            "    {{#if otherthing}}\n" +
            "        content\n" +
            "    {{else}}\n" +
            "        content\n" +
            "    {{/if}}\n" +
            "{{else}}\n" +
            "    content\n" +
            "{{/if}}");
		
        // ISSUE #800 and #1123: else if and #unless
        bth(
            "{{#if callOn}}\n" +
            "{{#unless callOn}}\n" +
            "      content\n" +
            "   {{else}}\n" +
            "{{translate \"offText\"}}\n" +
            "{{/unless callOn}}\n" +
            "   {{else if (eq callOn false)}}\n" +
            "content\n" +
            "        {{/if}}",
            //  -- output --
            "{{#if callOn}}\n" +
            "    {{#unless callOn}}\n" +
            "        content\n" +
            "    {{else}}\n" +
            "        {{translate \"offText\"}}\n" +
            "    {{/unless callOn}}\n" +
            "{{else if (eq callOn false)}}\n" +
            "    content\n" +
            "{{/if}}");
		bth(
            "<div {{someStyle}}>  </div>",
            //  -- output --
            "<div {{someStyle}}> </div>");
		
        // only partial support for complex templating in attributes
        bth(
            "<dIv {{#if test}}class=\"foo\"{{/if}}>content</dIv>",
            //  -- output --
            "<dIv {{#if test}}class=\"foo\" {{/if}}>content</dIv>");
		test_fragment(
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"{{else}}class=\"{{class2}}\"{{/if}}>content</diV>",
            //  -- output --
            "<diV {{#if thing}}{{somestyle}}class_spacing_for=\"{{class}}\"\n" +
            "    {{else}}class=\"{{class2}}\" {{/if}}>content</diV>");
		
        // partiial support for templating in attributes
        bth(
            "<span {{#if condition}}class=\"foo\"{{/if}}>content</span>",
            //  -- output --
            "<span {{#if condition}}class=\"foo\" {{/if}}>content</span>");
		bth("<{{ele}} unformatted=\"{{#if}}content{{/if}}\">content</{{ele}}>");
		bth("<div unformatted=\"{{#if}}content{{/if}}\">content</div>");
		bth("<div unformatted=\"{{#if  }}    content{{/if}}\">content</div>");
		bth("<div class=\"{{#if thingIs \"value\"}}content{{/if}}\"></div>");
		bth("<div class=\"{{#if thingIs \'value\'}}content{{/if}}\"></div>");
		bth("<div class=\'{{#if thingIs \"value\"}}content{{/if}}\'></div>");
		bth("<div class=\'{{#if thingIs \'value\'}}content{{/if}}\'></div>");
		bth("<span>{{condition < 0 ? \"result1\" : \"result2\"}}</span>");
		bth("<span>{{condition1 && condition2 && condition3 && condition4 < 0 ? \"resForTrue\" : \"resForFalse\"}}</span>");
	}


	@Test
	@DisplayName("Handlebars Else If, Each, and Inverted Section tag indenting")
	void Handlebars_Else_If_Each_and_Inverted_Section_tag_indenting() {
		opts.indent_handlebars = true;
		bth(
            "{{#if test}}<div></div>{{else}}<div></div>{{/if}}",
            //  -- output --
            "{{#if test}}\n" +
            "    <div></div>\n" +
            "{{else}}\n" +
            "    <div></div>\n" +
            "{{/if}}");
		bth("{{#if test}}<span></span>{{else}}<span></span>{{/if}}");
		bth(
            "<a class=\"navbar-brand\">\n" +
            "    {{#if connected}}\n" +
            "        <i class=\"fa fa-link\" style=\"color:green\"></i> {{else if sleep}}\n" +
            "        <i class=\"fa fa-sleep\" style=\"color:yellow\"></i>\n" +
            "    {{else}}\n" +
            "        <i class=\"fa fa-unlink\" style=\"color:red\"></i>\n" +
            "    {{/if}}\n" +
            "</a>",
            //  -- output --
            "<a class=\"navbar-brand\">\n" +
            "    {{#if connected}}\n" +
            "        <i class=\"fa fa-link\" style=\"color:green\"></i>\n" +
            "    {{else if sleep}}\n" +
            "        <i class=\"fa fa-sleep\" style=\"color:yellow\"></i>\n" +
            "    {{else}}\n" +
            "        <i class=\"fa fa-unlink\" style=\"color:red\"></i>\n" +
            "    {{/if}}\n" +
            "</a>");
		bth(
            "{{#each clinics as |clinic|}}\n" +
            "    <p>{{clinic.name}}</p>\n" +
            "{{else}}\n" +
            "    <p>Unfortunately no clinics found.</p>\n" +
            "{{/each}}");
		
        // Issue #1623 - Fix indentation of `^` inverted section tags in Handlebars/Mustache code
        bth(
            "{{^inverted-condition}}\n" +
            "    <p>Unfortunately this condition is false.</p>\n" +
            "{{/inverted-condition}}");
		
        // Issue #1756 - Fix indentation of partials
        bth(
            "{{#*inline \"myPartial\"}}\n" +
            "    <p>Unfortunately this condition is false.</p>\n" +
            "{{/inline}}");
		bth(
            "{{#> myPartial}}\n" +
            "    <p>Unfortunately this condition is false.</p>\n" +
            "{{/myPartial}}");
		
        // Issue #1946 - Indentation of partial blocks with whitespace following partial name
        bth(
            "{{#> myPartial }}\n" +
            "    <p>Unfortunately this condition is false.</p>\n" +
            "{{/myPartial}}");
		
        // Issue #1946 - Indentation of partial blocks with parameters
        bth(
            "{{#> myPartial param=\"test\"}}\n" +
            "    <p>Unfortunately this condition is false.</p>\n" +
            "{{/myPartial}}");
		
        // Issue #1946 - Indentation of inline partials with parameters
        bth(
            "{{#*inline \"myPartial\" param=\"test\"}}\n" +
            "    <p>Unfortunately this condition is false.</p>\n" +
            "{{/inline}}");
	}


	@Test
	@DisplayName("Unclosed html elements")
	void Unclosed_html_elements() {
		bth(
            "<source>\n" +
            "<source>");
		bth(
            "<br>\n" +
            "<br>");
		bth(
            "<input>\n" +
            "<input>");
		bth(
            "<meta>\n" +
            "<meta>");
		bth(
            "<link>\n" +
            "<link>");
		bth(
            "<colgroup>\n" +
            "    <col>\n" +
            "    <col>\n" +
            "</colgroup>");
		bth(
            "<source>\n" +
            "    <source>",
            //  -- output --
            "<source>\n" +
            "<source>");
		bth(
            "<br>\n" +
            "    <br>",
            //  -- output --
            "<br>\n" +
            "<br>");
		bth(
            "<input>\n" +
            "    <input>",
            //  -- output --
            "<input>\n" +
            "<input>");
		bth(
            "<meta>\n" +
            "    <meta>",
            //  -- output --
            "<meta>\n" +
            "<meta>");
		bth(
            "<link>\n" +
            "    <link>",
            //  -- output --
            "<link>\n" +
            "<link>");
		bth(
            "<colgroup>\n" +
            "        <col>\n" +
            "        <col>\n" +
            "</colgroup>",
            //  -- output --
            "<colgroup>\n" +
            "    <col>\n" +
            "    <col>\n" +
            "</colgroup>");
	}


	@Test
	@DisplayName("Optional html elements")
	void Optional_html_elements() {
		test_fragment(
            "<li>test content\n" +
            "<li>test content\n" +
            "<li>test content");
		bth(
            "<ol>\n" +
            "    <li>test content\n" +
            "    <li>test content\n" +
            "    <li>test content\n" +
            "</ol>");
		bth(
            "<menu>\n" +
            "    <li>test content\n" +
            "    <li>test content\n" +
            "    <li>test content\n" +
            "</menu>");
		bth(
            "<ol>\n" +
            "    <li>\n" +
            "        test content\n" +
            "    <li>\n" +
            "        <ul>\n" +
            "            <li> extra text\n" +
            "            <li> depth check\n" +
            "        </ul>\n" +
            "    <li> test content\n" +
            "    <li>\n" +
            "        test content\n" +
            "</ol>");
		bth(
            "<menu>\n" +
            "    <li>\n" +
            "        test content\n" +
            "    <li>\n" +
            "        <ol>\n" +
            "            <li> level 1 check\n" +
            "            <li>\n" +
            "                <menu>\n" +
            "                    <li> level 2 check\n" +
            "                    <li>\n" +
            "                        <ul>\n" +
            "                            <li> level 3 check\n" +
            "                        </ul>\n" +
            "                    <li>\n" +
            "                        test content\n" +
            "                </menu>\n" +
            "        </ol>\n" +
            "    <li> test content\n" +
            "    <li>\n" +
            "        test content\n" +
            "</menu>");
		bth(
            "<dl>\n" +
            "    <dt>\n" +
            "        test content\n" +
            "    <dt>\n" +
            "        test content\n" +
            "    <dd>\n" +
            "        test content\n" +
            "    <dd>\n" +
            "        test content\n" +
            "    <dt>\n" +
            "        test content\n" +
            "    <dd>\n" +
            "        <dl>\n" +
            "            <dt>\n" +
            "                test content\n" +
            "            <dt>\n" +
            "                test content\n" +
            "            <dd>\n" +
            "                test content\n" +
            "        </dl>\n" +
            "</dl>");
		bth(
            "<select>\n" +
            "    <optgroup>\n" +
            "        test content\n" +
            "    <optgroup>\n" +
            "        test content\n" +
            "        <option>\n" +
            "            test content\n" +
            "        <option>\n" +
            "            test content\n" +
            "    <optgroup>\n" +
            "        test content\n" +
            "        <option>\n" +
            "            <p>test content\n" +
            "        <option>\n" +
            "            test content\n" +
            "</select>");
		
        // Regression test for #1649
        bth(
            "<table>\n" +
            "    <tbody>\n" +
            "        <tr>\n" +
            "            <td>\n" +
            "                <table>\n" +
            "                    <thead>\n" +
            "                        <th>\n" +
            "                        </th>\n" +
            "                    </thead>\n" +
            "                    <tbody>\n" +
            "                        <tr>\n" +
            "                            <td>\n" +
            "                            </td>\n" +
            "                        </tr>\n" +
            "                    </tbody>\n" +
            "                </table>\n" +
            "            </td>\n" +
            "        </tr>\n" +
            "    </tbody>\n" +
            "</table>");
		bth(
            "<table>\n" +
            "    <caption>37547 TEE Electric Powered Rail Car Train Functions (Abbreviated)\n" +
            "    <colgroup>\n" +
            "        <col>\n" +
            "        <col>\n" +
            "        <col>\n" +
            "    <thead>\n" +
            "        <tr>\n" +
            "            <th>Function\n" +
            "            <th>Control Unit\n" +
            "            <th>Central Station\n" +
            "    <tbody>\n" +
            "        <tr>\n" +
            "            <td>Headlights\n" +
            "            <td>✔\n" +
            "            <td>✔\n" +
            "        <tr>\n" +
            "            <td>Interior Lights\n" +
            "            <td>✔\n" +
            "            <td>✔\n" +
            "        <tr>\n" +
            "            <td>Electric locomotive operating sounds\n" +
            "            <td>✔\n" +
            "                <table>\n" +
            "                    <caption>37547 TEE Electric Powered Rail Car Train Functions (Abbreviated)\n" +
            "                    <colgroup>\n" +
            "                        <col>\n" +
            "                        <col>\n" +
            "                        <col>\n" +
            "                    <thead>\n" +
            "                        <tr>\n" +
            "                            <th>Function\n" +
            "                            <th>\n" +
            "                                <p>Control Unit\n" +
            "                            <th>Central Station\n" +
            "                    <tbody>\n" +
            "                        <tr>\n" +
            "                            <td>Headlights\n" +
            "                            <td>✔\n" +
            "                            <td>✔\n" +
            "                        <tr>\n" +
            "                            <td>Interior Lights\n" +
            "                            <td>✔\n" +
            "                            <td>✔\n" +
            "                        <tr>\n" +
            "                            <td>Electric locomotive operating sounds\n" +
            "                            <td>✔\n" +
            "                            <td>✔\n" +
            "                        <tr>\n" +
            "                            <td>Engineer’s cab lighting\n" +
            "                            <td>\n" +
            "                            <td>✔\n" +
            "                        <tr>\n" +
            "                            <td>Station Announcements - Swiss\n" +
            "                            <td>\n" +
            "                            <td>✔\n" +
            "                    <tfoot>\n" +
            "                        <tr>\n" +
            "                            <td>Station Announcements - Swiss\n" +
            "                            <td>\n" +
            "                            <td>✔\n" +
            "                </table>\n" +
            "            <td>✔\n" +
            "        <tr>\n" +
            "            <td>Engineer’s cab lighting\n" +
            "            <td>\n" +
            "            <td>✔\n" +
            "        <tr>\n" +
            "            <td>Station Announcements - Swiss\n" +
            "            <td>\n" +
            "            <td>✔\n" +
            "    <tfoot>\n" +
            "        <tr>\n" +
            "            <td>Station Announcements - Swiss\n" +
            "            <td>\n" +
            "            <td>✔\n" +
            "</table>");
		
        // Regression test for #1213
        bth(
            "<ul><li>ab<li>cd</li><li>cd</li></ul><dl><dt>ef<dt>gh</dt><dt>gh</dt></dl>\n" +
            "<ul><li>ab</li><li>cd<li>cd</li></ul><dl><dt>ef</dt><dt>gh<dt>gh</dt></dl>",
            //  -- output --
            "<ul>\n" +
            "    <li>ab\n" +
            "    <li>cd</li>\n" +
            "    <li>cd</li>\n" +
            "</ul>\n" +
            "<dl>\n" +
            "    <dt>ef\n" +
            "    <dt>gh</dt>\n" +
            "    <dt>gh</dt>\n" +
            "</dl>\n" +
            "<ul>\n" +
            "    <li>ab</li>\n" +
            "    <li>cd\n" +
            "    <li>cd</li>\n" +
            "</ul>\n" +
            "<dl>\n" +
            "    <dt>ef</dt>\n" +
            "    <dt>gh\n" +
            "    <dt>gh</dt>\n" +
            "</dl>");
		
        // P element optional closing tag - #1503
        bth(
            "<p><p><dl><dt>ef<dt><p>gh</dt><dt>gh</dt></dl><p><h3>headers are outside paragraphs</h3>\n" +
            "<p>.<textarea><p><p>.</textarea><textarea><p><p>.</textarea><p>.<p>.</p>",
            //  -- output --
            "<p>\n" +
            "<p>\n" +
            "<dl>\n" +
            "    <dt>ef\n" +
            "    <dt>\n" +
            "        <p>gh\n" +
            "    </dt>\n" +
            "    <dt>gh</dt>\n" +
            "</dl>\n" +
            "<p>\n" +
            "<h3>headers are outside paragraphs</h3>\n" +
            "<p>.<textarea><p><p>.</textarea><textarea><p><p>.</textarea>\n" +
            "<p>.\n" +
            "<p>.</p>");
	}


	@Test
	@DisplayName("Unformatted tags")
	void Unformatted_tags() {
		bth(
            "<ol>\n" +
            "    <li>b<pre>c</pre></li>\n" +
            "</ol>",
            //  -- output --
            "<ol>\n" +
            "    <li>b\n" +
            "        <pre>c</pre>\n" +
            "    </li>\n" +
            "</ol>");
		bth(
            "<ol>\n" +
            "    <li>b<code>c</code></li>\n" +
            "</ol>");
		bth(
            "<ul>\n" +
            "    <li>\n" +
            "        <span class=\"octicon octicon-person\"></span>\n" +
            "        <a href=\"/contact/\">Kontakt</a>\n" +
            "    </li>\n" +
            "</ul>");
		bth("<div class=\"searchform\"><input type=\"text\" value=\"\" name=\"s\" id=\"s\" /><input type=\"submit\" id=\"searchsubmit\" value=\"Search\" /></div>");
		bth("<div class=\"searchform\"><input type=\"text\" value=\"\" name=\"s\" id=\"s\"><input type=\"submit\" id=\"searchsubmit\" value=\"Search\"></div>");
		bth(
            "<p>\n" +
            "    <a href=\"/test/\"><img src=\"test.jpg\" /></a>\n" +
            "</p>");
		bth(
            "<p>\n" +
            "    <a href=\"/test/\"><img src=\"test.jpg\" /></a><a href=\"/test/\"><img src=\"test.jpg\" /></a>\n" +
            "</p>");
		bth(
            "<p>\n" +
            "    <a href=\"/test/\"><img src=\"test.jpg\" /></a><a href=\"/test/\"><img src=\"test.jpg\" /></a><a href=\"/test/\"><img src=\"test.jpg\" /></a><a href=\"/test/\"><img src=\"test.jpg\" /></a>\n" +
            "</p>");
		bth(
            "<p>\n" +
            "    <span>image: <img src=\"test.jpg\" /></span><span>image: <img src=\"test.jpg\" /></span>\n" +
            "</p>");
		bth(
            "<p>\n" +
            "    <strong>image: <img src=\"test.jpg\" /></strong><strong>image: <img src=\"test.jpg\" /></strong>\n" +
            "</p>");
	}


	@Test
	@DisplayName("File starting with comment")
	void File_starting_with_comment() {
		bth(
            "<!--sample comment -->\n" +
            "\n" +
            "<html>\n" +
            "<body>\n" +
            "    <span>a span</span>\n" +
            "</body>\n" +
            "\n" +
            "</html>");
	}


	@Test
	@DisplayName("ISSUE #545 and #944 Ignore directive works in html")
	void ISSUE_545_and_944_Ignore_directive_works_in_html() {
		
        // ignore starts _after_ the start comment, ends after the end comment
        bth(
            "<div>\n" +
            "    <!-- beautify ignore:start -->\n" +
            "@{\n" +
            "\n" +
            "    ViewBag.Title = \"Dashboard\";\n" +
            "    string firstName = string.Empty;\n" +
            "    string userId = ViewBag.UserId;\n" +
            "\n" +
            "    if( !string.IsNullOrEmpty(ViewBag.FirstName ) ) {\n" +
            "\n" +
            "         firstName = \"<h2>Hi \" + ViewBag.FirstName + \"</h2>\";\n" +
            "\n" +
            "    }\n" +
            "\n" +
            "}\n" +
            " <!-- beautify ignore:end -->\n" +
            "\n" +
            "    <header class=\"layout-header\">\n" +
            "\n" +
            "        <h2 id=\"logo\"><a href=\"/\">Logo</a></h2>\n" +
            "\n" +
            "        <ul class=\"social\">\n" +
            "\n" +
            "            <li class=\"facebook\"><a href=\"#\">Facebook</a></li>\n" +
            "            <li class=\"twitter\"><a href=\"#\">Twitter</a></li>\n" +
            "\n" +
            "        </ul>\n" +
            "\n" +
            "    </header>\n" +
            "</div>");
	}


	@Test
	@DisplayName("Issue 1478 - Space handling inside self closing tag")
	void Issue_1478_Space_handling_inside_self_closing_tag() {
		bth(
            "<div>\n" +
            "    <br/>\n" +
            "    <br />\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <br />\n" +
            "    <br />\n" +
            "</div>");
	}


	@Test
	@DisplayName("Single line comment after closing tag")
	void Single_line_comment_after_closing_tag() {
		bth(
            "<div class=\"col\">\n" +
            "    <div class=\"row\">\n" +
            "        <div class=\"card\">\n" +
            "\n" +
            "            <h1>Some heading</h1>\n" +
            "            <p>Some text for the card.</p>\n" +
            "            <img src=\"some/image.jpg\" alt=\"\">\n" +
            "\n" +
            "            </div>    <!-- /.card -->\n" +
            "    </div>\n" +
            "            <!-- /.row -->\n" +
            "</div> <!-- /.col -->",
            //  -- output --
            "<div class=\"col\">\n" +
            "    <div class=\"row\">\n" +
            "        <div class=\"card\">\n" +
            "\n" +
            "            <h1>Some heading</h1>\n" +
            "            <p>Some text for the card.</p>\n" +
            "            <img src=\"some/image.jpg\" alt=\"\">\n" +
            "\n" +
            "        </div> <!-- /.card -->\n" +
            "    </div>\n" +
            "    <!-- /.row -->\n" +
            "</div> <!-- /.col -->");
	}


	@Test
	@DisplayName("Regression Tests")
	void Regression_Tests() {
		
        // #1202
        bth("<a class=\"js-open-move-from-header\" href=\"#\">5A - IN-SPRINT TESTING</a>");
		test_fragment("<a \">9</a\">");
		bth("<a href=\"javascript:;\" id=\"_h_url_paid_pro3\" onmousedown=\"_h_url_click_paid_pro(this);\" rel=\"nofollow\" class=\"pro-title\" itemprop=\"name\">WA GlassKote</a>");
		bth("<a href=\"/b/yergey-brewing-a-beer-has-no-name/1745600\">\"A Beer Has No Name\"</a>");
		
        // #1304
        bth("<label>Every</label><input class=\"scheduler__minutes-input\" type=\"text\">");
		
        // #1377
        bth(
            "<a href=\'\' onclick=\'doIt(\"<?php echo str_replace(\"\'\", \"\\ \", $var); ?>  \"); \'>\n" +
            "    Test\n" +
            "</a>\n" +
            "\n" +
            "<?php include_once $_SERVER[\'DOCUMENT_ROOT\'] . \"/shared/helpModal.php\";  ?>");
		
        // #1736 - unquoted attribute with slashes
        bth(
            "<div>\n" +
            "    <a href=http://www.example.com></a>\n" +
            "</div>");
	}


	@Test
	@DisplayName("minimal template handling - ()")
	void minimal_template_handling_() {
		bth("<h1  class=\"content-page-header\"><?php$view[\"name\"]; ?></h1>", "<h1 class=\"content-page-header\"><?php$view[\"name\"]; ?></h1>");
		bth(
            "<?php\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "?>");
		test_fragment(
            "<?php ?>\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "<?php \"A\" ?>abc<?php \"D\" ?>\n" +
            "<?php \"B\" ?>\n" +
            "<?php \"C\" ?>");
		bth(
            "<?php\n" +
            "echo \"A\";\n" +
            "?>\n" +
            "<span>Test</span>");
		bth("<<?php html_element(); ?> <?phplanguage_attributes();?>>abc</<?php html_element(); ?>>");
		bth("<input type=\"text\" value=\"<?php$x[\"test\"] . $x[\'test\']?>\">");
	}

	@Test
	@DisplayName("minimal template handling - ()")
	void minimal_template_handling_1() {
		bth("<h1  class=\"content-page-header\"><?=$view[\"name\"]; ?></h1>", "<h1 class=\"content-page-header\"><?=$view[\"name\"]; ?></h1>");
		bth(
            "<?=\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "?>");
		test_fragment(
            "<?= ?>\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "<?= \"A\" ?>abc<?= \"D\" ?>\n" +
            "<?= \"B\" ?>\n" +
            "<?= \"C\" ?>");
		bth(
            "<?=\n" +
            "echo \"A\";\n" +
            "?>\n" +
            "<span>Test</span>");
		bth("<<?= html_element(); ?> <?=language_attributes();?>>abc</<?= html_element(); ?>>");
		bth("<input type=\"text\" value=\"<?=$x[\"test\"] . $x[\'test\']?>\">");
	}

	@Test
	@DisplayName("minimal template handling - ()")
	void minimal_template_handling_2() {
		bth("<h1  class=\"content-page-header\"><? $view[\"name\"]; ?></h1>", "<h1 class=\"content-page-header\"><? $view[\"name\"]; ?></h1>");
		bth(
            "<? \n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "?>");
		test_fragment(
            "<?  ?>\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "<?  \"A\" ?>abc<?  \"D\" ?>\n" +
            "<?  \"B\" ?>\n" +
            "<?  \"C\" ?>");
		bth(
            "<? \n" +
            "echo \"A\";\n" +
            "?>\n" +
            "<span>Test</span>");
		bth("<<?  html_element(); ?> <? language_attributes();?>>abc</<?  html_element(); ?>>");
		bth("<input type=\"text\" value=\"<? $x[\"test\"] . $x[\'test\']?>\">");
	}

	@Test
	@DisplayName("minimal template handling - ()")
	void minimal_template_handling_3() {
		bth("<h1  class=\"content-page-header\"><%$view[\"name\"]; %></h1>", "<h1 class=\"content-page-header\"><%$view[\"name\"]; %></h1>");
		bth(
            "<%\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "%>");
		test_fragment(
            "<% %>\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "<% \"A\" %>abc<% \"D\" %>\n" +
            "<% \"B\" %>\n" +
            "<% \"C\" %>");
		bth(
            "<%\n" +
            "echo \"A\";\n" +
            "%>\n" +
            "<span>Test</span>");
		bth("<<% html_element(); %> <%language_attributes();%>>abc</<% html_element(); %>>");
		bth("<input type=\"text\" value=\"<%$x[\"test\"] . $x[\'test\']%>\">");
	}

	@Test
	@DisplayName("minimal template handling - ()")
	void minimal_template_handling_4() {
		bth("<h1  class=\"content-page-header\">{{$view[\"name\"]; }}</h1>", "<h1 class=\"content-page-header\">{{$view[\"name\"]; }}</h1>");
		bth(
            "{{\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "}}");
		test_fragment(
            "{{ }}\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "{{ \"A\" }}abc{{ \"D\" }}\n" +
            "{{ \"B\" }}\n" +
            "{{ \"C\" }}");
		bth(
            "{{\n" +
            "echo \"A\";\n" +
            "}}\n" +
            "<span>Test</span>");
		bth("<{{ html_element(); }} {{language_attributes();}}>abc</{{ html_element(); }}>");
		bth("<input type=\"text\" value=\"{{$x[\"test\"] . $x[\'test\']}}\">");
	}

	@Test
	@DisplayName("minimal template handling - ()")
	void minimal_template_handling_5() {
		bth("<h1  class=\"content-page-header\">{#$view[\"name\"]; #}</h1>", "<h1 class=\"content-page-header\">{#$view[\"name\"]; #}</h1>");
		bth(
            "{#\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "#}");
		test_fragment(
            "{# #}\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "{# \"A\" #}abc{# \"D\" #}\n" +
            "{# \"B\" #}\n" +
            "{# \"C\" #}");
		bth(
            "{#\n" +
            "echo \"A\";\n" +
            "#}\n" +
            "<span>Test</span>");
		bth("<{# html_element(); #} {#language_attributes();#}>abc</{# html_element(); #}>");
		bth("<input type=\"text\" value=\"{#$x[\"test\"] . $x[\'test\']#}\">");
	}

	@Test
	@DisplayName("minimal template handling - ()")
	void minimal_template_handling_6() {
		bth("<h1  class=\"content-page-header\">{%$view[\"name\"]; %}</h1>", "<h1 class=\"content-page-header\">{%$view[\"name\"]; %}</h1>");
		bth(
            "{%\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "%}");
		test_fragment(
            "{% %}\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "{% \"A\" %}abc{% \"D\" %}\n" +
            "{% \"B\" %}\n" +
            "{% \"C\" %}");
		bth(
            "{%\n" +
            "echo \"A\";\n" +
            "%}\n" +
            "<span>Test</span>");
		bth("<{% html_element(); %} {%language_attributes();%}>abc</{% html_element(); %}>");
		bth("<input type=\"text\" value=\"{%$x[\"test\"] . $x[\'test\']%}\">");
	}

	@Test
	@DisplayName("minimal template handling - (templating = \"\"smarty\"\")")
	void minimal_template_handling_templating_smarty_() {
		opts.templating = EnumSet.of(TemplateLanguage.valueOf("smarty"));
		bth("<h1  class=\"content-page-header\">{a$view[\"name\"]; a}</h1>", "<h1 class=\"content-page-header\">{a$view[\"name\"]; a}</h1>");
		bth(
            "{a\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "a}");
		test_fragment(
            "{a a}\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "{a \"A\" a}abc{a \"D\" a}\n" +
            "{a \"B\" a}\n" +
            "{a \"C\" a}");
		bth(
            "{a\n" +
            "echo \"A\";\n" +
            "a}\n" +
            "<span>Test</span>");
		bth("<{a html_element(); a} {alanguage_attributes();a}>abc</{a html_element(); a}>");
		bth("<input type=\"text\" value=\"{a$x[\"test\"] . $x[\'test\']a}\">");
	}

	@Test
	@DisplayName("minimal template handling - (templating = \"\"smarty\"\")")
	void minimal_template_handling_templating_smarty_1() {
		opts.templating = EnumSet.of(TemplateLanguage.valueOf("smarty"));
		bth("<h1  class=\"content-page-header\">{*$view[\"name\"]; *}</h1>", "<h1 class=\"content-page-header\">{*$view[\"name\"]; *}</h1>");
		bth(
            "{*\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "*}");
		test_fragment(
            "{* *}\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "{* \"A\" *}abc{* \"D\" *}\n" +
            "{* \"B\" *}\n" +
            "{* \"C\" *}");
		bth(
            "{*\n" +
            "echo \"A\";\n" +
            "*}\n" +
            "<span>Test</span>");
		bth("<{* html_element(); *} {*language_attributes();*}>abc</{* html_element(); *}>");
		bth("<input type=\"text\" value=\"{*$x[\"test\"] . $x[\'test\']*}\">");
	}

	@Test
	@DisplayName("minimal template handling - (templating = \"\"smarty\"\")")
	void minimal_template_handling_templating_smarty_2() {
		opts.templating = EnumSet.of(TemplateLanguage.valueOf("smarty"));
		bth("<h1  class=\"content-page-header\">{literal}$view[\"name\"]; {/literal}</h1>", "<h1 class=\"content-page-header\">{literal}$view[\"name\"]; {/literal}</h1>");
		bth(
            "{literal}\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "{/literal}");
		test_fragment(
            "{literal} {/literal}\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "{literal} \"A\" {/literal}abc{literal} \"D\" {/literal}\n" +
            "{literal} \"B\" {/literal}\n" +
            "{literal} \"C\" {/literal}");
		bth(
            "{literal}\n" +
            "echo \"A\";\n" +
            "{/literal}\n" +
            "<span>Test</span>");
		bth("<{literal} html_element(); {/literal} {literal}language_attributes();{/literal}>abc</{literal} html_element(); {/literal}>");
		bth("<input type=\"text\" value=\"{literal}$x[\"test\"] . $x[\'test\']{/literal}\">");
	}

	@Test
	@DisplayName("minimal template handling - (indent_handlebars = \"false\")")
	void minimal_template_handling_indent_handlebars_false_() {
		opts.indent_handlebars = false;
		bth("<h1  class=\"content-page-header\">{{$view[\"name\"]; }}</h1>", "<h1 class=\"content-page-header\">{{$view[\"name\"]; }}</h1>");
		bth(
            "{{\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "}}");
		test_fragment(
            "{{ }}\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "{{ \"A\" }}abc{{ \"D\" }}\n" +
            "{{ \"B\" }}\n" +
            "{{ \"C\" }}");
		bth(
            "{{\n" +
            "echo \"A\";\n" +
            "}}\n" +
            "<span>Test</span>");
		bth("<{{ html_element(); }} {{language_attributes();}}>abc</{{ html_element(); }}>");
		bth("<input type=\"text\" value=\"{{$x[\"test\"] . $x[\'test\']}}\">");
	}

	@Test
	@DisplayName("minimal template handling - (indent_handlebars = \"false\")")
	void minimal_template_handling_indent_handlebars_false_1() {
		opts.indent_handlebars = false;
		bth("<h1  class=\"content-page-header\">{{#$view[\"name\"]; }}</h1>", "<h1 class=\"content-page-header\">{{#$view[\"name\"]; }}</h1>");
		bth(
            "{{#\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "}}");
		test_fragment(
            "{{# }}\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "{{# \"A\" }}abc{{# \"D\" }}\n" +
            "{{# \"B\" }}\n" +
            "{{# \"C\" }}");
		bth(
            "{{#\n" +
            "echo \"A\";\n" +
            "}}\n" +
            "<span>Test</span>");
		bth("<{{# html_element(); }} {{#language_attributes();}}>abc</{{# html_element(); }}>");
		bth("<input type=\"text\" value=\"{{#$x[\"test\"] . $x[\'test\']}}\">");
	}

	@Test
	@DisplayName("minimal template handling - (indent_handlebars = \"false\")")
	void minimal_template_handling_indent_handlebars_false_2() {
		opts.indent_handlebars = false;
		bth("<h1  class=\"content-page-header\">{{!$view[\"name\"]; }}</h1>", "<h1 class=\"content-page-header\">{{!$view[\"name\"]; }}</h1>");
		bth(
            "{{!\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "}}");
		test_fragment(
            "{{! }}\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "{{! \"A\" }}abc{{! \"D\" }}\n" +
            "{{! \"B\" }}\n" +
            "{{! \"C\" }}");
		bth(
            "{{!\n" +
            "echo \"A\";\n" +
            "}}\n" +
            "<span>Test</span>");
		bth("<{{! html_element(); }} {{!language_attributes();}}>abc</{{! html_element(); }}>");
		bth("<input type=\"text\" value=\"{{!$x[\"test\"] . $x[\'test\']}}\">");
	}

	@Test
	@DisplayName("minimal template handling - (indent_handlebars = \"false\")")
	void minimal_template_handling_indent_handlebars_false_3() {
		opts.indent_handlebars = false;
		bth("<h1  class=\"content-page-header\">{{!--$view[\"name\"]; --}}</h1>", "<h1 class=\"content-page-header\">{{!--$view[\"name\"]; --}}</h1>");
		bth(
            "{{!--\n" +
            "for($i = 1; $i <= 100; $i++;) {\n" +
            "    #count to 100!\n" +
            "    echo($i . \"</br>\");\n" +
            "}\n" +
            "--}}");
		test_fragment(
            "{{!-- --}}\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<head></head>\n" +
            "\n" +
            "<body></body>\n" +
            "\n" +
            "</html>");
		bth(
            "{{!-- \"A\" --}}abc{{!-- \"D\" --}}\n" +
            "{{!-- \"B\" --}}\n" +
            "{{!-- \"C\" --}}");
		bth(
            "{{!--\n" +
            "echo \"A\";\n" +
            "--}}\n" +
            "<span>Test</span>");
		bth("<{{!-- html_element(); --}} {{!--language_attributes();--}}>abc</{{!-- html_element(); --}}>");
		bth("<input type=\"text\" value=\"{{!--$x[\"test\"] . $x[\'test\']--}}\">");
	}


	@Test
	@DisplayName("Support simple language specific option inheritance/overriding - (js = \"{ \"indent_size\": 3 }\", css = \"{ \"indent_size\": 5 }\")")
	void Support_simple_language_specific_option_inheritance_overriding_js_indent_size_3_css_indent_size_5_() {
		opts.js().apply(new JSONObject("{ 'indent_size': 3 }"));
		opts.css().apply(new JSONObject("{ 'indent_size': 5 }"));
		test_fragment(
            "<head>\n" +
            "    <script>\n" +
            "        if (a == b) {\n" +
            "           test();\n" +
            "        }\n" +
            "    </script>\n" +
            "    <style>\n" +
            "        .selector {\n" +
            "             font-size: 12px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>");
		test_fragment(
            "<head>\n" +
            "<script>\n" +
            "if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "</script>\n" +
            "<style>\n" +
            ".selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>",
            //  -- output --
            "<head>\n" +
            "    <script>\n" +
            "        if (a == b) {\n" +
            "           test();\n" +
            "        }\n" +
            "    </script>\n" +
            "    <style>\n" +
            "        .selector {\n" +
            "             font-size: 12px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>");
		test_fragment(
            "<body>\n" +
            "    <script src=\"one.js\"></script> <!-- one -->\n" +
            "    <script src=\"two.js\"></script> <!-- two-->\n" +
            "</body>");
	}

	@Test
	@DisplayName("Support simple language specific option inheritance/overriding - (html = \"{ \"js\": { \"indent_size\": 3 }, \"css\": { \"indent_size\": 5 } }\")")
	void Support_simple_language_specific_option_inheritance_overriding_html_js_indent_size_3_css_indent_size_5_() {
		opts.html().apply(new JSONObject("{ 'js': { 'indent_size': 3 }, 'css': { 'indent_size': 5 } }"));
		test_fragment(
            "<head>\n" +
            "    <script>\n" +
            "        if (a == b) {\n" +
            "           test();\n" +
            "        }\n" +
            "    </script>\n" +
            "    <style>\n" +
            "        .selector {\n" +
            "             font-size: 12px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>");
		test_fragment(
            "<head>\n" +
            "<script>\n" +
            "if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "</script>\n" +
            "<style>\n" +
            ".selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>",
            //  -- output --
            "<head>\n" +
            "    <script>\n" +
            "        if (a == b) {\n" +
            "           test();\n" +
            "        }\n" +
            "    </script>\n" +
            "    <style>\n" +
            "        .selector {\n" +
            "             font-size: 12px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>");
		test_fragment(
            "<body>\n" +
            "    <script src=\"one.js\"></script> <!-- one -->\n" +
            "    <script src=\"two.js\"></script> <!-- two-->\n" +
            "</body>");
	}

	@Test
	@DisplayName("Support simple language specific option inheritance/overriding - (indent_size = \"9\", js = \"{ \"indent_size\": 5 }\", css = \"{ \"indent_size\": 3 }\")")
	void Support_simple_language_specific_option_inheritance_overriding_indent_size_9_js_indent_size_5_css_indent_size_3_() {
		opts.indent_size = 9;
		opts.js().apply(new JSONObject("{ 'indent_size': 5 }"));
		opts.css().apply(new JSONObject("{ 'indent_size': 3 }"));
		test_fragment(
            "<head>\n" +
            "         <script>\n" +
            "                  if (a == b) {\n" +
            "                       test();\n" +
            "                  }\n" +
            "         </script>\n" +
            "         <style>\n" +
            "                  .selector {\n" +
            "                     font-size: 12px;\n" +
            "                  }\n" +
            "         </style>\n" +
            "</head>");
		test_fragment(
            "<head>\n" +
            "<script>\n" +
            "if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "</script>\n" +
            "<style>\n" +
            ".selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>",
            //  -- output --
            "<head>\n" +
            "         <script>\n" +
            "                  if (a == b) {\n" +
            "                       test();\n" +
            "                  }\n" +
            "         </script>\n" +
            "         <style>\n" +
            "                  .selector {\n" +
            "                     font-size: 12px;\n" +
            "                  }\n" +
            "         </style>\n" +
            "</head>");
		test_fragment(
            "<body>\n" +
            "         <script src=\"one.js\"></script> <!-- one -->\n" +
            "         <script src=\"two.js\"></script> <!-- two-->\n" +
            "</body>");
	}

	@Test
	@DisplayName("Support simple language specific option inheritance/overriding - (indent_size = \"9\", js = \"{ \"indent_size\": 5, \"disabled\": true }\", css = \"{ \"indent_size\": 3 }\")")
	void Support_simple_language_specific_option_inheritance_overriding_indent_size_9_js_indent_size_5_disabled_true_css_indent_size_3_() {
		opts.indent_size = 9;
		opts.js().apply(new JSONObject("{ 'indent_size': 5, 'disabled': true }"));
		opts.css().apply(new JSONObject("{ 'indent_size': 3 }"));
		test_fragment(
            "<head>\n" +
            "         <script>\n" +
            "                  if (a == b) {\n" +
            "                       test();\n" +
            "                  }\n" +
            "         </script>\n" +
            "         <style>\n" +
            "                  .selector {\n" +
            "                     font-size: 12px;\n" +
            "                  }\n" +
            "         </style>\n" +
            "</head>");
		test_fragment(
            "<head>\n" +
            "<script>\n" +
            "if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "</script>\n" +
            "<style>\n" +
            ".selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>",
            //  -- output --
            "<head>\n" +
            "         <script>\n" +
            "                  if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "         </script>\n" +
            "         <style>\n" +
            "                  .selector {\n" +
            "                     font-size: 12px;\n" +
            "                  }\n" +
            "         </style>\n" +
            "</head>");
		test_fragment(
            "<body>\n" +
            "         <script src=\"one.js\"></script> <!-- one -->\n" +
            "         <script src=\"two.js\"></script> <!-- two-->\n" +
            "</body>");
	}

	@Test
	@DisplayName("Support simple language specific option inheritance/overriding - (indent_size = \"9\", js = \"{ \"indent_size\": 5 }\", css = \"{ \"indent_size\": 3, \"disabled\": true }\")")
	void Support_simple_language_specific_option_inheritance_overriding_indent_size_9_js_indent_size_5_css_indent_size_3_disabled_true_() {
		opts.indent_size = 9;
		opts.js().apply(new JSONObject("{ 'indent_size': 5 }"));
		opts.css().apply(new JSONObject("{ 'indent_size': 3, 'disabled': true }"));
		test_fragment(
            "<head>\n" +
            "         <script>\n" +
            "                  if (a == b) {\n" +
            "                       test();\n" +
            "                  }\n" +
            "         </script>\n" +
            "         <style>\n" +
            "                  .selector {\n" +
            "                     font-size: 12px;\n" +
            "                  }\n" +
            "         </style>\n" +
            "</head>");
		test_fragment(
            "<head>\n" +
            "<script>\n" +
            "if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "</script>\n" +
            "<style>\n" +
            ".selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>",
            //  -- output --
            "<head>\n" +
            "         <script>\n" +
            "                  if (a == b) {\n" +
            "                       test();\n" +
            "                  }\n" +
            "         </script>\n" +
            "         <style>\n" +
            "                  .selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "         </style>\n" +
            "</head>");
		test_fragment(
            "<body>\n" +
            "         <script src=\"one.js\"></script> <!-- one -->\n" +
            "         <script src=\"two.js\"></script> <!-- two-->\n" +
            "</body>");
	}

	@Test
	@DisplayName("Support simple language specific option inheritance/overriding - (indent_size = \"9\", html = \"{ \"js\": { \"indent_size\": 3 }, \"css\": { \"indent_size\": 5 }, \"indent_size\": 2}\", js = \"{ \"indent_size\": 5 }\", css = \"{ \"indent_size\": 3 }\")")
	void Support_simple_language_specific_option_inheritance_overriding_indent_size_9_html_js_indent_size_3_css_indent_size_5_indent_size_2_js_indent_size_5_css_indent_size_3_() {
		opts.indent_size = 9;
		opts.html().apply(new JSONObject("{ 'js': { 'indent_size': 3 }, 'css': { 'indent_size': 5 }, 'indent_size': 2}"));
		opts.js().apply(new JSONObject("{ 'indent_size': 5 }"));
		opts.css().apply(new JSONObject("{ 'indent_size': 3 }"));
		test_fragment(
            "<head>\n" +
            "  <script>\n" +
            "    if (a == b) {\n" +
            "       test();\n" +
            "    }\n" +
            "  </script>\n" +
            "  <style>\n" +
            "    .selector {\n" +
            "         font-size: 12px;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>");
		test_fragment(
            "<head>\n" +
            "<script>\n" +
            "if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "</script>\n" +
            "<style>\n" +
            ".selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>",
            //  -- output --
            "<head>\n" +
            "  <script>\n" +
            "    if (a == b) {\n" +
            "       test();\n" +
            "    }\n" +
            "  </script>\n" +
            "  <style>\n" +
            "    .selector {\n" +
            "         font-size: 12px;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>");
		test_fragment(
            "<body>\n" +
            "  <script src=\"one.js\"></script> <!-- one -->\n" +
            "  <script src=\"two.js\"></script> <!-- two-->\n" +
            "</body>");
	}

	@Test
	@DisplayName("Support simple language specific option inheritance/overriding - (indent_size = \"9\", html = \"{ \"js\": { \"indent_size\": 3, \"disabled\": true }, \"css\": { \"indent_size\": 5 }, \"indent_size\": 2}\", js = \"{ \"indent_size\": 5 }\", css = \"{ \"indent_size\": 3 }\")")
	void Support_simple_language_specific_option_inheritance_overriding_indent_size_9_html_js_indent_size_3_disabled_true_css_indent_size_5_indent_size_2_js_indent_size_5_css_indent_size_3_() {
		opts.indent_size = 9;
		opts.html().apply(new JSONObject("{ 'js': { 'indent_size': 3, 'disabled': true }, 'css': { 'indent_size': 5 }, 'indent_size': 2}"));
		opts.js().apply(new JSONObject("{ 'indent_size': 5 }"));
		opts.css().apply(new JSONObject("{ 'indent_size': 3 }"));
		test_fragment(
            "<head>\n" +
            "  <script>\n" +
            "    if (a == b) {\n" +
            "       test();\n" +
            "    }\n" +
            "  </script>\n" +
            "  <style>\n" +
            "    .selector {\n" +
            "         font-size: 12px;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>");
		test_fragment(
            "<head>\n" +
            "<script>\n" +
            "if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "</script>\n" +
            "<style>\n" +
            ".selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>",
            //  -- output --
            "<head>\n" +
            "  <script>\n" +
            "    if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "  </script>\n" +
            "  <style>\n" +
            "    .selector {\n" +
            "         font-size: 12px;\n" +
            "    }\n" +
            "  </style>\n" +
            "</head>");
		test_fragment(
            "<body>\n" +
            "  <script src=\"one.js\"></script> <!-- one -->\n" +
            "  <script src=\"two.js\"></script> <!-- two-->\n" +
            "</body>");
	}


	@Test
	@DisplayName("Tests script indent behavior - (indent_scripts = \"\"normal\"\")")
	void Tests_script_indent_behavior_indent_scripts_normal_() {
		opts.indent_scripts = IndentScripts.normal;
		test_fragment(
            "<head>\n" +
            "<script>\n" +
            "if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "</script>\n" +
            "<style>\n" +
            ".selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>",
            //  -- output --
            "<head>\n" +
            "    <script>\n" +
            "        if (a == b) {\n" +
            "            test();\n" +
            "        }\n" +
            "    </script>\n" +
            "    <style>\n" +
            "        .selector {\n" +
            "            font-size: 12px;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>");
		test_fragment(
            "<body>\n" +
            "    <script src=\"one.js\"></script> <!-- one -->\n" +
            "    <script src=\"two.js\"></script> <!-- two-->\n" +
            "</body>");
	}

	@Test
	@DisplayName("Tests script indent behavior - (indent_scripts = \"\"keep\"\")")
	void Tests_script_indent_behavior_indent_scripts_keep_() {
		opts.indent_scripts = IndentScripts.keep;
		test_fragment(
            "<head>\n" +
            "<script>\n" +
            "if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "</script>\n" +
            "<style>\n" +
            ".selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>",
            //  -- output --
            "<head>\n" +
            "    <script>\n" +
            "    if (a == b) {\n" +
            "        test();\n" +
            "    }\n" +
            "    </script>\n" +
            "    <style>\n" +
            "    .selector {\n" +
            "        font-size: 12px;\n" +
            "    }\n" +
            "    </style>\n" +
            "</head>");
		test_fragment(
            "<body>\n" +
            "    <script src=\"one.js\"></script> <!-- one -->\n" +
            "    <script src=\"two.js\"></script> <!-- two-->\n" +
            "</body>");
	}

	@Test
	@DisplayName("Tests script indent behavior - (indent_scripts = \"\"separate\"\")")
	void Tests_script_indent_behavior_indent_scripts_separate_() {
		opts.indent_scripts = IndentScripts.separate;
		test_fragment(
            "<head>\n" +
            "<script>\n" +
            "if (a == b) {\n" +
            "test();\n" +
            "}\n" +
            "</script>\n" +
            "<style>\n" +
            ".selector {\n" +
            "font-size: 12px;\n" +
            "}\n" +
            "</style>\n" +
            "</head>",
            //  -- output --
            "<head>\n" +
            "    <script>\n" +
            "if (a == b) {\n" +
            "    test();\n" +
            "}\n" +
            "    </script>\n" +
            "    <style>\n" +
            ".selector {\n" +
            "    font-size: 12px;\n" +
            "}\n" +
            "    </style>\n" +
            "</head>");
		test_fragment(
            "<body>\n" +
            "    <script src=\"one.js\"></script> <!-- one -->\n" +
            "    <script src=\"two.js\"></script> <!-- two-->\n" +
            "</body>");
	}


	@Test
	@DisplayName("ASP(X) and JSP directives <%@ indent formatting")
	void ASP_X_and_JSP_directives_indent_formatting() {
		bth(
            "<%@Master language=\"C#\"%>\n" +
            "<%@Register TagPrefix=\"a\" Namespace=\"a\" Assembly=\"a\"%>\n" +
            "<%@Register TagPrefix=\"b\" Namespace=\"a\" Assembly=\"a\"%>\n" +
            "<%@Register TagPrefix=\"c\" Namespace=\"a\" Assembly=\"a\"%>\n" +
            "<!DOCTYPE html>\n" +
            "\n" +
            "<html>\n" +
            "\n" +
            "<some-content />\n" +
            "\n" +
            "</html>");
	}


	@Test
	@DisplayName("Issue #1027 -- Formatting SVG files")
	void Issue_1027_Formatting_SVG_files() {
		bth(
            "<svg version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\"\n" +
            "     viewBox=\"0 0 36 36\" style=\"enable-background:new 0 0 36 36;\" xml:space=\"preserve\">\n" +
            "                    <rect id=\"XMLID_20_\" x=\"-7\"\n" +
            "                          class=\"st0\"\n" +
            "                          width=\"49\" height=\"36\"/>\n" +
            "</svg>",
            //  -- output --
            "<svg version=\"1.1\" id=\"Layer_1\" xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" x=\"0px\" y=\"0px\" viewBox=\"0 0 36 36\" style=\"enable-background:new 0 0 36 36;\" xml:space=\"preserve\">\n" +
            "    <rect id=\"XMLID_20_\" x=\"-7\" class=\"st0\" width=\"49\" height=\"36\" />\n" +
            "</svg>");
	}


	@Test
	@DisplayName("Linewrap length")
	void Linewrap_length() {
		opts.wrap_line_length = 80;
		
        // This test shows how line wrapping is still not correct.
        test_fragment(
            "<body>\n" +
            "    <div>\n" +
            "        <div>\n" +
            "            <p>Reconstruct the schematic editor the EDA system <a href=\"http://www.jedat.co.jp/eng/products.html\"><i>AlphaSX</i></a> series</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>",
            //  -- output --
            "<body>\n" +
            "    <div>\n" +
            "        <div>\n" +
            "            <p>Reconstruct the schematic editor the EDA system <a\n" +
            "                    href=\"http://www.jedat.co.jp/eng/products.html\"><i>AlphaSX</i></a>\n" +
            "                series</p>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "</body>");
		test_fragment(
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014 0015 0016 0017 0018 0019 0020</span>",
            //  -- output --
            "<span>0 0001 0002 0003 0004 0005 0006 0007 0008 0009 0010 0011 0012 0013 0014\n" +
            "    0015 0016 0017 0018 0019 0020</span>");
		test_fragment("<div>----1---------2---------3---------4---------5---------6---------7----</div>");
		bth("<span>----1---------2---------3---------4---------5---------6---------7----</span>");
		bth("<span>----1---------2---------3---------4---------5---------6---------7----<br /></span>");
		bth(
            "<div>----1---------2---------3---------4---------5---------6---------7-----</div>",
            //  -- output --
            "<div>----1---------2---------3---------4---------5---------6---------7-----\n" +
            "</div>");
		test_fragment(
            "<div>----1---------2---------3---------4---------5---------6---------7-----<br /></div>",
            //  -- output --
            "<div>\n" +
            "    ----1---------2---------3---------4---------5---------6---------7-----<br />\n" +
            "</div>");
		test_fragment(
            "<div>----1---------2---------3---------4---------5---------6---------7-----<hr /></div>",
            //  -- output --
            "<div>----1---------2---------3---------4---------5---------6---------7-----\n" +
            "    <hr />\n" +
            "</div>");
		test_fragment(
            "<div>----1---------2---------3---------4---------5---------6---------7-----<hr />-</div>",
            //  -- output --
            "<div>----1---------2---------3---------4---------5---------6---------7-----\n" +
            "    <hr />-\n" +
            "</div>");
		bth(
            "<div>----1---------2---------3---------4---------5---------6---------7 --------81 ----2---------3---------4---------5---------6---------7-----</div>",
            //  -- output --
            "<div>----1---------2---------3---------4---------5---------6---------7\n" +
            "    --------81 ----2---------3---------4---------5---------6---------7-----\n" +
            "</div>");
		bth(
            "<span>---1---------2---------3---------4---------5---------6---------7 --------81 ----2---------3---------4---------5---------6</span>",
            //  -- output --
            "<span>---1---------2---------3---------4---------5---------6---------7\n" +
            "    --------81 ----2---------3---------4---------5---------6</span>");
		bth(
            "<p>---------1---------2---------3---------4 ---------1---------2---------3---------4</p>",
            //  -- output --
            "<p>---------1---------2---------3---------4\n" +
            "    ---------1---------2---------3---------4</p>");
		bth(
            "<div>----1---------2---------3---------4---------5---------6---------7 --------81 ----2---------3---------4---------5---------6</div>",
            //  -- output --
            "<div>----1---------2---------3---------4---------5---------6---------7\n" +
            "    --------81 ----2---------3---------4---------5---------6</div>");
		bth(
            "<div>----1---------2---------3---------4---------5---------6---------7 --------81 ----2---------3---------4---------5---------6<br /></div>",
            //  -- output --
            "<div>----1---------2---------3---------4---------5---------6---------7\n" +
            "    --------81 ----2---------3---------4---------5---------6<br /></div>");
		bth(
            "<div>----1---------2---------3---------4---------5---------6---------7 --------81 ----2---------3---------4---------5---------6<hr /></div>",
            //  -- output --
            "<div>----1---------2---------3---------4---------5---------6---------7\n" +
            "    --------81 ----2---------3---------4---------5---------6\n" +
            "    <hr />\n" +
            "</div>");
		
        // #1238  Fixed
        bth(
            "<span uib-tooltip=\"[[firstName]] [[lastName]]\" tooltip-enable=\"showToolTip\">\n" +
            "   <ng-letter-avatar charCount=\"2\" data=\"[[data]]\"\n" +
            "        shape=\"round\" fontsize=\"[[font]]\" height=\"[[height]]\" width=\"[[width]]\"\n" +
            "   avatarcustombgcolor=\"[[bgColor]]\" dynamic=\"true\"></ng-letter-avatar>\n" +
            "     </span>",
            //  -- output --
            "<span uib-tooltip=\"[[firstName]] [[lastName]]\" tooltip-enable=\"showToolTip\">\n" +
            "    <ng-letter-avatar charCount=\"2\" data=\"[[data]]\" shape=\"round\"\n" +
            "        fontsize=\"[[font]]\" height=\"[[height]]\" width=\"[[width]]\"\n" +
            "        avatarcustombgcolor=\"[[bgColor]]\" dynamic=\"true\"></ng-letter-avatar>\n" +
            "</span>");
		
        // Issue #1122
        test_fragment(
            "<div>\n" +
            "<div>\n" +
            "<p>\n" +
            "    В РАБОЧЕМ РЕЖИМЕ, после ввода параметров опыта (номер, шаг отсчетов и глубина зондирования), текущие\n" +
            "    отсчеты сохраняются в контроллере при нажатии кнопки «ПУСК». Одновременно, они распечатываются\n" +
            "    на минипринтере. Управлять контроллером для записи данных зондирования можно при помощи <link_row to=\"РК.05.01.01\">Радиокнопки РК-11</link_row>.\n" +
            "</p>\n" +
            "</div>\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <div>\n" +
            "        <p>\n" +
            "            В РАБОЧЕМ РЕЖИМЕ, после ввода параметров опыта (номер, шаг отсчетов\n" +
            "            и глубина зондирования), текущие\n" +
            "            отсчеты сохраняются в контроллере при нажатии кнопки «ПУСК».\n" +
            "            Одновременно, они распечатываются\n" +
            "            на минипринтере. Управлять контроллером для записи данных\n" +
            "            зондирования можно при помощи <link_row to=\"РК.05.01.01\">Радиокнопки\n" +
            "                РК-11</link_row>.\n" +
            "        </p>\n" +
            "    </div>\n" +
            "</div>");
		
        // Issue #1122
        test_fragment(
            "<div>\n" +
            "<div>\n" +
            "<p>\n" +
            "    В РАБОЧЕМ РЕЖИМЕ, после ввода параметров опыта (номер, шаг отсчетов и глубина зондирования), текущие отсчеты сохраняются в контроллере при нажатии кнопки «ПУСК». Одновременно, они распечатываются на минипринтере. Управлять контроллером для записи данных зондирования можно при помощи <link_row to=\"РК.05.01.01\">Радиокнопки РК-11</link_row>.\n" +
            "</p>\n" +
            "</div>\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <div>\n" +
            "        <p>\n" +
            "            В РАБОЧЕМ РЕЖИМЕ, после ввода параметров опыта (номер, шаг отсчетов\n" +
            "            и глубина зондирования), текущие отсчеты сохраняются в контроллере\n" +
            "            при нажатии кнопки «ПУСК». Одновременно, они распечатываются на\n" +
            "            минипринтере. Управлять контроллером для записи данных зондирования\n" +
            "            можно при помощи <link_row to=\"РК.05.01.01\">Радиокнопки РК-11\n" +
            "            </link_row>.\n" +
            "        </p>\n" +
            "    </div>\n" +
            "</div>");
		
        // #607 - preserve-newlines makes this look a bit odd now, but it much better
        test_fragment(
            "<p>В РАБОЧЕМ РЕЖИМЕ, после ввода параметров опыта (номер, шаг отсчетов и глубина зондирования), текущие\n" +
            "    отсчеты сохраняются в контроллере при нажатии кнопки «ПУСК». Одновременно, они распечатываются\n" +
            "    на минипринтере. Управлять контроллером для записи данных зондирования можно при помощи <link_row to=\"РК.05.01.01\">Радиокнопки РК-11</link_row>.</p>",
            //  -- output --
            "<p>В РАБОЧЕМ РЕЖИМЕ, после ввода параметров опыта (номер, шаг отсчетов и глубина\n" +
            "    зондирования), текущие\n" +
            "    отсчеты сохраняются в контроллере при нажатии кнопки «ПУСК». Одновременно,\n" +
            "    они распечатываются\n" +
            "    на минипринтере. Управлять контроллером для записи данных зондирования можно\n" +
            "    при помощи <link_row to=\"РК.05.01.01\">Радиокнопки РК-11</link_row>.</p>");
	}


	@Test
	@DisplayName("Indent with tabs")
	void Indent_with_tabs() {
		opts.indent_with_tabs = true;
		test_fragment(
            "<div>\n" +
            "<div>\n" +
            "</div>\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "\t<div>\n" +
            "\t</div>\n" +
            "</div>");
	}


	@Test
	@DisplayName("Indent without tabs")
	void Indent_without_tabs() {
		opts.indent_with_tabs = false;
		test_fragment(
            "<div>\n" +
            "<div>\n" +
            "</div>\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    <div>\n" +
            "    </div>\n" +
            "</div>");
	}


	@Test
	@DisplayName("Do not indent html inner html by default")
	void Do_not_indent_html_inner_html_by_default() {
		test_fragment(
            "<html>\n" +
            "<body>\n" +
            "<div></div>\n" +
            "</body>\n" +
            "\n" +
            "</html>",
            //  -- output --
            "<html>\n" +
            "<body>\n" +
            "    <div></div>\n" +
            "</body>\n" +
            "\n" +
            "</html>");
	}


	@Test
	@DisplayName("indent_inner_html set to true indents html inner html")
	void indent_inner_html_set_to_true_indents_html_inner_html() {
		opts.indent_inner_html = true;
		test_fragment(
            "<html>\n" +
            "    <body>\n" +
            "        <div></div>\n" +
            "    </body>\n" +
            "\n" +
            "</html>");
	}


	@Test
	@DisplayName("Indent body inner html by default")
	void Indent_body_inner_html_by_default() {
		test_fragment(
            "<html>\n" +
            "<body>\n" +
            "<div></div>\n" +
            "</body>\n" +
            "\n" +
            "</html>",
            //  -- output --
            "<html>\n" +
            "<body>\n" +
            "    <div></div>\n" +
            "</body>\n" +
            "\n" +
            "</html>");
	}


	@Test
	@DisplayName("indent_body_inner_html set to false prevents indent of body inner html")
	void indent_body_inner_html_set_to_false_prevents_indent_of_body_inner_html() {
		opts.indent_body_inner_html = false;
		test_fragment(
            "<html>\n" +
            "<body>\n" +
            "<div></div>\n" +
            "</body>\n" +
            "\n" +
            "</html>");
	}


	@Test
	@DisplayName("Indent head inner html by default")
	void Indent_head_inner_html_by_default() {
		test_fragment(
            "<html>\n" +
            "\n" +
            "<head>\n" +
            "<meta>\n" +
            "</head>\n" +
            "\n" +
            "</html>",
            //  -- output --
            "<html>\n" +
            "\n" +
            "<head>\n" +
            "    <meta>\n" +
            "</head>\n" +
            "\n" +
            "</html>");
	}


	@Test
	@DisplayName("indent_head_inner_html set to false prevents indent of head inner html")
	void indent_head_inner_html_set_to_false_prevents_indent_of_head_inner_html() {
		opts.indent_head_inner_html = false;
		test_fragment(
            "<html>\n" +
            "\n" +
            "<head>\n" +
            "<meta>\n" +
            "</head>\n" +
            "\n" +
            "</html>");
	}


	@Test
	@DisplayName("Inline tags formatting")
	void Inline_tags_formatting() {
		bth(
            "<div><span></span></div><span><div></div></span>",
            //  -- output --
            "<div><span></span></div><span>\n" +
            "    <div></div>\n" +
            "</span>");
		bth(
            "<div><div><span><span>Nested spans</span></span></div></div>",
            //  -- output --
            "<div>\n" +
            "    <div><span><span>Nested spans</span></span></div>\n" +
            "</div>");
		bth(
            "<p>Should remove <span><span \n" +
            "\n" +
            "class=\"some-class\">attribute</span></span> newlines</p>",
            //  -- output --
            "<p>Should remove <span><span class=\"some-class\">attribute</span></span> newlines</p>");
		bth("<div><span>All</span> on <span>one</span> line</div>");
		bth("<span class=\"{{class_name}}\">{{content}}</span>");
		bth("{{#if 1}}<span>{{content}}</span>{{/if}}");
	}


	@Test
	@DisplayName("Preserve newlines false")
	void Preserve_newlines_false() {
		opts.indent_size = 2;
		opts.preserve_newlines = false;
		bth(
            "<div>\n" +
            "\tfoo\n" +
            "</div>",
            //  -- output --
            "<div> foo </div>");
		bth(
            "<div>Should not</div>\n" +
            "\n" +
            "\n" +
            "<div>preserve newlines</div>",
            //  -- output --
            "<div>Should not</div>\n" +
            "<div>preserve newlines</div>");
		bth(
            "<header>\n" +
            "  <h1>\n" +
            "\n" +
            "\n" +
            "    <ul>\n" +
            "\n" +
            "      <li class=\"menuactive menuparent\">\n" +
            "        <a>\n" +
            "          <span>Anita Koppe</span>\n" +
            "        </a>\n" +
            "\n" +
            "\n" +
            "      </li>\n" +
            "    </ul>\n" +
            "  </h1>\n" +
            "</header>",
            //  -- output --
            "<header>\n" +
            "  <h1>\n" +
            "    <ul>\n" +
            "      <li class=\"menuactive menuparent\">\n" +
            "        <a>\n" +
            "          <span>Anita Koppe</span>\n" +
            "        </a>\n" +
            "      </li>\n" +
            "    </ul>\n" +
            "  </h1>\n" +
            "</header>");
	}


	@Test
	@DisplayName("Preserve newlines true")
	void Preserve_newlines_true() {
		opts.indent_size = 1;
		opts.indent_char = "	";
		opts.preserve_newlines = true;
		test_fragment(
            "<div>\n" +
            "\tfoo\n" +
            "</div>");
	}


	@Test
	@DisplayName("Preserve newlines true with zero max newline")
	void Preserve_newlines_true_with_zero_max_newline() {
		opts.preserve_newlines = true;
		opts.max_preserve_newlines = 0;
		opts.indent_size = 2;
		bth(
            "<div>Should</div>\n" +
            "\n" +
            "\n" +
            "<div>preserve zero newlines</div>",
            //  -- output --
            "<div>Should</div>\n" +
            "<div>preserve zero newlines</div>");
		bth(
            "<header>\n" +
            "  <h1>\n" +
            "\n" +
            "\n" +
            "    <ul>\n" +
            "\n" +
            "      <li class=\"menuactive menuparent\">\n" +
            "        <a>\n" +
            "          <span>Anita Koppe</span>\n" +
            "        </a>\n" +
            "\n" +
            "\n" +
            "      </li>\n" +
            "    </ul>\n" +
            "  </h1>\n" +
            "</header>",
            //  -- output --
            "<header>\n" +
            "  <h1>\n" +
            "    <ul>\n" +
            "      <li class=\"menuactive menuparent\">\n" +
            "        <a>\n" +
            "          <span>Anita Koppe</span>\n" +
            "        </a>\n" +
            "      </li>\n" +
            "    </ul>\n" +
            "  </h1>\n" +
            "</header>");
	}


	@Test
	@DisplayName("Preserve newlines true with 1 max newline")
	void Preserve_newlines_true_with_1_max_newline() {
		opts.preserve_newlines = true;
		opts.indent_size = 2;
		opts.max_preserve_newlines = 1;
		bth(
            "<div>Should</div>\n" +
            "\n" +
            "\n" +
            "<div>preserve one newline</div>",
            //  -- output --
            "<div>Should</div>\n" +
            "\n" +
            "<div>preserve one newline</div>");
		bth(
            "<header>\n" +
            "  <h1>\n" +
            "\n" +
            "\n" +
            "    <ul>\n" +
            "\n" +
            "      <li class=\"menuactive menuparent\">\n" +
            "        <a>\n" +
            "          <span>Anita Koppe</span>\n" +
            "        </a>\n" +
            "\n" +
            "\n" +
            "      </li>\n" +
            "    </ul>\n" +
            "  </h1>\n" +
            "</header>",
            //  -- output --
            "<header>\n" +
            "  <h1>\n" +
            "\n" +
            "    <ul>\n" +
            "\n" +
            "      <li class=\"menuactive menuparent\">\n" +
            "        <a>\n" +
            "          <span>Anita Koppe</span>\n" +
            "        </a>\n" +
            "\n" +
            "      </li>\n" +
            "    </ul>\n" +
            "  </h1>\n" +
            "</header>");
	}


	@Test
	@DisplayName("Preserve newlines true with null max newline")
	void Preserve_newlines_true_with_null_max_newline() {
		opts.preserve_newlines = true;
		opts.indent_size = 2;
		opts.max_preserve_newlines = 32786;
		bth(
            "<div>Should</div>\n" +
            "\n" +
            "\n" +
            "<div>preserve zero newlines</div>");
		bth(
            "<header>\n" +
            "  <h1>\n" +
            "\n" +
            "\n" +
            "    <ul>\n" +
            "\n" +
            "      <li class=\"menuactive menuparent\">\n" +
            "        <a>\n" +
            "          <span>Anita Koppe</span>\n" +
            "        </a>\n" +
            "\n" +
            "\n" +
            "      </li>\n" +
            "    </ul>\n" +
            "  </h1>\n" +
            "</header>");
	}


	@Test
	@DisplayName("unformatted to prevent formatting changes")
	void unformatted_to_prevent_formatting_changes() {
		opts.unformatted = new java.util.HashSet<>(java.util.Arrays.asList("h1", "br", "u", "span", "textarea"));
		bth("<u><div><div>Ignore block tags in unformatted regions</div></div></u>");
		bth("<div><u>Do not wrap unformatted regions with extra newlines</u></div>");
		bth(
            "<div>\n" +
            "<u>Do not wrap unformatted regions with extra newlines</u></div>",
            //  -- output --
            "<div>\n" +
            "    <u>Do not wrap unformatted regions with extra newlines</u>\n" +
            "</div>");
		bth("<div><br /></div>");
		bth(
            "<div>\n" +
            "<br /></div>",
            //  -- output --
            "<div>\n" +
            "    <br />\n" +
            "</div>");
		bth("<div><h1 /></div>");
		bth(
            "<div>\n" +
            "<h1 /></div>",
            //  -- output --
            "<div>\n" +
            "    <h1 />\n" +
            "</div>");
		bth("<label><br /></label>");
		
        // Inline parent should not add newline unlike block
        bth(
            "<label>\n" +
            "<br /></label>",
            //  -- output --
            "<label>\n" +
            "    <br /></label>");
		
        // Inline parent with unformatted non-inline child
        bth("<label><h1>Unformatted non-inline</h1></label>");
		
        // Inline parent with unformatted non-inline child
        bth(
            "<label>\n" +
            "<h1>Unformatted non-inline</h1></label>",
            //  -- output --
            "<label>\n" +
            "    <h1>Unformatted non-inline</h1></label>");
		
        // Inline parent with unformatted non-inline empty child
        bth("<label><h1 /></label>");
		
        // Inline parent with unformatted non-inline empty child
        bth(
            "<label>\n" +
            "<h1 /></label>",
            //  -- output --
            "<label>\n" +
            "    <h1 /></label>");
		bth(
            "<u>  \n" +
            "\n" +
            "\n" +
            "  Ignore extra \"\"\"whitespace mostly  \n" +
            "\n" +
            "\n" +
            "  </u>",
            //  -- output --
            "<u>\n" +
            "\n" +
            "\n" +
            "  Ignore extra \"\"\"whitespace mostly  \n" +
            "\n" +
            "\n" +
            "  </u>");
		bth(
            "<u><div \n" +
            "\t\n" +
            "class=\"\"\">Ignore whitespace in attributes\t</div></u>");
		
        // Regression test #1534 - interaction between unformatted, content_unformatted, and inline
        bth(
            "<div>\n" +
            "    <textarea></textarea>\n" +
            "    <textarea>\n" +
            "\n" +
            "</textarea>\n" +
            "    <span></span>\n" +
            "    <span>\n" +
            "\n" +
            "</span>\n" +
            "</div>");
		bth(
            "<u \n" +
            "\n" +
            "\t\t  class=\"\">Ignore whitespace\n" +
            "in\tattributes</u>",
            //  -- output --
            "<u\n" +
            "\n" +
            "\t\t  class=\"\">Ignore whitespace\n" +
            "in\tattributes</u>");
	}


	@Test
	@DisplayName("content_unformatted to prevent formatting content")
	void content_unformatted_to_prevent_formatting_content() {
		opts.content_unformatted = new java.util.HashSet<>(java.util.Arrays.asList("?php", "script", "style", "p", "span", "br", "meta", "textarea"));
		test_fragment(
            "<html><body><h1>A</h1><script>if(1){f();}</script><style>.a{display:none;}</style></body></html>",
            //  -- output --
            "<html>\n" +
            "<body>\n" +
            "    <h1>A</h1>\n" +
            "    <script>if(1){f();}</script>\n" +
            "    <style>.a{display:none;}</style>\n" +
            "</body>\n" +
            "\n" +
            "</html>");
		bth(
            "<div><p>Beautify me</p></div><p><div>But not me</div></p>",
            //  -- output --
            "<div>\n" +
            "    <p>Beautify me</p>\n" +
            "</div>\n" +
            "<p><div>But not me</div></p>");
		bth(
            "<div><p\n" +
            "  class=\"beauty-me\"\n" +
            ">Beautify me</p></div><p><div\n" +
            "  class=\"iamalreadybeauty\"\n" +
            ">But not me</div></p>",
            //  -- output --
            "<div>\n" +
            "    <p class=\"beauty-me\">Beautify me</p>\n" +
            "</div>\n" +
            "<p><div\n" +
            "  class=\"iamalreadybeauty\"\n" +
            ">But not me</div></p>");
		bth("<div><span>blabla<div>something here</div></span></div>");
		bth("<div><br /></div>");
		bth("<div><br></div>");
		bth(
            "<div>\n" +
            "<br>\n" +
            "<br />\n" +
            "<br></div>",
            //  -- output --
            "<div>\n" +
            "    <br>\n" +
            "    <br />\n" +
            "    <br>\n" +
            "</div>");
		
        // Regression test #1534 - interaction between unformatted, content_unformatted, and inline
        bth(
            "<div>\n" +
            "    <textarea></textarea>\n" +
            "    <textarea>\n" +
            "\n" +
            "</textarea>\n" +
            "    <span></span>\n" +
            "    <span>\n" +
            "\n" +
            "</span>\n" +
            "</div>");
		bth(
            "<div>\n" +
            "<meta>\n" +
            "<meta />\n" +
            "<meta></div>",
            //  -- output --
            "<div>\n" +
            "    <meta>\n" +
            "    <meta />\n" +
            "    <meta>\n" +
            "</div>");
		bth(
            "<div><pre>var a=1;\n" +
            "var b=a;</pre></div>",
            //  -- output --
            "<div>\n" +
            "    <pre>var a=1;\n" +
            "        var b=a;</pre>\n" +
            "</div>");
		bth(
            "<?php\n" +
            "/**\n" +
            " * Comment\n" +
            " */\n" +
            "\n" +
            "?>\n" +
            "<div class=\"\">\n" +
            "\n" +
            "</div>");
		bth(
            "<div><pre>\n" +
            "var a=1;\n" +
            "var b=a;\n" +
            "</pre></div>",
            //  -- output --
            "<div>\n" +
            "    <pre>\n" +
            "        var a=1;\n" +
            "        var b=a;\n" +
            "    </pre>\n" +
            "</div>");
	}


	@Test
	@DisplayName("default content_unformatted and inline element test")
	void default_content_unformatted_and_inline_element_test() {
		test_fragment(
            "<html><body><h1>A</h1><script>if(1){f();}</script><style>.a{display:none;}</style></body></html>",
            //  -- output --
            "<html>\n" +
            "<body>\n" +
            "    <h1>A</h1>\n" +
            "    <script>\n" +
            "        if (1) {\n" +
            "            f();\n" +
            "        }\n" +
            "    </script>\n" +
            "    <style>\n" +
            "        .a {\n" +
            "            display: none;\n" +
            "        }\n" +
            "    </style>\n" +
            "</body>\n" +
            "\n" +
            "</html>");
		bth(
            "<div><p>Beautify me</p></div><p><p>But not me</p></p>",
            //  -- output --
            "<div>\n" +
            "    <p>Beautify me</p>\n" +
            "</div>\n" +
            "<p>\n" +
            "<p>But not me</p>\n" +
            "</p>");
		bth(
            "<div><p\n" +
            "  class=\"beauty-me\"\n" +
            ">Beautify me</p></div><p><p\n" +
            "  class=\"iamalreadybeauty\"\n" +
            ">But not me</p></p>",
            //  -- output --
            "<div>\n" +
            "    <p class=\"beauty-me\">Beautify me</p>\n" +
            "</div>\n" +
            "<p>\n" +
            "<p class=\"iamalreadybeauty\">But not me</p>\n" +
            "</p>");
		bth("<div><span>blabla<div>something here</div></span></div>");
		bth("<div><br /></div>");
		
        // Regression test #1534 - interaction between unformatted, content_unformatted, and inline
        bth(
            "<div>\n" +
            "    <textarea></textarea>\n" +
            "    <textarea>\n" +
            "\n" +
            "</textarea>\n" +
            "    <span></span>\n" +
            "    <span>\n" +
            "\n" +
            "    </span>\n" +
            "</div>");
		bth(
            "<div><pre>var a=1;\n" +
            "var b=a;</pre></div>",
            //  -- output --
            "<div>\n" +
            "    <pre>var a=1;\n" +
            "var b=a;</pre>\n" +
            "</div>");
		bth(
            "<div><pre>\n" +
            "var a=1;\n" +
            "var b=a;\n" +
            "</pre></div>",
            //  -- output --
            "<div>\n" +
            "    <pre>\n" +
            "var a=1;\n" +
            "var b=a;\n" +
            "</pre>\n" +
            "</div>");
		
        // Test for #1041
        bth(
            "<p><span class=\"foo\">foo <span class=\"bar\">bar</span></span></p>\n" +
            "\n" +
            "<aside><p class=\"foo\">foo <span class=\"bar\">bar</span></p></aside>\n" +
            "<p class=\"foo\"><span class=\"bar\">bar</span></p>",
            //  -- output --
            "<p><span class=\"foo\">foo <span class=\"bar\">bar</span></span></p>\n" +
            "\n" +
            "<aside>\n" +
            "    <p class=\"foo\">foo <span class=\"bar\">bar</span></p>\n" +
            "</aside>\n" +
            "<p class=\"foo\"><span class=\"bar\">bar</span></p>");
		
        // Test for #869 - not exactly what the user wants but no longer horrible
        bth("<div><input type=\"checkbox\" id=\"j\" name=\"j\" value=\"foo\">&nbsp;<label for=\"j\">Foo</label></div>");
		
        // Test for #1167
        bth(
            "<span>\n" +
            "    <span><img src=\"images/off.svg\" alt=\"\"></span>\n" +
            "    <span><img src=\"images/on.svg\" alt=\"\"></span>\n" +
            "</span>");
		
        // Test for #882
        bth(
            "<tr><th><h3>Name</h3></th><td class=\"full-width\"></td></tr>",
            //  -- output --
            "<tr>\n" +
            "    <th>\n" +
            "        <h3>Name</h3>\n" +
            "    </th>\n" +
            "    <td class=\"full-width\"></td>\n" +
            "</tr>");
		
        // Test for #1184
        bth(
            "<div><div></div>Connect</div>",
            //  -- output --
            "<div>\n" +
            "    <div></div>Connect\n" +
            "</div>");
		
        // Test for #1383
        bth(
            "<p class=\"newListItem\">\n" +
            "  <svg height=\"40\" width=\"40\">\n" +
            "              <circle cx=\"20\" cy=\"20\" r=\"18\" stroke=\"black\" stroke-width=\"0\" fill=\"#bddffa\" />\n" +
            "              <text x=\"50%\" y=\"50%\" text-anchor=\"middle\" stroke=\"#1b97f3\" stroke-width=\"2px\" dy=\".3em\">1</text>\n" +
            "            </svg> This is a paragraph after an SVG shape.\n" +
            "</p>",
            //  -- output --
            "<p class=\"newListItem\">\n" +
            "    <svg height=\"40\" width=\"40\">\n" +
            "        <circle cx=\"20\" cy=\"20\" r=\"18\" stroke=\"black\" stroke-width=\"0\" fill=\"#bddffa\" />\n" +
            "        <text x=\"50%\" y=\"50%\" text-anchor=\"middle\" stroke=\"#1b97f3\" stroke-width=\"2px\" dy=\".3em\">1</text>\n" +
            "    </svg> This is a paragraph after an SVG shape.\n" +
            "</p>");
	}


	@Test
	@DisplayName("indent_empty_lines true")
	void indent_empty_lines_true() {
		opts.indent_empty_lines = true;
		test_fragment(
            "<div>\n" +
            "\n" +
            "    <div>\n" +
            "\n" +
            "    </div>\n" +
            "\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    \n" +
            "    <div>\n" +
            "        \n" +
            "    </div>\n" +
            "    \n" +
            "</div>");
	}


	@Test
	@DisplayName("indent_empty_lines false")
	void indent_empty_lines_false() {
		opts.indent_empty_lines = false;
		test_fragment(
            "<div>\n" +
            "\n" +
            "    <div>\n" +
            "\n" +
            "    </div>\n" +
            "\n" +
            "</div>");
	}


	@Test
	@DisplayName("Smarty tests for extra whitespace in nested quotes")
	void Smarty_tests_for_extra_whitespace_in_nested_quotes() {
		opts.templating = EnumSet.of(TemplateLanguage.valueOf("smarty"));
		bth("<div class=\"foo{if $bar==\"1\"} bar{/if}\">foo</div>");
		bth("<input type=\"radio\" name=\"foo\" {if $bar==\"\"}checked{/if}>");
	}


	@Test
	@DisplayName("Smarty tests for script")
	void Smarty_tests_for_script() {
		opts.templating = EnumSet.of(TemplateLanguage.valueOf("smarty"));
		bth(
            "<script>\n" +
            "    var foo = {$bar|json_encode};\n" +
            "</script>");
	}


	@Test
	@DisplayName("Recognize handlebars with whitespace control")
	void Recognize_handlebars_with_whitespace_control() {
		opts.indent_handlebars = true;
		bth(
            "{{#if true}}<div><div>\n" +
            "{{~#if true ~}}<p>true</p>{{/if}}\n" +
            "</div></div>{{/if}}",
            //  -- output --
            "{{#if true}}\n" +
            "    <div>\n" +
            "        <div>\n" +
            "            {{~#if true ~}}\n" +
            "                <p>true</p>\n" +
            "            {{/if}}\n" +
            "        </div>\n" +
            "    </div>\n" +
            "{{/if}}");
		bth(
            "{{~#*inline \"MyInlinePartial\"}}\n" +
            "{{MyIdentifier}}\n" +
            "{{/inline}}",
            //  -- output --
            "{{~#*inline \"MyInlinePartial\"}}\n" +
            "    {{MyIdentifier}}\n" +
            "{{/inline}}");
		bth(
            "{{~#> myPartial }}\n" +
            "<span>format correctly</span>\n" +
            "{{/myPartial}}",
            //  -- output --
            "{{~#> myPartial }}\n" +
            "    <span>format correctly</span>\n" +
            "{{/myPartial}}");
		bth(
            "{{#if callOn}}\n" +
            "    {{translate \"onText\"}}\n" +
            "{{~else if (eq callOn false)}}\n" +
            "    {{translate \"offText\"}}\n" +
            "{{/if}}");
		bth(
            "{{~#if callOn}}\n" +
            "    {{translate \"onText\"}}\n" +
            "{{~else if (eq callOn false)}}\n" +
            "    {{translate \"offText\"}}\n" +
            "{{/if}}");
	}


	@Test
	@DisplayName("Corrects Partial Behavior Involving Whitespace")
	void Corrects_Partial_Behavior_Involving_Whitespace() {
		bth(
            "{{#>row}}\n" +
            "    {{#>column}}\n" +
            "        <span>content</span>\n" +
            "        {{/column}}\n" +
            "        {{/row}}",
            //  -- output --
            "{{#>row}}\n" +
            "    {{#>column}}\n" +
            "        <span>content</span>\n" +
            "    {{/column}}\n" +
            "{{/row}}");
		bth(
            "{{~#>row}}\n" +
            "{{#>column}}\n" +
            "<p>content</p>\n" +
            "{{/column}}\n" +
            "{{/row}}",
            //  -- output --
            "{{~#>row}}\n" +
            "    {{#>column}}\n" +
            "        <p>content</p>\n" +
            "    {{/column}}\n" +
            "{{/row}}");
		bth(
            "{{#>row}}\n" +
            "    {{#>column}}\n" +
            "        <span>content</span>\n" +
            "    {{/column}}\n" +
            "{{/row}}");
		bth(
            "{{#> row}}\n" +
            "    {{#> column}}\n" +
            "        <span>content</span>\n" +
            "    {{/column}}\n" +
            "{{/row}}");
	}


	@Test
	@DisplayName("Does not add whitespace around custom elements ")
	void Does_not_add_whitespace_around_custom_elements_() {
		bth(
            "<span>\n" +
            "    <span>\n" +
            "        <span>The time for this result is 1:02</span\n" +
            "        ><div>.</div\n" +
            "        ><section>27</section>\n" +
            "    </span>\n" +
            "</span>",
            //  -- output --
            "<span>\n" +
            "    <span>\n" +
            "        <span>The time for this result is 1:02</span>\n" +
            "        <div>.</div>\n" +
            "        <section>27</section>\n" +
            "    </span>\n" +
            "</span>");
		bth(
            "<span>\n" +
            "    <span>\n" +
            "        <span>The time for this result is 1:02</span\n" +
            "        ><time-dot>.</time-dot\n" +
            "        ><time-decimals>27</time-decimals>\n" +
            "    </span>\n" +
            "</span>",
            //  -- output --
            "<span>\n" +
            "    <span>\n" +
            "        <span>The time for this result is 1:02</span><time-dot>.</time-dot><time-decimals>27</time-decimals>\n" +
            "    </span>\n" +
            "</span>");
	}


	@Test
	@DisplayName("Disables custom elements inlining with inline_custom_elements=false")
	void Disables_custom_elements_inlining_with_inline_custom_elements_false() {
		opts.inline_custom_elements = false;
		bth(
            "<span>\n" +
            "    <span>\n" +
            "        <span>The time for this result is 1:02</span\n" +
            "        ><time-dot>.</time-dot\n" +
            "        ><time-decimals>27</time-decimals>\n" +
            "    </span>\n" +
            "</span>",
            //  -- output --
            "<span>\n" +
            "    <span>\n" +
            "        <span>The time for this result is 1:02</span>\n" +
            "        <time-dot>.</time-dot>\n" +
            "        <time-decimals>27</time-decimals>\n" +
            "    </span>\n" +
            "</span>");
	}


	@Test
	@DisplayName("Indenting angular control flow with indent size 2")
	void Indenting_angular_control_flow_with_indent_size_2() {
		opts.templating = EnumSet.of(TemplateLanguage.valueOf("angular"));
		opts.indent_size = 2;
		bth(
            "@if (a > b) {\n" +
            "{{a}} is greater than {{b}}\n" +
            "}\n" +
            "\n" +
            "@if (a > b) {\n" +
            "{{a}} is greater than {{b}}\n" +
            "} @else if (b > a) {\n" +
            "{{a}} is less than {{b}}\n" +
            "} @else {\n" +
            "{{a}} is equal to {{b}}\n" +
            "}\n" +
            "\n" +
            "@for (item of items; track item.name) {\n" +
            "<li> {{ item.name }} </li>\n" +
            "} @empty {\n" +
            "<li> There are no items. </li>\n" +
            "}\n" +
            "\n" +
            "@switch (condition) {\n" +
            "@case (caseA) { \n" +
            "Case A.\n" +
            "}\n" +
            "@case (caseB) {\n" +
            "Case B.\n" +
            "}\n" +
            "@default {\n" +
            "Default case.\n" +
            "}\n" +
            "}",
            //  -- output --
            "@if (a > b) {\n" +
            "  {{a}} is greater than {{b}}\n" +
            "}\n" +
            "\n" +
            "@if (a > b) {\n" +
            "  {{a}} is greater than {{b}}\n" +
            "} @else if (b > a) {\n" +
            "  {{a}} is less than {{b}}\n" +
            "} @else {\n" +
            "  {{a}} is equal to {{b}}\n" +
            "}\n" +
            "\n" +
            "@for (item of items; track item.name) {\n" +
            "  <li> {{ item.name }} </li>\n" +
            "} @empty {\n" +
            "  <li> There are no items. </li>\n" +
            "}\n" +
            "\n" +
            "@switch (condition) {\n" +
            "  @case (caseA) {\n" +
            "    Case A.\n" +
            "  }\n" +
            "  @case (caseB) {\n" +
            "    Case B.\n" +
            "  }\n" +
            "  @default {\n" +
            "    Default case.\n" +
            "  }\n" +
            "}");
	}


	@Test
	@DisplayName("Indenting angular control flow with default indent size")
	void Indenting_angular_control_flow_with_default_indent_size() {
		opts.templating = EnumSet.of(TemplateLanguage.valueOf("angular"));
		bth(
            "@if (a > b) {\n" +
            "{{a}} is greater than {{b}}\n" +
            "}\n" +
            "\n" +
            "@if (a > b) {\n" +
            "{{a}} is greater than {{b}}\n" +
            "} @else if (b > a) {\n" +
            "{{a}} is less than {{b}}\n" +
            "} @else {\n" +
            "{{a}} is equal to {{b}}\n" +
            "}\n" +
            "\n" +
            "@for (item of items; track item.name) {\n" +
            "<li> {{ item.name }} </li>\n" +
            "} @empty {\n" +
            "<li> There are no items. </li>\n" +
            "}\n" +
            "\n" +
            "@switch (condition) {\n" +
            "@case (caseA) { \n" +
            "Case A.\n" +
            "}\n" +
            "@case (caseB) {\n" +
            "Case B.\n" +
            "}\n" +
            "@default {\n" +
            "Default case.\n" +
            "}\n" +
            "}",
            //  -- output --
            "@if (a > b) {\n" +
            "    {{a}} is greater than {{b}}\n" +
            "}\n" +
            "\n" +
            "@if (a > b) {\n" +
            "    {{a}} is greater than {{b}}\n" +
            "} @else if (b > a) {\n" +
            "    {{a}} is less than {{b}}\n" +
            "} @else {\n" +
            "    {{a}} is equal to {{b}}\n" +
            "}\n" +
            "\n" +
            "@for (item of items; track item.name) {\n" +
            "    <li> {{ item.name }} </li>\n" +
            "} @empty {\n" +
            "    <li> There are no items. </li>\n" +
            "}\n" +
            "\n" +
            "@switch (condition) {\n" +
            "    @case (caseA) {\n" +
            "        Case A.\n" +
            "    }\n" +
            "    @case (caseB) {\n" +
            "        Case B.\n" +
            "    }\n" +
            "    @default {\n" +
            "        Default case.\n" +
            "    }\n" +
            "}");
		bth(
            "@if (a > b) {\n" +
            "       {{a}} is greater than {{b}}\n" +
            "     }\n" +
            "\n" +
            "   @if (a > b) {\n" +
            " {{a}} is greater than {{b}}\n" +
            "     } @else if (b > a) {\n" +
            "       {{a}} is less than {{b}}\n" +
            "     } @else {\n" +
            "       {{a}} is equal to {{b}}\n" +
            "}\n" +
            "\n" +
            "   @for (item of items; track item.name) {\n" +
            "             <li> {{ item.name }} </li>\n" +
            "       } @empty {\n" +
            "     <li> There are no items. </li>\n" +
            "   }\n" +
            "\n" +
            " @switch (condition) {\n" +
            "@case (caseA) { \n" +
            "Case A.\n" +
            "       }\n" +
            "       @case (caseB) {\n" +
            "Case B.\n" +
            "   }\n" +
            " @default {\n" +
            "Default case.\n" +
            "}\n" +
            "     }",
            //  -- output --
            "@if (a > b) {\n" +
            "    {{a}} is greater than {{b}}\n" +
            "}\n" +
            "\n" +
            "@if (a > b) {\n" +
            "    {{a}} is greater than {{b}}\n" +
            "} @else if (b > a) {\n" +
            "    {{a}} is less than {{b}}\n" +
            "} @else {\n" +
            "    {{a}} is equal to {{b}}\n" +
            "}\n" +
            "\n" +
            "@for (item of items; track item.name) {\n" +
            "    <li> {{ item.name }} </li>\n" +
            "} @empty {\n" +
            "    <li> There are no items. </li>\n" +
            "}\n" +
            "\n" +
            "@switch (condition) {\n" +
            "    @case (caseA) {\n" +
            "        Case A.\n" +
            "    }\n" +
            "    @case (caseB) {\n" +
            "        Case B.\n" +
            "    }\n" +
            "    @default {\n" +
            "        Default case.\n" +
            "    }\n" +
            "}");
		bth(
            "@if( {value: true}; as val) {\n" +
            "<div>{{val.value}}</div>\n" +
            "}",
            //  -- output --
            "@if( {value: true}; as val) {\n" +
            "    <div>{{val.value}}</div>\n" +
            "}");
		bth(
            "@if( {value: true}; as val) {\n" +
            "<div>\n" +
            "@defer {\n" +
            "{{val.value}}\n" +
            "}\n" +
            "</div>\n" +
            "}",
            //  -- output --
            "@if( {value: true}; as val) {\n" +
            "    <div>\n" +
            "        @defer {\n" +
            "            {{val.value}}\n" +
            "        }\n" +
            "    </div>\n" +
            "}");
		bth("<div> @if(true) { {{\"{}\" + \" }\"}} } </div>");
		bth(
            "<div>\n" +
            "@for (item of items; track item.id; let idx = $index, e = $even) {\n" +
            "Item #{{ idx }}: {{ item.name }}\n" +
            "<p>\n" +
            "Item #{{ idx }}: {{ item.name }}\n" +
            "</p>\n" +
            "}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    @for (item of items; track item.id; let idx = $index, e = $even) {\n" +
            "        Item #{{ idx }}: {{ item.name }}\n" +
            "        <p>\n" +
            "            Item #{{ idx }}: {{ item.name }}\n" +
            "        </p>\n" +
            "    }\n" +
            "</div>");
		bth(
            "<div>\n" +
            "@for (item of items; track item.id; let idx = $index, e = $even) {\n" +
            "{{{value: true} | json}}\n" +
            "<p>\n" +
            "Item #{{ idx }}: {{ item.name }}\n" +
            "</p>\n" +
            "{{ {value: true} }}\n" +
            "<div>\n" +
            "@if(true) {\n" +
            "{{ {value: true} }}\n" +
            " }\n" +
            "\n" +
            "Placeholder\n" +
            "</div>\n" +
            "}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    @for (item of items; track item.id; let idx = $index, e = $even) {\n" +
            "        {{{value: true} | json}}\n" +
            "        <p>\n" +
            "            Item #{{ idx }}: {{ item.name }}\n" +
            "        </p>\n" +
            "        {{ {value: true} }}\n" +
            "        <div>\n" +
            "            @if(true) {\n" +
            "                {{ {value: true} }}\n" +
            "            }\n" +
            "\n" +
            "            Placeholder\n" +
            "        </div>\n" +
            "    }\n" +
            "</div>");
		
        // If no whitespace before @, then don't indent
        bth(
            "My email is loremipsum@if.com (only for work).\n" +
            "loremipsum@if {\n" +
            "<p>\n" +
            "Text\n" +
            "</p>\n" +
            "}",
            //  -- output --
            "My email is loremipsum@if.com (only for work).\n" +
            "loremipsum@if {\n" +
            "<p>\n" +
            "    Text\n" +
            "</p>\n" +
            "}");
		
        // Check if control flow is indented well if we have oneliners with no space before the closing token
        bth(
            "{{b}} @if (a > b) {is less than}@else{is greater than or equal to} {{a}}\n" +
            "<div>\n" +
            "Hello there\n" +
            "</div>\n" +
            "<div>\n" +
            "{{b}} @if (a > b) {is less than}@else{\n" +
            "is greater than or equal to} {{a}}\n" +
            "Hello there\n" +
            "</div>",
            //  -- output --
            "{{b}} @if (a > b) {is less than}@else{is greater than or equal to} {{a}}\n" +
            "<div>\n" +
            "    Hello there\n" +
            "</div>\n" +
            "<div>\n" +
            "    {{b}} @if (a > b) {is less than}@else{\n" +
            "        is greater than or equal to} {{a}}\n" +
            "    Hello there\n" +
            "</div>");
		
        // Multiline conditions should also be recognized and indented correctly
        bth(
            "@if(\n" +
            "condition1\n" +
            "&& condition2\n" +
            ") {\n" +
            "Text inside if\n" +
            "}",
            //  -- output --
            "@if(\n" +
            "condition1\n" +
            "&& condition2\n" +
            ") {\n" +
            "    Text inside if\n" +
            "}");
		
        // Indentation should work if opening brace is in new line
        bth(
            "@if( condition )\n" +
            "{\n" +
            "Text inside if\n" +
            "}",
            //  -- output --
            "@if( condition )\n" +
            "{\n" +
            "    Text inside if\n" +
            "}");
		
        // Indentation should work if condition is in new line
        bth(
            "@if\n" +
            "( condition )\n" +
            "{\n" +
            "Text inside if\n" +
            "} @else if\n" +
            "(condition2)\n" +
            "{\n" +
            "<div>\n" +
            "Text\n" +
            "</div>\n" +
            "}",
            //  -- output --
            "@if\n" +
            "( condition )\n" +
            "{\n" +
            "    Text inside if\n" +
            "} @else if\n" +
            "(condition2)\n" +
            "{\n" +
            "    <div>\n" +
            "        Text\n" +
            "    </div>\n" +
            "}");
		
        // CSS @media should remain unchanged
        bth(
            "<style type=\"text/css\">\n" +
            "    @media only screen and (min-width:480px) {\n" +
            "        .mj-column-per-100 {\n" +
            "            width: 100% !important;\n" +
            "            max-width: 100%;\n" +
            "        }\n" +
            "    }\n" +
            "</style>");
		
        // CSS @media, the inside of <script> tag and control flows should be indented correctly
        test_fragment(
            "<head>\n" +
            "<style type=\"text/css\">\n" +
            "@media only screen and (min-width:480px) {\n" +
            ".mj-column-per-100 {\n" +
            "width: 100% !important;\n" +
            "max-width: 100%;\n" +
            "}\n" +
            "}\n" +
            "</style>\n" +
            "<script>\n" +
            "if(someExpression) {\n" +
            "callFunc();\n" +
            "}\n" +
            "</script>\n" +
            "</head>\n" +
            "<body>\n" +
            "<div>\n" +
            "@if(someOtherExpression) {\n" +
            "Text\n" +
            "}\n" +
            "</div>\n" +
            "</body>",
            //  -- output --
            "<head>\n" +
            "    <style type=\"text/css\">\n" +
            "        @media only screen and (min-width:480px) {\n" +
            "            .mj-column-per-100 {\n" +
            "                width: 100% !important;\n" +
            "                max-width: 100%;\n" +
            "            }\n" +
            "        }\n" +
            "    </style>\n" +
            "    <script>\n" +
            "        if (someExpression) {\n" +
            "            callFunc();\n" +
            "        }\n" +
            "    </script>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div>\n" +
            "        @if(someOtherExpression) {\n" +
            "            Text\n" +
            "        }\n" +
            "    </div>\n" +
            "</body>");
	}


	@Test
	@DisplayName("No indenting for angular control flow should be done if angular templating is not set")
	void No_indenting_for_angular_control_flow_should_be_done_if_angular_templating_is_not_set() {
		bth(
            "@if (a > b) {\n" +
            "{{a}} is greater than {{b}}\n" +
            "}\n" +
            "\n" +
            "@if (a > b) {\n" +
            "{{a}} is greater than {{b}}\n" +
            "} @else if (b > a) {\n" +
            "{{a}} is less than {{b}}\n" +
            "} @else {\n" +
            "{{a}} is equal to {{b}}\n" +
            "}\n" +
            "\n" +
            "@for (item of items; track item.name) {\n" +
            "<li> {{ item.name }} </li>\n" +
            "} @empty {\n" +
            "<li> There are no items. </li>\n" +
            "}\n" +
            "\n" +
            "@switch (condition) {\n" +
            "@case (caseA) {\n" +
            "Case A.\n" +
            "}\n" +
            "@case (caseB) {\n" +
            "Case B.\n" +
            "}\n" +
            "@default {\n" +
            "Default case.\n" +
            "}\n" +
            "}");
		bth(
            "@if( {value: true}; as val) {\n" +
            "<div>{{val.value}}</div>\n" +
            "}");
		bth(
            "@if( {value: true}; as val) {\n" +
            "<div>\n" +
            "@defer {\n" +
            "{{val.value}}\n" +
            "}\n" +
            "</div>\n" +
            "}",
            //  -- output --
            "@if( {value: true}; as val) {\n" +
            "<div>\n" +
            "    @defer {\n" +
            "    {{val.value}}\n" +
            "    }\n" +
            "</div>\n" +
            "}");
		bth("<div> @if(true) { {{\"{}\" + \" }\"}} } </div>");
		bth(
            "<div>\n" +
            "@for (item of items; track item.id; let idx = $index, e = $even) {\n" +
            "Item #{{ idx }}: {{ item.name }}\n" +
            "<p>\n" +
            "Item #{{ idx }}: {{ item.name }}\n" +
            "</p>\n" +
            "}\n" +
            "</div>",
            //  -- output --
            "<div>\n" +
            "    @for (item of items; track item.id; let idx = $index, e = $even) {\n" +
            "    Item #{{ idx }}: {{ item.name }}\n" +
            "    <p>\n" +
            "        Item #{{ idx }}: {{ item.name }}\n" +
            "    </p>\n" +
            "    }\n" +
            "</div>");
		
        // CSS @media should remain unchanged
        bth(
            "<style type=\"text/css\">\n" +
            "    @media only screen and (min-width:480px) {\n" +
            "        .mj-column-per-100 {\n" +
            "            width: 100% !important;\n" +
            "            max-width: 100%;\n" +
            "        }\n" +
            "    }\n" +
            "</style>");
	}


	@Test
	@DisplayName("New Test Suite")
	void New_Test_Suite() {
	}



	@Test
	void beautifier_unconverted_tests_null() {
		test_fragment(null, "");
	}

	@Test
	void beautifier_unconverted_tests_end_with_newline() {
		opts.end_with_newline = true;

		test_fragment("", "\n");
		test_fragment("<div></div>\n");
		test_fragment("<div></div>\n\n\n", "<div></div>\n");
		test_fragment("<head>\n" +
			"    <script>\n" +
			"        mocha.setup(\"bdd\");\n" +
			"\n" +
			"    </script>\n" +
			"</head>\n");
	}

	@Test
	void beautifier_unconverted_tests_error_cases() {
		// error cases need love too
		bth("<img title=\"Bad food!\" src=\"foo.jpg\" alt=\"Evil\" \">");
		bth("<!-- don't blow up if a comment is not complete"); // -->
	}

	@Test
	void beautifier_unconverted_tests_basic() {
		test_fragment(
			"<head>\n" +
			"    <script>\n" +
			"        mocha.setup(\"bdd\");\n" +
			"    </script>\n" +
			"</head>");

		test_fragment("<div></div>\n", "<div></div>");
		bth("<div></div>");
		bth("<div>content</div>");
		bth("<div><div></div></div>",
			"<div>\n" +
			"    <div></div>\n" +
			"</div>");
		bth("<div><div>content</div></div>",
			"<div>\n" +
			"    <div>content</div>\n" +
			"</div>");
		bth("<div>\n" +
			"    <span>content</span>\n" +
			"</div>");
		bth("<div>\n" +
			"</div>");
		bth("<div>\n" +
			"    content\n" +
			"</div>");
		bth("<div>\n" +
			"    </div>",
			"<div>\n" +
			"</div>");
		test_fragment("   <div>\n" +
			"    </div>",
			"   <div>\n" +
			"   </div>");
		bth("<div>\n" +
			"</div>\n" +
			"    <div>\n" +
			"    </div>",
			"<div>\n" +
			"</div>\n" +
			"<div>\n" +
			"</div>");
		test_fragment("   <div>\n" +
			"</div>",
			"   <div>\n" +
			"   </div>");
		bth("<div        >content</div>",
			"<div>content</div>");
		bth("<div     thinger=\"preserve  space  here\"   ></div  >",
			"<div thinger=\"preserve  space  here\"></div>");
		bth("content\n" +
			"    <div>\n" +
			"    </div>\n" +
			"content",
			"content\n" +
			"<div>\n" +
			"</div>\n" +
			"content");
		bth("<li>\n" +
			"    <div>\n" +
			"    </div>\n" +
			"</li>");
		bth("<li>\n" +
			"<div>\n" +
			"</div>\n" +
			"</li>",
			"<li>\n" +
			"    <div>\n" +
			"    </div>\n" +
			"</li>");
		bth("<li>\n" +
			"    content\n" +
			"</li>\n" +
			"<li>\n" +
			"    content\n" +
			"</li>");

		bth("<img>content");
		bth("<img> content");
		bth("<img>   content", "<img> content");

		bth("<img><img>content");
		bth("<img> <img>content");
		bth("<img>   <img>content", "<img> <img>content");

		bth("<img><b>content</b>");
		bth("<img> <b>content</b>");
		bth("<img>   <b>content</b>", "<img> <b>content</b>");

		bth("<div>content<img>content</div>");
		bth("<div> content <img> content</div>");
		bth("<div>    content <img>    content </div>",
			"<div> content <img> content </div>");
		bth("Text <a href=\"#\">Link</a> Text");
	}

	@Test
	void beautifier_unconverted_tests_content_unformatted() {
		opts.content_unformatted = new HashSet<>(Arrays.asList("script", "style"));
		bth("<script id=\"javascriptTemplate\" type=\"text/x-kendo-template\">\n" +
			"  <ul>\n" +
			"  # for (var i = 0; i < data.length; i++) { #\n" +
			"    <li>#= data[i] #</li>\n" +
			"  # } #\n" +
			"  </ul>\n" +
			"</script>");
		bth("<style>\n" +
			"  body {background-color:lightgrey}\n" +
			"  h1   {color:blue}\n" +
			"</style>");
	}

	@Test
	void beautifier_unconverted_tests_inline_custom_element() {
		opts.inline = Collections.singleton("custom-element");
		test_fragment("<div>should <custom-element>not</custom-element>" +
					  " insert newlines</div>",
					  "<div>should <custom-element>not</custom-element>" +
					  " insert newlines</div>");
	}

	@Test
	void beautifier_unconverted_tests_line_wrap() {
		bth("<div><span>content</span></div>");

		opts.wrap_line_length = 0;
		//...---------1---------2---------3---------4---------5---------6---------7
		//...1234567890123456789012345678901234567890123456789012345678901234567890
		bth("<div>Some text that should not wrap at all.</div>",
			/* expected */
			"<div>Some text that should not wrap at all.</div>");

		// A value of 0 means no max line length, and should not wrap.
		//...---------1---------2---------3---------4---------5---------6---------7---------8---------9--------10--------11--------12--------13--------14--------15--------16--------17--------18--------19--------20--------21--------22--------23--------24--------25--------26--------27--------28--------29
		//...12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
		bth("<div>Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all.</div>",
			/* expected */
			"<div>Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all.</div>");

		// A value of "0" means no max line length, and should not wrap
		//...---------1---------2---------3---------4---------5---------6---------7---------8---------9--------10--------11--------12--------13--------14--------15--------16--------17--------18--------19--------20--------21--------22--------23--------24--------25--------26--------27--------28--------29
		//...12345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890
		bth("<div>Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all.</div>",
			/* expected */
			"<div>Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all. Some text that should not wrap at all.</div>");

		opts.wrap_line_length = 40;
		//...---------1---------2---------3---------4---------5---------6---------7
		//...1234567890123456789012345678901234567890123456789012345678901234567890
		bth("<div>Some test text that should wrap_inside_this section here__.</div>",
			/* expected */
			"<div>Some test text that should\n" +
			"    wrap_inside_this section here__.\n" +
			"</div>");
	}

}
