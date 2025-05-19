/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectArea;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.ui._2d.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.input.JoypadKeyBinding;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.model.actors.Bonus.STATE_EATEN;
import static de.amr.pacmanfx.model.actors.Bonus.STATE_EDIBLE;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_ANY_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.MapRepository.strangeMap15Sprite;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_PacAnimationMap.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_SpriteSheet.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.pacmanfx.ui.PacManGamesEnv.*;
import static de.amr.pacmanfx.ui._2d.GameSpriteSheet.NO_SPRITE;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_Renderer2D implements GameRenderer {

    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final FloatProperty scalingPy = new SimpleFloatProperty(1);
    private final TengenMsPacMan_SpriteSheet spriteSheet;
    private final MapRepository mapRepository;
    private final Canvas canvas;

    private ColoredMapSet coloredMapSet;

    public TengenMsPacMan_Renderer2D(TengenMsPacMan_SpriteSheet spriteSheet, MapRepository mapRepository, Canvas canvas) {
        this.spriteSheet = requireNonNull(spriteSheet);
        this.mapRepository = requireNonNull(mapRepository);
        this.canvas = requireNonNull(canvas);
    }

    public ObjectProperty<Color> backgroundColorProperty() { return  backgroundColorPy; }

    @Override
    public void applyMapSettings(WorldMap worldMap) {
        int flashCount = 5; // TODO: is this correct for all levels?
        coloredMapSet = mapRepository.createMazeSet(worldMap, flashCount);
        Logger.info("Created maze set with {} flash colors {}", flashCount, coloredMapSet);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public Canvas canvas() { return canvas; }

    @Override
    public FloatProperty scalingProperty() {
        return scalingPy;
    }

    @Override
    public void drawActor(Actor actor) {
        ctx().setImageSmoothing(false);
        if (actor instanceof Pac pac) {
            drawAnyPac(pac);
        } else {
            GameRenderer.super.drawActor(actor);
        }
    }

    private void drawAnyPac(Pac pac) {
        if (!pac.isVisible()) {
            return;
        }
        pac.animations().map(SpriteAnimationMap.class::cast).ifPresent(spriteAnimations -> {
            SpriteAnimation animation = spriteAnimations.currentAnimation();
            if (animation != null) {
                switch (spriteAnimations.selectedAnimationID()) {
                    case ANIM_ANY_PAC_MUNCHING,
                         ANIM_PAC_MAN_MUNCHING,
                         ANIM_MS_PAC_MAN_BOOSTER,
                         ANIM_PAC_MAN_BOOSTER,
                         ANIM_JUNIOR -> drawCreature(pac, pac.moveDir(), animation.currentSprite());
                    case ANIM_ANY_PAC_DYING -> {
                        Direction dir = Direction.UP;
                        if (animation.frameIndex() < 11) {
                            dir = switch (animation.frameIndex() % 4) {
                                default -> Direction.DOWN; // start with DOWN
                                case 1 -> Direction.LEFT;
                                case 2 -> Direction.UP;
                                case 3 -> Direction.RIGHT;
                            };
                        }
                        drawCreature(pac, dir, animation.currentSprite());
                    }
                    default -> GameRenderer.super.drawActor(pac);
                }
            } else {
                Logger.error("No current animation for character {}", pac);
            }
        });
    }

    private void drawCreature(Creature creature, Direction dir, RectArea spriteLookingLeft) {
        Vector2f center = creature.position().plus(HTS, HTS).scaled(scaling());
        ctx().save();
        ctx().translate(center.x(), center.y());
        switch (dir) {
            case UP    -> ctx().rotate(90);
            case LEFT  -> {}
            case RIGHT -> ctx().scale(-1, 1);
            case DOWN  -> { ctx().scale(-1, 1); ctx().rotate(-90); }
        }
        drawSpriteScaledCentered(spriteLookingLeft, 0, 0);
        ctx().restore();
    }

    public void drawSceneBorderLines() {
        ctx().setLineWidth(0.5);
        ctx().setStroke(Color.grayRgb(50));
        ctx().strokeLine(0.5, 0, 0.5, canvas().getHeight());
        ctx().strokeLine(canvas().getWidth() - 0.5, 0, canvas().getWidth() - 0.5, canvas().getHeight());
    }

    @Override
    public void drawMaze(GameLevel level, double x, double y, Color unusedBackgroundColor, boolean unusedHighlighted, boolean unusedBlinking) {
        if (coloredMapSet == null) {
            Logger.warn("Tick {}: Maze cannot be drawn, no map set found", theClock().tickCount());
            return;
        }

        final var tengenGame = (TengenMsPacMan_GameModel) theGame();
        final int mapNumber = level.worldMap().getConfigValue("mapNumber");

        ctx().setImageSmoothing(false);

        if (!tengenGame.optionsAreInitial()) {
            drawGameOptions(tengenGame.mapCategory(), tengenGame.difficulty(), tengenGame.pacBooster(),
                level.worldMap().numCols() * HTS, tiles_to_px(2) + HTS);
        }

        RectArea area = tengenGame.mapCategory() == MapCategory.STRANGE && mapNumber == 15
            ? strangeMap15Sprite(theClock().tickCount()) // Strange map #15: psychedelic animation
            : coloredMapSet.normalMaze().region();
        ctx().drawImage(coloredMapSet.normalMaze().source(),
            area.x(), area.y(), area.width(), area.height(),
            scaled(x), scaled(y), scaled(area.width()), scaled(area.height())
        );
        // The maze images also contain the ghost and Ms. Pac-Man sprites at their initial positions
        overPaintActors(level);
    }

    public void drawFood(GameLevel level) {
        if (coloredMapSet == null) {
            Logger.error("Draw food: no map set found");
            return;
        }
        ctx().save();
        ctx().scale(scaling(), scaling());
        Color pelletColor = Color.web(coloredMapSet.normalMaze().colorScheme().pelletColor());
        drawPellets(level, pelletColor);
        drawEnergizers(level, pelletColor);
        ctx().restore();
    }

    public void drawHighlightedWorld(GameLevel level, double mapX, double mapY, int flashingIndex) {
        final var tengenGame = (TengenMsPacMan_GameModel) theGame();
        ctx().setImageSmoothing(false);
        if (!tengenGame.optionsAreInitial()) {
            drawGameOptions(tengenGame.mapCategory(), tengenGame.difficulty(), tengenGame.pacBooster(),
                level.worldMap().numCols() * HTS, tiles_to_px(2) + HTS);
        }
        ColoredMapImage mapImage = coloredMapSet.flashingMazes().get(flashingIndex);
        RectArea region = mapImage.region();
        ctx().drawImage(mapImage.source(),
            region.x(), region.y(), region.width(), region.height(),
            scaled(mapX), scaled(mapY), scaled(region.width()), scaled(region.height())
        );
        overPaintActors(level);

        // draw food to erase eaten food!
        ctx().save();
        ctx().scale(scaling(), scaling());
        Color pelletColor = Color.web(coloredMapSet.normalMaze().colorScheme().pelletColor());
        drawPellets(level, pelletColor);
        drawEnergizers(level, pelletColor);
        ctx().restore();
    }

    private void drawPellets(GameLevel level, Color pelletColor) {
        level.worldMap().tiles().filter(level::isFoodPosition).filter(not(level::isEnergizerPosition)).forEach(tile -> {
            double cx = tile.x() * TS + HTS, cy = tile.y() * TS + HTS;
            ctx().setFill(backgroundColorPy.get());
            ctx().fillRect(cx - 2, cy - 2, 4, 4);
            if (!level.hasEatenFoodAt(tile)) {
                ctx().setFill(pelletColor);
                ctx().fillRect(cx - 1, cy - 1, 2, 2);
            }
        });
    }

    private void drawEnergizers(GameLevel level, Color pelletColor) {
        double size = TS;
        double offset = 0.5 * HTS;
        level.worldMap().tiles().filter(level::isEnergizerPosition).forEach(tile -> {
            double x = tile.x() * TS, y = tile.y() * TS;
            ctx().setFill(backgroundColorPy.get());
            ctx().fillRect(x - 1, y - 1, TS + 2, TS + 2); // avoid blitzer
            if (!level.hasEatenFoodAt(tile) && level.blinking().isOn()) {
                ctx().setFill(pelletColor);
                // draw pixelated "circle"
                ctx().fillRect(x + offset, y, HTS, size);
                ctx().fillRect(x, y + offset, size, HTS);
                ctx().fillRect(x + 1, y + 1, size - 2, size - 2);
            }
        });
    }

    @Override
    public void drawBonus(Bonus bonus) {
        MovingBonus movingBonus = (MovingBonus) bonus;
        ctx().save();
        ctx().setImageSmoothing(false);
        ctx().translate(0, movingBonus.elongationY());
        switch (bonus.state()) {
            case STATE_EDIBLE -> drawActorSprite(bonus.actor(), spriteSheet.bonusSymbolSprite(bonus.symbol()));
            case STATE_EATEN  -> drawActorSprite(bonus.actor(), spriteSheet.bonusValueSprite(bonus.symbol()));
            default -> {}
        }
        ctx().restore();
    }

    public void drawLevelMessage(GameLevel level, Vector2f position, Font font) {
        if (level.message() != null) {
            String ans = theUIConfig().current().assetNamespace();
            float x = position.x(), y = position.y();
            switch (level.message()) {
                case READY -> drawTextCenteredOver("READY!", x, y, theAssets().color(ans + ".color.ready_message"), font);
                case GAME_OVER -> {
                    Color color = theAssets().color(ans + ".color.game_over_message");
                    if (level.isDemoLevel()) {
                        NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
                        color = Color.web(nesColorScheme.strokeColor());
                    }
                    drawTextCenteredOver("GAME OVER", x, y, color, font);
                }
                case TEST_LEVEL -> drawTextCenteredOver("TEST L%02d".formatted(level.number()), x, y,
                    nesPaletteColor(0x28), font);
            }
        }
    }

    private void overPaintActors(GameLevel world) {
        Vector2f topLeftPosition = world.houseMinTile().plus(1, 2).scaled(TS * scaling());
        Vector2f size = new Vector2i(world.houseSizeInTiles().x() - 2, 2).scaled(TS * scaling());
        ctx().setFill(backgroundColorPy.get());
        ctx().fillRect(topLeftPosition.x(), topLeftPosition.y(), size.x(), size.y());
        overPaint(world.worldMap().getTerrainTileProperty("pos_pac", Vector2i.of(14, 26)));
        overPaint(world.worldMap().getTerrainTileProperty("pos_ghost_1_red", Vector2i.of(13, 14)));
    }

    private void overPaint(Vector2i tile) {
        // Parameter tile denotes the left of the two tiles where actor is located between. Compute center position.
        double cx = tile.x() * TS;
        double cy = tile.y() * TS - HTS;
        ctx().setFill(backgroundColorPy.get());
        ctx().fillRect(scaled(cx), scaled(cy), scaled(16), scaled(16));
    }

    public void drawGameOptions(MapCategory mapCategory, Difficulty difficulty, PacBooster pacBooster, double centerX, double y) {
        RectArea categorySprite = switch (mapCategory) {
            case BIG     -> BIG_SPRITE;
            case MINI    -> MINI_SPRITE;
            case STRANGE -> STRANGE_SPRITE;
            case ARCADE  -> NO_SPRITE;
        };
        RectArea difficultySprite = switch (difficulty) {
            case EASY   -> EASY_SPRITE;
            case HARD   -> HARD_SPRITE;
            case CRAZY  -> CRAZY_SPRITE;
            case NORMAL -> NO_SPRITE;
        };
        if (pacBooster != PacBooster.OFF) {
            drawSpriteScaledCentered(BOOSTER_SPRITE, centerX - tiles_to_px(6), y);
        }
        drawSpriteScaledCentered(difficultySprite, centerX, y);
        drawSpriteScaledCentered(categorySprite, centerX + tiles_to_px(4.5), y);
        drawSpriteScaledCentered(INFO_FRAME_SPRITE, centerX, y);
    }

    @Override
    public void drawScores(ScoreManager scoreManager, Color color, Font font) {
        if (scoreManager.isScoreVisible()) {
            if (theClock().tickCount() % 60 < 30) {
                fillTextAtScaledPosition("1UP", color, font, tiles_to_px(2), tiles_to_px(1));
            }
            fillTextAtScaledPosition("HIGH SCORE", color, font, tiles_to_px(9), tiles_to_px(1));
            fillTextAtScaledPosition("%6d".formatted(scoreManager.score().points()), color, font, 0, tiles_to_px(2));
            fillTextAtScaledPosition("%6d".formatted(scoreManager.highScore().points()), color, font, tiles_to_px(11), tiles_to_px(2));
        }
    }

    public void drawLevelCounterWithLevelNumbers(int levelNumber, LevelCounter levelCounter, Vector2f sizeInPx) {
        ctx().setImageSmoothing(false);
        float x = sizeInPx.x() - 2 * TS, y = sizeInPx.y() - TS;
        drawLevelNumberBox(levelNumber, 0, y); // left box
        drawLevelNumberBox(levelNumber, x, y); // right box
        x -= 2 * TS;
        for (byte symbol : levelCounter.symbols()) {
            drawSpriteScaled(spriteSheet().bonusSymbolSprite(symbol), x, y);
            x -= TS * 2;
        }
    }

    @Override
    public void drawLevelCounter(LevelCounter levelCounter, Vector2f sizeInPx) {
        ctx().setImageSmoothing(false);
        float x = sizeInPx.x() - 4 * TS, y = sizeInPx.y() - TS;
        for (byte symbol : levelCounter.symbols()) {
            drawSpriteScaled(spriteSheet().bonusSymbolSprite(symbol), x, y);
            x -= TS * 2;
        }
    }

    public void drawLevelNumberBox(int levelNumber, double x, double y) {
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
        ctx().save();
        ctx().scale(scaling, scaling);
        ctx().setFill(outlineColor);
        ctx().fillRect(0, y, width, TS);
        ctx().setFill(barColor);
        ctx().fillRect(0, y + 1, width, TS - 2);
        ctx().restore();
    }

    public void drawClapperBoard(ClapperboardAnimation animation, String text, int number, double x, double y, Font font) {
        animation.sprite().ifPresent(clapperBoard -> {
            ctx().setImageSmoothing(false);
            drawSpriteScaledOverSquare(clapperBoard, x, y);
            var numberX = x + 8;
            var numberY = y + 18; // baseline

            // over-paint number from sprite sheet
            ctx().save();
            ctx().scale(scaling(), scaling());
            ctx().setFill(backgroundColorPy.get());
            ctx().fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx().restore();

            ctx().setFont(font);
            ctx().setFill(nesPaletteColor(0x20));
            ctx().fillText(String.valueOf(number), scaled(numberX), scaled(numberY));
            if (animation.isTextVisible()) {
                double textX = x + clapperBoard.width();
                double textY = y + 2;
                ctx().fillText(text, scaled(textX), scaled(textY));
            }
        });
    }

    public void drawStork(SpriteAnimation storkAnimation, Actor stork, boolean hideBag) {
        if (!stork.isVisible()) {
            return;
        }
        Vector2f pos = stork.position();
        ctx().setImageSmoothing(false);
        drawSpriteScaled(storkAnimation.currentSprite(), pos.x(), pos.y());
        if (hideBag) { // over-paint bag under beak
            ctx().setFill(PY_CANVAS_BG_COLOR.get());
            ctx().fillRect(scaled(pos.x() - 1), scaled(pos.y() + 7), scaled(9), scaled(9));
        }
    }

    public void drawJoypadKeyBinding(JoypadKeyBinding joypad) {
        String line1 = " [SELECT]=%s   [START]=%s   [BUTTON B]=%s   [BUTTON A]=%s";
        String line2 = " [UP]=%s   [DOWN]=%s   [LEFT]=%s   [RIGHT]=%s";
        ctx().setFont(Font.font("Sans", scaled(TS)));
        ctx().setStroke(Color.WHITE);
        ctx().strokeText(line1.formatted(
                joypad.key(JoypadButton.SELECT),
                joypad.key(JoypadButton.START),
                joypad.key(JoypadButton.B),
                joypad.key(JoypadButton.A)
        ), 0, scaled(TS));
        ctx().strokeText(line2.formatted(
                joypad.key(JoypadButton.UP),
                joypad.key(JoypadButton.DOWN),
                joypad.key(JoypadButton.LEFT),
                joypad.key(JoypadButton.RIGHT)
        ), 0, scaled(2*TS));

    }

    private void drawTextCenteredOver(String text, double cx, double y, Color color, Font font) {
        double x = (cx - text.length() * 0.5 * TS);
        fillTextAtScaledPosition(text, color, font, x, y);
    }
}