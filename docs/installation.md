# Installation

### For users

For users, the mod can be obtained on either [Modrinth](https://modrinth.com/mod/patched) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/patched).

### For modders

For modders, Patched can be added to your dev environment using Modrinth's maven:

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
implementation fg.deobf('maven.modrinth:patched:forge-<minecraft version>-<latest version here>')
```

#### On Fabric

```gradle
modImplementation 'maven.modrinth:patched:fabric-<minecraft version>-<latest version here>'
```