package com.example.gamestudio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AINode;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.Assimp;

public final class ModelImporter {
    private static final int IMPORT_FLAGS = Assimp.aiProcess_Triangulate
        | Assimp.aiProcess_JoinIdenticalVertices
        | Assimp.aiProcess_ImproveCacheLocality
        | Assimp.aiProcess_GenSmoothNormals;

    private ModelImporter() {
    }

    public static ImportedModel importModel(Path sourceFile, Path modelsDirectory) throws IOException {
        Files.createDirectories(modelsDirectory);
        Path storedFile = modelsDirectory.resolve(sourceFile.getFileName());
        Files.copy(sourceFile, storedFile, StandardCopyOption.REPLACE_EXISTING);

        AIScene scene = Assimp.aiImportFile(storedFile.toAbsolutePath().toString(), IMPORT_FLAGS);
        if (scene == null) {
            throw new IOException("Assimp failed to import model: " + Assimp.aiGetErrorString());
        }

        try {
            int totalVertices = 0;
            int totalFaces = 0;
            List<String> meshNames = new ArrayList<>();
            PointerBuffer meshes = scene.mMeshes();
            if (meshes != null) {
                for (int i = 0; i < scene.mNumMeshes(); i++) {
                    AIMesh mesh = AIMesh.create(meshes.get(i));
                    totalVertices += mesh.mNumVertices();
                    totalFaces += mesh.mNumFaces();
                    String meshName = mesh.mName().dataString();
                    if (meshName == null || meshName.isBlank()) {
                        meshName = "Mesh-" + (i + 1);
                    }
                    meshNames.add(meshName);
                }
            }

            String fileName = storedFile.getFileName().toString();
            int extensionIndex = fileName.lastIndexOf('.');
            String format = extensionIndex >= 0 ? fileName.substring(extensionIndex + 1).toUpperCase() : "UNKNOWN";

            return new ImportedModel(
                fileName,
                format,
                storedFile,
                Files.size(storedFile),
                scene.mNumMeshes(),
                scene.mNumMaterials(),
                scene.mNumAnimations(),
                countNodes(scene.mRootNode()),
                totalVertices,
                totalFaces,
                List.copyOf(meshNames)
            );
        } finally {
            Assimp.aiReleaseImport(scene);
        }
    }

    private static int countNodes(AINode node) {
        if (node == null) {
            return 0;
        }

        int total = 1;
        PointerBuffer children = node.mChildren();
        if (children != null) {
            for (int i = 0; i < node.mNumChildren(); i++) {
                total += countNodes(AINode.create(children.get(i)));
            }
        }
        return total;
    }
}
