/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.ghost.comp;

import javafx.scene.paint.PhongMaterial;

public record Ghost3DMaterialSet(
    PhongMaterial dress,
    PhongMaterial eyeballs,
    PhongMaterial pupils
) {}
