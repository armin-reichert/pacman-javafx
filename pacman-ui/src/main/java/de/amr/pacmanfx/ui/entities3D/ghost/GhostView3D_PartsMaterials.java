/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.ghost;

import javafx.scene.paint.PhongMaterial;

public record GhostView3D_PartsMaterials(
    PhongMaterial dressMaterial,
    PhongMaterial eyeballsMaterial,
    PhongMaterial pupilsMaterial)
{}
