/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.common;

import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE_IN_PIXELS;
import static de.amr.games.pacman.model.pacman.PacManArcadeGame.ARCADE_MAP_SIZE_IN_TILES;
import static de.amr.games.pacman.ui2d.GameAssets2D.ARCADE_PALE;

public class ArcadeBootScene extends GameScene2D {

    @Override
    public void bindGameActions() {}

    @Override
    public void doInit() {
        context.setScoreVisible(false);
    }

    @Override
    public void update() {
        if (context.gameState().timer().atSecond(4)) {
            context.gameController().terminateCurrentState();
        }
    }

    @Override
    public Vector2f size() {
        return ARCADE_MAP_SIZE_IN_PIXELS;
    }

    @Override
    public void draw(GameRenderer renderer) {
        Vector2f sceneSize = size();
        renderer.setScaling(scaling());
        renderer.setBackgroundColor(backgroundColor());
        var timer = context.gameState().timer();
        if (timer.currentTick() == 1) {
            renderer.clearCanvas();
        } else if (timer.betweenSeconds(1, 2) && timer.currentTick() % 8 == 0) {
            paintRandomHexCodes(renderer, sceneSize);
        } else if (timer.betweenSeconds(2, 3.5) && timer.currentTick() % 4 == 0) {
            paintRandomSprites(context.currentGameSceneConfig().spriteSheet().sourceImage(), renderer, sceneSize);
        } else if (timer.atSecond(3.5)) {
            paintTestGrid(renderer, sceneSize);
        }
    }

    @Override
    protected void drawSceneContent(GameRenderer renderer) {}

    private void paintRandomHexCodes(GameRenderer renderer, Vector2f sceneSize) {
        renderer.clearCanvas();
        renderer.ctx().setFill(ARCADE_PALE);
        renderer.ctx().setFont(renderer.scaledArcadeFont(TS));
        int numRows = (int) (sceneSize.y() / TS), numCols = (int) (sceneSize.x() / TS);
        for (int row = 0; row < numRows; ++row) {
            for (int col = 0; col < numCols; ++col) {
                var hexCode = Integer.toHexString(RND.nextInt(16));
                renderer.ctx().fillText(hexCode, scaled(t(col)), scaled(t(row + 1)));
            }
        }
    }

    private void paintRandomSprites(Image spriteImage, GameRenderer renderer, Vector2f sceneSize) {
        renderer.clearCanvas();
        int numRows = (int) (sceneSize.y() / TS), numCols = (int) (sceneSize.x() / TS);
        for (int row = 0; row < numRows / 2; ++row) {
            if (RND.nextInt(100) > 20) {
                var region1 = randomSpriteSheetTile(spriteImage);
                var region2 = randomSpriteSheetTile(spriteImage);
                var splitX = numCols / 8 + RND.nextInt(numCols / 4);
                for (int col = 0; col < numCols / 2; ++col) {
                    var region = col < splitX ? region1 : region2;
                    renderer.drawSpriteScaled(region, region.width() * col, region.height() * row);
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
    private void paintTestGrid(GameRenderer renderer, Vector2f sceneSize) {
        GraphicsContext g = renderer.ctx();
        renderer.clearCanvas();
        Vector2i sizeInTiles = context.worldSizeInTilesOrElse(ARCADE_MAP_SIZE_IN_TILES);
        int numRows = sizeInTiles.y() / 2, numCols = sizeInTiles.y() / 2;
        g.setStroke(ARCADE_PALE);
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