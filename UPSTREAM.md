# Upstream provenance

This repository starts from the official Mo' Bends 0.20.1 binary for Minecraft 1.7.10. The original 0.20.1 Java source does not appear in the public history of the later official MoBends GitHub repository, so this baseline was mechanically recovered from the release artifact.

## Exact inputs

- CurseForge project: Mo' Bends (`mobends`)
- CurseForge file ID: `2242799`
- Published filename inside the download: `MoBends-0.20.1 for MC 1.7.10.jar`
- Jar SHA-256: `b1cab36f73e334d02153ec179460e0c8a21add1de90137bd86b32c74485cc3a3`
- Download ZIP SHA-256: `51e6b1f8a25364800ad4dd7fb21e0a28a96d2adcdd884f5cad44e75a5c284c20`
- Remapper: SpecialSource 1.11.0
- Decompiler: CFR 0.152
- Names: Forge 1.7.10 SRG plus MCP stable 12 CSV mappings

The resources under `src/main/resources` were copied from that official jar. Java sources under `src/main/java/net/gobbob/mobends` were remapped and decompiled, then received only mechanical MCP-name cleanup before compatibility work began.

## Reproducing the recovered tree

Run `tools/recover-upstream.sh` with the official jar and an empty output directory. The script refuses an artifact whose SHA-256 differs from the value above. It expects the standard ForgeGradle 1.7.10 cache, SpecialSource 1.11.0, and CFR 0.152; their paths can be overridden with the environment variables documented by the script.

```sh
tools/recover-upstream.sh "/path/to/MoBends-0.20.1 for MC 1.7.10.jar" /tmp/mobends-recovered
```

## License and authorship

Mo' Bends is distributed under the MIT License. The repository retains that license in `LICENSES/MIT.txt`. The recovered code remains attributable to the original Mo' Bends author, Iwo Plaza / GobBob; fork-specific changes should be identified in Git history.

Some player animations are adapted from makamys' Smart Moving fork, which is GPL-3.0-or-later. Each derived source file carries an SPDX GPL marker and an upstream reference. Combined Even Mo' Bends binaries containing this code are therefore distributed under GPL-3.0-or-later; independently MIT-licensed source files remain available under MIT.

### Smart Moving input

- Repository: `https://github.com/makamys/SmartMoving`
- Tag: `15.8.2-maka`
- Commit: `4fe6a9dd080162859b3630b6a886bfe47d50f009`
- Relevant source: `src/main/java/net/smart/moving/render/SmartMovingModel.java`
- License: GNU General Public License version 3 or later

### Entity Texture Features input

- Repository: `https://github.com/Traben-0/Entity_Texture_Features`
- Branch inspected: `ETF-Main`
- Commit inspected: `a7fc2a1172264899a3fe4058c9958a2c42269920`
- Relevant source: `ETFPlayerTexture.java`
- Relevant documentation: `.github/README-assets/SKIN_GUIDE.md`
- Reused scope: player-skin blink marker constants and embedded blink-frame layout only
- License: GNU Lesser General Public License version 3 only

No source or assets from MoBends-Reforged or Astryxion projects are used here.
