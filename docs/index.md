# PatchedMod Documentation

Before reading through this, you might want to look at [the library's documentation](https://github.com/EnderTurret/Patched/blob/main/docs/index.md) as it documents most of the things you will encounter.

Here is a list of all the pages in this documentation:
* [Installation](installation.md)
	* [For users and data/resource pack developers](installation.md#for-users-and-data-resource-pack-developers)
	* [For modders](installation.md#for-modders)
* [Creating patches](creating_patches.md)
	* [Patch structure](creating_patches.md#patch-structure)
	* [Ordering](creating_patches.md#ordering)
	* [Using `include` patches](creating_patches.md#using-include-patches)
* [Extensions](extensions.md)
	* [`patched:mod_loaded`](extensions.md#patchedmod_loaded)
	* [`patched:registered`](extensions.md#patchedregistered)
	* [`patched:item_registered`](extensions.md#patcheditem_registered)
* [Commands](commands.md)
	* [The `list` subcommand](commands.md#the-list-subcommand)
	* [The `dump` subcommand](commands.md#the-dump-subcommand)
* [API (for modders)](api.md)
	* [Custom test registration](api.md#custom-test-registration)
	* [Patch datagen](api.md#patch-datagen)