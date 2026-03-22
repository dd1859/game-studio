# Game Studio for Windows (Java 21+)

This project is now a **Windows-friendly desktop game studio shell** built in **Java 21+** with a Swing UI and **LWJGL Assimp** model importing.

## What you can do

- Open a real desktop UI with panels for project files, Java scripting, imported models, build output, and a built-in LWJGL preview.
- Create and edit `.java` gameplay/script files directly inside the studio.
- Compile the current Java source file with the installed JDK using **Java 21 only** settings.
- Import common 3D model formats such as **OBJ, FBX, GLTF, GLB, and DAE**.
- Package the app as a **fat jar** so it is easier to move and run, especially on Windows.

## Windows-friendly features

- Uses the system look and feel for a more native desktop experience.
- Produces a single runnable jar at `build/libs/game-studio.jar`.
- Bundles LWJGL native libraries for Windows and other major desktop platforms inside the jar build.
- Includes an embedded LWJGL OpenGL preview canvas directly inside the studio window.

## Java version requirement

This project is intentionally restricted to **Java 21 or newer**.

- Gradle compilation targets Java 21.
- The application checks the runtime version at startup and shows an error if Java is too old.

## Build the jar

```bash
gradle shadowJar
```

## Run from the jar

```bash
java -jar build/libs/game-studio.jar
```

## Build everything

```bash
gradle build
```

## In-app workflow

1. Launch the studio.
2. Edit Java files in the **Java Script Editor** tab.
3. Click **Compile Java** to build the selected script into the local workspace.
4. Click **Import 3D Model** to bring a model into `studio-workspace/assets/models`.
5. Review imported mesh/material/animation counts in the models table, details view, and the built-in LWJGL preview panel.
