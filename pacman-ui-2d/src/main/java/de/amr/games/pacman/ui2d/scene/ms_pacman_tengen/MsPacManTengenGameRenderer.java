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
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.model.ms_pacman_tengen.*;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
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
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class MsPacManTengenGameRenderer implements GameRenderer {

    // Strange map #15 (level 32) has 3 different images to create an animation effect
    // Image file "non_arcade_mazes.png"
    static final RectArea[] STRANGE_MAP_15_SPRITES = {
        rect(1568,  840, 224, 248),
        rect(1568, 1088, 224, 248),
        rect(1568, 1336, 224, 248),
    };

    // Creates pattern (00000000 11111111 22222222 11111111)...
    private static RectArea strangeMap15Sprite(long tick) {
        int numFrames = 4, frameDuration = 8;
        int index = (int) ((tick % (numFrames * frameDuration)) / frameDuration);
        // (0, 1, 2, 3) -> (0, 1, 2, 1)
        return STRANGE_MAP_15_SPRITES[index == 3 ? 1 : index];
    }

    // Strange map row counts as they appear in the sprite sheet
    private static final byte[] STRANGE_MAPS_ROW_COUNTS = {
        31, 31, 31, 31, 31, 31, 30, 31,
        31, 37, 31, 31, 31, 37, 31, 25,
        37, 31, 37, 37, 37, 37, 37, 31,
        37, 37, 31, 25, 31, 25, 31, 31, 37,
        25, 25, 25, 25,
    };

    private final AssetStorage assets;
    private final MsPacManTengenGameSpriteSheet spriteSheet;
    private final Image arcadeMazeImages;
    private final Image nonArcadeMazeImages;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();
    private final Canvas canvas;

    private Color bgColor = Color.BLACK;
    private ImageArea mapSprite;
    private boolean blinking;
    private boolean levelNumberBoxesVisible;
    private Vector2f messageAnchorPosition;

    public MsPacManTengenGameRenderer(AssetStorage assets, MsPacManTengenGameSpriteSheet spriteSheet, Canvas canvas) {
        this.assets = checkNotNull(assets);
        this.spriteSheet = checkNotNull(spriteSheet);
        this.canvas = checkNotNull(canvas);
        arcadeMazeImages = assets.image("tengen.mazes.arcade");
        nonArcadeMazeImages = assets.image("tengen.mazes.non_arcade");
        messageAnchorPosition = new Vector2f(14f * TS, 20 * TS);
        terrainRenderer.scalingPy.bind(scalingPy);
        terrainRenderer.setMapBackgroundColor(bgColor);
        foodRenderer.scalingPy.bind(scalingPy);
    }

    @Override
    public void update(Map<String, Object> mapConfig) {
        MapCategory category = (MapCategory) mapConfig.get("mapCategory");
        NES_ColorScheme nesColorScheme = (NES_ColorScheme) mapConfig.get("nesColorScheme");
        mapSprite = switch (category) {
            case ARCADE  -> arcadeMapSprite(mapConfig);
            case MINI    -> miniMapSprite(mapConfig);
            case BIG     -> bigMapSprite(mapConfig);
            case STRANGE -> strangeMapSprite(mapConfig);
        };

        Map<String, String> colorMap = MsPacManTengenGameMapConfig.COLOR_MAPS.get(nesColorScheme);
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
    public MsPacManTengenGameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public Canvas canvas() {
        return canvas;
    }

    @Override
    public void setFlashMode(boolean flashMode) {}

    @Override
    public void setBlinking(boolean blinking) {
        this.blinking = blinking;
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
        ctx().setImageSmoothing(false);
        if (guy instanceof Pac pac) {
            drawMsOrMrPacMan(pac);
        } else {
            GameRenderer.super.drawAnimatedEntity(guy);
        }
    }

    @Override
    public Vector2f getMessageAnchorPosition() {
        return messageAnchorPosition;
    }

    public void setMessageAnchorPosition(Vector2f position) {
        messageAnchorPosition = position;
    }

    public void setLevelNumberBoxesVisible(boolean visible) {
        levelNumberBoxesVisible = visible;
    }

    private ImageArea arcadeMapSprite(Map<String, Object> mapConfig) {
        int mapNumber = (int) mapConfig.get("mapNumber");
        NES_ColorScheme colorScheme = (NES_ColorScheme) mapConfig.get("nesColorScheme");
        int index = switch (mapNumber) {
            case 1 -> 0;
            case 2 -> 1;
            case 3 -> switch (colorScheme) {
                case _16_20_15_ORANGE_WHITE_RED   -> 2;
                case _35_28_20_PINK_YELLOW_WHITE  -> 4;
                case _17_20_20_BROWN_WHITE_WHITE  -> 6;
                case _0F_20_28_BLACK_WHITE_YELLOW -> 8;
                default -> throw new IllegalArgumentException("No image found for map #3 and color scheme: " + colorScheme);
            };
            case 4 -> switch (colorScheme) {
                case _01_38_20_BLUE_YELLOW_WHITE   -> 3;
                case _36_15_20_PINK_RED_WHITE      -> 5;
                case _13_20_28_VIOLET_WHITE_YELLOW -> 7;
                default -> throw new IllegalArgumentException("No image found for map #4 and color scheme: " + colorScheme);
            };
            default -> throw new IllegalArgumentException("Illegal Arcade map number: " + mapNumber);
        };
        int col = index % 3, row = index / 3;
        int width = 28*8, height = 31*8;
        return new ImageArea(arcadeMazeImages, new RectArea(col * width, row * height, width, height));
    }

    private ImageArea miniMapSprite(Map<String, Object> mapConfig) {
        int mapNumber = (int) mapConfig.get("mapNumber");
        int spriteNumber = switch (mapNumber) {
            case 1 -> 34;
            case 2 -> 35;
            case 3 -> 36;
            case 4 -> 30;
            case 5 -> 28;
            case 6 -> 37;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + mapNumber);
        };
        return nonArcadeMapSprite(spriteNumber);
    }

    private ImageArea bigMapSprite(Map<String, Object> mapConfig) {
        int mapNumber = (int) mapConfig.get("mapNumber");
        int spriteNumber = switch (mapNumber) {
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
            default -> throw new IllegalArgumentException("Illegal BIG map number: " + mapNumber);
        };
        return nonArcadeMapSprite(spriteNumber);
    }

    private ImageArea strangeMapSprite(Map<String, Object> mapConfig) {
        int levelNumber = (int) mapConfig.get("levelNumber");
        return nonArcadeMapSprite(levelNumber);
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
        int width = 28 * TS, height = STRANGE_MAPS_ROW_COUNTS[spriteNumber - 1] * TS;
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
        ctx().setImageSmoothing(false);
        pac.animations().ifPresent(animations -> {
            if (animations instanceof SpriteAnimationCollection spriteAnimations) {
                SpriteAnimation spriteAnimation = spriteAnimations.currentAnimation();
                if (spriteAnimation != null) {
                    switch (animations.currentAnimationID()) {
                        case GameModel.ANIM_PAC_MUNCHING,
                             MsPacManTengenGame.ANIM_MS_PACMAN_BOOSTER,
                             MsPacManArcadeGame.ANIM_MR_PACMAN_MUNCHING,
                             MsPacManTengenGame.ANIM_PACMAN_BOOSTER -> drawGuyHeading(pac, pac.moveDir(), spriteAnimation);
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
                            drawGuyHeading(pac, dir, spriteAnimation);
                        }
                        default -> GameRenderer.super.drawAnimatedEntity(pac);
                    }
                } else {
                    Logger.error("No current animation for character {}", pac);
                }
            }
        });
    }

    private void drawGuyHeading(Creature guy, Direction dir, SpriteAnimation spriteAnimation) {
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
        ctx().setImageSmoothing(false);
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
    public void drawWorld(GameContext context, GameWorld world, double mapX, double mapY) {
        var game = (MsPacManTengenGame) context.game();
        GameLevel level = game.level().orElseThrow();
        MapCategory mapCategory = (MapCategory) level.mapConfig().get("mapCategory");

        if (!isUsingDefaultGameOptions(game)) {
            drawGameOptionsInfo(world.map().terrain(), game);
        }

        // All maps with a different color scheme than that in the sprite sheet have to be rendered using the
        // generic vector renderer for now.
        // TODO: vector rendering still looks really bad for some maps.
        boolean mapImageExists = game.isDemoLevel() || isMapImageAvailable(level.number, mapCategory);
        if (mapImageExists) {
            drawLevelMessage(context); // message appears under map image!
            int mapNumber = (int) level.mapConfig().get("mapNumber");
            RectArea area = mapCategory == MapCategory.STRANGE && mapNumber == 15
                ? strangeMap15Sprite(context.tick()) // Strange map #15: psychedelic animation
                : mapSprite.area();
            ctx().setImageSmoothing(false);
            ctx().drawImage(mapSprite.source(),
                area.x(), area.y(),
                area.width(), area.height(),
                scaled(mapX), scaled(mapY),
                scaled(area.width()), scaled(area.height())
            );
            overPaintActors(world);
            //TODO: fixme over-painting pellets also over-paints moving message!
            ctx().save();
            ctx().scale(scaling(), scaling());
            overPaintEatenPellets(world);
            overPaintEnergizers(world, tile -> !blinking || world.hasEatenFoodAt(tile));
            ctx().restore();
        }
        else {
            world.map().food().tiles()
                .filter(world::hasFoodAt)
                .filter(not(world::isEnergizerPosition))
                .forEach(tile -> foodRenderer.drawPellet(ctx(), tile));
            if (blinking) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(ctx(), tile));
            }
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
            NES_ColorScheme nesColorScheme = (NES_ColorScheme) level.mapConfig().get("nesColorScheme");
            Color color = Color.valueOf(nesColorScheme.strokeColor());
            drawText("GAME  OVER", x, y, color);
        } else if (context.gameState() == GameState.GAME_OVER) {
            Color color = assets.color(assetPrefix + ".color.game_over_message");
            drawText("GAME  OVER", x, y, color);
        } else if (context.gameState() == GameState.STARTING_GAME) {
            Color color = assets.color(assetPrefix + ".color.ready_message");
            drawText("READY!", x, y, color);
        } else if (context.gameState() == GameState.TESTING_LEVEL_BONI) {
            drawText("TEST L%02d".formatted(level.number), x, y, MsPacManTengenGameSceneConfig.nesPaletteColor(0x28));
        }
    }

    private boolean isUsingDefaultGameOptions(MsPacManTengenGame game) {
        return game.boosterMode() == PacBooster.OFF &&
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

    private void drawGameOptionsInfo(TileMap terrain, MsPacManTengenGame tengenGame) {
        MapCategory category = tengenGame.mapCategory();
        RectArea categorySprite = switch (category) {
            case BIG     -> MsPacManTengenGameSpriteSheet.BIG_SPRITE;
            case MINI    -> MsPacManTengenGameSpriteSheet.MINI_SPRITE;
            case STRANGE -> MsPacManTengenGameSpriteSheet.STRANGE_SPRITE;
            case ARCADE  -> MsPacManTengenGameSpriteSheet.NO_SPRITE;
        };
        RectArea difficultySprite = switch (tengenGame.difficulty()) {
            case EASY   -> MsPacManTengenGameSpriteSheet.EASY_SPRITE;
            case HARD   -> MsPacManTengenGameSpriteSheet.HARD_SPRITE;
            case CRAZY  -> MsPacManTengenGameSpriteSheet.CRAZY_SPRITE;
            case NORMAL -> MsPacManTengenGameSpriteSheet.NO_SPRITE;
        };
        double centerX = terrain.numCols() * HTS;
        double y = t(2) + HTS;
        if (tengenGame.boosterMode() != PacBooster.OFF) {
            //TODO: always displayed when TOGGLE_USING_KEY is selected or only if booster is active?
            drawSpriteCenteredOverPosition(MsPacManTengenGameSpriteSheet.BOOSTER_SPRITE, centerX - t(6), y);
        }
        drawSpriteCenteredOverPosition(difficultySprite, centerX, y);
        drawSpriteCenteredOverPosition(categorySprite, centerX + t(4.5), y);
        drawSpriteCenteredOverPosition(MsPacManTengenGameSpriteSheet.INFO_FRAME_SPRITE, centerX, y);
    }

    @Override
    public void drawScores(GameContext context) {
        Color color = MsPacManTengenGameSceneConfig.nesPaletteColor(0x20);
        Font font = scaledArcadeFont(TS);
        if (context.gameClock().getTickCount() % 60 < 30) { drawText("1UP", color, font, t(2), t(1)); }
        drawText("HIGH SCORE", color, font, t(9), t(1));
        drawText("%6d".formatted(context.game().scoreManager().score().points()),     color, font, 0,     t(2));
        drawText("%6d".formatted(context.game().scoreManager().highScore().points()), color, font, t(11), t(2));
    }

    @Override
    public void drawLivesCounter(int numLives, int maxLives, Vector2f size) {
        ctx().setImageSmoothing(false);
        GameRenderer.super.drawLivesCounter(numLives, maxLives, size.minus(0, TS)); //TODO this is ugly
    }

    @Override
    public void drawLevelCounter(GameContext context,  Vector2f size) {
        ctx().setImageSmoothing(false);
        MsPacManTengenGame game = (MsPacManTengenGame) context.game();
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
        return MsPacManTengenGameSceneConfig.nesPaletteColor(0x01 + 0x10 * i);
    }

    private void drawLevelNumberBox(int levelNumber, double x, double y) {
        ctx().setImageSmoothing(false);
        drawSpriteScaled(MsPacManTengenGameSpriteSheet.LEVEL_BOX_SPRITE, x, y);
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
        ctx().setImageSmoothing(false);
        ctx().translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> drawEntitySprite(bonus.entity(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
            case Bonus.STATE_EATEN  -> drawEntitySprite(bonus.entity(), spriteSheet.bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        ctx().restore();
    }

    public void drawClapperBoard(
        ClapperboardAnimation animation,
        String text, int number,
        Font font, Color textColor,
        double x, double y)
    {
        animation.sprite().ifPresent(sprite -> {
            ctx().setImageSmoothing(false);
            drawSpriteCenteredOverTile(sprite, x, y);
            var numberX = x + 8;
            var numberY = y + 18; // baseline
            ctx().setFill(bgColor);
            ctx().save();
            ctx().scale(scaling(), scaling());
            ctx().fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx().restore();
            ctx().setFont(font);
            ctx().setFill(textColor);
            ctx().fillText(String.valueOf(number), scaled(numberX), scaled(numberY));
            if (animation.isTextVisible()) {
                double textX = x + sprite.width();
                double textY = y + 2;
                ctx().fillText(text, scaled(textX), scaled(textY));
            }
        });
    }

    public void drawStork(SpriteAnimation storkAnimation, Entity stork, boolean hideBag) {
        if (!stork.isVisible()) {
            return;
        }
        Vector2f pos = stork.position();
        ctx().setImageSmoothing(false);
        drawSpriteScaled(storkAnimation.currentSprite(), pos.x(), pos.y());
        if (hideBag) { // over-paint bag under beak
            ctx().setFill(bgColor);
            ctx().fillRect(scaled(pos.x() - 1), scaled(pos.y() + 7), scaled(9), scaled(9));
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