# Priority 6 — Custom cloud, focus, idle and social: DONE

Custom messages are separate from pack text. Unicode add/edit/delete and per-entry/global enable are supported; atomic persistence never edits pack messages. Arabic and English pack files select with UI language; missing English falls back to the pack's Arabic messages. Both layers deduplicate.

Offline focus uses a pure synchronized Pomodoro state machine: default 25/5, configurable times, pause/resume/stop, gentle transition notices and a temporary celebration. Resting pets pause random behavior during focus; falling/climbing/drag/combat remain safe. Missed/overnight ticks emit a single transition, preventing a backlog of notifications.

Idle levels select only manifest-declared poses, hold briefly on a safe resting surface, cancel on interaction and suppress activity during focus. Current packs declare sit/sleep/rest/stretch/look/train references; reading is absent because no reading art was supplied. Social gestures are a distinct Manager-owned service, near resting pets at long intervals; they face each other and show declared friendly poses. It has no combat-start path.

Validation: Maven BUILD SUCCESS. FocusCheck 25 assertions covers focus deadlines, pause, break, missed ticks, stop, Unicode editing/enable, independent custom layer, real mascot quiet/resume, activity disabling, social poses for both participants without combat, and both-language focus UI controls. MoodCheck now passes 43 assertions; a wake-event dwell issue found in the first model check was corrected.

New: cloud/CustomMessages, ui/{CloudEditorDialog,FocusDialog}, focus/{FocusSession,FocusService}, interaction/SocialService, tools/FocusCheck.java, five English cloud files in both pack trees. Modified: CloudMessages, CompanionFeatures, Manager, Mascot, Main, SettingsService, SettingsDialog, CharacterManagerWindow, MoodState, InteractionState.
