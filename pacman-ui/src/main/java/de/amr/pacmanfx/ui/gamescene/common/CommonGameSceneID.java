/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.gamescene.common;

import de.amr.basics.Naming;

/**
 * Common scene identifiers shared across most game variants.
 * <p>
 * These cover the standard Pac‑Man flow: boot, intro, start menu, 2D/3D play scenes,
 * and the four intermission cutscenes.
 */
public enum CommonGameSceneID implements Naming {
    BOOT_SCENE,
    INTRO_SCENE,
    START_SCENE,
    PLAY_SCENE_2D,
    PLAY_SCENE_3D,
    CUTSCENE_1,
    CUTSCENE_2,
    CUTSCENE_3,
    CUTSCENE_4
}
