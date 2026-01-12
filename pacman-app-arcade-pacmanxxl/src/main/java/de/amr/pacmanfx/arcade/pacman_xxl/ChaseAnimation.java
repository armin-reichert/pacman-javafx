/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI_Config;
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

import static de.amr.pacmanfx.Globals.*;
import static java.util.Objects.requireNonNull;

/**
 * Simple chasing animation used in XXL menu.
 */
public class ChaseAnimation {

    enum ChasingState {GHOSTS_CHASING_PAC, PAC_CHASING_GHOSTS}

    private static final float FPS = 60;
    private static final Duration FRAME_TIME = Duration.millis(1000.0 / FPS);

    private static final int GHOST_DISTANCE = 18;
    private static final float PAC_FLEEING_SPEED = 1.0f;
    private static final float GHOST_CHASE_SPEED = 1.05f;

    private final int numTilesX;
    private final Timeline timeline;
    private final FloatProperty scaling = new SimpleFloatProperty(1);
    private float y;
    private Pac pac;
    private List<Ghost> ghosts;
    private ActorRenderer actorRenderer;
    private ChasingState state;

    public ChaseAnimation(int numTilesX) {
        this.numTilesX = numTilesX;
        timeline = new Timeline(new KeyFrame(FRAME_TIME, _ -> update()));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.statusProperty().addListener((_,_,newStatus) -> Logger.info("Chase animation {}", newStatus));
    }

    public FloatProperty scalingProperty() {
        return scaling;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    public void init(GameUI_Config uiConfig, Canvas canvas) {
        requireNonNull(uiConfig);

        actorRenderer = uiConfig.createActorRenderer(canvas);
        actorRenderer.scalingProperty().bind(scalingProperty());

        pac = ArcadePacMan_GameModel.createPacMan();
        pac.setAnimationManager(uiConfig.createPacAnimations());
        pac.playAnimation(Pac.AnimationID.PAC_MUNCHING);
        pac.setX(numTilesX * TS);
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.setSpeed(PAC_FLEEING_SPEED);
        pac.setVisible(true);

        ghosts = List.of(
            uiConfig.createGhostWithAnimations(RED_GHOST_SHADOW),
            uiConfig.createGhostWithAnimations(PINK_GHOST_SPEEDY),
            uiConfig.createGhostWithAnimations(CYAN_GHOST_BASHFUL),
            uiConfig.createGhostWithAnimations(ORANGE_GHOST_POKEY)
        );
        for (Ghost ghost : ghosts) {
            ghost.setX((numTilesX + 4) * TS + ghost.personality() * GHOST_DISTANCE);
            ghost.setMoveDir(Direction.LEFT);
            ghost.setWishDir(Direction.LEFT);
            ghost.setSpeed(GHOST_CHASE_SPEED);
            ghost.setVisible(true);
            ghost.playAnimation(Ghost.AnimationID.GHOST_NORMAL);
        }

        state = ChasingState.GHOSTS_CHASING_PAC;
    }

    private void update() {
        switch (state) {
            case GHOSTS_CHASING_PAC -> ghostsChasePacMan();
            case PAC_CHASING_GHOSTS -> pacManChasesGhosts();
        }
    }

    private void moveActors() {
        pac.move();
        for (Ghost ghost : ghosts) {
            ghost.move();
        }
    }

    private void pacManChasesGhosts() {
        moveActors();
        // If ghosts and Pac leave screen at right border, ghosts start chasing Pac moving left
        if (pac.x() > (numTilesX + 14) * TS) {
            pac.setMoveDir(Direction.LEFT);
            pac.setWishDir(Direction.LEFT);
            pac.setX(numTilesX * TS);
            for (Ghost ghost : ghosts) {
                ghost.setVisible(true);
                ghost.setMoveDir(Direction.LEFT);
                ghost.setWishDir(Direction.LEFT);
                ghost.setX((numTilesX + 4) * TS + ghost.personality() * 2 * TS);
                ghost.setSpeed(1.05f);
                ghost.playAnimation(Ghost.AnimationID.GHOST_NORMAL);
            }
            state = ChasingState.GHOSTS_CHASING_PAC;
        }
        else { // continue chasing ghosts moving right
            // ghost eaten?
            int eatenGhostIndex = -1;
            for (int i = 0; i < 4; ++i) {
                final Ghost ghost = ghosts.get(i);
                if (Math.abs(pac.x() - ghost.x()) < 4) {
                    eatenGhostIndex = i;
                    break;
                }
            }
            if (eatenGhostIndex != -1) {
                ghosts.get(eatenGhostIndex).selectAnimationAt(Ghost.AnimationID.GHOST_POINTS, eatenGhostIndex);
                if (eatenGhostIndex > 0) {
                    ghosts.get(eatenGhostIndex - 1).setVisible(false);
                }
            }

        }
    }

    private void ghostsChasePacMan() {
        moveActors();
        if (ghosts.getLast().x() < -4 * TS) { // ghosts left screen on the left side
            pac.setMoveDir(Direction.RIGHT);
            pac.setWishDir(Direction.RIGHT);
            pac.setX(-(numTilesX - 6) * TS);
            for (Ghost ghost : ghosts) {
                ghost.setVisible(true);
                ghost.setX(pac.x() + 22 * TS + ghost.personality() * GHOST_DISTANCE);
                ghost.setMoveDir(Direction.RIGHT);
                ghost.setWishDir(Direction.RIGHT);
                ghost.setSpeed(0.58f);
                ghost.playAnimation(Ghost.AnimationID.GHOST_FRIGHTENED);
            }
            // Let Pac-Man chase the ghosts from left to right side of the screen
            state = ChasingState.PAC_CHASING_GHOSTS;
        }
    }

    public void draw() {
        if (actorRenderer != null) {
            final GraphicsContext ctx = actorRenderer.ctx();
            ctx.save();
            ctx.translate(0, scaling.get() * y);;
            actorRenderer.setImageSmoothing(true);
            ghosts.forEach(actorRenderer::drawActor);
            actorRenderer.drawActor(pac);
            ctx.restore();
        }
    }
}
