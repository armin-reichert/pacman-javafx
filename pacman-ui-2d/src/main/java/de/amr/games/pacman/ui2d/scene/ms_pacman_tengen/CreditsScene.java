/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.paletteColor;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfig.*;

public class CreditsScene extends GameScene2D {

    static final float DISPLAY_SECONDS = 12;

    @Override
    public void bindGameActions() {
        context.setScoreVisible(false);
        context.enableJoypad();
        bind(GameActions2D.START_GAME, context.joypad().keyCombination(NES.Joypad.START));
    }

    @Override
    public void update() {
        if (context.gameState().timer().atSecond(DISPLAY_SECONDS)) {
            context.gameState().timer().expire();
        }
    }

    @Override
    public Vector2f size() {
        return new Vector2f(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    }

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        var r = (TengenMsPacManGameRenderer) renderer;
        Font scaledFont = renderer.scaledArcadeFont(TS);
        double width = size().x();
        r.drawBar(paletteColor(0x20), paletteColor(0x13), width, 20);
        r.drawBar(paletteColor(0x20), paletteColor(0x13), width, 212);
        if (context.gameState().timer().betweenSeconds(0.5*DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            drawJavaFXVersionAuthors(r, scaledFont);
        } else {
            drawOriginalGameAuthors(r, scaledFont);
        }
    }

    private void drawOriginalGameAuthors(TengenMsPacManGameRenderer r, Font scaledFont) {
        int y = 7 * TS;
        r.drawText("CREDITS FOR MS PAC-MAN", paletteColor(0x20), scaledFont, 3 * TS, y);
        y += 4 * TS;
        r.drawText("GAME PROGRAMMER:", paletteColor(0x23), scaledFont, 4 * TS, y);
        y += 2 * TS;
        r.drawText("FRANZ LANZINGER", paletteColor(0x23), scaledFont, 10 * TS, y);
        y += 3 * TS;
        r.drawText("SPECIAL THANKS:", paletteColor(0x23), scaledFont, 4 * TS, y);
        y += 2 * TS;
        r.drawText("JEFF YONAN", paletteColor(0x23), scaledFont, 10 * TS, y);
        y += TS;
        r.drawText("DAVE O'RIVA", paletteColor(0x23), scaledFont, 10 * TS, y);
        y += 4 * TS;
        r.drawText("MS PAC-MAN TM NAMCO LTD", paletteColor(0x19), scaledFont, centerX(23), y);
        y += TS;
        r.drawText("©1990 TENGEN INC", paletteColor(0x19), scaledFont, centerX(16), y);
        y += TS;
        r.drawText("ALL RIGHTS RESERVED", paletteColor(0x19), scaledFont, centerX(19), y);
    }

    private void drawJavaFXVersionAuthors(TengenMsPacManGameRenderer r, Font scaledFont) {
        int y = 7 * TS;
        r.drawText("CREDITS FOR JAVAFX REMAKE", paletteColor(0x20), scaledFont, 3 * TS, y);
        y += 4 * TS;
        r.drawText("GAME PROGRAMMER:", paletteColor(0x23), scaledFont, 4 * TS, y);
        y += 2 * TS;
        r.drawText("ARMIN REICHERT", paletteColor(0x23), scaledFont, 10 * TS, y);
        y += 3 * TS;
        r.drawText("SPECIAL THANKS:", paletteColor(0x23), scaledFont, 4 * TS, y);
        y += 2 * TS;
        r.drawText("RUSSIANMANSMWC", paletteColor(0x23), scaledFont, 10 * TS, y);
        y += TS;
        r.drawText("ANDYANA JONSEPH", paletteColor(0x23), scaledFont, 10 * TS, y);
        y += TS;
        r.drawText("FLICKY1211", paletteColor(0x23), scaledFont, 10 * TS, y);
        y += 3 * TS;
        r.drawText("MS PAC-MAN TM NAMCO LTD", paletteColor(0x19), scaledFont, centerX(23), y);
        y += TS;
        r.drawText("©1990 TENGEN INC", paletteColor(0x19), scaledFont, centerX(16), y);
        y += TS;
        r.drawText("ALL RIGHTS RESERVED", paletteColor(0x19), scaledFont, centerX(19), y);
    }

    private double centerX(int textLength) {
        return (NES_TILES_X - textLength) * HTS;
    }
}