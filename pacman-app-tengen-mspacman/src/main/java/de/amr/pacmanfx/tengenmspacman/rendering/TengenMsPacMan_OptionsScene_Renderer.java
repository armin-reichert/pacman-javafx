/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.ecs.systems.SpriteAnimSystem;
import de.amr.pacmanfx.core.session.GameSession;
import de.amr.pacmanfx.game.GameVariantConfig;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_OptionsScene;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.action.core.GameAppContext;
import de.amr.pacmanfx.ui.gamescene.d2.AbstractGameScene2D;
import de.amr.pacmanfx.ui.gamescene.d2.BaseDebugInfoRenderer;
import de.amr.pacmanfx.ui.gamescene.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_OptionsScene.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_OptionsScene_Renderer extends BaseRenderer
    implements GameScene2D_Renderer, SpriteRenderer, TengenMsPacMan_SceneRendererMixin
{
    private static final int COL_ARROW = 2 * TS;
    private static final int COL_LABEL = 4 * TS;
    private static final int COL_COLON = 19 * TS;
    private static final int COL_VALUE = 21  * TS;

    private static final Color NES_YELLOW = NES_Palette.color(0x28);
    private static final Color NES_WHITE = NES_Palette.color(0x20);

    private final SpriteAnimSystem animSystem;
    private final BaseDebugInfoRenderer debugRenderer;

    public TengenMsPacMan_OptionsScene_Renderer(AbstractGameScene2D scene, SpriteAnimSystem animSystem, Canvas canvas) {
        super(canvas);
        requireNonNull(scene);
        this.animSystem = requireNonNull(animSystem);
        debugRenderer = GameScene2D_Renderer.createDefaultSceneDebugRenderer(scene, canvas);
    }

    @Override
    public SpriteAnimSystem animSystem() {
        return animSystem;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public GameScene2D_Renderer renderer() {
        return this;
    }

    @Override
    public void draw(AbstractGameScene2D gameScene2D, long tick) {
        final GameAppContext app = gameScene2D.app();

        final GameVariantConfig gameVariantConfig = app.gameVariants().currentGameVariant().config();
        final TengenMsPacMan_UISettings uiSettings = app.getExtensionValue(
            TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);


        final GameContext game = gameScene2D.game();
        final GameSession session = game.session();

        final TengenMsPacMan_GamePlay gamePlay = (TengenMsPacMan_GamePlay) game.gamePlay();
        final var scene = (TengenMsPacMan_OptionsScene) gameScene2D;

        clearCanvas();
        if (scene.initialDelay > 0) return;

        ctx.setFont(arcadeFont8());

        if (uiSettings.joypadBindingsDisplayed.get()) {
            drawJoypadKeyBinding(gameScene2D.app().input().joypad().currentKeyBinding());
        }

        drawHorizontalBar(NES_Palette.color(0x20), NES_Palette.color(0x21), scene.unscaledWidth(), TS, 20);

        float y = 48;
        fillText("MS PAC-MAN OPTIONS", NES_YELLOW, COL_LABEL + 3 * TS, 48);

        y += tilesPx(3);
        // Players (not implemented)
        drawMarkerIfSelected(scene, OPTION_PLAYERS, y, arcadeFont8());
        fillText("TYPE", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_LABEL + 4 * TS + 4, y);
        // gray out
        fillText("1 PLAYER", NES_Palette.color(0x10), COL_LABEL + 6 * TS, y);

        y += tilesPx(3);
        // Pac-Booster
        drawMarkerIfSelected(scene, OPTION_PAC_BOOSTER, y, arcadeFont8());
        fillText("PAC BOOSTER", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        String pacBoosterText = switch (gamePlay.boosterMode(session)) {
            case BOOSTER_OFF -> "OFF";
            case BOOSTER_ALWAYS_ON -> "ALWAYS ON";
            case ACTIVATE_WITH_A_OR_B -> "USE A OR B";
        };
        fillText(pacBoosterText, NES_WHITE, COL_VALUE, y);

        y += tilesPx(3);
        // Game difficulty
        drawMarkerIfSelected(scene, OPTION_DIFFICULTY, y, arcadeFont8());
        fillText("GAME DIFFICULTY", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(gamePlay.difficulty(session).name(), NES_WHITE, COL_VALUE, y);

        y += tilesPx(3);
        // Maze (type) selection
        drawMarkerIfSelected(scene, OPTION_MAZE_SELECTION, y, arcadeFont8());
        fillText("MAZE SELECTION", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(gamePlay.mapCategory(session).name(), NES_WHITE, COL_VALUE, y);

        y += tilesPx(3);

        final int startLevelNumber = gamePlay.startLevelNumber(session);
        final int numContinues = gamePlay.numContinues(session);

        drawMarkerIfSelected(scene, OPTION_STARTING_LEVEL, y, arcadeFont8());
        fillText("STARTING LEVEL", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(String.valueOf(startLevelNumber), NES_WHITE, COL_VALUE, y);
        if (numContinues < 4) {
            final var spriteSheet = (TengenMsPacMan_SpriteSheet) gameVariantConfig.renderConfig().spriteSheet();
            final RectShort continuesSprite = spriteSheet.findSprite(switch (numContinues) {
                case 0 -> SpriteID.CONTINUES_0;
                case 1 -> SpriteID.CONTINUES_1;
                case 2 -> SpriteID.CONTINUES_2;
                case 3 -> SpriteID.CONTINUES_3;
                default -> throw new IllegalArgumentException("Illegal number of continues: " + numContinues);
            });
            drawSprite(continuesSprite, COL_VALUE + 3 * TS, y - 8, true);
        }

        y += tilesPx(3);
        fillText("MOVE ARROW WITH JOYPAD",      NES_YELLOW, tilesPx(4), y);

        y += tilesPx(1);
        fillText("CHOOSE OPTIONS WITH A AND B", NES_YELLOW, tilesPx(2), y);

        y += tilesPx(1);
        fillText("PRESS START TO START GAME",   NES_YELLOW, tilesPx(3), y);

        drawHorizontalBar(NES_Palette.color(0x20), NES_Palette.color(0x21), scene.unscaledWidth(), TS, 212);

        if (gameScene2D.app().ui().viewModel().debugModeOnProperty.get()) {
            debugRenderer.draw(gameScene2D, tick);
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