# SVN X — Roadmap

Goal: bring IntelliJ's SVN experience to the level of its Git integration.
Base: fork of JetBrains' open-source svn4idea (~56K LOC, Apache 2.0),
extracted from the intellij-community monorepo into this standalone Gradle build.

## Phase 0 — Extraction (make it build & run)
**Milestone: `./gradlew runIde` opens a sandbox IDE where checkout, commit,
update, diff and history all work against a scratch repo.**
- Fix whatever doesn't compile against IC 2026.1 outside the monorepo
  (expect a handful of internal-API rough edges, not hundreds)
- Tests wired: `./gradlew test` runs the upstream suite against a real svn client

## Phase 1 — Parity release
**Milestone: v0.x live on JetBrains Marketplace.**
- `verifyPlugin` clean enough, `signPlugin`, `publishPlugin`
- Marketplace listing: screenshots, and "disable the official Subversion plugin" as the
  first line of the description
- Verify the copyright headers
- Marketplace naming rule: the name must not suggest an official JetBrains product
- CI: GitHub Actions running build + verifier on push
- Value already: SVN is now an optional install, so the UX gap with Git is the story

## Phase 2 — The Git-level features (one per minor release)
1. **Revision Graph** — the flagship. A Log tool window with a visual graph like
   Git's. Data: `svn log --xml -g` + copy-from info to draw branch/merge edges
   (TortoiseSVN's revision graph as reference, but inside the IDE, clickable,
   diff-on-click). This is the single biggest "wow" gap today.
2. **Branch widget** — status-bar popup like Git's: list `^/branches` + `^/tags`,
   one-click Switch / Compare / Merge from / New branch. SVN branch ops exist in
   the old plugin but are buried three menus deep.
3. **Mergeinfo inspector** — visualize `svn:mergeinfo`: per branch, show
   merged / eligible / blocked revisions (`svn mergeinfo --show-revs`), with
   cherry-pick-style "integrate selected revisions".
4. **Commit UX** — partial commits backed by changelists, commit-message history,
   amend-message via `EditCommitMessage` surfaced properly.

## Phase 3 — Performance & trust
- `svn status` caching / background refresh on large working copies
- Auth UX (credential store integration polish)
- Verifier-warning burn-down, wider `sinceBuild` range if feasible

## Positioning
SVN is no longer bundled with IntelliJ IDEA: since 2026.1 it is installed from the
Marketplace. The official plugin still exists, so SVN X does not replace an abandoned
plugin. Its pitch is focus: Git-level UX for teams that are staying on SVN.
