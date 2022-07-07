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

There is [a guide](#creating-patches-a-guide) on creating patches later in this document.

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

Simply grab a copy of the mod from Modrinth and drop it in your mods folder.

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

## Creating Patches: a Guide

Patches are named after the file they modify and are located in the same directory.
All patches also end with the extension `.patch`.
For example, `assets/minecraft/models/block/amethyst_block.json.patch` modifies the file `amethyst_block.json` at the location `assets/minecraft/models/block`.
This translates to the file `minecraft:models/block/amethyst_block.json`.

Before creating a patch, you first need to know what changes you're trying to make.
This could be *adding* a structure, *replacing* a texture, *removing* an item from a tag, etc.
Notice these actions here, as these are what your patches will need to perform.

### A simple patch

Let's say you want to achieve the third example listed earlier: removing an item from a tag.

Maybe you have the tag `mytag:cool_items` that looks like this:

```json
{
  "replace": false,
  "values": [
    "minecraft:diamond",
    "minecraft:netherite_scrap",
    "minecraft:sea_pickle"
  ]
}
```

And maybe you're not a huge fan of pickles, and want to remove it.
This could be achieved like so:

```json
[ // A list of patches to apply
  { // A single patch
    "op": "remove", // The operation, in this case "remove"
    "path": "/values/2" // The path to the value to remove
  }
]
```

In this patch, we're *removing* the element at index 2 in `values`.
This might seem like we'd be removing `minecraft:netherite_scrap`, but array indices start at 0.
This would place `minecraft:netherite_scrap` at index 1 and `minecraft:sea_pickle` at index 2.

### Json paths

The most important part of a patch is the `path` element,
as it identifies what element is being modified by the patch.

Fortunately, they're not particularly complicated.

Each path begins with a slash (`/`) and is made up of zero or more sub-paths.
Each sub-path has a slash between them.
For example, `/values/2` has two sub-paths, and there is a slash between `values` and `2`.

A sub-path can be either the name of an element, or an index in an array (remember that indices start at 0).

For example, `/value` points to the `value` element in this Json document:

```json
{
  "value": 1
}
```

For another example, `/1` points to the second element in this Json document:

```json
[
  "a string",
  3
]
```

What happens if you have an element with no name, or an element with a slash as a name?
Such as in this document:

```json
{
  "": [1, 2, 3],
  "/": true
}
```

First off, you can use `//` to path into the first element.
(I'm pretty sure that targeting that element is not currently possible because of ambiguity with the root document.)

Next, there is a way to escape a slash so that it isn't interpreted as part of the path.
The string `~1` is interpreted as a `/`, and since tildes are now special, `~0` is interpreted as a `~`.
This allows for the path `/~1` to point to the second element in that document.

Lastly, `-` points to the end of an array, or an element named `-`.
For example, in this document:

```json
{
  "-": 1,
  "array": [3, 2, 1]
}
```

The path `/-` refers to the first element (1), and the path `/array/-` refers to index 3,
or in other words, the fourth element in the array (which doesn't exist).

This can be used to add elements to the end of arrays.

### Operations

There are currently 7 operations:
* `add`
* `copy`
* `find`
* `move`
* `remove`
* `replace`
* `test`

For most of them, I would refer to [this site](https://jsonpatch.com#operations) for an explanation on how they work.
Additionally, see [this repository](https://github.com/EnderTurret/Patched) on the `find` operation and extensions to the `test` operation.