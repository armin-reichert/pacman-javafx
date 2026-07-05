/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.model;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameRules;
import de.amr.pacmanfx.arcade.pacman.model.Arcade_GameModel;
import de.amr.pacmanfx.arcade.pacman.model.LevelData;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.BonusActivatedEvent;
import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.HuntingTimer;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.amr.basics.math.RandomNumberSupport.*;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.model.world.WorldMapPropertyName.*;
import static java.util.Objects.requireNonNull;

/**
 * Ms. Pac-Man Arcade game.
 *
 * <p>There are slight differences to the original Arcade game.
 * <ul>
 *     <li>Attract mode is just a random hunting for at least 20 seconds.</li>
 *     <li>Timing of hunting phases unclear, just took all the information I had</li>
 *     <li>Bonus does not follow original "fruit paths" but randomly selects a portal to
 *     enter the maze, turns around the house and leaves the maze at a random portal on the other side</li>
 * </ul>
 * </p>
 */
public class ArcadeMsPacMan_GameModel extends Arcade_GameModel {

    private static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;

    protected static final int GAME_OVER_STATE_TICKS = 150;

    public ArcadeMsPacMan_GameModel() {
        this(new ArcadeMsPacMan_MapSelector());
    }

    public ArcadeMsPacMan_GameModel(WorldMapSelector mapSelector) {
        this.mapSelector = requireNonNull(mapSelector);
        levelCounter = new ArcadeMsPacMan_LevelCounter();
        demoLevelSteering = new RuleBasedPacSteering();
        automaticSteering = new RuleBasedPacSteering();
        actorSpeedControl = new ArcadeMsPacMan_ActorSpeedControl();
        createGateKeeper();
        mapSelector.loadMapPrototypes();
    }

    @Override
    public GameLevel createLevel(int levelNumber, boolean demoLevel) {
        requireValidLevelNumber(levelNumber);

        final WorldMap worldMap = mapSelector.supplyWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final Vector2i houseMinTile = terrain.getTilePropertyOrDefault(POS_HOUSE_MIN_TILE, ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.propertyMap().put(POS_HOUSE_MIN_TILE, houseMinTile.toString());

        terrain.setHouse(new ArcadeHouse(houseMinTile));

        final HuntingTimer huntingTimer = createHuntingTimer(rules);
        final int numFlashes = ArcadePacMan_GameRules.levelData(levelNumber).numFlashes();

        final GameLevel level = new GameLevel(this, levelNumber, worldMap, huntingTimer, numFlashes);
        level.setDemoLevel(demoLevel);

        level.setGameOverStateTicks(GAME_OVER_STATE_TICKS);

        final LevelData levelData = ArcadePacMan_GameRules.levelData(levelNumber);
        level.setPacPowerSeconds(levelData.secPacPower());
        level.setPacPowerFadingSeconds(0.5f * numFlashes); //TODO correct?

        createAndSetMsPacMan(level);
        createAndSetGhosts(level, terrain.house());

        level.setBonusSymbolCode(0, rules.selectBonusSymbolCode(level.number(), 0));
        level.setBonusSymbolCode(1, rules.selectBonusSymbolCode(level.number(), 1));

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        levelCounter.setEnabled(levelNumber < 8);

        return level;
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        float runningMillis = System.currentTimeMillis() - demoLevel.startTime();
        return runningMillis <= DEMO_LEVEL_MIN_DURATION_MILLIS;
    }

    /**
     * Bonus symbol that enters the world at some tunnel entry, walks to the house entry, takes a tour around the
     * house and finally leaves the world through a tunnel on the opposite side of the world.
     * <p>
     * Note: This is not the exact behavior from the original Arcade game that uses fruit paths.
     * <p>
     * According to <a href="https://strategywiki.org/wiki/Ms._Pac-Man/Walkthrough">this</a> Wiki,
     * some maps have a fixed entry tile for the bonus.
     * TODO: Not sure if that's correct.
     *
     **/
    @Override
    public void activateNextBonus(GameContext context, GameLevel level) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();

        if (level.optBonus().isPresent() && level.optBonus().get().state() == BonusState.EDIBLE) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        final House house = terrain.optHouse().orElse(null);
        if (house == null) {
            Logger.error("Moving bonus cannot be activated, no house exists in this level!");
            return;
        }

        level.selectNextBonus();
        final int bonusSymbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final var bonus = new Bonus(bonusSymbolCode, rules.pointsForBonus(bonusSymbolCode));
        if (terrain.horizontalPortals().isEmpty()) {
            final Vector2i bonusTile = terrain.getTilePropertyOrDefault(WorldMapPropertyName.POS_BONUS, new Vector2i(13, 20));
            bonus.setPosition(WorldMap.halfTileRightOf(bonusTile));
            bonus.showEdibleForSeconds(randomFloat(9, 10));
        } else {
            computeBonusRoute(bonus, terrain, house);
            bonus.showEdibleAndStartWandering(actorSpeedControl().bonusSpeed(level));
        }

        level.setBonus(bonus);
        context.flow().publishGameEvent(new BonusActivatedEvent(context, bonus));
    }

