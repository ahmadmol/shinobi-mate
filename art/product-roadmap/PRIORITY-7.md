# Priority 7 — Progression and challenges: DONE

Optional profile XP and cosmetic levels are keyed by pack id, shared across duplicate instances and persisted atomically in conf/progress.properties. Interaction XP is paced, usage XP accrues slowly, completed focus/battle events award small amounts. Neither levels nor challenges are consulted by combat damage/HP/energy/AI.

Local daily and ISO-week challenge counters track interaction, focus, completed manual battles and two active characters. RUN_TWO counts once per calendar day even across restarts. Stale periods are pruned. Features are disabled by default and can be enabled independently. No backend, payments or grinding gate.

A single coalesced background writer handles dirty snapshots; exit flushes the last changes. UI displays profiles and daily/weekly progress. Manager self-stop no longer joins its own thread when the final instance exits.

Verification: Maven BUILD SUCCESS; ProgressionCheck 18 assertions covers level thresholds, profile isolation, Unicode-compatible property persistence, daily/year/ISO-week transitions, optional flags, interaction pacing, battle awards and content access models. Real bilingual dialog screenshots are available.

New source: progression/{ProgressionModel,ProgressionService}, ui/ProgressionDialog; tools/ProgressionCheck.java. Modified: CompanionFeatures, FocusService, CombatEngine, Manager, Main, SettingsDialog, CharacterManagerWindow.
