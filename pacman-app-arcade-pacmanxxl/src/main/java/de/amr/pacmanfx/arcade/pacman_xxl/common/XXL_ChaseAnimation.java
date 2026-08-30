/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.ecs.systems.MovementSystem;
import de.amr.pacmanfx.core.ecs.systems.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.GhostPoints;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimContainer;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationTimer;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Simple chasing animation used in XXL menu.
 */
class XXL_ChaseAnimation {

    enum ChasingState {GHOSTS_CHASING_PAC, PAC_CHASING_GHOSTS}

    static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    private static final float FPS = 60;
    private static final Duration FRAME_TIME = Duration.millis(1000.0 / FPS);

    private static final int GHOST_DISTANCE = 18;
    private static final float PAC_FLEEING_SPEED = 1.0f;
    private static final float GHOST_CHASE_SPEED = 1.05f;

    private final int numTilesX;
    private final Timeline chaseSimulation;
    private final FloatProperty scaling = new SimpleFloatProperty(1);
    private float y;
    private Pac pac;
    private List<Ghost> ghosts;
    private GhostPoints ghostPoints;
    private ActorRenderer actorRenderer;
    private ChasingState state;

    record Collision(Ghost ghost, long time) {}
    private final List<Collision> collisions = new ArrayList<>();

    private final SpriteAnimationTimer animationTimer = new SpriteAnimationTimer();
    private final SpriteAnimContainer animContainer = new SpriteAnimContainer();
    private final ActorSpriteAnimController animController = new ActorSpriteAnimController();

    public XXL_ChaseAnimation(int numTilesX) {
        this.numTilesX = numTilesX;
        chaseSimulation = new Timeline();
        chaseSimulation.setCycleCount(Animation.INDEFINITE);
        animationTimer.attachAnimContainer(animContainer);
    }

    public FloatProperty scalingProperty() {
        return scaling;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void startChaseSimulation() {
        chaseSimulation.play();
        animationTimer.start();
    }

    public void stopChaseSimulation() {
        chaseSimulation.stop();
        animationTimer.stop();
    }

    public void displayGameVariant(GameContext game, GameVariantRenderConfig renderConfig, Canvas canvas) {
        requireNonNull(game);
        requireNonNull(renderConfig);
        requireNonNull(canvas);
        requireNonNull(animContainer);

        final ArcadePacMan_ActorFactory actorFactory = ArcadePacMan_ActorFactory.instance();
        final GameSystems systems = game.variant().systems();
        final WorldNavigationSystem worldNavigationSystem = systems.navigator();

        chaseSimulation.getKeyFrames().setAll(new KeyFrame(FRAME_TIME, _ -> update(systems)));

        actorRenderer = renderConfig.createActorRenderer(animController, canvas);
        actorRenderer.scalingProperty().bind(scalingProperty());

        pac = actorFactory.createPacMan();
        pac.pos().setX(numTilesX * WorldMap.TS);
        pac.show();

        worldNavigationSystem.setMoveDir(pac, Direction.LEFT);
        worldNavigationSystem.setWishDir(pac, Direction.LEFT);
        worldNavigationSystem.setMoveDirSpeed(pac, PAC_FLEEING_SPEED);

        animController.setAnimations(pac, renderConfig.createPacAnimations(animContainer));
        animController.select(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        animController.playSelected(pac);

        ghosts = List.of(
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.ORANGE_GHOST_POKEY)
        );

        for (Ghost ghost : ghosts) {
            ghost.pos().setX((numTilesX + 4) * WorldMap.TS + ghost.personality().ordinal() * GHOST_DISTANCE);
            ghost.show();

            worldNavigationSystem.setMoveDir(ghost, Direction.LEFT);
            worldNavigationSystem.setWishDir(ghost, Direction.LEFT);
            worldNavigationSystem.setMoveDirSpeed(ghost, GHOST_CHASE_SPEED);

            animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
            animController.playSelected(ghost);
        }

        collisions.clear();
        state = ChasingState.GHOSTS_CHASING_PAC;
    }

    private void update(GameSystems systems) {
        switch (state) {
            case GHOSTS_CHASING_PAC -> ghostsChasePacMan(systems);
            case PAC_CHASING_GHOSTS -> pacManChasesGhosts(systems);
        }
    }

    private void moveActors(MovementSystem movementSystem) {
        movementSystem.move(pac);
        for (Ghost ghost : ghosts) {
            movementSystem.move(ghost);
        }
    }

    private void pacManChasesGhosts(GameSystems systems) {
        final WorldNavigationSystem worldNavigationSystem = systems.navigator();

        moveActors(systems.motor());

        // If ghosts and Pac leave screen at right border, ghosts start chasing Pac moving left
        if (pac.pos().x() > (numTilesX + 14) * WorldMap.TS) {
            worldNavigationSystem.setMoveDir(pac, Direction.LEFT);
            worldNavigationSystem.setWishDir(pac, Direction.LEFT);
            pac.pos().setX(numTilesX * WorldMap.TS);

            for (Ghost ghost : ghosts) {
                ghost.pos().setX((numTilesX + 4) * WorldMap.TS + ghost.personality().ordinal() * 2 * WorldMap.TS);
                ghost.show();

                worldNavigationSystem.setMoveDir(ghost, Direction.LEFT);
                worldNavigationSystem.setWishDir(ghost, Direction.LEFT);
                worldNavigationSystem.setMoveDirSpeed(ghost, 1.05f);

                animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
                animController.playSelected(ghost);
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
                    ghostPoints = new GhostPoints(GHOST_POINTS[i]); //TODO correct value
                    ghostPoints.pos().set(ghost.pos().asVector2f());
                    ghostPoints.setLifetime(1);
                    ghostPoints.show();
                    ghost.hide();
                    //animController.selectAndSetFrame(ghost, CommonSpriteAnimationID.GHOST_POINTS, i);
                    break;
                }
            }
        }
        if (ghostPoints != null) {
            ghostPoints.lifetime().becomeOlder();
            if (ghostPoints.lifetime().ends()) {
                ghostPoints = null;
            }
        }
    }

    private static boolean colliding(GameEntity either, GameEntity other) {
        return Math.abs(either.pos().x() - other.pos().x()) < 1;
    }

    private void ghostsChasePacMan(GameSystems systems) {
        final WorldNavigationSystem worldNavigationSystem = systems.navigator();

        moveActors(systems.motor());

        if (ghosts.getLast().pos().x() < -4 * WorldMap.TS) { // ghosts left screen on the left side
            pac.pos().setX(-(numTilesX - 6) * WorldMap.TS);
            worldNavigationSystem.setMoveDir(pac, Direction.RIGHT);
            worldNavigationSystem.setWishDir(pac, Direction.RIGHT);

            for (Ghost ghost : ghosts) {
                ghost.show();
                ghost.pos().setX(pac.pos().x() + 22 * WorldMap.TS + ghost.personality().ordinal() * GHOST_DISTANCE);

                worldNavigationSystem.setMoveDir(ghost, Direction.RIGHT);
                worldNavigationSystem.setWishDir(ghost, Direction.RIGHT);
                worldNavigationSystem.setMoveDirSpeed(ghost, 0.58f);

                animController.select(ghost, CommonSpriteAnimationID.GHOST_FRIGHTENED);
                animController.playSelected(ghost);
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
            if (ghostPoints != null) {
                actorRenderer.drawActor(ghostPoints);
            }
            ctx.restore();
        }
    }
}
