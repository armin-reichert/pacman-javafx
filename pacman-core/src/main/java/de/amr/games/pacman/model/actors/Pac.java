/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.lib.Steering;
import de.amr.games.pacman.lib.TickTimer;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.model.actors.CreatureMovement.tryMoving;

/**
 * Pac-Man / Ms. Pac-Man.
 *
 * @author Armin Reichert
 */
public class Pac extends Creature {

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

    private final String name;
    private final TickTimer powerTimer = new TickTimer("PacPower");
    private boolean dead;
    private byte restingTicks;
    private long starvingTicks;
    private int powerFadingTicks;

    private Steering manualSteering;
    private Steering autopilot;
    private boolean useAutopilot;

    private final List<Ghost> victims = new ArrayList<>();

    private Animations animations;

    public Pac(String name) {
        checkNotNull(name);
        this.name = name;
    }

    public String name() {
        return name;
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

    public Optional<Animations> animations() {
        return Optional.ofNullable(animations);
    }

    public void selectAnimation(String name) {
        selectAnimation(name, 0);
    }

    public void selectAnimation(String name, int index) {
        if (animations != null) {
            animations.select(name, index);
        }
    }

    public void startAnimation() {
        if (animations != null) {
            animations.startSelected();
        }
    }

    public void stopAnimation() {
        if (animations != null) {
            animations.stopSelected();
        }
    }

    public void resetAnimation() {
        if (animations != null) {
            animations.resetSelected();
        }
    }

    @Override
    public boolean canReverse() {
        return newTileEntered;
    }

    @Override
    public boolean canAccessTile(Vector2i tile, World world) {
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

    public void update(GameModel game) {
        if (game.level().isEmpty()) {
            return;
        }
        GameLevel level = game.level().get();
        if (dead || restingTicks == REST_INDEFINITE) {
            return;
        }
        if (restingTicks == 0) {
            setSpeedPct(powerTimer.isRunning()
                ? level.pacSpeedPoweredPercentage()
                : level.pacSpeedPercentage());
            if (useAutopilot) {
                autopilot.steer(this, game.world());
            } else {
                manualSteering.steer(this, game.world());
            }
            tryMoving(this, game.world());
            if (moveResult.moved) {
                startAnimation();
            } else {
                stopAnimation();
            }
        } else {
            --restingTicks;
        }
        powerTimer.advance();
        if (powerTimer.hasExpired()) {
            victims.clear();
        }
    }

    public List<Ghost> victims() {
        return victims;
    }

    /**
     * When a level is complete, Pac-Man is displayed in its full beauty and does not move anymore.
     */
    public void freeze() {
        setSpeed(0);
        setRestingTicks(Pac.REST_INDEFINITE);
        stopAnimation();
        selectAnimation(Pac.ANIM_MUNCHING);
        resetAnimation();
    }

    public void die() {
        powerTimer.stop(); // necessary?
        setSpeed(0);
        stopAnimation();
        dead = true;
    }

    public boolean isDead() {
        return dead;
    }

    public void setPowerFadingTicks(int ticks) {
        powerFadingTicks = ticks;
    }

    public boolean isPowerFading() {
        return powerTimer.isRunning() && powerTimer.remaining() <= powerFadingTicks;
    }

    public boolean isPowerFadingStarting() {
        return powerTimer.isRunning() && powerTimer.remaining() == powerFadingTicks
            || powerTimer().duration() < powerFadingTicks && powerTimer().tick() == 1;
    }

    public TickTimer powerTimer() {
        return powerTimer;
    }

    public void setRestingTicks(byte ticks) {
        if (ticks == REST_INDEFINITE || ticks >= 0) {
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
        return velocity().length() == 0 || !moveResult.moved || restingTicks == REST_INDEFINITE;
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