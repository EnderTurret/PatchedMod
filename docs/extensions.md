# Extensions

As mentioned in [the library's documentation](https://github.com/EnderTurret/Patched/blob/main/docs/patches/differences.md), there are a few "extensions" available.
This mod enables all of these extensions, so you can use all of the features of the library.
Additionally, it adds a few extra things.

## Custom tests

The library's documentation does not go into huge detail about these, since the library only provides the framework for declaring them.

Patched (the mod) provides one custom test type currently.

### `patched:mod_loaded`

As the name suggests, this condition is `true` only if a certain mod is loaded/present.
The best way to show its structure is likely through an example:

```json
{
  "op": "test"
  "type": "patched:mod_loaded"
  "value": "optifine"
}
```

As you might expect, this condition checks if OptiFine is loaded.