/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.ms_pacman.scenes;

import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_HUDRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SceneRenderer;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.ArcadeMsPacMan_SpriteSheet;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.SpriteID;
import de.amr.pacmanfx.event.GameEvent;
import de.amr.pacmanfx.ui._2d.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.action.ArcadeActions;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.ui.api.ArcadePalette.ARCADE_ORANGE;

public class ArcadeMsPacMan_StartScene extends GameScene2D {

    private ArcadeMsPacMan_HUDRenderer hudRenderer;

    public ArcadeMsPacMan_StartScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void createRenderers(Canvas canvas) {
        final GameUI_Config uiConfig = ui.currentConfig();
        sceneRenderer     = configureRenderer(new ArcadeMsPacMan_SceneRenderer(canvas, uiConfig));
        hudRenderer       = configureRenderer((ArcadeMsPacMan_HUDRenderer) uiConfig.createHUDRenderer(canvas));
        debugInfoRenderer = configureRenderer(new BaseDebugInfoRenderer(ui, canvas));
    }

    @Override
    public ArcadeMsPacMan_HUDRenderer hudRenderer() {
        return hudRenderer;
    }

    @Override
    public void doInit() {
        context().game().hud().creditVisible(true).scoreVisible(true).levelCounterVisible(true).livesCounterVisible(false);
        actionBindings.bind(ArcadeActions.ACTION_INSERT_COIN, ui.actionBindings());
        actionBindings.bind(ArcadeActions.ACTION_START_GAME, ui.actionBindings());
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
        final Font font6 = sceneRenderer.arcadeFont6();
        final Font font8 = sceneRenderer.arcadeFont8();
        final ArcadeMsPacMan_SpriteSheet spriteSheet = (ArcadeMsPacMan_SpriteSheet) sceneRenderer.spriteSheet();
        sceneRenderer.fillText("PUSH START BUTTON", ARCADE_ORANGE, font8, TS(6), TS(16));
        sceneRenderer.fillText("1 PLAYER ONLY", ARCADE_ORANGE, font8, TS(8), TS(18));
        sceneRenderer.fillText("ADDITIONAL    AT 10000", ARCADE_ORANGE, font8,TS(2), TS(25));
        sceneRenderer.drawSprite(spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL), TS(13), TS(23) + 1, true);
        sceneRenderer.fillText("PTS", ARCADE_ORANGE, font6, TS(25), TS(25));
        if (sceneRenderer instanceof ArcadeMsPacMan_SceneRenderer msPacManSceneRenderer) {
            msPacManSceneRenderer.drawMidwayCopyright(ui.currentConfig().assets().image("logo.midway"), TS * 6, TS * 28);
        }
    }
}