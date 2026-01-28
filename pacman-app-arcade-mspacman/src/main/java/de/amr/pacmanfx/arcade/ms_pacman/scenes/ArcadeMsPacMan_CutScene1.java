/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_PacAnimations;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.arcade.pacman.model.ArcadePacMan_GameModel;
import de.amr.pacmanfx.lib.TickTimer;
import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.SingleSpriteNoAnimation;

import static de.amr.pacmanfx.Globals.*;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 */
public class ArcadeMsPacMan_CutScene1 extends GameScene2D {

    private static final int UPPER_LANE_Y  = TS * 12;
    private static final int MIDDLE_LANE_Y = TS * 18;
    private static final int LOWER_LANE_Y  = TS * 24;

    private static final float SPEED_PAC_CHASING = 1.125f;
    private static final float SPEED_PAC_RISING = 0.75f;
    private static final float SPEED_GHOST_AFTER_COLLISION = 0.3f;
    private static final float SPEED_GHOST_CHASING = 1.25f;

    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;
    private Actor heart;
    private Clapperboard clapperboard;

    public ArcadeMsPacMan_CutScene1() {}

    public Pac pacMan() {
        return pacMan;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    public Ghost inky() {
        return inky;
    }

    public Ghost pinky() {
        return pinky;
    }

    public Actor heart() {
        return heart;
    }

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    @Override
    public void doInit(Game game) {
        final GameUI_Config uiConfig = ui.currentConfig();
        final var spriteSheet = (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        game.hud().credit(false).score(true).levelCounter(true).livesCounter(false).show();

        pacMan = ArcadePacMan_GameModel.createPacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());

        msPacMan = ArcadeMsPacMan_GameModel.createMsPacMan();
        msPacMan.setAnimationManager(uiConfig.createPacAnimations());

        inky = uiConfig.createGhostWithAnimations(CYAN_GHOST_BASHFUL);

        pinky = uiConfig.createGhostWithAnimations(PINK_GHOST_SPEEDY);

        heart = new Actor();
        heart.setAnimationManager(new SingleSpriteNoAnimation(spriteSheet.sprite(SpriteID.HEART)));

        clapperboard = new Clapperboard("1", "THEY MEET");
        clapperboard.setPosition(TS(3), TS(10));
        clapperboard.startAnimation();

        setState(SceneState.CLAPPERBOARD, 120);
    }

    @Override
    protected void doEnd(Game game) {}

    @Override
    public void update(Game game) {
        switch (sceneState) {
            case CLAPPERBOARD -> updateStateClapperboard();
            case CHASED_BY_GHOSTS -> updateStateChasedByGhosts();
            case COMING_TOGETHER -> updateStateComingTogether();
            case IN_HEAVEN -> updateStateInHeaven();
            default -> throw new IllegalStateException("Illegal scene state: " + sceneState);
        }
        sceneTimer.doTick();
    }

    // Scene controller state machine

    private enum SceneState {CLAPPERBOARD, CHASED_BY_GHOSTS, COMING_TOGETHER, IN_HEAVEN}

    private SceneState sceneState;
    private final TickTimer sceneTimer = new TickTimer("MsPacMan_CutScene1");

    private void setState(SceneState state, long ticks) {
        sceneState = state;
        sceneTimer.reset(ticks);
        sceneTimer.start();
    }

    private void updateStateClapperboard() {
        clapperboard.tick();
        if (sceneTimer.atSecond(1)) {
            ui.soundManager().play(SoundID.INTERMISSION_1);
        } else if (sceneTimer.hasExpired()) {
            enterStateChasedByGhosts();
        }
    }

    private void enterStateChasedByGhosts() {
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(TS * (-2), UPPER_LANE_Y);
        pacMan.setSpeed(SPEED_PAC_CHASING);
        pacMan.playAnimation(ArcadeMsPacMan_PacAnimations.AnimationID.PAC_MAN_MUNCHING);
        pacMan.show();

        inky.setMoveDir(Direction.RIGHT);
        inky.setWishDir(Direction.RIGHT);
        inky.setPosition(pacMan.x() - 6 * TS, pacMan.y());
        inky.setSpeed(SPEED_GHOST_CHASING);
        inky.playAnimation(Ghost.AnimationID.GHOST_NORMAL);
        inky.show();

        msPacMan.setMoveDir(Direction.LEFT);
        msPacMan.setPosition(TS * 30, LOWER_LANE_Y);
        msPacMan.setSpeed(SPEED_PAC_CHASING);
        msPacMan.playAnimation(Pac.AnimationID.PAC_MUNCHING);
        msPacMan.show();

        pinky.setMoveDir(Direction.LEFT);
        pinky.setWishDir(Direction.LEFT);
        pinky.setPosition(msPacMan.x() + 6 * TS, msPacMan.y());
        pinky.setSpeed(SPEED_GHOST_CHASING);
        pinky.playAnimation(Ghost.AnimationID.GHOST_NORMAL);
        pinky.show();

        setState(SceneState.CHASED_BY_GHOSTS, TickTimer.INDEFINITE);
    }

    private void updateStateChasedByGhosts() {
        if (inky.x() > TS * 30) {
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
        msPacMan.setPosition(TS * (-3), MIDDLE_LANE_Y);
        msPacMan.setMoveDir(Direction.RIGHT);

        pinky.setPosition(msPacMan.x() - 5 * TS, msPacMan.y());
        pinky.setMoveDir(Direction.RIGHT);
        pinky.setWishDir(Direction.RIGHT);

        pacMan.setPosition(TS * 31, MIDDLE_LANE_Y);
        pacMan.setMoveDir(Direction.LEFT);

        inky.setPosition(pacMan.x() + 5 * TS, pacMan.y());
        inky.setMoveDir(Direction.LEFT);
        inky.setWishDir(Direction.LEFT);

        setState(SceneState.COMING_TOGETHER, TickTimer.INDEFINITE);
    }

    private void updateStateComingTogether() {
        // Pac-Man and Ms. Pac-Man reach end position?
        if (pacMan.moveDir() == Direction.UP && pacMan.y() < UPPER_LANE_Y) {
            enterStateInHeaven();
        }

        // Pac-Man and Ms. Pac-Man meet?
        else if (pacMan.moveDir() == Direction.LEFT && pacMan.x() - msPacMan.x() < TS * 2) {
            pacMan.setMoveDir(Direction.UP);
            pacMan.setSpeed(SPEED_PAC_RISING);
            msPacMan.setMoveDir(Direction.UP);
            msPacMan.setSpeed(SPEED_PAC_RISING);
        }

        // Inky and Pinky collide?
        else if (inky.moveDir() == Direction.LEFT && inky.x() - pinky.x() < TS * 2) {
            inky.setMoveDir(Direction.RIGHT);
            inky.setWishDir(Direction.RIGHT);
            inky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            inky.setVelocity(inky.velocity().minus(0, 2.0f));
            inky.setAcceleration(0, 0.4f);

            pinky.setMoveDir(Direction.LEFT);
            pinky.setWishDir(Direction.LEFT);
            pinky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            pinky.setVelocity(pinky.velocity().minus(0, 2.0f));
            pinky.setAcceleration(0, 0.4f);
        }

        else {
            pacMan.move();
            msPacMan.move();
            inky.move();
            pinky.move();

            // Collision with ground?
            if (inky.y() > MIDDLE_LANE_Y) {
                inky.setY(MIDDLE_LANE_Y);
                inky.setAcceleration(Vector2f.ZERO);
            }
            if (pinky.y() > MIDDLE_LANE_Y) {
                pinky.setY(MIDDLE_LANE_Y);
                pinky.setAcceleration(Vector2f.ZERO);
            }
        }
    }

    private void enterStateInHeaven() {
        pacMan.setSpeed(0);
        pacMan.setMoveDir(Direction.LEFT);
        pacMan.optAnimationManager().ifPresent(am -> {
            am.stop();
            am.reset();
        });

        msPacMan.setSpeed(0);
        msPacMan.setMoveDir(Direction.RIGHT);
        msPacMan.optAnimationManager().ifPresent(am -> {
            am.stop();
            am.reset();
        });

        inky.hide();
        pinky.hide();

        heart.setPosition((pacMan.x() + msPacMan.x()) * 0.5f, pacMan.y() - TS * 2);
        heart.show();

        setState(SceneState.IN_HEAVEN, 3 * NUM_TICKS_PER_SEC);
    }

    private void updateStateInHeaven() {
        if (sceneTimer.hasExpired()) {
            context().currentGame().control().terminateCurrentGameState();
        }
    }
}