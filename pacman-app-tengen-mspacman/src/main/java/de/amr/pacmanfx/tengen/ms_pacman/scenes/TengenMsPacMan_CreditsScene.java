/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_ENTER_START_SCREEN;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;

/**
 * Gives credit to the people that helped in making the game, original and remake authors.
 */
public class TengenMsPacMan_CreditsScene extends GameScene2D {

    static final float DISPLAY_SECONDS = 12;

    public TengenMsPacMan_CreditsScene(GameUI ui) {
        super(ui);
    }

    @Override
    protected void doInit() {
        setHudRenderer(ui.currentConfig().createHUDRenderer(canvas, scaling));
        gameContext().game().hudData().credit(false).score(false).levelCounter(false).livesCounter(false);
        var tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().actionBindings();
        actionBindings.assign(ACTION_ENTER_START_SCREEN, tengenActionBindings);
    }

    @Override
    protected void doEnd() {
    }

    @Override
    public void update() {
        if (gameContext().gameState().timer().atSecond(DISPLAY_SECONDS)) {
            gameContext().gameController().letCurrentGameStateExpire();
        }
    }

    @Override
    public Vector2f sizeInPx() { return NES_SIZE_PX; }

    @SuppressWarnings("unchecked")
    @Override
    public TengenMsPacMan_GameRenderer renderer() {
        return (TengenMsPacMan_GameRenderer) gameRenderer;
    }

    @Override
    public void drawSceneContent() {
        gameRenderer.ctx().setFont(scaledArcadeFont8());
        double barWidth = sizeInPx().x();
        renderer().drawBar(nesPaletteColor(0x20), nesPaletteColor(0x13), barWidth, 20);
        renderer().drawBar(nesPaletteColor(0x20), nesPaletteColor(0x13), barWidth, 212);
        int y = 7 * TS;
        if (gameContext().gameState().timer().betweenSeconds(0.5 * DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            renderer().fillText("CREDITS FOR JAVAFX REMAKE", nesPaletteColor(0x20), 3 * TS, y);
            y += 4 * TS;
            renderer().fillText("GAME PROGRAMMER:", nesPaletteColor(0x23), 4 * TS, y);
            y += 2 * TS;
            renderer().fillText("ARMIN REICHERT", nesPaletteColor(0x23), 10 * TS, y);
            y += 3 * TS;
            renderer().fillText("SPECIAL THANKS:", nesPaletteColor(0x23), 4 * TS, y);
            y += 2 * TS;
            renderer().fillText("@RUSSIANMANSMWC", nesPaletteColor(0x23), 10 * TS, y);
            y += TS;
            renderer().fillText("@FLICKY1211", nesPaletteColor(0x23), 10 * TS, y);
            y += TS;
            renderer().fillText("ANDYANA JONSEPH", nesPaletteColor(0x23), 10 * TS, y);
            y += 3 * TS;
            renderer().fillText("GITHUB.COM/ARMIN-REICHERT", nesPaletteColor(0x19), 3 * TS, y);
            y += TS;
            renderer().fillText("©2024 MIT LICENSE", nesPaletteColor(0x19), 6 * TS, y);
            y += TS;
            renderer().fillText("ALL RIGHTS GRANTED", nesPaletteColor(0x19), 5 * TS, y);
        } else {
            renderer().fillText("CREDITS FOR MS PAC-MAN", nesPaletteColor(0x20), 3 * TS, y);
            y += 4 * TS;
            renderer().fillText("GAME PROGRAMMER:", nesPaletteColor(0x23), 4 * TS, y);
            y += 2 * TS;
            renderer().fillText("FRANZ LANZINGER", nesPaletteColor(0x23), 10 * TS, y);
            y += 3 * TS;
            renderer().fillText("SPECIAL THANKS:", nesPaletteColor(0x23), 4 * TS, y);
            y += 2 * TS;
            renderer().fillText("JEFF YONAN", nesPaletteColor(0x23), 10 * TS, y);
            y += TS;
            renderer().fillText("DAVE O'RIVA", nesPaletteColor(0x23), 10 * TS, y);
            y += 4 * TS;
            renderer().fillText("MS PAC-MAN TM NAMCO LTD", nesPaletteColor(0x19), 5 * TS, y);
            y += TS;
            renderer().fillText("©1990 TENGEN INC", nesPaletteColor(0x19), 7 * TS, y);
            y += TS;
            renderer().fillText("ALL RIGHTS RESERVED", nesPaletteColor(0x19), 6 * TS, y);
        }
    }
}