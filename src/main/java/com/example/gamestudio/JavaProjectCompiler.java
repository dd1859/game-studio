package com.example.gamestudio;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
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
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return "No system Java compiler is available. Please run the studio with a full JDK 21+ installation.";
        }

        Files.createDirectories(outputDirectory);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StringWriter compilerOutput = new StringWriter();

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjects(sourceFile.toFile());
            List<String> options = List.of(
                "--release", "21",
                "-d", outputDirectory.toAbsolutePath().toString()
            );

            boolean success = Boolean.TRUE.equals(compiler.getTask(compilerOutput, fileManager, diagnostics, options, null, units).call());
            StringBuilder result = new StringBuilder();
            result.append("Compiling: ").append(sourceFile).append(System.lineSeparator());
            result.append("Target output: ").append(outputDirectory).append(System.lineSeparator()).append(System.lineSeparator());

            if (!compilerOutput.toString().isBlank()) {
                result.append(compilerOutput).append(System.lineSeparator());
            }

            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                result.append(diagnostic.getKind())
                    .append(": line ")
                    .append(diagnostic.getLineNumber())
                    .append(" - ")
                    .append(diagnostic.getMessage(Locale.getDefault()))
                    .append(System.lineSeparator());
            }

            result.append(System.lineSeparator());
            result.append(success ? "Compilation finished successfully with Java 21 settings." : "Compilation failed.");
            return result.toString();
        }
    }
}
