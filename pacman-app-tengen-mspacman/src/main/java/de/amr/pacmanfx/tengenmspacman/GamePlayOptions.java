/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.model.Difficulty;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;

public class GamePlayOptions {

    public enum Key implements GameSession.SessionValueKey {GAME_PLAY_OPTIONS};

    public static final int DEFAULT_START_LEVEL = 1;
    public static final int DEFAULT_NUM_CONTINUES = 4;
    public static final BoosterMode DEFAULT_BOOSTER_MODE = BoosterMode.BOOSTER_OFF;
    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;
    public static final MapCategory DEFAULT_MAP_CATEGORY = MapCategory.ARCADE;

    private BoosterMode boosterMode;
    private Difficulty difficulty;
    private MapCategory mapCategory;

    private boolean boosterEnabled;
    private boolean canStartNewGame;
    private int numContinues;
    private int startLevelNumber;

    public GamePlayOptions() {
        boosterMode = DEFAULT_BOOSTER_MODE;
        difficulty = DEFAULT_DIFFICULTY;
        mapCategory = DEFAULT_MAP_CATEGORY;
        startLevelNumber = DEFAULT_START_LEVEL;
        numContinues = DEFAULT_NUM_CONTINUES;
        boosterEnabled = false;
        canStartNewGame = false;
    }

    public boolean areInitial() {
        return boosterMode == DEFAULT_BOOSTER_MODE
            && difficulty == DEFAULT_DIFFICULTY
            && mapCategory == DEFAULT_MAP_CATEGORY
            && startLevelNumber == DEFAULT_START_LEVEL
            && numContinues == DEFAULT_NUM_CONTINUES;
    }

    public BoosterMode boosterMode() {
        return boosterMode;
    }

    public void setBoosterMode(BoosterMode boosterMode) {
        this.boosterMode = boosterMode;
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public MapCategory mapCategory() {
        return mapCategory;
    }

    public void setMapCategory(MapCategory mapCategory) {
        this.mapCategory = mapCategory;
    }

    public boolean boosterEnabled() {
        return boosterEnabled;
    }

    public void setBoosterEnabled(boolean boosterEnabled) {
        this.boosterEnabled = boosterEnabled;
    }

    public boolean canStartNewGame() {
        return canStartNewGame;
    }

    public void setCanStartNewGame(boolean canStartNewGame) {
        this.canStartNewGame = canStartNewGame;
    }

    public int numContinues() {
        return numContinues;
    }

    public void setNumContinues(int numContinues) {
        this.numContinues = numContinues;
    }

    public int startLevelNumber() {
        return startLevelNumber;
    }

    public void setStartLevelNumber(int startLevelNumber) {
        this.startLevelNumber = startLevelNumber;
    }
}
