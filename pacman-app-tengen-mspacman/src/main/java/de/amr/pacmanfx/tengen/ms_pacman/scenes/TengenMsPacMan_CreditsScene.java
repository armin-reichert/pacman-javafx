/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_GameLevelRenderer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_ENTER_START_SCREEN;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;

/**
 * Gives credit to the people that helped in making the game, original and remake authors.
 */
public class TengenMsPacMan_CreditsScene extends GameScene2D {

    static final float DISPLAY_SECONDS = 12;

    private int y;

    private void skipTiles(int n) {
        y += n * TS;
    }

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
    public TengenMsPacMan_GameLevelRenderer renderer() {
        return (TengenMsPacMan_GameLevelRenderer) gameLevelRenderer;
    }

    @Override
    public void drawSceneContent() {
        gameLevelRenderer.ctx().setFont(scaledArcadeFont8());
        double barWidth = sizeInPx().x();
        renderer().drawHorizontalBar(nesColor(0x20), nesColor(0x13), barWidth, TS, 20);
        renderer().drawHorizontalBar(nesColor(0x20), nesColor(0x13), barWidth, TS, 212);
        y = 7 * TS;
        if (gameContext().gameState().timer().betweenSeconds(0.5 * DISPLAY_SECONDS, DISPLAY_SECONDS)) {
            renderer().fillText("CREDITS FOR JAVAFX REMAKE", nesColor(0x20), 3 * TS, y);
            skipTiles(4);
            renderer().fillText("GAME PROGRAMMER:", nesColor(0x23), 4 * TS, y);
            skipTiles(2);
            renderer().fillText("ARMIN REICHERT", nesColor(0x23), 10 * TS, y);
            skipTiles(3);
            renderer().fillText("SPECIAL THANKS:", nesColor(0x23), 4 * TS, y);
            skipTiles(2);
            renderer().fillText("@RUSSIANMANSMWC", nesColor(0x23), 10 * TS, y);
            skipTiles(1);
            renderer().fillText("@FLICKY1211", nesColor(0x23), 10 * TS, y);
            skipTiles(1);
            renderer().fillText("ANDYANA JONSEPH", nesColor(0x23), 10 * TS, y);
            skipTiles(3);
            renderer().fillText("GITHUB.COM/ARMIN-REICHERT", nesColor(0x19), 3 * TS, y);
            skipTiles(1);
            renderer().fillText("©2024 MIT LICENSE", nesColor(0x19), 6 * TS, y);
            skipTiles(1);
            renderer().fillText("ALL RIGHTS GRANTED", nesColor(0x19), 5 * TS, y);
        } else {
            renderer().fillText("CREDITS FOR MS PAC-MAN", nesColor(0x20), 3 * TS, y);
            skipTiles(4);
            renderer().fillText("GAME PROGRAMMER:", nesColor(0x23), 4 * TS, y);
            skipTiles(2);
            renderer().fillText("FRANZ LANZINGER", nesColor(0x23), 10 * TS, y);
            skipTiles(3);
            renderer().fillText("SPECIAL THANKS:", nesColor(0x23), 4 * TS, y);
            skipTiles(2);
            renderer().fillText("JEFF YONAN", nesColor(0x23), 10 * TS, y);
            skipTiles(1);
            renderer().fillText("DAVE O'RIVA", nesColor(0x23), 10 * TS, y);
            skipTiles(4);
            renderer().fillText("MS PAC-MAN TM NAMCO LTD", nesColor(0x19), 5 * TS, y);
            skipTiles(1);
            renderer().fillText("©1990 TENGEN INC", nesColor(0x19), 7 * TS, y);
            skipTiles(1);
            renderer().fillText("ALL RIGHTS RESERVED", nesColor(0x19), 6 * TS, y);
        }
    }
}