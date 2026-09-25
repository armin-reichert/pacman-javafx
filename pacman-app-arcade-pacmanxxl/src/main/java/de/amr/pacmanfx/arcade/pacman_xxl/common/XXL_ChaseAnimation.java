/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman_xxl.common;

import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ecs.comp.MovementComp;
import de.amr.basics.math.Direction;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.entities.props.ghostpoints.GhostPoints;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.WorldNavigationSystem;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.rendering.GameEntityViewBuilder;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Simple chasing animation used in XXL menu.
 */
class XXL_ChaseAnimation {

    public enum ChasingState {GHOSTS_CHASING_PAC, PAC_CHASING_GHOSTS}

    public static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    public static final int GHOST_POINTS_DISPLAY_SEC = 1;
    public static final int GHOST_DISTANCE = 18;
    public static final float PAC_FLEEING_SPEED = 1.0f;
    public static final float GHOST_CHASE_SPEED = 1.05f;

    private final int numTilesX;

    private final GameVariantRuntime runtime;

    private Pac pac;
    private List<Ghost> ghosts;
    private GhostPoints ghostPoints;
    private ChasingState state;

    private GameEntityView pacView;
    private final Map<Ghost, GameEntityView> ghostViews = new HashMap<>();
    private GameEntityView ghostPointsView;

    private final float offsetY;

    private int collisionCount;

    public XXL_ChaseAnimation(int numTilesX, float offsetY, GameVariantRuntime runtime) {
        this.numTilesX = numTilesX;
        this.offsetY = offsetY;
        this.runtime = requireNonNull(runtime);
        createPac();
        createGhosts();
    }

    public void simulate() {
        switch (state) {
            case GHOSTS_CHASING_PAC -> letGhostsChasePacMan();
            case PAC_CHASING_GHOSTS -> letPacManChaseGhosts();
        }
    }

    public Stream<Renderable> renderables() {
        return Ufx.streamOf(
            pacView,
            ghostViews.values().stream().filter(view -> view.entity().isVisible()),
            ghostPointsView
        );
    }

    private GameEntityView createView(GameEntity entity) {
        return GameEntityViewBuilder.builder()
            .entity(entity)
            .layer(RenderingLayer.PROPS)
            .offset(new Vector2f(0, offsetY))
            .build();
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

        pacView = createView(pac);
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

        ghosts.forEach(ghost -> ghostViews.put(ghost, createView(ghost)));
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
                    ghostPointsView = null;
                }
            }
        }
    }

    public void startGhostsChasePacMan() {
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

                ghostPointsView = createView(ghostPoints);

                it.remove();
                ghostViews.remove(ghost);

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
