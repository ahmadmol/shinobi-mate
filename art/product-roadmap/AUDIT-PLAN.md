# ShinobiMate — Baseline audit and implementation sequence

Date: 2026-10-08. No git add/commit/push or jar patching is authorized. Source changes are built through Maven.

## Phase 0: audited baseline

| Area | Existing implementation | Compatibility boundary |
|---|---|---|
| Startup/lifecycle | `Main` loads UTF-8 language bundles, actions/behaviors/info XML; creates instances through `createMascot`; starts `Manager` | Keep existing launchers, language bundles and five packs |
| Instances | `Mascot` owns native window, behavior, anchor, monotonically increasing ID, cloud and mouse listeners | Per-instance state; never infer identity from display name |
| Updates | `Manager` updates environment, combat, behaviors and display at 40ms | Fixed logic tick; render throttling must be independent |
| Packs | `ImageSetChooser` discovers folders with actions/behaviors and PNGs; `Main` loads each configuration | Manifest is optional; retain legacy folders and XML combat |
| Combat | Manager-owned generic `CombatEngine`, pure `CombatMatch`, validated `CombatProfile`, independent overlay | Keep logic; extend visuals/settings via configuration and adapters |
| Cloud | Per-mascot `SpiritualCloud`, pure `CloudState`, optional UTF-8 pack messages, coalesced EDT updates | Custom messages are a separate layer; preserve click/pin/drag semantics |
| Mouse/menu | Mascot listeners, threshold-based `ClickGesture`, behavior mouse handlers, Arabic context menu | One reliable gesture path; no competing click handlers |
| Settings | `Main.properties`, direct property-file writes, a large legacy `SettingsWindow` with advanced appearance/environment controls | Atomic persistence service; preserve advanced settings window as an accessible compatibility surface |
| Windows | NativeFactory/Environment/TranslucentWindow abstraction; GDI bitmap cache, Cleaner and DPI adaptation | UI coordinates and monitor work area must agree; combat cannot move external windows |
| UI | Swing + FlatLaf; several separate windows and manually constructed tray menu | Shared tokens, localized labels and recursively applied RTL/LTR |
| Import | Existing `.smp` archive/localization manager | Add a staged, bounded safe importer; do not silently install executables or overwrite engine files |
| Build | Maven fat JAR, release 21, bundled Maven; JDK 25 available for checks | All binaries originate from clean Maven build |
| Portable | Existing jpackage launcher + Java 21 runtime, app JAR, conf, img | New distribution is a filtered staging directory; never package source/tests/logs |

## Ordered implementation plan and dependencies

1. **Priority 1 / Phase 1: combat visuals.** Generate five independent, transparent combat atlases; validate/crop/export dedicated poses; link profile animations; add charge/hit flash and stage-specific VFX. Test pose anchors, mirrored attacks, bounds and native rendering using existing combat checks. Combat logic remains the baseline.
2. **Priority 2 / Phases 4–5 plus sound settings:** shared UI tokens, typed settings service, atomic saves, settings panels, render profiles with the 40ms logic clock unchanged. Wire each control to behavior; preserve legacy advanced settings. Cloud controls and custom-message layer depend on settings. Test persistence, numeric bounds, render cadence and both languages.
3. **Priority 3 / Phases 6–7:** character cards and onboarding, using a public service facade rather than adding responsibilities to Main. Keep the legacy chooser accessible. Manager launch/remove/details must operate on real instances.
4. **Priority 4 / Phases 8–9 and 20:** manifest model, capability validation, legacy adapter, safe preview/confirm/stage/install import; content entitlement models only, no payment API. This supplies metadata to character cards and future content discovery.
5. **Priority 5 / Phases 2–3:** per-instance mood and interaction state, using monotonic idle times and the existing gesture detector. Pack capabilities choose available expressions; mouse/cloud/drag/combat arbitration stays in one path.
6. **Priority 6 / Phases 10–13:** custom cloud editor, offline focus session state machine and tray controls, declared idle activities and a separate friendly social service. Focus gates random activity; combat remains manual.
7. **Priority 7 / Phases 14–15:** optional persistent profile XP and local daily/weekly challenge events. No combat-stat advantage, grinding requirement, backend or payments.
8. **Priority 8 / Phases 16–19:** categorized original/placeholder sounds, filtered portable ZIP with included runtime, documented reputable installer recipe/toolchain, explicit HTTPS update check with download-page-only navigation. No silent executable update.

Each priority ends with Maven build, focused tests, regression checks and an updated report. Large priorities are split into separately verified sub-phases. A phase is DONE only when its implemented behavior is tested; partial art or installer tooling stays PARTIAL rather than being hidden by placeholder UI.

## Implementation rules

- New packages: `settings`, `performance`, `ui`, `character`, `pack`, `mood`, `interaction`, `focus`, `progression`, `update`, `audio`; reuse `combat` and `cloud`.
- Services expose testable models independent of Swing. Swing adapters use EDT and coalesce updates. Use the existing logic clock, not per-mascot timers.
- Optional fields and defaults preserve existing packs/settings. New UI controls must actually affect runtime behavior.
- Settings/profile writes use sibling temporary files and atomic replacement where supported. Imports stage below an approved pack root and move only after validation and explicit UI confirmation.
- Preserve user changes already present in the workspace. Snapshot the relevant current baseline before asset/profile changes.
- Report phase status, evidence, regressions, asset source/prompts, performance limits and deferred tool requirements in `art/product-roadmap/REPORT.md`.
