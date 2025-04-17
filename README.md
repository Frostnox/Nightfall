# Nightfall
Nightfall is a total overhaul that reimagines Minecraft survival to deliver a challenging, immersive, and mechanically deep experience inside a novel dark fantasy universe.

For more info, visit the [Modrinth page](https://modrinth.com/mod/frostnox-nightfall).

If you wish to contribute, see ```CONTRIBUTING.md```.

## Mod & Modpack Development
Nightfall's codebase has been developed with addon mods and resource packs in mind.

Before you explore the codebase yourself, here's a more technical overview of unique features:

### Actions
Actions are implemented through a custom Forge registry. You may use it like any other registry.

### Spawn Groups
Entities no longer spawn using the vanilla methods.

Instead, all entities are spawned by picking a random spawn group.

Spawn groups are implemented through a custom Forge registry. You may use it like any other registry.

Make sure to add your spawn group to one of ```TagsNF.SURFACE_GROUPS```, ```TagsNF.FRESHWATER_GROUPS```, or ```TagsNF.OCEAN_GROUPS```.

Be careful when spawning entities that do not despawn on their own as the world does not take into account how many entities are already present.

### Food Groups
Food groups are implemented through item tags.

Adjust the food group size by adding the item to ```TagsNF.MIXTURE_X``` tags. The final value is the summation of all added tags.

### Gravity
Gravity is implemented through block tags.

```TagsNF.HAS_PHYSICS``` will cause the block to be affected by gravity.

```TagsNF.SUPPORT_X``` tags will provide integrity equal to the summation of all added tags.

```TagsNF.FLOATS``` will let the block be supported by fluids.

### Dynamic Lighting
```EntityLightEngine``` automatically tracks and updates dynamic lighting for entities with a ```LightData``` capability.

Nightfall uses these only for the player and dropped items, but you can add this capability to any entity.

Implement ```IItemLightSource``` in items to add dynamic lighting when the item is dropped.

### Encyclopedia Categories
Categories only exist on the client. You may add a new category by calling ```ClientEngine.registerCategory```.

### Encyclopedia Entries
Entries are implemented through a custom Forge registry. You may use it like any other registry.

Make sure to call ```ClientEngine.registerEntry``` to add the necessary client data.

### Knowledge
Knowledge is implemented through a custom Forge registry. You may use it like any other registry.

You should typically create a Knowledge object for all items and item tags you add.

### World Conditions
World conditions are implemented through a custom Forge registry. You may use it like any other registry.

### World Generation
Apart from features and structures, world generation cannot be changed and is hardcoded.

The base terrain is done in a single pass, so there are no surface rules or carvers.

Biomes in the vanilla sense essentially do not exist anymore.

Biomes do technically exist but are hardcoded to select only from Nightfall's pool.

Nightfall's features and structures do not use biomes when generating, instead opting for direct temperature and humidity values.

### Wrapped Registries
A number of custom Forge registries exist that store enums wrapped as interfaces.

This includes Armaments, Dyes, Metals, Soils, Stones, Styles, TieredArmorMaterials, and Trees.

This allows for easily querying and iterating values across multiple mods without explicit implementation.

For example, any registered TieredArmorMaterial will have a model automatically baked for use with the renderer.

Make sure to register your enums during the corresponding registry event.