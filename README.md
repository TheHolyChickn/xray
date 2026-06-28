# Hyperion Mod

This mod adds Hyperion to your game for use in production.

## Registering Episodes

An episode should be a class extending the abstract class [Episode](src/client/kotlin/com/github/theholychickn/xray/client/episode/Episode.kt).
It takes the following parameters:
- `id: String` An episode is referenced by referencing its `id`.
  - Upload $n$ should be given `id = "episode_n"`
  - Apollyon upload $n$ should be given `id = "apollyon_upload_n"`
- `shouldRenderThoughts: Boolean` Whether or not to render the Hyperion thought summary GUI. Set to false for all player episodes, and true for relevant Apollyon uploads.
- `thoughtLog: List<ThoughtEntry>` A list of entries in the thought log.
  - A `ThoughtEntry` which displays "Text" for $10$ms is declared as
    ```kt
    ThoughtEntry("Text to appear in the thought log", 10)
    ```
- `blockKeybindMap: Map<Int, List<ChangeSpec>>` Defines keybinds for changing blocks.
  - To find the int corresponding to keybind `key`, use `GLFW.GLFW_KEY_$key`
  - If you want to change a single block at coordinates $(x,y,z)$ to `state: BlockState`, use `ChangeSpec.Single(BlockPos(x,y,z),  state)`
  - If you want to change a rectangular region with corners $v1$ and $v2$ to `state: BlockState`, use `ChangeSpec.Region(BlockPos(v1), BlockPos(v2), state)`
  - Most `BlockState`s can be accessed by doing something like `Blocks.STONE`
  - The key is then bound to setting every block registered by a `ChangeSpec` in the list to its target block
  - 
You should also override the `keybinds` property of an `Episode`. `keybinds` is a `Map<KeyMapping, (Minecraft) -> Unit>`. A `KeyMapping`
should always be declared by
```kt
KeyMapping(
    name = "key.hyperion.name_of_the_keybind",
    key = GLFW.GLFW_KEY_H,
    HyperionModClient.category
)
```
This example constructs a keybind for the key "H". Note that it does not register an action to this keybind. We need to provide an action to this keybind;
the `Episode` and `Episodes` classes handle registering the keybind with this action. The registration exposes the `client: Minecraft`,
which is useful for spawning entities and sending chat messages. An example `action: (Minecraft) -> Unit`:
```kt
action = { client ->
    client.player?.sendSystemMessage(
        Component.literal("[Hyperion] haiiiiiiiiiiii")
    )
}
```

After writing an episode class, add the episode to the `Episodes.register(...)` block in `HyperionModClient.kt.onInitializeClient()`. To
set it as the active episode, go to the config file, and set the `activeEpisode` field to your episode's `id`.


### Convenience functions for keybinds

The `Episode` class has several convenience methods for constructing keybinds.

To assign a list of Hyperion actions to a keybind, use `hyperionActionKeybindBuilder` as follows:
```kt
hyperionActionKeybindBuilder(
    name,
    key
) { hyperionLoadoutBuilder ->
    say("hai im hyperion :3")
    giveItem(hyperionLoadoutBuilder.fortunePick)
    wait(10) // time in ticks
    say("now i have a pickaxe owo")
}
```
The method exposes a `HyperionLoadout.Builder` in the lambda to manage Hyperion's inventory. See [HyperionAction](src/main/kotlin/com/github/theholychickn/xray/entities/HyperionAction.kt)
for all possible actions that can be given to Hyperion.

To spawn Hyperion, use the `spawnHyperion` method as follows:
```kt
keybinds += (KeyMapping(...) to { client -> spawnHyperion(client, x, y, z, loadout) }) 
```
If `loadout` is not passed, it defaults to `HyperionLoadout.Builder.defaultLoadout`

For more complex methods involving Hyperion, you can use `Minecraft.withHyperion { hyperion -> (...) }`.