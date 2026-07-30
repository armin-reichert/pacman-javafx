/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.systems.common.GameSystems;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
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

        final GameSystems sys = gameContext.systems();

        timeline.getKeyFrames().setAll(new KeyFrame(FRAME_TIME, _ -> update(sys)));

        actorRenderer = renderConfig.createActorRenderer(sys.spriteAnim(), canvas);
        actorRenderer.scalingProperty().bind(scalingProperty());

        final ArcadePacMan_ActorFactory factory = ArcadePacMan_ActorFactory.instance();

        pac = factory.createPacMan();
        pac.position().setX(numTilesX * WorldMap.TS);
        pac.visibility().show();

        sys.navigator().setMoveDir(pac, Direction.LEFT);
        sys.navigator().setWishDir(pac, Direction.LEFT);
        sys.navigator().setSpeed(pac, PAC_FLEEING_SPEED);

        sys.spriteAnim().setAnimations(pac, renderConfig.createPacAnimations(container));
        sys.spriteAnim().select(pac, CommonAnimationID.PAC_MUNCHING);
        sys.spriteAnim().playSelected(pac);

        ghosts = List.of(
            renderConfig.createAnimatedGhost(gameContext, container, GhostPersonality.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(gameContext, container, GhostPersonality.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(gameContext, container, GhostPersonality.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(gameContext, container, GhostPersonality.ORANGE_GHOST_POKEY)
        );
        for (Ghost ghost : ghosts) {
            ghost.position().setX((numTilesX + 4) * WorldMap.TS + ghost.personality().ordinal() * GHOST_DISTANCE);
            ghost.visibility().show();

            sys.navigator().setMoveDir(ghost, Direction.LEFT);
            sys.navigator().setWishDir(ghost, Direction.LEFT);
            sys.navigator().setSpeed(ghost, GHOST_CHASE_SPEED);

            sys.spriteAnim().select(ghost, CommonAnimationID.GHOST_NORMAL);
            sys.spriteAnim().playSelected(ghost);
        }

        collisions.clear();

        state = ChasingState.GHOSTS_CHASING_PAC;
    }

    private void update(GameSystems sys) {
        switch (state) {
            case GHOSTS_CHASING_PAC -> ghostsChasePacMan(sys);
            case PAC_CHASING_GHOSTS -> pacManChasesGhosts(sys);
        }
    }

    private void moveActors(MovementSystem movementSystem) {
        movementSystem.moveAccelerated(pac);
        for (Ghost ghost : ghosts) {
            movementSystem.moveAccelerated(ghost);
        }
    }

    private void pacManChasesGhosts(GameSystems sys) {
        moveActors(sys.motor());
        // If ghosts and Pac leave screen at right border, ghosts start chasing Pac moving left
        if (pac.position().x > (numTilesX + 14) * WorldMap.TS) {
            sys.navigator().setMoveDir(pac, Direction.LEFT);
            sys.navigator().setWishDir(pac, Direction.LEFT);
            pac.position().setX(numTilesX * WorldMap.TS);

            for (Ghost ghost : ghosts) {
                ghost.position().setX((numTilesX + 4) * WorldMap.TS + ghost.personality().ordinal() * 2 * WorldMap.TS);
                ghost.visibility().show();

                sys.navigator().setMoveDir(ghost, Direction.LEFT);
                sys.navigator().setWishDir(ghost, Direction.LEFT);
                sys.navigator().setSpeed(ghost, 1.05f);

                sys.spriteAnim().select(ghost, CommonAnimationID.GHOST_NORMAL);
                sys.spriteAnim().playSelected(ghost);
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
                    sys.spriteAnim().selectAndSetFrame(ghost, CommonAnimationID.GHOST_POINTS, i);
                    break;
                }
            }
        }
    }

    private static boolean colliding(Actor either, Actor other) {
        return Math.abs(either.position().x - other.position().x) < 1;
    }

    private void ghostsChasePacMan(GameSystems sys) {
        moveActors(sys.motor());

        if (ghosts.getLast().position().x < -4 * WorldMap.TS) { // ghosts left screen on the left side
            pac.position().setX(-(numTilesX - 6) * WorldMap.TS);
            sys.navigator().setMoveDir(pac, Direction.RIGHT);
            sys.navigator().setWishDir(pac, Direction.RIGHT);

            for (Ghost ghost : ghosts) {
                ghost.visibility().show();
                ghost.position().setX(pac.position().x + 22 * WorldMap.TS + ghost.personality().ordinal() * GHOST_DISTANCE);

                sys.navigator().setMoveDir(ghost, Direction.RIGHT);
                sys.navigator().setWishDir(ghost, Direction.RIGHT);
                sys.navigator().setSpeed(ghost, 0.58f);

                sys.spriteAnim().select(ghost, CommonAnimationID.GHOST_FRIGHTENED);
                sys.spriteAnim().playSelected(ghost);
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
