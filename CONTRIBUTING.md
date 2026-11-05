# Contributing to SVN X

SVN X is early. The most useful things right now are bug reports from real repositories and the open issues labelled `good first issue` and `help wanted`.

## Build and run

You need JDK 25 and an `svn` command line client on your PATH.

```bash
./gradlew runIde        # sandbox IDE with the plugin
./gradlew buildPlugin   # zip in build/distributions, install from disk
```

In the sandbox IDE, disable the official Subversion plugin. Both plugins register as `svn`, and SVN X warns you if both are enabled.

## Tests

The upstream test suite runs in a headless IDE against your `svn` client. The full suite takes about 30 minutes, so run one class while you work:

```bash
./gradlew test --tests '*.SvnAddTest'
```

## Pull requests

- Keep each pull request to one change, and say how you tested it: a test, or the steps you followed in the IDE.
- Put new behaviour on the class that already owns the concept, rather than in a new helper class.
- Match the style of the file you are editing.
- When you modify a file that came from upstream svn4idea, keep its JetBrains header and add the one-line modification notice under it (Apache 2.0, section 4).
- AI-assisted code is welcome if you have read every line and can explain it in review.

## Reporting a bug

Include your svn version (`svn --version --quiet`), your IDE version, your OS, what you did, and what happened.
