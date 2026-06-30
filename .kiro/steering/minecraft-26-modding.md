---
inclusion: fileMatch
fileMatchPattern: '*.java'
---

# Minecraft 26.x Fabric Modding Reference (26.1 & 26.2)

Verified facts gathered by inspecting the real 26.2 jars (`javap`) and the official
Fabric docs. Use this instead of re-researching. 26.1 is the **first unobfuscated**
Minecraft release — there are **no Yarn mappings**; code uses **Mojang official names**,
which sometimes differ from the historical "official mappings" used by NeoForge.

## Toolchain / versions

| Thing | Value |
|-------|-------|
| Minecraft | `26.1.2`, `26.2` |
| Fabric Loader | `0.19.3` |
| Fabric Loom | `1.17-SNAPSHOT` (plugin id `net.fabricmc.fabric-loom`) |
| Gradle | `9.5.1` |
| Java | `25` (min for Gradle JVM; compile `release = 25`) |
| Fabric API | `0.153.0+26.1.2` / `0.153.0+26.2` |
| ModMenu | `20.x` (26.2), `18.x` (26.1) — maven.terraformersmc.com |
| Mixin `compatibilityLevel` | `JAVA_25` |
| IntelliJ (for mixins) | 2025.3+ |

`fabric.mod.json` depends: `fabricloader >=0.19.3`, `minecraft >=26.1`, `java >=25`, `fabric-api *`.

## Build system (unobfuscated — no remap)

- Plugin: `net.fabricmc.fabric-loom` (NOT `fabric-loom`). No `mappings`, no Yarn.
- Use `implementation` (NOT `modImplementation`); `jar` (NOT `remapJar`).
- `minecraft "com.mojang:minecraft:${minecraft_version}"`
- ModMenu API: use `compileOnly` so it's not bundled.

```gradle
plugins { id 'net.fabricmc.fabric-loom' version "${loom_version}" }
dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    implementation "net.fabricmc:fabric-loader:${project.loader_version}"
    implementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
}
tasks.withType(JavaCompile).configureEach { it.options.release = 25 }
```

## Class renames (Yarn → Mojang 26.x)

| Yarn name | Mojang 26.x |
|-----------|-------------|
| `net.minecraft.client.MinecraftClient` | `net.minecraft.client.Minecraft` |
| `net.minecraft.client.option.GameOptions` | `net.minecraft.client.Options` |
| `net.minecraft.client.option.GraphicsMode` | `net.minecraft.client.GraphicsPreset` *(FAST, FANCY, FABULOUS, CUSTOM)* |
| `net.minecraft.client.option.CloudRenderMode` | `net.minecraft.client.CloudStatus` *(OFF, FAST, FANCY)* |
| `net.minecraft.client.option.KeyBinding` | `net.minecraft.client.KeyMapping` |
| `net.minecraft.client.util.InputUtil` | `com.mojang.blaze3d.platform.InputConstants` |
| `net.minecraft.client.font.TextRenderer` | `net.minecraft.client.gui.Font` |
| `net.minecraft.client.gui.DrawContext` | `net.minecraft.client.gui.GuiGraphicsExtractor` |
| `net.minecraft.client.gui.screen.Screen` | `net.minecraft.client.gui.screens.Screen` |
| `net.minecraft.client.gui.screen.TitleScreen` | `net.minecraft.client.gui.screens.TitleScreen` |
| `net.minecraft.client.gui.widget.ButtonWidget` | `net.minecraft.client.gui.components.Button` |
| `net.minecraft.client.gui.widget.SliderWidget` | `net.minecraft.client.gui.components.AbstractSliderButton` |
| `net.minecraft.text.Text` | `net.minecraft.network.chat.Component` |
| `net.minecraft.util.Identifier` / `ResourceLocation` | **`net.minecraft.resources.Identifier`** |
| `net.minecraft.registry.RegistryKey` | `net.minecraft.resources.ResourceKey` |
| `net.minecraft.world.World` | `net.minecraft.world.level.Level` |
| `net.minecraft.client.toast.SystemToast` | `net.minecraft.client.gui.components.toasts.SystemToast` |
| `DeltaTracker` | `net.minecraft.client.DeltaTracker` |

