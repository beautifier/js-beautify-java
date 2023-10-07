# js-beautify Java

This is a Java port of [`js-beautify`](https://github.com/beautify-web/js-beautify).

This port was last synchronized with `js-beautify` on October 7, 2023, to commit [`6cf678219105741393b077642e6caddc51143e9b`](https://github.com/beautify-web/js-beautify/commit/6cf678219105741393b077642e6caddc51143e9b).

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
