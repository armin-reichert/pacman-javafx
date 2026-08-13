/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapManager;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly">Ms.Pac-Man-NES-Tengen-Disassembly</a>
 */
public class TengenMsPacMan_GameModel implements GameModel {

    public static final int DEFAULT_START_LEVEL = 1;

    public static final int DEFAULT_NUM_CONTINUES = 4;

    public static final BoosterMode DEFAULT_PAC_BOOSTER = BoosterMode.BOOSTER_OFF;

    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;

    public static final MapCategory DEFAULT_MAP_CATEGORY = MapCategory.ARCADE;

    public static final String GAME_OVER_MESSAGE_TEXT = "GAME OVER";

    public static final String READY_MESSAGE_TEXT = "READY!";

    public static final Vector2i HOUSE_MIN_TILE = WorldMap.tile(10, 15);

    private final WorldMapManager worldMapManager;

    public TengenMsPacMan_GameModel() {
        worldMapManager = new TengenMsPacMan_WorldMapManager();
    }

    @Override
    public TengenMsPacMan_WorldMapManager worldMapManager() {
        return (TengenMsPacMan_WorldMapManager) worldMapManager;
    }
}