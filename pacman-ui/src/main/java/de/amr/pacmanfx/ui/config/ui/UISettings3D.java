package de.amr.pacmanfx.ui.config.ui;

import de.amr.pacmanfx.ui.gamescene.d3.camera.PerspectiveID;
import javafx.scene.paint.Color;
import javafx.scene.shape.DrawMode;

public record UISettings3D(
    Boolean axesVisible,
    PerspectiveID cameraPerspectiveId,
    DrawMode drawMode,
    boolean view3DEnabled,
    Color mazeFloorColor,
    Color mazeLightColor,
    double mazeWallHeight,
    double mazeWallOpacity)
{
}
