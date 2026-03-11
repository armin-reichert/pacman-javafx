/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.d3.animation;

import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.uilib.animation.AnimationRegistry;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.MutableGhost3D;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
import static java.util.Objects.requireNonNull;

/**
 * Animation that periodically transfers a point light to the ghost currently
 * hunting Pac‑Man. The light follows the ghost’s position and adopts its color.
 * <p>
 * If no ghost is hunting, the light is turned off.
 */
public class GhostLightAnimation extends ManagedAnimation {

    private final List<MutableGhost3D> ghosts3D;
    private final PointLight light;
    private byte currentGhostID;

    public GhostLightAnimation(AnimationRegistry animationRegistry, List<MutableGhost3D> ghosts3D) {
        super(animationRegistry, "GhostLight");
        this.ghosts3D = requireNonNull(ghosts3D);

        currentGhostID = RED_GHOST_SHADOW;

        light = new PointLight();
        light.setColor(Color.WHITE);
        light.setMaxRange(30);
        light.lightOnProperty().addListener((_, _, on) ->
            Logger.info("Ghost light {}", on ? "ON" : "OFF"));

        setFactory(this::createAnimationFX);
    }

    public PointLight light() {
        return light;
    }

    private static byte nextGhostID(byte id) {
        return (byte) ((id + 1) % 4);
    }

    /**
     * Moves the spotlight to the given ghost and updates its color.
     */
    private void illuminateGhost(byte ghostID) {
        final MutableGhost3D ghost3D = ghosts3D.get(ghostID);
        light.setColor(ghost3D.colorSet().normal().dress());
        light.translateXProperty().bind(ghost3D.translateXProperty());
        light.translateYProperty().bind(ghost3D.translateYProperty());
        light.setTranslateZ(-25);
        light.setLightOn(true);
        currentGhostID = ghostID;
        Logger.debug("Ghost light passed to ghost {}", currentGhostID);
    }

    private Animation createAnimationFX() {
        var timeline = new Timeline(new KeyFrame(Duration.millis(3000), _ -> {
            Logger.debug("Try to pass light from ghost {} to next", currentGhostID);
            byte id = nextGhostID(currentGhostID);
            while (id != currentGhostID) {
                final Ghost ghost = ghosts3D.get(id).ghost();
                if (ghost.state() == GhostState.HUNTING_PAC) {
                    illuminateGhost(id);
                    return;
                }
                id = nextGhostID(id);
            }
            light.setLightOn(false);
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    @Override
    public void playFromStart() {
        illuminateGhost(RED_GHOST_SHADOW);
        super.playFromStart();
    }

    @Override
    public void stop() {
        light.setLightOn(false);
        super.stop();
    }
}
