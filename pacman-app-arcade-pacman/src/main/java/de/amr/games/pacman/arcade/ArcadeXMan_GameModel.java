/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.event.GameEventType;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.Waypoint;
import de.amr.games.pacman.lib.tilemap.LayerID;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.steering.Steering;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.tilemap.WorldMap.formatTile;

/**
 * Common data and functionality of Pac-Man and Ms. Pac-Man Arcade games.
 */
public abstract class ArcadeXMan_GameModel extends GameModel {

    public static Waypoint wp(int x, int y) { return new Waypoint(x, y); }

    // Level settings as specified in the "Pac-Man dossier"
    public static final byte[][] LEVEL_DATA = {
        /* 1*/ { 80, 75, 40,  20,  80, 10,  85,  90, 50, 6, 5},
        /* 2*/ { 90, 85, 45,  30,  90, 15,  95,  95, 55, 5, 5},
        /* 3*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 4, 5},
        /* 4*/ { 90, 85, 45,  40,  90, 20,  95,  95, 55, 3, 5},
        /* 5*/ {100, 95, 50,  40, 100, 20, 105, 100, 60, 2, 5},
        /* 6*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 5, 5},
        /* 7*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
        /* 8*/ {100, 95, 50,  50, 100, 25, 105, 100, 60, 2, 5},
        /* 9*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 1, 3},
        /*10*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 5, 5},
        /*11*/ {100, 95, 50,  60, 100, 30, 105, 100, 60, 2, 5},
        /*12*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
        /*13*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 1, 3},
        /*14*/ {100, 95, 50,  80, 100, 40, 105, 100, 60, 3, 5},
        /*15*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
        /*16*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
        /*17*/ {100, 95, 50, 100, 100, 50, 105,   0,  0, 0, 0},
        /*18*/ {100, 95, 50, 100, 100, 50, 105, 100, 60, 1, 3},
        /*19*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
        /*20*/ {100, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
        /*21*/ { 90, 95, 50, 120, 100, 60, 105,   0,  0, 0, 0},
    };

    protected static final byte PELLET_VALUE = 10;
    protected static final byte ENERGIZER_VALUE = 50;
    protected static final int POINTS_ALL_GHOSTS_EATEN_IN_LEVEL = 12_000;
    protected static final int EXTRA_LIFE_SCORE = 10_000;
    protected static final byte[] KILLED_GHOST_VALUE_MULTIPLIER = {2, 4, 8, 16}; // points = value * 100

    protected MapSelector mapSelector;
    protected LevelCounter levelCounter;
    protected HuntingTimer huntingTimer;
    protected GateKeeper gateKeeper;
    protected Steering autopilot;
    protected Steering demoLevelSteering;

    protected byte cruiseElroy;

    protected LevelData levelData(int levelNumber) {
        return new LevelData(LEVEL_DATA[Math.min(levelNumber - 1, LEVEL_DATA.length - 1)]);
    }

    protected void setCruiseElroyEnabled(boolean enabled) {
        if (enabled && cruiseElroy < 0 || !enabled && cruiseElroy > 0) {
            cruiseElroy = (byte) -cruiseElroy;
        }
    }

    protected void addArcadeHouse(WorldMap worldMap) {
        if (!worldMap.hasProperty(LayerID.TERRAIN, WorldMapProperty.POS_HOUSE_MIN_TILE)) {
            Logger.warn("No house min tile found in map!");
            worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_HOUSE_MIN_TILE, formatTile(Vector2i.of(10, 15)));
        }
        if (!worldMap.hasProperty(LayerID.TERRAIN, WorldMapProperty.POS_HOUSE_MAX_TILE)) {
            Logger.warn("No house max tile found in map!");
            worldMap.setProperty(LayerID.TERRAIN, WorldMapProperty.POS_HOUSE_MAX_TILE, formatTile(Vector2i.of(17, 19)));
        }
        Vector2i minTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MIN_TILE, null);
        Vector2i maxTile = worldMap.getTerrainTileProperty(WorldMapProperty.POS_HOUSE_MAX_TILE, null);
        level.createArcadeHouse(minTile.x(), minTile.y(), maxTile.x(), maxTile.y());
    }

    @Override
    protected Optional<GateKeeper> gateKeeper() { return Optional.of(gateKeeper); }

    @Override
    public HuntingTimer huntingTimer() {
        return huntingTimer;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends LevelCounter> T levelCounter() {
        return (T) levelCounter;
    }

    @Override
    public MapSelector mapSelector() {
        return mapSelector;
    }

    @Override
    public void init() {
        initialLivesProperty().set(3);
        mapSelector.loadAllMaps(this);
    }

    @Override
    public void resetEverything() {
        resetForStartingNewGame();
    }

    @Override
    public void resetForStartingNewGame() {
        playingProperty().set(false);
        livesProperty().set(initialLivesProperty().get());
        level = null;
        cruiseElroy = 0;
        levelCounter().reset();
        scoreManager().loadHighScore();
        scoreManager.resetScore();
        gateKeeper.reset();
        huntingTimer.reset();
    }

    @Override
    public boolean canStartNewGame() { return !THE_COIN_MECHANISM.isEmpty(); }

    @Override
    public boolean continueOnGameOver() { return false; }

    @Override
    public boolean isOver() {
        return livesProperty().get() == 0;
    }

    @Override
    protected void setActorBaseSpeed(int levelNumber) {
        level.pac().setBaseSpeed(1.25f);
        level.ghosts().forEach(ghost -> ghost.setBaseSpeed(1.25f));
        Logger.debug("{} base speed: {0.00} px/tick", level.pac().name(), level.pac().baseSpeed());
        level.ghosts().forEach(ghost -> Logger.debug("{} base speed: {0.00} px/tick", ghost.name(), ghost.baseSpeed()));
    }

    @Override
    public float pacNormalSpeed() {
        if (level == null) {
            return 0;
        }
        byte percentage = levelData(level.number()).pacSpeedPercentage();
        return percentage > 0 ? percentage * 0.01f * level.pac().baseSpeed() : level.pac().baseSpeed();
    }

    @Override
    public float pacPowerSpeed() {
        if (level == null) {
            return 0;
        }
        byte percentage = levelData(level.number()).pacSpeedPoweredPercentage();
        return percentage > 0 ? percentage * 0.01f * level.pac().baseSpeed() : pacNormalSpeed();
    }

    @Override
    public long pacPowerTicks() {
        return level != null ? 60 * levelData(level.number()).pacPowerSeconds() : 0;
    }

    @Override
    public float ghostAttackSpeed(Ghost ghost) {
        LevelData levelData = levelData(level.number());
        if (level.isTunnel(ghost.tile())) {
            return levelData.ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed();
        }
        if (ghost.id() == RED_GHOST_ID && cruiseElroy == 1) {
            return levelData.elroy1SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        if (ghost.id() == RED_GHOST_ID && cruiseElroy == 2) {
            return levelData.elroy2SpeedPercentage() * 0.01f * ghost.baseSpeed();
        }
        return levelData.ghostSpeedPercentage() * 0.01f * ghost.baseSpeed();
    }

    @Override
    public float ghostSpeedInsideHouse(Ghost ghost) {
        return 0.5f;
    }

    @Override
    public float ghostSpeedReturningToHouse(Ghost ghost) {
        return 2;
    }

    @Override
    public float ghostFrightenedSpeed(Ghost ghost) {
        if (level == null) return 0;
        float percentage = levelData(level.number()).ghostSpeedFrightenedPercentage();
        return percentage > 0 ? percentage * 0.01f * ghost.baseSpeed() : ghost.baseSpeed();
    }

    @Override
    public float ghostTunnelSpeed(Ghost ghost) {
        return level != null ? levelData(level.number()).ghostSpeedTunnelPercentage() * 0.01f * ghost.baseSpeed() : 0;
    }

    @Override
    protected void onFoodEaten(Vector2i tile, int uneatenFoodCount, boolean energizer) {
        level.pac().setRestingTicks(energizer ? 3 : 1);
        if (uneatenFoodCount == levelData(level.number()).elroy1DotsLeft()) {
            cruiseElroy = 1;
        } else if (uneatenFoodCount == levelData(level.number()).elroy2DotsLeft()) {
            cruiseElroy = 2;
        }
        if (energizer) {
            onEnergizerEaten();
            scoreManager().scorePoints(ENERGIZER_VALUE);
            Logger.info("Scored {} points for eating energizer", ENERGIZER_VALUE);
        } else {
            scoreManager.scorePoints(PELLET_VALUE);
        }
        gateKeeper.registerFoodEaten(level);
        if (isBonusReached()) {
            activateNextBonus();
            eventsThisFrame().setBonusIndex(level.nextBonusIndex());
        }
    }

    @Override
    public void onPacKilled() {
        huntingTimer.stop();
        level.pac().powerTimer().stop();
        level.pac().powerTimer().reset(0);
        gateKeeper.resetCounterAndSetEnabled(true);
        setCruiseElroyEnabled(false);
        level.pac().die();
    }

    @Override
    public void killGhost(Ghost ghost) {
        eventsThisFrame().killedGhosts().add(ghost);
        int killedSoFar = level.victims().size();
        int points = 100 * KILLED_GHOST_VALUE_MULTIPLIER[killedSoFar];
        level.victims().add(ghost);
        ghost.eaten(killedSoFar);
        scoreManager.scorePoints(points);
        Logger.info("Scored {} points for killing {} at tile {}", points, ghost.name(), ghost.tile());
        if (level.victims().size() == 16) {
            int extraPoints = POINTS_ALL_GHOSTS_EATEN_IN_LEVEL;
            scoreManager.scorePoints(extraPoints);
            Logger.info("Scored {} points for killing all ghosts in level {}", extraPoints, level.number());
        }
    }

    @Override
    public void endGame() {
        playingProperty().set(false);
        if (!THE_COIN_MECHANISM.isEmpty()) {
            THE_COIN_MECHANISM.consumeCoin();
        }
        scoreManager().updateHighScore();
        if (level != null) {
            level.showMessage(GameLevel.Message.GAME_OVER);
        }
        THE_GAME_EVENT_MANAGER.publishEvent(this, GameEventType.STOP_ALL_SOUNDS);
    }

    @Override
    public void buildDemoLevel() {
        buildLevel(1);
        level.setDemoLevel(true);
        assignDemoLevelBehavior(level.pac());
        demoLevelSteering.init();
        levelCounter.setEnabled(false);
    }

    @Override
    public void assignDemoLevelBehavior(Pac pac) {
        pac.setAutopilot(demoLevelSteering);
        pac.setUsingAutopilot(true);
        pac.setImmune(false);
    }
}
