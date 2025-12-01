/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_HUDRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_StartScene_Renderer;
import de.amr.pacmanfx.arcade.pacman.ArcadeActions;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;

import static de.amr.pacmanfx.ui.input.Keyboard.bare;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    private ArcadeMsPacMan_HUDRenderer hudRenderer;
    private ArcadeMsPacMan_StartScene_Renderer sceneRenderer;

    public ArcadeMsPacMan_StartScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();

        hudRenderer = configureRenderer(
            (ArcadeMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas));

        sceneRenderer = configureRenderer(
            new ArcadeMsPacMan_StartScene_Renderer(this, canvas, (ArcadeMsPacMan_SpriteSheet) uiConfig.spriteSheet()));
    }

    @Override
    public ArcadeMsPacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public ArcadeMsPacMan_StartScene_Renderer sceneRenderer() {
        return sceneRenderer;
    }

    @Override
    public void doInit(Game game) {
        game.hud().creditVisible(true).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);
        actionBindings.addKeyCombination(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.DIGIT5));
        actionBindings.addKeyCombination(ArcadeActions.ACTION_INSERT_COIN, bare(KeyCode.NUMPAD5));
        actionBindings.addKeyCombination(ArcadeActions.ACTION_START_GAME, bare(KeyCode.DIGIT1));
        actionBindings.addKeyCombination(ArcadeActions.ACTION_START_GAME, bare(KeyCode.NUMPAD1));
    }

    @Override
    protected void doEnd(Game game) {}

    @Override
    public void update(Game game) {}

    @Override
    public void onCreditAdded(GameEvent e) {
        ui.soundManager().play(SoundID.COIN_INSERTED);
    }
}