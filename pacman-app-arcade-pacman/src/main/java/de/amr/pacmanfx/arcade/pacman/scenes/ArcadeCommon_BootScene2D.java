/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.arcade.pacman.rendering.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.ui.PacManGames.theSound;
import static de.amr.pacmanfx.ui.PacManGames.theUI;

/**
 * The boot screen is showing some strange screen patterns and eventually  a grid.
 * This scene tries to mimic that to a certain degree.
 */
public class ArcadeCommon_BootScene2D extends GameScene2D {

    private static final int FRAGMENT_SIZE = 16;

    private Vector2f minPoint, maxPoint;

    @Override
    public void doInit() {
        theGame().hud().hideScore();
        theGame().hud().hideLevelCounter();
        theGame().hud().hideLivesCounter();

        SpriteSheet<?> spriteSheet = theUI().configuration().spriteSheet();
        double width = spriteSheet.sourceImage().getWidth(), height = spriteSheet.sourceImage().getHeight();
        // ignore left half of sprite sheet image containing maze images
        minPoint = Vector2f.of(width / 2, 0);
        maxPoint = Vector2f.of(width - FRAGMENT_SIZE, height - FRAGMENT_SIZE);
        theSound().playVoice("voice.explain", 0);
    }

    @Override
    public void update() {
        if (theGameState().timer().atSecond(4)) {
            theGameController().letCurrentGameStateExpire();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void draw() {
        gr().setScaling(scaling());
        if (theGameState().timer().tickCount() == 1) {
            gr().fillCanvas(backgroundColor());
        } else {
            drawSceneContent();
        }
    }

    @Override
    protected void drawSceneContent() {
        TickTimer timer = theGameState().timer();
        if (timer.betweenSeconds(1, 2) && timer.tickCount() % 4 == 0) {
            gr().fillCanvas(backgroundColor());
            drawRandomHexDigits();
        } else if (timer.betweenSeconds(2, 3.5) && timer.tickCount() % 4 == 0) {
            gr().fillCanvas(backgroundColor());
            drawRandomSpriteFragments();
        } else if (timer.atSecond(3.5)) {
            gr().fillCanvas(backgroundColor());
            drawGridLines();
        }
    }

    private void drawRandomHexDigits() {
        int numRows = (int) (ARCADE_MAP_SIZE_IN_PIXELS.y() / TS);
        int numCols = (int) (ARCADE_MAP_SIZE_IN_PIXELS.x() / TS);
        ctx().setFill(ARCADE_WHITE);
        ctx().setFont(arcadeFont8());
        for (int row = 0; row < numRows; ++row) {
            double y = scaled(tiles_to_px(row + 1));
            for (int col = 0; col < numCols; ++col) {
                int hexDigit = theRNG().nextInt(16);
                ctx().fillText(Integer.toHexString(hexDigit), scaled(tiles_to_px(col)), y);
            }
        }
    }

    private void drawRandomSpriteFragments() {
        int numRows = (int) (ARCADE_MAP_SIZE_IN_PIXELS.y() / FRAGMENT_SIZE);
        int numCols = (int) (ARCADE_MAP_SIZE_IN_PIXELS.x() / FRAGMENT_SIZE);
        for (int row = 0; row < numRows; ++row) {
            if (theRNG().nextInt(100) < 20) continue;
            Sprite fragment1 = randomSpriteFragment(), fragment2 = randomSpriteFragment();
            int split = numCols / 8 + theRNG().nextInt(numCols / 4);
            for (int col = 0; col < numCols; ++col) {
                gr().drawSpriteScaled(col < split ? fragment1 : fragment2, FRAGMENT_SIZE * col, FRAGMENT_SIZE * row);
            }
        }
    }

    private Sprite randomSpriteFragment() {
        return new Sprite(
            (int) lerp(minPoint.x(), maxPoint.x(), theRNG().nextDouble()),
            (int) lerp(minPoint.y(), maxPoint.y(), theRNG().nextDouble()),
            FRAGMENT_SIZE, FRAGMENT_SIZE);
    }

    private void drawGridLines() {
        double gridWidth = scaled(ARCADE_MAP_SIZE_IN_PIXELS.x());
        double gridHeight = scaled(ARCADE_MAP_SIZE_IN_PIXELS.y());
        int numRows = (int) (ARCADE_MAP_SIZE_IN_PIXELS.y() / 16);
        int numCols = (int) (ARCADE_MAP_SIZE_IN_PIXELS.x() / 16);
        double thin = scaled(2), thick = scaled(4);
        ctx().setStroke(ARCADE_WHITE);
        for (int row = 0; row <= numRows; ++row) {
            ctx().setLineWidth(row == 0 || row == numRows ? thick : thin);
            double y = scaled(row * 16);
            ctx().strokeLine(0, y, gridWidth, y);
        }
        for (int col = 0; col <= numCols; ++col) {
            ctx().setLineWidth(col == 0 || col == numCols ? thick : thin);
            double x = scaled(col * 16);
            ctx().strokeLine(x, 0, x, gridHeight);
        }
    }
}