# Rename & Detach Checklist — do this BEFORE first `runIde`

Apache 2.0 lets you fork freely, but you must (a) keep LICENSE.txt + NOTICE.txt
and copyright headers, and (b) not pass the fork off as a JetBrains product.

## 1. plugin.xml (`resources/META-INF/plugin.xml`)
- [x] Remove `allow-bundled-update="true"` from `<idea-plugin>` (only meaningful for bundled plugins)
- [x] `<id>` and `<name>`: Gradle patches these at build time from `build.gradle.kts`,
      but edit them in the file too so the IDE dev experience is coherent
- [x] `<vendor>JetBrains</vendor>` → you
- [x] `<description>`: rewrite; state clearly it's an independent fork of the
      open-source Subversion plugin (Apache 2.0), not affiliated with JetBrains
- [x] The `<content namespace="jetbrains">` block: third-party plugins can't use
      the `jetbrains` namespace. Try removing the block entirely; the
      `intellij.libraries.javax.activation` module dep is replaced by the explicit
      `javax.activation-api` Maven dep already in build.gradle.kts, so also delete
      `<module name="intellij.libraries.javax.activation"/>` from `<dependencies>`
- [x] KEEP `<module name="intellij.platform.vcs"/>`, `vcs.impl`, `vcs.impl.shared` —
      these platform module deps are what make the fork viable

## 2. The plugin-ID conflict (day-1 decision, important)
The official plugin's id is literally `Subversion`. Your fork uses a NEW id, so a
user could have BOTH installed → two VCSes registered under the name `svn` → breakage.

- [x] Keep the internal VCS name `"svn"` (SvnVcs.VCS_NAME) so existing
      `.idea/vcs.xml` mappings keep working
- [x] Add a startup `ProjectActivity` that checks
      `PluginManagerCore.getPlugin(PluginId.getId("Subversion"))` — if installed and
      enabled, show a notification: "SVN X replaces the bundled Subversion plugin —
      disable it" with a disable action
- [ ] Say the same thing in the Marketplace description, first line

## 3. Statistics (FUS)
The code depends on `intellij.platform.statistics`. Third-party plugins cannot
ship JetBrains FUS collectors — the Marketplace verifier will complain.
- [x] Grep for `CounterUsagesCollector` / `EventLogGroup` under `src/` and strip or
      no-op those classes + their registrations in plugin.xml

## 4. Housekeeping
- [ ] Keep copyright headers ("Copyright 2000-20xx JetBrains s.r.o. ... Apache 2.0")
      in copied files; add your own header only to NEW files
- [ ] Add a NOTICE line: "Contains code derived from IntelliJ IDEA Community Edition
      (svn4idea plugin), © JetBrains s.r.o., Apache License 2.0"
- [ ] Marketplace naming rule: name must not suggest an official JetBrains product —
      "SVN X", "Subversion Reloaded", "TortoiseIDEA"… fine; "JetBrains SVN" not fine

## 5. First run
```bash
./gradlew runIde          # sandbox IDE with the plugin
# make a scratch repo to test against:
svnadmin create /tmp/repo && svn co file:///tmp/repo /tmp/wc
./gradlew verifyPlugin    # binary-compat + internal-API report
./gradlew buildPlugin     # -> build/distributions/*.zip
```
Expect `verifyPlugin` to flag internal-API usages (the original code lives inside
the monorepo and doesn't care). Warnings are acceptable for v0; burn them down over time.

## Status (2026-10-27)
- Sources re-synced from intellij-community `idea/261.22158.277`; builds (`./gradlew buildPlugin`) with 0 errors on JDK 25 / Gradle 9.1.
- Plugin id set to `io.github.oussamasiharde.svnx` (change it before the first Marketplace upload if you prefer another — it is immutable after).
- FUS collector `SvnStatisticsCollector` deleted (it was the only source of verifier compatibility problems).
- Still open: Marketplace description first line, NOTICE line, burning down internal/deprecated API warnings.
