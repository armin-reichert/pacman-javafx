/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.House;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.MapCategory;
import de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_GameModel;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.Stork;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.rendering.GameLevelRenderer;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.NonArcadeMapsSpriteSheet.MazeID.MAZE32_ANIMATED;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_GameLevelRenderer extends GameLevelRenderer {

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);
    private final TengenMsPacMan_UIConfig uiConfig;

    public TengenMsPacMan_GameLevelRenderer(TengenMsPacMan_UIConfig uiConfig, Canvas canvas) {
        super(canvas, uiConfig.spriteSheet());
        this.uiConfig = requireNonNull(uiConfig);
        ctx().setImageSmoothing(false);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return uiConfig.spriteSheet();
    }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColor; }

    public Color backgroundColor() { return backgroundColor.get(); }

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
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (actor.isVisible()) {
            switch (actor) {
                case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
                case Bonus bonus -> drawMovingBonus(bonus);
                case Pac pac -> drawAnyKindOfPac(pac);
                case Stork stork -> {
                    super.drawActor(stork);
                    if (stork.isBagReleasedFromBeak()) {
                        hideStorkBag(stork);
                    }
                }
                default -> super.drawActor(actor);
            }
        }
    }

    public void drawMovingBonus(Bonus bonus) {
        if (bonus.state() == BonusState.INACTIVE) return;
        ctx().save();
        ctx().translate(0, bonus.jumpHeight());
        switch (bonus.state()) {
            case EDIBLE -> {
                RectShort sprite = uiConfig.spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS)[bonus.symbol()];
                drawSpriteCentered(bonus.center(), sprite);
            }
            case EATEN  -> {
                RectShort sprite = uiConfig.spriteSheet().spriteSequence(SpriteID.BONUS_VALUES)[bonus.symbol()];
                drawSpriteCentered(bonus.center(), sprite);
            }
        }
        ctx().restore();
    }

    private void drawAnyKindOfPac(Pac pac) {
        pac.animations().map(SpriteAnimationManager.class::cast).ifPresent(spriteAnimations -> {
            SpriteAnimation spriteAnimation = spriteAnimations.currentAnimation();
            if (spriteAnimation == null) {
                Logger.error("No sprite animation found for {}", pac);
                return;
            }
            if (ANIM_PAC_DYING.equals(spriteAnimations.selectedID())) {
                drawPacDyingAnimation(pac, spriteAnimation);
            } else {
                drawActorSprite(pac, pac.moveDir(), spriteAnimation.currentSprite());
            }
        });
    }

    // Simulates dying animation by providing the right direction for each animation frame
    private void drawPacDyingAnimation(Pac pac, SpriteAnimation animation) {
        Direction dir = Direction.DOWN;
        if (animation.frameIndex() < 11) {
            dir = switch (animation.frameIndex() % 4) {
                case 1 -> Direction.LEFT;
                case 2 -> Direction.UP;
                case 3 -> Direction.RIGHT;
                default -> Direction.DOWN; // start with DOWN
            };
        }
        drawActorSprite(pac, dir, animation.currentSprite());
    }

    // There are only left-pointing Ms. Pac-Man sprites in the sprite sheet, so we rotate and mirror in the renderer
    private void drawActorSprite(MovingActor actor, Direction dir, RectShort sprite) {
        Vector2f center = actor.center().scaled(scaling());
        ctx().save();
        ctx().translate(center.x(), center.y());
        switch (dir) {
            case LEFT  -> {}
            case UP    -> ctx().rotate(90);
            case RIGHT -> ctx().scale(-1, 1);
            case DOWN  -> { ctx().scale(-1, 1); ctx().rotate(-90); }
        }
        drawSpriteCentered(0, 0, sprite);
        ctx().restore();
    }

    // Sprite sheet has no stork without bag under its beak so we over-paint the bag
    private void hideStorkBag(Stork stork) {
        ctx().setFill(backgroundColor());
        ctx().fillRect(scaled(stork.x() - 13), scaled(stork.y() + 3), scaled(8), scaled(10));
    }

    @Override
    public void drawGameLevel(GameContext gameContext, Color backgroundColor, boolean mazeBright, boolean energizerBright) {
        TengenMsPacMan_GameModel game = gameContext.game();
        int mapNumber = gameContext.gameLevel().worldMap().getConfigValue("mapNumber");
        applyLevelSettings(gameContext);

        // maze sprite set is now stored in world map configuration, take it from there:
        MazeSpriteSet mazeSpriteSet = gameContext.gameLevel().worldMap().getConfigValue(TengenMsPacMan_UIConfig.MAZE_SPRITE_SET_PROPERTY);

        //TODO this logic does not belong into the renderer
        RectShort mazeSprite = checkIfAnimatedMaze(game, mapNumber, mazeSpriteSet);
        drawGameLevel(gameContext, mazeSpriteSet.mazeImage().spriteSheetImage(), mazeSprite);
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

    public void drawLevelMessage(GameLevel level, Vector2f position, Font font) {
        requireNonNull(level);
        requireNonNull(position);
        requireNonNull(font);

        if (level.messageType() == GameLevel.MessageType.NONE) return;

        NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
        float x = position.x(), y = position.y() + TS;
        switch (level.messageType()) {
            case GameLevel.MessageType.READY
                -> fillTextCentered("READY!", uiConfig.assets().color("color.ready_message"), font, x, y);
            case GameLevel.MessageType.GAME_OVER -> {
                Color color = level.isDemoLevel()
                    ? Color.web(nesColorScheme.strokeColorRGB())
                    : uiConfig.assets().color("color.game_over_message");
                fillTextCentered("GAME OVER", color, font, x, y);
            }
            case GameLevel.MessageType.TEST
                -> fillTextCentered("TEST L%02d".formatted(level.number()), nesColor(0x28), font, x, y);
        }
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

    private void drawClapperBoard(Clapperboard clapperboard) {
        requireNonNull(clapperboard);
        if (!clapperboard.isVisible()) return;
        clapperboard.sprite().ifPresent(sprite -> {
            double numberX = clapperboard.x() + 8, numberY = clapperboard.y() + 18; // baseline
            drawSpriteCentered(clapperboard.center(), sprite);
            // over-paint number from sprite sheet
            ctx().save();
            ctx().scale(scaling(), scaling());
            ctx().setFill(backgroundColor());
            ctx().fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx().restore();

            ctx().setFont(clapperboard.font());
            ctx().setFill(nesColor(0x20));
            ctx().fillText(String.valueOf(clapperboard.number()), scaled(numberX), scaled(numberY));
            if (clapperboard.isTextVisible()) {
                double textX = clapperboard.x() + sprite.width(), textY = clapperboard.y() + 2;
                ctx().fillText(clapperboard.text(), scaled(textX), scaled(textY));
            }
        });
    }
}