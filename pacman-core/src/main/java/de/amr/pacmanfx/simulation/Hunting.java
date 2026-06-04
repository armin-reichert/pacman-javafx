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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.amr.pacmanfx.core.Globals.HTS;
import static de.amr.pacmanfx.core.Globals.TS;

public class Hunting {

    public static class Result {
        private Vector2i foodFoundTile;
        private boolean energizerFound;
        private int bonusIndex;
        private Bonus edibleBonus;
        private boolean pacGotPower;
        private boolean pacStartsLosingPower;
        private boolean pacLostPower;
        private Ghost pacKiller;
        private final List<Ghost> ghostsKilled = new ArrayList<>();
        private final Set<Ghost> ghostsCollidingWithPac = new HashSet<>(4);

        public Result() {
            bonusIndex = -1;
        }

        public List<String> createReport() {
            var messages = new ArrayList<String>();
            for (Ghost ghost : ghostsCollidingWithPac) {
                messages.add("%s collided with Pac at tile %s, state after collision: %s".formatted(ghost.name(), ghost.computeTile(), ghost.state()));
            }
            if (energizerFound) {
                messages.add("Energizer found at " + foodFoundTile);
            }
            if (bonusIndex != -1) {
                messages.add("Bonus score reached, index=" + bonusIndex);
            }
            if (edibleBonus != null) {
                messages.add("Bonus eaten: %s".formatted(edibleBonus));
            }
            if (pacGotPower) {
                messages.add("Pac gained power");
            }
            if (pacStartsLosingPower) {
                messages.add("Pac starts losing power");
            }
            if (pacLostPower) {
                messages.add("Pac lost power");
            }
            if (pacKiller != null) {
                messages.add("Pac killed by %s at tile %s".formatted(pacKiller.name(), pacKiller.computeTile()));
            }
            for (Ghost ghost : ghostsKilled) {
                messages.add("%s killed at %s".formatted(ghost.name(), ghost.computeTile()));
            }
            return messages;
        }

        public void printLog() {
            var report = createReport();
            if (!report.isEmpty()) {
                Logger.info("Step:");
                for (var msg : report) {
                    Logger.info("- " + msg);
                }
            }
        }

        public Vector2i foodFoundTile() {
            return foodFoundTile;
        }

        public boolean foodFound() {
            return foodFoundTile != null;
        }

        public boolean energizerFound() {
            return energizerFound;
        }

        public Set<Ghost> ghostsCollidingWithPac() {
            return ghostsCollidingWithPac;
        }

        public boolean detectedPacGhostCollision() {
            return !ghostsCollidingWithPac.isEmpty();
        }

        public boolean hasPacManBeenKilled() {
            return pacKiller != null;
        }

        public List<Ghost> ghostsKilled() {
            return ghostsKilled;
        }

        public boolean hasGhostBeenKilled() {
            return !ghostsKilled.isEmpty();
        }

        public Bonus edibleBonus() {
            return edibleBonus;
        }

        public boolean foundEdibleBonus() {
            return edibleBonus != null;
        }

        public void setBonusIndex(int bonusIndex) {
            this.bonusIndex = bonusIndex;
        }

        public void setPacGotPower(boolean pacGotPower) {
            this.pacGotPower = pacGotPower;
        }

        public void setPacStartsLosingPower(boolean pacStartsLosingPower) {
            this.pacStartsLosingPower = pacStartsLosingPower;
        }

        public void setPacLostPower(boolean pacLostPower) {
            this.pacLostPower = pacLostPower;
        }

        public boolean pacLostPower() {
            return pacLostPower;
        }
    }

    /* -------------------------------------------------------------------------
     * Main simulation step
     * ---------------------------------------------------------------------- */

    public static void doHuntingStep(GameContext context) {

        context.startNewHuntingStep();

        final Result result = context.huntingResult();

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

    public static void detectCollisions(Result result, GameModel game, GameLevel level, Pac pac, List<Ghost> ghosts, Bonus bonus) {
        // Ghosts colliding with Pac?
        result.ghostsCollidingWithPac.clear();
        ghosts.stream().filter(ghost -> game.collisionStrategy().collide(pac, ghost))
            .forEach(result.ghostsCollidingWithPac::add);

        result.edibleBonus = null;
        if (bonus != null && bonus.state() == BonusState.EDIBLE && game.collisionStrategy().collide(pac, bonus)) {
            result.edibleBonus = bonus;
        }

        final Vector2i pacTile = pac.computeTile();
        if (level.worldMap().foodLayer().hasFoodAtTile(pacTile)) {
            result.foodFoundTile = pacTile;
            result.energizerFound = level.worldMap().foodLayer().isEnergizerTile(pacTile);
        }
    }

    private static boolean evalCollisions(Result result, GameModel game, GameLevel level, Pac pac, List<Ghost> ghosts, Bonus bonus) {
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
                    .forEach(result.ghostsKilled::add);
                // More than one ghost might have been killed in this step
                result.ghostsKilled.forEach(ghost -> game.onEatGhost(level, ghost));
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

    private static void checkPacKilled(Result result, GameModel game, GameLevel level, Pac pac) {
        final boolean demoLevel = level.isDemoLevel();
        if (demoLevel && game.isPacSafeInDemoLevel(level) || !demoLevel && pac.isImmune()) {
            return;
        }
        result.pacKiller = result.ghostsCollidingWithPac.stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst()
            .orElse(null);
    }

    private static void checkFoodFound(Result result, GameModel game, GameLevel level, Pac pac) {
        if (!result.foodFound()) {
            pac.continueStarving();
        } else {
            level.worldMap().foodLayer().markFoodEatenAt(result.foodFoundTile);
            pac.endStarving();
            if (result.energizerFound()) {
                if (!game.rules().isLevelCompleted(level)) {
                    startPacPowerMode(result, game, level, pac);
                }
                game.eatEnergizer(level, result.foodFoundTile);
            } else {
                game.eatPellet(level, result.foodFoundTile);
            }
            if (game.rules().isBonusAwarded(level)) {
                game.activateNextBonus(level);
                result.setBonusIndex(level.currentBonusIndex());
            }
            game.flow().publishGameEvent(new PacEatsFoodEvent(game.flow().context(), pac, result.energizerFound(), false));
        }
    }

    private static void checkBonusFound(Result result, GameModel game, GameLevel level) {
        if (result.foundEdibleBonus()) {
            game.eatBonus(level, result.edibleBonus);
        }
    }

    private static void startPacPowerMode(Result result, GameModel game, GameLevel level, Pac pac) {
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

    private static void updatePacPowerMode(Result result, GameModel game, GameLevel level, Pac pac) {
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