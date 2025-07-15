# js-beautify Java

[![CI Tests](https://github.com/beautifier/js-beautify-java/actions/workflows/github-actions-build.yml/badge.svg)](https://github.com/beautifier/js-beautify-java/actions/workflows/github-actions-build.yml)

This is a Java port of [`js-beautify`](https://github.com/beautifier/js-beautify).

This port was last synchronized with `js-beautify` on July 15, 2025, to commit [`03e3cc02949b42970ab12c2bfbfb33c7bced8eba`](https://github.com/beautifier/js-beautify/commit/03e3cc02949b42970ab12c2bfbfb33c7bced8eba).

## Testing

Testing data has been copied from the `js-beautify` project into the [`test`](./test) folder,
with support for generating automated Java tests.

Recreate the generated tests by running the `generate-tests.js` script:

```shell
cd test
npm install
node generate-tests.js
cd ..
```

The Java tests can then be run using Maven:

```shell
mvn test
```

## Updating

This port follows `js-beautify` and is periodically synced with the latest changes in that repository, including
test case changes.

The method is to compare changes in the `js-beautify` repository between the last synced commit and HEAD and then applying the
changes to this repository. Note that the `tests.js` data files can simply be copied across.
