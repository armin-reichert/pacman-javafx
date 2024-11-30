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
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.model.ms_pacman_tengen.*;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.input.JoypadKeyBinding;
import de.amr.games.pacman.ui2d.rendering.GameRenderer;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.rendering.ImageArea;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.SpriteAnimation;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.Animations.*;
import static de.amr.games.pacman.ui2d.GameAssets2D.assetPrefix;
import static de.amr.games.pacman.ui2d.rendering.GameSpriteSheet.NO_SPRITE;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenSceneConfig.nesPaletteColor;
import static de.amr.games.pacman.ui2d.scene.ms_pacman_tengen.MsPacManGameTengenSpriteSheet.*;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class MsPacManGameTengenRenderer implements GameRenderer {

    private final AssetStorage assets;
    private final MsPacManGameTengenSpriteSheet spriteSheet;
    private final NonArcadeMaps nonArcadeMaps;
    private final ArcadeMaps arcadeMaps;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final TerrainMapRenderer terrainRenderer = new TerrainMapRenderer();
    private final FoodMapRenderer foodRenderer = new FoodMapRenderer();
    private final Canvas canvas;

    private Color bgColor = Color.BLACK;
    private ImageArea mapSprite;
    private boolean blinking;
    private boolean levelNumberBoxesVisible;
    private Vector2f messageAnchorPosition;

    public MsPacManGameTengenRenderer(AssetStorage assets, MsPacManGameTengenSpriteSheet spriteSheet, Canvas canvas) {
        this.assets = checkNotNull(assets);
        this.spriteSheet = checkNotNull(spriteSheet);
        this.canvas = checkNotNull(canvas);

        arcadeMaps = new ArcadeMaps(assets);
        nonArcadeMaps = new NonArcadeMaps(assets);
        messageAnchorPosition = new Vector2f(14f * TS, 21 * TS);
        terrainRenderer.scalingPy.bind(scalingPy);
        terrainRenderer.setMapBackgroundColor(bgColor);
        foodRenderer.scalingPy.bind(scalingPy);
    }

    @Override
    public void update(WorldMap worldMap) {
        MapCategory category = worldMap.getConfigValue("mapCategory");
        NES_ColorScheme nesColorScheme = worldMap.getConfigValue("nesColorScheme");
        mapSprite = switch (category) {
            case ARCADE  -> arcadeMaps.sprite(worldMap);
            case MINI    -> nonArcadeMaps.miniMapSprite(worldMap);
            case BIG     -> nonArcadeMaps.bigMapSprite(worldMap);
            case STRANGE -> nonArcadeMaps.strangeMapSprite(worldMap);
        };

        terrainRenderer.setMapBackgroundColor(bgColor);
        terrainRenderer.setWallStrokeColor(Color.valueOf(nesColorScheme.strokeColor()));
        terrainRenderer.setWallFillColor(Color.valueOf(nesColorScheme.fillColor()));
        terrainRenderer.setDoorColor(Color.valueOf(nesColorScheme.strokeColor()));

        foodRenderer.setPelletColor(Color.valueOf(nesColorScheme.pelletColor()));
        foodRenderer.setEnergizerColor(Color.valueOf(nesColorScheme.pelletColor()));
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public MsPacManGameTengenSpriteSheet spriteSheet() {
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

    private boolean isMapImageAvailable(int levelNumber, MapCategory mapCategory) {
        return switch (mapCategory) {
            case ARCADE -> true; // all available in sprite sheet
            case MINI -> false; // TODO use map sprite if level uses color scheme in sprite sheet
            case BIG -> false; // TODO use map sprite  if level uses color scheme in sprite sheet
            case STRANGE -> !inClosedRange(levelNumber, 28, 31); // all except those with random color scheme
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
                        case ANIM_PAC_MUNCHING, ANIM_MS_PACMAN_BOOSTER,
                             ANIM_MR_PACMAN_MUNCHING, ANIM_MR_PACMAN_BOOSTER,
                             ANIM_JUNIOR_PACMAN -> drawGuyHeading(pac, pac.moveDir(), spriteAnimation);
                        case ANIM_PAC_DYING -> {
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

    public void drawSceneBorders() {
        ctx().setLineWidth(0.5);
        ctx().setStroke(Color.grayRgb(50));
        ctx().strokeLine(0.5, 0, 0.5, canvas.getHeight());
        ctx().strokeLine(canvas.getWidth() - 0.5, 0, canvas.getWidth() - 0.5, canvas.getHeight());
    }

    public void drawEmptyMap(WorldMap worldMap, Color fillColor, Color strokeColor) {
        terrainRenderer.setMapBackgroundColor(bgColor);
        terrainRenderer.setWallFillColor(fillColor);
        terrainRenderer.setWallStrokeColor(strokeColor);
        terrainRenderer.setDoorColor(strokeColor);
        terrainRenderer.drawMap(ctx(), worldMap.terrain());
    }

    @Override
    public void drawWorld(GameContext context, GameWorld world, double mapX, double mapY) {
        ctx().setImageSmoothing(false);

        var game = (MsPacManGameTengen) context.game();
        GameLevel level = game.level().orElseThrow();

        if (!isUsingDefaultGameOptions(game)) {
            drawGameOptionsInfo(world.map().terrain(), game);
        }

        // Maps with a color scheme differing from that in the sprite sheet have to be rendered using the
        // vector renderer for now.
        // TODO: vector rendering looks really bad for some maps.
        MapCategory mapCategory = game.mapCategory(); // must use this one because e.g. STRANGE maps use maps from BIG map set etc.
        int mapNumber = world.map().getConfigValue("mapNumber");
        boolean mapImageExists = game.isDemoLevel() || isMapImageAvailable(level.number, mapCategory);
        if (mapImageExists) {
            drawLevelMessage(level, game.isDemoLevel()); // message appears under map image so draw it first
            RectArea area = mapCategory == MapCategory.STRANGE && mapNumber == 15
                ? nonArcadeMaps.strangeMap15Sprite(context.tick()) // Strange map #15: psychedelic animation
                : mapSprite.area();
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
            world.map().food().tiles().filter(world::hasFoodAt).filter(not(world::isEnergizerPosition))
                .forEach(tile -> foodRenderer.drawPellet(ctx(), tile));
            if (blinking) {
                world.energizerTiles().filter(world::hasFoodAt).forEach(tile -> foodRenderer.drawEnergizer(ctx(), tile));
            }
            drawLevelMessage(level, game.isDemoLevel());
            terrainRenderer.drawMap(ctx(), world.map().terrain());
        }
        context.level().bonus().ifPresent(bonus -> drawMovingBonus(spriteSheet, (MovingBonus) bonus));
    }

    private void drawLevelMessage(GameLevel level, boolean demoLevel) {
        if (level.message() != null) {
            String assetPrefix = assetPrefix(GameVariant.MS_PACMAN_TENGEN);
            float x = getMessageAnchorPosition().x(), y = getMessageAnchorPosition().y();
            switch (level.message().type()) {
                case READY -> drawText("READY!", x, y, assets.color(assetPrefix + ".color.ready_message"));
                case GAME_OVER -> {
                    Color color = assets.color(assetPrefix + ".color.game_over_message");
                    if (demoLevel) {
                        WorldMap worldMap = level.world().map();
                        NES_ColorScheme nesColorScheme = worldMap.getConfigValue("nesColorScheme");
                        color = Color.valueOf(nesColorScheme.strokeColor());
                    }
                    drawText("GAME OVER", x, y, color);
                }
                case TEST_LEVEL -> drawText("TEST L%02d".formatted(level.number), x, y, nesPaletteColor(0x28));
            }
        }
    }

    private boolean isUsingDefaultGameOptions(MsPacManGameTengen game) {
        return game.pacBooster() == PacBooster.OFF &&
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

    private void drawGameOptionsInfo(TileMap terrain, MsPacManGameTengen game) {
        RectArea categorySprite = switch (game.mapCategory()) {
            case BIG     -> BIG_SPRITE;
            case MINI    -> MINI_SPRITE;
            case STRANGE -> STRANGE_SPRITE;
            case ARCADE  -> NO_SPRITE;
        };
        RectArea difficultySprite = switch (game.difficulty()) {
            case EASY   -> EASY_SPRITE;
            case HARD   -> HARD_SPRITE;
            case CRAZY  -> CRAZY_SPRITE;
            case NORMAL -> NO_SPRITE;
        };
        double centerX = terrain.numCols() * HTS;
        double y = t(2) + HTS;
        if (game.pacBooster() != PacBooster.OFF) {
            drawSpriteCenteredOverPosition(BOOSTER_SPRITE, centerX - t(6), y);
        }
        drawSpriteCenteredOverPosition(difficultySprite, centerX, y);
        drawSpriteCenteredOverPosition(categorySprite, centerX + t(4.5), y);
        drawSpriteCenteredOverPosition(INFO_FRAME_SPRITE, centerX, y);
    }

    @Override
    public void drawScores(GameContext context) {
        Color color = nesPaletteColor(0x20);
        Font font = scaledArcadeFont(TS);
        if (context.gameClock().getTickCount() % 60 < 30) { drawText("1UP", color, font, t(2), t(1)); }
        drawText("HIGH SCORE", color, font, t(9), t(1));
        drawText("%6d".formatted(context.game().scoreManager().score().points()), color, font, 0, t(2));
        drawText("%6d".formatted(context.game().scoreManager().highScore().points()), color, font, t(11), t(2));
    }

    @Override
    public void drawLevelCounter(GameContext context, double x, double y) {
        ctx().setImageSmoothing(false);
        MsPacManGameTengen game = (MsPacManGameTengen) context.game();
        int levelNumber = context.level().number;
        if (levelNumberBoxesVisible) {
            drawLevelNumberBox(levelNumber, 0, y); // left box
            drawLevelNumberBox(levelNumber, x, y); // right box
        }
        double symbolX = x - 2 * TS;
        for (byte symbol : game.levelCounter()) {
            drawSpriteScaled(spriteSheet().bonusSymbolSprite(symbol), symbolX, y);
            symbolX -= TS * 2;
        }
    }

    private void drawLevelNumberBox(int levelNumber, double x, double y) {
        drawSpriteScaled(LEVEL_BOX_SPRITE, x, y);
        double digitY = y + 2;
        int tens = levelNumber / 10, ones = levelNumber % 10;
        drawSpriteScaled(spriteSheet.digit(ones), x + 10, digitY);
        if (tens > 0) {
            drawSpriteScaled(spriteSheet.digit(tens), x + 2,  digitY);
        }
    }

    public void drawAnimatedTengenPresentsText(long t, double x, double y) {
        drawText("TENGEN PRESENTS", shadeOfBlue(t), scaledArcadeFont(TS), x, y);
    }

    // Blue colors used in intro, dark to brighter blue shade.
    // Cycles through palette indices 0x01, 0x11, 0x21, 0x31, each frame takes 16 ticks.
    private Color shadeOfBlue(long tick) {
        int i = (int) (tick % 64) / 16;
        return nesPaletteColor(0x01 + 0x10 * i);
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

    public void drawJoypadBindings(JoypadKeyBinding binding) {
        String line1 = " [SELECT]=%s   [START]=%s   [BUTTON B]=%s   [BUTTON A]=%s";
        String line2 = " [UP]=%s   [DOWN]=%s   [LEFT]=%s   [RIGHT]=%s";
        ctx().setFont(Font.font("Sans", scaled(TS)));
        ctx().setStroke(Color.WHITE);
        ctx().strokeText(line1.formatted(
            binding.key(NES.JoypadButton.BTN_SELECT),
            binding.key(NES.JoypadButton.BTN_START),
            binding.key(NES.JoypadButton.BTN_B),
            binding.key(NES.JoypadButton.BTN_A)
        ), 0, scaled(TS));
        ctx().strokeText(line2.formatted(
                binding.key(NES.JoypadButton.BTN_UP),
                binding.key(NES.JoypadButton.BTN_DOWN),
                binding.key(NES.JoypadButton.BTN_LEFT),
                binding.key(NES.JoypadButton.BTN_RIGHT)
        ), 0, scaled(2*TS));

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