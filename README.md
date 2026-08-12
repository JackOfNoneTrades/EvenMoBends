# Even Mo' Bends

Maintainable Minecraft 1.7.10 fork of Mo' Bends 0.20.1.

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
| Smart Moving (with or without WawelAuth) | Smart Moving / Smart Render; Mo' Bends player features disabled | Enabled |

### WawelAuth coverage

When WawelAuth is installed without Smart Moving, Even Mo' Bends keeps its animated player model while WawelAuth continues to resolve authenticated skins and capes. The compatibility layer supports:

- modern 64x64 skins and the legacy layout;
- classic and slim player arms;
- independent left-arm and left-leg textures;
- hat, jacket, sleeves, and pants overlays across bent limb segments;
- WawelAuth's skin-layer visibility and armor-hiding rules; and
- the animated first-person right arm.

WawelAuth's optional voxel-thickness 3D skin layers currently fall back to the corresponding 2D overlays on the bent player model. Those voxel meshes assume vanilla single-piece limbs, so applying them unchanged would detach them at the elbows and knees.

### Smart Moving isolation

If either Smart Moving or Smart Render is detected, Even Mo' Bends does not construct or register its player renderer and does not create or update player animation data or sword trails. Zombie and spider animations remain available.

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
* [GT:NH buildscript](https://github.com/GTNewHorizons/ExampleMod1.7.10).

See [UPSTREAM.md](UPSTREAM.md) for exact source and binary provenance.

## License

`MIT`.

## Buy me some creatine

* [ko-fi.com](https://ko-fi.com/jackisasubtlejoke)
* Monero: `893tQ56jWt7czBsqAGPq8J5BDnYVCg2tvKpvwTcMY1LS79iDabopdxoUzNLEZtRTH4ewAcKLJ4DM4V41fvrJGHgeKArxwmJ`
