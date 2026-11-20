# Nyaa Client (Fabric, Minecraft 1.21.1)

Client-side mod with a module system, ClickGUI, HUD and JSON config. The mod icon and the HUD/GUI logo are the cat image you supplied.

Status: not built or run by the author. Verify with the checks below.

## Build

Requires JDK 21 and Gradle 8.10 or newer. The Gradle wrapper is not included (it could not be generated offline). Create it once with `gradle wrapper --gradle-version 8.14.3`, or open the folder in IntelliJ IDEA, which imports it directly.

    gradle build             (or ./gradlew build after creating the wrapper)
    gradle runClient         (starts a dev client with the mod)

The jar is written to `build/libs/nyaaclient-1.0.0.jar`. Drop it into `.minecraft/mods` together with Fabric API for 1.21.1.

Versions pinned in `gradle.properties` (confirm against https://fabricmc.net/develop/ for 1.21.1): Yarn `1.21.1+build.3`, Loader `0.16.9`, Fabric API `0.102.0+1.21.1`. Loom `1.8-SNAPSHOT` is set in `build.gradle`.

## Controls

- Right Shift opens the ClickGUI (rebind it in `config/nyaaclient.json`, module `ClickGUI`).
- Left click toggles a module or setting. Right click a module to expand its settings. Middle click a module, then press a key to bind it (Esc cancels, Backspace/Delete clears).
- Drag a panel header to move it. Right click a header to collapse it.
- Config is saved to `config/nyaaclient.json` when the GUI closes and when the game stops.

## Modules

| Category | Module | What it does |
|---|---|---|
| Combat | KillAura | Attacks the best target in range when the attack cooldown is ready. Range, FOV, sort, rotation and turn speed settings. |
| Combat | TriggerBot | Attacks the entity under the crosshair when the cooldown is ready. |
| Combat | Velocity | Cancels velocity packets for your own player (mixin). |
| Movement | Sprint | Sprints while moving forward. |
| Movement | Speed | Sets horizontal velocity along your input direction. |
| Movement | Fly | Creative-style flight with an adjustable speed. |
| Movement | NoFall | Sends an on-ground packet while falling. |
| Movement | Step | Raises the step height attribute. |
| Render | ESP | Through-wall boxes for players, hostile and passive mobs. |
| Render | Tracers | Lines from the crosshair to entities. |
| Render | Fullbright | Client-side night vision. |
| Misc | HUD | Watermark with the cat icon and the enabled-module list. |
| Misc | Main menu | Replaces the vanilla title screen with the Nyaa menu (borderless bold entries, wavy ribbons, twinkling stars). Toggle it off in the ClickGUI to get the vanilla title screen back. |

The menu entries are Singleplayer, Multiplayer, Minecraft Realms, Minecraft Settings, Client (opens the ClickGUI) and Quit Game.

## Mapping-sensitive spots to check first if the compile fails

These use Yarn 1.21.1 names that were written from memory, not from a compiler:

- `mixin/ClientPlayNetworkHandlerMixin.java`: method `onEntityVelocityUpdate`, `packet.getEntityId()`.
- `modules/Step.java`: `EntityAttributes.GENERIC_STEP_HEIGHT`.
- `modules/NoFall.java`: `PlayerMoveC2SPacket.OnGroundOnly(boolean)`.
- `util/LineBatch.java`: `Tessellator.begin(DrawMode.DEBUG_LINES, POSITION_COLOR)`, `BufferBuilder.endNullable()`, `GameRenderer::getPositionColorProgram`.
- `modules/Esp.java`, `Tracers.java`: `mc.getRenderTickCounter().getTickDelta(false)`.
- `modules/HudModule.java`, `gui/ClickGuiScreen.java`: the long `DrawContext.drawTexture(...)` overload.
- `gui/NyaaMainMenuScreen.java`: `RealmsMainScreen`, `MultiplayerWarningScreen`, `OptionsScreen`, `SelectWorldScreen` constructors, `options.skipMultiplayerWarning`, `client.scheduleStop()`, `DrawContext.fillGradient`.
- `modules/Speed.java`, `Sprint.java`: `player.input.movementForward` and `movementSideways`.

Values marked uncalibrated in the source (Speed, KillAura turn speed) are starting points, not tuned numbers.
