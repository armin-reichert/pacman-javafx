/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_HUDRenderer;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.action.ArcadeActions;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.*;

/**
 * Scene shown after credit has been added and where game can be started.
 */
public class ArcadePacMan_StartScene extends GameScene2D {

    private ArcadePacMan_HUDRenderer hudRenderer;

    public ArcadePacMan_StartScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        super.createRenderers(canvas);
        hudRenderer = configureRenderer(new ArcadePacMan_HUDRenderer(canvas, ui.currentConfig()));
    }

    @Override
    public ArcadePacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public void doInit() {
        context().game().hud().creditVisible(true).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);
        actionBindingsManager.bindAction(ArcadeActions.ACTION_INSERT_COIN, ui.actionBindings());
        actionBindingsManager.bindAction(ArcadeActions.ACTION_START_GAME, ui.actionBindings());
    }

    @Override
    protected void doEnd() {}

    @Override
    public void update() {
        //TODO use binding
        ui.gameContext().game().hud().setNumCoins(ui.gameContext().coinMechanism().numCoins());
    }

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
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