/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_ScenesRenderer;
import de.amr.pacmanfx.ui.ActionBinding;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;

import java.util.Set;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_ENTER_START_SCREEN;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;

/**
 * Gives credit to the people that helped in making the game, original and remake authors.
 */
public class TengenMsPacMan_CreditsScene extends GameScene2D {

    private static final float DISPLAY_SECONDS = 16;

    private int y;

    private TengenMsPacMan_ScenesRenderer scenesRenderer;

    public TengenMsPacMan_CreditsScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void doInit() {
        GameUI_Config uiConfig = ui.currentConfig();

        scenesRenderer = new TengenMsPacMan_ScenesRenderer(canvas, uiConfig);
        bindRendererScaling(scenesRenderer);

        gameContext().game().hudData().credit(false).score(false).levelCounter(false).livesCounter(false);

        Set<ActionBinding> tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().actionBindings();
        actionBindings.assign(ACTION_ENTER_START_SCREEN, tengenActionBindings);
    }

    @Override
    protected void doEnd() {}

    @Override
    public void update() {
        if (gameContext().gameState().timer().atSecond(DISPLAY_SECONDS)) {
            gameContext().gameController().letCurrentGameStateExpire();
        }
    }

    @Override
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    @Override
    public void drawHUD() {
        // No HUD
    }

    @Override
    public void drawSceneContent() {
        scenesRenderer.drawHorizontalBar(nesColor(0x20), nesColor(0x13), sizeInPx().x(), TS, 20);
        scenesRenderer.drawHorizontalBar(nesColor(0x20), nesColor(0x13), sizeInPx().x(), TS, 212);
        ctx().setFont(scenesRenderer.arcadeFont(TS));
        y = 7 * TS; // important: reset on every draw!
        if (gameContext().gameState().timer().betweenSeconds(0, 0.5 * DISPLAY_SECONDS)) {
            drawOriginalCreditsText();
        } else {
            drawRemakeCreditsText();
        }
    }
    private void drawOriginalCreditsText() {
        printAndAdvanceTiles(4, "CREDITS FOR MS PAC-MAN",    0x20,  3);
        printAndAdvanceTiles(2, "GAME PROGRAMMER:",          0x23,  4);
        printAndAdvanceTiles(3, "FRANZ LANZINGER",           0x23, 10);
        printAndAdvanceTiles(2, "SPECIAL THANKS:",           0x23,  4);
        printAndAdvanceTiles(1, "JEFF YONAN",                0x23, 10);
        printAndAdvanceTiles(4, "DAVE O'RIVA",               0x23, 10);
        printAndAdvanceTiles(1, "MS PAC-MAN TM NAMCO LTD",   0x19,  5);
        printAndAdvanceTiles(1, "©1990 TENGEN INC",          0x19,  7);
        printAndAdvanceTiles(0, "ALL RIGHTS RESERVED",       0x19,  6);
    }

    private void drawRemakeCreditsText() {
        printAndAdvanceTiles(4, "CREDITS FOR JAVAFX REMAKE", 0x20,  3);
        printAndAdvanceTiles(2, "GAME PROGRAMMER:",          0x23,  4);
        printAndAdvanceTiles(3, "ARMIN REICHERT",            0x23, 10);
        printAndAdvanceTiles(2, "SPECIAL THANKS:",           0x23,  4);
        printAndAdvanceTiles(1, "@RUSSIANMANSMWC",           0x23, 10);
        printAndAdvanceTiles(1, "@FLICKY1211",               0x23, 10);
        printAndAdvanceTiles(3, "ANDYANA JONSEPH",           0x23, 10);
        printAndAdvanceTiles(1, "GITHUB.COM/ARMIN-REICHERT", 0x19,  3);
        printAndAdvanceTiles(1, "©2024 MIT LICENSE",         0x19,  6);
        printAndAdvanceTiles(0, "ALL RIGHTS GRANTED",        0x19,  5);
    }

    private void printAndAdvanceTiles(int numTiles, String text, int colorIndex, int tilesX) {
        scenesRenderer.fillText(text, nesColor(colorIndex), TS(tilesX), y);
        y += numTiles * TS;
    }
}