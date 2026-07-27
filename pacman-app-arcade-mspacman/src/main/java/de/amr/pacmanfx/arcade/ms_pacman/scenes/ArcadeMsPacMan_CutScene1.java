/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.basics.math.Direction;
import de.amr.basics.spriteanim.SpriteAnimationContainer;
import de.amr.basics.timer.TickTimer;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_ActorFactory;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_ActorFactory;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.model.GameModel;
import de.amr.pacmanfx.core.model.actors.Actor;
import de.amr.pacmanfx.core.model.actors.CommonAnimationID;
import de.amr.pacmanfx.core.model.actors.Ghost;
import de.amr.pacmanfx.core.model.actors.Pac;
import de.amr.pacmanfx.core.model.systems.common.MovementSystem;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantRenderConfig;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import java.util.List;

import static de.amr.basics.spriteanim.SpriteAnimationAccess.singleSpriteAnimation;
import static de.amr.pacmanfx.core.model.world.WorldMap.tilesPx;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 */
public class ArcadeMsPacMan_CutScene1 extends AbstractGameScene2D {

    static final int UPPER_Y  = WorldMap.TS * 12;
    static final int MIDDLE_Y = WorldMap.TS * 18;
    static final int LOWER_Y  = WorldMap.TS * 24;

    static final float SPEED_PAC_CHASING = 1.125f;
    static final float SPEED_GHOST_CHASING = 1.25f;
    static final float SPEED_RISING = 0.75f;
    static final float SPEED_GHOST_AFTER_COLLISION = 0.3f;

    // Public for access by renderer
    public Pac pacMan;
    public Pac msPacMan;
    public Ghost inky;
    public Ghost pinky;
    public Actor heart;
    public Clapperboard clapperboard;

    public ArcadeMsPacMan_CutScene1(GameAppContext appContext) {
        super(appContext);
    }

