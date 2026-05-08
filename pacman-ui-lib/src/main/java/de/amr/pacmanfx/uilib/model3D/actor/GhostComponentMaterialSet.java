/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D.actor;

import javafx.scene.paint.PhongMaterial;

public record GhostComponentMaterialSet(
    PhongMaterial dressMaterial,
    PhongMaterial eyeballsMaterial,
    PhongMaterial pupilsMaterial) {}
