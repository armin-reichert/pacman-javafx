/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman.rendering2d;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.RectArea;
import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.lib.tilemap.TileMap;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.model.GameWorld;
import de.amr.games.pacman.model.actors.*;
import de.amr.games.pacman.tengen.ms_pacman.Difficulty;
import de.amr.games.pacman.tengen.ms_pacman.MsPacManGameTengen;
import de.amr.games.pacman.tengen.ms_pacman.PacBooster;
import de.amr.games.pacman.tengen.ms_pacman.maps.ColoredMapImage;
import de.amr.games.pacman.tengen.ms_pacman.maps.ColoredMapSet;
import de.amr.games.pacman.tengen.ms_pacman.maps.MapCategory;
import de.amr.games.pacman.tengen.ms_pacman.maps.MapRepository;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.GameRenderer;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.assets.SpriteAnimation;
import de.amr.games.pacman.ui2d.assets.SpriteAnimationSet;
import de.amr.games.pacman.ui2d.input.JoypadKeyBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.games.pacman.lib.Globals.*;
import static de.amr.games.pacman.model.actors.ActorAnimations.*;
import static de.amr.games.pacman.model.actors.Bonus.STATE_EATEN;
import static de.amr.games.pacman.model.actors.Bonus.STATE_EDIBLE;
import static de.amr.games.pacman.tengen.ms_pacman.TengenMsPacMan_GameConfiguration.nesPaletteColor;
import static de.amr.games.pacman.tengen.ms_pacman.maps.MapRepository.strangeMap15Sprite;
import static de.amr.games.pacman.tengen.ms_pacman.rendering2d.TengenMsPacMan_SpriteSheet.*;
import static de.amr.games.pacman.ui2d.assets.GameSpriteSheet.NO_SPRITE;
import static java.util.function.Predicate.not;

/**
 * @author Armin Reichert
 */
public class TengenMsPacMan_Renderer2D implements GameRenderer {

    private static final Color CANVAS_BACKGROUND_COLOR = Color.BLACK;
    private static final Vector2f DEFAULT_MESSAGE_ANCHOR_POS = new Vector2f(14f * TS, 21 * TS);

    private final AssetStorage assets;
    private final TengenMsPacMan_SpriteSheet spriteSheet;
    private final MapRepository mapRepository;
    private final DoubleProperty scalingPy = new SimpleDoubleProperty(1.0);
    private final GraphicsContext ctx;

    private ColoredMapSet coloredMapSet;
    private boolean blinking;
    private boolean levelNumberBoxesVisible;
    private Vector2f messageAnchorPosition;

    public TengenMsPacMan_Renderer2D(
        AssetStorage assets,
        TengenMsPacMan_SpriteSheet spriteSheet,
        MapRepository mapRepository,
        Canvas canvas)
    {
        this.assets = assertNotNull(assets);
        this.spriteSheet = assertNotNull(spriteSheet);
        this.mapRepository = mapRepository;
        assertNotNull(canvas);
        ctx = canvas.getGraphicsContext2D();
        messageAnchorPosition = DEFAULT_MESSAGE_ANCHOR_POS;
    }

    @Override
    public void setWorldMap(WorldMap worldMap) {
        int flashCount = 5; // TODO correct for all levels?
        Logger.info("Create maze set with {} flash colors", flashCount);
        coloredMapSet = mapRepository.createMazeSet(worldMap, flashCount);
        Logger.info("Maze set {}", coloredMapSet);
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public Canvas canvas() { return ctx.getCanvas(); }

    @Override
    public void setMazeHighlighted(boolean flashMode) {}

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
        return CANVAS_BACKGROUND_COLOR;
    }

    @Override
    public void setBackgroundColor(Color color) {}

    @Override
    public void drawAnimatedActor(AnimatedActor2D guy) {
        ctx.setImageSmoothing(false);
        if (guy instanceof Pac pac) {
            drawMsOrMrPacMan(pac);
        } else {
            GameRenderer.super.drawAnimatedActor(guy);
        }
    }

    @Override
    public Vector2f getMessagePosition() {
        return messageAnchorPosition;
    }

    public void setMessagePosition(Vector2f position) {
        messageAnchorPosition = position;
    }

    public void setLevelNumberBoxesVisible(boolean visible) {
        levelNumberBoxesVisible = visible;
    }

