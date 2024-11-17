/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Entity;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.lib.Globals.t;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManTengenGameSceneConfig.NES_SIZE;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManTengenGameSceneConfig.NES_TILES_X;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManTengenGameSpriteSheet.HEART_SPRITE;

/**
 * Intermission scene 1: "They meet".
 * <p>
 * Pac-Man leads Inky and Ms. Pac-Man leads Pinky. Soon, the two Pac-Men are about to collide, they quickly move
 * upwards, causing Inky and Pinky to collide and vanish. Finally, Pac-Man and Ms. Pac-Man face each other at the top of
 * the screen and a big pink heart appears above them. (Played after round 2)
 *
 * @author Armin Reichert
 */
public class CutScene1 extends GameScene2D {

    static final int UPPER_LANE_Y = TS * 8;
    static final int LOWER_LANE_Y = TS * 24;
    static final int MIDDLE_LANE_Y = TS * 16;

    static final float SPEED_PAC_CHASING = 2f;
    static final float SPEED_PAC_RISING = 1f;
    static final float SPEED_GHOST_AFTER_COLLISION = 1f;
    static final float SPEED_GHOST_CHASING = 2f;

    private MediaPlayer music;
    private Pac pacMan;
    private Pac msPac;
    private Ghost inky;
    private Ghost pinky;
    private Entity heart;
    private ClapperboardAnimation clapAnimation;

    private int t = 0;

    @Override
    public void bindGameActions() {
        bind(context -> context.gameController().terminateCurrentState(), context.joypad().keyCombination(NES.Joypad.START));
    }

    @Override
    public void doInit() {
        t = 0;
        context.setScoreVisible(false);

        pacMan = new Pac();
        msPac = new Pac();
        inky = Ghost.inky();
        pinky = Ghost.pinky();
        heart = new Entity();

        music = context.sound().makeSound("intermission.1",1.0, false);

        var spriteSheet = (MsPacManTengenGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
        msPac.setAnimations(new PacAnimations(spriteSheet));
        pacMan.setAnimations(new PacAnimations(spriteSheet));
        inky.setAnimations(new GhostAnimations(spriteSheet, inky.id()));
        pinky.setAnimations(new GhostAnimations(spriteSheet, pinky.id()));

    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        if (t == 0) {
            clapAnimation = new ClapperboardAnimation();
            clapAnimation.start();
            music.play();
        }
        else if (t == 130) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setPosition(2*TS, UPPER_LANE_Y);
            pacMan.setSpeed(SPEED_PAC_CHASING);
            pacMan.selectAnimation(MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING);
            pacMan.animations().ifPresent(Animations::startCurrentAnimation);
            pacMan.show();

            msPac.setMoveDir(Direction.LEFT);
            msPac.setPosition(TS * (NES_TILES_X-2), LOWER_LANE_Y);
            msPac.setSpeed(SPEED_PAC_CHASING);
            msPac.selectAnimation(GameModel.ANIM_PAC_MUNCHING);
            msPac.animations().ifPresent(Animations::startCurrentAnimation);
            msPac.show();
        }
        else if (t == 160) {
            inky.setMoveAndWishDir(Direction.RIGHT);
            inky.setPosition(2*TS, UPPER_LANE_Y);
            inky.setSpeed(SPEED_GHOST_CHASING);
            inky.selectAnimation(GameModel.ANIM_GHOST_NORMAL);
            inky.startAnimation();
            inky.show();

            pinky.setMoveAndWishDir(Direction.LEFT);
            pinky.setPosition(TS * (NES_TILES_X - 2), LOWER_LANE_Y);
            pinky.setSpeed(SPEED_GHOST_CHASING);
            pinky.selectAnimation(GameModel.ANIM_GHOST_NORMAL);
            pinky.startAnimation();
            pinky.show();
        }
        else if (t == 400) {
            msPac.setPosition(TS, MIDDLE_LANE_Y);
            msPac.setMoveDir(Direction.RIGHT);

            pacMan.setPosition(TS * (NES_TILES_X - 1), MIDDLE_LANE_Y);
            pacMan.setMoveDir(Direction.LEFT);

            pinky.setPosition(msPac.position().minus(TS * 11, 0));
            pinky.setMoveAndWishDir(Direction.RIGHT);

            inky.setPosition(pacMan.position().plus(TS * 11, 0));
            inky.setMoveAndWishDir(Direction.LEFT);
        }
        else if (t == 455) {
            pacMan.setMoveDir(Direction.UP);
            pacMan.setSpeed(SPEED_PAC_RISING);
            msPac.setMoveDir(Direction.UP);
            msPac.setSpeed(SPEED_PAC_RISING);
        }
        else if (t == 500) {
            inky.setMoveAndWishDir(Direction.RIGHT);
            inky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            //inky.setVelocity(inky.velocity().minus(0, 2.0f));
            //inky.setAcceleration(0, 0.4f);

            pinky.setMoveAndWishDir(Direction.LEFT);
            pinky.setSpeed(SPEED_GHOST_AFTER_COLLISION);
            //pinky.setVelocity(pinky.velocity().minus(0, 2.0f));
            //pinky.setAcceleration(0, 0.4f);
        }
        else if (t == 530) {
            inky.hide();
            pinky.hide();
            pacMan.setSpeed(0);
            pacMan.setMoveDir(Direction.LEFT);
            msPac.setSpeed(0);
            msPac.setMoveDir(Direction.RIGHT);
        }
        else if (t == 545) {
            pacMan.animations().ifPresent(Animations::stopCurrentAnimation);
            pacMan.animations().ifPresent(Animations::resetCurrentAnimation);
            msPac.animations().ifPresent(Animations::stopCurrentAnimation);
            msPac.animations().ifPresent(Animations::resetCurrentAnimation);
        }
        else if (t == 560) {
            heart.setPosition((pacMan.posX() + msPac.posX()) / 2, pacMan.posY() - TS * (2));
            heart.show();
        }
        else if (t == 760) {
            pacMan.hide();
            msPac.hide();
            heart.hide();
        }
        else if (t == 775) {
            context.gameController().terminateCurrentState();
            return;
        }

        pacMan.move();
        msPac.move();
        inky.move();
        pinky.move();

        clapAnimation.tick();
        ++t;
    }

    @Override
    public Vector2f size() {
        return NES_SIZE;
    }

    @Override
    public void drawSceneContent(GameRenderer renderer) {
        String assetPrefix = assetPrefix(context.gameVariant());
        Color color = context.assets().color(assetPrefix + ".color.clapperboard");
        var r = (MsPacManTengenGameRenderer) renderer;
        r.setLevelNumberBoxesVisible(false);
        r.drawClapperBoard(clapAnimation, "THEY MEET", 1, r.scaledArcadeFont(TS), color, t(3), t(10));
        r.drawAnimatedEntity(msPac);
        r.drawAnimatedEntity(pacMan);
        r.drawAnimatedEntity(inky);
        r.drawAnimatedEntity(pinky);
        r.drawSprite(heart, HEART_SPRITE);
        if (context.game().level().isPresent()) {
            // avoid exception in cut scene test mode
            r.drawLevelCounter(context, size());
        }
    }

    @Override
    protected void drawDebugInfo(GameRenderer renderer) {
        renderer.drawTileGrid(size());
        renderer.ctx().setFill(Color.WHITE);
        renderer.ctx().setFont(Font.font(20));
        renderer.ctx().fillText("Tick " + t, 20, 20);
    }
}