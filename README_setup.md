# Project Setup

This document captures the rationale behind the infrastructure decisions in this project so future sessions don't have to reconstruct it.

## Git Remotes

Two remotes, two purposes:

| Remote | Target | Purpose |
|--------|--------|---------|
| `origin` | Local Gitea instance | Daily work — agent commits, feature branches, all iteration |
| `github` | GitHub | Deliberate public pushes only |

The local Gitea instance is the primary remote. It is private and available on the local network, making it a safe target for rapid agent iteration: noisy work-in-progress commits, experimental branches, and incomplete states never appear publicly. The repo was imported into Gitea from GitHub once as a starting point; after that Gitea became the primary.

GitHub is a publish target, not a development remote. There is no automatic sync between the two — pushing to `github` is an explicit act taken when work is ready to be public.

**Never push to `github` without explicit instruction.**

## Beads Issue Tracking

Beads (`bd`) is the issue tracker. The `.beads/` scaffold is committed to the repo:

```
.beads/
  .gitignore       # excludes Dolt internals and runtime files
  config.yaml      # beads configuration
  hooks/           # git hooks installed by bd
  issues.jsonl     # source of truth — all issues as JSONL
  metadata.json    # project metadata
  README.md        # beads quickstart
```

`issues.jsonl` is the portable source of truth for issues. The Dolt database under `.beads/embeddeddolt/` is a local index built from it — binary, machine-specific, excluded by `.beads/.gitignore`. Committing the scaffold means issues sync across machines via normal git push/pull.

Beads was initialized with `--stealth` which added `.beads/` to `.git/info/exclude`. That entry was subsequently removed since the scaffold is committed — it was a remnant of the initial setup.

## Claude Code Integration

`.claude/` is in `.gitignore` — intentionally. This directory contains Claude Code-specific configuration (hooks, worktree state) that should not couple the repository to a particular IDE or LLM vendor.

**After cloning on a new machine:** run `bd setup claude --stealth` to install the SessionStart and PreCompact hooks that load beads context into Claude automatically.

## Development Approach

This project uses a vibe-coding workflow: the user describes changes conversationally, planning is ad-hoc and triggered by request rather than driven by a formal requirements/design/implementation pipeline.

Beads issues are the unit of work dispatch. The `CLAUDE.md` Work Commands section defines how issues are picked up and worked:

- **Bugs** — implement directly from the issue description
- **Features** — read the issue description and design fields, draft an implementation plan, spawn agents serially with build gates between steps

Work happens on feature branches (`<beads-id>-<short-slug>`). Agent chatter commits accumulate on the branch and are squashed before the branch is pushed.

Build verification always uses the Bash tool (not PowerShell — `gradlew.bat` has a classpath issue in PS):

```bash
./gradlew :app:compileDebugKotlin                          # always
./gradlew :engine:test :engine:jacocoTestReport            # when engine is touched
```

Engine coverage is enforced at 100% instruction and 100% branch — untested branches in the theory engine are silent wrong-note bugs.
