/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.MapColorScheme;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.maps.rendering.FoodMapRenderer;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.model.ms_pacman_tengen.BoosterMode;
import de.amr.games.pacman.model.ms_pacman_tengen.MapCategory;
import de.amr.games.pacman.model.ms_pacman_tengen.NESColorPalette;
import de.amr.games.pacman.model.ms_pacman_tengen.TengenMsPacManGame;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
import de.amr.games.pacman.ui2d.scene.ms_pacman.ClapperboardAnimation;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.List;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.RectArea.rect;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameRenderer implements GameRenderer {

    static Color paletteColor(int index) {
        return Color.web(NESColorPalette.color(index));
    }

    public static final Color TENGEN_BABY_BLUE          = paletteColor(0x21);
    public static final Color TENGEN_PINK               = paletteColor(0x25);
    public static final Color TENGEN_YELLOW             = paletteColor(0x28);
    public static final Color TENGEN_MARQUEE_COLOR      = paletteColor(0x15);
    public static final Color TENGEN_PAC_COLOR          = paletteColor(0x28);
    public static final Color TENGEN_RED_GHOST_COLOR    = paletteColor(0x05);
    public static final Color TENGEN_PINK_GHOST_COLOR   = paletteColor(0x25);
    public static final Color TENGEN_CYAN_GHOST_COLOR   = paletteColor(0x11);
    public static final Color TENGEN_ORANGE_GHOST_COLOR = paletteColor(0x16);

    // Blue colors used in intro, dark to brighter blue shade.
    // Cycles through palette indices 0x01, 0x11, 0x21, 0x31, each frame takes 16 ticks.
    static Color shadeOfBlue(long tick) {
        int index = (int) (tick % 64) / 16;
        return paletteColor(0x01 + 16*index);
    }

    // Maze images are taken from files "arcade_mazes.png" and "non_arcade_mazes.png" via AssetStorage

    /**
     * @param mapNumber number of non-Arcade map (1-37)
     * @param width map width in pixels
     * @param height map height in pixels
     * @return map sprite in non-Arcade maps sprite sheet
     */
    private static RectArea nonArcadeMapSprite(int mapNumber, int width, int height) {
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

    // Map #32 has 3 different images to create an animation effect.
    private static final RectArea[] MAP_32_ANIMATION_FRAMES = {
        rect(1568, 840, 224, 248), rect(1568, 1088, 224, 248), rect(1568, 1336, 224, 248),
    };

    private final AssetStorage assets;
    private final TengenMsPacManGameSpriteSheet spriteSheet;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();
    private final Image arcadeMazesImage;
    private final Image nonArcadeMazesImage;

    private Color bgColor = Color.BLACK;
    private ImageArea mapSprite;
    private boolean flashMode;
    private boolean blinkingOn;
    private Canvas canvas;

    public TengenMsPacManGameRenderer(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        arcadeMazesImage = assets.image("tengen.mazes.arcade");
        nonArcadeMazesImage = assets.image("tengen.mazes.non_arcade");
        spriteSheet = assets.get("tengen.spritesheet");
        terrainRenderer.scalingPy.bind(scalingPy);
        terrainRenderer.setMapBackgroundColor(bgColor);
        foodRenderer.scalingPy.bind(scalingPy);
    }

    @Override
    public GameRenderer copy() {
        return new TengenMsPacManGameRenderer(assets);
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public TengenMsPacManGameSpriteSheet spriteSheet() {
        return spriteSheet;
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
    public Color backgroundColor() {
        return bgColor;
    }

    @Override
    public void setBackgroundColor(Color color) {
        bgColor = checkNotNull(color);
    }

    @Override
    public void drawAnimatedEntity(AnimatedEntity guy) {
        if (guy instanceof Pac pac) {
            drawMsOrMrPacMan(pac);
        } else {
            GameRenderer.super.drawAnimatedEntity(guy);
        }
    }

    private void drawMsOrMrPacMan(Pac pac) {
        if (!pac.isVisible()) {
            return;
        }
        pac.animations().ifPresent(animations -> {
            if (animations instanceof SpriteAnimationCollection spriteAnimations) {
                SpriteAnimation spriteAnimation = spriteAnimations.currentAnimation();
                if (spriteAnimation != null) {
                    switch (animations.currentAnimationID()) {
                        case GameModel.ANIM_PAC_MUNCHING,
                             TengenMsPacManGame.ANIM_PAC_MUNCHING_BOOSTER,
                             MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING,
                             TengenMsPacManGame.ANIM_PAC_HUSBAND_MUNCHING_BOOSTER -> drawRotatedTowardsDir(pac, pac.moveDir(), spriteAnimation);
                        case GameModel.ANIM_PAC_DYING -> {
                            Direction dir = Direction.UP;
                            if (spriteAnimation.frameIndex() < 11) {
                                dir = switch (spriteAnimation.frameIndex() % 4) {
                                    default -> Direction.DOWN; // start with DOWN
                                    case 1 -> Direction.LEFT;
                                    case 2 -> Direction.UP;
                                    case 3 -> Direction.RIGHT;
                                };
                            }
                            drawRotatedTowardsDir(pac, dir, spriteAnimation);
                        }
                        default -> GameRenderer.super.drawAnimatedEntity(pac);
                    }
                } else {
                    Logger.error("No current animation for character {}", pac);
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
        drawSpriteCenteredOverPosition(spriteAnimation.currentSprite(), 0, 0);
        ctx().restore();
    }

    @Override
    public void drawWorld(GameContext context, GameWorld world) {
        TengenMsPacManGame game = (TengenMsPacManGame) context.game();
        TileMap terrain = world.map().terrain();

        if (flashMode) {
            Color wallFillColor = Color.web(world.map().colorSchemeOrDefault().fill());
            terrainRenderer.setMapBackgroundColor(bgColor);
            terrainRenderer.setWallStrokeColor(Color.WHITE);
            terrainRenderer.setWallFillColor(blinkingOn ? Color.BLACK : wallFillColor);
            terrainRenderer.setDoorColor(Color.BLACK);
            terrainRenderer.drawMap(ctx(), terrain);
            return;
        }

        boolean useVectorRenderer = game.mapCategory() != MapCategory.ARCADE
            && game.mapCategory() != MapCategory.STRANGE;
        boolean isDemoLevel = context.game().isDemoLevel();

        if (useVectorRenderer) {
            if (!isDemoLevel) {
                drawInfoOnTopOfMap(terrain, game);
            }
            MapColorScheme colorScheme = world.map().colorScheme();
            terrainRenderer.setMapBackgroundColor(bgColor);
            terrainRenderer.setWallStrokeColor(Color.web(colorScheme.stroke()));
            terrainRenderer.setWallFillColor(Color.web(colorScheme.fill()));
            terrainRenderer.setDoorColor(Color.web(colorScheme.door()));
            terrainRenderer.drawMap(ctx(), terrain);
            foodRenderer.setPelletColor(Color.web(colorScheme.pellet()));
            foodRenderer.setEnergizerColor(Color.web(colorScheme.pellet()));
            world.map().food().tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerPosition))
                .forEach(tile -> foodRenderer.drawPellet(ctx(), tile));
            if (blinkingOn) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(ctx(), tile));
            }
        }
        else { // draw using sprite sheet
            if (mapSprite == null) {
                Logger.error("Cannot draw world: No map sprite selected");
                return;
            }
            if (!game.isDemoLevel()) {
                drawInfoOnTopOfMap(game.world().map().terrain(), game);
            }
            drawWorldUsingSpriteSheet(game, context.gameClock().getUpdateCount());
        }
    }

    private void drawWorldUsingSpriteSheet(TengenMsPacManGame game, long t) {
        // Maze #32 has this psychedelic animation effect
        int mapNumber = game.currentMapNumber();
        if (mapNumber == 32) {
            drawAnimatedMaze(t, MAP_32_ANIMATION_FRAMES);
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
        hideActorSprites(game.world().map().terrain());
        drawFoodUsingMapSprite(game, game.world(), spriteSheet);
    }

    private void hideActorSprites(TileMap terrain) {
        // Tengen maps contain actor sprites, overpaint them
        hideActorSprite(terrain.getTileProperty("pos_pac", v2i(14, 26)), 0, 0);
        hideActorSprite(terrain.getTileProperty("pos_ghost_1_red", v2i(13, 14)), 0, 0);
        // The ghosts in the house are sitting some pixels below their home position
        // TODO: check if they really start from the bottom of the house, if yes, change map properties
        hideActorSprite(terrain.getTileProperty("pos_ghost_2_pink",   v2i(13, 17)), 0, 4);
        hideActorSprite(terrain.getTileProperty("pos_ghost_3_cyan",   v2i(11, 17)), 0, 4);
        hideActorSprite(terrain.getTileProperty("pos_ghost_4_orange", v2i(15, 17)), 0, 4);
    }

    private void drawFoodUsingMapSprite(TengenMsPacManGame game, GameWorld world, GameSpriteSheet spriteSheet) {
        ctx().save();
        ctx().scale(scaling(), scaling());
        overPaintEatenPellets(world);
        overPaintEnergizers(world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
        ctx().restore();
        game.bonus().ifPresent(bonus -> drawMovingBonus(spriteSheet, (MovingBonus) bonus));
    }

    private void drawAnimatedMaze(long tick, RectArea[] sprites) {
        long frameTicks = 8; // TODO correct?
        int frameIndex = (int) ( (tick % (sprites.length * frameTicks)) / frameTicks );
        RectArea currentSprite = sprites[frameIndex];
        ctx().drawImage(mapSprite.source(),
            currentSprite.x(), currentSprite.y(),
            currentSprite.width(), currentSprite.height(),
            0, scaled(3 * TS),
            scaled(currentSprite.width()), scaled(currentSprite.height())
        );
    }

    private void drawInfoOnTopOfMap(TileMap terrain, TengenMsPacManGame tengenGame) {
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
        if (tengenGame.pacBoosterMode() != BoosterMode.OFF) {
            //TODO: always displayed when TOGGLE_USING_KEY is selected or only if booster is active?
            drawSpriteCenteredOverPosition(TengenMsPacManGameSpriteSheet.BOOSTER_SPRITE, centerX - t(6), y);
        }
        drawSpriteCenteredOverPosition(difficultySprite, centerX, y);
        drawSpriteCenteredOverPosition(categorySprite, centerX + t(4.5), y);
        drawSpriteCenteredOverPosition(TengenMsPacManGameSpriteSheet.INFO_FRAME_SPRITE, centerX, y);
    }

    @Override
    public void drawScores(GameContext context) {
        Color color = Color.WHITE;
        Font font = scaledArcadeFont(TS);
        if (context.gameClock().getTickCount() % 60 < 30) { drawText("1UP", color, font, t(2), t(1)); }
        drawText("HIGH SCORE", color, font, t(9), t(1));
        drawText("%6d".formatted(context.game().scoreManager().score().points()),     color, font, 0,     t(2));
        drawText("%6d".formatted(context.game().scoreManager().highScore().points()), color, font, t(11), t(2));
    }

    @Override
    public void drawLivesCounter(int numLives, int maxLives, Vector2f size) {
        GameRenderer.super.drawLivesCounter(numLives, maxLives, size.minus(0, TS)); //TODO this is ugly
    }

    @Override
    public void drawLevelCounter(int levelNumber, boolean demoLevel, List<Byte> symbols, Vector2f size) {
        // TODO: This is ugly, maybe change all Tengen maps instead?
        double y = size.y() - 3 * TS;
        if (!demoLevel && levelNumber > 0) {
            drawLevelNumberBox(levelNumber, 0, y); // left box
            drawLevelNumberBox(levelNumber, size.x() - 2 * TS, y); // right box
        }
        double symbolX = size.x() - 4 * TS;
        for (byte symbol : symbols) {
            drawSpriteScaled(spriteSheet().bonusSymbolSprite(symbol), symbolX, y);
            symbolX -= TS * 2;
        }
    }

    private void drawLevelNumberBox(int levelNumber, double x, double y) {
        drawSpriteScaled(TengenMsPacManGameSpriteSheet.LEVEL_BOX_SPRITE, x, y);
        double digitY = y + 2;
        int tens = levelNumber / 10, ones = levelNumber % 10;
        drawSpriteScaled(spriteSheet.digit(ones), x + 10, digitY);
        if (tens > 0) {
            drawSpriteScaled(spriteSheet.digit(tens), x + 2,  digitY);
        }
    }

    @Override
    public void update(GameModel game) {
        TengenMsPacManGame tengenGame = (TengenMsPacManGame) game;
        if (game.world() == null) {
            Logger.warn("Cannot set renderer for game, no world exists");
            return;
        }
        WorldMap worldMap = game.world().map();
        int width  = worldMap.terrain().numCols() * TS;
        int height = (worldMap.terrain().numRows() - 5) * TS; // 3 empty rows over, 2 under maze image
        int mapNumber = game.currentMapNumber();
        mapSprite = switch (tengenGame.mapCategory()) {
            // we have a sprite sheet with the maps in the required color scheme (to be fixed in the sprite sheet image though)
            case ARCADE -> arcadeMapSprite(game.levelNumber());
            case BIG, STRANGE, MINI -> new ImageArea(nonArcadeMazesImage, nonArcadeMapSprite(mapNumber, width, height));
        };
        Logger.info("Tengen map # {}: area: {}", mapNumber, mapSprite.area());
    }

    private ImageArea arcadeMapSprite(int levelNumber) {
        return switch (levelNumber) {
            case 1, 2           -> arcadeMapSprite(0, 0);
            case 3, 4, 5        -> arcadeMapSprite(0, 1);
            case 6, 7, 8, 9     -> arcadeMapSprite(0, 2);
            case 10, 11, 12, 13 -> arcadeMapSprite(1, 0);
            case 14, 15, 16, 17 -> arcadeMapSprite(1, 1);
            case 18, 19, 20, 21 -> arcadeMapSprite(1, 2);
            case 22, 23, 24, 25 -> arcadeMapSprite(2, 0);
            case 26, 27, 28, 29 -> arcadeMapSprite(2, 1);
            case 30, 31, 32     -> arcadeMapSprite(2, 2);
            default             -> arcadeMapSprite(2, 2); // should not happen
        };
    }

    private ImageArea arcadeMapSprite(int rowIndex, int colIndex) {
        int width = 28 * TS, height = 31 * TS;
        return new ImageArea(arcadeMazesImage, new RectArea(colIndex * width, rowIndex * height, width, height));
    }

    public void drawMovingBonus(GameSpriteSheet spriteSheet, MovingBonus bonus) {
        ctx().save();
        ctx().translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawSprite(bonus.entity(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawSprite(bonus.entity(), spriteSheet.bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        ctx().restore();
    }

    public void drawClapperBoard(Font font, Color textColor, ClapperboardAnimation animation, double x, double y) {
        var sprite = animation.currentSprite(TengenMsPacManGameSpriteSheet.CLAPPERBOARD_SPRITES);
        if (sprite != RectArea.PIXEL) {
            drawSpriteCenteredOverBox(sprite, x, y);
            var numberX = x + 8;
            var numberY = y + 18; // baseline
            ctx().setFill(bgColor);
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

    public void drawStork(SpriteAnimation storkAnimation, Entity stork, boolean bagReleased) {
        if (!stork.isVisible()) {
            return;
        }
        Vector2f pos = stork.position();
        // sprites are not vertically aligned in sprite sheet! wtf?
        double eyeY = pos.y() + (storkAnimation.frameIndex() == 1 ? 5 : 1);
        drawSpriteScaled(storkAnimation.currentSprite(), pos.x(), eyeY);
        // over-paint bag when released from beak
        if (bagReleased) {
            ctx().scale(scaling(), scaling());
            ctx().setFill(bgColor);
            ctx().fillRect(pos.x(), eyeY + 4, scaled(7), scaled(6));
        }
    }

    private void hideActorSprite(Vector2i tile, double offX, double offY) {
        // Parameter tile denotes the left of the two tiles where actor is located between. Compute center position.
        double cx = tile.x() * TS + TS + offX;
        double cy = tile.y() * TS + HTS + offY;
        double spriteSize = 2 * TS;
        ctx().setFill(bgColor);
        ctx().fillRect(scaled(cx - TS), scaled(cy - TS), scaled(spriteSize), scaled(spriteSize));
    }
}