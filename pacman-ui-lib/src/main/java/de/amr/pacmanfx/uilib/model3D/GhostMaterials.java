/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.uilib.model3D;

import de.amr.pacmanfx.uilib.model3D.actor.GhostComponentMaterials;

public record GhostMaterials(GhostComponentMaterials normal, GhostComponentMaterials frightened, GhostComponentMaterials flashing) {}
