package com.example.gamestudio;

import java.awt.Dimension;
import javax.swing.Timer;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.awt.AWTGLCanvas;
import org.lwjgl.opengl.awt.GLData;

public final class LwjglPreviewCanvas extends AWTGLCanvas {
    private ImportedModel importedModel;
    private float rotation;

    public LwjglPreviewCanvas() {
        super(createCanvasData());
        setPreferredSize(new Dimension(560, 360));
        Timer repaintTimer = new Timer(16, event -> {
            if (isDisplayable()) {
                repaint();
            }
        });
        repaintTimer.start();
    }

    private static GLData createCanvasData() {
        GLData data = new GLData();
        data.samples = 4;
        data.swapInterval = 1;
        return data;
    }

    public void setImportedModel(ImportedModel importedModel) {
        this.importedModel = importedModel;
        repaint();
    }

    @Override
    public void initGL() {
        GL.createCapabilities();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(1.5f);
    }

    @Override
    public void paintGL() {
        int width = Math.max(getWidth(), 1);
        int height = Math.max(getHeight(), 1);
        int mainWidth = Math.max((int) (width * 0.74f), 1);
        int sideWidth = Math.max(width - mainWidth, 1);
        rotation += 0.8f;

        GL11.glClearColor(0.06f, 0.08f, 0.13f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

        renderMainPreview(mainWidth, height);
        renderSideMap(mainWidth, sideWidth, height);

        swapBuffers();
    }

    private void renderMainPreview(int width, int height) {
        float aspect = (float) width / (float) Math.max(height, 1);
        float backgroundShift = importedModel == null ? 0.08f : Math.min(importedModel.meshCount() * 0.04f, 0.20f);

        GL11.glViewport(0, 0, width, height);
        GL11.glScissor(0, 0, width, height);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glClearColor(0.08f + backgroundShift, 0.10f, 0.16f + backgroundShift, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glFrustum(-aspect, aspect, -1.0, 1.0, 2.0, 45.0);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0f, -0.15f, -12.0f);
        GL11.glRotatef(18.0f, 1.0f, 0.0f, 0.0f);

        drawBackdropPanel();
        drawBaseplate();
        drawFloorGrid();

        GL11.glPushMatrix();
        GL11.glTranslatef(0.0f, 1.35f, 0.0f);
        GL11.glRotatef(rotation, 0.0f, 1.0f, 0.0f);
        GL11.glRotatef(18.0f, 1.0f, 0.0f, 0.0f);
        drawPreviewCube();
        GL11.glPopMatrix();
    }

    private void renderSideMap(int xOffset, int width, int height) {
        GL11.glViewport(xOffset, 0, width, height);
        GL11.glScissor(xOffset, 0, width, height);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glClearColor(0.11f, 0.13f, 0.19f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(-7.0, 7.0, -7.0, 7.0, -20.0, 20.0);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);

        drawMiniMapBackdrop();
        drawMiniMapPlate();
        drawMiniMapGrid();
        drawMiniMapMarker();
    }

    private void drawBackdropPanel() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(0.10f, 0.12f, 0.18f, 1.0f);
        GL11.glVertex3f(-8.0f, 6.0f, -18.0f);
        GL11.glVertex3f(8.0f, 6.0f, -18.0f);
        GL11.glColor4f(0.06f, 0.08f, 0.13f, 1.0f);
        GL11.glVertex3f(8.0f, -3.5f, -18.0f);
        GL11.glVertex3f(-8.0f, -3.5f, -18.0f);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void drawBaseplate() {
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0f, -0.02f, 0.0f);
        GL11.glColor4f(0.18f, 0.22f, 0.30f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);
        vertex(-5.5f, 0.0f, -5.5f);
        vertex(5.5f, 0.0f, -5.5f);
        vertex(5.5f, 0.0f, 5.5f);
        vertex(-5.5f, 0.0f, 5.5f);
        GL11.glEnd();

        GL11.glColor4f(0.34f, 0.40f, 0.50f, 1.0f);
        GL11.glBegin(GL11.GL_LINE_LOOP);
        vertex(-5.5f, 0.0f, -5.5f);
        vertex(5.5f, 0.0f, -5.5f);
        vertex(5.5f, 0.0f, 5.5f);
        vertex(-5.5f, 0.0f, 5.5f);
        GL11.glEnd();
        GL11.glPopMatrix();
    }

    private void drawFloorGrid() {
        GL11.glColor4f(0.30f, 0.36f, 0.44f, 0.95f);
        GL11.glBegin(GL11.GL_LINES);
        for (int i = -5; i <= 5; i++) {
            GL11.glVertex3f(i, 0.02f, -5.0f);
            GL11.glVertex3f(i, 0.02f, 5.0f);
            GL11.glVertex3f(-5.0f, 0.02f, i);
            GL11.glVertex3f(5.0f, 0.02f, i);
        }
        GL11.glEnd();
    }

    private void drawMiniMapBackdrop() {
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glColor4f(0.13f, 0.16f, 0.24f, 1.0f);
        GL11.glVertex3f(-7.0f, -7.0f, -18.0f);
        GL11.glVertex3f(7.0f, -7.0f, -18.0f);
        GL11.glColor4f(0.08f, 0.10f, 0.15f, 1.0f);
        GL11.glVertex3f(7.0f, 7.0f, -18.0f);
        GL11.glVertex3f(-7.0f, 7.0f, -18.0f);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
    }

