/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_ActorSpriteRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUDRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.uilib.animation.SingleSpriteActor;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_GHOST_NORMAL;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.*;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationManager.ANIM_PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.ui.CommonGameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them.
 */
public class TengenMsPacMan_CutScene1 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission.1";

    private static final int UPPER_LANE   = TS * 8;
    private static final int LOWER_LANE   = TS * 24;
    private static final int MIDDLE_LANE  = TS * 16;
    private static final int LEFT_BORDER  = TS;
    private static final int RIGHT_BORDER = TS * 30;

    private static final float SPEED_CHASING = 2.0f;
    private static final float SPEED_RISING = 1.0f;
    private static final float SPEED_GHOST_AFTER_COLLISION = 0.5f;

    private TengenMsPacMan_HUDRenderer hudRenderer;
    private TengenMsPacMan_ActorSpriteRenderer actorSpriteRenderer;

    private Clapperboard clapperboard;
    private SingleSpriteActor heart;

    private Pac pacMan;
    private Pac msPacMan;
    private Ghost inky;
    private Ghost pinky;

    private boolean collided;
    private int t;

    public TengenMsPacMan_CutScene1(GameUI ui) {
        super(ui);
    }

    @Override
    public void doInit() {
        GameUI_Config uiConfig = ui.currentConfig();
        var spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        hudRenderer = (TengenMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas);
        actorSpriteRenderer = (TengenMsPacMan_ActorSpriteRenderer) uiConfig.createActorSpriteRenderer(canvas);
        debugInfoRenderer = new DefaultDebugInfoRenderer(ui, canvas);
        bindRendererScaling(hudRenderer, actorSpriteRenderer, debugInfoRenderer);

        context().game().hudData().credit(false).score(false).levelCounter(true).livesCounter(false);

        actionBindings.bind(ACTION_LET_GAME_STATE_EXPIRE, ui.joypad().key(JoypadButton.START));

        clapperboard = new Clapperboard(spriteSheet, 1, "THEY MEET");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.setFont(actorSpriteRenderer.arcadeFontTS());

        msPacMan = createMsPacMan();
        msPacMan.setAnimations(uiConfig.createPacAnimations(msPacMan));

        pacMan = createPacMan();
        pacMan.setAnimations(uiConfig.createPacAnimations(pacMan));

        inky = createGhost(CYAN_GHOST_BASHFUL);
        inky.setAnimations(uiConfig.createGhostAnimations(inky));

        pinky = createGhost(PINK_GHOST_SPEEDY);
        pinky.setAnimations(uiConfig.createGhostAnimations(pinky));

        heart = new SingleSpriteActor(spriteSheet.sprite(SpriteID.HEART));

        t = -1;
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(MUSIC_ID);
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            clapperboard.show();
            clapperboard.startAnimation();
            ui.soundManager().play(MUSIC_ID);
        }
        else if (t == 130) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setPosition(LEFT_BORDER, UPPER_LANE);
            pacMan.setSpeed(SPEED_CHASING);
            pacMan.playAnimation(ANIM_PAC_MAN_MUNCHING);
            pacMan.show();

            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
            msPacMan.setSpeed(SPEED_CHASING);
            msPacMan.playAnimation(ANIM_PAC_MUNCHING);
            msPacMan.show();
        }
        else if (t == 160) {
            inky.setMoveDir(Direction.RIGHT);
            inky.setWishDir(Direction.RIGHT);
            inky.setPosition(LEFT_BORDER, UPPER_LANE);
            inky.setSpeed(SPEED_CHASING);
            inky.playAnimation(ANIM_GHOST_NORMAL);
            inky.show();

            pinky.setMoveDir(Direction.LEFT);
            pinky.setWishDir(Direction.LEFT);
            pinky.setPosition(RIGHT_BORDER, LOWER_LANE);
            pinky.setSpeed(SPEED_CHASING);
            pinky.playAnimation(ANIM_GHOST_NORMAL);
            pinky.show();

            collided = false;
        }
        else if (t == 400) {
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
        else if (t == 454) {
            pacMan.setMoveDir(Direction.UP);
            pacMan.setSpeed(SPEED_RISING);
            msPacMan.setMoveDir(Direction.UP);
            msPacMan.setSpeed(SPEED_RISING);
        }
        else if (t == 498) {
            collided = true;

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
        else if (t == 530) {
            inky.hide();
            pinky.hide();
            pacMan.setSpeed(0);
            pacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(0);
            msPacMan.setMoveDir(Direction.RIGHT);
        }
        else if (t == 545) {
            pacMan.animations().ifPresent(am -> {
                am.stop();
                am.reset();
            });
            msPacMan.animations().ifPresent(am -> {
                am.stop();
                am.reset();
            });
        }
        else if (t == 560) {
            heart.setPosition((pacMan.x() + msPacMan.x()) / 2, pacMan.y() - TS * (2));
            heart.show();
        }
        else if (t == 760) {
            pacMan.hide();
            msPacMan.hide();
            heart.hide();
        }
        else if (t == 775) {
            context().gameController().letCurrentGameStateExpire();
            return;
        }

        pacMan.move();
        msPacMan.move();
        inky.move();
        pinky.move();

        if (collided) {
            if (inky.y() > MIDDLE_LANE) {
                inky.setPosition(inky.x(), MIDDLE_LANE);
            }
            if (pinky.y() > MIDDLE_LANE) {
                pinky.setPosition(pinky.x(), MIDDLE_LANE);
            }
        }

        clapperboard.tick();
    }

    @Override
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    @Override
    public void drawHUD() {
        if (hudRenderer != null) {
            var game = context().<TengenMsPacMan_GameModel>game();
            if (game.mapCategory() != MapCategory.ARCADE) {
                hudRenderer.drawHUD(context(), game.hudData(), sizeInPx().minus(0, 2 * TS));
            }
        }
    }

    @Override
    public void drawSceneContent() {
        if (actorSpriteRenderer != null) {
            Stream.of(clapperboard, msPacMan, pacMan, inky, pinky, heart).forEach(actorSpriteRenderer::drawActor);
        }
    }
}