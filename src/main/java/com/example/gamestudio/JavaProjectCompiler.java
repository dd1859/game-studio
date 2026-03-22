package com.example.gamestudio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public final class JavaProjectCompiler {
    private JavaProjectCompiler() {
    }

    public static String compile(Path sourceFile, Path outputDirectory) throws IOException {
        return compileAll(List.of(sourceFile), outputDirectory);
    }

    public static String compileAll(List<Path> sourceFiles, Path outputDirectory) throws IOException {
        if (sourceFiles.isEmpty()) {
            throw new IOException("No Java source files were provided for compilation.");
        }

        Files.createDirectories(outputDirectory);
        cleanGeneratedClasses(outputDirectory);

        JavaInstallationFinder.JavaInstallation requiredJdk = JavaInstallationFinder.findRequiredJdk();
        if (requiredJdk != null) {
            return compileWithExternalJavac(sourceFiles, outputDirectory, requiredJdk);
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return "No Java 26 JDK was auto-detected, and no in-process system compiler is available. "
                + "Install Java 26 and point JAVA_HOME to it.";
        }

        int currentFeature = Runtime.version().feature();
        if (currentFeature != JavaInstallationFinder.REQUIRED_JAVA_FEATURE) {
            return "Compilation requires Java 26 exactly. The current in-process compiler is running on Java "
                + currentFeature + ". Install a Java 26 JDK so the studio can auto-detect and use it.";
        }

        return compileWithSystemCompiler(sourceFiles, outputDirectory, compiler);
    }

    private static String compileWithSystemCompiler(List<Path> sourceFiles, Path outputDirectory, JavaCompiler compiler) throws IOException {
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StringWriter compilerOutput = new StringWriter();

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjectsFromPaths(sourceFiles);
            List<String> options = List.of(
                "--release", Integer.toString(JavaInstallationFinder.REQUIRED_JAVA_FEATURE),
                "-d", outputDirectory.toAbsolutePath().toString()
            );

            boolean success = Boolean.TRUE.equals(compiler.getTask(compilerOutput, fileManager, diagnostics, options, null, units).call());
            return formatCompilationResult(sourceFiles, outputDirectory, compilerOutput.toString(), diagnostics.getDiagnostics(), success, "in-process Java compiler");
        }
    }

    private static String compileWithExternalJavac(
        List<Path> sourceFiles,
        Path outputDirectory,
        JavaInstallationFinder.JavaInstallation javaInstallation
    ) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(javaInstallation.javacExecutable().toAbsolutePath().toString());
        command.add("--release");
        command.add(Integer.toString(JavaInstallationFinder.REQUIRED_JAVA_FEATURE));
        command.add("-d");
        command.add(outputDirectory.toAbsolutePath().toString());
        for (Path sourceFile : sourceFiles) {
            command.add(sourceFile.toAbsolutePath().toString());
        }

        Process process = new ProcessBuilder(command)
            .directory(outputDirectory.toFile())
            .redirectErrorStream(true)
            .start();

        String output;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            output = reader.lines().reduce("", (left, right) -> left + right + System.lineSeparator());
        }

        boolean success;
        try {
            success = process.waitFor() == 0;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Compilation was interrupted.", exception);
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Compiling with auto-detected Java 26 JDK: ")
            .append(javaInstallation.home())
            .append(System.lineSeparator());
        builder.append("Target output: ")
            .append(outputDirectory)
            .append(System.lineSeparator());
        builder.append("Sources:")
            .append(System.lineSeparator());
        for (Path sourceFile : sourceFiles) {
            builder.append(" - ").append(sourceFile).append(System.lineSeparator());
        }
        builder.append(System.lineSeparator());
        if (!output.isBlank()) {
            builder.append(output).append(System.lineSeparator());
        }
        builder.append(success
            ? "Compilation finished successfully with Java 26 settings."
            : "Compilation failed while using the auto-detected Java 26 JDK.");
        return builder.toString();
    }

    private static String formatCompilationResult(
        List<Path> sourceFiles,
        Path outputDirectory,
        String compilerOutput,
        List<Diagnostic<? extends JavaFileObject>> diagnostics,
        boolean success,
        String compilerLabel
    ) {
        StringBuilder result = new StringBuilder();
        result.append("Compiling with ").append(compilerLabel).append(System.lineSeparator());
        result.append("Target output: ").append(outputDirectory).append(System.lineSeparator());
        result.append("Sources:").append(System.lineSeparator());
        for (Path sourceFile : sourceFiles) {
            result.append(" - ").append(sourceFile).append(System.lineSeparator());
        }
        result.append(System.lineSeparator());

        if (!compilerOutput.isBlank()) {
            result.append(compilerOutput).append(System.lineSeparator());
        }

        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            result.append(diagnostic.getKind())
                .append(": line ")
                .append(diagnostic.getLineNumber())
                .append(" - ")
                .append(diagnostic.getMessage(Locale.getDefault()))
                .append(System.lineSeparator());
        }

        result.append(System.lineSeparator());
        result.append(success
            ? "Compilation finished successfully with Java 26 settings."
            : "Compilation failed.");
        return result.toString();
    }

    private static void cleanGeneratedClasses(Path outputDirectory) throws IOException {
        try (var stream = Files.walk(outputDirectory)) {
            for (Path path : stream.sorted(java.util.Comparator.reverseOrder()).toList()) {
                if (!path.equals(outputDirectory)) {
                    Files.deleteIfExists(path);
                }
            }
        }
    }
}