    // Helpers

    private void createAndSetMsPacMan(GameLevel level) {
        final Pac msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAutomaticSteering(automaticSteering);
        level.setPac(msPacMan);
    }

    private void createAndSetGhosts(GameLevel level, House house) {
        final TerrainLayer terrain = level.worldMap().terrainLayer();
        level.setGhosts(
            ArcadeMsPacMan_ActorFactory.createGhost(RED_GHOST_SHADOW, terrain, house, POS_GHOST_1_RED),
            ArcadeMsPacMan_ActorFactory.createGhost(PINK_GHOST_SPEEDY, terrain, house, POS_GHOST_2_PINK),
            ArcadeMsPacMan_ActorFactory.createGhost(CYAN_GHOST_BASHFUL, terrain, house, POS_GHOST_3_CYAN),
            ArcadeMsPacMan_ActorFactory.createGhost(ORANGE_GHOST_POKEY, terrain, house, POS_GHOST_4_ORANGE)
        );
    }

    private void createGateKeeper() {
        gateKeeper = new GateKeeper();
        gateKeeper.setOnGhostReleased((level, releasedPrisoner) -> {
            if (releasedPrisoner.personality() == ORANGE_GHOST_POKEY) {
                final Ghost blinky = level.ghost(RED_GHOST_SHADOW);
                if (blinky.elroy().boost() != Elroy.Boost.NONE && !blinky.elroy().enabled()) {
                    blinky.elroy().setEnabled(true);
                    Logger.debug("Re-enabled {}'s Elroy state ({}). Reason; ({} got released):",
                        blinky.name(), blinky.elroy(), releasedPrisoner.name());
                }
            }
        });
    }

    private HuntingTimer createHuntingTimer(GameRules gameRules) {
        final var huntingTimer = new HuntingTimer("Arcade Ms. Pac-Man Hunting Timer", gameRules.numHuntingPhases());
        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> optGameLevel().ifPresent(level -> {
            if (newPhaseIndex > 0) {
                level.ghostsInAnyOfStates(Set.of(GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE))
                    .forEach(Ghost::requestTurnBack);
            }
        }));
        return huntingTimer;
    }

    private void computeBonusRoute(Bonus bonus, TerrainLayer terrain, House house) {
        final List<HPortal> portals = terrain.horizontalPortals();
        if (portals.isEmpty()) {
            Logger.error("Moving bonus cannot be activated, game level does not contain any portals");
            return;
        }

        Vector2i entryTile = terrain.getTileProperty(WorldMapPropertyName.POS_BONUS);
        Vector2i exitTile;
        boolean leftToRight;
        if (entryTile != null) { // Map defines bonus entry tile
            final int exitPortalIndex = randomInt(0, portals.size());
            final HPortal exitPortal = portals.get(exitPortalIndex);
            if (entryTile.x() == 0) { // enter maze at left border
                exitTile = exitPortal.rightBorderEntryTile().plus(1, 0);
                leftToRight = true;
            } else { // bonus entry is at right map border
                exitTile = exitPortal.leftBorderEntryTile().minus(1, 0);
                leftToRight = false;
            }
        }
        else { // choose random crossing direction and random entry and exit portals
            final HPortal entryPortal = portals.get(randomInt(0, portals.size()));
            final HPortal exitPortal = portals.get(randomInt(0, portals.size()));
            leftToRight = randomBoolean();
            if (leftToRight) {
                entryTile = entryPortal.leftBorderEntryTile();
                exitTile  = exitPortal.rightBorderEntryTile().plus(1, 0);
            } else {
                entryTile = entryPortal.rightBorderEntryTile();
                exitTile = exitPortal.leftBorderEntryTile().minus(1, 0);
            }
        }

        final Vector2i houseEntry = WorldMap.computeTileAt(house.entryPosition());
        final Vector2i backyard = houseEntry.plus(0, house.sizeInTiles().y() + 1);
        final List<Vector2i> route = Stream.of(entryTile, houseEntry, backyard, houseEntry, exitTile).toList();

        bonus.setMazeRoute(route, leftToRight);
        Logger.info("Moving bonus route: {} (crossing {})", route, leftToRight ? "left to right" : "right to left");
    }
}