/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.ms_pacman_tengen;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.NES;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.maps.rendering.FoodMapRenderer;
import de.amr.games.pacman.maps.rendering.TerrainMapRenderer;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.MapConfig;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.ms_pacman.MsPacManArcadeGame;
import de.amr.games.pacman.model.ms_pacman_tengen.BoosterMode;
import de.amr.games.pacman.model.ms_pacman_tengen.Difficulty;
import de.amr.games.pacman.model.ms_pacman_tengen.MapCategory;
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
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.util.Map;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.ms_pacman_tengen.NamedMapColorScheme.*;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class TengenMsPacManGameRenderer implements GameRenderer {

    public static Color paletteColor(int index) {
        return Color.web(NES.Palette.color(index));
    }

    // Blue colors used in intro, dark to brighter blue shade.
    // Cycles through palette indices 0x01, 0x11, 0x21, 0x31, each frame takes 16 ticks.
    private static Color shadeOfBlue(long tick) {
        int i = (int) (tick % 64) / 16;
        return paletteColor(0x01 + 0x10 * i);
    }

    // Maze images are taken from files "arcade_mazes.png" and "non_arcade_mazes.png" via AssetStorage

    private final AssetStorage assets;
    private final TengenMsPacManGameSpriteSheet spriteSheet;
    private final TengenArcadeMapsSpriteSheet arcadeMapsSpriteSheet;
    private final TengenNonArcadeMapsSpriteSheet nonArcadeMapSpriteSheet;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();

    private Canvas canvas;
    private Color bgColor = Color.BLACK;
    private ImageArea mapSprite;
    private boolean blinkingOn;
    private boolean levelNumberBoxesVisible;

    public TengenMsPacManGameRenderer(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        spriteSheet = assets.get("tengen.spritesheet");
        arcadeMapsSpriteSheet = new TengenArcadeMapsSpriteSheet(assets);
        nonArcadeMapSpriteSheet = new TengenNonArcadeMapsSpriteSheet(assets);
        terrainRenderer.scalingPy.bind(scalingPy);
        terrainRenderer.setMapBackgroundColor(bgColor);
        foodRenderer.scalingPy.bind(scalingPy);
    }

    private boolean mapSpriteExists(int levelNumber, MapCategory mapCategory) {
        return switch (mapCategory) {
            case ARCADE -> true; // all available in spritesheet
            case MINI -> false; // TODO use map sprite if level uses color scheme in sprite sheet
            case BIG -> false; // TODO use map sprite  if level uses color scheme in sprite sheet
            case STRANGE -> !inRange(levelNumber, 28, 31);
        };
    }

    @Override
    public void update(GameModel game) {
        if (game.level().isEmpty()) {
            Logger.warn("Cannot update renderer for game, no level exists");
            return;
        }
        GameLevel level = game.level().get();
        MapConfig mapConfig = level.mapConfig();
        MapCategory category = (MapCategory) mapConfig.mapCategory();
        mapSprite = switch (category) {
            case ARCADE  -> arcadeMapSpriteImageArea(mapConfig);
            case MINI    -> miniMapSpriteImageArea(mapConfig);
            case BIG     -> bigMapSpriteImageArea(mapConfig);
            case STRANGE -> strangeMapSpriteImageArea(mapConfig);
        };
        Logger.info("Level {}: Using map sprite area #{}", game.level().get().number, mapSprite.area());

        Map<String, String> mapColorScheme = mapConfig.colorScheme();
        terrainRenderer.setMapBackgroundColor(bgColor);
        terrainRenderer.setWallStrokeColor(Color.web(mapColorScheme.get("stroke")));
        terrainRenderer.setWallFillColor(Color.web(mapColorScheme.get("fill")));
        terrainRenderer.setDoorColor(Color.web(mapColorScheme.get("door")));
        foodRenderer.setPelletColor(Color.web(mapColorScheme.get("pellet")));
        foodRenderer.setEnergizerColor(Color.web(mapColorScheme.get("pellet")));
    }

    private ImageArea arcadeMapSpriteImageArea(MapConfig config) {
        var colorScheme = config.colorScheme();
        return switch (config.mapNumber()) {
            case 1 -> arcadeMapsSpriteSheet.mapSprite(0, 0);
            case 2 -> arcadeMapsSpriteSheet.mapSprite(0, 1);
            case 3 -> {
                if (colorScheme == MCS_16_20_15_ORANGE_WHITE_RED.get()) {
                    yield arcadeMapsSpriteSheet.mapSprite(0, 2);
                }
                if (colorScheme == MCS_35_28_20_PINK_YELLOW_WHITE.get()) {
                    yield arcadeMapsSpriteSheet.mapSprite(1, 1);
                }
                if (colorScheme == MCS_17_20_20_BROWN_WHITE_WHITE.get()) {
                    yield arcadeMapsSpriteSheet.mapSprite(2, 0);
                }
                if (colorScheme == MCS_0F_20_28_BLACK_WHITE_YELLOW.get()) {
                    yield arcadeMapsSpriteSheet.mapSprite(2, 2);
                }
                throw new IllegalArgumentException("Unknown color scheme for map 3: " + colorScheme);
            }
            case 4 -> {
                if (colorScheme == MCS_01_38_20_BLUE_YELLOW_WHITE.get()) {
                    yield arcadeMapsSpriteSheet.mapSprite(1, 0);
                }
                if (colorScheme == MCS_36_15_20_PINK_RED_WHITE.get()) {
                    yield arcadeMapsSpriteSheet.mapSprite(1, 2);
                }
                if (colorScheme == MCS_13_20_28_VIOLET_WHITE_YELLOW.get()) {
                    yield arcadeMapsSpriteSheet.mapSprite(2, 1);
                }
                throw new IllegalArgumentException("Unknown color scheme for map 4: " + colorScheme);
            }
            default -> throw new IllegalArgumentException("Illegal Arcade map number: " + config.mapNumber());
        };
    }

    private ImageArea miniMapSpriteImageArea(MapConfig config) {
        int spriteNumber = switch (config.mapNumber()) {
            case 1 -> 34;
            case 2 -> 35;
            case 3 -> 36;
            case 4 -> 30;
            case 5 -> 28;
            case 6 -> 37;
            default -> throw new IllegalArgumentException("Illegal MINI map number: " + config.mapNumber());
        };
        return nonArcadeMapSpriteSheet.mapSprite(spriteNumber);
    }

    private ImageArea bigMapSpriteImageArea(MapConfig config) {
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
        return nonArcadeMapSpriteSheet.mapSprite(spriteNumber);
    }

    private ImageArea strangeMapSpriteImageArea(MapConfig config) {
        // Dirty hack, don't tell Mommy!
        int spriteNumber = Integer.parseInt(config.worldMap().terrain().getProperty("levelNumber"));
        return nonArcadeMapSpriteSheet.mapSprite(spriteNumber);
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
        canvas.getGraphicsContext2D().setImageSmoothing(false);
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

    public void setLevelNumberBoxesVisible(boolean levelNumberBoxesVisible) {
        this.levelNumberBoxesVisible = levelNumberBoxesVisible;
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
    public void drawWorld(GameContext context, GameWorld world, double x, double y) {
        TengenMsPacManGame game = (TengenMsPacManGame) context.game();
        if (!isUsingDefaultGameOptions(game)) {
            drawGameOptionsInfo(context.level().world().map().terrain(), game);
        }

        // All maps that use a different color scheme than that in the sprite sheet have to be rendered using the
        // generic vector renderer for now. This looks more or less bad for specific maps.
        boolean mapSpriteExists = context.game().isDemoLevel() || mapSpriteExists(context.level().number, game.mapCategory());
        if (!mapSpriteExists) {
            terrainRenderer.drawMap(ctx(), world.map().terrain());
            world.map().food().tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerPosition))
                .forEach(tile -> foodRenderer.drawPellet(ctx(), tile));
            if (blinkingOn) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(ctx(), tile));
            }
        }
        else {
            // draw using map sprite
            drawWorldUsingMapSprite(game, context.gameClock().getUpdateCount(), x, y);
        }
        context.level().bonus().ifPresent(bonus -> drawMovingBonus(spriteSheet, (MovingBonus) bonus));
    }

    private boolean isUsingDefaultGameOptions(TengenMsPacManGame game) {
        return game.boosterMode() == BoosterMode.OFF &&
            game.difficulty() == Difficulty.NORMAL &&
            game.mapCategory() == MapCategory.ARCADE;
    }

    private void drawWorldUsingMapSprite(TengenMsPacManGame game, long t, double x, double y) {
        if (mapSprite == null) {
            return; // not yet selected
        }
        GameLevel level = game.level().orElseThrow();
        // Maze #32 of STRANGE has psychedelic animation
        if (level.mapConfig().mapCategory() == MapCategory.STRANGE &&
                level.mapConfig().mapNumber() == 32) {
            drawAnimatedMap(t, TengenNonArcadeMapsSpriteSheet.MAP_32_ANIMATION_FRAMES);
        } else {
            RectArea mapArea = mapSprite.area();
            ctx().drawImage(mapSprite.source(),
                mapArea.x(), mapArea.y(),
                mapArea.width(), mapArea.height(),
                scaled(x), scaled(y),
                scaled(mapArea.width()), scaled(mapArea.height())
            );
        }
        cleanHouse(level.world());
        drawFoodUsingMapSprite(level.world());
    }

    private void cleanHouse(GameWorld world) {
        Vector2f topLeftPosition = world.houseTopLeftTile().plus(1, 2).scaled(TS * scaling());
        Vector2f size = new Vector2i(world.houseSize().x() - 2, 2).scaled(TS * scaling());
        ctx().setFill(bgColor);
        ctx().fillRect(topLeftPosition.x(), topLeftPosition.y(), size.x(), size.y());
        hideActorSprite(world.map().terrain().getTileProperty("pos_pac", v2i(14, 26)));
        hideActorSprite(world.map().terrain().getTileProperty("pos_ghost_1_red", v2i(13, 14)));
    }

    private void drawFoodUsingMapSprite(GameWorld world) {
        ctx().save();
        ctx().scale(scaling(), scaling());
        overPaintEatenPellets(world);
        overPaintEnergizers(world, tile -> !blinkingOn || world.hasEatenFoodAt(tile));
        ctx().restore();
    }

    // Animation goes forward and backward: Cycle (0, 1, 2, 1)
    private void drawAnimatedMap(long tick, RectArea[] sprites) {
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
        Color color = paletteColor(0x20);
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
}