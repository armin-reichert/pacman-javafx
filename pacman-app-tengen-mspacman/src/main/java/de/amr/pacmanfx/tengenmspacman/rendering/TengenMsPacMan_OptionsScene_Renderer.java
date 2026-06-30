/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_OptionsScene;
import de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.ui.GameVariantConfig;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.model.world.WorldMap.TS;
import static de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_OptionsScene.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_OptionsScene_Renderer extends BaseRenderer
    implements GameScene2D_Renderer, SpriteRendererMixin, TengenMsPacMan_SceneRendererMixin
{
    private static final int COL_ARROW = 2 * TS;
    private static final int COL_LABEL = 4 * TS;
    private static final int COL_COLON = 19 * TS;
    private static final int COL_VALUE = 21  * TS;

    private static final Color NES_YELLOW = NES_Palette.color(0x28);
    private static final Color NES_WHITE = NES_Palette.color(0x20);

    private final BaseDebugInfoRenderer debugRenderer;

    public TengenMsPacMan_OptionsScene_Renderer(AbstractGameScene2D scene, Canvas canvas) {
        super(canvas);
        requireNonNull(scene);
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public GameScene2D_Renderer renderer() {
        return this;
    }

    public void draw(AbstractGameScene2D gameScene2D) {
        final GameVariantConfig variantConfig = gameScene2D.game().currentVariantConfig();
        final var gameScene = (TengenMsPacMan_OptionsScene) gameScene2D;
        final var gameModel = (TengenMsPacMan_GameModel) gameScene.gameModel();
        final var uiSettings = gameScene2D.game().extensions()
            .value(TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

        clearCanvas();
        if (gameScene.initialDelay > 0) return;

        ctx.setFont(arcadeFont8());

        if (uiSettings.joypadBindingsDisplayed.get()) {
            drawJoypadKeyBinding(gameScene2D.game().input().joypad().currentKeyBinding());
        }

        drawHorizontalBar(NES_Palette.color(0x20), NES_Palette.color(0x21), gameScene.unscaledWidth(), TS, 20);

        float y = 48;
        fillText("MS PAC-MAN OPTIONS", NES_YELLOW, COL_LABEL + 3 * TS, 48);

        y += TS(3);
        // Players (not implemented)
        drawMarkerIfSelected(gameScene, OPTION_PLAYERS, y, arcadeFont8());
        fillText("TYPE", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_LABEL + 4 * TS + 4, y);
        // gray out
        fillText("1 PLAYER", NES_Palette.color(0x10), COL_LABEL + 6 * TS, y);

        y += TS(3);
        // Pac-Booster
        drawMarkerIfSelected(gameScene, OPTION_PAC_BOOSTER, y, arcadeFont8());
        fillText("PAC BOOSTER", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        String pacBoosterText = switch (gameModel.pacBoosterMode()) {
            case OFF -> "OFF";
            case ALWAYS_ON -> "ALWAYS ON";
            case USE_A_OR_B -> "USE A OR B";
        };
        fillText(pacBoosterText, NES_WHITE, COL_VALUE, y);

        y += TS(3);
        // Game difficulty
        drawMarkerIfSelected(gameScene, OPTION_DIFFICULTY, y, arcadeFont8());
        fillText("GAME DIFFICULTY", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(gameModel.difficulty().name(), NES_WHITE, COL_VALUE, y);

        y += TS(3);
        // Maze (type) selection
        drawMarkerIfSelected(gameScene, OPTION_MAZE_SELECTION, y, arcadeFont8());
        fillText("MAZE SELECTION", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(gameModel.mapCategory().name(), NES_WHITE, COL_VALUE, y);

        y += TS(3);
        // Starting level number
        drawMarkerIfSelected(gameScene, OPTION_STARTING_LEVEL, y, arcadeFont8());
        fillText("STARTING LEVEL", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(String.valueOf(gameModel.startLevelNumber()), NES_WHITE, COL_VALUE, y);
        final int numContinues = gameModel.numContinues();
        if (numContinues < 4) {
            var spriteSheet = (TengenMsPacMan_SpriteSheet) variantConfig.spriteSheet();
            RectShort continuesSprite = spriteSheet.sprite(switch (numContinues) {
                case 0 -> SpriteID.CONTINUES_0;
                case 1 -> SpriteID.CONTINUES_1;
                case 2 -> SpriteID.CONTINUES_2;
                case 3 -> SpriteID.CONTINUES_3;
                default -> throw new IllegalArgumentException("Illegal number of continues: " + numContinues);
            });
            drawSprite(continuesSprite, COL_VALUE + 3 * TS, y - 8, true);
        }

        y += TS(3);
        fillText("MOVE ARROW WITH JOYPAD",      NES_YELLOW, TS(4), y);

        y += TS(1);
        fillText("CHOOSE OPTIONS WITH A AND B", NES_YELLOW, TS(2), y);

        y += TS(1);
        fillText("PRESS START TO START GAME",   NES_YELLOW, TS(3), y);

        drawHorizontalBar(NES_Palette.color(0x20), NES_Palette.color(0x21), gameScene.unscaledWidth(), TS, 212);

        if (gameScene2D.game().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(gameScene2D);
        }
    }

    private void drawMarkerIfSelected(TengenMsPacMan_OptionsScene optionsScene, int optionIndex, double y, Font font) {
        if (optionsScene.selectedOption() == optionIndex) {
            ctx.setFill(NES_YELLOW);
            ctx.fillRect(scaled(COL_ARROW + 2.25), scaled(y - 4.5), scaled(7.5), scaled(1.75));
            fillText(">", NES_YELLOW, font, COL_ARROW + 3, y);
        }
    }
}