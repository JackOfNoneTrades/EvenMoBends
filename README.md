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

## Building

```sh
./gradlew build
```

The development client can load makamys' Smart Moving stack without making it a published dependency:

```sh
./gradlew -PsmartMoving runClient
```

## Credits

* [Iwo Plaza / GobBob](https://www.curseforge.com/minecraft/mc-mods/mo-bends), creator of Mo' Bends.
* [makamys](https://github.com/makamys/SmartMoving), maintainer of the Minecraft 1.7.10 Smart Moving fork used for compatibility testing.
* [GTNH Et Futurum Requiem](https://github.com/GTNewHorizons/Et-Futurum-Requiem), whose optional API supplies Elytra flight state.
* [GT:NH buildscript](https://github.com/GTNewHorizons/ExampleMod1.7.10).

See [UPSTREAM.md](UPSTREAM.md) for exact source and binary provenance.

## License

Mixed MIT and GPL-3.0-or-later; see [LICENSE](LICENSE) for details.

## Buy me some creatine

* [ko-fi.com](https://ko-fi.com/jackisasubtlejoke)
* Monero: `893tQ56jWt7czBsqAGPq8J5BDnYVCg2tvKpvwTcMY1LS79iDabopdxoUzNLEZtRTH4ewAcKLJ4DM4V41fvrJGHgeKArxwmJ`
