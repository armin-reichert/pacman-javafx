/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GamePlay;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GameVariantConfig;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterBehavior;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntitySet;
import de.amr.pacmanfx.core.model.rules.GameRules;
import de.amr.pacmanfx.core.model.rules.HuntingTimer;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapPropertyName;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.amr.basics.math.RandomNumberSupport.*;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_GamePlay extends ArcadePacMan_GamePlay {

    private static final Set<GhostState> TURNBACK_STATES = Set.of(
        GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE);

    private static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;

    @Override
    public void configureLevelCounter(GameContext game, LevelCounter levelCounter) {
        final LevelCounterSystem system = game.variantConfig().systems().levelCounterSystem();
        system.setCounterBehavior(levelCounter, LevelCounterBehavior.DISABLE_WHEN_FULL);
        system.setCounterCapacity(levelCounter, 7);
        system.clearCounter(levelCounter);
        system.enableCounter(levelCounter, true);
    }

    @Override
    public GameLevel createLevel(GameContext game, int levelNumber) {
        requireNonNull(game);
        requireValidLevelNumber(levelNumber);

        final GameLevelEntitySet entities = new GameLevelEntitySet();

        final GameSession session = game.session();
        final WorldNavigationSystem navigator = game.variantConfig().systems().worldNavigator();
        final WorldMap worldMap = game.variantConfig().worldMapManager().supplyWorldMap(levelNumber);
        final TerrainLayer terrain = worldMap.terrainLayer();

        final Vector2i houseMinTile = terrain.getTilePropertyOrDefault(
            WorldMapPropertyName.POS_HOUSE_MIN_TILE, ArcadePacMan_GameVariantConfig.ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.propertyMap().put(WorldMapPropertyName.POS_HOUSE_MIN_TILE, houseMinTile.toString());

        final House house = HouseFactory.createArcadeHouse(houseMinTile);
        entities.add(house);

        createAndSetMsPacMan(entities, game.variantConfig().systems());
        createAndSetGhosts(entities, worldMap.terrainLayer(), house);

        entities.add(new MessageView());

        final HuntingTimer huntingTimer = new HuntingTimer("Arcade Ms. Pac-Man Hunting Timer", game.variantConfig().rules().numHuntingPhases());

        final GameLevel level = new GameLevel(levelNumber, worldMap, entities, huntingTimer);

        session.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        session.setLevel(level);

        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> {
            if (newPhaseIndex > 0) {
                level.entities().ghostsInAnyOfStates(TURNBACK_STATES).forEach(navigator::requestTurnBack);
            }
        });

        final GameRules rules = game.variantConfig().rules();
        level.setBonusSymbolCodes(rules.bonusSymbols(levelNumber));

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        final LevelCounterSystem levelCounterSystem = game.variantConfig().systems().levelCounterSystem();
        levelCounterSystem.enableCounter(session.levelCounter(), levelNumber < 8);

        return level;
    }

    protected void createAndSetMsPacMan(GameLevelEntitySet entities, GameSystems systems) {
        final var factory = new ArcadeMsPacMan_ActorFactory();
        final Pac msPacMan = factory.createMsPacMan();
        entities.add(msPacMan);

        msPacMan.autoSteering().setSteering(new RuleGuidedPacSteering(
            systems.worldNavigator(), systems.pacWorldMovementPolicy()
        ));
    }

    private void createAndSetGhosts(GameLevelEntitySet entities, TerrainLayer terrain, House house) {
        final var factory = new ArcadeMsPacMan_ActorFactory();

        final Ghost redGhost = factory.createRedGhost();
        final Ghost pinkGhost = factory.createPinkGhost();
        final Ghost cyanGhost = factory.createCyanGhost();
        final Ghost orangeGhost = factory.createOrangeGhost();

        redGhost.worldInfo()   .init(terrain, house, WorldMapPropertyName.POS_GHOST_1_RED);
        pinkGhost.worldInfo()  .init(terrain, house, WorldMapPropertyName.POS_GHOST_2_PINK);
        cyanGhost.worldInfo()  .init(terrain, house, WorldMapPropertyName.POS_GHOST_3_CYAN);
        orangeGhost.worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_4_ORANGE);

        entities.add(redGhost);
        entities.add(pinkGhost);
        entities.add(cyanGhost);
        entities.add(orangeGhost);
    }

    @Override
    public GameLevel buildDemoLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameSystems systems = game.variantConfig().systems();

        final GameLevel level = createLevel(game, 1);

        session.setLevel(level);
        session.setAttractMode(true);

        final Pac pac = level.entities().pac();
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);

        final var steering = new RuleGuidedPacSteering(
            systems.worldNavigator(),
            systems.pacWorldMovementPolicy()
        );
        pac.autoSteering().setSteering(steering);

        session.gateKeeper().setLevelNumber(1);
        ScoreSystem.setLevelNumber(session.score(), 1);

        final LevelCounterSystem levelCounterSystem = game.variantConfig().systems().levelCounterSystem();
        levelCounterSystem.enableCounter(session.levelCounter(), true);

        return level;
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
    public void activateNextBonus(GameContext game, GameLevel level) {
        requireNonNull(game);
        requireNonNull(level);

        final GameSystems sys = game.variantConfig().systems();

        final TerrainLayer terrain = level.worldMap().terrainLayer();

        if (level.entities().optBonus().isPresent() && level.entities().optBonus().get().bonusState() == BonusState.EDIBLE) {
            Logger.info("Previous bonus is still active, skip this bonus");
            return;
        }

        final House house = level.entities().house();
        if (house == null) {
            Logger.error("Moving bonus cannot be activated, no house exists in this level!");
            return;
        }

        level.selectNextBonus();

        final int symbolCode = level.bonusSymbolCode(level.currentBonusIndex());
        final int value = game.variantConfig().rules().scoringRules().pointsForBonus(symbolCode);

        Bonus bonus;
        if (terrain.horizontalPortals().isEmpty()) {
            bonus = Bonus.createStaticBonus(symbolCode, value);
            final Vector2i bonusTile = terrain.getTilePropertyOrDefault(WorldMapPropertyName.POS_BONUS, new Vector2i(13, 20));
            bonus.pos().set(WorldMap.halfTileRightOf(bonusTile));
            sys.bonusState().showEdibleForSeconds(bonus, randomFloat(9, 10));
        } else {
            bonus = Bonus.createMovingBonus(symbolCode, value);
            computeBonusRoute(game, bonus, terrain, house);
            final float speed = game.variantConfig().rules().actorSpeedRules().bonusSpeed(game, level);
            sys.bonusState().showEdibleAndStartWandering(bonus, speed);
        }

        level.entities().optBonus().ifPresent(oldBonus -> level.entities().remove(oldBonus));
        level.entities().add(bonus);

        game.eventManager().publishGameEvent(new BonusActivatedEvent(bonus));
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameSession session, GameLevel demoLevel) {
        float runningMillis = System.currentTimeMillis() - session.levelStartTimeMillis();
        return runningMillis <= DEMO_LEVEL_MIN_DURATION_MILLIS;
    }

    // ------------------------------------------------

    private void computeBonusRoute(GameContext game, Bonus bonus, TerrainLayer terrain, House house) {
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

        final Vector2i houseEntry = WorldMap.computeTileAt(house.floorplan().entryPosition());
        final Vector2i backyard = houseEntry.plus(0, house.sizeInTiles().y() + 1);
        final List<Vector2i> route = Stream.of(entryTile, houseEntry, backyard, houseEntry, exitTile).toList();

        game.variantConfig().systems().bonusMoveAndJump().setRoute(bonus, route, leftToRight);
        Logger.info("Moving bonus route: {} (crossing {})", route, leftToRight ? "left to right" : "right to left");
    }
}
