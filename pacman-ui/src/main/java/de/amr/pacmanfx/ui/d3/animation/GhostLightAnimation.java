/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.animation;

import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.actor.GhostAppearance3D;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.PointLight;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static java.util.Objects.requireNonNull;

/**
 * Animation that periodically transfers a point light between the ghosts hunting Pac‑Man.
 * The light follows the ghost’s position and adopts its color.
 * <p>
 * If no ghost is hunting, the light is turned off.
 */
public class GhostLightAnimation extends ManagedAnimation {

    public static final float LIGHT_HEIGHT_OVER_FLOOR = 25.0f;
    public static final int LIGHT_MAX_RANGE = 30;
    public static final Duration LIGHT_CHANGE_INTERVAL = Duration.millis(3000);

    private final List<GhostAppearance3D> ghosts3D;
    private final PointLight light = new PointLight();
    private byte currentGhostID = RED_GHOST_SHADOW;

    public GhostLightAnimation(AnimationRegistry animationRegistry, List<GhostAppearance3D> ghosts3D) {
        super(animationRegistry, "GhostLight");
        this.ghosts3D = requireNonNull(ghosts3D);
        light.setMaxRange(LIGHT_MAX_RANGE);
        setFactory(this::createAnimationFX);
    }

    @Override
    public void playFromStart() {
        illuminateGhost(RED_GHOST_SHADOW);
        super.playFromStart();
    }

    public PointLight light() {
        return light;
    }

    /**
     * Moves the spotlight to the given ghost and updates its color.
     */
    private void illuminateGhost(byte ghostID) {
        final GhostAppearance3D g3D = ghosts3D.get(ghostID);
        light.setColor(g3D.ghost3D().colorSet().normal().dress());
        light.translateXProperty().bind(g3D.translateXProperty());
        light.translateYProperty().bind(g3D.translateYProperty());
        light.setTranslateZ(-LIGHT_HEIGHT_OVER_FLOOR);
        light.setLightOn(true);
        currentGhostID = ghostID;
        Logger.info("Ghost light passed to ghost {}", currentGhostID);
    }

    private Animation createAnimationFX() {
        final var loop = new Timeline(
            new KeyFrame(LIGHT_CHANGE_INTERVAL, _ -> passGhostLightToNextHunter())
        );
        loop.setCycleCount(Animation.INDEFINITE);
        loop.statusProperty().addListener((_, _, status) -> {
            if (status == Animation.Status.STOPPED) {
                turnLightOff();
            }
        });
        return loop;
    }

    private void turnLightOff() {
        light.setLightOn(false);
    }

    private void passGhostLightToNextHunter() {
        final byte nextID = findNextHunter();
        if (nextID != -1) {
            illuminateGhost(nextID);
        } else {
            turnLightOff();
        }
    }

    private byte findNextHunter() {
        byte id = nextGhostID(currentGhostID);
        while (id != currentGhostID) {
            if (ghosts3D.get(id).ghost().state() == GhostState.HUNTING_PAC) {
                return id;
            }
            id = nextGhostID(id);
        }
        return -1;
    }

    private byte nextGhostID(int id) {
        return (byte) ((id + 1) % ghosts3D.size());
    }
}
