/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.settings.ui;

import de.amr.pacmanfx.ui.gamescene.view3d.camera.PerspectiveID;
import javafx.scene.shape.DrawMode;

public record Game3DSettings(
    boolean axesVisible,
    PerspectiveID cameraPerspectiveId,
    DrawMode drawMode,
    boolean view3DEnabled) {}
