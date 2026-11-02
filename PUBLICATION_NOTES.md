# Reviewed source publication

This branch publishes source and build/test tools only. Character packs, audio, artwork, packaged applications, third-party Maven/runtime binaries and local settings are excluded and preserved privately. Install Maven and JDK21 separately for source builds. Native pack-dependent tests require the separately preserved private assets; this branch does not grant redistribution rights. No binary release is published.

The four commits are reconstructed from the local candidate on top of the existing remote main. Original local branches, commits, working files and backups are retained. Excluding files here does not remove them from older GitHub history. Previously published assets and upstream LICENSE/NOTICE still require a separate rights review; no history rewrite was attempted.

The candidate fixes lifecycle/performance, combat/pose handling, UI clipping and multi-monitor falling/window collision. Existing QA reports describe their named local builds, not a new clean-device acceptance of this source branch.
