# Installation

### For users

For users, the mod can be obtained on either [Modrinth](https://modrinth.com/mod/patched) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/patched).

### For modders

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

#### On Forge

```gradle
implementation fg.deobf('maven.modrinth:patched:forge-<latest version here>+<minecraft version>')
```

#### On Fabric / Quilt

```gradle
modImplementation 'maven.modrinth:patched:<platform>-<latest version here>+<minecraft version>'
```

Something to note is that you still need a `pack.mcmeta` even when modding using Fabric/Quilt; if you want to have patches, you need to add one.
Normally you don't need one when using the Textile Loaders,
but it's really hard for Patched to decide whether you provide any patches if you don't have one for it to read.
If you're completely against the concept of a `pack.mcmeta`, you might be able to get away with this extremely barebones one:

```json
{
  "pack": {
    "patched:has_patches": true
  }
}
```