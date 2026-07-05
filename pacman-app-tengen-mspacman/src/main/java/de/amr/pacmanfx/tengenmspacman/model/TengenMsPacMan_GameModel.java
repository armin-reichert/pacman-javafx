/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.core.GameClock;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameException;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.level.GameLevelMessage;
import de.amr.pacmanfx.model.level.GameLevelMessageType;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;

import java.util.Set;

import static de.amr.pacmanfx.model.world.WorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man (Tengen).
 *
 * @see <a href="https://github.com/RussianManSMWC/Ms.-Pac-Man-NES-Tengen-Disassembly">Ms.Pac-Man-NES-Tengen-Disassembly</a>
 */
public class TengenMsPacMan_GameModel extends AbstractGameModel {

    public static final int DEFAULT_START_LEVEL = 1;
    public static final int DEFAULT_NUM_CONTINUES = 4;
    public static final PacBooster DEFAULT_PAC_BOOSTER = PacBooster.OFF;
    public static final Difficulty DEFAULT_DIFFICULTY = Difficulty.NORMAL;
    public static final MapCategory DEFAULT_MAP_CATEGORY = MapCategory.ARCADE;

    public static final String GAME_OVER_MESSAGE_TEXT = "GAME OVER";
    public static final String READY_MESSAGE_TEXT = "READY!";

    public static final Vector2i HOUSE_MIN_TILE = WorldMap.tile(10, 15);

    public static final byte GAME_OVER_MESSAGE_DELAY_SEC = 2;

    private static final int ARCADE_MAP_GAME_OVER_TICKS = 420;
    private static final int NON_ARCADE_MAP_GAME_OVER_TICKS = 600;

    private MapCategory mapCategory;
    private Difficulty difficulty;
    private PacBooster pacBoosterMode;

    private boolean boosterActive;

    private int startLevelNumber; // 1-7

    private boolean canStartNewGame;

    private int numContinues;

    public TengenMsPacMan_GameModel() {
        mapSelector = new TengenMsPacMan_MapSelector();
        levelCounter = new TengenMsPacMan_LevelCounter();
        hud = new TengenMsPacMan_HUDState();
        actorSpeedControl = new TengenMsPacMan_ActorSpeedControl();
        gateKeeper = new GateKeeper(); //TODO implement original logic from Tengen game
        automaticSteering = new RuleBasedPacSteering();
        demoLevelSteering = new RuleBasedPacSteering();

        setDifficulty(Difficulty.NORMAL);
    }

