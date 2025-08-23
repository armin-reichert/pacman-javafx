/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.MessageType;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.NonArcadeMapsSpriteSheet.MazeID.MAZE32_ANIMATED;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_GameLevelRenderer extends GameLevelRenderer implements SpriteRendererMixin {

    private final TengenMsPacMan_UIConfig uiConfig;

    public TengenMsPacMan_GameLevelRenderer(Canvas canvas, TengenMsPacMan_UIConfig uiConfig) {
        super(canvas);
        this.uiConfig = requireNonNull(uiConfig);
        ctx().setImageSmoothing(false);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return uiConfig.spriteSheet();
    }

    @Override
    public void applyLevelSettings(GameContext gameContext) {
        GameLevel gameLevel = gameContext.gameLevel();
        WorldMap worldMap = gameLevel.worldMap();
        // store the maze sprite set with the correct colors for this level in the map configuration:
        if (!worldMap.hasConfigValue(TengenMsPacMan_UIConfig.MAZE_SPRITE_SET_PROPERTY)) {
            int numFlashes = gameLevel.data().numFlashes();
            MazeSpriteSet mazeSpriteSet = uiConfig.createMazeSpriteSet(worldMap, numFlashes);
            worldMap.setConfigValue(TengenMsPacMan_UIConfig.MAZE_SPRITE_SET_PROPERTY, mazeSpriteSet);
            Logger.info("Maze sprite set created: {}", mazeSpriteSet);
        }
    }

    @Override
    public void drawGameLevel(GameContext gameContext, Color backgroundColor, boolean mazeBright, boolean energizerBright) {
        TengenMsPacMan_GameModel game = gameContext.game();
        GameLevel gameLevel = gameContext.gameLevel();
        int mapNumber = gameLevel.worldMap().getConfigValue("mapNumber");
        applyLevelSettings(gameContext);

        // maze sprite set is now stored in world map configuration, take it from there:
        MazeSpriteSet mazeSpriteSet = gameLevel.worldMap().getConfigValue(TengenMsPacMan_UIConfig.MAZE_SPRITE_SET_PROPERTY);

        //TODO this logic does not belong into the renderer
        RectShort mazeSprite = checkIfAnimatedMaze(game, mapNumber, mazeSpriteSet);
        drawGameLevel(gameContext, mazeSpriteSet.mazeImage().spriteSheetImage(), mazeSprite);

        drawMessage(gameLevel);
    }

    public void drawGameLevel(GameContext gameContext, Image mazeImage, RectShort mazeSprite) {
        ctx().setImageSmoothing(false);
        int x = 0, y = GameLevel.EMPTY_ROWS_OVER_MAZE * TS;
        ctx().drawImage(mazeImage,
            mazeSprite.x(), mazeSprite.y(), mazeSprite.width(), mazeSprite.height(),
            scaled(x), scaled(y), scaled(mazeSprite.width()), scaled(mazeSprite.height())
        );
        overPaintActorSprites(gameContext.gameLevel());
        drawFood(gameContext.gameLevel());
        drawMessage(gameContext.gameLevel());
    }

    private RectShort checkIfAnimatedMaze(TengenMsPacMan_GameModel game, int mapNumber, MazeSpriteSet mazeSpriteSet) {
        if (game.mapCategory() == MapCategory.STRANGE && mapNumber == 15) {
            int spriteIndex = mazeAnimationSpriteIndex(uiConfig.theUI().clock().tickCount());
            return uiConfig.nonArcadeMapsSpriteSheet().spriteSequence(MAZE32_ANIMATED)[spriteIndex];
        }
        return mazeSpriteSet.mazeImage().sprite();
    }

    /*
       Strange map #15 (maze #32): psychedelic animation:
       Frame pattern: (00000000 11111111 22222222 11111111)+, numFrames = 4, frameDuration = 8
     */
    private int mazeAnimationSpriteIndex(long tick) {
        long block = (tick % 32) / 8;
        return (int) (block < 3 ? block : 1);
    }

    private void drawFood(GameLevel gameLevel) {
        requireNonNull(gameLevel);
        ctx().save();
        ctx().scale(scaling(), scaling());
        MazeSpriteSet recoloredMaze =  gameLevel.worldMap().getConfigValue(TengenMsPacMan_UIConfig.MAZE_SPRITE_SET_PROPERTY);
        Color pelletColor = Color.web(recoloredMaze.mazeImage().colorScheme().pelletColorRGB());
        drawPellets(gameLevel, pelletColor);
        drawEnergizers(gameLevel, pelletColor);
        ctx().restore();
    }

    private void drawPellets(GameLevel gameLevel, Color pelletColor) {
        gameLevel.worldMap().tiles().filter(gameLevel::isFoodPosition).filter(not(gameLevel::isEnergizerPosition)).forEach(tile -> {
            ctx().setFill(backgroundColor());
            fillSquareAtTileCenter(tile, 4);
            if (!gameLevel.tileContainsEatenFood(tile)) {
                // draw pellet using the right color
                ctx().setFill(pelletColor);
                fillSquareAtTileCenter(tile, 2);
            }
        });
    }

    private void drawEnergizers(GameLevel level, Color pelletColor) {
        double size = TS;
        double offset = 0.5 * HTS;
        level.worldMap().tiles().filter(level::isEnergizerPosition).forEach(tile -> {
            ctx().setFill(backgroundColor());
            fillSquareAtTileCenter(tile, TS + 2);
            if (!level.tileContainsEatenFood(tile) && level.blinking().isOn()) {
                ctx().setFill(pelletColor);
                // draw pixelated "circle"
                double cx = tile.x() * TS, cy = tile.y() * TS;
                ctx().fillRect(cx + offset, cy, HTS, size);
                ctx().fillRect(cx, cy + offset, size, HTS);
                ctx().fillRect(cx + 1, cy + 1, size - 2, size - 2);
            }
        });
    }

    private void drawMessage(GameLevel gameLevel) {
        gameLevel.optMessage().ifPresent(message -> {
            NES_ColorScheme colorScheme = gameLevel.worldMap().getConfigValue("nesColorScheme");
            switch (message.type()) {
                case MessageType.GAME_OVER -> {
                    Color color = gameLevel.isDemoLevel()
                        ? Color.web(colorScheme.strokeColorRGB())
                        : uiConfig.assets().color("color.game_over_message");
                    fillTextCentered("GAME  OVER",
                        color, arcadeFontTS(), message.x(), message.y());
                }
                case MessageType.READY -> fillTextCentered("READY!",
                    uiConfig.assets().color("color.ready_message"), arcadeFontTS(), message.x(), message.y());
                case MessageType.TEST -> fillTextCentered("TEST    L%02d".formatted(gameLevel.number()),
                    nesColor(0x28), arcadeFontTS(), message.x(), message.y());
            }
        });
    }

    private void overPaintActorSprites(GameLevel level) {
        House house = level.house().orElse(null);
        if (house == null) {
            Logger.error("No house exists in game level!");
            return;
        }

        double margin = scaled(1), halfMargin = 0.5f * margin;
        double s = scaled(TS);

        // Over-paint area at house bottom where the ghost sprites are shown in map
        var inHouseArea = new Rectangle2D(
            halfMargin + s * (house.minTile().x() + 1),
            halfMargin + s * (house.minTile().y() + 2),
            s * (house.sizeInTiles().x() - 2) - margin,
            s * 2 - margin
        );

        ctx().setFill(backgroundColor());
        ctx().fillRect(inHouseArea.getMinX(), inHouseArea.getMinY(), inHouseArea.getWidth(), inHouseArea.getHeight());

        // Now the actor sprites outside the house. Be careful not to over-paint nearby obstacle edges!
        Vector2i pacTile = level.worldMap().getTerrainTileProperty("pos_pac", Vector2i.of(14, 26));
        overPaintActorSprite(pacTile, margin);

        Vector2i redGhostTile = level.worldMap().getTerrainTileProperty("pos_ghost_1_red", Vector2i.of(13, 14));
        overPaintActorSprite(redGhostTile, margin);
    }

    private void overPaintActorSprite(Vector2i tile, double margin) {
        double halfMargin = 0.5f * margin;
        double overPaintSize = scaled(2 * TS) - margin;
        ctx().fillRect(
            halfMargin + scaled(tile.x() * TS),
            halfMargin + scaled(tile.y() * TS - HTS),
            overPaintSize, overPaintSize);
    }
}