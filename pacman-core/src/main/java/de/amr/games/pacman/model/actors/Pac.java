/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Steering;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;

import java.util.Optional;

/**
 * Pac-Man / Ms. Pac-Man.
 *
 * @author Armin Reichert
 */
public class Pac extends Creature implements AnimationDirector {

    public static final String ANIM_MUNCHING = "munching";
    public static final String ANIM_DYING = "dying";
    /**
     * In Pac-Man cutscene, big Pac-Man appears.
     */
    public static final String ANIM_BIG_PACMAN = "big_pacman";
    /**
     * In Ms. Pac-Man cutscenes, also Ms. PacMan's husband appears.
     */
    public static final String ANIM_HUSBAND_MUNCHING = "husband_munching";

    public static final byte REST_INDEFINITE = -1;

    private final TickTimer powerTimer = new TickTimer("PacPower");
    private boolean dead;
    private byte restingTicks;
    private long starvingTicks;
    private int fadingTicks;
    private Steering steering;

    private Animations animations;

    public Pac(String name) {
        super(name);
        reset();
    }

    @Override
    public String toString() {
        return "Pac{" +
            "dead=" + dead +
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

    public void setAnimations(Animations animations) {
        this.animations = animations;
    }

    @Override
    public Optional<Animations> animations() {
        return Optional.ofNullable(animations);
    }

    @Override
    public boolean canReverse() {
        return isNewTileEntered();
    }

    @Override
    public boolean canAccessTile(Vector2i tile) {
        if (world.house().contains(tile)) {
            return false;
        }
        if (world.insideBounds(tile)) {
            return !world.isWall(tile);
        }
        return world.belongsToPortal(tile);
    }

    @Override
    public void reset() {
        super.reset();
        dead = false;
        restingTicks = 0;
        starvingTicks = 0;
        corneringSpeedUp = 1.5f; // TODO experimental
        powerTimer.reset(0);
        selectAnimation(ANIM_MUNCHING);
    }

    public void update(GameLevel level) {
        if (dead || restingTicks == REST_INDEFINITE) {
            return;
        }
        if (restingTicks == 0) {
            setPercentageSpeed(powerTimer.isRunning()
                ? level.data().pacSpeedPoweredPercentage()
                : level.data().pacSpeedPercentage());
            level.pacSteering().ifPresent(steering -> steering.steer(level));
            tryMoving();
            if (moved()) {
                startAnimation();
            } else {
                stopAnimation();
            }
        } else {
            --restingTicks;
        }
        powerTimer.advance();
    }

    public void kill() {
        stopAnimation();
        setSpeed(0);
        dead = true;
        starvingTicks = 0;
        restingTicks = 0;
        powerTimer.stop();
    }

    public boolean isDead() {
        return dead;
    }

    public void setFadingTicks(int fadingTicks) {
        this.fadingTicks = fadingTicks;
    }

    public boolean isPowerFading() {
        return powerTimer.isRunning() && powerTimer.remaining() <= fadingTicks;
    }

    public boolean isPowerStartingToFade() {
        return powerTimer.isRunning() && powerTimer.remaining() == fadingTicks
            || powerTimer().duration() < fadingTicks && powerTimer().tick() == 1;
    }

    public TickTimer powerTimer() {
        return powerTimer;
    }

    /* Number of ticks Pac is resting and not moving (1 after eating pellet, 3 after eating energizer). */
    public byte restingTicks() {
        return restingTicks;
    }

    public void setRestingTicks(byte ticks) {
        if (ticks == REST_INDEFINITE || ticks >= 0) {
            restingTicks = ticks;
        } else {
            throw new IllegalArgumentException("Resting time cannot be negative, but is: " + ticks);
        }
    }

    /* Number of ticks since Pac has eaten a pellet or energizer. */
    public long starvingTicks() {
        return starvingTicks;
    }

    public void starve() {
        ++starvingTicks;
    }

    public void endStarving() {
        starvingTicks = 0;
    }

    public boolean isStandingStill() {
        return velocity().length() == 0 || !moved() || restingTicks == REST_INDEFINITE;
    }

    public Optional<Steering> steering() {
        return Optional.ofNullable(steering);
    }

    public void setSteering(Steering steering) {
        this.steering = steering;
    }
}