    private void drawMsOrMrPacMan(Pac pac) {
        if (!pac.isVisible()) {
            return;
        }
        pac.animations().map(SpriteAnimationSet.class::cast).ifPresent(spriteAnimations -> {
            SpriteAnimation animation = spriteAnimations.currentAnimation();
            if (animation != null) {
                switch (spriteAnimations.currentID()) {
                    case ANIM_PAC_MUNCHING, ANIM_MS_PACMAN_BOOSTER,
                         ANIM_MR_PACMAN_MUNCHING, ANIM_MR_PACMAN_BOOSTER,
                         ANIM_JUNIOR_PACMAN -> drawGuy(pac, pac.moveDir(), animation.currentSprite());
                    case ANIM_PAC_DYING -> {
                        Direction dir = Direction.UP;
                        if (animation.frameIndex() < 11) {
                            dir = switch (animation.frameIndex() % 4) {
                                default -> Direction.DOWN; // start with DOWN
                                case 1 -> Direction.LEFT;
                                case 2 -> Direction.UP;
                                case 3 -> Direction.RIGHT;
                            };
                        }
                        drawGuy(pac, dir, animation.currentSprite());
                    }
                    default -> GameRenderer.super.drawAnimatedActor(pac);
                }
            } else {
                Logger.error("No current animation for character {}", pac);
            }
        });
    }

    private void drawGuy(Creature guy, Direction dir, RectArea spriteLookingLeft) {
        Vector2f center = guy.position().plus(HTS, HTS).scaled((float) scaling());
        ctx.save();
        ctx.translate(center.x(), center.y());
        switch (dir) {
            case UP    -> ctx.rotate(90);
            case LEFT  -> {}
            case RIGHT -> ctx.scale(-1, 1);
            case DOWN  -> { ctx.scale(-1, 1); ctx.rotate(-90); }
        }
        drawSpriteCenteredOverPosition(spriteLookingLeft, 0, 0);
        ctx.restore();
    }

    public void drawSceneBorderLines() {
        ctx.setLineWidth(0.5);
        ctx.setStroke(Color.grayRgb(50));
        ctx.strokeLine(0.5, 0, 0.5, canvas().getHeight());
        ctx.strokeLine(canvas().getWidth() - 0.5, 0, canvas().getWidth() - 0.5, canvas().getHeight());
    }

    @Override
    public void drawWorld(GameWorld world, double x, double y) {
    }

    public void drawWorld(GameContext context, GameWorld world, double mapX, double mapY) {
        ctx.setImageSmoothing(false);

        MsPacManGameTengen game = (MsPacManGameTengen) context.game();
        GameLevel level = context.level();
        MapCategory mapCategory = game.mapCategory();
        int mapNumber = world.map().getConfigValue("mapNumber");
        String assetKeyPrefix = context.gameConfiguration().assetKeyPrefix();

        if (areGameOptionsChanged(game)) {
            drawGameOptionsInfo(world.map().terrain(), game);
        }

        if (coloredMapSet == null) {
            // setWorldMap() not yet called?
            Logger.warn("Tick {}: No maze available", context.tick());
            return;
        }

        drawLevelMessage(assetKeyPrefix, level, game.isDemoLevel()); // message appears under map image so draw it first
        RectArea area = mapCategory == MapCategory.STRANGE && mapNumber == 15
            ? strangeMap15Sprite(context.tick()) // Strange map #15: psychedelic animation
            : coloredMapSet.normalMaze().area();
        ctx.drawImage(coloredMapSet.normalMaze().source(),
            area.x(), area.y(), area.width(), area.height(),
            scaled(mapX), scaled(mapY), scaled(area.width()), scaled(area.height())
        );
        overPaintActors(world);
        ctx.save();
        ctx.scale(scaling(), scaling());
        Color pelletColor = Color.valueOf(coloredMapSet.normalMaze().colorScheme().pelletColor());
        drawPellets(world, pelletColor);
        drawEnergizers(world, pelletColor);
        ctx.restore();
    }

    public void drawWorldHighlighted(GameContext context, GameWorld world, double mapX, double mapY, int flashingIndex) {
        ctx.setImageSmoothing(false);
        MsPacManGameTengen game = (MsPacManGameTengen) context.game();
        if (areGameOptionsChanged(game)) {
            drawGameOptionsInfo(world.map().terrain(), game);
        }
        ColoredMapImage maze = coloredMapSet.flashingMazes().get(flashingIndex);
        RectArea area = maze.area();
        ctx.drawImage(maze.source(),
            area.x(), area.y(), area.width(), area.height(),
            scaled(mapX), scaled(mapY), scaled(area.width()), scaled(area.height())
        );
        overPaintActors(world);
        // draw food to erase eaten food!
        ctx.save();
        ctx.scale(scaling(), scaling());
        Color pelletColor = Color.valueOf(coloredMapSet.normalMaze().colorScheme().pelletColor());
        drawPellets(world, pelletColor);
        drawEnergizers(world, pelletColor);
        ctx.restore();
    }

