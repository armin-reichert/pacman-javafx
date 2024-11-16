/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.maps.rendering.FoodMapRenderer;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import de.amr.games.pacman.model.*;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.model.ms_pacman_tengen.*;
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

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.lib.RectArea.rect;
import static de.amr.games.pacman.model.ms_pacman_tengen.NES_ColorScheme.*;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameRenderer implements GameRenderer {

    // Strange map #15 (level 32) has 3 different images to create an animation effect
    private static final RectArea[] STRANGE_MAP_15_SPRITES = {
        rect(1568,  840, 224, 248),
        rect(1568, 1088, 224, 248),
        rect(1568, 1336, 224, 248),
    };

    // Create  (00000000 11111111 22222222 11111111)*
    private static RectArea currentSpriteForStrangeMap15(long tick) {
        int numFrames = 4, frameDuration = 8;
        int index = (int)((tick % (numFrames * frameDuration)) / frameDuration);
        // map frames (0, 1, 2, 3) to sprites (0, 1, 2, 1)
        int spriteIndex = index == 3 ? 1 : index;
        return STRANGE_MAP_15_SPRITES[spriteIndex];
    }

    // Strange map row counts as they appear in the sprite sheet
    private static final byte[] MAP_ROW_COUNTS = {
        31, 31, 31, 31, 31, 31, 30, 31,
        31, 37, 31, 31, 31, 37, 31, 25,
        37, 31, 37, 37, 37, 37, 37, 31,
        37, 37, 31, 25, 31, 25, 31, 31, 37,
        25, 25, 25, 25,
    };

    private final AssetStorage assets;
    private final TengenMsPacManGameSpriteSheet spriteSheet;
    private final Image arcadeMazeImages;
    private final Image nonArcadeMazeImages;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();
    private final Canvas canvas;

    private Color bgColor = Color.BLACK;
    private ImageArea mapSprite;
    private boolean blinkingOn;
    private boolean levelNumberBoxesVisible;
    private Vector2f messageAnchorPosition;

    public TengenMsPacManGameRenderer(AssetStorage assets, TengenMsPacManGameSpriteSheet spriteSheet, Canvas canvas) {
        this.assets = checkNotNull(assets);
        this.spriteSheet = checkNotNull(spriteSheet);
        this.canvas = checkNotNull(canvas);
        canvas.getGraphicsContext2D().setImageSmoothing(false);
        arcadeMazeImages = assets.image("tengen.mazes.arcade");
        nonArcadeMazeImages = assets.image("tengen.mazes.non_arcade");
        // set default value
        messageAnchorPosition = new Vector2f(14f * TS, 20 * TS);
        terrainRenderer.scalingPy.bind(scalingPy);
        terrainRenderer.setMapBackgroundColor(bgColor);
        foodRenderer.scalingPy.bind(scalingPy);
    }

    @Override
    public void update(GameLevel level) {
        // this method should be fast because it is called in every frame
        MapConfig mapConfig = level.mapConfig();
        MapCategory category = (MapCategory) mapConfig.mapCategory();
        mapSprite = switch (category) {
            case ARCADE  -> arcadeMapSprite(mapConfig);
            case MINI    -> miniMapSprite(mapConfig);
            case BIG     -> bigMapSprite(mapConfig);
            case STRANGE -> strangeMapSprite(mapConfig);
        };
        Logger.debug("Level {}: Using map sprite with area #{}", level.number, mapSprite.area());

        var nesColorScheme = (NES_ColorScheme) mapConfig.colorScheme();
        Map<String, String> colorMap = MapConfigurationManager.COLOR_MAPS_OF_NES_COLOR_SCHEMES.get(nesColorScheme);

        terrainRenderer.setMapBackgroundColor(bgColor);
        terrainRenderer.setWallStrokeColor(Color.valueOf(colorMap.get("stroke")));
        terrainRenderer.setWallFillColor(Color.valueOf(colorMap.get("fill")));
        terrainRenderer.setDoorColor(Color.valueOf(colorMap.get("door")));

        foodRenderer.setPelletColor(Color.valueOf(colorMap.get("pellet")));
        foodRenderer.setEnergizerColor(Color.valueOf(colorMap.get("pellet")));
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
    public Canvas canvas() {
        return canvas;
    }

    @Override
    public void setFlashMode(boolean flashMode) {}

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

    public Vector2f getMessageAnchorPosition() {
        return messageAnchorPosition;
    }

    public void setMessageAnchorPosition(Vector2f position) {
        messageAnchorPosition = position;
    }

    public void setLevelNumberBoxesVisible(boolean visible) {
        levelNumberBoxesVisible = visible;
    }

    private ImageArea arcadeMapSprite(MapConfig config) {
        var colorScheme = config.colorScheme();
        Vector2i coordinate = switch (config.mapNumber()) {
            case 1 -> v2i(0, 0);
            case 2 -> v2i(1, 0);
            case 3 -> {
                if (colorScheme == MCS_16_20_15_ORANGE_WHITE_RED) {
                    yield v2i(2, 0);
                }
                if (colorScheme == MCS_35_28_20_PINK_YELLOW_WHITE) {
                    yield v2i(1, 1);
                }
                if (colorScheme == MCS_17_20_20_BROWN_WHITE_WHITE) {
                    yield v2i(0, 2);
                }
                if (colorScheme == MCS_0F_20_28_BLACK_WHITE_YELLOW) {
                    yield v2i(2, 2);
                }
                throw new IllegalArgumentException("Unknown color scheme for map 3: " + colorScheme);
            }
            case 4 -> {
                if (colorScheme == MCS_01_38_20_BLUE_YELLOW_WHITE) {
                    yield v2i(0, 1);
                }
                if (colorScheme == MCS_36_15_20_PINK_RED_WHITE) {
                    yield v2i(2, 1);
                }
                if (colorScheme == MCS_13_20_28_VIOLET_WHITE_YELLOW) {
                    yield v2i(1, 2);
                }
                throw new IllegalArgumentException("Unknown color scheme for map #4: " + colorScheme);
            }
            default -> throw new IllegalArgumentException("Illegal Arcade map number: " + config.mapNumber());
        };
        int width = 28*8, height = 31*8;
        return new ImageArea(arcadeMazeImages, new RectArea(coordinate.x() * width, coordinate.y() * height, width, height));
    }

    private ImageArea miniMapSprite(MapConfig config) {
        int spriteNumber = switch (config.mapNumber()) {
            case 1 -> 34;
            case 2 -> 35;
            case 3 -> 36;
            case 4 -> 30;
            case 5 -> 28;
            case 6 -> 37;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + config.mapNumber());
        };
        return nonArcadeMapSprite(spriteNumber);
    }

    private ImageArea bigMapSprite(MapConfig config) {
        int spriteNumber = switch (config.mapNumber()) {
            case  1 -> 19;
            case  2 -> 20;
            case  3 -> 21;
            case  4 -> 22;
            case  5 -> 23;
            case  6 -> 17;
            case  7 -> 10;
            case  8 -> 14;
            case  9 -> 26;
            case 10 -> 25;
            case 11 -> 33;
            default -> throw new IllegalArgumentException("Illegal BIG map number: " + config.mapNumber());
        };
        return nonArcadeMapSprite(spriteNumber);
    }

    private ImageArea strangeMapSprite(MapConfig config) {
        // Dirty hack, don't tell Mommy! See MapConfigurationManager.
        int spriteNumber = Integer.parseInt(config.worldMap().terrain().getProperty("levelNumber"));
        return nonArcadeMapSprite(spriteNumber);
    }

    /**
     * @param spriteNumber number (1 based) of map sprite in sprite sheet (row-wise)
     * @return map sprite in non-Arcade maps sprite sheet
     */
    private ImageArea nonArcadeMapSprite(int spriteNumber) {
        int columnIndex, y;
        switch (spriteNumber) {
            case 1,2,3,4,5,6,7,8            -> { columnIndex = (spriteNumber - 1);  y = 0;    }
            case 9,10,11,12,13,14,15,16     -> { columnIndex = (spriteNumber - 9);  y = 248;  }
            case 17,18,19,20,21,22,23,24    -> { columnIndex = (spriteNumber - 17); y = 544;  }
            case 25,26,27,28,29,30,31,32,33 -> { columnIndex = (spriteNumber - 25); y = 840;  }
            case 34,35,36,37                -> { columnIndex = (spriteNumber - 34); y = 1136; }
            default -> throw new IllegalArgumentException("Illegal non-Arcade map number: " + spriteNumber);
        }
        int width = 28 * TS, height = MAP_ROW_COUNTS[spriteNumber - 1] * TS;
        return new ImageArea(nonArcadeMazeImages, new RectArea(columnIndex * width, y, width, height));
    }

    private boolean isMapImageAvailable(int levelNumber, MapCategory mapCategory) {
        return switch (mapCategory) {
            case ARCADE -> true; // all available in sprite sheet
            case MINI -> false; // TODO use map sprite if level uses color scheme in sprite sheet
            case BIG -> false; // TODO use map sprite  if level uses color scheme in sprite sheet
            case STRANGE -> !inRange(levelNumber, 28, 31); // all except those with random color scheme
        };
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
                             TengenMsPacManGame.ANIM_MS_PACMAN_BOOSTER,
                             MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING,
                             TengenMsPacManGame.ANIM_PACMAN_BOOSTER -> drawRotatedTowardsDir(pac, pac.moveDir(), spriteAnimation);
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

    public void drawEmptyMap(WorldMap worldMap, Map<String, Color> colorScheme) {
        Color wallFillColor = colorScheme.get("fill");
        Color wallStrokeColor = colorScheme.get("stroke");
        Color doorColor = colorScheme.get("door");
        terrainRenderer.setMapBackgroundColor(bgColor);
        terrainRenderer.setWallStrokeColor(wallStrokeColor);
        terrainRenderer.setWallFillColor(wallFillColor);
        terrainRenderer.setDoorColor(doorColor);
        terrainRenderer.drawMap(ctx(), worldMap.terrain());
    }

    @Override
    public void drawWorld(GameContext context, GameWorld world, double mazeX, double mazeY) {
        TengenMsPacManGame game = (TengenMsPacManGame) context.game();
        GameLevel level = game.level().orElseThrow();
        if (!isUsingDefaultGameOptions(game)) {
            drawGameOptionsInfo(context.level().world().map().terrain(), game);
        }

        // All maps with a different color scheme than that in the sprite sheet have to be rendered using the
        // generic vector renderer for now.
        // TODO: Improve renderer because vector rendering still looks really bad for some maps.
        boolean mapSpriteExists = context.game().isDemoLevel() || isMapImageAvailable(context.level().number, game.mapCategory());
        if (mapSpriteExists) {
            drawLevelMessage(context); // message appears behind map!
            RectArea sprite = level.mapConfig().mapCategory() == MapCategory.STRANGE && level.mapConfig().mapNumber() == 15
                ? currentSpriteForStrangeMap15(context.tick()) // Strange map #15: psychedelic animation
                : mapSprite.area();
            ctx().drawImage(mapSprite.source(),
                sprite.x(), sprite.y(),
                sprite.width(), sprite.height(),
                scaled(mazeX), scaled(mazeY),
                scaled(sprite.width()), scaled(sprite.height())
            );
            overPaintActors(level.world());
            //TODO over-painting pellets also over-paints moving message!
            ctx().save();
            ctx().scale(scaling(), scaling());
            overPaintEatenPellets(world);
            overPaintEnergizers(world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
            ctx().restore();
        }
        else {
            world.map().food().tiles()
                .filter(world::hasFoodAt)
                .filter(not(world::isEnergizerPosition))
                .forEach(tile -> foodRenderer.drawPellet(ctx(), tile));
            if (blinkingOn) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(ctx(), tile));
            }
            // in Tengen Ms. Pac-Man the level message appears under the maze image, wtf?
            drawLevelMessage(context);
            terrainRenderer.drawMap(ctx(), world.map().terrain());
        }
        context.level().bonus().ifPresent(bonus -> drawMovingBonus(spriteSheet, (MovingBonus) bonus));
    }

    //TODO too much game logic in here
    private void drawLevelMessage(GameContext context) {
        GameLevel level = context.level();
        String assetPrefix = assetPrefix(GameVariant.MS_PACMAN_TENGEN);
        float x = getMessageAnchorPosition().x(), y = getMessageAnchorPosition().y();
        if (context.game().isDemoLevel()) {
            NES_ColorScheme nesColorScheme = (NES_ColorScheme) level.mapConfig().colorScheme();
            Color color = Color.valueOf(nesColorScheme.strokeColor());
            drawText("GAME  OVER", x, y, color);
        } else if (context.gameState() == GameState.GAME_OVER) {
            Color color = assets.color(assetPrefix + ".color.game_over_message");
            drawText("GAME  OVER", x, y, color);
        } else if (context.gameState() == GameState.STARTING_GAME) {
            Color color = assets.color(assetPrefix + ".color.ready_message");
            drawText("READY!", x, y, color);
        } else if (context.gameState() == GameState.TESTING_LEVEL_BONI) {
            drawText("TEST L%02d".formatted(level.number), x, y, TengenMsPacManGameSceneConfig.nesPaletteColor(0x28));
        }
    }

    private boolean isUsingDefaultGameOptions(TengenMsPacManGame game) {
        return game.boosterMode() == BoosterMode.OFF &&
            game.difficulty() == Difficulty.NORMAL &&
            game.mapCategory() == MapCategory.ARCADE;
    }

    private void overPaintActors(GameWorld world) {
        Vector2f topLeftPosition = world.houseTopLeftTile().plus(1, 2).scaled(TS * scaling());
        Vector2f size = new Vector2i(world.houseSize().x() - 2, 2).scaled(TS * scaling());
        ctx().setFill(bgColor);
        ctx().fillRect(topLeftPosition.x(), topLeftPosition.y(), size.x(), size.y());
        hideActorSprite(world.map().terrain().getTileProperty("pos_pac", v2i(14, 26)));
        hideActorSprite(world.map().terrain().getTileProperty("pos_ghost_1_red", v2i(13, 14)));
    }

    private void drawGameOptionsInfo(TileMap terrain, TengenMsPacManGame tengenGame) {
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
        if (tengenGame.boosterMode() != BoosterMode.OFF) {
            //TODO: always displayed when TOGGLE_USING_KEY is selected or only if booster is active?
            drawSpriteCenteredOverPosition(TengenMsPacManGameSpriteSheet.BOOSTER_SPRITE, centerX - t(6), y);
        }
        drawSpriteCenteredOverPosition(difficultySprite, centerX, y);
        drawSpriteCenteredOverPosition(categorySprite, centerX + t(4.5), y);
        drawSpriteCenteredOverPosition(TengenMsPacManGameSpriteSheet.INFO_FRAME_SPRITE, centerX, y);
    }

    @Override
    public void drawScores(GameContext context) {
        Color color = TengenMsPacManGameSceneConfig.nesPaletteColor(0x20);
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
    public void drawLevelCounter(GameContext context,  Vector2f size) {
        TengenMsPacManGame game = (TengenMsPacManGame) context.game();
        int levelNumber = context.level().number;
        // TODO: This is ugly, maybe change all Tengen maps instead?
        double y = size.y() - 3 * TS;
        if (levelNumberBoxesVisible) {
            drawLevelNumberBox(levelNumber, 0, y); // left box
            drawLevelNumberBox(levelNumber, size.x() - 2 * TS, y); // right box
        }
        double symbolX = size.x() - 4 * TS;
        for (byte symbol : game.levelCounter()) {
            drawSpriteScaled(spriteSheet().bonusSymbolSprite(symbol), symbolX, y);
            symbolX -= TS * 2;
        }
    }

    public void drawTengenPresents(long t, double x, double y) {
        drawText("TENGEN PRESENTS", shadeOfBlue(t), scaledArcadeFont(TS), x, y);
    }

    // Blue colors used in intro, dark to brighter blue shade.
    // Cycles through palette indices 0x01, 0x11, 0x21, 0x31, each frame takes 16 ticks.
    private Color shadeOfBlue(long tick) {
        int i = (int) (tick % 64) / 16;
        return TengenMsPacManGameSceneConfig.nesPaletteColor(0x01 + 0x10 * i);
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

    public void drawBar(Color outlineColor, Color barColor, double width, double y) {
        double scaling = scaling();
        ctx().save();
        ctx().scale(scaling, scaling);
        ctx().setFill(outlineColor);
        ctx().fillRect(0, y, width, TS);
        ctx().setFill(barColor);
        ctx().fillRect(0, y + 1, width, TS - 2);
        ctx().restore();
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

    private void hideActorSprite(Vector2i tile) {
        // Parameter tile denotes the left of the two tiles where actor is located between. Compute center position.
        double cx = tile.x() * TS;
        double cy = tile.y() * TS - HTS;
        ctx().setFill(bgColor);
        ctx().fillRect(scaled(cx), scaled(cy), scaled(16), scaled(16));
    }

    private void drawText(String text, double cx, double y, Color color) {
        double x = (cx - text.length() * 0.5 * TS);
        drawText(text, color, scaledArcadeFont(TS), x, y);
    }
}