/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.games.pacman.ui.Globals.*;

public class ArcadeBootScene2D extends GameScene2D {

    public static final Color WHITE = Color.web(Arcade.Palette.WHITE);

    private boolean isTickMultipleOf4;
    private boolean isTickMultipleOf8;

    @Override
    public void doInit() {
        game().setScoreVisible(false);
    }

    @Override
    public void update() {
        long tick = gameState().timer().tickCount();
        isTickMultipleOf4 = tick % 4 == 0;
        isTickMultipleOf8 = tick % 8 == 0;
        if (gameState().timer().atSecond(4)) {
            THE_GAME_CONTROLLER.terminateCurrentState();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void draw() {
        long tick = gameState().timer().tickCount();
        gr.setScaling(scaling());
        if (tick == 1) {
            gr.fillCanvas(backgroundColor());
        } else if (gameState().timer().betweenSeconds(1, 2) && isTickMultipleOf8) {
            paintRandomHexCodes(sizeInPx());
        } else if (gameState().timer().betweenSeconds(2, 3.5) && isTickMultipleOf4) {
            paintRandomSprites(sizeInPx());
        } else if (gameState().timer().atSecond(3.5)) {
            paintScreenTestGrid(sizeInPx());
        }
    }

    @Override
    protected void drawSceneContent() {}

    private void paintRandomHexCodes(Vector2f sceneSize) {
        Font font = THE_ASSETS.arcadeFontAtSize(scaled(TS));
        gr.fillCanvas(backgroundColor());
        gr.ctx().setFill(WHITE);
        gr.ctx().setFont(font);
        int numRows = (int) (sceneSize.y() / TS), numCols = (int) (sceneSize.x() / TS);
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                var hexCode = Integer.toHexString(RND.nextInt(16));
                gr.ctx().fillText(hexCode, scaled(tiles_to_px(col)), scaled(tiles_to_px(row + 1)));
            }
        }
    }

    private void paintRandomSprites(Vector2f sceneSize) {
        Image spriteImage = THE_UI_CONFIGS.current().spriteSheet().sourceImage();
        gr.fillCanvas(backgroundColor());
        int numRows = (int) (sceneSize.y() / TS), numCols = (int) (sceneSize.x() / TS);
        for (int row = 0; row < numRows / 2; ++row) {
            if (RND.nextInt(100) > 20) {
                var region1 = randomImageRegion(spriteImage, 16);
                var region2 = randomImageRegion(spriteImage, 16);
                var splitX = numCols / 8 + RND.nextInt(numCols / 4);
                for (int col = 0; col < numCols / 2; ++col) {
                    var region = col < splitX ? region1 : region2;
                    gr.drawSpriteScaled(region, region.width() * col, region.height() * row);
                }
            }
        }
    }

    private RectArea randomImageRegion(Image image, int size) {
        int x = (int) (RND.nextDouble() * (image.getWidth() - size));
        int y = (int) (RND.nextDouble() * (image.getHeight() - size));
        return new RectArea(x, y, size, size);
    }

    // was probably used to correct screen geometry
    private void paintScreenTestGrid(Vector2f sceneSize) {
        gr.fillCanvas(backgroundColor());
        Vector2i sizeInTiles = levelSizeInTilesOrElse(ARCADE_MAP_SIZE_IN_TILES);
        int numRows = sizeInTiles.y() / 2, numCols = sizeInTiles.y() / 2;
        gr.ctx().setStroke(WHITE);
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