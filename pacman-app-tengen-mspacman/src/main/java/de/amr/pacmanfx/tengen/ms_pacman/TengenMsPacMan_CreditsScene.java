/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.ActionBindingSupport;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_ActionBindings.TENGEN_ACTION_BINDINGS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;

public class TengenMsPacMan_CreditsScene extends GameScene2D implements ActionBindingSupport {

    static final float DISPLAY_SECONDS = 12;

    @Override
    protected void doInit() {
        theGame().setScoreVisible(false);
        bindAction(TengenMsPacMan_Action.ACTION_START_GAME, TENGEN_ACTION_BINDINGS);
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
    public TengenMsPacMan_Renderer2D gr() {
        return (TengenMsPacMan_Renderer2D) gameRenderer;
    }

    @Override
    protected void drawSceneContent() {
        double barWidth = sizeInPx().x();
        gr().drawBar(nesPaletteColor(0x20), nesPaletteColor(0x13), barWidth, 20);
        gr().drawBar(nesPaletteColor(0x20), nesPaletteColor(0x13), barWidth, 212);
        if (theGameState().timer().betweenSeconds(0.5 * DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            drawJavaFXVersionAuthors(arcadeFont8());
        } else {
            drawOriginalGameAuthors(arcadeFont8());
        }
    }

    private void drawOriginalGameAuthors(Font font) {
        int y = 7 * TS;
        gr().fillText("CREDITS FOR MS PAC-MAN", nesPaletteColor(0x20), font, 3 * TS, y);
        y += 4 * TS;
        gr().fillText("GAME PROGRAMMER:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        gr().fillText("FRANZ LANZINGER", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 3 * TS;
        gr().fillText("SPECIAL THANKS:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        gr().fillText("JEFF YONAN", nesPaletteColor(0x23), font, 10 * TS, y);
        y += TS;
        gr().fillText("DAVE O'RIVA", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 4 * TS;
        gr().fillText("MS PAC-MAN TM NAMCO LTD", nesPaletteColor(0x19), font, 5 * TS, y);
        y += TS;
        gr().fillText("©1990 TENGEN INC", nesPaletteColor(0x19), font, 7 * TS, y);
        y += TS;
        gr().fillText("ALL RIGHTS RESERVED", nesPaletteColor(0x19), font, 6 * TS, y);
    }

    private void drawJavaFXVersionAuthors(Font font) {
        int y = 7 * TS;
        gr().fillText("CREDITS FOR JAVAFX REMAKE", nesPaletteColor(0x20), font, 3 * TS, y);
        y += 4 * TS;
        gr().fillText("GAME PROGRAMMER:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        gr().fillText("ARMIN REICHERT", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 3 * TS;
        gr().fillText("SPECIAL THANKS:", nesPaletteColor(0x23), font, 4 * TS, y);
        y += 2 * TS;
        gr().fillText("@RUSSIANMANSMWC", nesPaletteColor(0x23), font, 10 * TS, y);
        y += TS;
        gr().fillText("@FLICKY1211", nesPaletteColor(0x23), font, 10 * TS, y);
        y += TS;
        gr().fillText("ANDYANA JONSEPH", nesPaletteColor(0x23), font, 10 * TS, y);
        y += 3 * TS;
        gr().fillText("GITHUB.COM/ARMIN-REICHERT", nesPaletteColor(0x19), font, 3 * TS, y);
        y += TS;
        gr().fillText("©2024 MIT LICENSE", nesPaletteColor(0x19), font, 6 * TS, y);
        y += TS;
        gr().fillText("ALL RIGHTS GRANTED", nesPaletteColor(0x19), font, 5 * TS, y);
    }
}