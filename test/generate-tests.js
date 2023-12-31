#!/usr/bin/env node

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

'use strict';

var fs = require('fs');
var mustache = require('mustache');
var path = require('path');

mustache.escape = function(text) {
  return text;
};

function generate_tests() {
  // javascript
  generate_test_files('javascript', 'bt', 'js/test/generated/beautify-javascript-tests.js', 'python/jsbeautifier/tests/generated/tests.py', 'src/test/java/io/beautifier/javascript/GeneratedTests.java');

  // css
  generate_test_files('css', 't', 'js/test/generated/beautify-css-tests.js', 'python/cssbeautifier/tests/generated/tests.py', 'src/test/java/io/beautifier/css/GeneratedTests.java');

  // html
  // no python html beautifier, so no tests
  generate_test_files('html', 'bth', 'js/test/generated/beautify-html-tests.js', '', 'src/test/java/io/beautifier/html/GeneratedTests.java');
}

function generate_test_files(data_folder, test_method, node_output, python_output, java_output) {
  var data_file_path, input_path, template_file_path;
  var test_data, template;

  input_path = path.resolve(__dirname, 'data', data_folder);
  data_file_path = path.resolve(input_path, 'tests.js');
  test_data = require(data_file_path).test_data;

  template_file_path = path.resolve(input_path, 'node.mustache');
  if (fs.existsSync(template_file_path)) {
    template = fs.readFileSync(template_file_path, { encoding: 'utf-8' });
    set_formatters(test_data, test_method, '// ', data_folder);
    set_generated_header(test_data, data_file_path, template_file_path);
    fs.mkdirSync(path.resolve(__dirname, '..', node_output, '..'), { recursive: true });
    fs.writeFileSync(path.resolve(__dirname, '..', node_output),
      mustache.render(template, test_data), { encoding: 'utf-8' });
  }

  if (python_output) {
    template_file_path = path.resolve(input_path, 'python.mustache');
    if (fs.existsSync(template_file_path)) {
      template = fs.readFileSync(template_file_path, { encoding: 'utf-8' });
      set_formatters(test_data, test_method, '# ', data_folder);
      set_generated_header(test_data, data_file_path, template_file_path);
      fs.mkdirSync(path.resolve(__dirname, '..', python_output, '..'), { recursive: true });
      fs.writeFileSync(path.resolve(__dirname, '..', python_output),
        mustache.render(template, test_data), { encoding: 'utf-8' });
    }
  }
  if (java_output) {
    template_file_path = path.resolve(input_path, 'java.mustache');
    if (fs.existsSync(template_file_path)) {
      template = fs.readFileSync(template_file_path, { encoding: 'utf-8' });
      set_formatters(test_data, test_method, '// ', data_folder, '"');
      set_generated_header(test_data, data_file_path, template_file_path);
      fs.mkdirSync(path.resolve(__dirname, '..', java_output, '..'), { recursive: true });
      fs.writeFileSync(path.resolve(__dirname, '..', java_output),
        mustache.render(template, test_data), { encoding: 'utf-8' });
    }
  }
}

function set_generated_header(data, data_file_path, template_file_path) {
  var relative_script_path = path.relative(process.cwd(), __filename).split(path.sep).join('/');
  var relative_data_file_path = path.relative(process.cwd(), data_file_path).split(path.sep).join('/');
  var relative_template_file_path = path.relative(process.cwd(), template_file_path).split(path.sep).join('/');

  data.header_text =
    '    AUTO-GENERATED. DO NOT MODIFY.\n' +
    '    Script: ' + relative_script_path + '\n' +
    '    Template: ' + relative_template_file_path + '\n' +
    '    Data: ' + relative_data_file_path;

}

function isStringOrArray(val) {
  return typeof val === 'string' || val instanceof Array;
}

