/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.pacmanfx.arcade.pacman.rendering.ArcadePacMan_HUDRenderer;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.timer.TickTimer;
import de.amr.pacmanfx.ui._2d.GameScene2D;
import de.amr.pacmanfx.ui.api.GameUI;
import de.amr.pacmanfx.ui.api.GameUI_Config;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;

import java.util.Random;

import static de.amr.pacmanfx.Globals.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomFloat;
import static de.amr.pacmanfx.lib.RandomNumberSupport.randomInt;
import static de.amr.pacmanfx.lib.UsefulFunctions.lerp;
import static de.amr.pacmanfx.ui._2d.ArcadePalette.ARCADE_WHITE;

/**
 * The boot screen is showing some strange screen patterns and eventually  a grid.
 * This scene tries to mimic that to a certain degree.
 */
public class ArcadePacMan_BootScene2D extends GameScene2D {

    private static final int FRAGMENT_SIZE = 16;

    private ArcadePacMan_HUDRenderer hudRenderer;
    private SpriteRenderer spriteRenderer;

    private Vector2f minPoint, maxPoint;

    public ArcadePacMan_BootScene2D(GameUI ui) {
        super(ui);
    }

    @Override
    public void doInit() {
        GameUI_Config uiConfig = ui.currentConfig();
        // This can be different spritesheet types!
        SpriteSheet<?> spriteSheet = ui.currentConfig().spriteSheet();

        hudRenderer = new ArcadePacMan_HUDRenderer(uiConfig, canvas);
        spriteRenderer = new SpriteRenderer(canvas) {
            @Override
            public SpriteSheet<?> spriteSheet() {
                return uiConfig.spriteSheet();
            }
        };
        bindRendererScaling(hudRenderer, spriteRenderer);

        context().game().hudData().all(false);

        double width = spriteSheet.sourceImage().getWidth(), height = spriteSheet.sourceImage().getHeight();
        // ignore left half of sprite sheet image containing maze images
        minPoint = Vector2f.of(width / 2, 0);
        maxPoint = Vector2f.of(width - FRAGMENT_SIZE, height - FRAGMENT_SIZE);
        ui.soundManager().playVoice(SoundID.VOICE_EXPLAIN, 0);
    }

    @Override
    protected void doEnd() {}

    @Override
    public void update() {
        if (context().gameState().timer().atSecond(4)) {
            context().gameController().letCurrentGameStateExpire();
        }
    }

    @Override
    public Vector2f sizeInPx() { return ARCADE_MAP_SIZE_IN_PIXELS; }

    @Override
    public void draw() {
        if (context().gameState().timer().tickCount() == 1) {
            clear();
        } else {
            drawSceneContent();
        }
        drawHUD();
    }

    @Override
    public void drawHUD() {
        if (hudRenderer != null) {
            hudRenderer.drawHUD(context(), context().game().hudData(), sizeInPx());
        }
    }

    @Override
    public void drawSceneContent() {
        TickTimer timer = context().gameState().timer();
        if (timer.betweenSeconds(1, 2) && timer.tickCount() % 4 == 0) {
            clear();
            drawRandomHexDigits();
        } else if (timer.betweenSeconds(2, 3.5) && timer.tickCount() % 4 == 0) {
            clear();
            drawRandomSpriteFragments();
        } else if (timer.atSecond(3.5)) {
            clear();
            drawGridLines();
        }
    }

    private void drawRandomHexDigits() {
        var random = new Random();
        int numRows = (int) (ARCADE_MAP_SIZE_IN_PIXELS.y() / TS);
        int numCols = (int) (ARCADE_MAP_SIZE_IN_PIXELS.x() / TS);
        ctx().setFill(ARCADE_WHITE);
        ctx().setFont(spriteRenderer.arcadeFontTS());
        for (int row = 0; row < numRows; ++row) {
            double y = scaled(TS(row + 1));
            for (int col = 0; col < numCols; ++col) {
                int hexDigit = random.nextInt(16);
                ctx().fillText(Integer.toHexString(hexDigit), scaled(TS(col)), y);
            }
        }
    }

    private void drawRandomSpriteFragments() {
        int numRows = (int) (ARCADE_MAP_SIZE_IN_PIXELS.y() / FRAGMENT_SIZE);
        int numCols = (int) (ARCADE_MAP_SIZE_IN_PIXELS.x() / FRAGMENT_SIZE);
        for (int row = 0; row < numRows; ++row) {
            if (randomInt(0, 100) < 20) continue;
            RectShort fragment1 = randomSpriteFragment(), fragment2 = randomSpriteFragment();
            int split = numCols / 8 + randomInt(0, numCols / 4);
            for (int col = 0; col < numCols; ++col) {
                spriteRenderer.drawSprite(col < split ? fragment1 : fragment2, FRAGMENT_SIZE * col, FRAGMENT_SIZE * row, true);
            }
        }
    }

    private RectShort randomSpriteFragment() {
        return new RectShort(
            (int) lerp(minPoint.x(), maxPoint.x(), randomFloat(0, 1)),
            (int) lerp(minPoint.y(), maxPoint.y(), randomFloat(0, 1)),
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