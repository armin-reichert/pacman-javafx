/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.systems.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
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
class XXL_ChaseAnimation {

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

    public XXL_ChaseAnimation(int numTilesX) {
        this.numTilesX = numTilesX;
        timeline = new Timeline();
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.statusProperty().addListener((_,_,newStatus) -> Logger.debug("Chase animation {}", newStatus));
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

    public void init(GameContext gameContext, GameVariantRenderConfig renderConfig, Canvas canvas, SpriteAnimationContainer container) {
        requireNonNull(gameContext);
        requireNonNull(renderConfig);
        requireNonNull(canvas);
        requireNonNull(container);

        final WorldMovementSystem worldMovementSystem = gameContext.systems().worldMovementSystem;

        timeline.getKeyFrames().setAll(new KeyFrame(FRAME_TIME, _ -> update(worldMovementSystem)));

        actorRenderer = renderConfig.createActorRenderer(canvas);
        actorRenderer.scalingProperty().bind(scalingProperty());

        pac = ArcadePacMan_ActorFactory.createPacMan();
        pac.animations = renderConfig.createPacAnimations(container);
        pac.animations.select(CommonAnimationID.PAC_MUNCHING);
        pac.animations.playSelected();
        pac.position().setX(numTilesX * WorldMap.TS);
        worldMovementSystem.setMoveDir(pac, Direction.LEFT);
        worldMovementSystem.setWishDir(pac, Direction.LEFT);
        worldMovementSystem.setSpeed(pac, PAC_FLEEING_SPEED);
        pac.visibility().show();

        ghosts = List.of(
            renderConfig.createAnimatedGhost(container, GameModel.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(container, GameModel.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(container, GameModel.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(container, GameModel.ORANGE_GHOST_POKEY)
        );
        for (Ghost ghost : ghosts) {
            ghost.position().setX((numTilesX + 4) * WorldMap.TS + ghost.personality() * GHOST_DISTANCE);
            ghost.setMoveDir(Direction.LEFT);
            ghost.setWishDir(Direction.LEFT);
            GameContext.SYSTEMS.worldMovementSystem.setSpeed(ghost, GHOST_CHASE_SPEED);
            ghost.visibility().show();
            ghost.animations.select(CommonAnimationID.GHOST_NORMAL);
            ghost.animations.playSelected();
        }

        collisions.clear();

        state = ChasingState.GHOSTS_CHASING_PAC;
    }

    private void update(WorldMovementSystem worldMovementSystem) {
        switch (state) {
            case GHOSTS_CHASING_PAC -> ghostsChasePacMan(worldMovementSystem);
            case PAC_CHASING_GHOSTS -> pacManChasesGhosts(worldMovementSystem);
        }
    }

    private void moveActors() {
        GameContext.SYSTEMS.movementSystem.moveAccelerated(pac);
        for (Ghost ghost : ghosts) {
            GameContext.SYSTEMS.movementSystem.moveAccelerated(ghost);
        }
    }

    private void pacManChasesGhosts(WorldMovementSystem worldMovementSystem) {
        moveActors();
        // If ghosts and Pac leave screen at right border, ghosts start chasing Pac moving left
        if (pac.position().x > (numTilesX + 14) * WorldMap.TS) {
            worldMovementSystem.setMoveDir(pac, Direction.LEFT);
            worldMovementSystem.setWishDir(pac, Direction.LEFT);
            pac.position().setX(numTilesX * WorldMap.TS);
            for (Ghost ghost : ghosts) {
                ghost.visibility().show();
                ghost.setMoveDir(Direction.LEFT);
                ghost.setWishDir(Direction.LEFT);
                ghost.position().setX((numTilesX + 4) * WorldMap.TS + ghost.personality() * 2 * WorldMap.TS);
                GameContext.SYSTEMS.worldMovementSystem.setSpeed(ghost, 1.05f);
                ghost.animations.select(CommonAnimationID.GHOST_NORMAL);
                ghost.animations.playSelected();
            }
            state = ChasingState.GHOSTS_CHASING_PAC;
        }
        else {
            final long now = System.currentTimeMillis();
            for (int i = collisions.size() -1; i >= 0; --i) { // backwards to avoid CCME
                final Collision collision = collisions.get(i);
                if (now - collision.time() >= 1000) {
                    collisions.remove(collision);
                    collision.ghost.visibility().hide();
                }
            }
            // Collision check
            for (int i = 0; i < 4; ++i) {
                final Ghost ghost = ghosts.get(i);
                if (colliding(pac, ghost) && collisions.stream().noneMatch(collision -> collision.ghost() == ghost)) {
                    final var collision = new Collision(ghost, System.currentTimeMillis());
                    collisions.add(collision);
                    ghost.animations.selectAndSetFrame(CommonAnimationID.GHOST_POINTS, i);
                    Logger.debug("Collision: {}", collision);
                    break;
                }
            }
        }
    }

    private static boolean colliding(Actor either, Actor other) {
        return Math.abs(either.position().x - other.position().x) < 1;
    }

    private void ghostsChasePacMan(WorldMovementSystem worldMovementSystem) {
        moveActors();
        if (ghosts.getLast().position().x < -4 * WorldMap.TS) { // ghosts left screen on the left side
            worldMovementSystem.setMoveDir(pac, Direction.RIGHT);
            worldMovementSystem.setWishDir(pac, Direction.RIGHT);
            pac.position().setX(-(numTilesX - 6) * WorldMap.TS);
            for (Ghost ghost : ghosts) {
                ghost.visibility().show();
                ghost.position().setX(pac.position().x + 22 * WorldMap.TS + ghost.personality() * GHOST_DISTANCE);
                ghost.setMoveDir(Direction.RIGHT);
                ghost.setWishDir(Direction.RIGHT);
                GameContext.SYSTEMS.worldMovementSystem.setSpeed(ghost, 0.58f);
                ghost.animations.select(CommonAnimationID.GHOST_FRIGHTENED);
                ghost.animations.playSelected();
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
