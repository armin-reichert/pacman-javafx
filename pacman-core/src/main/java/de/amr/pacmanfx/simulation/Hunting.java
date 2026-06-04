/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.event.PacEatsFoodEvent;
import de.amr.pacmanfx.event.PacGetsPowerEvent;
import de.amr.pacmanfx.event.PacLostPowerEvent;
import de.amr.pacmanfx.event.PacPowerFadesEvent;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.model.world.TerrainLayer;
import org.tinylog.Logger;

import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.core.Globals.HTS;
import static de.amr.pacmanfx.core.Globals.TS;

public class Hunting {

    public static void doHuntingStep(GameContext context) {

        context.startNewHuntingStep();

        final HuntingStepResult result = context.huntingResult();

        final GameModel game = context.gameModel();
        final GameLevel level = game.optGameLevel().orElseThrow();

        if (game.gateKeeper() != null) {
            game.gateKeeper().unlockGhostIfPossible(level, level.worldMap().terrainLayer().house());
        }
        game.cheats().update(level);

        level.heartbeat().triggerPulse();

        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        final Bonus bonus = level.entities().optBonus().orElse(null);

        boolean quitHunting;
        if (game.isCollisionDoubleChecked()) {
            quitHunting = evalCollisions(result, game, level, pac, ghosts, bonus);
            if (!quitHunting) {
                level.entities().forEach(e -> e.update(level));
                quitHunting = evalCollisions(result, game, level, pac, ghosts, bonus);
            }
        }
        else {
            level.entities().forEach(e -> e.update(level));
            quitHunting = evalCollisions(result, game, level, pac, ghosts, bonus);
        }

        if (quitHunting) {
            Logger.info("Hunting has been stopped!");
            return;
        }

        checkFoodFound(result, game, level, pac);
        checkBonusFound(result, game, level);

        if (!game.rules().isLevelCompleted(level)) {
            updatePacPowerMode(result, game, level, pac);
            level.huntingTimer().update(game.rules(), level.number());
        }
    }

    // Collision behavior is controlled by the current collision strategy.
    // The original Arcade games use tile-based collision which can lead to missed collisions
    // by passing through!

    public static void detectCollisions(HuntingStepResult result, GameModel game, GameLevel level, Pac pac, List<Ghost> ghosts, Bonus bonus) {

        result.ghostsCollidingWithPac().clear();
        ghosts.stream().filter(ghost -> game.collisionStrategy().collide(pac, ghost))
            .forEach(result.ghostsCollidingWithPac()::add);

        if (bonus != null && bonus.state() == BonusState.EDIBLE && game.collisionStrategy().collide(pac, bonus)) {
            result.setEdibleBonus(bonus);
        }

        final Vector2i pacTile = pac.computeTile();
        if (level.worldMap().foodLayer().hasFoodAtTile(pacTile)) {
            result.setFoodFoundTile(pacTile);
            result.setEnergizerFound(level.worldMap().foodLayer().isEnergizerTile(pacTile));
        }
    }

    private static boolean evalCollisions(HuntingStepResult result, GameModel game, GameLevel level, Pac pac, List<Ghost> ghosts, Bonus bonus) {
        detectCollisions(result, game, level, pac, ghosts, bonus);
        if (result.detectedPacGhostCollision()) {
            // Is Pac getting killed after the collision with a ghost?
            // He might stay alive if immune or in level's safe phase!
            checkPacKilled(result, game, level, pac);
            if (result.hasPacManBeenKilled()) {
                return true;
            }
            else {
                // Frightened ghosts get killed when colliding with Pac
                result.ghostsCollidingWithPac().stream()
                    .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                    .forEach(result.ghostsKilled()::add);
                // More than one ghost might have been killed in this step
                result.ghostsKilled().forEach(ghost -> game.onEatGhost(level, ghost));
                if (result.hasGhostBeenKilled()) {
                    return true;
                }
            }

            // If collision happened while teleporting (horizontally), move collided actors into visible world
            final TerrainLayer terrain = level.worldMap().terrainLayer();
            terrain.hPortalContainingTile(pac.computeTile()).ifPresent(hPortal -> {
                if (pac.moveDir() == Direction.LEFT) {
                    pac.setX(hPortal.rightBorderEntryTile().x() * TS + HTS);
                } else if (pac.moveDir() == Direction.RIGHT) {
                    pac.setX(hPortal.leftBorderEntryTile().x() * TS - HTS);
                }
                // Not sure if colliding ghosts should also be moved back to light
                //simStep.ghostsCollidingWithPac.forEach(ghost -> ghost.setX(pac.x()));
                Logger.info("Detected collision while teleporting, moved Pac-Man back into world");
            });
        }

        return false;
    }

