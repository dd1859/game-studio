package com.example.gamestudio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class JavaInstallationFinder {
    public static final int REQUIRED_JAVA_FEATURE = 26;
    private static final Pattern FEATURE_PATTERN = Pattern.compile("(\\d+)");

    private JavaInstallationFinder() {
    }

    public static List<JavaInstallation> findInstallations() {
        Set<Path> candidates = new LinkedHashSet<>();
        addFromEnvironment(candidates, "JAVA_HOME");
        addFromEnvironment(candidates, "JDK_HOME");

        Path userHome = Path.of(System.getProperty("user.home", "."));
        addDirectoryIfPresent(candidates, userHome.resolve(".jdks"));
        addDirectoryIfPresent(candidates, userHome.resolve(".sdkman").resolve("candidates").resolve("java"));
        addDirectoryIfPresent(candidates, userHome.resolve(".asdf").resolve("installs").resolve("java"));
        addDirectoryIfPresent(candidates, userHome.resolve(".local").resolve("share").resolve("mise").resolve("installs").resolve("java"));
        addDirectoryIfPresent(candidates, Path.of("/usr/lib/jvm"));
        addDirectoryIfPresent(candidates, Path.of("/Library/Java/JavaVirtualMachines"));
        addDirectoryIfPresent(candidates, Path.of("C:/Program Files/Java"));
        addDirectoryIfPresent(candidates, Path.of("C:/Program Files/Eclipse Adoptium"));
        addDirectoryIfPresent(candidates, Path.of("C:/Program Files/Microsoft"));

        return candidates.stream()
            .map(JavaInstallationFinder::inspectInstallation)
            .filter(javaInstallation -> javaInstallation != null)
            .sorted(Comparator
                .comparing(JavaInstallation::isRequiredVersion)
                .reversed()
                .thenComparing(JavaInstallation::featureVersion)
                .reversed()
                .thenComparing(javaInstallation -> javaInstallation.home().toString().toLowerCase(Locale.ROOT)))
            .toList();
    }

    public static JavaInstallation findRequiredJdk() {
        return findInstallations().stream()
            .filter(JavaInstallation::isRequiredVersion)
            .filter(JavaInstallation::hasCompiler)
            .findFirst()
            .orElse(null);
    }

    public static String describeInstallations(List<JavaInstallation> installations) {
        if (installations.isEmpty()) {
            return "No Java installations were discovered automatically.";
        }

        StringBuilder builder = new StringBuilder("Detected Java installations:\n");
        for (JavaInstallation installation : installations) {
            builder.append(" - ")
                .append(installation.displayName())
                .append(" [feature ")
                .append(installation.featureVersion())
                .append("]")
                .append(installation.hasCompiler() ? " [JDK]" : " [runtime only]")
                .append(installation.isRequiredVersion() ? " [Java 26]" : "")
                .append(" -> ")
                .append(installation.home())
                .append(System.lineSeparator());
        }
        return builder.toString();
    }

    private static void addFromEnvironment(Set<Path> candidates, String variableName) {
        String value = System.getenv(variableName);
        if (value != null && !value.isBlank()) {
            candidates.add(normalizeHome(Path.of(value)));
        }
    }

    private static void addDirectoryIfPresent(Set<Path> candidates, Path directory) {
        if (!Files.isDirectory(directory)) {
            return;
        }

        candidates.add(normalizeHome(directory));
        try (Stream<Path> children = Files.list(directory)) {
            children.forEach(path -> candidates.add(normalizeHome(path)));
        } catch (IOException ignored) {
            // Ignore discovery errors and continue with other locations.
        }
    }

    private static JavaInstallation inspectInstallation(Path candidate) {
        Path home = normalizeHome(candidate);
        Path javaExecutable = resolveExecutable(home, "java");
        if (javaExecutable == null) {
            return null;
        }

        int featureVersion = parseFeatureVersion(home);
        Path javacExecutable = resolveExecutable(home, "javac");
        String name = home.getFileName() == null ? home.toString() : home.getFileName().toString();
        return new JavaInstallation(
            name,
            home,
            javaExecutable,
            javacExecutable,
            featureVersion,
            featureVersion == REQUIRED_JAVA_FEATURE,
            javacExecutable != null
        );
    }

    private static Path normalizeHome(Path path) {
        Path candidate = path.toAbsolutePath().normalize();
        Path macHome = candidate.resolve("Contents").resolve("Home");
        if (Files.isDirectory(macHome)) {
            return macHome;
        }
        return candidate;
    }

    private static Path resolveExecutable(Path home, String baseName) {
        List<Path> candidates = new ArrayList<>();
        candidates.add(home.resolve("bin").resolve(baseName));
        candidates.add(home.resolve("bin").resolve(baseName + ".exe"));
        if (Files.isRegularFile(home) && home.getFileName() != null) {
            String fileName = home.getFileName().toString().toLowerCase(Locale.ROOT);
            if (fileName.equals(baseName) || fileName.equals(baseName + ".exe")) {
                candidates.add(home);
            }
        }
        return candidates.stream().filter(Files::isRegularFile).findFirst().orElse(null);
    }

    private static int parseFeatureVersion(Path home) {
        Matcher matcher = FEATURE_PATTERN.matcher(home.toString());
        while (matcher.find()) {
            String token = matcher.group(1);
            try {
                int version = Integer.parseInt(token);
                if (version >= 8) {
                    return version;
                }
            } catch (NumberFormatException ignored) {
                // Ignore malformed version chunks in installation names.
            }
        }
        return -1;
    }

    public record JavaInstallation(
        String displayName,
        Path home,
        Path javaExecutable,
        Path javacExecutable,
        int featureVersion,
        boolean isRequiredVersion,
        boolean hasCompiler
    ) {
    }
}