function getTestString(val, quote) {
  val = val.split('\n');

  var result = "'" + val.join("\\n' +\n            '").replace(/\t/g, '\\t') + "'";
  result = result.replace(/' \+\n            ''$/, "'");

  if (quote === '"') {
    result = convertSingleToDoubleQuotes(result);
  }
  return result;
}

function set_formatters(data, test_method, comment_mark, mode, quote) {

  // utility mustache functions
  data.matrix_context_string = function() {
    var context = this;
    return function(text, render) {
      var outputs = [];
      // text is ignored for this
      if (context.options) {
        var item;
        for (var x = 0; x < context.options.length; x++) {
          item = context.options[x];
          outputs.push(item.name + ' = "' + item.value.replace(/\n/g, '\\n').replace(/\t/g, '\\t').replace(/[']/g, "\"") + '"');
        }
      }
      return render(outputs.join(', '));
    };
  };

  data.test_line = function() {
    return function(text, render) {
      var method_text = this.fragment ? 'test_fragment' : test_method;
      var comment = '';
      var before_input = method_text + '(';
      var input = null;
      var before_output = ', ';
      var output = null;

      // text is ignored for this.
      if (typeof this.comment === 'string') {
        this.comment = this.comment.split('\n');
      }

      if (this.comment instanceof Array) {
        comment = '\n        ' + comment_mark + this.comment.join('\n        ' + comment_mark) + '\n        ';
      }

      // input: the default field
      // input_: allow underscore for formatting alignment with "output"
      // unchanged: use "unchanged" instead of "input" if there is no output
      input = this.input || this.input_ || this.unchanged;
      if (input instanceof Array) {
        input = input.join('\n');
      }

      if (isStringOrArray(this.output)) {
        output = this.output;
        if (output instanceof Array) {
          output = output.join('\n');
        }
      }

      // Do all most error checking
      if (!(this.input !== null || this.input_ !== null || this.unchanged !== null)) {
        throw "Missing test input field (input, input_, or unchanged).";
      } else if ((this.input !== null && (this.input_ !== null || this.unchanged !== null)) &&
        (this.input_ === null || this.unchanged === null)) {
        throw "Only one test input field allowed (input, input_, or unchanged): " + input;
      } else if (output && isStringOrArray(this.unchanged)) {
        throw "Cannot specify 'output' with 'unchanged' test input: " + input;
      } else if (!output && !isStringOrArray(this.unchanged)) {
        throw "Neither 'output' nor 'unchanged' specified for test input: " + input;
      } else if (input === output) {
        // Raw input and output can be the same, just omit output.
        throw "Test strings are identical.  Omit 'output' and use 'unchanged': " + input;
      }

      input = getTestString(render(input), quote);

      if (output) {
        output = getTestString(render(output), quote);
      } else {
        output = '';
      }

      if (this.input_ || input.indexOf('\n') !== -1 || output.indexOf('\n') !== -1) {
        before_input = method_text + '(\n            ';
        before_output = ',\n            ' + comment_mark + ' -- output --\n            ';
      }
      if (output === '') {
        before_output = '';
      }

      // Rendered input and output can be the same, just omit output.
      if (output === input) {
        before_output = '';
        output = '';
      }
      return comment + before_input + input + before_output + output + ')';
    };
  };

  data.set_mustache_tags = function() {
    return function( /* text, render */ ) {
      if (this.template) {
        mustache.tags = this.template.split(' ');
      }
      return '';
    };
  };

  data.unset_mustache_tags = function() {
    return function( /* text , render */ ) {
      if (this.template) {
        mustache.tags = ['{{', '}}'];
      }
      return '';
    };
  };

  var generated_java_identifiers = new Set();

  data.java_identifier = function() {
    return function(text, render) {
      const saveMustacheTags = mustache.tags;
      mustache.tags = ['{{', '}}'];
      var identifier = render(text).replace(/[^a-zA-Z0-9_]+/g, '_').replace(/^[0-9]/, '_$0');
      mustache.tags = saveMustacheTags;
      
      if (!identifier) {
        identifier = "untitled";
      }
      
      if (!generated_java_identifiers.has(identifier)) {
        generated_java_identifiers.add(identifier);
        return identifier;
      }

      for (var counter = 1; true; counter++) {
        if (!generated_java_identifiers.has(identifier + counter)) {
          generated_java_identifiers.add(identifier + counter);
          return identifier + counter;
        }
      }
    };
  };
  
  data.java_escape_string = function() {
    return function(text, render) {
      const saveMustacheTags = mustache.tags;
      mustache.tags = ['{{', '}}'];
      var result = render(text);
      mustache.tags = saveMustacheTags;
      result = result.replace(/"/g, "\\\"");
      return result;
    };
  };

  data.java_opt = function() {
    return function(text, render) {
      const saveMustacheTags = mustache.tags;
      mustache.tags = ['{{', '}}'];
      var result = render(text);
      mustache.tags = saveMustacheTags;

      const match = result.match(/opts.([a-zA-Z0-9_]+) = (.*)/);
      const name = match[1];
      const value = match[2];
      if (name === 'brace_style') {
        const statements = [];
        const brace_style_split = value.replace(/["']/g, "").split(/\s*,\s*/);
        for (var bs = 0; bs < brace_style_split.length; bs++) {
          if (brace_style_split[bs] === 'preserve-inline') {
            statements.push('opts.brace_preserve_inline = true');
          } else if (brace_style_split[bs] === 'end-expand') {
            statements.push(`opts.brace_style = BraceStyle.endExpand`);
          } else if (brace_style_split[bs] === 'collapse-preserve-inline') {
            statements.push('opts.brace_preserve_inline = true');
            statements.push('opts.brace_style = BraceStyle.collapse');
          } else {
            statements.push(`opts.brace_style = BraceStyle.${brace_style_split[bs]}`)
          }
        }
        
        return statements.join('; ');
      } else if (name === 'operator_position') {
        if (value === "'before-newline'") {
          return 'opts.operator_position = OperatorPosition.beforeNewline';
        } else if (value === "'after-newline'") {
          return 'opts.operator_position = OperatorPosition.afterNewline';
        } else if (value === "'preserve-newline'") {
          return 'opts.operator_position = OperatorPosition.preserveNewline';
        } else {
          throw new Error(`Unsupported operator_position: ${value}`);
        }
      } else if (name === 'wrap_attributes') {
        if (value === "'auto'") {
          return 'opts.wrap_attributes = WrapAttributes.auto';
        } else if (value === "'force'") {
          return 'opts.wrap_attributes = WrapAttributes.force';
        } else if (value === "'force-aligned'") {
          return 'opts.wrap_attributes = WrapAttributes.forceAligned';
        } else if (value === "'force-expand-multiline'") {
          return 'opts.wrap_attributes = WrapAttributes.forceExpandMultiline';
        } else if (value === "'aligned-multiple'") {
          return 'opts.wrap_attributes = WrapAttributes.alignedMultiple';
        } else if (value === "'preserve'") {
          return 'opts.wrap_attributes = WrapAttributes.preserve';
        } else if (value === "'preserve-aligned'") {
          return 'opts.wrap_attributes = WrapAttributes.preserveAligned';
        } else {
          throw new Error(`Unsupported wrap_attributes: ${value}`);
        }
      } else if (name == 'indent_scripts') {
        if (value === "'normal'") {
          return 'opts.indent_scripts = IndentScripts.normal';
        } else if (value === "'keep'") {
          return 'opts.indent_scripts = IndentScripts.keep';
        } else if (value === "'separate'") {
          return 'opts.indent_scripts = IndentScripts.separate';
        } else {
          throw new Error(`Unsupported indent_scripts: ${value}`);
        }
      } else if (name === 'templating') {
        return `opts.${name} = EnumSet.of(${value.replace(/[\[\]]/g, '').split(/, ?/).map(t => `TemplateLanguage.valueOf(${convertSingleToDoubleQuotes(t)})`)})`
      } else if (name === 'extra_liners' || name === 'unformatted' || name == 'content_unformatted') {
        if (value === 'null') {
          return `opts.${name} = null`;
        } else if (value.match(/^'[^']*'$/)) {
          return `opts.${name} = new java.util.HashSet<>(java.util.Arrays.asList(${'"' + value.replace(/^'/, '').replace(/'$/, '').split(/[^a-zA-Z0-9_\/\-]+/).join('", "') + '"'}))`
        } else {
          return `opts.${name} = new java.util.HashSet<>(java.util.Arrays.asList(${convertSingleToDoubleQuotes(value.replace(/[\[\]]/g, ''))}))`
        }
      } else if (name === 'max_preserve_newlines') {
        return `opts.${name} = ${!value || value === 'null' ? 32786 : value}`
      } else if (name === 'js') {
        return `opts.js().apply(new JSONObject("${value}"))`
      } else if (name === 'css') {
        return `opts.css().apply(new JSONObject("${value}"))`
      } else if (name === 'html') {
        return `opts.html().apply(new JSONObject("${value}"))`
      } else {
        return `opts.${name} = ${convertSingleToDoubleQuotes(value)}`;
      }
    }
  }
}

function convertSingleToDoubleQuotes(str) {
  if (str.match(/^".*"$/)) {
    return str;
  }

  var result = str;
  result = result.replace(/"/g, "TEMP_REPLACED_DOUBLE_QUOTE");
  result = result.replace(/(?<!(^|[^\\])\\)'/g, '"');
  result = result.replace(/TEMP_REPLACED_DOUBLE_QUOTE/g, "\\\"");
  return result;
}

generate_tests();