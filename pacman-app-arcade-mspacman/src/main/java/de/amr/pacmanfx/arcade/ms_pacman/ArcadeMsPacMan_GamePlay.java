/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GamePlay;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.PositionSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusRouteInfo;
import de.amr.pacmanfx.core.entities.bonus.comp.BonusState;
import de.amr.pacmanfx.core.entities.ghost.comp.GhostState;
import de.amr.pacmanfx.core.entities.levelCounter.comp.LevelCounterBehavior;
import de.amr.pacmanfx.core.entities.levelCounter.system.LevelCounterSystem;
import de.amr.pacmanfx.core.event.bonus.BonusActivatedEvent;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.level.GameLevelEntities;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapPropertyName;
import de.amr.pacmanfx.core.rules.DefaultHuntingTimer;
import de.amr.pacmanfx.core.rules.GameRules;
import de.amr.pacmanfx.core.steering.RuleGuidedPacSteering;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static de.amr.basics.math.RandomNumbers.*;
import static de.amr.pacmanfx.core.Validations.requireValidLevelNumber;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_GamePlay extends ArcadePacMan_GamePlay {

    private static final Set<GhostState> TURNBACK_STATES = Set.of(
        GhostState.HUNTING_PAC, GhostState.LOCKED, GhostState.LEAVING_HOUSE);

    @Override
    public void configureLevelCounter(GameContext game, LevelCounterSystem levelCounterSystem, LevelCounter levelCounter) {
        levelCounter.pos().set(24 * TS, 34 * TS + 2);
        levelCounter.data().setBehavior(LevelCounterBehavior.DISABLE_WHEN_FULL);
        levelCounter.data().setCapacity(7);
        levelCounter.data().setEnabled(true);
        levelCounterSystem.clear(levelCounter);
    }

    @Override
    public GameLevel createLevel(GameContext game, int levelNumber) {
        requireNonNull(game);
        requireValidLevelNumber(levelNumber);

        final GameLevelEntities entities = new GameLevelEntities();

        final WorldNavigationSystem navigator = game.variant().systems().navigator();
        final WorldMap worldMap = game.variant().worldMapManager().supplyWorldMap(levelNumber);

        createAndAddEntities(entities, worldMap.terrainLayer());
        configurePacAndGhosts(entities, game.variant().systems(), worldMap.terrainLayer(), entities.house());

        final DefaultHuntingTimer huntingTimer = new DefaultHuntingTimer("Arcade Ms. Pac-Man Hunting Timer", game.variant().rules().numHuntingPhases());
        huntingTimer.setPhaseChangeCallback(newPhaseIndex -> {
            if (newPhaseIndex > 0) {
                entities.ghostsInAnyOfStates(TURNBACK_STATES).forEach(navigator::requestTurnBack);
            }
        });

        final GameLevel level = new GameLevel(levelNumber, worldMap, entities, huntingTimer);

        final GameRules rules = game.variant().rules();
        level.setBonusSymbolCodes(rules.bonusSymbols(levelNumber));

        final GameSession session = game.session();

        /* In Ms. Pac-Man, the level counter stays fixed from level 8 on and bonus symbols are created randomly
         * (also inside a level) whenever a bonus score is reached. At least that's what I was told. */
        final LevelCounter levelCounter = session.hud().levelCounter();
        levelCounter.data().setEnabled(levelNumber < 8);

        session.setGameOverStateTicks(GAME_OVER_STATE_TICKS);
        session.setLevel(level);

        return level;
    }

    private void createAndAddEntities(GameLevelEntities entities, TerrainLayer terrain) {
        final Vector2i houseMinTile = terrain.getTilePropertyOrDefault(
            WorldMapPropertyName.POS_HOUSE_MIN_TILE, ArcadePacMan_GamePlay.ARCADE_MAP_HOUSE_MIN_TILE);
        terrain.propertyMap().put(WorldMapPropertyName.POS_HOUSE_MIN_TILE, houseMinTile.toString());

        final House house = HouseFactory.createArcadeHouse(houseMinTile);
        final MessageView messageView = createMessageView(house);

        final var actorFactory  = new ArcadeMsPacMan_ActorFactory();
        final Pac msPacMan      = actorFactory.createMsPacMan();
        final Ghost redGhost    = actorFactory.createRedGhost();
        final Ghost pinkGhost   = actorFactory.createPinkGhost();
        final Ghost cyanGhost   = actorFactory.createCyanGhost();
        final Ghost orangeGhost = actorFactory.createOrangeGhost();

        entities.add(house);
        entities.add(messageView);
        entities.add(msPacMan);
        entities.add(redGhost);
        entities.add(pinkGhost);
        entities.add(cyanGhost);
        entities.add(orangeGhost);
    }

    private void configurePacAndGhosts(GameLevelEntities entities, GameSystems systems, TerrainLayer terrain, House house) {
        entities.pac().autoSteering().setSteering(new RuleGuidedPacSteering(
            systems.navigator(), systems.pacWorldMovementPolicy()
        ));

        entities.ghost(GhostPersonality.RED_GHOST_SHADOW)  .worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_1_RED);
        entities.ghost(GhostPersonality.PINK_GHOST_SPEEDY) .worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_2_PINK);
        entities.ghost(GhostPersonality.CYAN_GHOST_BASHFUL).worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_3_CYAN);
        entities.ghost(GhostPersonality.ORANGE_GHOST_POKEY).worldInfo().init(terrain, house, WorldMapPropertyName.POS_GHOST_4_ORANGE);
    }

    @Override
    public GameLevel buildDemoLevel(GameContext game) {
        requireNonNull(game);

        final GameSession session = game.session();
        final GameSystems systems = game.variant().systems();

        final GameLevel level = createLevel(game, 1);

        session.setLevel(level);
        session.setAttractMode(true);

        final Pac pac = level.entities().pac();
        pac.cheats().setImmune(false);
        pac.cheats().setUsingAutopilot(true);
        pac.autoSteering().setSteering(
            new RuleGuidedPacSteering(systems.navigator(), systems.pacWorldMovementPolicy()));

        session.hud().gameScore().data().setLevelNumber(level.number());
        session.hud().levelCounter().data().setEnabled(true);

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

        final Bonus prevBonus = level.entities().optBonus().orElse(null);
        if (prevBonus != null) {
            if (prevBonus.state().enumValue() == BonusState.EDIBLE) {
                //TODO Can this happen in original game?
                Logger.info("Previous bonus is still edible, skip new bonus creation");
                return;
            }
            level.entities().remove(prevBonus);
        }

        level.selectNextBonus();
        final int symbolCode = level.bonusSymbolCode(level.currentBonusIndex());

        // Maps in XXL game variant or custom maps might have no portal, in this case use static bonus
        final boolean portalExists = !level.worldMap().terrainLayer().horizontalPortals().isEmpty();
        final Bonus bonus = portalExists
            ? createMovingBonus(game, level, symbolCode)
            : createStaticBonus(level, symbolCode, randomFloat(9, 10));
        level.entities().add(bonus);

        game.variant().systems().bonusState().setEdible(bonus);

        game.eventManager().publishGameEvent(new BonusActivatedEvent(bonus));
    }

    private Bonus createStaticBonus(GameLevel level, int symbolCode, float lifetimeSec) {
        final Bonus bonus = Bonus.createStaticBonus(symbolCode);
        final Vector2i bonusTile = level.worldMap().terrainLayer()
            .getTilePropertyOrDefault(WorldMapPropertyName.POS_BONUS, new Vector2i(13, 20));
        bonus.pos().set(WorldMap.halfTileRightOf(bonusTile));
        bonus.setLifetimeSec(lifetimeSec);
        return bonus;
    }

    private Bonus createMovingBonus(GameContext game, GameLevel level, int symbolCode) {
        final House house = level.entities().house();
        if (house == null) {
            throw new IllegalStateException("Moving bonus cannot be activated, no house exists in this level!");
        }

        final GameSystems systems = game.variant().systems();
        final GameRules rules = game.variant().rules();

        final Bonus movingBonus = Bonus.createMovingBonus(symbolCode);
        systems.bonusState().setEdible(movingBonus);

        final BonusRouteInfo routeInfo = computeBonusRoute(level.worldMap().terrainLayer(), house);
        final float speed = rules.actorSpeedRules().bonusSpeed(game, level);
        systems.bonusMoveAndJump().startWandering(movingBonus, routeInfo, speed);

        return movingBonus;
    }

    private BonusRouteInfo computeBonusRoute(TerrainLayer terrain, House house) {
        final List<HPortal> portals = terrain.horizontalPortals();
        if (portals.isEmpty()) {
            Logger.error("Moving bonus cannot be activated, game level does not contain any portals");
            return new BonusRouteInfo(false, List.of());
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

        final Vector2i houseEntry = PositionSystem.computeTileAt(house.floorplan().entryPosition());
        final Vector2i backyard = houseEntry.plus(0, house.sizeInTiles().y() + 1);
        final List<Vector2i> waypoints = Stream.of(entryTile, houseEntry, backyard, houseEntry, exitTile).toList();

        Logger.info("Moving bonus route: {} (crossing {})", waypoints, leftToRight ? "left to right" : "right to left");

        return new BonusRouteInfo(leftToRight, waypoints);
    }
}
