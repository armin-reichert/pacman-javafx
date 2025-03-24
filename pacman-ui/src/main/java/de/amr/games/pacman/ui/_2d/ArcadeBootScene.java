/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui._2d;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.arcade.Arcade;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.Globals.*;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.lib.arcade.Arcade.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.games.pacman.ui.UIGlobals.THE_GAME_CONTEXT;

public class ArcadeBootScene extends GameScene2D {

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        THE_GAME_CONTEXT.setScoreVisible(false);
    }

    @Override
    public void update() {
        if (THE_GAME_CONTROLLER.state().timer().atSecond(4)) {
            THE_GAME_CONTROLLER.terminateCurrentState();
        }
    }

    @Override
    public Vector2f sizeInPx() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void draw() {
        Vector2f sceneSize = sizeInPx();
        gr.setScaling(scaling());
        gr.setBackgroundColor(backgroundColor());
        var timer = THE_GAME_CONTROLLER.state().timer();
        if (timer.tickCount() == 1) {
            gr.clearCanvas();
        } else if (timer.betweenSeconds(1, 2) && timer.tickCount() % 8 == 0) {
            paintRandomHexCodes(sceneSize);
        } else if (timer.betweenSeconds(2, 3.5) && timer.tickCount() % 4 == 0) {
            paintRandomSprites(THE_GAME_CONTEXT.gameConfiguration().spriteSheet().sourceImage(), sceneSize);
        } else if (timer.atSecond(3.5)) {
            paintScreenTestGrid(sceneSize);
        }
    }

    @Override
    protected void drawSceneContent() {}

    private void paintRandomHexCodes(Vector2f sceneSize) {
        gr.clearCanvas();
        gr.ctx().setFill(Color.web(Arcade.Palette.WHITE));
        gr.ctx().setFont(gr.scaledArcadeFont(TS));
        int numRows = (int) (sceneSize.y() / TS), numCols = (int) (sceneSize.x() / TS);
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                var hexCode = Integer.toHexString(RND.nextInt(16));
                gr.ctx().fillText(hexCode, scaled(tiles2Px(col)), scaled(tiles2Px(row + 1)));
            }
        }
    }

    private void paintRandomSprites(Image spriteImage, Vector2f sceneSize) {
        gr.clearCanvas();
        int numRows = (int) (sceneSize.y() / TS), numCols = (int) (sceneSize.x() / TS);
        for (int row = 0; row < numRows / 2; ++row) {
            if (RND.nextInt(100) > 20) {
                var region1 = randomSpriteSheetTile(spriteImage);
                var region2 = randomSpriteSheetTile(spriteImage);
                var splitX = numCols / 8 + RND.nextInt(numCols / 4);
                for (int col = 0; col < numCols / 2; ++col) {
                    var region = col < splitX ? region1 : region2;
                    gr.drawSpriteScaled(region, region.width() * col, region.height() * row);
                }
            }
        }
    }

    private RectArea randomSpriteSheetTile(Image spriteImage) {
        int x = (int) (RND.nextDouble() * (spriteImage.getWidth() - 16));
        int y = (int) (RND.nextDouble() * (spriteImage.getHeight() - 16));
        return new RectArea(x, y, 16, 16);
    }

    // was probably used to correct screen geometry
    private void paintScreenTestGrid(Vector2f sceneSize) {
        GraphicsContext g = gr.ctx();
        gr.clearCanvas();
        Vector2i sizeInTiles = THE_GAME_CONTEXT.worldSizeInTilesOrElse(ARCADE_MAP_SIZE_IN_TILES);
        int numRows = sizeInTiles.y() / 2, numCols = sizeInTiles.y() / 2;
        g.setStroke(Color.web(Arcade.Palette.WHITE));
        g.setLineWidth(scaled(2.0));
        for (int row = 0; row <= numRows; ++row) {
            g.setLineWidth(row == 0 || row == numRows ? scaled(4.0) : scaled(2.0));
            g.strokeLine(0, scaled(row * 16), scaled(sceneSize.x()), scaled(row * 16));
        }
        for (int col = 0; col <= numCols; ++col) {
            g.setLineWidth(col == 0 || col == numCols ? scaled(4.0) : scaled(2.0));
            g.strokeLine(scaled(col * 16), 0, scaled(col * 16), scaled(sceneSize.y()));
        }
    }
}