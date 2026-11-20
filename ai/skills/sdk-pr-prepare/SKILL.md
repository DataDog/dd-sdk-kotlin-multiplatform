---
name: sdk-pr-prepare
description: >-
  Pre-PR preparation flow for the dd-sdk-kotlin-multiplatform project — verifies branch naming, runs local CI checks (compile, tests, static analysis) and regenerates checked-in generated files so CI passes on the first push. Accepts optional combinable arguments: lint, test, compile, api, dep, local. Use when the user asks to "prepare PR", "get ready for PR", "run pre-PR checks", or invokes /sdk-pr-prepare before opening/pushing a pull request.
---

# sdk-pr-prepare — dd-sdk-kotlin-multiplatform PR Preparation

Run the steps below **in order**. Steps 3 and 6 say how to fix failures and retry; on any other failing step, surface the error and stop until the user resolves it.

Apple targets (iOS, tvOS) compile and test only on macOS with Xcode. On another OS, skip the commands marked *macOS only* and report them as not run; CI runs them on macOS runners.

## Arguments

Arguments are **combinable** — the user may pass several at once (e.g. `/sdk-pr-prepare lint test`). Each argument selects which checks to run; they map to the steps below:

- `compile` — run Step 2 only.
- `test` — run formatting (Step 2) first, then Step 3's test part only, with error fixing and restart on failure.
- `lint` — run formatting (Step 2) first, then Step 3's static analysis part, then the generated files regeneration (Step 4), the commit (Step 6) and the generated files validation after that commit.
- `api` — run Step 4's API surface and compiler metadata part only, then the commit and validation (Step 6).
- `dep` — run Step 4's transitive dependencies part only, then the commit and validation (Step 6).
- `local` — local-only mode. **Skip Step 7 (push) and Step 8 (create draft PR)** regardless of which checks run. Use when the user invokes `/sdk-pr-prepare local` or otherwise passes `local` in the arguments. In this mode, Step 9 reports that push/PR were intentionally skipped. `local` is a modifier, not a check selector: ignore it when deciding which checks to run. If no check arguments remain, run the full default flow.

**No arguments** — run the full default flow (Steps 2–4): first format and compile (Step 2), then the tests and static analysis (Step 3, with error fixing and restart as needed), then the generated files regeneration (Step 4), and validation after the commit (Step 6).

When specific check arguments are given, run only the corresponding parts (plus Step 0/1 branch checks). The commit step (Step 6) still runs if any of the executed steps modified tracked files.

## Step 0 — Protected / release branch guard

```bash
git branch --show-current
```

If the current branch matches any of:

- `main`
- `master`
- `develop`
- A release branch: anything matching the regex `^release/\d+\.\d+\.\d+$` (e.g. `release/2.1.0`, `release/10.4.12`),

do NOT silently continue. Use `AskUserQuestion` to confirm:

> Question: "You are on a protected branch (`<branch>`). prepare-pr will push changes and open a PR from this branch — that's usually not what you want. Continue anyway?"
> Options:
> - **Abort (Recommended)** — stop the skill; switch to a feature branch manually and re-run.
> - **Continue on `<branch>`** — proceed anyway (user takes responsibility for any push consequences).

- If the user picks **Abort**, exit the skill with a short message.
- If the user picks **Continue**, proceed to Step 1. Remember the override so later steps (push, PR creation) do not ask again.

Do not short-circuit this check; always surface the question when on one of the listed branches.

## Step 1 — Verify branch name

Expected format from `AGENTS.md`: `<username>/<ticket>/<short-description>` (e.g. `tvaleev/rum-1234/prepare-stack`).

```bash
git branch --show-current
```

If the current branch does not match the pattern, surface a warning before proceeding (this is a soft check — only Step 0 hard-aborts).

## Step 2 — Format and compile (shared artifacts)

Format sources first, then compile the main and test sources once, so the parallel Step 3 builds reuse the outputs and never see sources being rewritten. **Skip if only `api` or `dep` arguments were given.** When only `test` or `lint` was given, run just the format command.

```bash
./gradlew ktlintFormatAll
./gradlew compileAndroidHostTest   # Android main + host test sources of all modules
./gradlew iosSimulatorArm64TestBinaries tvosSimulatorArm64TestBinaries   # macOS only: Apple main + test sources
```

Don't run `./gradlew --stop` or `./local_ci.sh --clean` here: other checkouts may share the Gradle daemons.

## Step 3 — Run tests and static analysis

