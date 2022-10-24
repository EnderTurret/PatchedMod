# PatchedMod

A Minecraft mod that adds a Json patching framework for use via resource/data packs.
It is an implementation of [RFC 6902](https://datatracker.ietf.org/doc/html/rfc6902) (with extensions) and its corresponding library can be found [here](https://github.com/EnderTurret/Patched).

This mod supports both Forge and Fabric, and can be found on [Modrinth](https://modrinth.com/mod/patched) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/patched).

## What this mod can do that vanilla packs cannot

Patched implements json patching functionality that can be leveraged by packs to patch parts of files -- that is, the mod gives packs the option to only change parts of json files.
This allows packs to avoid having to replace the entire file when they only need to change a small part of it.

For example, if a data pack in vanilla wants to add something to a loot table, it has to replace the entire file.
This makes it completely incompatible with any other data pack looking to do something similar to that loot table.
Patched lets these data packs change only the part of the loot table they want to change; the first data pack can add their loot to it, and the second data pack can perform its changes.
This means that the loot table will have the modifications from both data packs, instead of only one of them.

Another good example would be biomes.
If a data pack wants to change any attributes about a biome (such as world generation features), it has to replace the *entire* biome.
With Patched, it can change only the parts that need changing.

Lastly, Patched also makes it possible for packs to "post-process" or patch their own files.
With Patched's [`mod_loaded` test condition](docs/extensions.md#patchedmod_loaded), packs can add their own integration with mods.

For example, Terralith could patch their own biomes to add Biomes o' Plenty foliage when BoP is installed.
It could also add blocks from other mods to spruce up its biomes more than vanilla can allow (when those mods are installed).

Another example might be a mod that adds configuration support for packs.
This could be exposed through a test condition that packs could use to enable/disable features.

## Documentation

Look [here](docs/index.md).

## Building the mod

### Forge

Build the mod normally:

Linux: `./gradlew build`<br>
Windows: `gradlew.bat build`

### Fabric

Build the mod normally:

Linux: `./gradlew build`<br>
Windows: `gradlew.bat build`

Then delete the shaded jar (the one with `-all` in it).
This jar is the unremapped shadow jar, so it's safe to remove.