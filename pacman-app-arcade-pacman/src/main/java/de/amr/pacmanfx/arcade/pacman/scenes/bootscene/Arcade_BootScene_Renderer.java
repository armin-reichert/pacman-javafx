/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.scenes.bootscene;

import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.ui.GlobalAssets;
import de.amr.pacmanfx.ui.gamescene.common.GameScene;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.ui.gamescene.d2.BaseGameSceneDebugInfoRenderer.createDefaultSceneDebugRenderer;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

/**
 * Boot scene simulating the boot process of the Arcade machine. Shows random hex codes, sprite fragments
 * and a grid before the intro scene starts. This scene is used by the Arcade and the XXL variants so we pass the
 * corresponding spritesheet as a parameter.
 */
public class Arcade_BootScene_Renderer extends BaseRenderer implements SpriteRenderer {

    public static final int GRID_SIZE = 16;

    private final SpriteSheet<?> spriteSheet;

    public Arcade_BootScene_Renderer(GameScene gameScene, Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas);
        requireNonNull(gameScene);
        this.spriteSheet = requireNonNull(spriteSheet);

        setDebugInfoRenderer(createDefaultSceneDebugRenderer(gameScene, canvas));
    }

    @Override
    public SpriteSheet<?> spriteSheet() {
        return spriteSheet;
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case BlankCanvas _ -> clearCanvas();
            case HexDigitsBlock hexBlock -> renderHexCodeBlock(hexBlock);
            case SpritesBlock spritesBlock -> renderSpritesBlock(spritesBlock);
            case GridPattern gridPattern -> renderGridPattern(gridPattern);
            default -> {}
        }
    }

    private void renderHexCodeBlock(HexDigitsBlock block) {
        final Font arcade8 = Ufx.deriveFont(GlobalAssets.Fonts.ARCADE.font(), scaled(TS));
        ctx.setFill(ARCADE_WHITE);
        ctx.setFont(arcade8);
        for (int row = 0; row < block.height(); ++row) {
            final double y = scaled(TS * row);
            for (int col = 0; col < block.width(); ++col) {
                final double x = scaled(TS * col);
                final byte number = block.digits()[row][col];
                ctx.fillText(Integer.toHexString(number), x, y + scaled(TS)); // Note: y param is baseline!
            }
        }
    }

    private void renderSpritesBlock(SpritesBlock block) {
        for (int row = 0; row < block.height(); ++row) {
            for (int col = 0; col < block.width(); ++col) {
                int i = row * block.width() + col;
                drawSprite(block.sprites()[i], GRID_SIZE * col, GRID_SIZE * row, true);
            }
        }
    }

    private void renderGridPattern(GridPattern grid) {
        final double widthPixels = scaled(grid.width() * TS);
        final double heightPixels = scaled(grid.height() * TS);
        final int numRows = (int) (heightPixels / GRID_SIZE);
        final int numCols = (int) (widthPixels / GRID_SIZE);
        final double fineLine = scaled(2), normalLine = scaled(4);
        ctx.setStroke(ARCADE_WHITE);
        for (int row = 0; row <= numRows; ++row) {
            final double y = scaled(row * GRID_SIZE);
            ctx.setLineWidth(row == 0 || row == numRows ? normalLine : fineLine);
            ctx.strokeLine(0, y, widthPixels, y);
        }
        for (int col = 0; col <= numCols; ++col) {
            final double x = scaled(col * GRID_SIZE);
            ctx.setLineWidth(col == 0 || col == numCols ? normalLine : fineLine);
            ctx.strokeLine(x, 0, x, heightPixels);
        }
    }
}