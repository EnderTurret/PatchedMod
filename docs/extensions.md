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