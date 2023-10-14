# Commands

Patched adds a few commands that can be used to inspect the state of resources.
They are useful when trying to figure out why your patches aren't working, or in creating patches in the first place.
They can also be used if you're curious what patches are doing.

Patched adds two commands for these purposes: `/patched` and `/patchedc`.
The difference is that `/patched` examines server-side resources -- that is, resources from data packs -- whereas `/patchedc` examines client-side resources (from resource packs).

### The `list` subcommand

The list subcommand allows listing the packs with patching enabled, as well as the patches contained within these packs.

For the first one, `/patched list packs` will list all the packs with patching enabled.

For the second, `/patched list patches <pack>` will list all the patches belonging to a specified pack. You can use the first command to find the pack you're looking for.

### The `dump` subcommand

This is the more useful subcommand -- it allows dumping the contents of patches and patched files, and are what separates this mod from being "just another patching mod" (not that there are many of those).

`/patched dump patch <pack> <location>` allows you to dump the contents of a patch, which can be used to figure out what a pack is doing (and possibly why.)

`/patched dump file <location>` allows you to dump the contents of a patched file.
The output of the command will also include comments indicating what elements were changed/added/removed and by who.
For example:

```json
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": [
    { // replaced by Better Steel
      "item": "minecraft:netherite_ingot"
    },
    {
      "item": "minecraft:flint"
    }
  ],
  "result": {
    "item": "minecraft:flint_and_steel"
  }
}
```

In this example, it is very clear what was changed, and we even know who changed it.
Specifically, we can gather that the "Better Steel" data pack replaced the ingredient with a different one.

There are also a few variations of this command.
Using the previous example, here's what it looks like using `/patched dump file <location> raw`:

```
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": [
    {
      "item": "minecraft:netherite_ingot"
    },
    {
      "item": "minecraft:flint"
    }
  ],
  "result": {
    "item": "minecraft:flint_and_steel"
  }
}
```

Looks pretty plain, right? This is the patched file without comments (which is what the code reading the file actually sees).

Finally, the command `/patched dump file <location> unpatched` can be used to dump the original file:

```
{
  "type": "minecraft:crafting_shapeless",
  "ingredients": [
    {
      "item": "minecraft:iron_ingot"
    },
    {
      "item": "minecraft:flint"
    }
  ],
  "result": {
    "item": "minecraft:flint_and_steel"
  }
}
```