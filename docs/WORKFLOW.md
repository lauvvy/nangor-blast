# WORKFLOW — Nangor Blast

How the coding agent and the team members work across sessions. Project rules are in [`CLAUDE.md`](../CLAUDE.md), scope in [`PRD.md`](PRD.md), the plan in [`ARCHITECTURE.md`](ARCHITECTURE.md), and status in [`TODO.md`](TODO.md).

## Starting a session

1. Read `CLAUDE.md`, this file, and `TODO.md`.
2. Pick the earliest-due task that is yours and whose dependencies are **Done**.
3. Read that task's completion criteria in `ARCHITECTURE.md` and its section in the specification.
4. Set the task to **In progress** in `TODO.md`.

## Working Rules

- **Proceed within scope.** Work independently on a task that is in `TODO.md`, inside your own packages, and within the scope in `PRD.md`.
- **Ask when a decision matters.** Stop and ask before any of these:
  - Changing a contract listed in `ARCHITECTURE.md`. Every member builds against them.
  - Editing a package owned by another member.
  - Adding a dependency or changing the stack.
  - Adding, dropping, or reordering scope, including bonuses.
  - Choosing an answer to an open question in `PRD.md` section 8 that differs from its working assumption.
  - Anything the specification leaves ambiguous and that changes game behavior.
- **Verify completion.** Check the task's completion criteria in `ARCHITECTURE.md` and the related acceptance criteria in `PRD.md`. Run the tests and the build. For anything on screen, run the game and check it by hand. Record what was checked in `TODO.md`, and say plainly which checks are still pending.
- **Hand off Git commands.** After code changes, summarize the work and provide `git add -- <changed-files>`, `git diff --cached`, `git commit -m "<message>"`, and `git push origin <branch>` for the user to run. The user performs staging, commits, and pushes.
- **Keep the project documents current.** When an agreed decision changes, update the source document first and then the affected rows in `TODO.md`.

## Definition of done

A task is **Done** when all of these are true.

1. Every completion criterion in `ARCHITECTURE.md` has been checked.
2. Each rule in the criteria has a unit test, and `make test` passes.
3. `make build` succeeds on a clean checkout.
4. The change stays inside the layer rules in `CLAUDE.md`.
5. The branch is merged into `main` through a reviewed pull request.
6. `TODO.md` is updated with the verification result.
7. The member added a row to `docs/log-activity.md`.

## Team Git workflow

- `main` always builds and runs. Nobody pushes to it directly. The one exception is the first commit, which holds the project documents, because a pull request needs a base branch.
- One branch per task, named `feat/<task-id>-<short-name>`. Use `fix/<short-name>` for a bug and `docs/<short-name>` for documents.
- Start each branch from the latest `main`. Rebase on `main` before opening a pull request.
- Open a pull request to `main`. One other member reviews it. Laura reviews changes to `app/GameFactory` and to any contract.
- Keep pull requests small, ideally one task each. Merge often so integration problems appear early.
- Commit messages follow Conventional Commits with the task ID as the scope: `<type>(<task-id>): <summary>`, for example `feat(C-04): stop player movement at active bombs`. Types: `feat` new behavior, `fix` a bug, `test` tests only, `refactor` no behavior change, `docs` documents only, `chore` build, dependencies, or tooling. Write the summary in English, in the imperative, and keep the line within 72 characters. Put details in the body. A task's code, tests, and document updates usually go in one `feat` commit.
- If a merge breaks `main`, revert the merge commit first and fix on a branch.
- Laura runs an integration build on `main` every evening from 15 November and reports failures to the owner.

## Branch plan

Each task has one branch. The branch name for every task is in the Branch column of `TODO.md`. This section gives the order.

### Rules

- Branch from the latest `main` only, never from another unmerged branch.
- Keep a branch open for at most three days. Rebase on `main` every day you work on it.
- Work on one branch at a time, except where a lane below marks two tasks as parallel.
- If a task's dependency is not merged yet, take the next task in your lane that has no open dependency.
- A branch changes only its owner's packages. The exception is one line per system in `app/GameFactory`.
- Delete the branch after it is merged.
- Tasks `S-03`, `S-04`, and `S-06` are meetings and have no branch. `S-09` is a tag on `main`. Bugs found in `S-10` are fixed on `fix/<short-name>` branches.

