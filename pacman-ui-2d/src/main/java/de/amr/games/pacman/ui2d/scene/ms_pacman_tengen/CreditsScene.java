package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.ui2d.GameActions2D;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.scene.common.GameScene2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.lib.Globals.HTS;
import static de.amr.games.pacman.lib.Globals.TS;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameRenderer.paletteColor;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.TengenMsPacManGameSceneConfig.*;

public class CreditsScene extends GameScene2D {

    @Override
    public void bindGameActions() {
        context.setScoreVisible(false);
        context.enableJoypad();
        bind(GameActions2D.START_GAME, context.joypad().keyCombination(NES.Joypad.START));
    }

    @Override
    public void update() {
        if (context.gameState().timer().atSecond(9)) {
            context.gameController().changeState(GameState.STARTING_GAME);
        }
    }

    @Override
    public Vector2f size() {
        return new Vector2f(NES_RESOLUTION_X, NES_RESOLUTION_Y);
    }

    @Override
    protected void drawSceneContent(GameRenderer renderer) {
        TengenMsPacManGameRenderer r = (TengenMsPacManGameRenderer) renderer;
        Font scaledFont = renderer.scaledArcadeFont(8);
        r.drawBar(paletteColor(0x20), paletteColor(0x13), size().x(), 20);
        r.drawBar(paletteColor(0x20), paletteColor(0x13), size().x(), 212);
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

    private double centerX(int textLength) {
        return (NES_TILES_X - textLength) * HTS;
    }

    private void centerLabelText(GameRenderer renderer, String text, Font font, Color color, double y) {
        renderer.drawText(text, color, font, (NES_TILES_X - text.length()) * HTS, y);
    }
}
