/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.ui.game.GameVariantConfig;
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

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Simple chasing animation used in XXL menu.
 */
class ChaseAnimation {

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

    record Collision(Ghost ghost, long time) {}
    private final List<Collision> collisions = new ArrayList<>();

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

    public void init(GameVariantConfig gameVariant, Canvas canvas, SpriteAnimationContainer spriteAnimationContainer) {
        requireNonNull(gameVariant);
        requireNonNull(canvas);

        actorRenderer = gameVariant.createActorRenderer(canvas);
        actorRenderer.scalingProperty().bind(scalingProperty());

        pac = ArcadePacMan_ActorFactory.createPacMan();
        pac.setAnimations(gameVariant.createPacAnimations(spriteAnimationContainer));
        pac.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pac.animations().playSelected();
        pac.setX(numTilesX * WorldMap.TS);
        pac.setMoveDir(Direction.LEFT);
        pac.setWishDir(Direction.LEFT);
        pac.setSpeed(PAC_FLEEING_SPEED);
        pac.setVisible(true);

        ghosts = List.of(
            gameVariant.createAnimatedGhost(spriteAnimationContainer, GameModel.RED_GHOST_SHADOW),
            gameVariant.createAnimatedGhost(spriteAnimationContainer, GameModel.PINK_GHOST_SPEEDY),
            gameVariant.createAnimatedGhost(spriteAnimationContainer, GameModel.CYAN_GHOST_BASHFUL),
            gameVariant.createAnimatedGhost(spriteAnimationContainer, GameModel.ORANGE_GHOST_POKEY)
        );
        for (Ghost ghost : ghosts) {
            ghost.setX((numTilesX + 4) * WorldMap.TS + ghost.personality() * GHOST_DISTANCE);
            ghost.setMoveDir(Direction.LEFT);
            ghost.setWishDir(Direction.LEFT);
            ghost.setSpeed(GHOST_CHASE_SPEED);
            ghost.setVisible(true);
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.animations().playSelected();
        }

        collisions.clear();

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
        if (pac.x() > (numTilesX + 14) * WorldMap.TS) {
            pac.setMoveDir(Direction.LEFT);
            pac.setWishDir(Direction.LEFT);
            pac.setX(numTilesX * WorldMap.TS);
            for (Ghost ghost : ghosts) {
                ghost.setVisible(true);
                ghost.setMoveDir(Direction.LEFT);
                ghost.setWishDir(Direction.LEFT);
                ghost.setX((numTilesX + 4) * WorldMap.TS + ghost.personality() * 2 * WorldMap.TS);
                ghost.setSpeed(1.05f);
                ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
                ghost.animations().playSelected();
            }
            state = ChasingState.GHOSTS_CHASING_PAC;
        }
        else {
            final long now = System.currentTimeMillis();
            for (int i = collisions.size() -1; i >= 0; --i) { // backwards to avoid CCME
                final Collision collision = collisions.get(i);
                if (now - collision.time() >= 1000) {
                    collisions.remove(collision);
                    collision.ghost.hide();
                }
            }
            // Collision check
            for (int i = 0; i < 4; ++i) {
                final Ghost ghost = ghosts.get(i);
                if (colliding(pac, ghost) && collisions.stream().noneMatch(collision -> collision.ghost() == ghost)) {
                    final var collision = new Collision(ghost, System.currentTimeMillis());
                    collisions.add(collision);
                    ghost.animations().selectAndSetFrame(ArcadePacMan_AnimationID.GHOST_POINTS, i);
                    Logger.info("Collision: {}", collision);
                    break;
                }
            }
        }
    }

    private static boolean colliding(Actor either, Actor other) {
        return Math.abs(either.x() - other.x()) < 1;
    }

    private void ghostsChasePacMan() {
        moveActors();
        if (ghosts.getLast().x() < -4 * WorldMap.TS) { // ghosts left screen on the left side
            pac.setMoveDir(Direction.RIGHT);
            pac.setWishDir(Direction.RIGHT);
            pac.setX(-(numTilesX - 6) * WorldMap.TS);
            for (Ghost ghost : ghosts) {
                ghost.setVisible(true);
                ghost.setX(pac.x() + 22 * WorldMap.TS + ghost.personality() * GHOST_DISTANCE);
                ghost.setMoveDir(Direction.RIGHT);
                ghost.setWishDir(Direction.RIGHT);
                ghost.setSpeed(0.58f);
                ghost.animations().select(ArcadePacMan_AnimationID.GHOST_FRIGHTENED);
                ghost.animations().playSelected();
            }
            // Let Pac-Man chase the ghosts from left to right side of the screen
            state = ChasingState.PAC_CHASING_GHOSTS;
        }
    }

    public void draw() {
        if (actorRenderer != null) {
            final GraphicsContext ctx = actorRenderer.ctx();
            ctx.save();
            ctx.translate(0, scaling.get() * y);
            actorRenderer.setImageSmoothing(true);
            ghosts.forEach(actorRenderer::drawActor);
            actorRenderer.drawActor(pac);
            ctx.restore();
        }
    }
}
