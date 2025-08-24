/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_HUDRenderer;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.rendering.BaseCanvasRenderer;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.CommonGameActions.ACTION_ARCADE_INSERT_COIN;
import static de.amr.pacmanfx.ui.CommonGameActions.ACTION_ARCADE_START_GAME;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.*;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    private ArcadePacMan_HUDRenderer hudRenderer;
    private BaseCanvasRenderer sceneRenderer;

    public ArcadePacMan_StartScene(GameUI ui) {
        super(ui);
    }

    @Override
    public void doInit() {
        hudRenderer = new ArcadePacMan_HUDRenderer(canvas, ui.currentConfig());
        sceneRenderer = new BaseCanvasRenderer(canvas);

        bindRendererProperties(hudRenderer, sceneRenderer);

        context().game().hudData().credit(true).score(true).levelCounter(true).livesCounter(false);

        actionBindings.assign(ACTION_ARCADE_INSERT_COIN, ui.actionBindings());
        actionBindings.assign(ACTION_ARCADE_START_GAME, ui.actionBindings());
    }

    @Override
    protected void doEnd() {
    }

    @Override
    public void update() {}

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }

    @Override
    public Vector2f sizeInPx() { return ARCADE_MAP_SIZE_IN_PIXELS; }

    @Override
    public void drawHUD() {
        if (hudRenderer != null) {
            hudRenderer.drawHUD(context(), context().game().hudData(), sizeInPx());
        }
    }

    @Override
    public void drawSceneContent() {
        Font font = sceneRenderer.arcadeFontTS();
        sceneRenderer.fillText("PUSH START BUTTON", ARCADE_ORANGE, font,        TS(6),  TS(17));
        sceneRenderer.fillText("1 PLAYER ONLY", ARCADE_CYAN, font,              TS(8),  TS(21));
        sceneRenderer.fillText("BONUS PAC-MAN FOR 10000", ARCADE_ROSE, font,    TS(1),  TS(25));
        sceneRenderer.fillText("PTS", ARCADE_ROSE, sceneRenderer.arcadeFont6(), TS(25), TS(25));
        sceneRenderer.fillText("© 1980 MIDWAY MFG.CO.", ARCADE_PINK, font,      TS(4),  TS(29));
    }
}