> **Gotcha:** Mojang 26.x calls it **`Identifier`** (package `net.minecraft.resources`),
> not `ResourceLocation`. Factory: `Identifier.fromNamespaceAndPath(ns, path)`.
> `id.toLanguageKey("prefix")` → `prefix.<ns>.<path>`.

## Method renames (the ones that bite)

**Minecraft (was MinecraftClient):**
- `getInstance()`, `.level` (was world), `.player`, `.options`, `.font` (was textRenderer)
- `getFps()` (was getCurrentFps)
- **`setScreenAndShow(Screen)`** — NOT `setScreen`
- `getCurrentServer()` (was getCurrentServerEntry) → `ServerData`
- `getWindow()` → `com.mojang.blaze3d.platform.Window`
- `getDebugOverlay()` → `DebugScreenOverlay`; `.showDebugScreen()` (was getDebugHud().shouldShowDebugHud())
- `getToastManager().addToast(toast)`

**Window:** `getWidth()/getHeight()` (framebuffer), `getGuiScaledWidth()/getGuiScaledHeight()` (was getScaledWidth/Height).

**Options (was GameOptions):** accessors return `OptionInstance<T>`:
- `renderDistance()` Integer, `simulationDistance()`, `entityDistanceScaling()` Double,
  `framerateLimit()`, `graphicsPreset()` *(was graphicsMode)*, `cloudStatus()`,
  `ambientOcclusion()` Boolean *(was getAo)*, `mipmapLevels()`, `enableVsync()`,
  `entityShadows()`, `gamma()`, `fov()`, `guiScale()`, `particles()`, `biomeBlendRadius()`
- `save()` *(was write)*, `applyGraphicsPreset(GraphicsPreset)`, `getEffectiveRenderDistance()`
- `preferredGraphicsBackend()` → `OptionInstance<PreferredGraphicsApi>` (DEFAULT/OPENGL/VULKAN, new in 26.2)

**OptionInstance<T>:** `get()`, `set(T)` *(was getValue/setValue)*.

**ResourceKey<Level>:** `level.dimension()` returns it; `.identifier()` *(was location())*, `.registry()`.

**Player / LivingEntity:** `getX/Y/Z()`, `isSprinting()`, `isFallFlying()` *(elytra; was isGliding)*,
`getAbilities().flying`, `hurtTime`, `swinging` *(field; was handSwinging)*.

**KeyMapping (was KeyBinding):**
- ctor `(String name, InputConstants.Type, int key, KeyMapping.Category)`
- `consumeClick()` *(was wasPressed)*
- `KeyMapping.Category`: `register(Identifier)`; built-ins MOVEMENT/MISC/MULTIPLAYER/...; label key = `id.toLanguageKey("key.category")` → `key.category.<ns>.<path>`

## GUI / rendering — the 26.1 render-state model

Rendering moved to an **extract** phase using `GuiGraphicsExtractor`.

**Screen overrides:**
- `extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta)` — main render (was `render`)
- `extractBackground(GuiGraphicsExtractor, int, int, float)` — custom background (was `renderBackground`); also `extractBlurredBackground`, `extractMenuBackground`, `extractTransparentBackground`
- Widgets: `addRenderableWidget(w)`, `addRenderableOnly`, `addWidget`, `clearWidgets`, `rebuildWidgets`
- `onClose()` (was close), `isPauseScreen()` (was shouldPause), `shouldCloseOnEsc()`
- Fields: `this.minecraft`, `this.font`, `this.width`, `this.height`