    @Override
    public void onActivate() {
        initScene();
        setState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    public void onTick(GameContext gameContext) {
        final MovementSystem motor = gameContext.systems().motor;
        final WorldMovementSystem navigator = gameContext.systems().navigator;

        switch (sceneState) {
            case CLAPPERBOARD -> updateStateClapperboard(navigator);
            case CHASED_BY_GHOSTS -> updateStateChasedByGhosts(motor, navigator);
            case COMING_TOGETHER -> updateStateComingTogether(motor, navigator);
            case IN_HEAVEN -> updateStateInHeaven();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    private void initScene() {
        final GameVariantRenderConfig renderConfig = appContext().variants().currentVariant().config().renderConfig();
        final SpriteAnimationContainer spriteAnimations = appContext().ui().sprites().animations();
        final var spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();

        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.animations = renderConfig.createPacAnimations(spriteAnimations);

        msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.animations = renderConfig.createPacAnimations(spriteAnimations);

        inky = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GameModel.CYAN_GHOST_BASHFUL);

        pinky = renderConfig.createAnimatedGhost(gameContext(), spriteAnimations, GameModel.PINK_GHOST_SPEEDY);

        heart = new Actor();
        heart.animations = singleSpriteAnimation(spriteSheet.findSprite(SpriteID.HEART));

        clapperboard = new Clapperboard("1", "THEY MEET");
        clapperboard.position().set(tilesPx(3), tilesPx(10));
        clapperboard.startAnimation();
    }

    // Scene controller state machine

    private enum SceneState {CLAPPERBOARD, CHASED_BY_GHOSTS, COMING_TOGETHER, IN_HEAVEN}

    private SceneState sceneState;
    private final TickTimer sceneTimer = new TickTimer("Timer-MsPacMan_CutScene1");

    private void setState(SceneState state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard(WorldMovementSystem navigator) {
        clapperboard.tick();
        if (sceneTimer.atSecond(1)) {
            appContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_1);
        } else if (sceneTimer.hasExpired()) {
            enterStateChasedByGhosts(navigator);
        }
    }

    private void enterStateChasedByGhosts(WorldMovementSystem navigator) {
        pacMan.position().set(WorldMap.TS * (-2), UPPER_Y);
        pacMan.visibility().show();

        navigator.setMoveDir(pacMan, Direction.RIGHT);
        navigator.setSpeed(pacMan, SPEED_PAC_CHASING);

        pacMan.animations.select(CommonAnimationID.MR_PAC_MAN_MUNCHING);
        pacMan.animations.playSelected();

        inky.position().set(pacMan.position().x - 6 * WorldMap.TS, pacMan.position().y);
        inky.visibility().show();

        navigator.setSpeed(inky, SPEED_GHOST_CHASING);
        navigator.setMoveDir(inky, Direction.RIGHT);
        navigator.setWishDir(inky, Direction.RIGHT);

        inky.animations.select(CommonAnimationID.GHOST_NORMAL);
        inky.animations.playSelected();

        msPacMan.position().set(WorldMap.TS * 30, LOWER_Y);
        msPacMan.visibility().show();

        navigator.setMoveDir(msPacMan, Direction.LEFT);
        navigator.setSpeed(msPacMan, SPEED_PAC_CHASING);

        msPacMan.animations.select(CommonAnimationID.PAC_MUNCHING);
        msPacMan.animations.playSelected();

        pinky.position().set(msPacMan.position().x + 6 * WorldMap.TS, msPacMan.position().y);
        pinky.visibility().show();

        navigator.setMoveDir(pinky, Direction.LEFT);
        navigator.setWishDir(pinky, Direction.LEFT);
        navigator.setSpeed(pinky, SPEED_GHOST_CHASING);

        pinky.animations.select(CommonAnimationID.GHOST_NORMAL);
        pinky.animations.playSelected();

        setState(SceneState.CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
    }

    private void updateStateChasedByGhosts(MovementSystem motor, WorldMovementSystem navigator) {
        if (inky.position().x > WorldMap.TS * 30) {
            enterStateComingTogether(navigator);
        }
        else {
            List.of(pacMan, msPacMan, inky, pinky).forEach(motor::moveAccelerated);
        }
    }

    private void enterStateComingTogether(WorldMovementSystem navigator) {
        msPacMan.position().set(WorldMap.TS * (-3), MIDDLE_Y);
        navigator.setMoveDir(msPacMan, Direction.RIGHT);

        pinky.position().set(msPacMan.position().x - 5 * WorldMap.TS, msPacMan.position().y);
        navigator.setMoveDir(pinky, Direction.RIGHT);
        navigator.setWishDir(pinky, Direction.RIGHT);

        pacMan.position().set(WorldMap.TS * 31, MIDDLE_Y);
        navigator.setMoveDir(pacMan, Direction.LEFT);

        inky.position().set(pacMan.position().x + 5 * WorldMap.TS, pacMan.position().y);
        navigator.setMoveDir(inky, Direction.LEFT);
        navigator.setWishDir(inky, Direction.LEFT);

        setState(SceneState.COMING_TOGETHER, TickTimer.INDEFINITE);
    }

    private void updateStateComingTogether(MovementSystem motor, WorldMovementSystem navigator) {
        // Pac-Man and Ms. Pac-Man reach end position?
        if (pacMan.worldMovement().moveDir() == Direction.UP && pacMan.position().y < UPPER_Y) {
            enterStateInHeaven(navigator);
        }

        // Pac-Man and Ms. Pac-Man meet?
        else if (pacMan.worldMovement().moveDir() == Direction.LEFT && pacMan.position().x - msPacMan.position().x < WorldMap.TS * 2) {
            navigator.setMoveDir(pacMan, Direction.UP);
            navigator.setSpeed(pacMan, SPEED_RISING);
            navigator.setMoveDir(msPacMan, Direction.UP);
            navigator.setSpeed(msPacMan, SPEED_RISING);
        }

        // Inky and Pinky collide?
        else if (inky.worldMovement().moveDir() == Direction.LEFT && inky.position().x - pinky.position().x < WorldMap.TS * 2) {
            navigator.setMoveDir(inky, Direction.RIGHT);
            navigator.setWishDir(inky, Direction.RIGHT);
            navigator.setSpeed(inky, SPEED_GHOST_AFTER_COLLISION);
            inky.movement().velY -= 2.0f;
            inky.movement().setAcceleration(0, 0.4f);

            navigator.setMoveDir(pinky, Direction.LEFT);
            navigator.setWishDir(pinky, Direction.LEFT);
            navigator.setSpeed(pinky, SPEED_GHOST_AFTER_COLLISION);
            pinky.movement().velY -= 2.0f;
            pinky.movement().setAcceleration(0, 0.4f);
        }

        else {
            List.of(pacMan, msPacMan, inky, pinky).forEach(motor::moveAccelerated);

            // Collision with ground?
            if (inky.position().y > MIDDLE_Y) {
                inky.position().setY(MIDDLE_Y);
                inky.movement().setAcceleration(0, 0);
            }
            if (pinky.position().y > MIDDLE_Y) {
                pinky.position().setY(MIDDLE_Y);
                pinky.movement().setAcceleration(0, 0);
            }
        }
    }

    private void enterStateInHeaven(WorldMovementSystem navigator) {
        navigator.setSpeed(pacMan, 0);
        navigator.setMoveDir(pacMan, Direction.LEFT);

        pacMan.animations.stopSelected();
        pacMan.animations.resetSelected();

        navigator.setSpeed(msPacMan, 0);
        navigator.setMoveDir(msPacMan, Direction.RIGHT);

        msPacMan.animations.stopSelected();
        msPacMan.animations.resetSelected();

        inky.visibility().hide();
        pinky.visibility().hide();

        heart.position().set((pacMan.position().x + msPacMan.position().x) * 0.5f, pacMan.position().y - WorldMap.TS * 2);
        heart.visibility().show();

        setState(SceneState.IN_HEAVEN, 3L * GameConstants.SIMULATION_FPS);
    }

    private void updateStateInHeaven() {
        if (sceneTimer.hasExpired()) {
            gameState().triggerTimeout();
        }
    }
}