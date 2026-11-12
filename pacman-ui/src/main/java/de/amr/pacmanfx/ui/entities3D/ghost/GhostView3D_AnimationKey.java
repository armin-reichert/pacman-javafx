/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.ghost;

import de.amr.pacmanfx.core.model.GhostPersonality;

public record GhostView3D_AnimationKey(GhostView3D_AnimationID animationID, GhostPersonality ghostID) {
}
