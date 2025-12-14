/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.math.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_HUD;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.MsPacMan;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.PacMan;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_CutScene2_Renderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUD_Renderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.ui._2d.GameScene2D_Renderer.configureRendererForGameScene;
import static de.amr.pacmanfx.ui.action.CommonGameActions.ACTION_LET_GAME_STATE_EXPIRE;

/**
 * Intermission scene 2: "The chase".
 * <p>
 * Pac-Man and Ms. Pac-Man chase each other across the screen over and over. After three turns, they both rapidly run
 * from left to right and right to left. (Played after round 5)
 */
public class TengenMsPacMan_CutScene2 extends GameScene2D {

    private static final int UPPER_LANE = TS * 8;
    private static final int LOWER_LANE = TS * 22;
    private static final int MIDDLE_LANE = TS * 10;
    private static final int LEFT_BORDER = TS;
    private static final int RIGHT_BORDER = TS * 30;

    private TengenMsPacMan_HUD_Renderer hudRenderer;
    private TengenMsPacMan_CutScene2_Renderer sceneRenderer;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;

    public TengenMsPacMan_CutScene2(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        if (context().<TengenMsPacMan_GameModel>currentGame().mapCategory() == MapCategory.ARCADE) {
            hudRenderer = null;
        }
        else {
            hudRenderer = configureRendererForGameScene(
                (TengenMsPacMan_HUD_Renderer) uiConfig.createHUDRenderer(canvas),
                this);
            hudRenderer.setOffsetY(-2*TS);
        }

        sceneRenderer = configureRendererForGameScene(
            new TengenMsPacMan_CutScene2_Renderer(this, canvas), this
        );
    }

    @Override
    public TengenMsPacMan_HUD_Renderer hudRenderer() {
        return hudRenderer;
    }

    public Clapperboard clapperboard() {
        return clapperboard;
    }

    public Pac pacMan() {
        return pacMan;
    }

    public Pac msPacMan() {
        return msPacMan;
    }

    @Override
    public TengenMsPacMan_CutScene2_Renderer sceneRenderer() {
        return sceneRenderer;
    }


    @Override
    public void doInit(Game game) {
        TengenMsPacMan_HUD hud = (TengenMsPacMan_HUD) game.hud();
        hud.credit(false).score(false).levelCounter(true).livesCounter(false);
        hud.showGameOptions(false);

        actionBindings.useKeyCombination(ACTION_LET_GAME_STATE_EXPIRE, GameUI.JOYPAD.key(JoypadButton.START));

        final GameUI_Config uiConfig = ui.currentConfig();
        final var spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 2, "THE CHASE");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = new MsPacMan();
        msPacMan.setAnimationManager(uiConfig.createPacAnimations());

        pacMan = new PacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());

        ui.soundManager().play(SoundID.INTERMISSION_2);
    }

    @Override
    protected void doEnd(Game game) {
        ui.soundManager().stop(SoundID.INTERMISSION_2);
    }

    @Override
    public void update(Game game) {
        final int tick = (int) game.control().state().timer().tickCount();

        pacMan.move();
        msPacMan.move();
        clapperboard.tick();

        switch (tick) {
            case 270 -> {
                msPacMan.setPosition(LEFT_BORDER, UPPER_LANE);
                msPacMan.setMoveDir(Direction.RIGHT);
                msPacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                msPacMan.setSpeed(2.0f);
                msPacMan.show();
            }
            case 320 -> {
                pacMan.setMoveDir(Direction.RIGHT);
                pacMan.playAnimation(TengenMsPacMan_UIConfig.AnimationID.ANIM_PAC_MAN_MUNCHING);
                pacMan.setPosition(LEFT_BORDER, UPPER_LANE);
                pacMan.setMoveDir(Direction.RIGHT);
                pacMan.setSpeed(2.0f);
                pacMan.show();
            }
            case 520 -> {
                pacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
                pacMan.setMoveDir(Direction.LEFT);
                pacMan.setSpeed(2.0f);
            }
            case 570 -> {
                msPacMan.setPosition(RIGHT_BORDER, LOWER_LANE);
                msPacMan.setMoveDir(Direction.LEFT);
                msPacMan.setSpeed(2.0f);
            }
            case 780 -> {
                msPacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
                msPacMan.setMoveDir(Direction.RIGHT);
                msPacMan.setSpeed(2.0f);
            }
            case 830 -> {
                pacMan.setPosition(LEFT_BORDER, MIDDLE_LANE);
                pacMan.setMoveDir(Direction.RIGHT);
                pacMan.setSpeed(2.0f);
            }
            case 1040 -> {
                pacMan.setPosition(RIGHT_BORDER, UPPER_LANE);
                pacMan.setMoveDir(Direction.LEFT);
                pacMan.setSpeed(4.0f); //TODO correct?
            }
            case 1055 -> {
                msPacMan.setPosition(RIGHT_BORDER, UPPER_LANE);
                msPacMan.setMoveDir(Direction.LEFT);
                msPacMan.setSpeed(4.0f);
            }
            case 1105 -> {
                msPacMan.setPosition(LEFT_BORDER, LOWER_LANE);
                msPacMan.setMoveDir(Direction.RIGHT);
                msPacMan.setSpeed(4.0f);
            }
            case 1120 -> {
                pacMan.setPosition(LEFT_BORDER, LOWER_LANE);
                pacMan.setMoveDir(Direction.RIGHT);
                pacMan.setSpeed(4.0f);
            }
            case 1380 -> game.control().terminateCurrentGameState();
        }
    }

    @Override
    public Vector2i unscaledSize() { return NES_SIZE_PX; }
}