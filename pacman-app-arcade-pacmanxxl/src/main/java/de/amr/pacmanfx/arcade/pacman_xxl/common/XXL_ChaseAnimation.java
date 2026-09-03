/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.basics.math.Direction;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.MovementComp;
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
import de.amr.pacmanfx.game.GameVariant;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationTimer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Simple chasing animation used in XXL menu.
 */
class XXL_ChaseAnimation {

    public enum ChasingState {GHOSTS_CHASING_PAC, PAC_CHASING_GHOSTS}

    public static final float FPS = 60;
    public static final Duration FRAME_TIME = Duration.millis(1000.0 / FPS);

    public static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    public static final int GHOST_POINTS_DISPLAY_SEC = 1;
    public static final int GHOST_DISTANCE = 18;
    public static final float PAC_FLEEING_SPEED = 1.0f;
    public static final float GHOST_CHASE_SPEED = 1.05f;

    private final SpriteAnimationTimer animationTimer = new SpriteAnimationTimer();
    private final SpriteAnimContainer animContainer = new SpriteAnimContainer();
    private final ActorSpriteAnimController animController = new ActorSpriteAnimController();

    private final int numTilesX;
    private final Timeline chaseSimulation = new Timeline();
    private final FloatProperty scaling = new SimpleFloatProperty(1);

    private float y;
    private Pac pac;
    private List<Ghost> ghosts;
    private GhostPoints ghostPoints;
    private BaseRenderer actorRenderer;
    private ChasingState state;

    private int collisionCount;

    private WorldNavigationSystem navigator;
    private MovementSystem motor;

    public XXL_ChaseAnimation(int numTilesX) {
        this.numTilesX = numTilesX;
        chaseSimulation.setCycleCount(Animation.INDEFINITE);
        chaseSimulation.getKeyFrames().setAll(new KeyFrame(FRAME_TIME, _ -> update()));
        animationTimer.attachAnimContainer(animContainer);
    }

