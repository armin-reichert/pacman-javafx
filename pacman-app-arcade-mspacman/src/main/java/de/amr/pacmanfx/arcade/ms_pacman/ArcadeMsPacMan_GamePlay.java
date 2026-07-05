/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_GamePlay;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.BonusActivatedEvent;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.BonusState;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.*;
import de.amr.pacmanfx.steering.RuleBasedPacSteering;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.basics.math.RandomNumberSupport.*;

public class ArcadeMsPacMan_GamePlay extends ArcadePacMan_GamePlay {

    private static final int DEMO_LEVEL_MIN_DURATION_MILLIS = 20_000;

    @Override
    public GameLevel buildDemoLevel(GameContext context) {
        final int demoLevelNumber = 1;
        final GameModel model = context.model();
        final GameLevel level = model.createLevel(demoLevelNumber, true);

        final Pac pac = level.entities().pac();
        pac.setImmune(false);
        pac.setUsingAutopilot(true);

        final var demoLevelSteering = new RuleBasedPacSteering();
        pac.setAutomaticSteering(demoLevelSteering);
        demoLevelSteering.init();

        model.gateKeeper().setLevelNumber(demoLevelNumber);
        model.levelCounter().setEnabled(true);
        model.score().setLevelNumber(demoLevelNumber);

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
        final var bonus = new Bonus(bonusSymbolCode, context.model().rules().pointsForBonus(bonusSymbolCode));
        if (terrain.horizontalPortals().isEmpty()) {
            final Vector2i bonusTile = terrain.getTilePropertyOrDefault(WorldMapPropertyName.POS_BONUS, new Vector2i(13, 20));
            bonus.setPosition(WorldMap.halfTileRightOf(bonusTile));
            bonus.showEdibleForSeconds(randomFloat(9, 10));
        } else {
            computeBonusRoute(bonus, terrain, house);
            bonus.showEdibleAndStartWandering(context.model().rules().actorSpeedControl().bonusSpeed(level));
        }

        level.setBonus(bonus);
        context.flow().publishGameEvent(new BonusActivatedEvent(context, bonus));
    }

    @Override
    public boolean isPacSafeInDemoLevel(GameLevel demoLevel) {
        float runningMillis = System.currentTimeMillis() - demoLevel.startTime();
        return runningMillis <= DEMO_LEVEL_MIN_DURATION_MILLIS;
    }

    // ------------------------------------------------

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
