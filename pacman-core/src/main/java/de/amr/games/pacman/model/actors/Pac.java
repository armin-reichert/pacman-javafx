/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.steering.Steering;

/**
 * Pac-Man / Ms. Pac-Man.
 *
 * @author Armin Reichert
 */
public class Pac extends Creature implements AnimatedEntity {

    public static final String ANIM_MUNCHING = "munching";
    public static final String ANIM_DYING = "dying";
    /** In Pac-Man cutscene, a big Pac-Man appears. */
    public static final String ANIM_BIG_PACMAN = "big_pacman";
    /** In Ms. Pac-Man cutscenes, also Ms. PacMan's husband appears. */
    public static final String ANIM_HUSBAND_MUNCHING = "husband_munching";

    public static final byte REST_INDEFINITELY = -1;

    private String name;
    private boolean dead;
    private byte restingTicks;
    private long starvingTicks;
    private Steering manualSteering;
    private Steering autopilot;
    private boolean useAutopilot;
    private Animations animations;
    private boolean immune;

    public Pac() {
        this(null);
    }

    public Pac(GameWorld world) {
        super(world);
    }

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
            animations.select(ANIM_MUNCHING, 0);
        }
    }

    public boolean isImmune() {
        return immune;
    }

    public void setImmune(boolean immune) {
        this.immune = immune;
    }

    public void update(GameModel game) {
        if (game.level().isEmpty()) {
            return;
        }
        GameLevel level = game.level().get();
        if (dead || restingTicks == REST_INDEFINITELY) {
            return;
        }
        if (restingTicks == 0) {
            if (useAutopilot) {
                autopilot.steer(this, game.world());
            } else {
                manualSteering.steer(this, game.world());
            }
            setSpeedPct(game.powerTimer().isRunning() ? level.pacSpeedPoweredPercentage() : level.pacSpeedPercentage());
            tryMoving();
            if (moveInfo.moved) {
                animations.startSelected();
            } else {
                animations.stopSelected();
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
        animations.stopSelected();
        selectAnimation(Pac.ANIM_MUNCHING);
        animations.resetSelected();
    }

    public void die() {
        setSpeed(0);
        animations.stopSelected();
        dead = true;
    }

    public boolean isAlive() {
        return !dead;
    }

    public void setRestingTicks(byte ticks) {
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

    public void onStarvingEnd() {
        starvingTicks = 0;
    }

    /**
     * @return {@code true} if Pac-Man has run against a wall and could not move, its speed is zero
     * or if he is resting for an indefinite time.
     */
    public boolean isStandingStill() {
        return velocity().length() == 0 || !moveInfo.moved || restingTicks == REST_INDEFINITELY;
    }

    public void setManualSteering(Steering steering) {
        manualSteering = steering;
    }

    public void setAutopilot(Steering steering) {
        autopilot = steering;
    }

    public void setUseAutopilot(boolean value) {
        useAutopilot = value;
    }
}