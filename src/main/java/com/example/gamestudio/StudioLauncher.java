package com.example.gamestudio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
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
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public final class StudioLauncher {
    private static final Color APP_BACKGROUND = new Color(245, 247, 252);
    private static final Color PANEL_BACKGROUND = new Color(255, 255, 255);
    private static final Color HEADER_BACKGROUND = new Color(21, 31, 50);
    private static final Color HEADER_ACCENT = new Color(92, 202, 255);
    private static final Color EDITOR_BACKGROUND = new Color(20, 27, 39);
    private static final Color EDITOR_FOREGROUND = new Color(235, 241, 255);
    private static final Color MUTED_TEXT = new Color(86, 98, 120);
    private static final Color BORDER_COLOR = new Color(210, 218, 230);
    private static final Color BUTTON_BACKGROUND = new Color(236, 242, 255);

    private static final String STARTER_CLASS = """
        public class PlayerController {
            public static void main(String[] args) {
                System.out.println(\"PlayerController ready for Java 26.\");
            }
        }
        """;

    private static final String WASD_CONTROLLER_CLASS = """
        import java.awt.event.KeyEvent;
        import java.awt.event.KeyListener;

        /**
         * Required studio controls script.
         * You can edit values and behavior here, but if you delete the file the studio recreates it automatically.
         */
        public class WASDController implements KeyListener {
            public int moveSpeed = 5;
            public int boostMultiplier = 2;
            private boolean forward;
            private boolean backward;
            private boolean left;
            private boolean right;
            private boolean boost;

            @Override
            public void keyTyped(KeyEvent event) {
            }

            @Override
            public void keyPressed(KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_W -> forward = true;
                    case KeyEvent.VK_S -> backward = true;
                    case KeyEvent.VK_A -> left = true;
                    case KeyEvent.VK_D -> right = true;
                    case KeyEvent.VK_SHIFT -> boost = true;
                    default -> {
                    }
                }
            }

            @Override
            public void keyReleased(KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.VK_W -> forward = false;
                    case KeyEvent.VK_S -> backward = false;
                    case KeyEvent.VK_A -> left = false;
                    case KeyEvent.VK_D -> right = false;
                    case KeyEvent.VK_SHIFT -> boost = false;
                    default -> {
                    }
                }
            }

            public int horizontalDelta() {
                int delta = 0;
                if (left) {
                    delta -= speed();
                }
                if (right) {
                    delta += speed();
                }
                return delta;
            }

            public int verticalDelta() {
                int delta = 0;
                if (forward) {
                    delta -= speed();
                }
                if (backward) {
                    delta += speed();
                }
                return delta;
            }

            public String hudText() {
                return boost ? "Boost active" : "Standard move";
            }

            private int speed() {
                return boost ? moveSpeed * boostMultiplier : moveSpeed;
            }
        }
        """;

    private static final String EXAMPLE_GAME_CLASS = """
        import java.awt.Color;
        import java.awt.Dimension;
        import java.awt.Font;
        import java.awt.Graphics;
        import java.awt.Graphics2D;
        import java.awt.RenderingHints;
        import java.awt.event.MouseAdapter;
        import java.awt.event.MouseEvent;
        import javax.swing.JFrame;
        import javax.swing.JPanel;
        import javax.swing.SwingUtilities;
        import javax.swing.Timer;

        public class ExampleGame extends JPanel {
            private final WASDController controls = new WASDController();
            private int playerX = 120;
            private int playerY = 120;
            private int targetX = 420;
            private int targetY = 220;
            private int score;
            private boolean inputCaptured;

            public ExampleGame() {
                setPreferredSize(new Dimension(960, 560));
                setBackground(new Color(18, 24, 38));
                setFocusable(true);
                addKeyListener(controls);
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent event) {
                        captureInput();
                    }

                    @Override
                    public void mouseEntered(MouseEvent event) {
                        captureInput();
                    }
                });

                Timer timer = new Timer(16, event -> {
                    playerX = clamp(playerX + controls.horizontalDelta(), 36, getWidth() - 72);
                    playerY = clamp(playerY + controls.verticalDelta(), 92, getHeight() - 72);

                    int dx = playerX - targetX;
                    int dy = playerY - targetY;
                    if (Math.abs(dx) < 40 && Math.abs(dy) < 40) {
                        score++;
                        targetX = 90 + (score * 113) % 720;
                        targetY = 120 + (score * 89) % 320;
                    }
                    repaint();
                });
                timer.start();

                SwingUtilities.invokeLater(this::captureInput);
            }

            private void captureInput() {
                inputCaptured = requestFocusInWindow();
            }

            private int clamp(int value, int min, int max) {
                return Math.max(min, Math.min(max, value));
            }

            @Override
            protected void paintComponent(Graphics graphics) {
                super.paintComponent(graphics);
                Graphics2D g2 = (Graphics2D) graphics;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(31, 42, 64));
                for (int x = 0; x < getWidth(); x += 40) {
                    g2.drawLine(x, 90, x, getHeight());
                }
                for (int y = 90; y < getHeight(); y += 40) {
                    g2.drawLine(0, y, getWidth(), y);
                }

                g2.setColor(new Color(15, 18, 28));
                g2.fillRoundRect(18, 18, getWidth() - 36, 56, 18, 18);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
                g2.drawString("Java Studio Sample · Roblox/Unreal-inspired sandbox", 32, 42);
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                g2.drawString("Click the viewport to auto-capture input. Move with W A S D, hold Shift to boost.", 32, 64);

                g2.setColor(new Color(255, 196, 61));
                g2.fillOval(targetX, targetY, 30, 30);

                g2.setColor(new Color(92, 202, 255));
                g2.fillRoundRect(playerX, playerY, 44, 44, 14, 14);

                g2.setColor(new Color(26, 31, 45, 220));
                g2.fillRoundRect(getWidth() - 246, 20, 220, 92, 16, 16);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
                g2.drawString("Viewport HUD", getWidth() - 214, 46);
                g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 15));
                g2.drawString("Score: " + score, getWidth() - 214, 68);
                g2.drawString("Input: " + (inputCaptured ? "Captured" : "Click to capture"), getWidth() - 214, 88);
                g2.drawString("Move Mode: " + controls.hudText(), getWidth() - 214, 108);
            }

            public static void main(String[] args) {
                JFrame frame = new JFrame("Example Game");
                ExampleGame gamePanel = new ExampleGame();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setContentPane(gamePanel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                SwingUtilities.invokeLater(gamePanel::captureInput);
            }
        }
        """;

    private final Path workspaceRoot = Path.of("studio-workspace");
    private final Path sourceDirectory = workspaceRoot.resolve("src");
    private final Path modelsDirectory = workspaceRoot.resolve("assets").resolve("models");
    private final Path compiledDirectory = workspaceRoot.resolve("build").resolve("classes");
    private final Path packagedJarDirectory = workspaceRoot.resolve("build").resolve("jars");
    private final Path studioJarOutput = Path.of("build", "libs", "game-studio.jar").toAbsolutePath().normalize();
    private final List<ImportedModel> importedModels = new ArrayList<>();
    private final ImportedModelTableModel modelTableModel = new ImportedModelTableModel(importedModels);
    private final List<JavaInstallationFinder.JavaInstallation> javaInstallations = new ArrayList<>();

    private JFrame frame;
    private JTextArea javaEditor;
    private JTextArea consoleOutput;
    private JTextArea modelDetails;
    private JTextArea assetBrowserArea;
    private JTextArea sceneExplorerArea;
    private JTextArea detailsInspectorArea;
    private JTextField currentFileField;
    private JTextField studioJarField;
    private JLabel statusLabel;
    private JLabel javaRuntimeLabel;
    private JTree projectTree;
    private JTable modelTable;
    private LwjglPreviewCanvas previewCanvas;
    private Path currentJavaFile;

    public static void main(String[] args) {
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
        refreshJavaInstallations();
        announceStudioJarLocation();
        if (Runtime.version().feature() != JavaInstallationFinder.REQUIRED_JAVA_FEATURE) {
            appendConsole("Studio runtime is Java " + Runtime.version().feature()
                + ". Compile/package actions still require an auto-detected Java 26 JDK.\n");
        }
        appendConsole("Studio ready. Java 26 packaging mode enabled.\n");
    }

    private void initializeWorkspace() throws IOException {
        Files.createDirectories(sourceDirectory);
        Files.createDirectories(modelsDirectory);
        Files.createDirectories(compiledDirectory);
        Files.createDirectories(packagedJarDirectory);
        currentJavaFile = sourceDirectory.resolve("ExampleGame.java");
        ensureRequiredStudioScripts();
        writeIfMissing(sourceDirectory.resolve("PlayerController.java"), STARTER_CLASS);
    }

    private void ensureRequiredStudioScripts() throws IOException {
        writeIfMissing(sourceDirectory.resolve("WASDController.java"), WASD_CONTROLLER_CLASS);
        writeIfMissing(sourceDirectory.resolve("ExampleGame.java"), EXAMPLE_GAME_CLASS);
    }

    private void writeIfMissing(Path file, String contents) throws IOException {
        if (Files.notExists(file)) {
            Files.writeString(file, contents);
        }
    }

    private void buildUi() {
        frame = new JFrame("Game Studio - Java 26 Game Packager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1480, 920));
        frame.setSize(1640, 980);
        frame.setLocationRelativeTo(null);
        frame.setJMenuBar(buildMenuBar());
        frame.setLayout(new BorderLayout(0, 0));
        frame.getContentPane().setBackground(APP_BACKGROUND);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.setOpaque(false);
        northPanel.add(buildHeroPanel(), BorderLayout.NORTH);
        northPanel.add(buildToolbar(), BorderLayout.SOUTH);

        frame.add(northPanel, BorderLayout.NORTH);
        frame.add(buildCenterLayout(), BorderLayout.CENTER);
        frame.add(buildStatusBar(), BorderLayout.SOUTH);
    }

    private JMenuBar buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem newScriptItem = new JMenuItem("New Java Class");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem packageJarItem = new JMenuItem("Package Game Jar");
        JMenuItem importModelItem = new JMenuItem("Import 3D Model");
        JMenuItem revealJarItem = new JMenuItem("Show Studio Jar Path");
        JMenuItem restoreScriptsItem = new JMenuItem("Restore Required WASD Scripts");
        JMenuItem exitItem = new JMenuItem("Exit");

        newScriptItem.addActionListener(event -> createNewJavaClass());
        saveItem.addActionListener(event -> saveCurrentJavaFile());
        packageJarItem.addActionListener(event -> packageCurrentGameJar());
        importModelItem.addActionListener(event -> importModel());
        revealJarItem.addActionListener(event -> announceStudioJarLocation());
        restoreScriptsItem.addActionListener(event -> restoreRequiredStudioScripts());
        exitItem.addActionListener(event -> frame.dispose());

        fileMenu.add(newScriptItem);
        fileMenu.add(saveItem);
        fileMenu.add(packageJarItem);
        fileMenu.add(importModelItem);
        fileMenu.add(revealJarItem);
        fileMenu.add(restoreScriptsItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);
        return menuBar;
    }

    private JPanel buildHeroPanel() {
        JPanel heroPanel = new JPanel(new BorderLayout(18, 18));
        heroPanel.setBackground(HEADER_BACKGROUND);
        heroPanel.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel titleLabel = new JLabel("Game Studio · Java 26 Build Deck");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 26));

        JLabel subtitleLabel = new JLabel("Roblox Studio layout + Unreal-style inspector ideas, packaged into a Java desktop workflow.");
        subtitleLabel.setForeground(new Color(214, 223, 242));
        subtitleLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));

        JPanel leftPanel = new JPanel(new GridLayout(2, 1, 0, 6));
        leftPanel.setOpaque(false);
        leftPanel.add(titleLabel);
        leftPanel.add(subtitleLabel);

        JPanel rightPanel = new JPanel(new BorderLayout(8, 8));
        rightPanel.setOpaque(false);

        JLabel jarLabel = new JLabel("Main studio jar output");
        jarLabel.setForeground(HEADER_ACCENT);
        jarLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));

        studioJarField = new JTextField(studioJarOutput.toString());
        studioJarField.setEditable(false);
        studioJarField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        studioJarField.setBackground(new Color(31, 44, 70));
        studioJarField.setForeground(Color.WHITE);
        studioJarField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 92, 129)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        rightPanel.add(jarLabel, BorderLayout.NORTH);
        rightPanel.add(studioJarField, BorderLayout.CENTER);
        rightPanel.setPreferredSize(new Dimension(470, 58));

        heroPanel.add(leftPanel, BorderLayout.CENTER);
        heroPanel.add(rightPanel, BorderLayout.EAST);
        return heroPanel;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setBackground(PANEL_BACKGROUND);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JButton refreshJavaButton = createActionButton("Find Java 26");
        JButton newClassButton = createActionButton("New Java Class");
        JButton saveButton = createActionButton("Save Java File");
        JButton compileButton = createActionButton("Compile Java 26");
        JButton packageButton = createActionButton("Save Game Jar");
        JButton importModelButton = createActionButton("Import 3D Model");
        JButton restoreControlsButton = createActionButton("Restore WASD Script");
        JButton showJarButton = createActionButton("Show Studio Jar");

        refreshJavaButton.addActionListener(event -> refreshJavaInstallations());
        newClassButton.addActionListener(event -> createNewJavaClass());
        saveButton.addActionListener(event -> saveCurrentJavaFile());
        compileButton.addActionListener(event -> compileCurrentJavaFile());
        packageButton.addActionListener(event -> packageCurrentGameJar());
        importModelButton.addActionListener(event -> importModel());
        restoreControlsButton.addActionListener(event -> restoreRequiredStudioScripts());
        showJarButton.addActionListener(event -> announceStudioJarLocation());

        javaRuntimeLabel = new JLabel("Runtime: Java " + Runtime.version().feature());
        javaRuntimeLabel.setForeground(MUTED_TEXT);
        javaRuntimeLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        javaRuntimeLabel.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 0));

        toolbar.add(refreshJavaButton);
        toolbar.add(newClassButton);
        toolbar.add(saveButton);
        toolbar.add(compileButton);
        toolbar.add(packageButton);
        toolbar.add(importModelButton);
        toolbar.add(restoreControlsButton);
        toolbar.add(showJarButton);
        toolbar.add(javaRuntimeLabel);
        return toolbar;
    }

    private JButton createActionButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBackground(BUTTON_BACKGROUND);
        button.setForeground(new Color(31, 47, 78));
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 202, 223)),
            BorderFactory.createEmptyBorder(7, 12, 7, 12)
        ));
        return button;
    }

    private JSplitPane buildCenterLayout() {
        JSplitPane leftSidebar = buildLeftSidebar();
        JTabbedPane editorTabs = new JTabbedPane();
        editorTabs.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        editorTabs.addTab("Java Script Editor", buildJavaEditorPanel());
        editorTabs.addTab("3D Models", buildModelPanel());

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftSidebar, editorTabs);
        centerSplit.setDividerLocation(360);
        centerSplit.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 6));

        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, centerSplit, buildRightInspectorPanel());
        mainSplit.setDividerLocation(1200);
        mainSplit.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 12));
        return mainSplit;
    }

    private JSplitPane buildLeftSidebar() {
        projectTree = new JTree(new DefaultMutableTreeNode("studio-workspace"));
        projectTree.setRootVisible(true);
        projectTree.setShowsRootHandles(true);
        projectTree.setBackground(PANEL_BACKGROUND);
        projectTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                if (event.getClickCount() == 2) {
                    openSelectedJavaFileFromTree();
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(projectTree);
        treeScroll.setBorder(createSectionBorder("Explorer · Workspace / Scene"));
        treeScroll.getViewport().setBackground(PANEL_BACKGROUND);

        assetBrowserArea = createInspectorArea();
        assetBrowserArea.setEditable(false);
        JScrollPane assetScroll = new JScrollPane(assetBrowserArea);
        assetScroll.setBorder(createSectionBorder("Asset Browser · Models / Jars / Scripts"));
        assetScroll.getViewport().setBackground(EDITOR_BACKGROUND);

        JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, treeScroll, assetScroll);
        leftSplit.setDividerLocation(420);
        leftSplit.setBorder(BorderFactory.createEmptyBorder());
        return leftSplit;
    }

    private JSplitPane buildRightInspectorPanel() {
        sceneExplorerArea = createInspectorArea();
        detailsInspectorArea = createInspectorArea();

        JScrollPane sceneScroll = new JScrollPane(sceneExplorerArea);
        sceneScroll.setBorder(createSectionBorder("Scene Outliner · Roblox / Unreal Style"));
        sceneScroll.getViewport().setBackground(EDITOR_BACKGROUND);

        JScrollPane detailsScroll = new JScrollPane(detailsInspectorArea);
        detailsScroll.setBorder(createSectionBorder("Details Inspector"));
        detailsScroll.getViewport().setBackground(EDITOR_BACKGROUND);

        JSplitPane inspectorSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sceneScroll, detailsScroll);
        inspectorSplit.setDividerLocation(360);
        inspectorSplit.setBorder(BorderFactory.createEmptyBorder(12, 6, 12, 0));
        return inspectorSplit;
    }

    private JPanel buildJavaEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        currentFileField = new JTextField();
        currentFileField.setEditable(false);
        currentFileField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        currentFileField.setBorder(createSectionBorder("Current Java File"));
        currentFileField.setBackground(PANEL_BACKGROUND);

        javaEditor = createEditorArea(15);
        consoleOutput = createEditorArea(11);
        consoleOutput.setEditable(false);
        consoleOutput.setRows(12);

        JScrollPane editorScroll = new JScrollPane(javaEditor);
        editorScroll.setBorder(createSectionBorder("Code Editor"));
        editorScroll.getViewport().setBackground(EDITOR_BACKGROUND);

        JScrollPane consoleScroll = new JScrollPane(consoleOutput);
        consoleScroll.setBorder(createSectionBorder("Build / Studio Output"));
        consoleScroll.getViewport().setBackground(EDITOR_BACKGROUND);

        panel.add(currentFileField, BorderLayout.NORTH);
        panel.add(editorScroll, BorderLayout.CENTER);
        panel.add(consoleScroll, BorderLayout.SOUTH);
        return panel;
    }

    private JTextArea createEditorArea(int fontSize) {
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, fontSize));
        textArea.setTabSize(4);
        textArea.setBackground(EDITOR_BACKGROUND);
        textArea.setForeground(EDITOR_FOREGROUND);
        textArea.setCaretColor(Color.WHITE);
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        return textArea;
    }

    private JTextArea createInspectorArea() {
        JTextArea textArea = createEditorArea(13);
        textArea.setEditable(false);
        textArea.setRows(14);
        return textArea;
    }

    private JPanel buildModelPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        modelTable = new JTable(modelTableModel);
        modelTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modelTable.setRowSelectionAllowed(true);
        modelTable.setFillsViewportHeight(true);
        modelTable.setBackground(PANEL_BACKGROUND);
        modelTable.getSelectionModel().addListSelectionListener(event -> updateModelDetails());

        modelDetails = createInspectorArea();

        previewCanvas = new LwjglPreviewCanvas();
        JPanel previewPanel = createCardPanel(new BorderLayout());
        previewPanel.setBorder(createSectionBorder("3D Stage Preview · Baseplate + Side Map"));
        previewPanel.add(previewCanvas, BorderLayout.CENTER);

        JLabel previewHint = new JLabel("Viewport reads like a mini world editor: main stage, grounded baseplate, and side tactical map.");
        previewHint.setForeground(MUTED_TEXT);
        previewHint.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
        previewPanel.add(previewHint, BorderLayout.SOUTH);

        JScrollPane tableScroll = new JScrollPane(modelTable);
        tableScroll.setBorder(createSectionBorder("Imported Models"));
        tableScroll.getViewport().setBackground(PANEL_BACKGROUND);

        JScrollPane detailsScroll = new JScrollPane(modelDetails);
        detailsScroll.setBorder(createSectionBorder("Model Details"));
        detailsScroll.getViewport().setBackground(EDITOR_BACKGROUND);

        JSplitPane detailsSplitPane = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            tableScroll,
            detailsScroll
        );
        detailsSplitPane.setDividerLocation(250);
        detailsSplitPane.setBorder(BorderFactory.createEmptyBorder());

        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            detailsSplitPane,
            previewPanel
        );
        splitPane.setDividerLocation(520);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createCardPanel(BorderLayout layout) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(PANEL_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return panel;
    }

    private javax.swing.border.Border createSectionBorder(String title) {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createTitledBorder(
                BorderFactory.createEmptyBorder(6, 6, 6, 6),
                title,
                0,
                0,
                new Font(Font.SANS_SERIF, Font.BOLD, 13),
                new Color(36, 52, 82)
            )
        );
    }

    private JPanel buildStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout(12, 0));
        statusPanel.setBackground(PANEL_BACKGROUND);
        statusPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        statusLabel = new JLabel("Ready");
        statusLabel.setForeground(new Color(34, 50, 79));

        JLabel jarHintLabel = new JLabel("Studio jar path: " + studioJarOutput, SwingConstants.RIGHT);
        jarHintLabel.setForeground(MUTED_TEXT);
        jarHintLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(jarHintLabel, BorderLayout.EAST);
        return statusPanel;
    }

    private void refreshJavaInstallations() {
        javaInstallations.clear();
        javaInstallations.addAll(JavaInstallationFinder.findInstallations());
        appendConsole(JavaInstallationFinder.describeInstallations(javaInstallations) + System.lineSeparator());
        JavaInstallationFinder.JavaInstallation java26Jdk = javaInstallations.stream()
            .filter(JavaInstallationFinder.JavaInstallation::isRequiredVersion)
            .filter(JavaInstallationFinder.JavaInstallation::hasCompiler)
            .findFirst()
            .orElse(null);

        if (java26Jdk != null) {
            setStatus("Java 26 JDK found: " + java26Jdk.home());
        } else {
            setStatus("Java 26 JDK not found. Install Java 26 or point JAVA_HOME to it.");
        }
        refreshStudioPanels();
    }

    private void announceStudioJarLocation() {
        String message = "Main studio jar output is under: " + studioJarOutput;
        if (studioJarField != null) {
            studioJarField.setText(studioJarOutput.toString());
        }
        appendConsole(message + System.lineSeparator());
        setStatus(message);
        refreshStudioPanels();
    }

    private void restoreRequiredStudioScripts() {
        try {
            ensureRequiredStudioScripts();
            refreshProjectTree();
            setStatus("Required studio scripts restored. WASDController.java is editable but auto-restored if removed.");
            appendConsole("Restored required scripts: ExampleGame.java and WASDController.java" + System.lineSeparator());
        } catch (IOException exception) {
            showError("Unable to restore required studio scripts", exception);
        }
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
                public static void main(String[] args) {
                    System.out.println(\"%s running from Game Studio with Java 26.\");
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
        ensureRequiredStudioScripts();
        currentFileField.setText(file.toAbsolutePath().toString());
        javaEditor.setText(Files.readString(file));
        javaEditor.setCaretPosition(0);
        refreshStudioPanels();
    }

    private void saveCurrentJavaFile() {
        try {
            ensureRequiredStudioScripts();
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
            refreshStudioPanels();
        } catch (IOException exception) {
            showError("Unable to compile Java file", exception);
        }
    }

    private void packageCurrentGameJar() {
        saveCurrentJavaFile();
        String mainClassName = stripJavaExtension(currentJavaFile.getFileName().toString());
        try {
            String output = GamePackager.packageGame(sourceDirectory, compiledDirectory, packagedJarDirectory, mainClassName);
            appendConsole(System.lineSeparator() + output + System.lineSeparator());
            refreshProjectTree();
            setStatus("Saved game jar: " + packagedJarDirectory.resolve(mainClassName + ".jar").getFileName());
            refreshStudioPanels();
        } catch (IOException exception) {
            showError("Unable to package game jar", exception);
        }
    }

    private String stripJavaExtension(String fileName) {
        int extensionIndex = fileName.lastIndexOf('.');
        return extensionIndex >= 0 ? fileName.substring(0, extensionIndex) : fileName;
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
            modelDetails.setText("No model selected.\n\nImport a 3D model to inspect its stats and view it on the preview baseplate + side map.");
            previewCanvas.setImportedModel(null);
            refreshStudioPanels();
            return;
        }
        ImportedModel selectedModel = importedModels.get(selectedRow);
        modelDetails.setText(selectedModel.summary());
        previewCanvas.setImportedModel(selectedModel);
        modelDetails.setCaretPosition(0);
        refreshStudioPanels();
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
        try {
            ensureRequiredStudioScripts();
        } catch (IOException exception) {
            appendConsole("Unable to ensure required scripts: " + exception.getMessage() + System.lineSeparator());
        }
        DefaultMutableTreeNode root = createTreeNode(workspaceRoot);
        projectTree.setModel(new DefaultTreeModel(root));
        for (int row = 0; row < projectTree.getRowCount(); row++) {
            projectTree.expandRow(row);
        }
        refreshStudioPanels();
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

    private void refreshStudioPanels() {
        if (assetBrowserArea != null) {
            assetBrowserArea.setText(buildAssetBrowserSummary());
            assetBrowserArea.setCaretPosition(0);
        }
        if (sceneExplorerArea != null) {
            sceneExplorerArea.setText(buildSceneExplorerSummary());
            sceneExplorerArea.setCaretPosition(0);
        }
        if (detailsInspectorArea != null) {
            detailsInspectorArea.setText(buildDetailsInspectorSummary());
            detailsInspectorArea.setCaretPosition(0);
        }
    }

    private String buildAssetBrowserSummary() {
        StringBuilder builder = new StringBuilder();
        builder.append("Required scripts\n");
        builder.append(" - WASDController.java (editable, auto-restored if removed)\n");
        builder.append(" - ExampleGame.java\n\n");
        builder.append("Workspace assets\n");
        appendRelativeFiles(builder, sourceDirectory, "src");
        appendRelativeFiles(builder, modelsDirectory, "assets/models");
        appendRelativeFiles(builder, packagedJarDirectory, "build/jars");
        return builder.toString();
    }

    private void appendRelativeFiles(StringBuilder builder, Path directory, String label) {
        builder.append(label).append(':').append('\n');
        if (!Files.exists(directory)) {
            builder.append(" - <missing>\n\n");
            return;
        }
        try (var stream = Files.walk(directory, 1)) {
            List<Path> files = stream
                .filter(path -> !path.equals(directory))
                .sorted(Comparator.comparing(Path::getFileName))
                .toList();
            if (files.isEmpty()) {
                builder.append(" - <empty>\n\n");
                return;
            }
            for (Path file : files) {
                builder.append(" - ").append(directory.relativize(file)).append('\n');
            }
            builder.append('\n');
        } catch (IOException exception) {
            builder.append(" - <error: ").append(exception.getMessage()).append(">\n\n");
        }
    }

    private String buildSceneExplorerSummary() {
        String currentFileName = currentJavaFile == null ? "None" : currentJavaFile.getFileName().toString();
        String selectedModel = importedModels.isEmpty() ? "None" : importedModels.get(Math.max(0, modelTable == null ? 0 : modelTable.getSelectedRow() >= 0 ? modelTable.getSelectedRow() : 0)).name();
        return """
            World
             ├─ WorkspaceRoot: %s
             ├─ CurrentScript: %s
             ├─ RequiredControls: WASDController.java
             ├─ SampleMap: ExampleGame.java
             ├─ ImportedModelCount: %d
             ├─ SelectedModel: %s
             ├─ PreviewViewport: Baseplate + SideMap
             └─ OutputJar: %s
            """.formatted(
            workspaceRoot.toAbsolutePath(),
            currentFileName,
            importedModels.size(),
            selectedModel,
            studioJarOutput.getFileName()
        );
    }

    private String buildDetailsInspectorSummary() {
        String java26Location = javaInstallations.stream()
            .filter(JavaInstallationFinder.JavaInstallation::isRequiredVersion)
            .filter(JavaInstallationFinder.JavaInstallation::hasCompiler)
            .map(javaInstallation -> javaInstallation.home().toString())
            .findFirst()
            .orElse("Not detected");

        return """
            Details Inspector
            -----------------
            Current file: %s
            Source root: %s
            Required control script: %s
            Editable protected script: yes
            Auto-restore on delete: yes
            Example viewport auto-capture: enabled
            Movement scheme: W / A / S / D + Shift boost
            Imported models: %d
            Game jar output: %s
            Main studio jar output: %s
            Runtime JVM: Java %d
            Auto-detected Java 26 JDK: %s
            """.formatted(
            currentJavaFile == null ? "None" : currentJavaFile.toAbsolutePath(),
            sourceDirectory.toAbsolutePath(),
            sourceDirectory.resolve("WASDController.java").toAbsolutePath(),
            importedModels.size(),
            packagedJarDirectory.toAbsolutePath(),
            studioJarOutput,
            Runtime.version().feature(),
            java26Location
        );
    }

    private void appendConsole(String message) {
        if (consoleOutput == null) {
            return;
        }
        consoleOutput.append(message);
        consoleOutput.setCaretPosition(consoleOutput.getDocument().getLength());
    }

    private void setStatus(String message) {
        if (statusLabel != null) {
            statusLabel.setText(message);
        }
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
