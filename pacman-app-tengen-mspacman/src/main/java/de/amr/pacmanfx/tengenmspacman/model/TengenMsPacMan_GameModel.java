/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapSelector;
import de.amr.pacmanfx.tengenmspacman.rules.TengenMsPacMan_GameRules;

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

    private final WorldMapSelector mapSelector;

    private int initialLifeCount;

    private final TengenMsPacMan_GameRules rules;

    public TengenMsPacMan_GameModel() {
        mapSelector = new TengenMsPacMan_MapSelector();
        rules = new TengenMsPacMan_GameRules();
        setInitialLifeCount(3);
    }

    // GameModel interface

    @Override
    public TengenMsPacMan_MapSelector mapSelector() {
        return (TengenMsPacMan_MapSelector) mapSelector;
    }

    @Override
    public void setInitialLifeCount(int initialLifeCount) {
        this.initialLifeCount = initialLifeCount;
    }

    @Override
    public int initialLifeCount() {
        return initialLifeCount;
    }

    @Override
    public TengenMsPacMan_GameRules rules() {
        return rules;
    }

}