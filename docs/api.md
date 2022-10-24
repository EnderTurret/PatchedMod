# API Documentation

From an API standpoint, there's not much that the mod itself provides.
The main things are [custom test condition registration](#custom-test-registration) and [rudimentary data generation support](#rudimentary-datagen)

### Custom Test Registration

Patched provides `PatchedTestConditions`, which you can use to register custom test conditions or evaluate them.

The main method of interest is `register(ResourceLocation, ITestEvaluator)`, and is where you can register your test conditions.
`ITestEvaluator` comes from the library, so you should look there for information on it.

A similar version is `registerSimple(ResourceLocation, ISimpleTestEvaluator)`, which is a version suitable for lambda test conditions only making use of the `value` property.

### Rudimentary Datagen

Patched also provides a patch `DataProvider` called `PatchProvider` which can be used to generate patch files.

To use it, simply override `registerPatches()` and call `patch(ResourceLocation)` to begin a patch file definition.
For the `ResourceLocation` argument, there are two `id()` helper methods that can make these less tedious to write.

Once you have a patch builder, you can call any of the methods named after an operation to add a patch of that operation. They are structured similar to patch files, so make sure to use compound patches when necessary!

When you've finished adding patches to a compound patch, you can call `end()` to terminate it. Terminating a "root patch" (the builder returned by `patch()`) does nothing, so you do not need to terminate those.