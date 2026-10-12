# ShinobiMate attribution and redistribution review

ShinobiMate includes a modified Shimeji/Shimeji-ee engine. Existing package names
and attribution to Kilkakon, Zero and Begonia are retained. The lineage also
includes Shimeji by Yuki Yamada (group-finity). This is a private Friends Beta.

The repository's LICENSE is preserved verbatim. Its ShinobiMate-only notice,
two-clause wording and the unusual phrase `IN CONTRACT mechanically` require
review against the exact upstream source used. README previously described
BSD-2-Clause and Zlib, while upstream fork documentation describes New BSD and
zlib/libpng. This notice is not a replacement license and does not resolve that
discrepancy. Recover the original notices and identify the exact upstream revision
before broader redistribution or a public commercial release.

Upstream reference: https://github.com/gil/shimeji-ee (its README names the
New BSD and zlib/libpng licenses). Do not erase upstream credit when branding.

Bundled dependencies: JNA/JNA Platform 5.17.0, Rhino 1.7.14, FlatLaf and FlatLaf
IntelliJ Themes 3.6, NetBeans AbsoluteLayout RELEASE270. Dependency POM metadata
and all license/notice entries from each original JAR are preserved individually
under `licenses/`, avoiding collisions caused by shading. The included OpenJDK
21.0.8 runtime retains its complete `runtime/legal/` directory and release file.
Review their actual license terms, third-party notices and runtime provenance
before commercial redistribution. Inno Setup's commercial licensing also needs
review if the project is later distributed commercially.

The five Naruto, Sasuke, Gojo, Sukuna and Asta character packs are separate
third-party/anime-inspired assets. Their inclusion does not grant ownership of
the characters, artwork, names, trademarks or sound assets. Asset authors,
source provenance and redistribution/commercial permissions remain unverified.
They are retained for the requested private beta; do not treat the core engine
license as permission for those assets. Use assets with documented permission
before a public commercial release.