Run only the parts selected by the arguments (both, one of them, or neither). Run them **sequentially in this checkout**, without `clean`; they reuse the artifacts produced by Step 2. Don't run two Gradle invocations in the same checkout in parallel: Kotlin compilation is finalized by `generateApiSurface` and `listTransitiveDependencies`, which write checked-in files, so concurrent builds race on them. Gradle already parallelizes work inside one invocation.

**Tests:**
```bash
./gradlew jvmUnitTestAll        # Android host tests (incl. commonTest) + build-plugins tests
./gradlew appleUnitTestAll      # macOS only: iOS and tvOS simulator tests
```

**Static analysis:**
```bash
./gradlew ktlintCheckAll detektAll lintRelease checkDependencyLicenses --continue
./tools/license/check-license.sh   # macOS only (uses BSD find); only if Swift, Objective-C or Python files changed
```

Analyze each result independently. On `FAILED`, inspect the error output, fix the issues, then **repeat from Step 2** (re-compile, then re-run Step 3).

- If **all requested parts pass** → proceed to Step 4.

**When the `lint` argument is given**, run the static analysis commands above; when it is combined with `test`, run the tests next. Then continue with the regeneration in Step 4 and the commit in Step 6, like the default flow. Don't validate generated files in this step: the checks compare them with `HEAD`, so intended changes only pass after the Step 6 commit.

**Never suppress Detekt issues** with `@Suppress(...)` just to make the check pass. Fix the code; suppress only where the rule is a known false positive for that call site (narrowest scope, see `AGENTS.md`). If `checkDependencyLicenses` fails, add the missing dependency to `LICENSE-3rdparty.csv` (alphabetical order).

Common issues to fix directly:
- `max-line-length` — lines over 120 chars
- `TooManyFunctions` — split the class
- `NestedBlockDepth` — extract helpers
- `ReturnCount` — restructure control flow
- Kotlin compiler warnings — they are errors in library modules (`allWarningsAsErrors`)

**Do not proceed until all requested checks in this step pass.**

## Step 4 — Regenerate generated files

Only start these after Step 3 passes (with all fixes and retries if needed). **When `api` and/or `dep` arguments are given**, run only the corresponding part(s).

**API surface and compiler metadata:**
```bash
./gradlew generateApiSurfaceAll generateCompilerMetadataAll
```

This regenerates `api/<sourceSet>/apiSurface` and `api/androidMain/compiler-meta.txt` for every publishable module. On macOS, it also regenerates the Apple compiler metadata (`api/appleMain/compiler-meta.txt`, or `api/iosMain/compiler-meta.txt` for modules without tvOS); elsewhere the Apple tasks are not registered, so report that file as not regenerated.

If an `apiSurface` diff contains generated model classes with no matching schema under `src/commonMain/json/`, the build directory holds stale generated models: run `./gradlew clean`, regenerate, and check again before committing.

**Transitive dependencies:**
```bash
./gradlew listTransitiveDependenciesAll
```

This regenerates `android-transitiveDependencies` for every publishable module.

On `FAILED`, surface the error and stop.

## Step 6 — Commit generated changes

The executed steps may have modified tracked files (`apiSurface`, `compiler-meta.txt`, `android-transitiveDependencies`, potentially linter-driven edits from Step 3). Compiling also regenerates `apiSurface` and `android-transitiveDependencies`, so they may have changed in Step 2 already. Check:

```bash
git status --porcelain
```

If the working tree is clean, skip to the validation at the end of this step.

Otherwise:

1. **Load the sibling [sdk-commit](../sdk-commit/SKILL.md) skill** via the `Skill` tool or read its SKILL.md. All commit messages MUST follow its rules.
2. Before staging anything, check that the index is empty, because `git commit` commits everything that is staged:
   ```bash
   git diff --cached --quiet
   ```
   - Exit status 1: changes are already staged. Stop without changing the index and ask the user to commit them or unstage them (`git restore --staged <paths>`, or `git stash push --staged` to keep them aside) before retrying. A plain `git stash` would also stash the files this flow regenerated or fixed.
   - Any other nonzero status: report the error and stop.
   - Exit status 0: stage only files touched by the prepare-pr flow (typically `**/api/**`, `**/android-transitiveDependencies`, `LICENSE-3rdparty.csv`, and any source edits you made to fix linter issues). Do not blind `git add -A` — other unrelated changes may still be in the tree.
