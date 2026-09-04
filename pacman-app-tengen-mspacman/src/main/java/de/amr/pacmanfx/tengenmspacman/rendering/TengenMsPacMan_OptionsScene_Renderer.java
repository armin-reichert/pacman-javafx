/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_OptionsScene;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.rendering.GameSceneRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.tengenmspacman.gamescene.TengenMsPacMan_OptionsScene.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_OptionsScene_Renderer extends GameSceneRenderer
    implements SpriteRenderer, TengenMsPacMan_SceneRendererMixin
{
    private static final int COL_ARROW = 2 * TS;
    private static final int COL_LABEL = 4 * TS;
    private static final int COL_COLON = 19 * TS;
    private static final int COL_VALUE = 21  * TS;

    private static final Color NES_YELLOW = NES_Palette.color(0x28);
    private static final Color NES_WHITE = NES_Palette.color(0x20);

    public TengenMsPacMan_OptionsScene_Renderer(GameScene scene, Canvas canvas) {
        super(canvas);
        requireNonNull(scene);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public Renderer renderer() {
        return this;
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof TengenMsPacMan_OptionsScene optionsScene)) {
            return;
        }

        final TengenMsPacMan_UISettings uiSettings = optionsScene.app().currentGameVariantUIConfig().extensionValue(
            TengenMsPacMan_GameExtension.UI_SETTINGS, TengenMsPacMan_UISettings.class);

        final GameContext game = optionsScene.game();
        final GameSession session = game.session();

        if (optionsScene.initialDelay > 0) return;

        ctx.setFont(arcadeFont8());

        if (uiSettings.joypadBindingsDisplayed.get()) {
            drawJoypadKeyBinding(optionsScene.app().input().joypad().currentKeyBinding());
        }

        drawHorizontalBar(NES_Palette.color(0x20), NES_Palette.color(0x21), optionsScene.reqCanvasRendering().unscaledWidth(), TS, 20);

        float y = 48;
        fillText("MS PAC-MAN OPTIONS", NES_YELLOW, COL_LABEL + 3 * TS, 48);

        y += tilesPx(3);
        // Players (not implemented)
        drawMarkerIfSelected(optionsScene, OPTION_PLAYERS, y, arcadeFont8());
        fillText("TYPE", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_LABEL + 4 * TS + 4, y);
        // gray out
        fillText("1 PLAYER", NES_Palette.color(0x10), COL_LABEL + 6 * TS, y);

        y += tilesPx(3);
        // Pac-Booster
        drawMarkerIfSelected(optionsScene, OPTION_PAC_BOOSTER, y, arcadeFont8());
        fillText("PAC BOOSTER", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        String pacBoosterText = switch (TengenMsPacMan_GamePlay.boosterMode(session)) {
            case BOOSTER_OFF -> "OFF";
            case BOOSTER_ALWAYS_ON -> "ALWAYS ON";
            case ACTIVATE_WITH_A_OR_B -> "USE A OR B";
        };
        fillText(pacBoosterText, NES_WHITE, COL_VALUE, y);

        y += tilesPx(3);
        // Game difficulty
        drawMarkerIfSelected(optionsScene, OPTION_DIFFICULTY, y, arcadeFont8());
        fillText("GAME DIFFICULTY", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(TengenMsPacMan_GamePlay.difficulty(session).name(), NES_WHITE, COL_VALUE, y);

        y += tilesPx(3);
        // Maze (type) selection
        drawMarkerIfSelected(optionsScene, OPTION_MAZE_SELECTION, y, arcadeFont8());
        fillText("MAZE SELECTION", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(TengenMsPacMan_GamePlay.mapCategory(session).name(), NES_WHITE, COL_VALUE, y);

        y += tilesPx(3);

        final int startLevelNumber = TengenMsPacMan_GamePlay.startLevelNumber(session);
        final int numContinues = TengenMsPacMan_GamePlay.numContinues(session);

        drawMarkerIfSelected(optionsScene, OPTION_STARTING_LEVEL, y, arcadeFont8());
        fillText("STARTING LEVEL", NES_YELLOW, COL_LABEL, y);
        fillText(":", NES_YELLOW, COL_COLON, y);
        fillText(String.valueOf(startLevelNumber), NES_WHITE, COL_VALUE, y);
        if (numContinues < 4) {
            final var spriteSheet = optionsScene.app().currentGameVariantUIConfig().renderConfig().spriteSheet();
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

        drawHorizontalBar(NES_Palette.color(0x20), NES_Palette.color(0x21), optionsScene.reqCanvasRendering().unscaledWidth(), TS, 212);
    }

    private void drawMarkerIfSelected(TengenMsPacMan_OptionsScene optionsScene, int optionIndex, double y, Font font) {
        if (optionsScene.selectedOption() == optionIndex) {
            ctx.setFill(NES_YELLOW);
            ctx.fillRect(scaled(COL_ARROW + 2.25), scaled(y - 4.5), scaled(7.5), scaled(1.75));
            fillText(">", NES_YELLOW, font, COL_ARROW + 3, y);
        }
    }
}