    private void drawPellets(GameWorld world, Color pelletColor) {
        world.map().food().tiles()
            .filter(world::isFoodPosition)
            .filter(not(world::isEnergizerPosition))
            .forEach(tile -> {
                double centerX = tile.x() * TS + HTS, centerY = tile.y() * TS + HTS;
                ctx.setFill(CANVAS_BACKGROUND_COLOR);
                ctx.fillRect(centerX - 2, centerY - 2, 4, 4);
                if (!world.hasEatenFoodAt(tile)) {
                    ctx.setFill(pelletColor);
                    ctx.fillRect(centerX - 1, centerY - 1, 2, 2);
                }
            });
    }

    private void drawEnergizers(GameWorld world, Color pelletColor) {
        double size = TS;
        double offset = 0.5 * (HTS);
        world.map().food().tiles().filter(world::isEnergizerPosition).forEach(energizerTile -> {
            double x = energizerTile.x() * TS, y = energizerTile.y() * TS;
            ctx.setFill(CANVAS_BACKGROUND_COLOR);
            ctx.fillRect(x-1, y-1, TS + 2, TS + 2); // avoid blitzer
            if (!world.hasEatenFoodAt(energizerTile) && blinking) {
                ctx.setFill(pelletColor);
                // draw pixelized "circle"
                ctx.fillRect(x + offset, y, HTS, size);
                ctx.fillRect(x, y + offset, size, HTS);
                ctx.fillRect(x + 1, y + 1, size - 2, size - 2);
            }
        });
    }

