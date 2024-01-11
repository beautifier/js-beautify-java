# js-beautify Java

This is a Java port of [`js-beautify`](https://github.com/beautifier/js-beautify).

This port was last synchronized with `js-beautify` on January 12, 2024, to commit [`29b51e08d99edd7c894e0d8bf04b2ceb4d6b1cd1`](https://github.com/beautifier/js-beautify/commit/29b51e08d99edd7c894e0d8bf04b2ceb4d6b1cd1).

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
