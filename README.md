# PatchedMod

A Minecraft mod that adds a Json patching framework for use via resource/data packs.
It is an implementation of [RFC 6902](https://datatracker.ietf.org/doc/html/rfc6902) (with extensions) and its corresponding library can be found [here](https://github.com/EnderTurret/Patched).

This mod supports [Forge](https://minecraftforge.net) (for 1.20.1 and below), [NeoForge](https://neoforged.net) (for 1.20.1 and above), [Fabric](https://fabricmc.net), and [Quilt](https://quiltmc.org/en),
and can be found on [Modrinth](https://modrinth.com/mod/patched) and [CurseForge](https://www.curseforge.com/minecraft/mc-mods/patched).

Patched's documentation can be found [here](docs/index.md).

## What this mod can do that vanilla packs cannot

Patched implements json patching functionality that can be leveraged by packs to patch parts of files -- that is, the mod gives packs the option to only change parts of json files.
This allows packs to avoid having to replace the entire file when they only need to change a small part of it.

For example, if a data pack in vanilla wants to add something to a loot table, it has to replace the *entire* file.
This makes it completely incompatible with any other data pack looking to do something similar to that loot table.

Here's a realistic scenario where one would encounter this problem: let's say you have two data packs A and B that both modify the `bat` loot table.
Data pack A adds leather as a drop to the loot table, and data pack B adds a custom "bat wing" item drop to the loot table.
Both data packs achieve this by replacing the loot table (since there are no other options), but this causes a problem: only one of the data packs' versions 'wins' and becomes the loot table the game uses.

Patched fixes this by letting these data packs change only the part of the loot table they want to change; both data packs can add their loot to the loot table without overwriting each other.
This means that the loot table will have the modifications from both data packs, instead of only one of them.

Another good example would be biomes.
If a data pack wants to change any attributes about a biome (such as world generation features), it has to replace the *entire* biome.

Another (less-contrived) realistic scenario: let's say you have two data packs A and B that both modify the desert biome.
Data pack A adds a new 'feature' (something that can be generated in the world) in order to spruce up the landscape of the desert biome a bit, and data pack B makes skeleton horses spawn naturally there.
Data pack A adds their feature to one of the sections in the `features` array, and data pack B adds an entry to the `creature` part of `spawners`.
Both data packs ship a replacement biome file (since that's the only way).
This of course results in the same problem: only one data pack's biome definition can win.

With Patched, the two data packs can modify the different parts of the biome separately, allowing both of their changes to persist.

Lastly, Patched also makes it possible for packs to "post-process" or patch their own files.
With Patched's [`mod_loaded` test condition](docs/extensions.md#patchedmod_loaded), packs can add their own integration with mods.

For example, Terralith could patch their own biomes to add Biomes o' Plenty (or BYG) foliage when that mod is installed.
It could also add blocks from other mods to spruce up its biomes more than vanilla can allow (when those mods are installed).

Another example might be a mod that adds configuration support for data/resource packs.
This could be exposed through [a test condition](docs/api.md#custom-test-registration) that these packs could use to enable/disable features.

> [!IMPORTANT]
> Patched is *not* a magic solution to data/resource pack incompatibility; installing it will not automatically make things compatible with each other.
> Patched does nothing by itself. Packs have to be written to make use of Patched, and only then will they be compatible with each other.

## Why use Patched over <insert mod-loader-specific biome/loot/whatever modification API>?

The primary thing is discoverability.
Patched provides a way to easily see how a file is being patched and by who, which allows someone -- say, a modpack developer -- to know exactly what is going on in said file.
Additionally, it allows one to write "counter-patches" to patch out someone else's changes (if necessary).

The same cannot be said for the many modification APIs, which at best might log the changes each mod makes (in practice this never happens).
For a modpack developer, unless they look through every mod's source code, the changes that are being made are completely invisible to them; they have no idea who's changing what.
Even worse, not all of these changes may be configurable. Some might, but there will be mods that simply don't provide the ability to configure them.

For those who have dabbled with both Forge and Fabric before, you may have noticed how all of Forge's changes are immediately visible in Minecraft's code, whereas Fabric's changes are (or were -- I haven't checked recently) hidden behind mixins that are applied at runtime.
This is similar to the modification API vs. patches difference, where the changes made by patches are perfectly visible (via commands), but the changes made using the modification API are invisible (like mixins).

If you use Patched, your changes to Minecraft's data are visible via commands, and can be changed or overwritten by data packs or other mods.
Even better, instead of needing to maintain a chunk of code for an API that could change in the future, you would only have to write the patch file (and you can data-gen them!),
which is more likely to stay compatible with future Minecraft versions since Mojang is less prone to rewriting the json format of something than they are to rewriting a bunch of code (since they have data packs to think about).

### Okay, but how do you configure a patch?

Mods can add custom [test conditions](docs/api.md#custom-test-registration) which can check config files for a value of some kind.
Once one has been defined, it can be used like so:

```json
[
  {
    "op": "test",
    "type": "mymod:config_value",
    "value": "Desert Features"
  },
  {
    "op": "add",
    "path": "/features/4/-",
    "value": "mymod:some_desert_feature"
  }
]
```

### Obligatory caveat

One thing that should be mentioned is that one major use-case of the modification APIs is blanket-modifying a bunch of things based on a condition.
For example, modifying every forest biome to add a feature of some kind.
As of the current moment, Patched provides no way to do this, so the only alternative approach is to copy-paste the same patch for every single thing that needs to be modified -- which is not really a solution.
It is for this reason that Patched does not and cannot "replace" these modification APIs.

## Why use Patched over any of the other json patching mods?

While there are other mods that allow a person to patch json files (it's not exactly a *new* concept),
Patched tries very hard to make sure that errors are handled gracefully (your game shouldn't crash if a patch fails) and that the changes that are made can be easily viewed.
And while I'm not saying other mods don't have decent error handling, I haven't seen another json patching mod that allows viewing the patched json data or any of the other informational features that Patched provides.

In addition, Patched is a small dependency (only ~120 kB) and doesn't pull in any dependencies itself (such as a language provider) except for its backing library (which is shaded in -- you won't even notice it's there).
It also takes a more future-proof approach to things by keeping the actual json-patching algorithm Minecraft-unaware, which prevents that part from breaking if Mojang changes resource loading or something again for the fifteenth time this update.

However, I would say the main appeal of Patched is the user-friendliness of being able to list/dump patches, dump files in patched/unpatched forms, and error messages that tell you what went wrong where (and that don't crash the game when they happen).

## Building the mod

Build the mod like one would build any other mod:

Linux: `./gradlew build`<br>
Windows: `gradlew.bat build`

Each platform's binaries will be in the `libs` folder of the platform's corresponding project folder.
(For example, the Fabric binaries are in `fabric/build/libs`.)

### For Fabric and Quilt

There's one more manual step that should be performed after running `build`: the shaded jar (the one with `-all` in it) needs to be deleted.
This jar is the unremapped shadow jar, so it's safe to remove.
Strictly-speaking it doesn't *need* to be deleted, but it might cause confusion otherwise.