    public void draw(long tick) {
        if (actorRenderer == null) {
            return;
        }
        final GraphicsContext ctx = actorRenderer.ctx();
        ctx.save();
        ctx.translate(0, scaling.get() * y);
        actorRenderer.setImageSmoothing(true);
        ghosts.forEach(ghost -> actorRenderer.render(ghost, tick));
        actorRenderer.render(pac, tick);
        if (ghostPoints != null) {
            actorRenderer.render(ghostPoints, tick);
        }
        ctx.restore();
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

    private void update() {
        switch (state) {
            case GHOSTS_CHASING_PAC -> letGhostsChasePacMan();
            case PAC_CHASING_GHOSTS -> letPacManChaseGhosts();
        }
    }

    private GameVariant variant;

    public void setGameVariant(GameContext game, GameVariant variant, Canvas canvas) {
        requireNonNull(game);
        this.variant = requireNonNull(variant);
        requireNonNull(canvas);
        final GameVariantRenderConfig renderConfig = variant.uiConfig().renderConfig();

        navigator = variant.config().systems().navigator();
        motor     = variant.config().systems().motor();

        actorRenderer = renderConfig.createActorRenderer(animController, canvas);
        actorRenderer.scalingProperty().bind(scalingProperty());

        createPac(renderConfig);
        createGhosts(renderConfig);
        startGhostsChasePacMan();
    }

    private void createPac(GameVariantRenderConfig renderConfig) {
        final var actorFactory = ArcadePacMan_ActorFactory.instance();

        pac = actorFactory.createPacMan();
        pac.pos().setX(numTilesX * WorldMap.TS);
        pac.show();

        navigator.setMoveDir(pac, Direction.LEFT);
        navigator.setWishDir(pac, Direction.LEFT);
        navigator.setMoveDirSpeed(pac, PAC_FLEEING_SPEED);

        animController.setAnimations(pac, renderConfig.createPacAnimations(animContainer));
        animController.select(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        animController.playSelected(pac);
    }

    private void createGhosts(GameVariantRenderConfig renderConfig) {
        ghosts = new ArrayList<>(List.of(
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(animController, animContainer, GhostPersonality.ORANGE_GHOST_POKEY)
        ));
    }

    private void letPacManChaseGhosts() {
        if (ghosts.isEmpty()) {
            createGhosts(variant.uiConfig().renderConfig());
            collisionCount = 0;
        }

        // If ghosts and Pac leave screen at right border, ghosts start chasing Pac moving left
        if (pac.pos().x() > (numTilesX + 14) * WorldMap.TS) {
            startGhostsChasePacMan();
        }
        else {
            if (ghostPoints == null) {
                pac.show();
                moveGhosts();
                movePac();
                checkCollisionPacGhost();
            }
            else {
                ghostPoints.lifetime().becomeOlder();
                if (ghostPoints.lifetime().ends()) {
                    ghostPoints = null;
                }
            }
        }
    }

    private void startGhostsChasePacMan() {
        navigator.setMoveDir(pac, Direction.LEFT);
        navigator.setWishDir(pac, Direction.LEFT);
        pac.pos().setX(numTilesX * WorldMap.TS);

        for (Ghost ghost : ghosts) {
            ghost.pos().setX((numTilesX + 4) * WorldMap.TS + ghost.personality().ordinal() * GHOST_DISTANCE);
            ghost.show();

            navigator.setMoveDir(ghost, Direction.LEFT);
            navigator.setWishDir(ghost, Direction.LEFT);
            navigator.setMoveDirSpeed(ghost, GHOST_CHASE_SPEED);

            animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
            animController.playSelected(ghost);
        }
        collisionCount = 0;
        state = ChasingState.GHOSTS_CHASING_PAC;
    }

    private void letGhostsChasePacMan() {
        moveGhosts();
        movePac();

        if (ghosts.getLast().pos().x() < -4 * WorldMap.TS) { // ghosts left screen on the left side
            pac.pos().setX(-(numTilesX - 4) * WorldMap.TS);
            navigator.setMoveDir(pac, Direction.RIGHT);
            navigator.setWishDir(pac, Direction.RIGHT);

            for (Ghost ghost : ghosts) {
                ghost.show();
                ghost.pos().setX(pac.pos().x() + 22 * WorldMap.TS + ghost.personality().ordinal() * GHOST_DISTANCE);

                navigator.setMoveDir(ghost, Direction.RIGHT);
                navigator.setWishDir(ghost, Direction.RIGHT);
                navigator.setMoveDirSpeed(ghost, 0.58f);

                animController.select(ghost, CommonSpriteAnimationID.GHOST_FRIGHTENED);
                animController.playSelected(ghost);
            }

            // Let Pac-Man chase the ghosts from left to right side of the screen
            state = ChasingState.PAC_CHASING_GHOSTS;
        }
    }

    private void checkCollisionPacGhost() {
        for (Iterator<Ghost> it = ghosts.iterator(); it.hasNext(); ) {
            final Ghost ghost = it.next();
            if (colliding(pac, ghost)) {
                ++collisionCount;

                ghostPoints = new GhostPoints(GHOST_POINTS[collisionCount - 1]);
                ghostPoints.pos().set(ghost.pos().asVector2f());
                ghostPoints.setComp(MovementComp.class, new MovementComp());
                ghostPoints.optMovement().ifPresent(movement -> movement.setVelocity(ghost.movement().velocity()));
                ghostPoints.setLifetimeSec(GHOST_POINTS_DISPLAY_SEC);
                ghostPoints.show();

                it.remove();
                pac.hide();
                break;
            }
        }
    }

    private static boolean colliding(GameEntity either, GameEntity other) {
        return Math.abs(either.pos().x() - other.pos().x()) < 1;
    }

    private void movePac() {
        motor.move(pac);
    }

    private void moveGhosts() {
        for (Ghost ghost : ghosts) {
            motor.move(ghost);
        }
    }
}
