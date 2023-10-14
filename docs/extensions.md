# Extensions

As mentioned in [the library's documentation](https://github.com/EnderTurret/Patched/blob/main/docs/patches/differences.md), there are a few extensions available.
This mod enables all of them, so patches can make use of all the features of the library.
Additionally, it adds a few extra things.

## [Custom tests](https://github.com/EnderTurret/Patched/blob/main/docs/patches/ops/test.md#custom)

The library's documentation does not go into huge detail about these, since the library only provides the framework for declaring them.

Patched (the mod) provides one custom test type currently.

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

As of version `2.3.0+1.19.2`, you can also test against specific mod versions:

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