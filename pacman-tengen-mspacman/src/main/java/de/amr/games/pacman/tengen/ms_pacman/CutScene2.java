/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.model.actors.Animations;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.scene.GameScene2D;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.model.actors.Animations.ANIM_MR_PACMAN_MUNCHING;
import static de.amr.games.pacman.model.actors.Animations.ANIM_PAC_MUNCHING;
import static de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengenConfiguration.NES_SIZE;
import static de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengenConfiguration.NES_TILES;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 *
 * @author Armin Reichert
 */
public class CutScene2 extends GameScene2D {

    static final int CLAP_TILE_X = TS * 3;
    static final int CLAP_TILE_Y = TS * 10;

    static final int UPPER_LANE = TS * 8;
    static final int LOWER_LANE = TS * 22;
    static final int MIDDLE_LANE = TS * 10;

    static final int LEFT_BORDER = TS;
    static final int RIGHT_BORDER = TS * (NES_TILES.x() - 2);

    private int t;
    private Pac pacMan;
    private Pac msPacMan;
    private MediaPlayer music;
    private ClapperboardAnimation clapAnimation;

    @Override
    public void bindGameActions() {
        bind(context -> context.gameController().terminateCurrentState(), context.joypadKeys().key(NES_JoypadButton.BTN_START));
    }

    @Override
    public void doInit() {
        t = -1;
        context.setScoreVisible(false);
        pacMan = new Pac();
        msPacMan = new Pac();
        var spriteSheet = (MsPacManGameTengenSpriteSheet) context.currentGameConfig().spriteSheet();
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
        t += 1;
        if (t == 0) {
            clapAnimation = new ClapperboardAnimation();
            clapAnimation.start();
            music.play();
        }
        else if (t == 270) {
            msPacMan.setPosition(LEFT_BORDER, UPPER_LANE);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.selectAnimation(ANIM_PAC_MUNCHING);
            msPacMan.optAnimations().ifPresent(Animations::startCurrentAnimation);
            msPacMan.setSpeed(2.0f);
            msPacMan.show();
        }
        else if (t == 320) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.selectAnimation(ANIM_MR_PACMAN_MUNCHING);
            pacMan.optAnimations().ifPresent(Animations::startCurrentAnimation);
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
    }

    @Override
    public Vector2f size() {
        return NES_SIZE.toVector2f();
    }

    @Override
    public void drawSceneContent() {
        String assetKeyPrefix = context.currentGameConfig().assetKeyPrefix();
        Color color = context.assets().color(assetKeyPrefix + ".color.clapperboard"); //TODO check
        var r = (MsPacManGameTengenRenderer) gr;
        r.drawSceneBorderLines();
        r.setLevelNumberBoxesVisible(false);
        r.drawClapperBoard(clapAnimation, "THE CHASE", 2,
            r.scaledArcadeFont(TS), color, CLAP_TILE_X, CLAP_TILE_Y);
        r.drawAnimatedEntity(msPacMan);
        r.drawAnimatedEntity(pacMan);
        if (context.game().level().isPresent()) { // avoid exception in cut scene test mode
            r.drawLevelCounter(context, size().x() - 4 * TS, size().y() - 3 * TS);
        }
    }

    @Override
    protected void drawDebugInfo() {
        gr.drawTileGrid(size().x(), size().y());
        gr.ctx().setFill(Color.WHITE);
        gr.ctx().setFont(DEBUG_FONT);
        gr.ctx().fillText("Tick " + t, 20, 20);
    }
}