    private void drawMiniMapPlate() {
        GL11.glColor4f(0.18f, 0.22f, 0.30f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);
        vertex(-5.6f, 0.0f, -5.6f);
        vertex(5.6f, 0.0f, -5.6f);
        vertex(5.6f, 0.0f, 5.6f);
        vertex(-5.6f, 0.0f, 5.6f);
        GL11.glEnd();
    }

    private void drawMiniMapGrid() {
        GL11.glColor4f(0.38f, 0.46f, 0.58f, 0.95f);
        GL11.glBegin(GL11.GL_LINES);
        for (int i = -5; i <= 5; i++) {
            GL11.glVertex3f(i, 0.02f, -5.0f);
            GL11.glVertex3f(i, 0.02f, 5.0f);
            GL11.glVertex3f(-5.0f, 0.02f, i);
            GL11.glVertex3f(5.0f, 0.02f, i);
        }
        GL11.glEnd();
    }

    private void drawMiniMapMarker() {
        float markerSize = importedModel == null ? 0.85f : Math.min(1.2f + importedModel.meshCount() * 0.04f, 1.9f);
        GL11.glColor4f(0.98f, 0.76f, 0.32f, 1.0f);
        GL11.glBegin(GL11.GL_QUADS);
        vertex(-markerSize, 0.05f, -markerSize);
        vertex(markerSize, 0.05f, -markerSize);
        vertex(markerSize, 0.05f, markerSize);
        vertex(-markerSize, 0.05f, markerSize);
        GL11.glEnd();

        GL11.glColor4f(0.96f, 0.96f, 0.98f, 0.9f);
        GL11.glBegin(GL11.GL_LINES);
        edge(-markerSize - 0.7f, 0.06f, 0.0f, markerSize + 0.7f, 0.06f, 0.0f);
        edge(0.0f, 0.06f, -markerSize - 0.7f, 0.0f, 0.06f, markerSize + 0.7f);
        GL11.glEnd();
    }

    private void drawPreviewCube() {
        float meshScale = importedModel == null ? 1.0f : Math.min(1.0f + importedModel.meshCount() * 0.08f, 1.8f);
        float animationTint = importedModel == null ? 0.18f : Math.min(importedModel.animationCount() * 0.1f, 0.6f);
        float faceTint = importedModel == null ? 0.32f : Math.min(importedModel.totalFaces() / 4000.0f, 0.7f);
        float size = 1.1f * meshScale;

        GL11.glBegin(GL11.GL_QUADS);

        color(0.26f + animationTint, 0.64f, 0.95f - animationTint);
        vertex(-size, size, size);
        vertex(size, size, size);
        vertex(size, -size, size);
        vertex(-size, -size, size);

        color(0.34f, 0.85f - faceTint, 0.52f + animationTint);
        vertex(-size, size, -size);
        vertex(-size, -size, -size);
        vertex(size, -size, -size);
        vertex(size, size, -size);

        color(0.98f - animationTint, 0.52f, 0.32f + faceTint);
        vertex(-size, size, -size);
        vertex(-size, size, size);
        vertex(-size, -size, size);
        vertex(-size, -size, -size);

        color(0.96f - faceTint, 0.78f, 0.28f + animationTint);
        vertex(size, size, -size);
        vertex(size, -size, -size);
        vertex(size, -size, size);
        vertex(size, size, size);

        color(0.62f, 0.41f + animationTint, 0.95f - faceTint);
        vertex(-size, size, -size);
        vertex(size, size, -size);
        vertex(size, size, size);
        vertex(-size, size, size);

        color(0.28f + faceTint, 0.35f, 0.92f - animationTint);
        vertex(-size, -size, -size);
        vertex(-size, -size, size);
        vertex(size, -size, size);
        vertex(size, -size, -size);

        GL11.glEnd();

        GL11.glColor4f(0.98f, 0.98f, 0.98f, 0.75f);
        GL11.glBegin(GL11.GL_LINES);
        edge(-size, size, size, size, size, size);
        edge(size, size, size, size, -size, size);
        edge(size, -size, size, -size, -size, size);
        edge(-size, -size, size, -size, size, size);

        edge(-size, size, -size, size, size, -size);
        edge(size, size, -size, size, -size, -size);
        edge(size, -size, -size, -size, -size, -size);
        edge(-size, -size, -size, -size, size, -size);

        edge(-size, size, size, -size, size, -size);
        edge(size, size, size, size, size, -size);
        edge(size, -size, size, size, -size, -size);
        edge(-size, -size, size, -size, -size, -size);
        GL11.glEnd();
    }

    private static void color(float red, float green, float blue) {
        GL11.glColor4f(red, green, blue, 1.0f);
    }

    private static void vertex(float x, float y, float z) {
        GL11.glVertex3f(x, y, z);
    }

    private static void edge(float x1, float y1, float z1, float x2, float y2, float z2) {
        GL11.glVertex3f(x1, y1, z1);
        GL11.glVertex3f(x2, y2, z2);
    }
}
