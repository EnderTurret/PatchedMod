# Installation

## For users and data/resource pack developers

The mod can be obtained on either [Modrinth](https://modrinth.com/mod/patched) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/patched).
Just drop it in the `mods` folder and things should just work.
On Fabric and Quilt you will also need the corresponding API installed, although on newer versions Patched can run without them in a sort of degraded functionality mode.

## For modders

For modders, Patched can be added to your dev environment using Modrinth's maven.
(Technically one could also use CurseForge instead via CurseMaven, but then you have to deal with file/project ids and it all gets kind of messy.)

```gradle
maven {
    name = 'Modrinth'
    url = 'https://api.modrinth.com/maven'
    content {
        includeGroup 'maven.modrinth'
    }
}
```

Next, you can add Patched as a dependency:

#### On Forge (≤1.20.1)

```gradle
implementation fg.deobf('maven.modrinth:patched:forge-<latest version here>+<minecraft version>')
```

#### On NeoForge (>1.20.1)

```gradle
implementation 'maven.modrinth:patched:neoforge-<latest version here>+<minecraft version>'
```

#### On Fabric / Quilt

```gradle
modImplementation 'maven.modrinth:patched:<platform>-<latest version here>+<minecraft version>'
```

### Patched metadata in the mod metadata

As of `7.1.0+1.21.1` (and backported to `3.3.0+1.20.1`), mods can optionally place the `patched` block that would normally be in the `pack.mcmeta` in their loader-specific metadata instead.
What this looks like for each loader is described below.
Regardless, the overall structure is identical to that of the `patched` `pack.mcmeta` block.

**Note**: if you have multiple packs in your mod, this metadata *may or may not* apply to all of them.

#### On (Neo)Forge

The Patched metadata can be placed in your (`neoforge.`)`mods.toml` like so:

```toml
[modproperties.<mod id>.patched]
    format_version = 1 # This tells Patched we want to patch things.

# Declares a "dynamic patch", i.e. a patch applying to more than one file.
# Few mods will need this functionality, but it's shown here for better understanding of how the declaration translates into TOML.
# This example applies a patch to every biome file.
[[modproperties.<mod id>.patched.patch_targets]]
    pack_type = "server_data"
    patch = "remove_all_biome_features"
    targets = [
        { namespace = ["minecraft"], path = [{ pattern = "worldgen/biome/.*\\.json" }] }
    ]
```

#### On Fabric

The Patched metadata can be placed in your `fabric.mod.json` like so:

```json
{
  // alongside the rest of your mod metadata:
  "custom": {
    "patched": {
      "format_version": 1,
      "patch_targets": [
        {
          "pack_type": "server_data",
          "patch": "remove_all_biome_features",
          "targets": [
            {
              "namespace": ["minecraft"],
              "path": [
                {
                  "pattern": "worldgen/biome/.*\\.json"
                }
              ]
            }
          ]
        }
      ]
    }
  }
}
```

#### On Quilt

The Patched metadata can be placed in your `quilt.mod.json` like so:

```json
{
  // alongside the rest of your mod metadata:
  "patched": {
    "format_version": 1,
    "patch_targets": [
      {
        "pack_type": "server_data",
        "patch": "remove_all_biome_features",
        "targets": [
          {
            "namespace": ["minecraft"],
            "path": [
              {
                "pattern": "worldgen/biome/.*\\.json"
              }
            ]
          }
        ]
      }
    ]
  }
}
```

This is similar to the Fabric version, but with the important detail that there's no `custom` block.