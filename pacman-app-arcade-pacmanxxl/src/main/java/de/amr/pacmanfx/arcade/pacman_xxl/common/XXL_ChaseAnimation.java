/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.MovementComp;
import de.amr.basics.math.Direction;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.entities.props.ghostpoints.GhostPoints;
import de.amr.basics.ui.rendering.Renderer;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.gamescene.d2.SpriteAnimationTimer;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;
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

import static de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder.pacView;
import static de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder.propView;
import static java.util.Objects.requireNonNull;

/**
 * Simple chasing animation used in XXL menu.
 */
class XXL_ChaseAnimation {

    public enum ChasingState {GHOSTS_CHASING_PAC, PAC_CHASING_GHOSTS}

    public static final Duration FRAME_TIME = Duration.millis(1000f / 60f);

    public static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    public static final int GHOST_POINTS_DISPLAY_SEC = 1;
    public static final int GHOST_DISTANCE = 18;
    public static final float PAC_FLEEING_SPEED = 1.0f;
    public static final float GHOST_CHASE_SPEED = 1.05f;

    private final int numTilesX;

    private final Timeline chaseSimulation;

    private final FloatProperty scaling = new SimpleFloatProperty(1);

    private GameVariantRuntime runtime;

    private Pac pac;
    private List<Ghost> ghosts;
    private GhostPoints ghostPoints;
    private ChasingState state;

    private float y;
    private int collisionCount;

    private Renderer variantRenderer;

    public XXL_ChaseAnimation(int numTilesX) {
        this.numTilesX = numTilesX;

        final var animationFrame = new KeyFrame(FRAME_TIME, _ -> {
            switch (state) {
                case GHOSTS_CHASING_PAC -> letGhostsChasePacMan();
                case PAC_CHASING_GHOSTS -> letPacManChaseGhosts();
            }
        });
        chaseSimulation = new Timeline();
        chaseSimulation.getKeyFrames().add(animationFrame);
        chaseSimulation.setCycleCount(Animation.INDEFINITE);
    }

    public void draw(long tick) {
        if (variantRenderer == null) {
            return;
        }

        final GraphicsContext ctx = variantRenderer.ctx();
        ctx.save();
        ctx.translate(0, scaling.get() * y);

        variantRenderer.render(pacView(pac), tick);
        ghosts.stream().map(GameEntityViewBuilder::ghostView).forEach(rg -> variantRenderer.render(rg, tick));
        if (ghostPoints != null) {
            variantRenderer.render(propView(ghostPoints), tick);
        }

        ctx.restore();
    }

