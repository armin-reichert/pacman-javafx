/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene;

import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.ui2d.rendering.RectangularArea;

import static de.amr.games.pacman.lib.Globals.RND;
import static de.amr.games.pacman.lib.Globals.t;

/**
 * @author Armin Reichert
 */
public class BootScene extends GameScene2D {

    @Override
    public boolean isCreditVisible() {
        return false;
    }

    @Override
    public void init() {
        super.init();
        context.setScoreVisible(false);
    }

    @Override
    public void update() {
        if (context.gameState().timer().atSecond(4)) {
            context.gameController().terminateCurrentState();
        }
    }

    @Override
    public void draw() {
        context.renderer().scalingProperty().set(scalingPy.get());
        context.renderer().backgroundColorProperty().set(backgroundColorPy.get());
        var timer = context.gameState().timer();
        if (timer.currentTick() == 1) {
            clearCanvas();
        } else if (timer.betweenSeconds(1, 2) && timer.currentTick() % 4 == 0) {
            paintRandomHexCodes();
        } else if (timer.betweenSeconds(2, 3.5) && timer.currentTick() % 4 == 0) {
            paintRandomSprites();
        } else if (timer.atSecond(3.5)) {
            paintGrid();
        }
    }

    @Override
    protected void drawSceneContent() {
        // not used here
    }

    private void paintRandomHexCodes() {
        clearCanvas();
        g.setFill(context.assets().color("palette.pale"));
        g.setFont(sceneFont(8));
        for (int row = 0; row < GameModel.ARCADE_MAP_TILES_Y; ++row) {
            for (int col = 0; col < GameModel.ARCADE_MAP_TILES_X; ++col) {
                var hexCode = Integer.toHexString(RND.nextInt(16));
                g.fillText(hexCode, scaled(t(col)), scaled(t(row + 1)));
            }
        }
    }

    private void paintRandomSprites() {
        clearCanvas();
        for (int row = 0; row < GameModel.ARCADE_MAP_TILES_Y / 2; ++row) {
            if (RND.nextInt(100) > 20) {
                var region1 = randomSpriteSheetTile();
                var region2 = randomSpriteSheetTile();
                var splitX = GameModel.ARCADE_MAP_TILES_X / 8 + RND.nextInt(GameModel.ARCADE_MAP_TILES_X / 4);
                for (int col = 0; col < GameModel.ARCADE_MAP_TILES_X / 2; ++col) {
                    var region = col < splitX ? region1 : region2;
                    context.renderer().drawSpriteScaled(g, region, region.width() * col, region.height() * row);
                }
            }
        }
    }

    private RectangularArea randomSpriteSheetTile() {
        var spriteSheet = context.renderer().spriteSheet();
        var source = spriteSheet.sourceImage();
        var raster = 16;
        int x = (int) (RND.nextDouble() * ((int) source.getWidth() - raster));
        int y = (int) (RND.nextDouble() * ((int) source.getHeight() - raster));
        return new RectangularArea(x, y, raster, raster);
    }

    private void paintGrid() {
        clearCanvas();
        double width = t(28), height = t(36), raster = 16;
        var numRows = GameModel.ARCADE_MAP_TILES_Y / 2;
        var numCols = GameModel.ARCADE_MAP_TILES_X / 2;
        g.setStroke(context.assets().color("palette.pale"));
        g.setLineWidth(scaled(2.0));
        for (int row = 0; row <= numRows; ++row) {
            g.setLineWidth(row == 0 || row == numRows ? scaled(4.0) : scaled(2.0));
            g.strokeLine(0, scaled(row * raster), scaled(width), scaled(row * raster));
        }
        for (int col = 0; col <= numCols; ++col) {
            g.setLineWidth(col == 0 || col == numCols ? scaled(4.0) : scaled(2.0));
            g.strokeLine(scaled(col * raster), 0, scaled(col * raster), scaled(height));
        }
    }
}