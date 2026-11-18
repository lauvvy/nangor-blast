# WORKFLOW — Nangor Blast

How the coding agent and the four team members work across sessions. Project rules are in [`CLAUDE.md`](../CLAUDE.md), scope in [`PRD.md`](PRD.md), the plan in [`ARCHITECTURE.md`](ARCHITECTURE.md), and status in [`TODO.md`](TODO.md).

**Execution revision: 19 November 2026.** Future tasks are assigned to Laura, Marshall, and Ilham. The original roster and past contributions remain recorded. TODO.md contains the current leaf tasks, estimated remaining effort, recovery targets, and file ownership; old parent IDs are coverage groups. All changes in this revision are planning changes, not completion evidence.

## Starting a session

1. Read `CLAUDE.md`, this file, and `TODO.md`.
2. Pick the earliest-due task that is yours and whose dependencies are **Done**.
3. Read that task's completion criteria in `ARCHITECTURE.md` and its section in the specification.
4. Set the task to **In progress** in `TODO.md`.

## Working Rules

- **Proceed within scope.** Work independently on a task that is in `TODO.md`, inside your own packages, and within the scope in `PRD.md`.
- **Ask when a decision matters.** Stop and ask before any of these:
  - Changing a contract listed in `ARCHITECTURE.md`. Every member builds against them.
  - Editing another owner's files outside the reviewed wiring seams explicitly assigned in TODO.md.
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
- A branch changes only its assigned files. Map/mapio/hazard/editor ownership is split per file in TODO.md. Laura coordinates reviewed wiring in `app/GameFactory`; M-02B may add one delegation call to Marshall's `MapValidator` with his review. M-06A may update only the help-text section of Laura's `GameFlow`, with her review. Keep algorithms in the named helper/system files.
- Delete the branch after it is merged.
- Tasks `S-03`, `S-04`, and `S-06` are meetings and have no branch. `S-09` is a tag on `main`. Bugs found in `S-10` are fixed on `fix/<short-name>` branches.

### Gates

Gates 0–4 retain their original historical dates; verify their evidence rather than inferring completion. The current recovery path is M-01A/M-05A → validators/lava → M-01C → M-01B/M-05B → C-09 by 26 Nov. GUI starts from 27 Nov in this plan, and all bonus work requires the actual MANDATORY gate defined in TODO.md. A later recovery target does not extend the official 17 Nov Milestone 1 deadline.

A gate is a date by which a set of branches must be merged. The next set of branches depends on it.

| Gate | Date | Merged into `main` by then | Opens |
| --- | --- | --- | --- |
| 0. Bootstrap | 11 Nov | The documents as the first commit, then `S-01` | Everything |
| 1. Contracts | 12 Nov | `C-01`, `S-02` | Every `B`, `E`, and `M` branch |
| 2. MVP-0 | 14 Nov | `C-02`, `C-03`, `C-04`, `C-05` | GUI work |
| 3. First slices (historical target) | 16 Nov | `B-01`, `B-03`, `B-02`, `E-01`, the M-01 children, `C-08`; map recovery remains open | `C-09` once its current dependencies close |
| 4. Milestone 1 | 17 Nov | `C-09`, `S-05` | — |
| 5. Mandatory complete | 26 Nov | S-02 and every `C`, `B`, `E`, `M` leaf, including split children, reviewed and Done | GUI and other bonuses in this recovery plan |
| 6. Bonus cut-off | 1 Dec | Every `G` and `X` branch that is finished | — |
| 7. Freeze | 2 Dec | Only `fix/` and `docs/` branches from here | `S-09` |

### Lanes

Take the next ready task in the lane; the dependency graph and targets in TODO.md take precedence over the display order below. Keep one implementation branch active; a ready independent task may proceed while another branch is awaiting review. Historical administrative checks S-03/S-04/S-05 must be reconciled separately.

| Member | Order |
| --- | --- |
| Laura | Reconcile S-02/S-03/S-04/S-05 → M-01A → M-02B → M-04A → M-01B → M-05B → C-09 → G-01/G-02/G-03/G-04; S-07A, X-01, S-06/S-09, and shared S-10 at their targets |
| Marshall | Finish review/cases for B-04/B-05/B-06; M-05A → M-02A → M-03 → M-01C → B-07 → B-08 → M-06B → X-02/X-03A/X-04 after the mandatory gate; shared S-10 |
| Ilham | Reconcile E-01 → E-02/E-03 → E-04 → E-05 and M-04B as ready → E-06/E-07 → M-06A; X-03B/X-05 after the mandatory gate; S-07B/S-07C/S-08 after the implementation deadline, plus S-06 and shared S-10 |

M-05A and M-01A can start independently after C-01. Agree their config and map/factory contracts before dependent branches. E-04 may proceed while E-02/E-03 awaits review; M-03 now depends on M-01A and M-05A. M-01C also waits for the actual Hades construction API in E-05. Do not use test stand-ins to hide that dependency.

The pre-Gate-1 diagram work remains historical S-05 evidence. It does not close an implementation task.

### Reviews

| Branches | Reviewer |
| --- | --- |
| Existing/shared contract changes, including former `C-01` APIs | Laura coordinates; Marshall and Ilham review changes she authors |
| `app/GameFactory` wiring | Laura, or Marshall/Ilham when Laura is the author |
| Laura's other branches | Marshall; Ilham may substitute when needed |
| Marshall's other branches | Ilham; Laura reviews map/config contract seams |
| Ilham's other branches | Laura; Marshall reviews the MapMakerIO handoff and technical booklet content |
| M-02B delegation into MapValidator | Marshall |

Review within one day. The author cannot approve their own work. A pull request that waits longer blocks the lane behind it.

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
