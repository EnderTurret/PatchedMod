# Extensions

As mentioned in [the library's documentation](https://github.com/EnderTurret/Patched/blob/main/docs/patches/differences.md), there are a few extensions available.
This mod enables all of them, so patches can make use of all the features of the library.
Additionally, it adds a few extra things.

## [Custom tests](https://github.com/EnderTurret/Patched/blob/main/docs/patches/ops/test.md#custom)

The library's documentation does not go into huge detail about these, since the library only provides the framework for declaring them.

Patched (the mod) provides three custom test types currently.

### `patched:mod_loaded`

As the name suggests, this condition is `true` if and only if a certain mod is loaded/present.
The best way to show its structure is likely through an example:

```json
{
  "op": "test"
  "type": "patched:mod_loaded"
  "value": "sodium"
}
```

As you might expect, this condition checks if Sodium (or one of its many forks) is loaded.

As of version `3.2.0+1.20.1`, you can also test against specific mod versions:

```json
{
  "op": "test",
  "type": "patched:mod_loaded",
  "value": {
    "mod": "minecraft",
    "version": "1.20"
  }
}
```

This tests to see if the Minecraft version is *at least* 1.20.
It will succeed if and only if the Minecraft version is 1.20 or some higher version (such as 1.20.1 or 1.21).
This example could be used in a data pack or multi-version mod to enable features only on newer Minecraft versions.

More realistically though, this would be used to add compatibility for a specific version of a mod:

```json
{
  "op": "test",
  "type": "patched:mod_loaded",
  "value": {
    "mod": "create",
    "version": "0.5.0"
  }
}
```

This would allow a data pack or mod to add compatibility features for newer versions of Create, while maintaining compatibility with older versions.

**NOTE**: Be careful with testing against specific versions, as it tends to break with mods that have the Minecraft version they're made for in their version, such as with `1.20.1-3.1.4`. In that case, only the Minecraft version can be tested.
It could also break with versions that contain letters, such as `0.5.1.b`. (And you're out of luck for snapshots or whatever `22w13oneblockatatime` is supposed to be.)
When dealing with a mod that has versions like this, it can be a better idea to use the simpler form of `patched:mod_loaded` that doesn't check the version.

Lastly, this might prove useful in the future:

```json
{
  "op": "test",
  "type": "patched:mod_loaded",
  "value": {
    "mod": "patched",
    "version": "<some future version here>"
  }
}
```

This could allow someone to use features Patched introduces later without causing errors or warnings to be printed in the logs when using older versions of Patched.

### `patched:registered`

This condition was added in `5.1.0+1.20.4` (and backported to `3.3.0+1.20.1`). It allows checking if something exists in a given registry:

```json
{
  "op": "test",
  "type": "patched:registered",
  "value": {
    "registry": "minecraft:entity_type",
    "id": "minecraft:breeze"
  }
}
```

Here the `registry` is the ID of a registry (in this case the entity type registry), and `id` is the ID of the thing to lookup in the registry.
The test succeeds if and only if the registry *and* thing are both registered (yes, registries are registered -- it's a long story).
If either of these aren't present (say, if you're trying to check for an origin but Origins isn't installed), the test fails.

The example test here checks if [the Breeze](https://minecraft.wiki/w/Breeze) is registered in the entity type registry.

Some caveats apply:
* `patched:registered` doesn't work on dynamic (or "data pack") registries, that is, ones like the biome registry.
	This is because those are not available during most client resource (re)loads, and not necessarily available during data-pack (re)loads either (e.g., checking for existence of a biome from the POV of another biome).
* The condition may also not play nicely with Minecraft's feature flags (what "[experiments](https://minecraft.wiki/w/Experiments)" are called internally).
	There's a chance that something that shouldn't be available is still "registered", or vice versa.
	This hasn't been tested at all, so tread carefully here.

### `patched:item_registered`

This is a simplified version of `patched:registered`, which only checks the item registry.
For example:

```json
{
  "op": "test",
  "type": "patched:item_registered",
  "value": "quark:crafter"
}
```

This tests if Quark's Crafter is present.
(Note: this doesn't guarantee that the crafter module is *enabled* -- see caveat #2.
Many mods that do "conditional" registration still register everything, and simply hide them from creative tabs and item lists.
This is usually so that opening saves or connecting to servers with different configs doesn't cause missing items.)