/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.simulation;

import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.event.PacEatsFoodEvent;
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

public class SimulationStep {
    private long tick;
    private Vector2i foodFoundTile;
    private boolean energizerFound;
    private int bonusIndex = -1;
    private Bonus edibleBonus;
    private boolean pacGotPower;
    private boolean pacStartsLosingPower;
    private boolean pacLostPower;
    private Ghost pacKiller;
    private boolean extraLifeWon;
    private int extraLifeScore;
    private Ghost ghostReleasedFromJailhouse;
    private String ghostReleaseInfo;
    private final List<Ghost> ghostsKilled = new ArrayList<>();
    private final Set<Ghost> ghostsCollidingWithPac = new HashSet<>(4);

    public void init(long tick) {
        this.tick = tick;
        foodFoundTile = null;
        energizerFound = false;
        bonusIndex = -1;
        edibleBonus = null;
        pacGotPower = false;
        pacStartsLosingPower = false;
        pacLostPower = false;
        pacKiller = null;
        extraLifeWon = false;
        extraLifeScore = 0;
        ghostReleasedFromJailhouse = null;
        ghostReleaseInfo = null;
        ghostsKilled.clear();
        ghostsCollidingWithPac.clear();
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
        if (extraLifeWon) {
            messages.add("Extra life won for scoring %d points".formatted(extraLifeScore));
        }
        if (ghostReleasedFromJailhouse != null) {
            messages.add("%s unlocked: %s".formatted(ghostReleasedFromJailhouse.name(), ghostReleaseInfo));
        }
        for (Ghost ghost : ghostsKilled) {
            messages.add("%s killed at %s".formatted(ghost.name(), ghost.computeTile()));
        }
        return messages;
    }

    public void printLog() {
        var report = createReport();
        if (!report.isEmpty()) {
            Logger.info("Step #{}:", tick);
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

    public void setGhostReleasedFromJailhouse(Ghost ghostReleasedFromJailhouse) {
        this.ghostReleasedFromJailhouse = ghostReleasedFromJailhouse;
    }

    public void setGhostReleaseInfo(String ghostReleaseInfo) {
        this.ghostReleaseInfo = ghostReleaseInfo;
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

    public void setExtraLifeWon(boolean won) {
        extraLifeWon = won;
    }

    public void setExtraLifeScore(int extraLifeScore) {
        this.extraLifeScore = extraLifeScore;
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

    /**
     * Checks if Pac-Man gets killed by a collision with an attacking ghost.
     *
     * <p>In attract mode (demo level), there is a time interval at the beginning when Pac-Man is safe.
     * This is to avoid having Pac-Man getting killed too early in demo mode.
     * In contrast to the original Arcade games, the demo mode is not fixed but uses random ghost moves so it
     * cannot be predicted how long the demo mode runs.</p>
     *
     * <p>In normal mode, Pac-Man can be made immune against ghost attacks using a cheat command.
     * In this case, Pac-Man is safe against ghost attacks too.</p>
     *
     * @param level the game level
     * @param pac   the Pac
     */
    public void checkPacKilled(GameModel game, GameLevel level, Pac pac) {
        final boolean demoLevel = level.isDemoLevel();
        if (demoLevel && game.isPacSafeInDemoLevel(level) || !demoLevel && pac.isImmune()) {
            return;
        }
        pacKiller = ghostsCollidingWithPac.stream()
            .filter(ghost -> ghost.state() == GhostState.HUNTING_PAC)
            .findFirst()
            .orElse(null);
    }

    /* -------------------------------------------------------------------------
     * Main simulation step
     * ---------------------------------------------------------------------- */

    public void doHuntingStep(GameModel game, GameLevel level) {
        level.heartbeat().triggerPulse();

        final Pac pac = level.entities().pac();
        final List<Ghost> ghosts = level.entities().ghosts();
        final Bonus bonus = level.entities().optBonus().orElse(null);

        //TODO rework collision handling

        boolean quitHunting;
        if (game.isCollisionDoubleChecked()) {
            quitHunting = evalCollisions(game, level, pac, ghosts, bonus);
            if (!quitHunting) {
                level.entities().forEach(e -> e.update(level));
                quitHunting = evalCollisions(game, level, pac, ghosts, bonus);
            }
        } else {
            level.entities().forEach(e -> e.update(level));
            quitHunting = evalCollisions(game, level, pac, ghosts, bonus);
        }

        if (quitHunting) {
            Logger.info("Hunting has been stopped!");
            return;
        }

        checkFoodFound(game, level, pac);
        checkBonusFound(game, level);

        if (!game.rules().isLevelCompleted(level)) {
            game.updatePacPowerMode(level, pac);
            level.huntingTimer().update(game.rules(), level.number());
        }
    }

    // Collision behavior is controlled by the current collision strategy.
    // The original Arcade games use tile-based collision which can lead to missed collisions
    // by passing through!

    public void detectCollisions(GameModel game, GameLevel level, Pac pac, List<Ghost> ghosts, Bonus bonus) {
        // Ghosts colliding with Pac?
        ghostsCollidingWithPac.clear();
        ghosts.stream().filter(ghost -> game.collisionStrategy().collide(pac, ghost))
            .forEach(ghostsCollidingWithPac::add);

        edibleBonus = null;
        if (bonus != null && bonus.state() == BonusState.EDIBLE && game.collisionStrategy().collide(pac, bonus)) {
            edibleBonus = bonus;
        }

        final Vector2i pacTile = pac.computeTile();
        if (level.worldMap().foodLayer().hasFoodAtTile(pacTile)) {
            foodFoundTile = pacTile;
            energizerFound = level.worldMap().foodLayer().isEnergizerTile(pacTile);
        }
    }

    private boolean evalCollisions(GameModel game, GameLevel level, Pac pac, List<Ghost> ghosts, Bonus bonus) {
        detectCollisions(game, level, pac, ghosts, bonus);
        if (detectedPacGhostCollision()) {
            // Is Pac getting killed after the collision with a ghost?
            // He might stay alive if immune or in level's safe phase!
            checkPacKilled(game, level, pac);
            if (hasPacManBeenKilled()) {
                return true;
            }
            else {
                // Frightened ghosts get killed when colliding with Pac
                ghostsCollidingWithPac().stream()
                    .filter(ghost -> ghost.state() == GhostState.FRIGHTENED)
                    .forEach(ghostsKilled::add);
                // More than one ghost might have been killed in this step
                ghostsKilled.forEach(ghost -> game.onEatGhost(level, ghost));
                if (hasGhostBeenKilled()) {
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

    private void checkFoodFound(GameModel game, GameLevel level, Pac pac) {
        if (!foodFound()) {
            pac.continueStarving();
        } else {
            level.worldMap().foodLayer().markFoodEatenAt(foodFoundTile);
            pac.endStarving();
            if (energizerFound()) {
                game.eatEnergizer(level, foodFoundTile);
            } else {
                game.eatPellet(level, foodFoundTile);
            }
            if (game.rules().isBonusAwarded(level)) {
                game.activateNextBonus(level);
                setBonusIndex(level.currentBonusIndex());
            }
            game.flow().publishGameEvent(new PacEatsFoodEvent(game, pac, energizerFound(), false));
        }
    }

    private void checkBonusFound(GameModel game, GameLevel level) {
        if (foundEdibleBonus()) {
            game.eatBonus(level, edibleBonus);
        }
    }
}