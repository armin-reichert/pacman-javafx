/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.gamescene.introscene;

import de.amr.basics.math.Direction;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ecs.system.MovementSystem;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.Arcade_GameExtensions;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSystems;
import de.amr.pacmanfx.core.entities.WorldNavigationSystem;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.basics.ui.entities.props.ghostpoints.GhostPoints;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostState;
import de.amr.pacmanfx.core.entities.actor.ghost.GhostAnimationSystem;
import de.amr.pacmanfx.core.model.GhostPersonality;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.core.rules.CollisionStrategy;
import de.amr.pacmanfx.game.GameVariantRuntime;
import de.amr.pacmanfx.ui.assets.VoiceID;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.GameSceneCanvasRenderingComp;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.pacmanfx.arcade.pacman.gamescene.introscene.IntroSceneController.*;
import static de.amr.pacmanfx.core.entities.actor.ghost.GhostState.EATEN;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

/**
 * The ghosts are presented one by one, then Pac-Man is chased by the ghosts, turns the cards and hunts the ghosts himself.
 */
public class ArcadePacMan_IntroScene extends AbstractGameScene {

    private static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };

    final IntroSceneController flow;
    final IntroSceneView view;

    int numGhostsEaten;
    int ghostIndex;
    long lastGhostEatenTick;

    public ArcadePacMan_IntroScene() {
        setComp(GameSceneCanvasRenderingComp.class, new GameSceneCanvasRenderingComp());
        flow = new IntroSceneController();
        view = new IntroSceneView();
    }

    @Override
    public void onActivate() {
        final Arcade_Actions actions = app().variantManager().currentRuntime()
            .extensionValue(Arcade_GameExtensions.ACTIONS, Arcade_Actions.class);

        final var bindingsMap = actionBindings().registry();
        bindingsMap.registerAllBindings(actions.gameStartActionBindings()); // insert coin + start game actions
        bindingsMap.registerAllBindings(app().commonActions().sceneTestActions().bindings()); // actions for starting tests

        flow.restartState(this, IntroSceneController.SceneState.STARTING);
    }

    @Override
    public void onDeactivate() {
        view.pulse.stop();
        soundManager().voice().stop();
    }

    @Override
    public void onTick(GameContext game) {
        flow.update(this);
    }

    @Override
    public Stream<Renderable> renderables() {
        return view.renderables();
    }

    void initState() {
        final GameVariantRuntime variant = app().variantManager().currentRuntime();
        view.createPacManAndGhosts(
            variant.uiConfig().renderConfig(),
            variant.playConfig().systems().actorSpriteAnimController(),
            variant.spriteAnimContainer()
        );
        view.hideEverything();

        ghostIndex = 0;
        lastGhostEatenTick = 0;
        numGhostsEaten = 0;

        soundManager().voice().playAfterSec(1, VoiceID.START_HINT.media());
    }

    void startChasingPacMan(GameContext game) {
        final GameSystems systems = game.playConfig().systems();
        final WorldNavigationSystem nav = systems.navigator();

        view.pulse.start();

        view.pacMan.pos().set(TS * 28, TS * 20);
        nav.setMoveDir(view.pacMan, Direction.LEFT);
        nav.setSpeed(view.pacMan, CHASING_SPEED);
        view.pacMan.show();

        for (Ghost ghost : view.ghosts) {
            ghost.pos().set(view.pacMan.pos().x() + 16 * ghost.personality().ordinal() + 18, view.pacMan.pos().y());
            nav.setMoveDir(ghost, Direction.LEFT);
            nav.setWishDir(ghost, Direction.LEFT);
            nav.setSpeed(ghost, CHASING_SPEED);
            systems.ghostState().setState(ghost, GhostState.HUNTING_PAC);
            ghost.show();
        }
    }

    void chasePacMan(long tick) {
        final GameSystems systems = game().playConfig().systems();
        final MovementSystem motor = systems.motor();
        final GhostAnimationSystem ghostSpriteAnimationSystem = systems.ghostAnimation();

        view.pulse.triggerPulse();

        motor.move(view.pacMan);
        for (Ghost ghost : view.ghosts) {
            motor.move(ghost);
        }

        // "shaking" effect
        final long tick_0_to_5 = tick % 6;
        final Ghost pinkGhost = view.ghosts[GhostPersonality.PINK_GHOST_SPEEDY.ordinal()];
        final Ghost cyanGhost = view.ghosts[GhostPersonality.CYAN_GHOST_BASHFUL.ordinal()];
        if (tick_0_to_5 == 2) {
            pinkGhost.pos().setX(pinkGhost.pos().x() + 0.5);
            cyanGhost.pos().setX(cyanGhost.pos().x() - 0.5);
        }
        else if (tick_0_to_5 == 5) {
            pinkGhost.pos().setX(pinkGhost.pos().x() - 0.5);
            cyanGhost.pos().setX(cyanGhost.pos().x() + 0.5);
        }

        for (Ghost ghost : view.ghosts) {
            ghostSpriteAnimationSystem.update(ghost);
        }
    }

    void turnCardsStopPacMan(GameContext game) {
        final GameSystems systems = game.playConfig().systems();
        final WorldNavigationSystem nav = systems.navigator();
        final ActorSpriteAnimController animController = systems.actorSpriteAnimController();

        nav.setSpeed(view.pacMan, 0);
        systems.actorSpriteAnimController().stopSelected(view.pacMan);

        for (Ghost ghost : view.ghosts) {
            nav.setMoveDir(ghost, Direction.RIGHT);
            nav.setWishDir(ghost, Direction.RIGHT);
            nav.setSpeed(ghost, GHOST_FRIGHTENED_SPEED);
            systems.ghostState().setState(ghost, GhostState.FRIGHTENED);
            animController.select(ghost, CommonSpriteAnimationID.GHOST_FRIGHTENED);
            animController.playSelected(ghost);
        }
    }

    void turnCardsRestartPacMan(GameSystems systems) {
        systems.navigator().setSpeed(view.pacMan, CHASING_SPEED);
        systems.actorSpriteAnimController().playSelected(view.pacMan);
    }

    void chaseGhosts(GameContext game, long tick) {
        final GameSystems systems = game.playConfig().systems();
        final MovementSystem motor = systems.motor();

        view.pulse.triggerPulse();

        motor.move(view.pacMan);
        for (Ghost ghost : view.ghosts) {
            motor.move(ghost);
        }

        findNextEdibleGhost().ifPresent(victim -> eatGhostAndStopChasing(game, victim, tick));
        if (tick == lastGhostEatenTick + GHOST_EATING_TICKS) {
            continueChasing(systems);
        }
    }

    private Optional<Ghost> findNextEdibleGhost() {
        return Stream.of(view.ghosts)
            .filter(ghost -> ghost.state().enumValue() != GhostState.EATEN)
            .filter(ghost -> CollisionStrategy.SAME_TILE.collide(ghost, view.pacMan))
            .findFirst();
    }

    private void eatGhostAndStopChasing(GameContext game, Ghost victim, long tick) {
        final GameSystems systems = game.playConfig().systems();

        victim.state().setEnumValue(GhostState.EATEN);
        victim.hide();

        view.pacMan.hide();
        systems.navigator().setSpeed(view.pacMan, 0);

        for (Ghost ghost : view.ghosts) {
            systems.navigator().setSpeed(ghost, 0);
            systems.actorSpriteAnimController().stopSelected(ghost);
        }

        view.points = new GhostPoints(GHOST_POINTS[numGhostsEaten]);
        view.points.pos().set(victim.pos().asVector2f());
        view.points.show();

        ++numGhostsEaten;
        lastGhostEatenTick = tick;
    }

    private void continueChasing(GameSystems systems) {
        view.pacMan.show();
        systems.navigator().setSpeed(view.pacMan, CHASING_SPEED);

        for (Ghost ghost : view.ghosts) {
            if (ghost.state().enumValue() == EATEN) {
                ghost.hide();
                view.points = null;
            } else {
                ghost.show();
                systems.navigator().setSpeed(ghost, GHOST_FRIGHTENED_SPEED);
                ghost.spriteAnimation().spriteAnimations().playSelected();
            }
        }
    }
}