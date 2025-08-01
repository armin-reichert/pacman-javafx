/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui._2d.GameScene2D;

import java.util.List;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createMsPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel.createPacMan;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationMap.ANIM_PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 */
public class TengenMsPacMan_CutScene2 extends GameScene2D {

    private static final String MUSIC_ID = "audio.intermission.2";

    private static final int UPPER_LANE = TS * 8;
    private static final int LOWER_LANE = TS * 22;
    private static final int MIDDLE_LANE = TS * 10;
    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * 30;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;

    private int t;

    public TengenMsPacMan_CutScene2(GameUI ui) {
        super(ui);
    }
    
    @Override
    public void doInit() {
        t = -1;

        gameContext().theGame().theHUD().credit(false).score(false).levelCounter(true).livesCounter(false);

        actionBindings.bind(ACTION_LET_GAME_STATE_EXPIRE, ui.theJoypad().key(JoypadButton.START));

        GameUI_Config config = ui.theConfiguration();
        var spriteSheet = (TengenMsPacMan_SpriteSheet) config.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 2, "THE CHASE");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.setFont(scaledArcadeFont8());
        msPacMan = createMsPacMan(null);
        msPacMan.setAnimations(config.createPacAnimations(msPacMan));
        pacMan = createPacMan(null);
        pacMan.setAnimations(config.createPacAnimations(pacMan));
    }

    @Override
    protected void doEnd() {
        ui.theSound().stop(MUSIC_ID);
    }

    @Override
    public void update() {
        t += 1;
        if (t == 0) {
            clapperboard.show();
            clapperboard.startAnimation();
            ui.theSound().play(MUSIC_ID);
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
            gameContext().theGameController().letCurrentGameStateExpire();
            return;
        }
        pacMan.move();
        msPacMan.move();
        clapperboard.tick();
    }

    @Override
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    @SuppressWarnings("unchecked")
    @Override
    public TengenMsPacMan_GameRenderer renderer() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void draw() {
        gameRenderer.setScaling(scaling());
        clear();
        drawSceneContent();
        if (debugInfoVisibleProperty.get()) {
            drawDebugInfo();
        }
        // draw HUD only for non-Arcade map mode
        var game = gameContext().<TengenMsPacMan_GameModel>theGame();
        if (game.mapCategory() != MapCategory.ARCADE) {
            gameRenderer.drawHUD(gameContext(), game.theHUD(), sizeInPx().minus(0, 2 * TS), ui.theGameClock().tickCount());
        }
    }

    @Override
    public void drawSceneContent() {
        renderer().drawVerticalSceneBorders();
        renderer().drawActors(List.of(clapperboard, msPacMan, pacMan));
    }
}