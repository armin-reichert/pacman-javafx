/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.scene;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_Renderer2D;
import de.amr.games.pacman.ui._2d.GameActions2D;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.THE_GAME_CONTROLLER;
import static de.amr.games.pacman.Globals.TS;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.games.pacman.ui.UIGlobals.THE_UI;

public class TengenMsPacMan_CreditsScene extends GameScene2D {

    static final float DISPLAY_SECONDS = 12;

    @Override
    public void bindGameActions() {
        THE_UI.setScoreVisible(false);
        THE_UI.joypadKeyBinding().register();
        bind(GameActions2D.START_GAME, THE_UI.joypadKeyBinding().key(NES_JoypadButton.BTN_START));
    }

    @Override
    public void update() {
        if (THE_GAME_CONTROLLER.state().timer().atSecond(DISPLAY_SECONDS)) {
            THE_GAME_CONTROLLER.state().timer().expire();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        var r = (TengenMsPacMan_Renderer2D) gr;
        Font scaledFont = r.scaledArcadeFont(TS);
        double width = sizeInPx().x();
        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x13), width, 20);
        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x13), width, 212);
        if (THE_GAME_CONTROLLER.state().timer().betweenSeconds(0.5*DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            drawJavaFXVersionAuthors(r, scaledFont);
        } else {
            drawOriginalGameAuthors(r, scaledFont);
        }
    }

    private void drawOriginalGameAuthors(TengenMsPacMan_Renderer2D r, Font scaledFont) {
        int y = 7 * TS;
        r.drawText("CREDITS FOR MS PAC-MAN", nesPaletteColor(0x20), scaledFont, 3 * TS, y);
        y += 4 * TS;
        r.drawText("GAME PROGRAMMER:", nesPaletteColor(0x23), scaledFont, 4 * TS, y);
        y += 2 * TS;
        r.drawText("FRANZ LANZINGER", nesPaletteColor(0x23), scaledFont, 10 * TS, y);
        y += 3 * TS;
        r.drawText("SPECIAL THANKS:", nesPaletteColor(0x23), scaledFont, 4 * TS, y);
        y += 2 * TS;
        r.drawText("JEFF YONAN", nesPaletteColor(0x23), scaledFont, 10 * TS, y);
        y += TS;
        r.drawText("DAVE O'RIVA", nesPaletteColor(0x23), scaledFont, 10 * TS, y);
        y += 4 * TS;
        r.drawText("MS PAC-MAN TM NAMCO LTD", nesPaletteColor(0x19), scaledFont, 5 * TS, y);
        y += TS;
        r.drawText("©1990 TENGEN INC", nesPaletteColor(0x19), scaledFont, 7 * TS, y);
        y += TS;
        r.drawText("ALL RIGHTS RESERVED", nesPaletteColor(0x19), scaledFont, 6 * TS, y);
    }

    private void drawJavaFXVersionAuthors(TengenMsPacMan_Renderer2D r, Font scaledFont) {
        int y = 7 * TS;
        r.drawText("CREDITS FOR JAVAFX REMAKE", nesPaletteColor(0x20), scaledFont, 3 * TS, y);
        y += 4 * TS;
        r.drawText("GAME PROGRAMMER:", nesPaletteColor(0x23), scaledFont, 4 * TS, y);
        y += 2 * TS;
        r.drawText("ARMIN REICHERT", nesPaletteColor(0x23), scaledFont, 10 * TS, y);
        y += 3 * TS;
        r.drawText("SPECIAL THANKS:", nesPaletteColor(0x23), scaledFont, 4 * TS, y);
        y += 2 * TS;
        r.drawText("@RUSSIANMANSMWC", nesPaletteColor(0x23), scaledFont, 10 * TS, y);
        y += TS;
        r.drawText("@FLICKY1211", nesPaletteColor(0x23), scaledFont, 10 * TS, y);
        y += TS;
        r.drawText("ANDYANA JONSEPH", nesPaletteColor(0x23), scaledFont, 10 * TS, y);
        y += 3 * TS;
        r.drawText("GITHUB.COM/ARMIN-REICHERT", nesPaletteColor(0x19), scaledFont, 3 * TS, y);
        y += TS;
        r.drawText("©2024 MIT LICENSE", nesPaletteColor(0x19), scaledFont, 6 * TS, y);
        y += TS;
        r.drawText("ALL RIGHTS GRANTED", nesPaletteColor(0x19), scaledFont, 5 * TS, y);
    }
}