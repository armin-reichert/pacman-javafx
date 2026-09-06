/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.GameSession;

public enum GamePlayOptions implements GameSession.GameSessionValueKey {
    BOOSTER_MODE, BOOSTER_ON, CAN_START_GAME, DIFFICULTY, MAP_CATEGORY, START_LEVEL_NUMBER, NUM_CONTINUES
}
