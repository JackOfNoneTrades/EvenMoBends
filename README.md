# Even Mo' Bends

Maintainable Minecraft 1.7.10 fork of Mo' Bends 0.20.1 with Wawel Auth and Smart Moving compat.

<!-- ![logo](images/logo.png) -->

[![hub](images/badges/github.png)](https://github.com/JackOfNoneTrades/EvenMoBends/releases)
![forge](images/badges/forge.png)
[![cord](images/badges/cord.png)](https://discord.gg/xAWCqGrguG)

<!--
[![curse](images/badges/curse.png)](https://www.curseforge.com/minecraft/mc-mods/even-mo-bends)
[![modrinth](images/badges/modrinth.png)](https://modrinth.com/mod/even-mo-bends)
[![67](images/badges/67.png)](https://67.fentanylsolutions.org/mod/even-mo-bends)
[![maven](images/badges/maven.png)](https://maven.fentanylsolutions.org/#/releases/net/gobbob/mobends/EvenMoBends)
-->

## Compatibility

| Installed mods | Player renderer | Mob animations |
| --- | --- | --- |
| Even Mo' Bends only | Mo' Bends | Enabled |
| WawelAuth + Even Mo' Bends | Mo' Bends with WawelAuth skins and modern-skin support | Enabled |
| GTNH Et Futurum Requiem + Even Mo' Bends | Mo' Bends with an EFR-aware Elytra flight pose | Enabled |
| Smart Moving (with or without WawelAuth) | Smart Moving / Smart Render; Mo' Bends player features disabled | Enabled |

## Blinking

Player skins can opt in to blinking with the embedded blink marker and frames documented by ETF's
[player skin guide](https://github.com/Traben-0/Entity_Texture_Features/blob/ETF-Main/.github/README-assets/SKIN_GUIDE.md#blinking---v230).
Even Mo' Bends implements only the blinking part of that convention. Sleeping and blindness keep the eyes closed.
By default, intermediate eyelid frames are generated and eased closed and open for a smoother, Fresh Moves-style
blink without adding a second player body rig. Set `blinking.smoothEyelids=false` to use the authored frames directly.

Pigs blink; spiders and zombie-family mobs deliberately do not. A resource pack can override a pig's generated
closed-eye texture by placing `_blink` before `.png`, with an optional `_blink2.png` half-closed frame. When Angelica
Random Mobs is enabled, these companions are resolved after the pig variant, so `pig2.png` can use `pig2_blink.png`.

Player blinking, smooth eyelids, pig blinking, blink frequency, and blink length are independently configurable in
`mobends.cfg`.

## Building

```sh
./gradlew build
```

The default development client includes WawelAuth and GTNH Et Futurum Requiem for compatibility testing. Launch it on Java 25 from this repository:

```sh
./gradlew runClient25
```

It can additionally load makamys' Smart Moving stack without making it a published dependency:

```sh
./gradlew -PsmartMoving runClient25
```

## Credits

* [Iwo Plaza / GobBob](https://www.curseforge.com/minecraft/mc-mods/mo-bends), creator of Mo' Bends.
* [makamys](https://github.com/makamys/SmartMoving), maintainer of the Minecraft 1.7.10 Smart Moving fork used for compatibility testing.
* [GTNH Et Futurum Requiem](https://github.com/GTNewHorizons/Et-Futurum-Requiem), whose optional API supplies Elytra flight state.
* [Entity Texture Features](https://github.com/Traben-0/Entity_Texture_Features), whose documented player-skin blink convention is supported.
* [GT:NH buildscript](https://github.com/GTNewHorizons/ExampleMod1.7.10).

See [UPSTREAM.md](UPSTREAM.md) for exact source and binary provenance.

## License

Mixed MIT and GPL-3.0-or-later; see [LICENSE](LICENSE) for details.

## Buy me some creatine

* [ko-fi.com](https://ko-fi.com/jackisasubtlejoke)
* Monero: `893tQ56jWt7czBsqAGPq8J5BDnYVCg2tvKpvwTcMY1LS79iDabopdxoUzNLEZtRTH4ewAcKLJ4DM4V41fvrJGHgeKArxwmJ`
