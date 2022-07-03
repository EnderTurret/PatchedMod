# PatchedMod

A Minecraft mod that adds a Json patching framework for use via resource/data packs.

This mod currently supports both Forge and Fabric.

## Usage

First, you'll need the mod installed. See the [next section](#installation) for details.

In order to add patches to a resource or data pack, you will need to start by
inserting this into your `pack.mcmeta` file somewhere in the `pack` block:

```json
"patched:has_patches": true
```

This is an optimization to avoid searching every pack for patches when only a few packs might have them.

Next, just add a patch file. They follow the name pattern `<json file to patch>.json.patch`.

For example, this patch (`amethyst_block.json.patch` in `assets/minecraft/models/block`) changes the texture of the Amethyst Block to that of a Beacon (the inner part, anyway):

```json
[
  {
    "op": "replace",
    "path": "/textures/all",
    "value": "minecraft:block/beacon"
  }
]
```

## Commands

This mod comes with a bunch of informational commands that can be used to debug patches or see what all is going on.

There is `/patched` for server-side patches and `/patchedc` for client-side patches.
Both of these function exactly the same. They're just different in what side they act upon.
Just remember that `/patched` is for data packs and `/patchedc` is for resource packs.

### /patched dump

This subcommand is used to view patched files and patches themselves.

For example, `/patchedc dump patch <amethyst pack> minecraft:models/block/amethyst_block.json.patch`
dumps the amethyst block model patch from earlier.

Additionally, `/patchedc dump file minecraft:models/block/amethyst_block.json`
dumps the patched amethyst block model. This is the file as the game sees it.

### /patched list

This subcommand is used to view a list of packs with patches and the patches contained within those packs.

For example, `/patchedc list packs` would output the name of the resource pack containing that model patch,
if loaded.

Additionally, `/patchedc list patches <amethyst pack>` would output a list of patches contained within the
pack that has that amethyst model patch.

## Installation

### For users

Simply grab a copy of the mod from Modrinth or CurseForge and drop it in your mods folder.

### For modders

Add this repository to your build.gradle:

```gradle
maven {
    name = 'Modrinth'
    url = 'https://api.modrinth.com/maven'
    content {
        includeGroup 'maven.modrinth'
    }
}
```

and then add one of these to your dependencies:

For Forge:

```gradle
implementation fg.deobf('maven.modrinth:patched:forge-1.18.2-1.0.0')
```

For Fabric:

```gradle
modImplementation 'maven.modrinth:patched:fabric-1.18.2-1.0.0'
```