    @Override
    public void drawBonus(Bonus bonus) {
        ctx.save();
        ctx.setImageSmoothing(false);
        ctx.translate(0, ((MovingBonus) bonus).elongationY());
        switch (bonus.state()) {
            case STATE_EDIBLE -> drawActorSprite(bonus.actor(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
            case STATE_EATEN  -> drawActorSprite(bonus.actor(), spriteSheet.bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        ctx.restore();
    }

    private void drawLevelMessage(String assetKeyPrefix, GameLevel level, boolean demoLevel) {
        if (level.message() != null) {
            float x = getMessagePosition().x(), y = getMessagePosition().y();
            switch (level.message().type()) {
                case READY -> drawTextCenteredOver("READY!", x, y, assets.color(assetKeyPrefix + ".color.ready_message"));
                case GAME_OVER -> {
                    Color color = assets.color(assetKeyPrefix + ".color.game_over_message");
                    if (demoLevel) {
                        WorldMap worldMap = level.world().map();
                        NES_ColorScheme nesColorScheme = worldMap.getConfigValue("nesColorScheme");
                        color = Color.valueOf(nesColorScheme.strokeColor());
                    }
                    drawTextCenteredOver("GAME OVER", x, y, color);
                }
                case TEST_LEVEL -> drawTextCenteredOver("TEST L%02d".formatted(level.number), x, y, nesPaletteColor(0x28));
            }
        }
    }

    private boolean areGameOptionsChanged(MsPacManGameTengen game) {
        return game.pacBooster() != PacBooster.OFF || game.difficulty() != Difficulty.NORMAL || game.mapCategory() != MapCategory.ARCADE;
    }

    private void overPaintActors(GameWorld world) {
        Vector2f topLeftPosition = world.houseTopLeftTile().plus(1, 2).scaled(TS * scaling());
        Vector2f size = new Vector2i(world.houseSize().x() - 2, 2).scaled(TS * scaling());
        ctx.setFill(CANVAS_BACKGROUND_COLOR);
        ctx.fillRect(topLeftPosition.x(), topLeftPosition.y(), size.x(), size.y());

        overPaint(world.map().terrain().getTileProperty("pos_pac", vec_2i(14, 26)));
        overPaint(world.map().terrain().getTileProperty("pos_ghost_1_red", vec_2i(13, 14)));
    }

    private void overPaint(Vector2i tile) {
        // Parameter tile denotes the left of the two tiles where actor is located between. Compute center position.
        double cx = tile.x() * TS;
        double cy = tile.y() * TS - HTS;
        ctx.setFill(CANVAS_BACKGROUND_COLOR);
        ctx.fillRect(scaled(cx), scaled(cy), scaled(16), scaled(16));
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
        double y = tiles2Px(2) + HTS;
        if (game.pacBooster() != PacBooster.OFF) {
            drawSpriteCenteredOverPosition(BOOSTER_SPRITE, centerX - tiles2Px(6), y);
        }
        drawSpriteCenteredOverPosition(difficultySprite, centerX, y);
        drawSpriteCenteredOverPosition(categorySprite, centerX + tiles2Px(4.5), y);
        drawSpriteCenteredOverPosition(INFO_FRAME_SPRITE, centerX, y);
    }

    @Override
    public void drawScores(GameContext context) {
        Color color = nesPaletteColor(0x20);
        Font font = scaledArcadeFont(TS);
        if (context.gameClock().getTickCount() % 60 < 30) { drawText("1UP", color, font, tiles2Px(2), tiles2Px(1)); }
        drawText("HIGH SCORE", color, font, tiles2Px(9), tiles2Px(1));
        drawText("%6d".formatted(context.game().scoreManager().score().points()), color, font, 0, tiles2Px(2));
        drawText("%6d".formatted(context.game().scoreManager().highScore().points()), color, font, tiles2Px(11), tiles2Px(2));
    }

    @Override
    public void drawLevelCounter(GameContext context, double x, double y) {
        ctx.setImageSmoothing(false);
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

    // Blue colors used in intro, dark to brighter blue shade.
    // Cycles through palette indices 0x01, 0x11, 0x21, 0x31, each frame takes 16 ticks.
    public Color shadeOfBlue(long tick) {
        int i = (int) (tick % 64) / 16;
        return nesPaletteColor(0x01 + 0x10 * i);
    }

    public void drawBar(Color outlineColor, Color barColor, double width, double y) {
        double scaling = scaling();
        ctx.save();
        ctx.scale(scaling, scaling);
        ctx.setFill(outlineColor);
        ctx.fillRect(0, y, width, TS);
        ctx.setFill(barColor);
        ctx.fillRect(0, y + 1, width, TS - 2);
        ctx.restore();
    }

    public void drawClapperBoard(
        ClapperboardAnimation clap,
        String text, int number,
        double x, double y)
    {
        clap.sprite().ifPresent(sprite -> {
            ctx.setImageSmoothing(false);
            drawSpriteCenteredOverTile(sprite, x, y);
            var numberX = x + 8;
            var numberY = y + 18; // baseline
            ctx.setFill(CANVAS_BACKGROUND_COLOR);
            ctx.save();
            ctx.scale(scaling(), scaling());
            ctx.fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx.restore();
            ctx.setFont(scaledArcadeFont(TS));
            ctx.setFill(nesPaletteColor(0x20));
            ctx.fillText(String.valueOf(number), scaled(numberX), scaled(numberY));
            if (clap.isTextVisible()) {
                double textX = x + sprite.width();
                double textY = y + 2;
                ctx.fillText(text, scaled(textX), scaled(textY));
            }
        });
    }

    public void drawStork(SpriteAnimation storkAnimation, Actor2D stork, boolean hideBag) {
        if (!stork.isVisible()) {
            return;
        }
        Vector2f pos = stork.position();
        ctx.setImageSmoothing(false);
        drawSpriteScaled(storkAnimation.currentSprite(), pos.x(), pos.y());
        if (hideBag) { // over-paint bag under beak
            ctx.setFill(CANVAS_BACKGROUND_COLOR);
            ctx.fillRect(scaled(pos.x() - 1), scaled(pos.y() + 7), scaled(9), scaled(9));
        }
    }

    public void drawJoypadBindings(JoypadKeyBinding binding) {
        String line1 = " [SELECT]=%s   [START]=%s   [BUTTON B]=%s   [BUTTON A]=%s";
        String line2 = " [UP]=%s   [DOWN]=%s   [LEFT]=%s   [RIGHT]=%s";
        ctx.setFont(Font.font("Sans", scaled(TS)));
        ctx.setStroke(Color.WHITE);
        ctx.strokeText(line1.formatted(
            binding.key(NES_JoypadButton.BTN_SELECT),
            binding.key(NES_JoypadButton.BTN_START),
            binding.key(NES_JoypadButton.BTN_B),
            binding.key(NES_JoypadButton.BTN_A)
        ), 0, scaled(TS));
        ctx.strokeText(line2.formatted(
            binding.key(NES_JoypadButton.BTN_UP),
            binding.key(NES_JoypadButton.BTN_DOWN),
            binding.key(NES_JoypadButton.BTN_LEFT),
            binding.key(NES_JoypadButton.BTN_RIGHT)
        ), 0, scaled(2*TS));

    }

    private void drawTextCenteredOver(String text, double cx, double y, Color color) {
        double x = (cx - text.length() * 0.5 * TS);
        drawText(text, color, scaledArcadeFont(TS), x, y);
    }
}