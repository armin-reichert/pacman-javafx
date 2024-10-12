/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.tengen.TengenMsPacManGame;
import de.amr.games.pacman.model.tengen.TengenMsPacManGame.MapCategory;
import de.amr.games.pacman.model.tengen.TengenMsPacManGame.PacBooster;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.GameWorldRenderer;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
import de.amr.games.pacman.ui2d.scene.ms_pacman.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.RectArea.rect;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameRenderer implements GameWorldRenderer {

    // all colors picked from NES emulator
    public static final Color TENGEN_BABY_BLUE          = Color.web("#64b0ff");
    public static final Color TENGEN_PINK               = Color.web("#fe6ecc");
    public static final Color TENGEN_YELLOW             = Color.web("#bcbe00");
    public static final Color TENGEN_MARQUEE_COLOR      = Color.web("#b71e7b");
    public static final Color TENGEN_PAC_COLOR          = TENGEN_YELLOW;
    public static final Color TENGEN_RED_GHOST_COLOR    = Color.web("#6e0040");
    public static final Color TENGEN_PINK_GHOST_COLOR   = TENGEN_PINK;
    public static final Color TENGEN_CYAN_GHOST_COLOR   = Color.web("#155fd9");
    public static final Color TENGEN_ORANGE_GHOST_COLOR = Color.web("#b53120");

    // blue colors used in intro, dark to bright
    static final Color[]  SHADES_OF_BLUE = {
        Color.rgb(0, 42, 136), Color.rgb(21, 95, 217), Color.rgb(100, 176, 255), Color.rgb(192, 223, 255)
    };

    static Color shadeOfBlue(long tick, int ticksPerColor) {
        int ticksPerAnimation = (ticksPerColor * SHADES_OF_BLUE.length);
        int index = (int) (tick % ticksPerAnimation) / ticksPerColor;
        return SHADES_OF_BLUE[index];
    }

    // Maze images are taken from files "arcade_mazes.png" and "non_arcade_mazes.png" via AssetStorage

    /**
     * @param mapNumber number of Arcade map (1-9)
     * @param width map width in pixels
     * @param height map height in pixels
     * @return map area in Arcade maps image
     */
    private static RectArea arcadeMapArea(int mapNumber, int width, int height) {
        int index = mapNumber - 1;
        return new RectArea((index % 3) * width, (index / 3) * height, width, height);
    }

    /**
     * @param mapNumber number of non-Arcade map (1-37)
     * @param width map width in pixels
     * @param height map height in pixels
     * @return map area in non-Arcade maps image
     */
    private static RectArea nonArcadeMapArea(int mapNumber, int width, int height) {
        int col, y;
        switch (mapNumber) {
            case 1,2,3,4,5,6,7,8            -> { col = (mapNumber - 1);  y = 0;    }
            case 9,10,11,12,13,14,15,16     -> { col = (mapNumber - 9);  y = 248;  }
            case 17,18,19,20,21,22,23,24    -> { col = (mapNumber - 17); y = 544;  }
            case 25,26,27,28,29,30,31,32,33 -> { col = (mapNumber - 25); y = 840;  }
            case 34,35,36,37                -> { col = (mapNumber - 34); y = 1136; }
            default -> throw new IllegalArgumentException("Illegal non-Arcade map number: " + mapNumber);
        }
        return new RectArea(col * width, y, width, height);
    }

    // Map #32 has 3 different images to create a visual effect.
    private static final RectArea[] MAP_32_FRAMES = {
        rect(1568, 840, 224, 248), rect(1568, 1088, 224, 248), rect(1568, 1336, 224, 248),
    };

    private final AssetStorage assets;
    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final Image arcadeMazesImage;
    private final Image nonArcadeMazesImage;

    private ImageArea mapSprite;
    private boolean flashMode;
    private boolean blinkingOn;
    private Canvas canvas;

    public TengenMsPacManGameRenderer(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        terrainRenderer.scalingPy.bind(scalingPy);
        terrainRenderer.setMapBackgroundColor(backgroundColorPy.get());
        arcadeMazesImage = assets.image("tengen.mazes.arcade");
        nonArcadeMazesImage = assets.image("tengen.mazes.non_arcade");
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public Canvas canvas() {
        return canvas;
    }

    @Override
    public void setFlashMode(boolean flashMode) {
        this.flashMode = flashMode;
    }

    @Override
    public void setBlinkingOn(boolean blinkingOn) {
        this.blinkingOn = blinkingOn;
    }

    @Override
    public DoubleProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public ObjectProperty<Color> backgroundColorProperty() {
        return backgroundColorPy;
    }

    @Override
    public void drawAnimatedEntity(AnimatedEntity guy) {
        ctx().save();
        ctx().setImageSmoothing(false);
        if (guy instanceof Pac msPacMan) {
            drawMsPacMan(msPacMan);
        } else {
            GameWorldRenderer.super.drawAnimatedEntity(guy);
        }
        ctx().restore();
    }

    private void drawMsPacMan(Pac msPacMan) {
        if (!msPacMan.isVisible()) {
            return;
        }
        msPacMan.animations().ifPresent(animations -> {
            if (animations instanceof SpriteAnimationCollection spriteAnimations) {
                SpriteAnimation spriteAnimation = spriteAnimations.current();
                if (spriteAnimation != null) {
                    switch (animations.currentAnimationName()) {
                        case Pac.ANIM_MUNCHING,
                             Pac.ANIM_MUNCHING_BOOSTER -> drawRotatedTowardsDir(msPacMan, msPacMan.moveDir(), spriteAnimation);
                        case Pac.ANIM_DYING -> {
                            //TODO does not work yet!
                            drawSprite(msPacMan.entity(), spriteAnimation.spriteSheet(), spriteAnimation.currentSprite());
                        }
                        default -> GameWorldRenderer.super.drawAnimatedEntity(msPacMan);
                    }
                } else {
                    Logger.error("No current animation for character {}", msPacMan);
                }
            }
        });
    }

    private void drawRotatedTowardsDir(Creature guy, Direction dir, SpriteAnimation spriteAnimation) {
        Vector2f center = guy.center().scaled((float) scaling());
        ctx().save();
        ctx().translate(center.x(), center.y());
        switch (dir) {
            case UP    -> ctx().rotate(90);
            case LEFT  -> {}
            case RIGHT -> ctx().scale(-1, 1);
            case DOWN  -> { ctx().scale(-1, 1); ctx().rotate(-90); }
        }
        drawSpriteCenteredOverPosition(spriteAnimation.spriteSheet(), spriteAnimation.currentSprite(), 0, 0);
        ctx().restore();
    }

    @Override
    public void drawWorld(GameSpriteSheet spriteSheet, GameContext context, GameWorld world) {
        TengenMsPacManGame tengenGame = (TengenMsPacManGame) context.game();
        TileMap terrain = world.map().terrain();
        if (flashMode) {
            // Flash mode uses vector rendering
            Color wallFillColor = Color.web(world.map().colorScheme().fill());
            terrainRenderer.setWallStrokeColor(Color.WHITE);
            terrainRenderer.setWallFillColor(blinkingOn ? Color.BLACK : wallFillColor);
            terrainRenderer.setDoorColor(Color.BLACK);
            terrainRenderer.drawMap(ctx(), terrain);
        } else {
            if (mapSprite == null) {
                Logger.error("No map sprite selected");
                return;
            }
            if (!context.game().isDemoLevel()) {
                drawTop(spriteSheet, terrain, tengenGame);
            }
            // Maze #32 has this psychedelic animation effect
            int mapNumber = Integer.parseInt(world.map().terrain().getProperty("map_number"));
            if (mapNumber == 32) {
                drawAnimatedMaze(context.gameClock().getUpdateCount(), MAP_32_FRAMES);
            } else {
                RectArea mapArea = mapSprite.area();
                ctx().drawImage(mapSprite.source(),
                    //TODO check if these offsets are really needed to avoid rendering noise
                    mapArea.x() + 0.5, mapArea.y() + 0.5,
                    mapArea.width() - 1, mapArea.height() - 1,
                    0, scaled(3 * TS),
                    scaled(mapArea.width()), scaled(mapArea.height())
                );
            }

            // Tengen maps contain actor sprites, overpaint them
            hideActorSprite(terrain.getTileProperty("pos_pac", v2i(14, 26)), 0, 0);
            hideActorSprite(terrain.getTileProperty("pos_ghost_1_red", v2i(13, 14)), 0, 0);
            // The ghosts in the house are sitting some pixels below their home position
            // TODO: check if they really start from the bottom of the house, if yes, change map properties
            hideActorSprite(terrain.getTileProperty("pos_ghost_2_pink",   v2i(13, 17)), 0, 4);
            hideActorSprite(terrain.getTileProperty("pos_ghost_3_cyan",   v2i(11, 17)), 0, 4);
            hideActorSprite(terrain.getTileProperty("pos_ghost_4_orange", v2i(15, 17)), 0, 4);

            // Food
            ctx().save();
            ctx().scale(scaling(), scaling());
            overPaintEatenPellets(world);
            overPaintEnergizers(world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
            ctx().restore();

            tengenGame.bonus().ifPresent(bonus -> drawMovingBonus(spriteSheet, (MovingBonus) bonus));
        }
    }

    private void drawAnimatedMaze(long tick, RectArea[] sprites) {
        long frameTicks = 8; // TODO correct?
        int frameIndex = (int) ( (tick % (sprites.length * frameTicks)) / frameTicks );
        RectArea currentSprite = sprites[frameIndex];
        ctx().save();
        ctx().setImageSmoothing(false);
        ctx().drawImage(mapSprite.source(),
            currentSprite.x(), currentSprite.y(),
            currentSprite.width(), currentSprite.height(),
            0, scaled(3 * TS),
            scaled(currentSprite.width()), scaled(currentSprite.height())
        );
        ctx().restore();
    }

    private void drawTop(GameSpriteSheet spriteSheet, TileMap terrain, TengenMsPacManGame tengenGame) {
        MapCategory category = tengenGame.mapCategory();
        RectArea categorySprite = switch (category) {
            case BIG     -> TengenMsPacManGameSpriteSheet.BIG_SPRITE;
            case MINI    -> TengenMsPacManGameSpriteSheet.MINI_SPRITE;
            case STRANGE -> TengenMsPacManGameSpriteSheet.STRANGE_SPRITE;
            case ARCADE  -> TengenMsPacManGameSpriteSheet.NO_SPRITE;
        };
        RectArea difficultySprite = switch (tengenGame.difficulty()) {
            case EASY   -> TengenMsPacManGameSpriteSheet.EASY_SPRITE;
            case HARD   -> TengenMsPacManGameSpriteSheet.HARD_SPRITE;
            case CRAZY  -> TengenMsPacManGameSpriteSheet.CRAZY_SPRITE;
            case NORMAL -> TengenMsPacManGameSpriteSheet.NO_SPRITE;
        };
        double centerX = terrain.numCols() * HTS;
        double y = t(2) + HTS;
        if (tengenGame.pacBooster() != PacBooster.OFF) {
            //TODO: always displayed when TOGGLE_USING_KEY is selected or only if booster is active?
            drawSpriteCenteredOverPosition(spriteSheet, TengenMsPacManGameSpriteSheet.BOOSTER_SPRITE, centerX - t(6), y);
        }
        drawSpriteCenteredOverPosition(spriteSheet, difficultySprite, centerX, y);
        drawSpriteCenteredOverPosition(spriteSheet, categorySprite, centerX + t(4.5), y);
        drawSpriteCenteredOverPosition(spriteSheet, TengenMsPacManGameSpriteSheet.FRAME_SPRITE, centerX, y);
    }

    @Override
    public void drawScores(GameContext context) {
        Color color = Color.WHITE;
        Font font = scaledArcadeFont(TS);
        if (context.gameClock().getTickCount() % 60 < 30) { drawText("1UP", color, font, t(2), t(1)); }
        drawText("HIGH SCORE", color, font, t(9), t(1));
        drawText("%6d".formatted(context.game().score().points()),     color, font, 0,     t(2));
        drawText("%6d".formatted(context.game().highScore().points()), color, font, t(11), t(2));
    }

    @Override
    public void drawLivesCounter(GameSpriteSheet spriteSheet, int numLives, int maxLives, Vector2i worldSize) {
        ctx().save();
        ctx().translate(0, -5); // lift a bit
        GameWorldRenderer.super.drawLivesCounter(spriteSheet, numLives, maxLives, worldSize);
        ctx().restore();
    }

    @Override
    public void drawLevelCounter(GameSpriteSheet spriteSheet, int levelNumber, List<Byte> symbols, Vector2i worldSize) {
        ctx().save();
        ctx().translate(0, -5);
        GameWorldRenderer.super.drawLevelCounter(spriteSheet, levelNumber, symbols, worldSize);
        ctx().restore();
    }

    public void drawLevelNumberBoxes(GameSpriteSheet spriteSheet, int levelNumber, Vector2i worldSize) {
        ctx().save();
        ctx().translate(0, -5);
        if (levelNumber > 0) {
            double y = TS * (worldSize.y() - 2);
            drawLevelNumberBox(spriteSheet, levelNumber, 0, y); // left border
            drawLevelNumberBox(spriteSheet, levelNumber, TS * (worldSize.x() - 2), y); // right border
        }
        ctx().restore();
    }

    private void drawLevelNumberBox(GameSpriteSheet spriteSheet, int levelNumber, double x, double y) {
        TengenMsPacManGameSpriteSheet tss = (TengenMsPacManGameSpriteSheet) spriteSheet;
        drawSpriteScaled(spriteSheet, TengenMsPacManGameSpriteSheet.LEVEL_BOX_SPRITE, x, y);
        // erase violet area (what is it good for?)
        ctx().setFill(Color.BLACK);
        ctx().save();
        ctx().scale(scaling(), scaling());
        ctx().fillRect(x + 10, y + 2, 5, 5);
        ctx().restore();
        if (levelNumber < 10) {
            drawSpriteScaled(spriteSheet, tss.digit(levelNumber), x + 10, y + 2);
        } else if (levelNumber < 100) { // d1 d0
            int d0 = levelNumber % 10;
            int d1 = levelNumber / 10;
            drawSpriteScaled(spriteSheet, tss.digit(d0), x + 10, y + 2);
            drawSpriteScaled(spriteSheet, tss.digit(d1), x + 1,  y + 2);
        }
    }

    @Override
    public void selectMapSprite(WorldMap worldMap, int mapNumber, GameSpriteSheet spriteSheet) {
        TileMap terrainMap = worldMap.terrain();
        int width = terrainMap.numCols() * TS;
        int height = (terrainMap.numRows() - 5) * TS; // 3 empty rows before and 2 after maze source
        MapCategory mapCategory = MapCategory.valueOf(terrainMap.getProperty("map_category"));
        switch (mapCategory) {
            case ARCADE -> mapSprite = new ImageArea(arcadeMazesImage, arcadeMapArea(mapNumber, width, height));
            default -> {
                mapNumber = Integer.parseInt(terrainMap.getProperty("map_number"));
                mapSprite = new ImageArea(nonArcadeMazesImage, nonArcadeMapArea(mapNumber, width, height));
            }
        }
        Logger.info("Tengen map # {}: area: {}", mapNumber, mapSprite.area());
    }

    public void drawClapperBoard(GameSpriteSheet spriteSheet, Font font, Color textColor, ClapperboardAnimation animation, double x, double y) {
        var sprite = animation.currentSprite(spriteSheet.clapperboardSprites());
        if (sprite != RectArea.PIXEL) {
            drawSpriteCenteredOverBox(spriteSheet, sprite, x, y);
            var numberX = x + 8;
            var numberY = y + 18; // baseline
            ctx().setFill(backgroundColorPy.get());
            ctx().save();
            ctx().scale(scaling(), scaling());
            ctx().fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx().restore();
            ctx().setFont(font);
            ctx().setFill(textColor);
            ctx().fillText(animation.number(), scaled(numberX), scaled(numberY));
            var textX = scaled(x + sprite.width());
            ctx().fillText(animation.text(), textX, numberY);
        }
    }

    public void drawStork(GameSpriteSheet spriteSheet, SpriteAnimation storkAnimation, Entity stork, boolean bagReleased) {
        if (!stork.isVisible()) {
            return;
        }
        Vector2f pos = stork.position();
        // sprites are not vertically aligned in sprite sheet! wtf?
        double eyeY = pos.y() + (storkAnimation.frameIndex() == 1 ? 5 : 1);
        ctx().save();
        ctx().setImageSmoothing(false);
        drawSpriteScaled(spriteSheet, storkAnimation.currentSprite(), pos.x(), eyeY);
        // over-paint bag when released from beak
        if (bagReleased) {
            ctx().scale(scaling(), scaling());
            ctx().setFill(backgroundColorProperty().get());
            ctx().fillRect(pos.x(), eyeY + 4, scaled(7), scaled(6));
        }
        ctx().restore();
    }

    private void hideActorSprite(Vector2i tile, double offX, double offY) {
        // Parameter tile denotes the left of the two tiles where actor is located between. Compute center position.
        double cx = tile.x() * TS + TS + offX;
        double cy = tile.y() * TS + HTS + offY;
        double spriteSize = 2 * TS;
        ctx().setFill(backgroundColorProperty().get());
        ctx().fillRect(scaled(cx - TS), scaled(cy - TS), scaled(spriteSize), scaled(spriteSize));
    }
}