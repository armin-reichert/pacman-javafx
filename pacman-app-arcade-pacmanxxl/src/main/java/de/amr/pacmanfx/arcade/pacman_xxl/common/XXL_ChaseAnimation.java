/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.GameSystems;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
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

    public void init(GameContext game, GameVariantRenderConfig renderConfig, Canvas canvas, SpriteAnimationContainer container) {
        requireNonNull(game);
        requireNonNull(renderConfig);
        requireNonNull(canvas);
        requireNonNull(container);

        final GameSystems sys = game.variant().systems();

        timeline.getKeyFrames().setAll(new KeyFrame(FRAME_TIME, _ -> update(sys)));

        actorRenderer = renderConfig.createActorRenderer(sys.spriteAnimController(), canvas);
        actorRenderer.scalingProperty().bind(scalingProperty());

        final ArcadePacMan_ActorFactory factory = ArcadePacMan_ActorFactory.instance();

        pac = factory.createPacMan();
        pac.pos().setX(numTilesX * WorldMap.TS);
        pac.show();

        sys.worldNavigator().setMoveDir(pac, Direction.LEFT);
        sys.worldNavigator().setWishDir(pac, Direction.LEFT);
        sys.worldNavigator().setMoveDirSpeed(pac, PAC_FLEEING_SPEED);

        sys.spriteAnimController().setAnimations(pac, renderConfig.createPacAnimations(container));
        sys.spriteAnimController().select(pac, CommonSpriteAnimationID.PAC_MUNCHING);
        sys.spriteAnimController().playSelected(pac);

        ghosts = List.of(
            renderConfig.createAnimatedGhost(game, container, GhostPersonality.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(game, container, GhostPersonality.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(game, container, GhostPersonality.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(game, container, GhostPersonality.ORANGE_GHOST_POKEY)
        );
        for (Ghost ghost : ghosts) {
            ghost.pos().setX((numTilesX + 4) * WorldMap.TS + ghost.personality().ordinal() * GHOST_DISTANCE);
            ghost.show();

            sys.worldNavigator().setMoveDir(ghost, Direction.LEFT);
            sys.worldNavigator().setWishDir(ghost, Direction.LEFT);
            sys.worldNavigator().setMoveDirSpeed(ghost, GHOST_CHASE_SPEED);

            sys.spriteAnimController().select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
            sys.spriteAnimController().playSelected(ghost);
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
        movementSystem.move(pac);
        for (Ghost ghost : ghosts) {
            movementSystem.move(ghost);
        }
    }

    private void pacManChasesGhosts(GameSystems sys) {
        moveActors(sys.motor());
        // If ghosts and Pac leave screen at right border, ghosts start chasing Pac moving left
        if (pac.pos().x() > (numTilesX + 14) * WorldMap.TS) {
            sys.worldNavigator().setMoveDir(pac, Direction.LEFT);
            sys.worldNavigator().setWishDir(pac, Direction.LEFT);
            pac.pos().setX(numTilesX * WorldMap.TS);

            for (Ghost ghost : ghosts) {
                ghost.pos().setX((numTilesX + 4) * WorldMap.TS + ghost.personality().ordinal() * 2 * WorldMap.TS);
                ghost.show();

                sys.worldNavigator().setMoveDir(ghost, Direction.LEFT);
                sys.worldNavigator().setWishDir(ghost, Direction.LEFT);
                sys.worldNavigator().setMoveDirSpeed(ghost, 1.05f);

                sys.spriteAnimController().select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
                sys.spriteAnimController().playSelected(ghost);
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
                    sys.spriteAnimController().selectAndSetFrame(ghost, CommonSpriteAnimationID.GHOST_POINTS, i);
                    break;
                }
            }
        }
    }

    private static boolean colliding(GameEntity either, GameEntity other) {
        return Math.abs(either.pos().x() - other.pos().x()) < 1;
    }

    private void ghostsChasePacMan(GameSystems sys) {
        moveActors(sys.motor());

        if (ghosts.getLast().pos().x() < -4 * WorldMap.TS) { // ghosts left screen on the left side
            pac.pos().setX(-(numTilesX - 6) * WorldMap.TS);
            sys.worldNavigator().setMoveDir(pac, Direction.RIGHT);
            sys.worldNavigator().setWishDir(pac, Direction.RIGHT);

            for (Ghost ghost : ghosts) {
                ghost.show();
                ghost.pos().setX(pac.pos().x() + 22 * WorldMap.TS + ghost.personality().ordinal() * GHOST_DISTANCE);

                sys.worldNavigator().setMoveDir(ghost, Direction.RIGHT);
                sys.worldNavigator().setWishDir(ghost, Direction.RIGHT);
                sys.worldNavigator().setMoveDirSpeed(ghost, 0.58f);

                sys.spriteAnimController().select(ghost, CommonSpriteAnimationID.GHOST_FRIGHTENED);
                sys.spriteAnimController().playSelected(ghost);
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
