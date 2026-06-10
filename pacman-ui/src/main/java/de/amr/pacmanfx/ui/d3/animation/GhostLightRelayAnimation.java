/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.ui.d3.animation;

import de.amr.pacmanfx.model.actors.GhostState;
import de.amr.pacmanfx.uilib.animation.ManagedAnimation;
import de.amr.pacmanfx.uilib.model3D.ghost.Ghost3D;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.Optional;

import static de.amr.pacmanfx.model.GameModel.RED_GHOST_SHADOW;
import static java.util.Objects.requireNonNull;

/**
 * Animation that periodically transfers a point light between the ghosts hunting Pac‑Man.
 * The light follows the ghost’s position and adopts its color.
 * <p>
 * If no ghost is hunting, the light is turned off.
 * <p>The reason for this strategy instead of just giving each ghost its own light is that JavaFX can only
 * have 4(?) point lights per scene.</p>
 */
public class GhostLightRelayAnimation extends ManagedAnimation {

    public static final float LIGHT_HEIGHT_OVER_FLOOR = 25.0f;
    public static final int LIGHT_MAX_RANGE = 30;
    public static final Duration LIGHT_CHANGE_INTERVAL = Duration.millis(3000);

    private final PointLight light;
    private final List<Ghost3D> ghosts3D;
    private byte currentGhostID = RED_GHOST_SHADOW;

    public GhostLightRelayAnimation(PointLight light, List<Ghost3D> ghosts3DInOrder) {
        super("Ghost Light Animation");

        this.light = requireNonNull(light);
        this.ghosts3D = requireNonNull(ghosts3DInOrder);

        setFactory(() -> {
            final var timeline = new Timeline(
                new KeyFrame(LIGHT_CHANGE_INTERVAL, _ -> passGhostLightToNextHunter()));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.statusProperty().addListener((_, _, status) -> {
                switch (status) {
                    case STOPPED -> {
                        light.setLightOn(false);
                        currentGhostID = RED_GHOST_SHADOW;
                    }
                    case PAUSED -> {}
                    case RUNNING -> illuminateGhost(currentGhostID);
                }
            });
            return timeline;
        });
        light.setMaxRange(LIGHT_MAX_RANGE);
    }

    private void illuminateGhost(byte personality) {
        final Ghost3D ghost3D = ghosts3D.get(personality);
        final Color lightColor = ghosts3D.get(personality).config().colors().normalColors().dressColor();
        light.setColor(lightColor);
        light.translateXProperty().bind(ghost3D.translateXProperty());
        light.translateYProperty().bind(ghost3D.translateYProperty());
        light.setTranslateZ(-LIGHT_HEIGHT_OVER_FLOOR);
        light.setLightOn(true);
        currentGhostID = personality;
        Logger.trace("Ghost light passed to ghost {}", currentGhostID);
    }

    private void passGhostLightToNextHunter() {
        findNextHunter().ifPresentOrElse(this::illuminateGhost, () -> light.setLightOn(false));
    }

    private Optional<Byte> findNextHunter() {
        byte next = nextGhostPersonality(currentGhostID);
        while (next != currentGhostID) {
            if (ghosts3D.get(next).ghost().state() == GhostState.HUNTING_PAC) {
                return Optional.of(next);
            }
            next = nextGhostPersonality(next);
        }
        return Optional.empty();
    }

    private byte nextGhostPersonality(int personality) {
        return (byte) ((personality + 1) % ghosts3D.size());
    }
}