    public FloatProperty scalingProperty() {
        return scaling;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void startChaseSimulation(SpriteAnimationTimer animationTimer) {
        chaseSimulation.play();
        animationTimer.start();
    }

    public void stopChaseSimulation(SpriteAnimationTimer animationTimer) {
        chaseSimulation.stop();
        animationTimer.stop();
    }

    public void setGameVariant(GameVariantRuntime runtime, Canvas canvas) {
        this.runtime = requireNonNull(runtime);
        requireNonNull(canvas);

        final GameVariantRenderConfig renderConfig = runtime.uiConfig().renderConfig();
        final ActorSpriteAnimController animController = runtime.playConfig().systems().actorSpriteAnimController();

        variantRenderer = renderConfig.createVariantRenderer(animController, canvas);
        variantRenderer.scalingProperty().bind(scalingProperty());

        createPac();
        createGhosts();

        //TODO check this
        startGhostsChasePacMan();
    }

    private void createPac() {
        final GameVariantRenderConfig renderConfig = runtime.uiConfig().renderConfig();
        final ActorSpriteAnimController animController = runtime.playConfig().systems().actorSpriteAnimController();

        final var actorFactory = ArcadePacMan_ActorFactory.instance();
        pac = actorFactory.createPacMan();
        pac.pos().setX(numTilesX * WorldMap.TS);
        pac.show();

        final WorldNavigationSystem navigator = runtime.playConfig().systems().navigator();
        navigator.setMoveDir(pac, Direction.LEFT);
        navigator.setWishDir(pac, Direction.LEFT);
        navigator.setSpeed(pac, PAC_FLEEING_SPEED);

        animController.setAnimations(pac, renderConfig.createPacAnimations(runtime.spriteAnimContainer()));
        animController.select(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING);
        animController.playSelected(pac);
    }

    private void createGhosts() {
        final GameVariantRenderConfig renderConfig = runtime.uiConfig().renderConfig();
        final ActorSpriteAnimController animController = runtime.playConfig().systems().actorSpriteAnimController();

        ghosts = new ArrayList<>(List.of(
            renderConfig.createAnimatedGhost(animController, runtime.spriteAnimContainer(), GhostPersonality.RED_GHOST_SHADOW),
            renderConfig.createAnimatedGhost(animController, runtime.spriteAnimContainer(), GhostPersonality.PINK_GHOST_SPEEDY),
            renderConfig.createAnimatedGhost(animController, runtime.spriteAnimContainer(), GhostPersonality.CYAN_GHOST_BASHFUL),
            renderConfig.createAnimatedGhost(animController, runtime.spriteAnimContainer(), GhostPersonality.ORANGE_GHOST_POKEY)
        ));
    }

    private void letPacManChaseGhosts() {
        if (ghosts.isEmpty()) {
            createGhosts();
            collisionCount = 0;
        }

        // If ghosts and Pac leave screen at right border, ghosts start chasing Pac moving left
        if (pac.pos().x() > (numTilesX + 14) * WorldMap.TS) {
            startGhostsChasePacMan();
        }
        else {
            if (ghostPoints == null) {
                pac.show();
                moveActors();
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
        final ActorSpriteAnimController animController = runtime.playConfig().systems().actorSpriteAnimController();
        final WorldNavigationSystem navigator = runtime.playConfig().systems().navigator();

        navigator.setMoveDir(pac, Direction.LEFT);
        navigator.setWishDir(pac, Direction.LEFT);
        pac.pos().setX(numTilesX * WorldMap.TS);

        for (Ghost ghost : ghosts) {
            ghost.pos().setX((numTilesX + 4) * WorldMap.TS + ghost.personality().ordinal() * GHOST_DISTANCE);
            ghost.show();

            navigator.setMoveDir(ghost, Direction.LEFT);
            navigator.setWishDir(ghost, Direction.LEFT);
            navigator.setSpeed(ghost, GHOST_CHASE_SPEED);

            animController.select(ghost, CommonSpriteAnimationID.GHOST_NORMAL);
            animController.playSelected(ghost);
        }
        collisionCount = 0;
        state = ChasingState.GHOSTS_CHASING_PAC;
    }

    private void letGhostsChasePacMan() {
        final ActorSpriteAnimController animController = runtime.playConfig().systems().actorSpriteAnimController();
        final WorldNavigationSystem navigator = runtime.playConfig().systems().navigator();

        moveActors();

        if (ghosts.getLast().pos().x() < -4 * WorldMap.TS) { // ghosts left screen on the left side
            pac.pos().setX(-(numTilesX - 4) * WorldMap.TS);
            navigator.setMoveDir(pac, Direction.RIGHT);
            navigator.setWishDir(pac, Direction.RIGHT);

            for (Ghost ghost : ghosts) {
                ghost.show();
                ghost.pos().setX(pac.pos().x() + 22 * WorldMap.TS + ghost.personality().ordinal() * GHOST_DISTANCE);

                navigator.setMoveDir(ghost, Direction.RIGHT);
                navigator.setWishDir(ghost, Direction.RIGHT);
                navigator.setSpeed(ghost, 0.58f);

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

    private void moveActors() {
        final var motor = runtime.playConfig().systems().motor();
        motor.move(pac);
        for (Ghost ghost : ghosts) {
            motor.move(ghost);
        }
    }
}
