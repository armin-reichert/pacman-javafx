/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.ui._2d.GameScene2D;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.games.pacman.ui.GameAssets.ARCADE_WHITE;
import static de.amr.games.pacman.ui.Globals.THE_UI_CONFIGS;

public class ArcadeAny_BootScene2D extends GameScene2D {

    private static final int FRAGMENT_SIZE = 16;

    private double minX, maxX, minY, maxY;

    @Override
    public void doInit() {
        game().scoreManager().setScoreVisible(false);
        Image spriteImage = THE_UI_CONFIGS.current().spriteSheet().sourceImage();
        // ignore left half of sprite sheet image
        minX = spriteImage.getWidth() / 2;
        maxX = spriteImage.getWidth() - FRAGMENT_SIZE;
        minY = 0;
        maxY = spriteImage.getHeight() - FRAGMENT_SIZE;
    }

    @Override
    public void update() {
        if (gameState().timer().atSecond(4)) {
            THE_GAME_CONTROLLER.letCurrentStateExpire();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void draw() {
        gr.setScaling(scaling());
        if (gameState().timer().tickCount() == 1) {
            gr.fillCanvas(backgroundColor());
        } else {
            drawSceneContent();
        }
    }

    @Override
    protected void drawSceneContent() {
        TickTimer timer = gameState().timer();
        if (timer.betweenSeconds(1, 2) && timer.tickCount() % 4 == 0) {
            gr.fillCanvas(backgroundColor());
            paintRandomHexCodes();
        } else if (timer.betweenSeconds(2, 3.5) && timer.tickCount() % 4 == 0) {
            gr.fillCanvas(backgroundColor());
            paintRandomSpriteFragments();
        } else if (timer.atSecond(3.5)) {
            gr.fillCanvas(backgroundColor());
            paintScreenTestGrid();
        }
    }

    private void paintRandomHexCodes() {
        final Vector2f sceneSize = sizeInPx();
        final Font font = arcadeFontScaledTS();
        final int numRows = (int) (sceneSize.y() / TS), numCols = (int) (sceneSize.x() / TS);
        gr.ctx().setFill(ARCADE_WHITE);
        gr.ctx().setFont(font);
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                var hexCode = Integer.toHexString(THE_RNG.nextInt(16));
                gr.ctx().fillText(hexCode, scaled(tiles_to_px(col)), scaled(tiles_to_px(row + 1)));
            }
        }
    }

    private void paintRandomSpriteFragments() {
        final Vector2f sceneSize = sizeInPx();
        final int numFragmentsX = (int) (sceneSize.x() / FRAGMENT_SIZE);
        final int numFragmentsY = (int) (sceneSize.y() / FRAGMENT_SIZE);
        for (int row = 0; row < numFragmentsY; ++row) {
            if (THE_RNG.nextInt(100) < 20) continue;
            RectArea fragment1 = randomFragment(), fragment2 = randomFragment();
            int split = numFragmentsX / 8 + THE_RNG.nextInt(numFragmentsX / 4);
            for (int col = 0; col < numFragmentsX; ++col) {
                gr.drawSpriteScaled(col < split ? fragment1 : fragment2, FRAGMENT_SIZE * col, FRAGMENT_SIZE * row);
            }
        }
    }

    private RectArea randomFragment() {
        return new RectArea(
            (int) lerp(minX, maxX, THE_RNG.nextDouble()),
            (int) lerp(minY, maxY, THE_RNG.nextDouble()),
            FRAGMENT_SIZE, FRAGMENT_SIZE);
    }

    // was probably used to correct screen geometry
    private void paintScreenTestGrid() {
        Vector2f sceneSize = sizeInPx();
        Vector2i sizeInTiles = ARCADE_MAP_SIZE_IN_TILES;
        int numRows = sizeInTiles.y() / 2, numCols = sizeInTiles.y() / 2;
        gr.ctx().setStroke(ARCADE_WHITE);
        gr.ctx().setLineWidth(scaled(2.0));
        for (int row = 0; row <= numRows; ++row) {
            gr.ctx().setLineWidth(row == 0 || row == numRows ? scaled(4.0) : scaled(2.0));
            gr.ctx().strokeLine(0, scaled(row * 16), scaled(sceneSize.x()), scaled(row * 16));
        }
        for (int col = 0; col <= numCols; ++col) {
            gr.ctx().setLineWidth(col == 0 || col == numCols ? scaled(4.0) : scaled(2.0));
            gr.ctx().strokeLine(scaled(col * 16), 0, scaled(col * 16), scaled(sceneSize.y()));
        }
    }
}