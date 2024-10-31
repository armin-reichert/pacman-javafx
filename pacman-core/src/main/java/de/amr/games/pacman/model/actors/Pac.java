/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.steering.Steering;

/**
 * Pac-Man / Ms. Pac-Man.
 *
 * @author Armin Reichert
 */
public class Pac extends Creature implements AnimatedEntity {

    public static final byte REST_INDEFINITELY = -1;

    private String name;
    private boolean dead;
    private int restingTicks;
    private long starvingTicks;
    private Steering autopilot;
    private boolean usingAutopilot;
    private Animations animations;
    private boolean immune;

    public void setName(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return "Pac{" +
            "immune=" + immune +
            ", dead=" + dead +
            ", restingTicks=" + restingTicks +
            ", starvingTicks=" + starvingTicks +
            ", visible=" + visible +
            ", pos_x=" + posX +
            ", pos_y=" + posY +
            ", vel_x=" + velX +
            ", vel_y=" + velY +
            ", acc_x=" + accX +
            ", acc_y=" + accY +
            '}';
    }

    @Override
    public void setAnimations(Animations animations) {
        this.animations = animations;
    }

    @Override
    public Animations animationSet() {
        return animations;
    }

    @Override
    public boolean canReverse() {
        return newTileEntered;
    }

    @Override
    public boolean canAccessTile(Vector2i tile) {
        if (world.isPartOfHouse(tile)) {
            return false;
        }
        if (world.isInsideWorld(tile)) {
            return !world.isBlockedTile(tile);
        }
        return world.isPortalAt(tile);
    }

    @Override
    public void reset() {
        super.reset();
        dead = false;
        restingTicks = 0;
        starvingTicks = 0;
        corneringSpeedUp = 1.5f; // no real cornering implementation but better than nothing
        if (animations != null) {
            animations.select(GameModel.ANIM_PAC_MUNCHING, 0);
        }
    }

    public boolean isImmune() {
        return immune;
    }

    public void setImmune(boolean immune) {
        this.immune = immune;
    }

    public void update(GameModel game) {
        if (dead || restingTicks == REST_INDEFINITELY) {
            return;
        }
        if (restingTicks == 0) {
            if (usingAutopilot) {
                autopilot.steer(this, game.world());
            }
            setSpeed(game.powerTimer().isRunning() ? game.pacPowerSpeed() : game.pacNormalSpeed());
            tryMoving();
            //Logger.info(moveInfo);
            if (moveInfo.moved) {
                animations.startCurrentAnimation();
            } else {
                animations.stopCurrentAnimation();
            }
        } else {
            --restingTicks;
        }
    }

    /**
     * When a level is complete, Pac-Man is displayed in its full beauty and does not move anymore.
     */
    public void freeze() {
        setSpeed(0);
        setRestingTicks(Pac.REST_INDEFINITELY);
        animations.stopCurrentAnimation();
        selectAnimation(GameModel.ANIM_PAC_MUNCHING);
        animations.resetCurrentAnimation();
    }

    public void die() {
        setSpeed(0);
        animations.stopCurrentAnimation();
        dead = true;
    }

    public boolean isAlive() {
        return !dead;
    }

    public void setRestingTicks(int ticks) {
        if (ticks == REST_INDEFINITELY || ticks >= 0) {
            restingTicks = ticks;
        } else {
            throw new IllegalArgumentException("Resting time cannot be negative, but is: " + ticks);
        }
    }

    /**
     *  @return number of ticks passed since Pac has eaten a pellet or energizer.
     */
    public long starvingTicks() {
        return starvingTicks;
    }

    public void starve() {
        ++starvingTicks;
    }

    public void endStarving() {
        starvingTicks = 0;
    }

    /**
     * @return {@code true} if Pac-Man has run against a wall and could not move, its speed is zero
     * or if he is resting for an indefinite time.
     */
    public boolean isStandingStill() {
        return velocity().length() == 0 || !moveInfo.moved || restingTicks == REST_INDEFINITELY;
    }

    public void setAutopilot(Steering steering) {
        autopilot = steering;
    }

    public boolean isUsingAutopilot() {
        return usingAutopilot;
    }

    public void setUsingAutopilot(boolean value) {
        usingAutopilot = value;
    }
}