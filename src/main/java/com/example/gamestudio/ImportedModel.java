package com.example.gamestudio;

import java.nio.file.Path;
import java.util.List;

public record ImportedModel(
    String name,
    String format,
    Path storedPath,
    long fileSizeBytes,
    int meshCount,
    int materialCount,
    int animationCount,
    int nodeCount,
    int totalVertices,
    int totalFaces,
    List<String> meshNames
) {
    public String summary() {
        String meshes = meshNames.isEmpty() ? "(no mesh names reported)" : String.join(", ", meshNames);
        return """
            Name: %s
            Format: %s
            Stored Path: %s
            File Size: %,d bytes
            Meshes: %d
            Materials: %d
            Animations: %d
            Nodes: %d
            Vertices: %d
            Faces: %d
            Mesh Names: %s
            """.formatted(
            name,
            format,
            storedPath,
            fileSizeBytes,
            meshCount,
            materialCount,
            animationCount,
            nodeCount,
            totalVertices,
            totalFaces,
            meshes
        );
    }
}