**GuiGraphicsExtractor draw methods (verified):**
- `fill(x1,y1,x2,y2,argb)`
- `fillGradient(x1,y1,x2,y2,argbTop,argbBottom)`
- `outline(x,y,w,h,argb)` — 1px rectangle border
- `horizontalLine(x1,x2,y,argb)`, `verticalLine(x,y1,y2,argb)`
- `text(Font, String|Component|FormattedCharSequence, x, y, argb[, boolean dropShadow])`
- `centeredText(Font, String|Component|..., x, y, argb)` *(no shadow overload)*
- `textWithWordWrap(...)`, `textWithBackdrop(...)`
- `blit(...)`, `blitSprite(...)`
- `enableScissor(x,y,x2,y2)`, `disableScissor()`, `pose()` → `Matrix3x2fStack`

**Font:** `width(String)`, `lineHeight` (field).

**Button:** `Button.builder(Component, onPress).bounds(x,y,w,h).build()` *(was .dimensions)*. `button.setMessage(Component)`.

**AbstractSliderButton (was SliderWidget):** ctor `(int x,y,w,h, Component msg, double value)`;
override `protected void updateMessage()` and `protected void applyValue()`; field `protected double value`.

**Mouse events (use `net.minecraft.client.input.MouseButtonEvent`):**
- `mouseClicked(MouseButtonEvent click, boolean doubled)`
- `mouseReleased(MouseButtonEvent click)`
- `mouseDragged(MouseButtonEvent click, double dragX, double dragY)`
- `mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)`
- `MouseButtonEvent`: `.x()`, `.y()`, `.button()`, `.modifiers()`

**Toasts:** `SystemToast.multiline(minecraft, SystemToast.SystemToastId.X, Component, Component)`; `minecraft.getToastManager().addToast(toast)`.

## Fabric API (26.x)

- **HUD:** `HudRenderCallback` REMOVED → `net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry`
  - `attachElementBefore(VanillaHudElements.CHAT, Identifier, HudElement)` / `attachElementAfter(...)`
  - `HudElement` = lambda `(GuiGraphicsExtractor, DeltaTracker)`; SAM method `extractRenderState`
  - `VanillaHudElements` has constants (e.g. `CHAT`)
- **Keybindings:** module `fabric-key-binding-api-v1` RENAMED → **`fabric-key-mapping-api-v1`**
  - `net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(KeyMapping)`
  - `getBoundKeyOf(KeyMapping)`
- `ClientTickEvents.END_CLIENT_TICK` callback param is `Minecraft`
- General renames: `ItemGroupEvents` → `CreativeModeTabEvents`; `ColorProviderRegistry.BLOCK` → `BlockColorRegistry`
- Removed: `fabric-convention-tags-v1`, `fabric-loot-api-v2`, `HudRenderCallback`, the `fabric` mod-id (use `fabric-api`)

## Util / math

- `net.minecraft.Util.getMillis()` (real-world ms)
- `net.minecraft.util.Mth` (sin, abs, clamp, lerp...)
- `net.minecraft.util.ARGB` (color helpers, e.g. `linearLerp`)
- `DeltaTracker.getGameTimeDeltaPartialTick(boolean ignoreFreeze)`

## Other 26.1 changes (common/server)

- `ItemStack` can't be built before a world loads → use `ItemStackTemplate`
- Recipe serializers = `MapCodec` + `StreamCodec` (no inner serializer class)
- Block render layers auto-derived from sprite (`ChunkSectionLayer`)
- Villager trades are data-driven (`TradeOfferHelper` gone)
- Fluids: `FluidModel` + `FluidRenderingRegistry` (replaces `FluidRenderHandler`)
- 26.2: OpenGL/Vulkan backend selectable via `PreferredGraphicsApi`

## How to verify a name yourself (no Google)

The deobfuscated merged jar Loom downloads:
```
~/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/<ver>/minecraft-merged-deobf-<ver>.jar
```
Inspect with JDK 25 `javap`:
```
javap -p -classpath <that-jar> net.minecraft.client.Minecraft
javap -p -classpath <that-jar> net.minecraft.client.Options
```
Fabric module jars: `~/.gradle/caches/modules-2/files-2.1/net.fabricmc.fabric-api/<module>/...`
Online: **mcsrc.dev** (decompiled MC source viewer).
