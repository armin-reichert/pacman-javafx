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
import static de.amr.pacmanfx.ui.PacManGamesEnv.theJoypad;

public class TengenMsPacMan_CreditsScene extends GameScene2D {

    static final float DISPLAY_SECONDS = 12;

    @Override
    protected void doInit() {
        theGame().scoreManager().setScoreVisible(false);
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
        double width = sizeInPx().x();
        r.fillCanvas(backgroundColor());
        r.drawScores(theGame().scoreManager(), scoreColor(), defaultSceneFont());
        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x13), width, 20);
        r.drawBar(nesPaletteColor(0x20), nesPaletteColor(0x13), width, 212);
        if (theGameState().timer().betweenSeconds(0.5 * DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            drawJavaFXVersionAuthors(r, defaultSceneFont());
        } else {
            drawOriginalGameAuthors(r, defaultSceneFont());
        }
    }

    private void drawOriginalGameAuthors(TengenMsPacMan_Renderer2D r, Font font) {
        int y = 7 * TS;
        r.fillTextAtScaledPosition("CREDITS FOR MS PAC-MAN", nesPaletteColor(0x20), font, 3 * TS, y);
        y += 4 * TS;
        r.fillTextAtScaledPosition("GAME PROGRAMMER:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        r.fillTextAtScaledPosition("FRANZ LANZINGER", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 3 * TS;
        r.fillTextAtScaledPosition("SPECIAL THANKS:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        r.fillTextAtScaledPosition("JEFF YONAN", nesPaletteColor(0x23), font, 10 * TS, y);
        y += TS;
        r.fillTextAtScaledPosition("DAVE O'RIVA", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 4 * TS;
        r.fillTextAtScaledPosition("MS PAC-MAN TM NAMCO LTD", nesPaletteColor(0x19), font, 5 * TS, y);
        y += TS;
        r.fillTextAtScaledPosition("©1990 TENGEN INC", nesPaletteColor(0x19), font, 7 * TS, y);
        y += TS;
        r.fillTextAtScaledPosition("ALL RIGHTS RESERVED", nesPaletteColor(0x19), font, 6 * TS, y);
    }

    private void drawJavaFXVersionAuthors(TengenMsPacMan_Renderer2D r, Font font) {
        int y = 7 * TS;
        r.fillTextAtScaledPosition("CREDITS FOR JAVAFX REMAKE", nesPaletteColor(0x20), font, 3 * TS, y);
        y += 4 * TS;
        r.fillTextAtScaledPosition("GAME PROGRAMMER:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        r.fillTextAtScaledPosition("ARMIN REICHERT", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 3 * TS;
        r.fillTextAtScaledPosition("SPECIAL THANKS:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        r.fillTextAtScaledPosition("@RUSSIANMANSMWC", nesPaletteColor(0x23), font, 10 * TS, y);
        y += TS;
        r.fillTextAtScaledPosition("@FLICKY1211", nesPaletteColor(0x23), font, 10 * TS, y);
        y += TS;
        r.fillTextAtScaledPosition("ANDYANA JONSEPH", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 3 * TS;
        r.fillTextAtScaledPosition("GITHUB.COM/ARMIN-REICHERT", nesPaletteColor(0x19), font, 3 * TS, y);
        y += TS;
        r.fillTextAtScaledPosition("©2024 MIT LICENSE", nesPaletteColor(0x19), font, 6 * TS, y);
        y += TS;
        r.fillTextAtScaledPosition("ALL RIGHTS GRANTED", nesPaletteColor(0x19), font, 5 * TS, y);
    }
}