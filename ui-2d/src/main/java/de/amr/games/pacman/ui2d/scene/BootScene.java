/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.model.GameModel;
import javafx.geometry.Rectangle2D;

import static de.amr.games.pacman.lib.Globals.RND;
import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

    private double start; // seconds

    @Override
    public boolean isCreditVisible() {
        return false;
    }

    @Override
    public void init() {
        start = 1.0;
        setScoreVisible(false);
    }

    @Override
    public void update() {
        if (context.gameState().timer().atSecond(start + 3)) {
            context.gameController().terminateCurrentState();
        }
    }

    @Override
    public void draw() {
        spriteRenderer.setSpriteSheet(context().getSpriteSheet(context.game().variant()));
        var timer = context.gameState().timer();
        if (timer.tick() == 1) {
            clearCanvas();
        } else if (timer.betweenSeconds(start, start + 1) && timer.tick() % 4 == 0) {
            paintRandomHexCodes();
        } else if (timer.betweenSeconds(start + 1, start + 2.5) && timer.tick() % 4 == 0) {
            paintRandomSprites();
        } else if (timer.atSecond(start + 2.5)) {
            paintGrid();
        }
    }

    @Override
    protected void drawSceneContent() {
        // not used here
    }

    private void paintRandomHexCodes() {
        clearCanvas();
        g.setFill(context.theme().color("palette.pale"));
        g.setFont(sceneFont(8));
        for (int row = 0; row < GameModel.ARCADE_MAP_TILES_Y; ++row) {
            for (int col = 0; col < GameModel.ARCADE_MAP_TILES_X; ++col) {
                var hexCode = Integer.toHexString(RND.nextInt(16));
                g.fillText(hexCode, s(t(col)), s(t(row + 1)));
            }
        }
    }

    private void paintRandomSprites() {
        clearCanvas();
        for (int row = 0; row < GameModel.ARCADE_MAP_TILES_Y / 2; ++row) {
            if (RND.nextInt(100) > 33) {
                var region1 = randomSpriteSheetTile();
                var region2 = randomSpriteSheetTile();
                var splitX = GameModel.ARCADE_MAP_TILES_X / 8 + RND.nextInt(GameModel.ARCADE_MAP_TILES_X / 4);
                for (int col = 0; col < GameModel.ARCADE_MAP_TILES_X / 2; ++col) {
                    var region = col < splitX ? region1 : region2;
                    spriteRenderer.drawSpriteScaled(g, region, region.getWidth() * col, region.getHeight() * row);
                }
            }
        }
    }

    private Rectangle2D randomSpriteSheetTile() {
        var spriteSheet = context.getSpriteSheet(context.game().variant());
        var source = spriteSheet.source();
        var raster = spriteSheet.raster();
        double x = RND.nextDouble() * (source.getWidth() - raster);
        double y = RND.nextDouble() * (source.getHeight() - raster);
        return new Rectangle2D(x, y, raster, raster);
    }

    private void paintGrid() {
        clearCanvas();
        double width = t(28), height = t(36), raster = 16;
        var numRows = GameModel.ARCADE_MAP_TILES_Y / 2;
        var numCols = GameModel.ARCADE_MAP_TILES_X / 2;
        g.setStroke(context.theme().color("palette.pale"));
        g.setLineWidth(s(2.0));
        for (int row = 0; row <= numRows; ++row) {
            g.setLineWidth(row == 0 || row == numRows ? s(4.0) : s(2.0));
            g.strokeLine(0, s(row * raster), s(width), s(row * raster));
        }
        for (int col = 0; col <= numCols; ++col) {
            g.setLineWidth(col == 0 || col == numCols ? s(4.0) : s(2.0));
            g.strokeLine(s(col * raster), 0, s(col * raster), s(height));
        }
    }
}