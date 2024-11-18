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
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManTengenGameSceneConfig.*;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 *
 * @author Armin Reichert
 */
public class CutScene2 extends GameScene2D {

    static final int UPPER_LANE = TS * 8;
    static final int LOWER_LANE = TS * 22;
    static final int MIDDLE_LANE = TS * 10;

    static final int LEFT_BORDER = TS;
    static final int RIGHT_BORDER = TS * (NES_TILES_X - 2);

    private int t;
    private Pac pacMan;
    private Pac msPacMan;
    private MediaPlayer music;
    private ClapperboardAnimation clapAnimation;

    @Override
    public void bindGameActions() {
        bind(context -> context.gameController().terminateCurrentState(), context.joypad().keyCombination(NES.Joypad.START));
    }

    @Override
    public void doInit() {
        t = 0;
        context.setScoreVisible(false);
        pacMan = new Pac();
        msPacMan = new Pac();
        var spriteSheet = (MsPacManTengenGameSpriteSheet) context.currentGameSceneConfig().spriteSheet();
        msPacMan.setAnimations(new PacAnimations(spriteSheet));
        pacMan.setAnimations(new PacAnimations(spriteSheet));
        music = context.sound().makeSound("intermission.2",1.0, false);
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
        else if (t == 270) {
            msPacMan.setPosition(LEFT_BORDER, UPPER_LANE);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.selectAnimation(GameModel.ANIM_PAC_MUNCHING);
            msPacMan.animations().ifPresent(Animations::startCurrentAnimation);
            msPacMan.setSpeed(2.0f);
            msPacMan.show();
        }
        else if (t == 320) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.selectAnimation(MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING);
            pacMan.animations().ifPresent(Animations::startCurrentAnimation);
            pacMan.setPosition(LEFT_BORDER, UPPER_LANE);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);
            pacMan.show();
        }
        else if (t == 520) {
            pacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(2.0f);
        }
        else if (t == 570) {
            msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(2.0f);
        }
        else if (t == 780) {
            msPacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(2.0f);
        }
        else if (t == 830) {
            pacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(2.0f);
        }
        else if (t == 1040) {
            pacMan.setPosition(RIGHT_BORDER, UPPER_LANE);
            pacMan.setMoveDir(Direction.LEFT);
            pacMan.setSpeed(4.0f); //TODO correct?
        }
        else if (t == 1055) {
            msPacMan.setPosition(RIGHT_BORDER, UPPER_LANE);
            msPacMan.setMoveDir(Direction.LEFT);
            msPacMan.setSpeed(4.0f);
        }
        else if (t == 1105) {
            msPacMan.setPosition(LEFT_BORDER, LOWER_LANE);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.setSpeed(4.0f);
        }
        else if (t == 1120) {
            pacMan.setPosition(LEFT_BORDER, LOWER_LANE);
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.setSpeed(4.0f);
        }
        else if (t == 1380) {
            context.gameController().terminateCurrentState();
            return;
        }
        pacMan.move();
        msPacMan.move();
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
        Color color = context.assets().color(assetPrefix + ".color.clapperboard"); //TODO check
        var r = (MsPacManTengenGameRenderer) renderer;
        r.setLevelNumberBoxesVisible(false);
        r.drawClapperBoard(clapAnimation, "THE CHASE", 2, r.scaledArcadeFont(TS), color, t(3), t(10));
        r.drawAnimatedEntity(msPacMan);
        r.drawAnimatedEntity(pacMan);
        if (context.game().level().isPresent()) { // avoid exception in cut scene test mode
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