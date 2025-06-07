/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_GameActions.START_GAME;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.pacmanfx.ui.PacManGames_Env.theJoypad;

public class TengenMsPacMan_CreditsScene extends GameScene2D {

    static final float DISPLAY_SECONDS = 12;

    @Override
    protected void doInit() {
        theGame().setScoreVisible(false);
        bind(START_GAME, theJoypad().key(JoypadButton.START));
    }

    @Override
    public void update() {
        if (theGameState().timer().atSecond(DISPLAY_SECONDS)) {
            theGameController().letCurrentGameStateExpire();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return NES_SIZE.toVector2f();
    }

    @Override
    protected void drawSceneContent() {
        var r = (TengenMsPacMan_Renderer2D) gr();
        double barWidth = sizeInPx().x();
        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x13), barWidth, 20);
        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x13), barWidth, 212);
        if (theGameState().timer().betweenSeconds(0.5 * DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            drawJavaFXVersionAuthors(r, normalArcadeFont());
        } else {
            drawOriginalGameAuthors(r, normalArcadeFont());
        }
    }

    private void drawOriginalGameAuthors(TengenMsPacMan_Renderer2D r, Font font) {
        int y = 7 * TS;
        r.fillText("CREDITS FOR MS PAC-MAN", nesPaletteColor(0x20), font, 3 * TS, y);
        y += 4 * TS;
        r.fillText("GAME PROGRAMMER:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        r.fillText("FRANZ LANZINGER", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 3 * TS;
        r.fillText("SPECIAL THANKS:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        r.fillText("JEFF YONAN", nesPaletteColor(0x23), font, 10 * TS, y);
        y += TS;
        r.fillText("DAVE O'RIVA", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 4 * TS;
        r.fillText("MS PAC-MAN TM NAMCO LTD", nesPaletteColor(0x19), font, 5 * TS, y);
        y += TS;
        r.fillText("©1990 TENGEN INC", nesPaletteColor(0x19), font, 7 * TS, y);
        y += TS;
        r.fillText("ALL RIGHTS RESERVED", nesPaletteColor(0x19), font, 6 * TS, y);
    }

    private void drawJavaFXVersionAuthors(TengenMsPacMan_Renderer2D r, Font font) {
        int y = 7 * TS;
        r.fillText("CREDITS FOR JAVAFX REMAKE", nesPaletteColor(0x20), font, 3 * TS, y);
        y += 4 * TS;
        r.fillText("GAME PROGRAMMER:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        r.fillText("ARMIN REICHERT", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 3 * TS;
        r.fillText("SPECIAL THANKS:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        r.fillText("@RUSSIANMANSMWC", nesPaletteColor(0x23), font, 10 * TS, y);
        y += TS;
        r.fillText("@FLICKY1211", nesPaletteColor(0x23), font, 10 * TS, y);
        y += TS;
        r.fillText("ANDYANA JONSEPH", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 3 * TS;
        r.fillText("GITHUB.COM/ARMIN-REICHERT", nesPaletteColor(0x19), font, 3 * TS, y);
        y += TS;
        r.fillText("©2024 MIT LICENSE", nesPaletteColor(0x19), font, 6 * TS, y);
        y += TS;
        r.fillText("ALL RIGHTS GRANTED", nesPaletteColor(0x19), font, 5 * TS, y);
    }
}