    public boolean allOptionsDefault() {
        return pacBoosterMode == DEFAULT_PAC_BOOSTER
            && difficulty == DEFAULT_DIFFICULTY
            && mapCategory == DEFAULT_MAP_CATEGORY
            && startLevelNumber == DEFAULT_START_LEVEL
            && numContinues == DEFAULT_NUM_CONTINUES;
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
        actorSpeedControl().setDifficulty(difficulty);
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

    public boolean isBoosterActive() {
        return boosterActive;
    }

    public void setCanStartNewGame(boolean canStartNewGame) {
        this.canStartNewGame = canStartNewGame;
    }

    public void showMessage(GameLevel level, GameLevelMessageType type) {
        final Vector2f center = level.worldMap().terrainLayer().messageCenterPosition();
        // Non-Arcade maps show a moving "Game Over" message
        final GameLevelMessage message = type == GameLevelMessageType.GAME_OVER && mapCategory != MapCategory.ARCADE
            ? new MovingGameLevelMessage(type, center, GAME_OVER_MESSAGE_DELAY_SEC * GameClock.DEFAULT_TICKS_PER_SECOND)
            : new GameLevelMessage(type, center);
        level.setMessage(message);
    }

    @Override
    public TengenMsPacMan_ActorSpeedControl actorSpeedControl() {
        return (TengenMsPacMan_ActorSpeedControl) actorSpeedControl;
    }

    @Override
    public TengenMsPacMan_HUDState hudState() {
        return (TengenMsPacMan_HUDState) hud;
    }

    @Override
    public TengenMsPacMan_LevelCounter levelCounter() {
        return (TengenMsPacMan_LevelCounter) levelCounter;
    }

    @Override
    public void init() {
        super.init();

        setPacBoosterMode(DEFAULT_PAC_BOOSTER);
        setDifficulty(DEFAULT_DIFFICULTY);
        setMapCategory(DEFAULT_MAP_CATEGORY);
        setStartLevelNumber(DEFAULT_START_LEVEL);
        numContinues = DEFAULT_NUM_CONTINUES;
    }

    @Override
    public void resetForNewGame() {
        super.resetForNewGame();
        boosterActive = false;
    }

    @Override
    public TengenMsPacMan_GameRules rules() {
        return (TengenMsPacMan_GameRules) rules;
    }

    @Override
    public boolean canStartNewGame(GameContext context) {
        return canStartNewGame;
    }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        final WorldMap worldMap = mapSelector.supplyWorldMap(levelNumber, mapCategory);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final ArcadeHouse house = new ArcadeHouse(HOUSE_MIN_TILE);
        terrain.setHouse(house);

        final GameLevel level = new GameLevel(this, levelNumber, worldMap, createHuntingTimer(rules), 3);
        level.setDemoLevel(demoLevel);

        int index = levelNumber <= 19 ? levelNumber - 1 : 18;
        float powerSeconds = TengenMsPacMan_GameRules.POWER_PELLET_TIMES[index] / 16.0f;
        level.setPacPowerSeconds(powerSeconds);
        level.setPacPowerFadingSeconds(0.5f * 3);

        // For non-Arcade game levels, spend some extra time for the moving "game over" text animation
        level.setGameOverStateTicks(mapCategory == MapCategory.ARCADE
            ? ARCADE_MAP_GAME_OVER_TICKS : NON_ARCADE_MAP_GAME_OVER_TICKS);

        setMsPacMan(level);
        setGhosts(level, house);

        //TODO not sure about this:
        level.setBonusSymbolCode(0, rules.selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, rules.selectBonusSymbolCode(level.number(), 1));

        levelCounter.setEnabled(levelNumber < 8);

        return level;
    }

    @Override
    public GameLevel buildDemoLevel() {
        final GameLevel demoLevel = createLevel(1, true);
        demoLevel.setGameOverStateTicks(120);

        final Pac pac = demoLevel.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);
        pac.setAutomaticSteering(demoLevelSteering);

        gateKeeper.setLevelNumber(1);
        demoLevelSteering.init();
        score.setLevelNumber(1);

        return demoLevel;
    }

    // Helpers

    private void setMsPacMan(GameLevel level) {
        final Pac msPacMan = TengenMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAutomaticSteering(automaticSteering);
        activatePacBooster(msPacMan, pacBoosterMode == PacBooster.ALWAYS_ON);
        level.setPac(msPacMan);
    }

    private void setGhosts(GameLevel level, House house) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        level.setGhosts(
            TengenMsPacMan_ActorFactory.createGhost(RED_GHOST_SHADOW,   house, terrain, POS_GHOST_1_RED),
            TengenMsPacMan_ActorFactory.createGhost(PINK_GHOST_SPEEDY,  house, terrain, POS_GHOST_2_PINK),
            TengenMsPacMan_ActorFactory.createGhost(CYAN_GHOST_BASHFUL, house, terrain, POS_GHOST_3_CYAN),
            TengenMsPacMan_ActorFactory.createGhost(ORANGE_GHOST_POKEY, house, terrain, POS_GHOST_4_ORANGE)
        );
    }

    private HuntingTimer createHuntingTimer(GameRules gameRules) {
        final var huntingTimer = new HuntingTimer("Tengen Ms. Pac-Man Hunting Timer", gameRules.numHuntingPhases());
        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> optGameLevel().ifPresent(level -> {
            if (newPhaseIndex > 0) {
                level.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(Ghost::requestTurnBack);
            }
        }));
        return huntingTimer;
    }
}