/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.PacManGames_UIConfig;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.media.MediaPlayer;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationMap.ANIM_PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 *
 * @author Armin Reichert
 */
public class TengenMsPacMan_CutScene2 extends GameScene2D {

    private static final int UPPER_LANE = TS * 8;
    private static final int LOWER_LANE = TS * 22;
    private static final int MIDDLE_LANE = TS * 10;
    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * 30;

    private MediaPlayer music;
    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;

    private int t;

    @Override
    public void doInit() {
        t = -1;

        theGame().hud().hideScore();
        theGame().hud().showLevelCounter();
        theGame().hud().hideLivesCounter();

        bindActionToKeyCombination(ACTION_LET_GAME_STATE_EXPIRE, theJoypad().key(JoypadButton.START));
        music = theSound().createSound("intermission.2");

        PacManGames_UIConfig config = theUI().configuration();
        var spriteSheet = (TengenMsPacMan_SpriteSheet) config.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 2, "THE CHASE");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.setFont(arcadeFont8());
        msPacMan = createMsPacMan();
        msPacMan.setAnimations(config.createPacAnimations(msPacMan));
        pacMan = createPacMan();
        pacMan.setAnimations(config.createPacAnimations(pacMan));
    }

    @Override
    protected void doEnd() {
        music.stop();
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            clapperboard.show();
            clapperboard.startAnimation();
            music.play();
        }
        else if (t == 270) {
            msPacMan.setPosition(LEFT_BORDER, UPPER_LANE);
            msPacMan.setMoveDir(Direction.RIGHT);
            msPacMan.playAnimation(ANIM_PAC_MUNCHING);
            msPacMan.setSpeed(2.0f);
            msPacMan.show();
        }
        else if (t == 320) {
            pacMan.setMoveDir(Direction.RIGHT);
            pacMan.playAnimation(ANIM_PAC_MAN_MUNCHING);
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
            theGameController().letCurrentGameStateExpire();
            return;
        }
        pacMan.move();
        msPacMan.move();
        clapperboard.tick();
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    public TengenMsPacMan_GameRenderer gr() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void drawSceneContent() {
        gr().drawVerticalSceneBorders();
        gr().drawActor(clapperboard);
        gr().drawActor(msPacMan);
        gr().drawActor(pacMan);
    }
}