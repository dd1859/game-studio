package com.example.gamestudio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public final class GamePackager {
    private GamePackager() {
    }

    public static String packageGame(Path sourceDirectory, Path stagingDirectory, Path jarOutputDirectory, String mainClassName) throws IOException {
        if (mainClassName == null || mainClassName.isBlank()) {
            throw new IOException("A main class name is required to package the game jar.");
        }

        List<Path> javaSources = collectJavaSources(sourceDirectory);
        if (javaSources.isEmpty()) {
            throw new IOException("No Java source files were found in " + sourceDirectory.toAbsolutePath());
        }

        Files.createDirectories(stagingDirectory);
        Files.createDirectories(jarOutputDirectory);
        cleanDirectory(stagingDirectory);

        String compilationLog = JavaProjectCompiler.compileAll(javaSources, stagingDirectory);
        Path jarPath = jarOutputDirectory.resolve(mainClassName + ".jar");
        createJar(stagingDirectory, jarPath, mainClassName);

        return compilationLog
            + System.lineSeparator()
            + "Packaged runnable jar: " + jarPath.toAbsolutePath() + System.lineSeparator()
            + "Run it with Java 26: java -jar " + jarPath.toAbsolutePath();
    }

    private static List<Path> collectJavaSources(Path sourceDirectory) throws IOException {
        try (var stream = Files.walk(sourceDirectory)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> path.getFileName().toString().endsWith(".java"))
                .sorted()
                .toList();
        }
    }

    private static void createJar(Path classesDirectory, Path jarPath, String mainClassName) throws IOException {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.put(Attributes.Name.MANIFEST_VERSION, "1.0");
        attributes.put(Attributes.Name.MAIN_CLASS, mainClassName);
        attributes.putValue("Created-By", "Game Studio");
        attributes.putValue("Build-Java-Version", Integer.toString(JavaInstallationFinder.REQUIRED_JAVA_FEATURE));

        try (OutputStream outputStream = Files.newOutputStream(jarPath);
             JarOutputStream jarOutputStream = new JarOutputStream(outputStream, manifest)) {
            try (var stream = Files.walk(classesDirectory)) {
                for (Path file : stream.filter(Files::isRegularFile).sorted().toList()) {
                    String entryName = classesDirectory.relativize(file).toString().replace('\\', '/');
                    JarEntry entry = new JarEntry(entryName);
                    jarOutputStream.putNextEntry(entry);
                    try (InputStream inputStream = Files.newInputStream(file)) {
                        inputStream.transferTo(jarOutputStream);
                    }
                    jarOutputStream.closeEntry();
                }
            }
        }
    }

    private static void cleanDirectory(Path directory) throws IOException {
        if (Files.notExists(directory)) {
            return;
        }

        try (var stream = Files.walk(directory)) {
            for (Path path : stream.sorted(Comparator.reverseOrder()).toList()) {
                if (!path.equals(directory)) {
                    Files.deleteIfExists(path);
                }
            }
        }
        Files.createDirectories(directory);
    }
}
