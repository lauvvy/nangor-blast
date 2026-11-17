# Specification 1.2 alignment

Source: https://docs.google.com/document/d/11SiADQ6-lTOeH3joe7QI_XrsJZTnxrdqHmgrmWtK3lA/edit

Checked on 17 November 2026 using the document's text export. Revision history:
14 November adds responsible generative AI use; 15 November adds bomb rules.
The original specification copy is preserved.

## Bomb behavior

- Normal is the initial selection; N selects Normal, E selects Pumped, R selects
  Remote after Remote Detonator is unlocked. B or Space places the selected type;
  Q detonates all active Remote Bombs belonging to the player.
- Selection persists for subsequent placements and never changes existing bombs.
  The CLI HUD displays the selected type. CLI glyphs are `o` (Normal), `O`
  (Pumped), and `@` (Remote).
- Pumped is available immediately: 4.5-second fuse, player range + 2 capped at 7,
  2 boss damage, and destruction of Reinforced Blocks. Remote has no timed fuse,
  uses the player's range, and deals 1 boss damage. Each uses one capacity until
  its explosion ends.
- Locked Remote selection is ignored at the command boundary so an unavailable
  action cannot terminate the game loop. The model still rejects invalid direct
  selection with `InvalidActionException`.
- Explosion chains complete within the same tick regardless of entity insertion
  order. Each bomb is deactivated before its flames are created, bounding the
  chain by the active bomb count and preventing duplicate explosions.
- The game loop dispatches actions through `PlayerSystem` and `BombSystem`, then
  updates fuses and explosions with `dt` while PLAYING. Pause freezes those
  updates. Restart creates a new world and restores Normal selection.
  `GameFactory.systems` now registers `ExplosionSystem` immediately after
  `BombSystem`; without that registration, placed bombs never produced flames.

`BombRevisionTest` covers locked selection, Pumped timing/range/damage, unchanged
placed bomb types, Remote ownership/manual detonation, paused commands, same-tick
chains, single explosions, and capacity recovery. Existing tests cover CLI glyphs,
HUD, restart, explosion propagation, blocks, and pause.

## Responsible AI use

The specification permits responsible generative AI use and requires every team
member to be able to explain submitted code during assistance and demos. Review
the implementations and tests above before submission. This alignment does not
mark unrelated pending Milestone 2 features as complete.

## Verification

- `gradlew.bat build test --console=plain` with portable Eclipse Temurin
  21.0.13+9: BUILD SUCCESSFUL; 141 tests, 0 failures, 0 errors, 0 skipped.
- Java compilation uses the repository's `--release 21`, `-Xlint:all`, and
  `-Werror` settings. The system Java 26 failed during Gradle initialization;
  the portable JDK was used only for this build without changing system Java.
- `git diff --check`: passed.
- Interactive terminal play was not manually verified in this session.
