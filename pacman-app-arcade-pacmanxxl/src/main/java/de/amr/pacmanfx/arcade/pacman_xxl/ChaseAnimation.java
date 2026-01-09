/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;
import org.tinylog.Logger;

import java.util.List;
import java.util.stream.IntStream;

import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Simple chasing animation used in XXL menu.
 */
public class ChaseAnimation {

    private static final float FPS = 60;
    private static final Duration FRAME_TIME = Duration.millis(1000.0 / FPS);

    private final Timeline timeline;
    private final FloatProperty scaling = new SimpleFloatProperty(1);
    private float offsetY;
    private Pac pac;
    private List<Ghost> ghosts;
    private ActorRenderer actorRenderer;
    private boolean ghostsChased;

    public ChaseAnimation() {
        timeline = new Timeline(new KeyFrame(FRAME_TIME, _ -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.statusProperty().addListener((_,_,newStatus) -> Logger.info("Chase animation {}", newStatus));
    }

    public FloatProperty scalingProperty() {
        return scaling;
    }

    public void setOffsetY(float y) {
        offsetY = y;
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    public void init(GameUI_Config uiConfig, Canvas canvas) {
        requireNonNull(uiConfig);

        pac = ArcadePacMan_GameModel.createPacMan();
        pac.setAnimationManager(uiConfig.createPacAnimations());

        ghosts = List.of(
            uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW),
            uiConfig.createGhostWithAnimations(PINK_GHOST_SPEEDY),
            uiConfig.createGhostWithAnimations(CYAN_GHOST_BASHFUL),
            uiConfig.createGhostWithAnimations(ORANGE_GHOST_POKEY)
        );

        pac.playAnimation(Pac.AnimationID.PAC_MUNCHING);
        ghosts.forEach(ghost -> ghost.playAnimation(Ghost.AnimationID.GHOST_NORMAL));

        actorRenderer = uiConfig.createActorRenderer(canvas);
        actorRenderer.scalingProperty().bind(scalingProperty());

        reset();
    }

    public void reset() {
        pac.setX(42 * TS);
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.setSpeed(1.0f);
        pac.setVisible(true);

        for (Ghost ghost : ghosts) {
            ghost.setX(46 * TS + ghost.personality() * 18);
            ghost.setMoveDir(Direction.LEFT);
            ghost.setWishDir(Direction.LEFT);
            ghost.setSpeed(1.05f);
            ghost.setVisible(true);
        }
        ghostsChased = false;
    }

    private void update() {
        if (ghosts.getLast().x() < -4 * TS && !ghostsChased) {
            ghostsChased = true;
            pac.setMoveDir(pac.moveDir().opposite());
            pac.setWishDir(pac.moveDir().opposite());
            pac.setX(-36 * TS);
            for (Ghost ghost : ghosts) {
                ghost.setVisible(true);
                ghost.setX(pac.x() + 22 * TS + ghost.personality() * 18);
                ghost.setMoveDir(ghost.moveDir().opposite());
                ghost.setWishDir(ghost.moveDir().opposite());
                ghost.setSpeed(0.58f);
                ghost.playAnimation(Ghost.AnimationID.GHOST_FRIGHTENED);
            }
        } else if (pac.x() > 56 * TS && ghostsChased) {
            ghostsChased = false;
            pac.setMoveDir(Direction.LEFT);
            pac.setWishDir(Direction.LEFT);
            pac.setX(42 * TS);
            for (Ghost ghost : ghosts) {
                ghost.setVisible(true);
                ghost.setMoveDir(Direction.LEFT);
                ghost.setWishDir(Direction.LEFT);
                ghost.setX(46 * TS + ghost.personality() * 2 * TS);
                ghost.setSpeed(1.05f);
                ghost.playAnimation(Ghost.AnimationID.GHOST_NORMAL);
            }
        } else if (ghostsChased) {
            IntStream.range(0, 4).forEach(i -> {
                if (Math.abs(pac.x() - ghosts.get(i).x()) < 1) {
                    ghosts.get(i).selectAnimationAt(Ghost.AnimationID.GHOST_POINTS, i);
                    if (i > 0) {
                        ghosts.get(i - 1).setVisible(false);
                    }
                }
            });
        }
        pac.move();
        for (Ghost ghost : ghosts) {
            ghost.move();
        }
    }

    public void draw() {
        if (actorRenderer != null) {
            final GraphicsContext ctx = actorRenderer.ctx();
            ctx.save();
            ctx.translate(0, actorRenderer.scaled(offsetY));
            actorRenderer.setImageSmoothing(true);
            ghosts.forEach(actorRenderer::drawActor);
            actorRenderer.drawActor(pac);
            ctx.restore();
        }
    }
}
