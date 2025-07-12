/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;

import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.Globals.theGameContext;
import static de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.ui.PacManGames.theSound;
import static de.amr.pacmanfx.ui.PacManGames.theUI;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.ui._2d.GameRenderer.fillCanvas;

/**
 * The boot screen is showing some strange screen patterns and eventually  a grid.
 * This scene tries to mimic that to a certain degree.
 */
public class ArcadeCommon_BootScene2D extends GameScene2D {

    private static final int FRAGMENT_SIZE = 16;

    private Vector2f minPoint, maxPoint;

    public ArcadeCommon_BootScene2D(GameContext gameContext) {
        super(gameContext);
    }

    @Override
    public void doInit() {
        gameContext.theGame().hud().showScore(false);
        gameContext.theGame().hud().showLevelCounter(false);
        gameContext.theGame().hud().showLivesCounter(false);

        SpriteSheet<?> spriteSheet = theUI().configuration().spriteSheet();
        double width = spriteSheet.sourceImage().getWidth(), height = spriteSheet.sourceImage().getHeight();
        // ignore left half of sprite sheet image containing maze images
        minPoint = Vector2f.of(width / 2, 0);
        maxPoint = Vector2f.of(width - FRAGMENT_SIZE, height - FRAGMENT_SIZE);
        theSound().playVoice(SoundID.VOICE_EXPLAIN, 0);
    }

    @Override
    protected void doEnd() {}

    @Override
    public void update() {
        if (gameContext.theGameState().timer().atSecond(4)) {
            gameContext.theGameController().letCurrentGameStateExpire();
        }
    }

    @Override
    public Vector2f sizeInPx() { return ARCADE_MAP_SIZE_IN_PIXELS; }

    @Override
    public SpriteGameRenderer gr() {
        return (SpriteGameRenderer) super.gr();
    }

    @Override
    public void draw() {
        gr().setScaling(scaling());
        if (gameContext.theGameState().timer().tickCount() == 1) {
            fillCanvas(canvas, backgroundColor());
        } else {
            drawSceneContent();
        }
    }

    @Override
    public void drawSceneContent() {
        TickTimer timer = gameContext.theGameState().timer();
        if (timer.betweenSeconds(1, 2) && timer.tickCount() % 4 == 0) {
            fillCanvas(canvas, backgroundColor());
            drawRandomHexDigits();
        } else if (timer.betweenSeconds(2, 3.5) && timer.tickCount() % 4 == 0) {
            fillCanvas(canvas, backgroundColor());
            drawRandomSpriteFragments();
        } else if (timer.atSecond(3.5)) {
            fillCanvas(canvas, backgroundColor());
            drawGridLines();
        }
    }

    private void drawRandomHexDigits() {
        int numRows = (int) (ARCADE_MAP_SIZE_IN_PIXELS.y() / TS);
        int numCols = (int) (ARCADE_MAP_SIZE_IN_PIXELS.x() / TS);
        ctx().setFill(ARCADE_WHITE);
        ctx().setFont(scaledArcadeFont8());
        for (int row = 0; row < numRows; ++row) {
            double y = scaled(tiles_to_px(row + 1));
            for (int col = 0; col < numCols; ++col) {
                int hexDigit = gameContext.theRNG().nextInt(16);
                ctx().fillText(Integer.toHexString(hexDigit), scaled(tiles_to_px(col)), y);
            }
        }
    }

    private void drawRandomSpriteFragments() {
        int numRows = (int) (ARCADE_MAP_SIZE_IN_PIXELS.y() / FRAGMENT_SIZE);
        int numCols = (int) (ARCADE_MAP_SIZE_IN_PIXELS.x() / FRAGMENT_SIZE);
        for (int row = 0; row < numRows; ++row) {
            if (gameContext.theRNG().nextInt(100) < 20) continue;
            RectShort fragment1 = randomSpriteFragment(), fragment2 = randomSpriteFragment();
            int split = numCols / 8 + gameContext.theRNG().nextInt(numCols / 4);
            for (int col = 0; col < numCols; ++col) {
                gr().drawSpriteScaled(col < split ? fragment1 : fragment2, FRAGMENT_SIZE * col, FRAGMENT_SIZE * row);
            }
        }
    }

    private RectShort randomSpriteFragment() {
        return new RectShort(
            (int) lerp(minPoint.x(), maxPoint.x(), gameContext.theRNG().nextDouble()),
            (int) lerp(minPoint.y(), maxPoint.y(), gameContext.theRNG().nextDouble()),
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