package com.example.gamestudio;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.JTree;

public final class StudioLauncher {
    private static final String STARTER_CLASS = """
        public class PlayerController {
            public static void main(String[] args) {
                System.out.println(\"Game Studio Java 21 script is ready.\");
            }
        }
        """;

    private final Path workspaceRoot = Path.of("studio-workspace");
    private final Path sourceDirectory = workspaceRoot.resolve("src");
    private final Path modelsDirectory = workspaceRoot.resolve("assets").resolve("models");
    private final Path compiledDirectory = workspaceRoot.resolve("build").resolve("classes");
    private final List<ImportedModel> importedModels = new ArrayList<>();
    private final ImportedModelTableModel modelTableModel = new ImportedModelTableModel(importedModels);

    private JFrame frame;
    private JTextArea javaEditor;
    private JTextArea consoleOutput;
    private JTextArea modelDetails;
    private JTextField currentFileField;
    private JLabel statusLabel;
    private JTree projectTree;
    private JTable modelTable;
    private LwjglPreviewCanvas previewCanvas;
    private Path currentJavaFile;

    public static void main(String[] args) {
        ensureJava21Plus();
        SwingUtilities.invokeLater(() -> {
            installSystemLookAndFeel();
            try {
                new StudioLauncher().show();
            } catch (IOException exception) {
                JOptionPane.showMessageDialog(
                    null,
                    exception.getMessage(),
                    "Studio initialization error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    private static void ensureJava21Plus() {
        if (Runtime.version().feature() < 21) {
            JOptionPane.showMessageDialog(
                null,
                "Game Studio requires Java 21 or newer.",
                "Unsupported Java Version",
                JOptionPane.ERROR_MESSAGE
            );
            System.exit(1);
        }
    }

    private static void installSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Use Swing default look and feel if the system look and feel is unavailable.
        }
    }

    private void show() throws IOException {
        initializeWorkspace();
        buildUi();
        refreshProjectTree();
        loadJavaFile(currentJavaFile);
        frame.setVisible(true);
        appendConsole("Studio ready. Java 21+ mode enabled.\n");
    }

    private void initializeWorkspace() throws IOException {
        Files.createDirectories(sourceDirectory);
        Files.createDirectories(modelsDirectory);
        Files.createDirectories(compiledDirectory);
        currentJavaFile = sourceDirectory.resolve("PlayerController.java");
        if (Files.notExists(currentJavaFile)) {
            Files.writeString(currentJavaFile, STARTER_CLASS);
        }
    }

    private void buildUi() {
        frame = new JFrame("Game Studio - Windows Friendly Java Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1360, 860));
        frame.setSize(1440, 900);
        frame.setLocationRelativeTo(null);
        frame.setJMenuBar(buildMenuBar());
        frame.setLayout(new BorderLayout());

        frame.add(buildToolbar(), BorderLayout.NORTH);
        frame.add(buildCenterLayout(), BorderLayout.CENTER);
        frame.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newScriptItem = new JMenuItem("New Java Class");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem importModelItem = new JMenuItem("Import 3D Model");
        JMenuItem exitItem = new JMenuItem("Exit");

        newScriptItem.addActionListener(event -> createNewJavaClass());
        saveItem.addActionListener(event -> saveCurrentJavaFile());
        importModelItem.addActionListener(event -> importModel());
        exitItem.addActionListener(event -> frame.dispose());

        fileMenu.add(newScriptItem);
        fileMenu.add(saveItem);
        fileMenu.add(importModelItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        JButton newClassButton = new JButton("New Java Class");
        JButton saveButton = new JButton("Save Java File");
        JButton compileButton = new JButton("Compile Java");
        JButton importModelButton = new JButton("Import 3D Model");

        newClassButton.addActionListener(event -> createNewJavaClass());
        saveButton.addActionListener(event -> saveCurrentJavaFile());
        compileButton.addActionListener(event -> compileCurrentJavaFile());
        importModelButton.addActionListener(event -> importModel());

        toolbar.add(newClassButton);
        toolbar.add(saveButton);
        toolbar.add(compileButton);
        toolbar.add(importModelButton);
        return toolbar;
    }

    private JSplitPane buildCenterLayout() {
        projectTree = new JTree(new DefaultMutableTreeNode("studio-workspace"));
        projectTree.setRootVisible(true);
        projectTree.setShowsRootHandles(true);
        projectTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    openSelectedJavaFileFromTree();
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(projectTree);
        treeScroll.setBorder(BorderFactory.createTitledBorder("Project Workspace"));

        JTabbedPane editorTabs = new JTabbedPane();
        editorTabs.addTab("Java Script Editor", buildJavaEditorPanel());
        editorTabs.addTab("3D Models", buildModelPanel());

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, editorTabs);
        splitPane.setDividerLocation(320);
        return splitPane;
    }

    private JPanel buildJavaEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        currentFileField = new JTextField();
        currentFileField.setEditable(false);

        javaEditor = new JTextArea();
        javaEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));
        javaEditor.setTabSize(4);

        consoleOutput = new JTextArea();
        consoleOutput.setEditable(false);
        consoleOutput.setRows(10);
        consoleOutput.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        panel.add(currentFileField, BorderLayout.NORTH);
        panel.add(new JScrollPane(javaEditor), BorderLayout.CENTER);
        panel.add(new JScrollPane(consoleOutput), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildModelPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modelTable = new JTable(modelTableModel);
        modelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modelTable.setRowSelectionAllowed(true);
        modelTable.getSelectionModel().addListSelectionListener(event -> updateModelDetails());

        modelDetails = new JTextArea();
        modelDetails.setEditable(false);
        modelDetails.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        previewCanvas = new LwjglPreviewCanvas();
        JPanel previewPanel = new JPanel(new BorderLayout());
        previewPanel.setBorder(BorderFactory.createTitledBorder("Built-in LWJGL Preview"));
        previewPanel.add(previewCanvas, BorderLayout.CENTER);

        JSplitPane detailsSplitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            new JScrollPane(modelTable),
            new JScrollPane(modelDetails)
        );
        detailsSplitPane.setDividerLocation(260);

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            detailsSplitPane,
            previewPanel
        );
        splitPane.setDividerLocation(560);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        statusLabel = new JLabel("Ready");
        statusPanel.add(statusLabel, BorderLayout.WEST);
        return statusPanel;
    }

    private void createNewJavaClass() {
        String className = JOptionPane.showInputDialog(frame, "Enter a Java class name:", "New Java Class", JOptionPane.PLAIN_MESSAGE);
        if (className == null || className.isBlank()) {
            return;
        }

        String sanitizedClassName = className.replaceAll("[^A-Za-z0-9_]", "").trim();
        if (sanitizedClassName.isBlank()) {
            setStatus("Class name was empty after sanitizing.");
            return;
        }

        Path newFile = sourceDirectory.resolve(sanitizedClassName + ".java");
        String template = """
            public class %s {
                public void update() {
                    System.out.println(\"%s running from Game Studio.\");
                }
            }
            """.formatted(sanitizedClassName, sanitizedClassName);

        try {
            Files.writeString(newFile, template);
            currentJavaFile = newFile;
            loadJavaFile(newFile);
            refreshProjectTree();
            setStatus("Created Java class: " + sanitizedClassName);
            appendConsole("Created Java class " + sanitizedClassName + System.lineSeparator());
        } catch (IOException exception) {
            showError("Unable to create Java class", exception);
        }
    }

    private void loadJavaFile(Path file) throws IOException {
        currentFileField.setText(file.toAbsolutePath().toString());
        javaEditor.setText(Files.readString(file));
        javaEditor.setCaretPosition(0);
    }

    private void saveCurrentJavaFile() {
        try {
            Files.writeString(currentJavaFile, javaEditor.getText());
            refreshProjectTree();
            setStatus("Saved " + currentJavaFile.getFileName());
            appendConsole("Saved " + currentJavaFile + System.lineSeparator());
        } catch (IOException exception) {
            showError("Unable to save Java file", exception);
        }
    }

    private void compileCurrentJavaFile() {
        saveCurrentJavaFile();
        try {
            String output = JavaProjectCompiler.compile(currentJavaFile, compiledDirectory);
            appendConsole(System.lineSeparator() + output + System.lineSeparator());
            setStatus("Compile finished for " + currentJavaFile.getFileName());
        } catch (IOException exception) {
            showError("Unable to compile Java file", exception);
        }
    }

    private void importModel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import 3D Model");
        chooser.setFileFilter(new FileNameExtensionFilter("3D Models", "obj", "fbx", "gltf", "glb", "dae"));

        if (chooser.showOpenDialog(frame) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        Path chosenFile = chooser.getSelectedFile().toPath();
        try {
            ImportedModel importedModel = ModelImporter.importModel(chosenFile, modelsDirectory);
            importedModels.add(importedModel);
            modelTableModel.fireTableDataChanged();
            int selectedRow = importedModels.size() - 1;
            modelTable.getSelectionModel().setSelectionInterval(selectedRow, selectedRow);
            refreshProjectTree();
            setStatus("Imported model: " + importedModel.name());
            appendConsole("Imported model " + importedModel.storedPath() + System.lineSeparator());
            updateModelDetails();
        } catch (IOException exception) {
            showError("Unable to import 3D model", exception);
        }
    }

    private void updateModelDetails() {
        int selectedRow = modelTable.getSelectedRow();
        if (selectedRow < 0 || selectedRow >= importedModels.size()) {
            modelDetails.setText("No model selected.");
            previewCanvas.setImportedModel(null);
            return;
        }
        ImportedModel selectedModel = importedModels.get(selectedRow);
        modelDetails.setText(selectedModel.summary());
        previewCanvas.setImportedModel(selectedModel);
        modelDetails.setCaretPosition(0);
    }

    private void openSelectedJavaFileFromTree() {
        var selectedPath = projectTree.getSelectionPath();
        if (selectedPath == null) {
            return;
        }

        Path resolvedPath = workspaceRoot;
        Object[] pathElements = selectedPath.getPath();
        for (int i = 1; i < pathElements.length; i++) {
            resolvedPath = resolvedPath.resolve(pathElements[i].toString());
        }

        if (!Files.isRegularFile(resolvedPath) || !resolvedPath.getFileName().toString().endsWith(".java")) {
            return;
        }

        try {
            currentJavaFile = resolvedPath;
            loadJavaFile(resolvedPath);
            setStatus("Opened " + resolvedPath.getFileName());
            appendConsole("Opened " + resolvedPath + System.lineSeparator());
        } catch (IOException exception) {
            showError("Unable to open Java file", exception);
        }
    }

    private void refreshProjectTree() {
        DefaultMutableTreeNode root = createTreeNode(workspaceRoot);
        projectTree.setModel(new DefaultTreeModel(root));
        for (int row = 0; row < projectTree.getRowCount(); row++) {
            projectTree.expandRow(row);
        }
    }

    private DefaultMutableTreeNode createTreeNode(Path path) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(path.getFileName() == null ? path.toString() : path.getFileName().toString());
        if (!Files.isDirectory(path)) {
            return node;
        }

        try (var children = Files.list(path).sorted(Comparator.comparing(Path::getFileName))) {
            children.forEach(child -> node.add(createTreeNode(child)));
        } catch (IOException exception) {
            node.add(new DefaultMutableTreeNode("<error loading directory>"));
        }
        return node;
    }

    private void appendConsole(String message) {
        consoleOutput.append(message);
        consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
    }

    private void setStatus(String message) {
        statusLabel.setText(message);
    }

    private void showError(String title, Exception exception) {
        appendConsole(title + ": " + exception.getMessage() + System.lineSeparator());
        setStatus(title);
        JOptionPane.showMessageDialog(frame, exception.getMessage(), title, JOptionPane.ERROR_MESSAGE);
    }

    private static final class ImportedModelTableModel extends AbstractTableModel {
        private final List<ImportedModel> importedModels;
        private final String[] columns = {"Name", "Format", "Meshes", "Vertices", "Animations", "Size (bytes)"};

        private ImportedModelTableModel(List<ImportedModel> importedModels) {
            this.importedModels = importedModels;
        }

        @Override
        public int getRowCount() {
            return importedModels.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ImportedModel model = importedModels.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> model.name();
                case 1 -> model.format();
                case 2 -> model.meshCount();
                case 3 -> model.totalVertices();
                case 4 -> model.animationCount();
                case 5 -> model.fileSizeBytes();
                default -> "";
            };
        }
    }
}