### Gates

A gate is a date by which a set of branches must be merged. The next set of branches depends on it.

| Gate | Date | Merged into `main` by then | Opens |
| --- | --- | --- | --- |
| 0. Bootstrap | 11 Nov | The documents as the first commit, then `S-01` | Everything |
| 1. Contracts | 12 Nov | `C-01`, `S-02` | Every `B`, `E`, and `M` branch |
| 2. MVP-0 | 14 Nov | `C-02`, `C-03`, `C-04`, `C-05` | GUI work |
| 3. First slices | 16 Nov | `B-01`, `B-03`, `B-02`, `E-01`, `M-01`, `C-08` | `C-09` |
| 4. Milestone 1 | 17 Nov | `C-09`, `S-05` | — |
| 5. Mandatory complete | 26 Nov | Every remaining `C`, `B`, `E`, and `M` branch | Bonus work outside the GUI |
| 6. Bonus cut-off | 1 Dec | Every `G` and `X` branch that is finished | — |
| 7. Freeze | 2 Dec | Only `fix/` and `docs/` branches from here | `S-09` |

### Lanes

Each member merges their branches in this order. Two tasks joined by "and" can be open at the same time.

| Member | Order |
| --- | --- |
| Laura | `S-01` → `S-02` → `C-01` → `C-02` and `C-03` → `C-04` → `C-05` → `C-08` → `C-09` and `S-05` → `C-06` → `C-07` → `G-01` → `G-02` → `G-03` → `G-04` → `X-01` |
| Marshall | `B-01` and `B-03` → `B-02` → `B-04` → `B-05` → `B-06` → `B-07` → `B-08` → `X-02` → `X-04` |
| Ilham | `E-01` → `E-02` → `E-03` → `E-04` → `E-05` → `E-06` and `E-07` → `X-05` |

Additional responsibilities retain their existing branches and dependencies: Laura takes `M-01` → `M-02`, `M-05`, and `S-08`; Marshall takes `M-03` → `M-04` and `X-03` after `M-02`; Ilham takes `M-06` and `S-07`. The original lanes above remain unchanged.

`E-04` and `M-03` need only `C-01`. Ilham can open `E-04` while `E-02` or `E-03` is in review, and Marshall can open `M-03` while waiting for Laura's `M-01`/`M-02` handoff.

Before Gate 1, Marshall and Ilham have no code branch. They draw the class diagram for their packages, which goes into `S-05`.

### Reviews

| Branches | Reviewer |
| --- | --- |
| `C-01` | Marshall and Ilham, because both build against it. Review on 12 Nov. |
| Any change to a contract or to `app/GameFactory` | Laura |
| Laura's other branches | Whichever member is free first |
| Marshall's branches | Ilham |
| Ilham's branches | Laura |

Review within one day. A pull request that waits longer blocks the lane behind it.

## Activity log

The Milestone 2 submission needs a log per member. Each member adds a row to their own table in `docs/log-activity.md` on every day they work, with the date, the activity, and the result, including known bugs. Do not copy another member's entries.

## Assistance sessions

- Contact the assistant at least 24 hours before the requested time.
- Asistensi 1 is for clarifying the specification. Bring the open questions in `PRD.md` section 8.
- Every member who attends submits the assistance form within 1 hour of the session, with a screenshot or photo as proof of attendance. Write your own notes.
- Record each answer in `PRD.md` section 8 the same day and update the affected tasks.

## When the plan slips

- If a task will miss its due date, set the reason in `TODO.md` at least one day before and tell the team.
- Mandatory features come before every bonus. If the 26 November target for mandatory features is missed, bonus work stops until it is met.
- On 1 December, any unfinished bonus is dropped in the cut order in `PRD.md` section 7 and the change is recorded there.
