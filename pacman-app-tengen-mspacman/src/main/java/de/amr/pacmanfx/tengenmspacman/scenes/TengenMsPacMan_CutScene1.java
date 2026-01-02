/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengenmspacman.scenes;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_HeadsUpDisplay;
import de.amr.pacmanfx.tengenmspacman.model.actors.MsPacMan;
import de.amr.pacmanfx.tengenmspacman.model.actors.PacMan;
import de.amr.pacmanfx.tengenmspacman.rendering.SpriteID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.animation.SingleSpriteNoAnimation;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them.
 */
public class TengenMsPacMan_CutScene1 extends GameScene2D {

    private static final int UPPER_LANE   = TS * 8;
    private static final int LOWER_LANE   = TS * 24;
    private static final int MIDDLE_LANE  = TS * 16;
    private static final int LEFT_BORDER  = TS;
    private static final int RIGHT_BORDER = TS * 30;

    private static final float SPEED_CHASING = 2.0f;
    private static final float SPEED_RISING = 1.0f;
    private static final float SPEED_AFTER_COLLISION = 0.5f;

    private Clapperboard clapperboard;
    private Actor heart;
    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;

    private boolean collided;

    public TengenMsPacMan_CutScene1() {}

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    public Actor heart() {
        return heart;
    }

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

    @Override
    public void doInit(Game game) {
        final var tengenGame = (TengenMsPacMan_GameModel) game;
        final GameUI_Config uiConfig = ui.currentConfig();
        final var spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        final var hud = (TengenMsPacMan_HeadsUpDisplay) game.hud();
        if (tengenGame.mapCategory() == MapCategory.ARCADE) {
            hud.hide();
        } else {
            hud.gameOptions(false).score(false).levelCounter(true).livesCounter(false).show();
        }

        actionBindings.setKeyCombination(ACTION_LET_GAME_STATE_EXPIRE, GameUI.JOYPAD.key(JoypadButton.START));

        clapperboard = new Clapperboard(spriteSheet, 1, "THEY MEET");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = new MsPacMan();
        msPacMan.setAnimationManager(uiConfig.createPacAnimations());
        msPacMan.setMoveDir(Direction.LEFT);
        msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
        msPacMan.setSpeed(0);

        pacMan = new PacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());
        pacMan.setMoveDir(Direction.RIGHT);
        pacMan.setPosition(LEFT_BORDER, UPPER_LANE);
        pacMan.setSpeed(0);

        inky = uiConfig.createGhostWithAnimations(CYAN_GHOST_BASHFUL);
        inky.setMoveDir(Direction.RIGHT);
        inky.setWishDir(Direction.RIGHT);
        inky.setPosition(LEFT_BORDER, UPPER_LANE);
        inky.setSpeed(0);

        pinky = uiConfig.createGhostWithAnimations(PINK_GHOST_SPEEDY);
        pinky.setMoveDir(Direction.LEFT);
        pinky.setWishDir(Direction.LEFT);
        pinky.setPosition(RIGHT_BORDER, LOWER_LANE);
        pinky.setSpeed(0);

        heart = new Actor();
        heart.setAnimationManager(new SingleSpriteNoAnimation(spriteSheet.sprite(SpriteID.HEART)));

        collided = false;

        soundManager().play(SoundID.INTERMISSION_1);
    }

    @Override
    protected void doEnd(Game game) {
        soundManager().stop(SoundID.INTERMISSION_1);
    }

    @Override
    public void update(Game game) {
        final int tick = (int) context().currentGame().control().state().timer().tickCount();

        clapperboard.tick();

        pacMan.move();
        msPacMan.move();
        inky.move();
        pinky.move();

        if (collided) {
            if (inky.y() > MIDDLE_LANE) {
                inky.setY(MIDDLE_LANE);
            }
            if (pinky.y() > MIDDLE_LANE) {
                pinky.setY(MIDDLE_LANE);
            }
        }

        switch (tick) {
            case 130 -> {
                pacMan.setSpeed(SPEED_CHASING);
                pacMan.playAnimation(TengenMsPacMan_AnimationID.ANIM_PAC_MAN_MUNCHING);
                pacMan.show();

                msPacMan.setSpeed(SPEED_CHASING);
                msPacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                msPacMan.show();
            }
            case 160 -> {
                inky.setSpeed(SPEED_CHASING);
                inky.playAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
                inky.show();

                pinky.setSpeed(SPEED_CHASING);
                pinky.playAnimation(CommonAnimationID.ANIM_GHOST_NORMAL);
                pinky.show();
            }
            case 400 -> {
                msPacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
                msPacMan.setMoveDir(Direction.RIGHT);

                pacMan.setPosition(RIGHT_BORDER, MIDDLE_LANE);
                pacMan.setMoveDir(Direction.LEFT);

                pinky.setPosition(msPacMan.x() - TS * 11, msPacMan.y());
                pinky.setMoveDir(Direction.RIGHT);
                pinky.setWishDir(Direction.RIGHT);

                inky.setPosition(pacMan.x() + TS * 11, pacMan.y());
                inky.setMoveDir(Direction.LEFT);
                inky.setWishDir(Direction.LEFT);
            }
            case 454 -> {
                pacMan.setMoveDir(Direction.UP);
                pacMan.setSpeed(SPEED_RISING);
                msPacMan.setMoveDir(Direction.UP);
                msPacMan.setSpeed(SPEED_RISING);
            }
            case 498 -> {
                collided = true;

                inky.setMoveDir(Direction.RIGHT);
                inky.setWishDir(Direction.RIGHT);
                inky.setSpeed(SPEED_AFTER_COLLISION);
                inky.setVelocity(inky.velocity().minus(0, 2.0f));
                inky.setAcceleration(0, 0.4f);

                pinky.setMoveDir(Direction.LEFT);
                pinky.setWishDir(Direction.LEFT);
                pinky.setSpeed(SPEED_AFTER_COLLISION);
                pinky.setVelocity(pinky.velocity().minus(0, 2.0f));
                pinky.setAcceleration(0, 0.4f);
            }
            case 530 -> {
                inky.hide();
                pinky.hide();
                pacMan.setSpeed(0);
                pacMan.setMoveDir(Direction.LEFT);
                msPacMan.setSpeed(0);
                msPacMan.setMoveDir(Direction.RIGHT);
            }
            case 545 -> {
                pacMan.optAnimationManager().ifPresent(AnimationManager::reset);
                msPacMan.optAnimationManager().ifPresent(AnimationManager::reset);
            }
            case 560 -> {
                heart.setPosition(0.5f * (pacMan.x() + msPacMan.x()), pacMan.y() - TS(2));
                heart.show();
            }
            case 760 -> {
                pacMan.hide();
                msPacMan.hide();
                heart.hide();
            }
            case 775 -> game.control().terminateCurrentGameState();
        }
    }

    @Override
    public Vector2i unscaledSize() { return NES_SIZE_PX; }
}