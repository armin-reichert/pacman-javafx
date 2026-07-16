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
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.world.WorldMap;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.ui.action.core.GameActionContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.sound.PacManGameSoundID;

import static de.amr.basics.spriteanim.SpriteAnimationAccessor.singleSpriteAnimation;
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

    public ArcadeMsPacMan_CutScene1(GameActionContext actionContext) {
        super(actionContext);
    }

    @Override
    public void onActivate() {
        initScene();
        setState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    public void onTick(GameContext gameContext) {
        switch (sceneState) {
            case CLAPPERBOARD -> updateStateClapperboard();
            case CHASED_BY_GHOSTS -> updateStateChasedByGhosts();
            case COMING_TOGETHER -> updateStateComingTogether();
            case IN_HEAVEN -> updateStateInHeaven();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    private void initScene() {
        final GameVariantConfig gameVariantConfig = actionContext().variants().currentVariant().config();
        final SpriteAnimationContainer spriteAnimations = actionContext().ui().sprites().animations();
        final var spriteSheet = (ArcadeMsPacMan_SpriteSheet) gameVariantConfig.spriteSheet();

        pacMan = ArcadePacMan_ActorFactory.createPacMan();
        pacMan.setAnimations(gameVariantConfig.createPacAnimations(spriteAnimations));

        msPacMan = ArcadeMsPacMan_ActorFactory.createMsPacMan();
        msPacMan.setAnimations(gameVariantConfig.createPacAnimations(spriteAnimations));

        inky = gameVariantConfig.createAnimatedGhost(spriteAnimations, GameModel.CYAN_GHOST_BASHFUL);

        pinky = gameVariantConfig.createAnimatedGhost(spriteAnimations, GameModel.PINK_GHOST_SPEEDY);

        heart = new Actor();
        heart.setAnimations(singleSpriteAnimation(spriteSheet.sprite(SpriteID.HEART)));

        clapperboard = new Clapperboard("1", "THEY MEET");
        clapperboard.setPosition(tilesPx(3), tilesPx(10));
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

    private void updateStateClapperboard() {
        clapperboard.tick();
        if (sceneTimer.atSecond(1)) {
            actionContext().ui().sounds().play(PacManGameSoundID.INTERMISSION_1);
        } else if (sceneTimer.hasExpired()) {
            enterStateChasedByGhosts();
        }
    }

    private void enterStateChasedByGhosts() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(WorldMap.TS * (-2), UPPER_Y);
        pacMan.setSpeed(SPEED_PAC_CHASING);
        pacMan.animations().select(ArcadeMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING);
        pacMan.animations().playSelected();
        pacMan.show();

        inky.setMoveDir(Direction.RIGHT);
        inky.setWishDir(Direction.RIGHT);
        inky.setPosition(pacMan.x() - 6 * WorldMap.TS, pacMan.y());
        inky.setSpeed(SPEED_GHOST_CHASING);
        inky.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        inky.animations().playSelected();
        inky.show();

        msPacMan.setMoveDir(Direction.LEFT);
        msPacMan.setPosition(WorldMap.TS * 30, LOWER_Y);
        msPacMan.setSpeed(SPEED_PAC_CHASING);
        msPacMan.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        msPacMan.animations().playSelected();
        msPacMan.show();

        pinky.setMoveDir(Direction.LEFT);
        pinky.setWishDir(Direction.LEFT);
        pinky.setPosition(msPacMan.x() + 6 * WorldMap.TS, msPacMan.y());
        pinky.setSpeed(SPEED_GHOST_CHASING);
        pinky.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
        pinky.animations().playSelected();
        pinky.show();

        setState(SceneState.CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
    }

    private void updateStateChasedByGhosts() {
        if (inky.x() > WorldMap.TS * 30) {
            enterStateComingTogether();
        }
        else {
            pacMan.move();
            msPacMan.move();
            inky.move();
            pinky.move();
        }
    }

    private void enterStateComingTogether() {
        msPacMan.setPosition(WorldMap.TS * (-3), MIDDLE_Y);
        msPacMan.setMoveDir(Direction.RIGHT);

        pinky.setPosition(msPacMan.x() - 5 * WorldMap.TS, msPacMan.y());
        pinky.setMoveDir(Direction.RIGHT);
        pinky.setWishDir(Direction.RIGHT);

        pacMan.setPosition(WorldMap.TS * 31, MIDDLE_Y);
        pacMan.setMoveDir(Direction.LEFT);

        inky.setPosition(pacMan.x() + 5 * WorldMap.TS, pacMan.y());
        inky.setMoveDir(Direction.LEFT);
        inky.setWishDir(Direction.LEFT);

        setState(SceneState.COMING_TOGETHER, TickTimer.INDEFINITE);
    }

    private void updateStateComingTogether() {
        // Pac-Man and Ms. Pac-Man reach end position?
        if (pacMan.moveDir() == Direction.UP && pacMan.y() < UPPER_Y) {
            enterStateInHeaven();
        }

        // Pac-Man and Ms. Pac-Man meet?
        else if (pacMan.moveDir() == Direction.LEFT && pacMan.x() - msPacMan.x() < WorldMap.TS * 2) {
            pacMan.setMoveDir(Direction.UP);
            pacMan.setSpeed(SPEED_RISING);
            msPacMan.setMoveDir(Direction.UP);
            msPacMan.setSpeed(SPEED_RISING);
        }

        // Inky and Pinky collide?
        else if (inky.moveDir() == Direction.LEFT && inky.x() - pinky.x() < WorldMap.TS * 2) {
            inky.setMoveDir(Direction.RIGHT);
            inky.setWishDir(Direction.RIGHT);
            inky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            inky.setVelY(inky.velY() - 2.0f);
            inky.setAcceleration(0, 0.4f);

            pinky.setMoveDir(Direction.LEFT);
            pinky.setWishDir(Direction.LEFT);
            pinky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            pinky.setVelY(pinky.velY() - 2.0f);
            pinky.setAcceleration(0, 0.4f);
        }

        else {
            pacMan.move();
            msPacMan.move();
            inky.move();
            pinky.move();

            // Collision with ground?
            if (inky.y() > MIDDLE_Y) {
                inky.setY(MIDDLE_Y);
                inky.setAcceleration(0, 0);
            }
            if (pinky.y() > MIDDLE_Y) {
                pinky.setY(MIDDLE_Y);
                pinky.setAcceleration(0, 0);
            }
        }
    }

    private void enterStateInHeaven() {
        pacMan.setSpeed(0);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.animations().stopSelected();
        pacMan.animations().resetSelected();

        msPacMan.setSpeed(0);
        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.animations().stopSelected();
        msPacMan.animations().resetSelected();

        inky.hide();
        pinky.hide();

        heart.setPosition((pacMan.x() + msPacMan.x()) * 0.5f, pacMan.y() - WorldMap.TS * 2);
        heart.show();

        setState(SceneState.IN_HEAVEN, 3L * GameConstants.SIMULATION_FPS);
    }

    private void updateStateInHeaven() {
        if (sceneTimer.hasExpired()) {
            gameState().triggerTimeout();
        }
    }
}