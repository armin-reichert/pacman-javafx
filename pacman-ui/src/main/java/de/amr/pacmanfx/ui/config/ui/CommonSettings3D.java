package de.amr.pacmanfx.ui.config.ui;

import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import javafx.scene.shape.DrawMode;

public record CommonSettings3D(
    boolean axesVisible,
    PerspectiveID cameraPerspectiveId,
    DrawMode drawMode,
    boolean view3DEnabled) {}
