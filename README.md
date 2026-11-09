# SVN X

![CI](https://github.com/OussamaSiharde/svn-x/actions/workflows/build.yml/badge.svg)

A Subversion plugin for IntelliJ IDEA, for teams that are staying on SVN. It starts from
JetBrains' open-source svn4idea plugin and builds the workflows Git users take for
granted, starting with branch handling.

**Status: early.** It runs in IntelliJ IDEA 2026.1 and is not on the Marketplace yet. See
[ROADMAP.md](ROADMAP.md) for what comes next.

## Why it exists

Plenty of teams can't move to Git soon: large histories, binary assets, audit rules, or
tooling built around SVN. Since IntelliJ IDEA 2026.1, Subversion support is no longer
bundled and is installed from the Marketplace. The official plugin still works, and SVN X
doesn't try to replace it for everyone. It focuses on daily comfort for people who live
in SVN, starting with branches, which are buried in menus today.

## Architecture

```mermaid
flowchart LR
    subgraph IDE["IntelliJ IDEA"]
        VCS["VCS platform<br/>changes, history, diff"]
        Bar["Status bar"]
    end
    subgraph Plugin["SVN X"]
        Core["svn4idea core<br/>Apache 2.0"]
        Widget["Branch widget"]
        Guard["Startup guard<br/>conflict with official plugin"]
    end
    Core <--> VCS
    Widget --> Bar
    Widget --> Core
    Core -->|"svn --xml"| CLI["svn client"]
    CLI --> Repo[("Repository")]
```

The plugin drives your own `svn` client and parses its XML output, so it behaves the
same as the command line you already use.

## Two bugs worth reading about

**Deleted folders hid their files.** Plain `svn status` lists only the root of a deleted
directory, not the files inside it, so those files were missing from the Changes view.

```mermaid
sequenceDiagram
    participant P as SVN X
    participant S as svn client
    P->>S: status --xml
    S-->>P: D child (children not listed)
    P->>S: status --verbose child
    S-->>P: D child/a.txt
    Note over P: Changes view is complete again
```

A verbose status on every call would be slow, so the plugin asks only about the deleted
directories it finds.

**Green tests, broken plugin.** 242 tests passed while the real IDE failed on every
`svn info` call. The plugin bundled its own JAXB, the IDE ships another, and Java treated
them as two different classes. Tests use a flat classpath, so they could not see it.
Only running the IDE did. The plugin now uses the IDE's copy. The same run exposed an
upstream call that ran `svn` on the UI thread at startup, which now runs in the background.

## Features

| Today | Next |
|---|---|
| Checkout, commit, update, diff, history, merge and conflicts from svn4idea | Branch popup: list branches and tags, switch in one click |
| File status in the project view, on a real IDE run | Revision graph tool window |
| `svn: <branch>` in the status bar, with a popup of branch actions | `svn:mergeinfo` inspector |
| Warning when the official plugin is also enabled | |

## Install

1. Disable the official Subversion plugin. Both register as `svn`, and SVN X warns you
   if both are enabled.
2. Make sure an `svn` command line client is on your PATH.
3. Build the zip (needs JDK 25): `./gradlew buildPlugin` writes `build/distributions/svnx-0.1.0.zip`.
4. In IntelliJ IDEA: Settings > Plugins > gear icon > Install Plugin from Disk, pick the
   zip, and restart.

## Build from source

```bash
./gradlew runIde        # sandbox IDE with the plugin
./gradlew test          # upstream test suite against your svn client
./gradlew buildPlugin   # plugin zip in build/distributions
```

Pull requests are welcome: see [CONTRIBUTING.md](CONTRIBUTING.md).

## Tests

`./gradlew test` runs the upstream suite in a headless IDE against a real `svn`
client. Run one class at a time with `--tests` if you want results quickly; the
full suite takes about 30 minutes.

Latest full run, 9 Nov 2026, svn 1.15.0 on macOS:

| Result | Tests |
|---|---|
| Passed | 245 |
| Skipped | 18 |
| Failed | 8 |

The 8 failures are in one area: a file edited inside a renamed or moved directory
can show as modified rather than moved in the Changes view. See `SvnRollbackTest`,
`SvnRenameTest.testRenameFileRenameDir` and
`SvnChangesCorrectlyRefreshedTest.testMoveDirEditFileAndAfterRevert`.

## Compatibility

Plugin Verifier reports Compatible for IntelliJ IDEA 2026.1 (261.27258) and 2026.2
(262.11879). It also flags internal and override-only API usage inherited from
upstream, which still has to be addressed before a Marketplace release.

## Known issues

- The branch popup groups existing actions; it doesn't list branches yet.
- A file edited inside a renamed directory can show as modified, not moved (the 8 test failures).
- Commit, history and merge pass their tests but have not been walked through by hand in the IDE.
- Creating a file in a working copy runs `svn info` on the UI thread (upstream code); the IDE logs it.
- Only the first working copy of a project is shown in the status bar.

## Not affiliated with JetBrains
This is an independent community fork. "Subversion" plugin code
© 2000–2025 JetBrains s.r.o. and contributors, Apache License 2.0.

