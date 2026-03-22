# Game Studio for Windows (Java 26 only)

This project is a **Windows-friendly desktop game studio shell** built in **Java 26 only** with a Swing UI, **LWJGL Assimp** model importing, automatic Java installation discovery, and in-app game jar packaging.

## What you can do

- Open a desktop editor layout inspired by **Roblox Studio** and **Unreal Engine** with a workspace explorer, asset browser, scene outliner, details inspector, build output, and a 3D preview.
- Create and edit `.java` gameplay/script files directly inside the studio.
- Auto-detect installed Java runtimes/JDKs and highlight whether a **Java 26 JDK** is available.
- Compile the current Java source file with **Java 26 only** settings.
- Package the workspace source set into a runnable **game jar** directly from the app.
- Start from a seeded **ExampleGame.java** sample game that you can edit and package.
- Use the built-in **WASDController.java** script for movement defaults; it is editable, but if deleted the studio recreates it automatically.
- Import common 3D model formats such as **OBJ, FBX, GLTF, GLB, and DAE**.
- Package the app itself as a **fat jar** so it is easier to move and run, especially on Windows.

## Windows-friendly features

- Uses the system look and feel for a more native desktop experience.
- Produces a single runnable studio jar at `build/libs/game-studio.jar`.
- Bundles LWJGL native libraries for Windows and other major desktop platforms inside the jar build.
- Includes an embedded LWJGL OpenGL preview canvas directly inside the studio window.
- Shows a grounded baseplate stage plus a side-map style 3D overview when previewing imported models.
- Surfaces the main studio jar output path in the header, menu, toolbar, and status bar.
- Scans common Windows, macOS, Linux, SDKMAN, ASDF, and mise Java install locations when looking for Java 26.

## Java version requirement

This project is intentionally restricted to **Java 26 only**.

- Gradle compilation targets Java 26.
- The app's in-studio compiler and jar packager only use an auto-detected **Java 26 JDK**.
- If Java 26 is not found, the studio tells you where it looked and asks you to install Java 26 or point `JAVA_HOME` to it.

## Build the studio jar

```bash
gradle shadowJar
```

## Run the studio jar

```bash
java -jar build/libs/game-studio.jar
```

## Build everything

```bash
gradle build
```

## In-app workflow

1. Launch the studio.
2. Click **Find Java 26** so the app can auto-detect installed Java versions. The header and status bar show where the main studio jar is written: `build/libs/game-studio.jar`.
3. Use the left **Explorer** and **Asset Browser** panels like a lightweight studio shell, then review the right-side **Scene Outliner** and **Details Inspector** panels for context.
4. Edit Java files in the **Java Script Editor** tab.
5. Keep `WASDController.java` if you want the default movement system. You can edit it, but if you remove it the studio restores it automatically.
6. Click **Compile Java 26** to compile the selected script into the local workspace.
7. Click **Save Game Jar** to package the workspace sources into `studio-workspace/build/jars/<MainClass>.jar`.
8. Click **Import 3D Model** to bring a model into `studio-workspace/assets/models`.
9. Review imported mesh/material/animation counts in the models table, details view, and the built-in LWJGL preview panel.

## Included example game

The studio seeds `studio-workspace/src/ExampleGame.java` automatically on first launch. It auto-captures viewport input focus, uses `WASDController.java` for movement, supports Shift boost, and can be compiled or packaged as a runnable jar from inside the app.