    private static void checkPacKilled(HuntingStepResult result, GameModel game, GameLevel level, Pac pac) {
        final boolean demoLevel = level.isDemoLevel();
        if (demoLevel && game.isPacSafeInDemoLevel(level) || !demoLevel && pac.isImmune()) {
            return;
        }
        result.setPacKiller(result.ghostsCollidingWithPac().stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst()
            .orElse(null));
    }

    private static void checkFoodFound(HuntingStepResult result, GameModel game, GameLevel level, Pac pac) {
        if (!result.foodFound()) {
            pac.continueStarving();
        } else {
            level.worldMap().foodLayer().markFoodEatenAt(result.foodFoundTile());
            pac.endStarving();
            if (result.energizerFound()) {
                if (!game.rules().isLevelCompleted(level)) {
                    startPacPowerMode(result, game, level, pac);
                }
                game.eatEnergizer(level, result.foodFoundTile());
            } else {
                game.eatPellet(level, result.foodFoundTile());
            }
            if (game.rules().isBonusAwarded(level)) {
                game.activateNextBonus(level);
                result.setBonusIndex(level.currentBonusIndex());
            }
            game.flow().publishGameEvent(new PacEatsFoodEvent(game.flow().context(), pac, result.energizerFound(), false));
        }
    }

    private static void checkBonusFound(HuntingStepResult result, GameModel game, GameLevel level) {
        if (result.foundEdibleBonus()) {
            game.eatBonus(level, result.edibleBonus());
        }
    }

    private static void startPacPowerMode(HuntingStepResult result, GameModel game, GameLevel level, Pac pac) {
        level.ghostsInAnyOfStates(Set.of(GhostState.FRIGHTENED, GhostState.HUNTING_PAC)).forEach(MovingActor::requestTurnBack);
        final float powerSeconds = level.pacPowerSeconds();
        if (powerSeconds > 0) {
            level.huntingTimer().stop();
            Logger.debug("Hunting stopped (Pac-Man got power)");
            final long powerTicks = TickTimer.secToTicks(powerSeconds);
            pac.powerTimer().restartTicks(powerTicks);
            Logger.debug("Power timer restarted, {} ticks ({0.00} sec)", powerTicks, powerSeconds);
            level.ghostsInState(GhostState.HUNTING_PAC).forEach(ghost -> ghost.setState(GhostState.FRIGHTENED));
            game.flow().publishGameEvent(new PacGetsPowerEvent(game.flow().context(), pac));
        }
    }

    private static void updatePacPowerMode(HuntingStepResult result, GameModel game, GameLevel level, Pac pac) {
        if (pac.powerTimer().isRunning()) {
            pac.powerTimer().doTick();
            if (pac.isPowerFadingStarting(level)) {
                result.setPacStartsLosingPower(true);
                game.flow().publishGameEvent(new PacPowerFadesEvent(game.flow().context(), pac));
            } else if (pac.powerTimer().hasExpired()) {
                result.setPacLostPower(true);
                pac.powerTimer().stop();
                pac.powerTimer().reset(0);
                level.killedGhostsForCurrentEnergizer().clear();
                level.huntingTimer().start();
                level.ghostsInState(GhostState.FRIGHTENED).forEach(ghost -> ghost.setState(GhostState.HUNTING_PAC));
                game.flow().publishGameEvent(new PacLostPowerEvent(game.flow().context(), pac));
            }
        }
    }
}