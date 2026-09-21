/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman.gamescene.bootscene;

import de.amr.basics.util.Ufx;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.text.Font;

import java.util.Optional;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

/**
 * Boot scene simulating the boot process of the Arcade machine. Shows random hex codes, sprite fragments
 * and a grid before the intro scene starts. This scene is used by the Arcade and the XXL variants so we pass the
 * corresponding spritesheet as a parameter.
 */
public class Arcade_BootScene_Renderer extends BaseRenderer {

    private final SpriteSheet<?> spriteSheet;

    public Arcade_BootScene_Renderer(Canvas canvas, SpriteSheet<?> spriteSheet) {
        super(canvas);
        this.spriteSheet = requireNonNull(spriteSheet);
    }

    @Override
    public Optional<SpriteSheet<?>> optSpriteSheet() {
        return Optional.of(spriteSheet);
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
        final Font arcade8 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), scaled(TS));
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
        for (int row = 0; row < block.numSpritesY(); ++row) {
            for (int col = 0; col < block.numSpritesX(); ++col) {
                int i = row * block.numSpritesX() + col;
                drawSprite(block.sprites()[i], block.spriteSize() * col, block.spriteSize() * row, true);
            }
        }
    }

    private void renderGridPattern(GridPattern grid) {
        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.setStroke(ARCADE_WHITE);
        for (int row = 0; row < grid.height(); ++row) {
            final int y = row * grid.cellSize();
            ctx.strokeLine(0, y, grid.width() * TS, y);
        }
        for (int col = 0; col < grid.width(); ++col) {
            final int x = col * grid.cellSize();
            ctx.strokeLine(x, 0, x, grid.height() * TS);
        }
        ctx.restore();
    }
}