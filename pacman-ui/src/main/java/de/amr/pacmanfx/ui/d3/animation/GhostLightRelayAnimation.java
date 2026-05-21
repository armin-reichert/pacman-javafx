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

import static de.amr.pacmanfx.Globals.RED_GHOST_SHADOW;
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
            final var timeline = new Timeline(new KeyFrame(LIGHT_CHANGE_INTERVAL, _ -> passGhostLightToNextHunter()));
            timeline.setCycleCount(Animation.INDEFINITE);
            timeline.statusProperty().addListener((_, _, status) -> {
                if (status == Animation.Status.STOPPED) {
                    light.setLightOn(false);
                }
            });
            return timeline;
        });
        light.setMaxRange(LIGHT_MAX_RANGE);
    }

    @Override
    public void playFromStart() {
        illuminateGhost(RED_GHOST_SHADOW, lightColor(RED_GHOST_SHADOW));
        super.playFromStart();
    }

    /**
     * Moves the spotlight to the given ghost and updates its color.
     */
    private void illuminateGhost(byte ghostID, Color color) {
        final Ghost3D ghost3D = ghosts3D.get(ghostID);
        light.setColor(color);
        light.translateXProperty().bind(ghost3D.translateXProperty());
        light.translateYProperty().bind(ghost3D.translateYProperty());
        light.setTranslateZ(-LIGHT_HEIGHT_OVER_FLOOR);
        light.setLightOn(true);
        currentGhostID = ghostID;
        Logger.info("Ghost light passed to ghost {}", currentGhostID);
    }

    private void passGhostLightToNextHunter() {
        final byte nextID = findNextHunter();
        if (nextID != -1) {
            illuminateGhost(nextID, lightColor(nextID));
        } else {
            light.setLightOn(false);
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

    private Color lightColor(byte id) {
        return ghosts3D.get(id).config().colors().normalColors().dressColor();
    }
}
