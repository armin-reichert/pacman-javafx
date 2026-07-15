/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameException;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;

import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly">Ms.Pac-Man-NES-Tengen-Disassembly</a>
 */
public class TengenMsPacMan_GameModel extends GameModel {

    public static final int DEFAULT_START_LEVEL = 1;

    public static final int DEFAULT_NUM_CONTINUES = 4;

    public static final PacBooster DEFAULT_PAC_BOOSTER = PacBooster.OFF;

    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;

    public static final MapCategory DEFAULT_MAP_CATEGORY = MapCategory.ARCADE;

    public static final String GAME_OVER_MESSAGE_TEXT = "GAME OVER";

    public static final String READY_MESSAGE_TEXT = "READY!";

    public static final Vector2i HOUSE_MIN_TILE = WorldMap.tile(10, 15);

    // --- End static

    private final TengenMsPacMan_HUDState hudState;

    private final TengenMsPacMan_GameRules rules;

    private MapCategory mapCategory;

    private Difficulty difficulty;

    private PacBooster pacBoosterMode;

    private boolean boosterActive;

    private int startLevelNumber; // 1-7

    private boolean canStartNewGame;

    private int numContinues;

    public TengenMsPacMan_GameModel() {
        hudState =  new TengenMsPacMan_HUDState();
        mapSelector = new TengenMsPacMan_MapSelector();
        levelCounter = new TengenMsPacMan_LevelCounter();
        rules = new TengenMsPacMan_GameRules();
        setDifficulty(Difficulty.NORMAL);
    }

    public boolean allOptionsHaveDefaultValue() {
        return pacBoosterMode == DEFAULT_PAC_BOOSTER
            && difficulty == DEFAULT_DIFFICULTY
            && mapCategory == DEFAULT_MAP_CATEGORY
            && startLevelNumber == DEFAULT_START_LEVEL
            && numContinues == DEFAULT_NUM_CONTINUES;
    }

    public boolean canStartNewGame() {
        return canStartNewGame;
    }

    public void setPacBoosterMode(PacBooster mode) {
        pacBoosterMode = requireNonNull(mode);
    }

    public PacBooster pacBoosterMode() {
        return pacBoosterMode;
    }

    public void activatePacBooster(Pac pac, boolean active) {
        requireNonNull(pac);
        pac.animations().select(active ? TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER : ArcadePacMan_AnimationID.PAC_MUNCHING);
        boosterActive = active;
    }

    public void setMapCategory(MapCategory mapCategory) {
        this.mapCategory = requireNonNull(mapCategory);
    }

    public MapCategory mapCategory() {
        return mapCategory;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = requireNonNull(difficulty);
        rules().actorSpeedControl().setDifficulty(difficulty);
    }

    public Difficulty difficulty() {
        return difficulty;
    }

    public void setStartLevelNumber(int number) {
        if (number < TengenMsPacMan_GameRules.FIRST_LEVEL || number > TengenMsPacMan_GameRules.LAST_LEVEL_NUMBER) {
            throw GameException.invalidLevelNumber(number);
        }
        startLevelNumber = number;
    }

    public int startLevelNumber() {
        return startLevelNumber;
    }

    public void setNumContinues(int numContinues) {
        this.numContinues = numContinues;
    }

    public int numContinues() {
        return numContinues;
    }

    public boolean canContinueOnGameOver() {
        //TODO don't change values inside this method
        if (startLevelNumber >= 10 && numContinues > 0) {
            numContinues -= 1;
            return true;
        } else {
            numContinues = 4;
            return false;
        }
    }

    public void setBoosterActive(boolean boosterActive) {
        this.boosterActive = boosterActive;
    }

    public boolean isBoosterActive() {
        return boosterActive;
    }

    public void setCanStartNewGame(boolean canStartNewGame) {
        this.canStartNewGame = canStartNewGame;
    }

    // GameModel interface

    @Override
    public void init() {
        mapSelector().loadMapPrototypes();
        lives().setInitialCount(3);
        hudState().hide();
        setPacBoosterMode(DEFAULT_PAC_BOOSTER);
        setDifficulty(DEFAULT_DIFFICULTY);
        setMapCategory(DEFAULT_MAP_CATEGORY);
        setStartLevelNumber(DEFAULT_START_LEVEL);
        setNumContinues(DEFAULT_NUM_CONTINUES);
    }

    @Override
    public TengenMsPacMan_HUDState hudState() {
        return hudState;
    }

    @Override
    public TengenMsPacMan_MapSelector mapSelector() {
        return (TengenMsPacMan_MapSelector) mapSelector;
    }

    @Override
    public TengenMsPacMan_LevelCounter levelCounter() {
        return (TengenMsPacMan_LevelCounter) levelCounter;
    }

    @Override
    public TengenMsPacMan_GameRules rules() {
        return rules;
    }
}