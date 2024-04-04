# Creating Patches

The [library documentation](https://github.com/EnderTurret/Patched/blob/main/docs/patches/patching_guide.md) describes generally how patches are created and I would recommend looking at that first.

### Patch structure

As stated in the other document, patches have a name similar to the file they patch, in the same location as the file.
Minecraft is a little different, so we're going to need to do a little more work.

Patched supports patching resource packs and data packs, and patching is about the same for both.

First, you need to add the following to the `pack.mcmeta` file in order to enable patches:

```json
{
  "patched": {
    "format_version": 1
  }
}
```

This is necessary so that Patched only scans packs that say they contain patches (so it's not spending 3 minutes looking in all 500 mods of your modpack).

Next, you need to add your patch.
Patch placement behaves similar to file replacement, a concept we're all familiar with already.

If you're trying to patch the file `minecraft:recipes/flint_and_steel.json`, the patch will be at `minecraft:recipes/flint_and_steel.json.patch`.
This corresponds to the following directory tree:

```
data
└ minecraft
  └ recipes
    └ flint_and_steel.json.patch
```

### Ordering

Another concept that Minecraft introduces is ordering -- that is, resource packs can be ordered so they apply before or after each other.
For example, Minecraft's resource pack is usually at the bottom, so other resource packs change resources in it.

This concept also extends to patching, where patches in "earlier" packs are applied earlier than others. Additionally, file replacements in later packs will not be affected by patches from earlier ones.

For example, consider the following ordering (ordered like the resource pack screen):

```
- pack 4 (patches the same file)
- pack 3 (replaces the same file)
- pack 2 (patches the same file)
- pack 1 (patches minecraft:models/item/stick.json)
- vanilla
```

In this example, the order of events are as follows:
* Pack 1 patches the stick item model
* Pack 2 also patches the stick item model
* Pack 3 replaces the stick item model, which discards any modifications the prior packs might have made
* Pack 4 patches pack 3's replacement

While the ordering of resource packs can be easily controlled, the ordering of data packs cannot be as easily changed. A mod might exist that fixes this, or maybe Mojang will one day allow data pack ordering to be configured.

In a modded environment, mod ordering is not something you or even modders have much control over.
Mods may define vague ordering dependencies, but not much else.
For the most part, the mod loader creates an ordering with no guarantee of consistency between runs.
It's a slightly better state than data packs, but not by much.

Generally, the ordering of packs should not matter.
In practice, it is unlikely that patches will ever clash in a way where ordering affects anything.

### Extensions

Patched has a few extensions (which you may have seen in its documentation).
The document [here](extensions.md) describes the extensions the mod has enabled.

### More on `pack.mcmeta`

Let's take a closer look at that `patched` section in the `pack.mcmeta` file. Here it is:

```json
{
  "patched": {
    "format_version": 1
  }
}
```

The only field here is `format_version`, which should be pretty obvious in purpose.
It only exists to specify the section schema version the pack was made for.
The current format version is 1.

For the most part, this is as bare-bones as it gets -- and it was plenty enough for older versions of the mod (prior to `5.1.0+1.20.4`).
Previously, this was all we had:

```json
{
  "pack": {
    // ...
    "patched:has_patches": true
  }
}
```

Just a simple toggle; nothing more, nothing less.
But nowadays Patched allows specifying a few other attributes.
In fact, let's take a look at them.

```json
{
  "patched": {
    "format_version": 1,
    "patch_targets": []
  }
}
```

What's this? We've gained another field!

#### Patch targets

The `patch_targets` field allows specifying additional files that patches should apply to.
Its intended purpose is for patching many files at once, like say if one wanted to remove a specific ore from every biome.
How about we take a look at this example use case.

```json
{
  "patched": {
    // ...
    "patch_targets": [
      {
        "pack_type": "server_data",
        "patch": "remove_copper_ore",
        "targets": [
          {
            "namespace": "minecraft",
            "path": {
              "pattern": "worldgen/biome/.*\\.json"
            }
          }
        ]
      }
    ]
  }
}
```

Here we have a single patch target which specifies that the `remove_copper_ore` patch should be applied to every `.json` file in the `worldgen/biome` folder of the `minecraft` namespace.

Let's examine the format in more detail.
To begin with, one may have any number of patch targets.
Additionally, one may have any number of targets in a patch target (these identify files that should be patched).
Finally, both the `namespace` and `path` may be regular expressions; a string specifies a specific match (which is good for performance)
and an object containing a `pattern` attribute specifies a regular expression.
In this example, both of these are in use.

The `pack_type` field may be either `server_data` or `client_resources`.
It is only necessary for contexts where a data pack and resource pack are merged, such as in mods.
In this case, since the `pack.mcmeta` is shared, Patched might try to apply the patch in both client and server contexts (which could cause performance problems).
The `pack_type` field allows disambiguating this, so that Patched only tries to apply the patch in the intended context.
Again, this is only important for mods or resource/data packs *packaged as mods* (yes, this is a thing -- in fact, Modrinth can do it automatically, which is actually a problem in this case).

Patches specified in patch targets are relative to a special `patches` folder in the root of the pack.
This leads nicely into the following section:

### Using `include` patches

Include patches are a relatively new patch operation (added in `5.1.0+1.20.4`):

```json
{
  "op": "include",
  "path": "add_diamond_loot_pool"
}
```

This operation lets you include the contents of another patch, as if it was copy-pasted into the one including it.
It's mainly intended to reduce patch duplication, such as when making the same changes to four different loot tables.

The `path` specifies the name of the patch to include.
These patches are read from the `patches` folder of the pack.
In this case, that looks like:

```
More Diamond Drops
├ data
│ └ minecraft
│   └ loot_tables
│     └ entities
│       └ allay.json.patch
├ pack.mcmeta
└ patches
  └ add_diamond_loot_pool.json.patch
```

In other words, for a `path` of "something", the file will be at `patches/something.json.patch`.