3. Draft a commit message per the `sdk-commit` skill. The subject is imperative, English, ≤72 chars, and follows `<TICKET-ID>: <description>` (e.g. `RUM-1234: Regenerate apiSurface and fix detekt warnings`). If the ticket cannot be determined, ask the user before committing. The body lists what was regenerated / fixed and why.
4. Show the drafted message together with the output of `git diff --cached --stat` to the user via `AskUserQuestion` (Approve / Edit / Abort commit) before running `git commit`. On **Abort**, unstage the paths you staged (`git restore --staged <paths>`) and stop without running the validation below.
5. Commit with HEREDOC so formatting survives:
   ```bash
   git commit -m "$(cat <<'EOF'
   <subject>

   <body>
   EOF
   )"
   ```

Then validate the generated files against the commit (also when there was nothing to commit). Run this for the default flow and for `lint`, `api` and `dep`; skip it for runs with only `compile` and/or `test`:
```bash
./gradlew --stacktrace checkGeneratedFiles                                          # default flow and `lint`
./gradlew --stacktrace checkApiSurfaceChangesAll checkCompilerMetadataChangesAll   # `api` only
./gradlew --stacktrace checkTransitiveDependenciesListAll                          # `dep` only
```

`checkGeneratedFiles` covers the API surfaces, the compiler metadata and the transitive dependencies. On macOS the compiler metadata check includes the Apple (KLIB) metadata; elsewhere the Apple tasks are not registered (CI checks them with `checkKlibCompilerMetadataChangesAll` in a separate macOS job). The checks diff the regenerated files against `HEAD`, so they only pass once the regenerated files are committed. On failure, regenerate (Step 4), commit, and re-run. If the remaining failure comes only from uncommitted changes outside this flow (e.g. work in progress the user left unstaged), don't commit them: report the failure and its cause to the user.

## Step 7 — Push the branch

**Skip this step entirely if `local` was passed in the arguments.**

```bash
git push -u origin "$(git branch --show-current)"
```

If the push fails due to non-fast-forward (someone force-pushed upstream), surface the error and stop — do not force-push automatically.

## Step 8 — Open a draft PR

**Skip this step entirely if `local` was passed in the arguments.**

1. Detect the base branch: use `git config --get branch.<current>.merge` (strip `refs/heads/`) or fall back to `develop` if it exists, otherwise `main`. If the result is the current branch itself (the usual case after `git push -u` in Step 7), ask for the base via `AskUserQuestion` with options `develop` (Recommended) and `main`, plus "Other" for a custom branch.
2. **Load the sibling [sdk-commit](../sdk-commit/SKILL.md) skill** if it was not loaded in Step 6; otherwise reuse its rules for keeping PR descriptions separate from commit bodies.
3. Detect a PR template in the repo (in order):
   - `.github/PULL_REQUEST_TEMPLATE.md`
   - `.github/pull_request_template.md`
   - `docs/PULL_REQUEST_TEMPLATE.md`
   - `PULL_REQUEST_TEMPLATE.md` at repo root
   - Any file under `.github/PULL_REQUEST_TEMPLATE/`
4. Draft the PR body — **mandatory, never empty**:
   - If a template exists: follow its prompts and keep applicable sections and the review checklist in order.
   - Otherwise: 2–4 sentences in English covering **what changed**, **why**, and any relevant reviewer context.
   - Include validation only when it adds relevant context beyond pipeline results, such as an investigation with temporary tests or a custom harness, a manual check in an app or backend, or emitted data verified through Datadog MCP. Summarize the scenario, method, and observed result. Mention failures or coverage limitations when they affect confidence in the change. Keep routine successful build, test, lint, and generated-file checks in task output or reports.
   - An empty or placeholder-only body is never acceptable — the user may edit wording but **may not approve a blank description**.
5. Derive the PR title from the last commit on the branch (or from the ticket ID + short description). English, ≤72 chars. Same rules as `sdk-commit`'s header section.
6. Show title + body to the user via `AskUserQuestion` (Approve / Edit / Abort) before creating the PR.
7. Create the PR as a **draft**:
   ```bash
   gh pr create --draft --base "<base>" --head "$(git branch --show-current)" \
     --title "<title>" \
     --body "$(cat <<'EOF'
   <body>
   EOF
   )"
   ```
8. Capture the PR URL from the output and show it to the user.

If `gh pr create` fails (auth, protected base, duplicate PR), surface the error verbatim and stop.

## Step 9 — Report

Print a concise summary:

- Generated files (apiSurface / compiler-meta.txt / android-transitiveDependencies), and which Apple checks were skipped on a non-macOS host
- Fixed issues (tests / linter) with counts
- Commit hash created in Step 6 (if any)
- Draft PR URL from Step 8 — or, in `local` mode, an explicit note that push and PR creation were skipped and that the user can run `/sdk-pr-prepare` (without `local`) to publish
- Any remaining warnings the user should look at before marking the PR ready
