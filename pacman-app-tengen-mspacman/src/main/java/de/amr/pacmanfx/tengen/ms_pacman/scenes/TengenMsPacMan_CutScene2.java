/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.model.actors.CommonAnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_HUD;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.MsPacMan;
import de.amr.pacmanfx.tengen.ms_pacman.model.actors.PacMan;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_ActorRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_HUDRenderer;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui._2d.DefaultDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import javafx.scene.canvas.Canvas;

import java.util.stream.Stream;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationManager.ANIM_PAC_MAN_MUNCHING;
import static de.amr.pacmanfx.ui.CommonGameActions.ACTION_LET_GAME_STATE_EXPIRE;

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

    private TengenMsPacMan_HUDRenderer hudRenderer;
    private TengenMsPacMan_ActorRenderer actorRenderer;

    private Clapperboard clapperboard;
    private Pac pacMan;
    private Pac msPacMan;

    public TengenMsPacMan_CutScene2(GameUI ui) {
        super(ui);
    }

    @Override
    public void createRenderers(Canvas canvas) {
        super.createRenderers(canvas);

        final GameUI_Config uiConfig = ui.currentConfig();
        hudRenderer       = configureRenderer((TengenMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas));
        actorRenderer     = configureRenderer((TengenMsPacMan_ActorRenderer) uiConfig.createActorRenderer(canvas));
        debugInfoRenderer = configureRenderer(new DefaultDebugInfoRenderer(ui, canvas));
    }

    @Override
    public TengenMsPacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public void doInit() {
        TengenMsPacMan_HUD hud = (TengenMsPacMan_HUD) context().game().hud();
        hud.creditVisible(false).scoreVisible(false).levelCounterVisible(true).livesCounterVisible(false);
        hud.showGameOptions(false);

        actionBindingsManager.setKeyCombination(ACTION_LET_GAME_STATE_EXPIRE, ui.joypad().key(JoypadButton.START));

        final GameUI_Config uiConfig = ui.currentConfig();
        final var spriteSheet = (TengenMsPacMan_SpriteSheet) uiConfig.spriteSheet();

        clapperboard = new Clapperboard(spriteSheet, 2, "THE CHASE");
        clapperboard.setPosition(3 * TS, 10 * TS);
        clapperboard.setFont(actorRenderer.arcadeFontTS());
        clapperboard.show();
        clapperboard.startAnimation();

        msPacMan = new MsPacMan();
        msPacMan.setAnimationManager(uiConfig.createPacAnimations());

        pacMan = new PacMan();
        pacMan.setAnimationManager(uiConfig.createPacAnimations());

        ui.soundManager().play(MUSIC_ID);
    }

    @Override
    protected void doEnd() {
        ui.soundManager().stop(MUSIC_ID);
    }

    @Override
    public void update() {
        final int t = (int) context().gameState().timer().tickCount();

        pacMan.move();
        msPacMan.move();
        clapperboard.tick();

        switch (t) {
            case 270 -> {
                msPacMan.setPosition(LEFT_BORDER, UPPER_LANE);
                msPacMan.setMoveDir(Direction.RIGHT);
                msPacMan.playAnimation(CommonAnimationID.ANIM_PAC_MUNCHING);
                msPacMan.setSpeed(2.0f);
                msPacMan.show();
            }
            case 320 -> {
                pacMan.setMoveDir(Direction.RIGHT);
                pacMan.playAnimation(ANIM_PAC_MAN_MUNCHING);
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
            case 1380 -> context().gameController().letCurrentGameStateExpire();
        }
    }

    @Override
    public Vector2i sizeInPx() { return NES_SIZE_PX; }

    @Override
    protected void drawHUD() {
        if (hudRenderer != null) {
            var game = context().<TengenMsPacMan_GameModel>game();
            if (game.mapCategory() != MapCategory.ARCADE) {
                hudRenderer.drawHUD(context().game(), game.hud(), sizeInPx().minus(0, 2 * TS));
            }
        }
    }

    @Override
    public void drawSceneContent() {
        if (actorRenderer != null) {
            Stream.of(clapperboard, msPacMan, pacMan).forEach(actorRenderer::drawActor);
        }
    }
}