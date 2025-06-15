/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.input.JoypadKeyBinding;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.model.actors.Bonus.STATE_EATEN;
import static de.amr.pacmanfx.model.actors.Bonus.STATE_EDIBLE;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_MapRepository.strangeMap15Sprite;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_PacAnimationMap.*;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.pacmanfx.ui.PacManGames_Env.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_CANVAS_BG_COLOR;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_Renderer2D extends SpriteGameRenderer {

    public static Color blueShadedColor(long tick) {
        // Blue color, changing from dark blue to brighter blue.
        // Cycles through palette indices 0x01, 0x11, 0x21, 0x31, each 16 ticks.
        int i = (int) (tick % 64) / 16;
        return nesPaletteColor(0x01 + i * 0x10);
    }

    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final FloatProperty scalingPy = new SimpleFloatProperty(1);
    private final TengenMsPacMan_SpriteSheet spriteSheet;
    private final TengenMsPacMan_MapRepository mapRepository;
    private final GraphicsContext ctx;

    private ColoredMapConfiguration coloredMapSet;

    public TengenMsPacMan_Renderer2D(TengenMsPacMan_SpriteSheet spriteSheet, TengenMsPacMan_MapRepository mapRepository, Canvas canvas) {
        this.spriteSheet = requireNonNull(spriteSheet);
        this.mapRepository = requireNonNull(mapRepository);
        ctx = requireNonNull(canvas).getGraphicsContext2D();
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    public ObjectProperty<Color> backgroundColorProperty() { return  backgroundColorPy; }

    public void ensureMapSettingsApplied(GameLevel level) {
        requireNonNull(level);
        if (coloredMapSet == null) {
            applyRenderingHints(level);
        }
    }

    @Override
    public void applyRenderingHints(GameLevel level) {
        requireNonNull(level);
        int flashCount = level.data().numFlashes();
        coloredMapSet = mapRepository.createMapSequence(level.worldMap(), flashCount);
        Logger.info("Created maze set with {} flash colors {}", flashCount, coloredMapSet);
    }

    @Override
    public GraphicsContext ctx() { return ctx; }

    @Override
    public FloatProperty scalingProperty() { return scalingPy; }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        ctx().setImageSmoothing(false);
        if (actor instanceof Pac pac) {
            drawAnyKindOfPac(pac);
        }
        else if (actor instanceof TengenMsPacMan_CutScene3.Stork stork) {
            super.drawActor(stork);
            if (stork.isBagReleasedFromBeak()) { // over-paint bag still hanging at beak
                ctx().setFill(PY_CANVAS_BG_COLOR.get());
                //ctx().setFill(Color.RED);
                //TODO: clarify
                float spriteX = stork.x() - 13;
                float spriteY = stork.y() - 6;
                ctx().fillRect(scaled(spriteX), scaled(spriteY + 9), scaled(8), scaled(10));
            }
        }
        else {
            super.drawActor(actor);
        }
    }

    private void drawAnyKindOfPac(Pac pac) {
        if (!pac.isVisible()) {
            return;
        }
        pac.animations().map(SpriteAnimationMap.class::cast).ifPresent(spriteAnimations -> {
            SpriteAnimation animation = spriteAnimations.currentAnimation();
            if (animation == null) {
                Logger.error("No animation found for {}", pac);
                return;
            }
            switch (spriteAnimations.selectedAnimationID()) {
                case ANIM_PAC_MUNCHING,
                     ANIM_PAC_MAN_MUNCHING,
                     ANIM_MS_PAC_MAN_BOOSTER,
                     ANIM_PAC_MAN_BOOSTER,
                     ANIM_JUNIOR
                    -> drawMovingActor(pac, pac.moveDir(), (Sprite) animation.currentSprite());
                case ANIM_PAC_DYING -> {
                    //TODO: reconsider this
                    Direction dir = Direction.UP;
                    if (animation.frameIndex() < 11) {
                        dir = switch (animation.frameIndex() % 4) {
                            default -> Direction.DOWN; // start with DOWN
                            case 1 -> Direction.LEFT;
                            case 2 -> Direction.UP;
                            case 3 -> Direction.RIGHT;
                        };
                    }
                    drawMovingActor(pac, dir, (Sprite) animation.currentSprite());
                }
                default -> super.drawActor(pac);
            }
        });
    }

    private void drawMovingActor(MovingActor actor, Direction dir, Sprite spriteLookingLeft) {
        Vector2f center = actor.center().scaled(scaling());
        ctx().save();
        ctx().translate(center.x(), center.y());
        switch (dir) {
            case UP    -> ctx().rotate(90);
            case LEFT  -> {}
            case RIGHT -> ctx().scale(-1, 1);
            case DOWN  -> { ctx().scale(-1, 1); ctx().rotate(-90); }
        }
        drawSpriteScaledCenteredAt(spriteLookingLeft, 0, 0);
        ctx().restore();
    }

    public void drawVerticalSceneBorders() {
        double width = ctx.getCanvas().getWidth(), height = ctx.getCanvas().getHeight();
        ctx().setLineWidth(0.5);
        ctx().setStroke(Color.grayRgb(50));
        ctx().strokeLine(0.5, 0, 0.5, height);
        ctx().strokeLine(width - 0.5, 0, width - 0.5, height);
    }

    @Override
    public void drawLevel(GameLevel level, double x, double y, Color optionalBackgroundColor,
                          boolean mazeHighlighted, boolean energizerHighlighted) {
        requireNonNull(level);
        if (coloredMapSet == null) {
            Logger.warn("Tick {}: Level cannot be drawn, no colored map set found", theClock().tickCount());
            return;
        }

        final var tengenGame = (TengenMsPacMan_GameModel) theGame();
        final int mapNumber = level.worldMap().getConfigValue("mapNumber");

        ctx().setImageSmoothing(false);

        if (!tengenGame.optionsAreInitial()) {
            drawGameOptions(tengenGame.mapCategory(), tengenGame.difficulty(), tengenGame.pacBooster(),
                level.worldMap().numCols() * HTS, tiles_to_px(2) + HTS);
        }

        Sprite area = tengenGame.mapCategory() == MapCategory.STRANGE && mapNumber == 15
            ? strangeMap15Sprite(theClock().tickCount()) // Strange map #15: psychedelic animation
            : coloredMapSet.mapRegion().region();
        ctx().drawImage(coloredMapSet.mapRegion().image(),
            area.x(), area.y(), area.width(), area.height(),
            scaled(x), scaled(y), scaled(area.width()), scaled(area.height())
        );
        // The maze images also contain the ghost and Ms. Pac-Man sprites at their initial positions
        overPaintActors(level);
    }

    public void drawFood(GameLevel level) {
        requireNonNull(level);
        if (coloredMapSet == null) {
            Logger.error("Draw food: no map set found");
            return;
        }
        ctx().save();
        ctx().scale(scaling(), scaling());
        Color pelletColor = Color.web(coloredMapSet.mapRegion().colorScheme().pelletColor());
        drawPellets(level, pelletColor);
        drawEnergizers(level, pelletColor);
        ctx().restore();
    }

    public void drawHighlightedLevel(GameLevel level, double mapX, double mapY, int flashingIndex) {
        requireNonNull(level);
        final var game = (TengenMsPacMan_GameModel) theGame();
        final ColoredImageRegion mapImage = coloredMapSet.flashingMapRegions().get(flashingIndex);
        final Sprite region = mapImage.region();
        if (!game.optionsAreInitial()) {
            drawGameOptions(game.mapCategory(), game.difficulty(), game.pacBooster(),
                    level.worldMap().numCols() * HTS, tiles_to_px(2) + HTS);
        }
        ctx().setImageSmoothing(false);
        ctx().drawImage(mapImage.image(),
            region.x(), region.y(), region.width(), region.height(),
            scaled(mapX), scaled(mapY), scaled(region.width()), scaled(region.height())
        );
        overPaintActors(level);

        // draw food to erase eaten food!
        ctx().save();
        ctx().scale(scaling(), scaling());
        Color pelletColor = Color.web(coloredMapSet.mapRegion().colorScheme().pelletColor());
        drawPellets(level, pelletColor);
        drawEnergizers(level, pelletColor);
        ctx().restore();
    }

    private void drawPellets(GameLevel level, Color pelletColor) {
        level.worldMap().tiles().filter(level::isFoodPosition).filter(not(level::isEnergizerPosition)).forEach(tile -> {
            double cx = tile.x() * TS + HTS, cy = tile.y() * TS + HTS;
            ctx().setFill(backgroundColorPy.get());
            ctx().fillRect(cx - 2, cy - 2, 4, 4);
            if (!level.tileContainsEatenFood(tile)) {
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
            if (!level.tileContainsEatenFood(tile) && level.blinking().isOn()) {
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
        requireNonNull(bonus);
        MovingBonus movingBonus = (MovingBonus) bonus;
        ctx().save();
        ctx().setImageSmoothing(false);
        ctx().translate(0, movingBonus.elongationY());
        switch (bonus.state()) {
            case STATE_EDIBLE -> {
                Sprite sprite = theUI().configuration().createBonusSymbolSprite(bonus.symbol());
                drawActorSprite(bonus.actor(), sprite);
            }
            case STATE_EATEN  -> {
                Sprite sprite = theUI().configuration().createBonusValueSprite(bonus.symbol());
                drawActorSprite(bonus.actor(), sprite);
            }
            default -> {}
        }
        ctx().restore();
    }

    public void drawLevelMessage(GameLevel level, Vector2f position, Font font) {
        requireNonNull(level);
        requireNonNull(position);
        requireNonNull(font);
        if (level.message() == GameLevel.MESSAGE_NONE) return;

        float x = position.x(), y = position.y();
        String ans = theUI().configuration().assetNamespace();
        switch (level.message()) {
            case GameLevel.MESSAGE_READY
                -> fillTextAtCenter("READY!", theAssets().color(ans + ".color.ready_message"), font, x, y);
            case GameLevel.MESSAGE_GAME_OVER -> {
                Color color = theAssets().color(ans + ".color.game_over_message");
                if (level.isDemoLevel()) {
                    NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
                    color = Color.web(nesColorScheme.strokeColor());
                }
                fillTextAtCenter("GAME OVER", color, font, x, y);
            }
            case GameLevel.MESSAGE_TEST
                -> fillTextAtCenter("TEST L%02d".formatted(level.number()), nesPaletteColor(0x28), font, x, y);
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
        requireNonNull(mapCategory);
        requireNonNull(difficulty);
        requireNonNull(pacBooster);
        Sprite categorySprite = switch (mapCategory) {
            case BIG     -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_BIG);
            case MINI    -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_MINI);
            case STRANGE -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_STRANGE);
            case ARCADE  -> null; // drawSprite() accepts null sprites!
        };
        Sprite difficultySprite = switch (difficulty) {
            case EASY   -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_EASY);
            case HARD   -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_HARD);
            case CRAZY  -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_CRAZY);
            case NORMAL -> null; // drawSprite() accepts null sprites!
        };
        if (pacBooster != PacBooster.OFF) {
            drawSpriteScaledCenteredAt(spriteSheet.sprite(SpriteID.INFO_BOOSTER), centerX - tiles_to_px(6), y);
        }
        drawSpriteScaledCenteredAt(difficultySprite, centerX, y);
        drawSpriteScaledCenteredAt(categorySprite, centerX + tiles_to_px(4.5), y);
        drawSpriteScaledCenteredAt(spriteSheet.sprite(SpriteID.INFO_FRAME), centerX, y);
    }

    @Override
    public void drawScores(ScoreManager scoreManager, Color color, Font font) {
        requireNonNull(scoreManager);
        requireNonNull(color);
        requireNonNull(font);
        if (scoreManager.isScoreVisible()) {
            if (theClock().tickCount() % 60 < 30) {
                fillText("1UP", color, font, tiles_to_px(4), tiles_to_px(1));
            }
            fillText("HIGH SCORE", color, font, tiles_to_px(11), tiles_to_px(1));
            fillText("%6d".formatted(scoreManager.score().points()), color, font, tiles_to_px(2), tiles_to_px(2));
            fillText("%6d".formatted(scoreManager.highScore().points()), color, font, tiles_to_px(13), tiles_to_px(2));
        }
    }

    public void drawLevelCounterWithLevelNumbers(int levelNumber, LevelCounter levelCounter, Vector2f sizeInPx) {
        requireNonNull(levelCounter);
        requireNonNull(sizeInPx);
        ctx().setImageSmoothing(false);
        float x = sizeInPx.x() - 2 * TS, y = sizeInPx.y() - TS;
        drawLevelNumberBox(levelNumber, 0, y); // left box
        drawLevelNumberBox(levelNumber, x, y); // right box
        x -= 2 * TS;
        for (byte symbol : levelCounter.symbols()) {
            Sprite sprite = theUI().configuration().createBonusSymbolSprite(symbol);
            drawSpriteScaled(sprite, x, y);
            x -= TS * 2;
        }
    }

    @Override
    public void drawLevelCounter(LevelCounter levelCounter, Vector2f sceneSizeInPixels) {
        requireNonNull(levelCounter);
        requireNonNull(sceneSizeInPixels);
        ctx().setImageSmoothing(false);
        float x = sceneSizeInPixels.x() - 4 * TS, y = sceneSizeInPixels.y() - TS;
        for (byte symbol : levelCounter.symbols()) {
            Sprite sprite = theUI().configuration().createBonusSymbolSprite(symbol);
            drawSpriteScaled(sprite, x, y);
            x -= TS * 2;
        }
    }

    public void drawLevelNumberBox(int levelNumber, double x, double y) {
        drawSpriteScaled(spriteSheet.sprite(SpriteID.LEVEL_NUMBER_BOX), x, y);
        int tens = levelNumber / 10, ones = levelNumber % 10;
        if (tens > 0) {
            drawSpriteScaled(digitSprite(tens), x + 2, y + 2);
        }
        drawSpriteScaled(digitSprite(ones), x + 10, y + 2);
    }

    private Sprite digitSprite(int digit) {
        return spriteSheet.sprite(switch (digit) {
            case 1 -> SpriteID.DIGIT_1;
            case 2 -> SpriteID.DIGIT_2;
            case 3 -> SpriteID.DIGIT_3;
            case 4 -> SpriteID.DIGIT_4;
            case 5 -> SpriteID.DIGIT_5;
            case 6 -> SpriteID.DIGIT_6;
            case 7 -> SpriteID.DIGIT_7;
            case 8 -> SpriteID.DIGIT_8;
            case 9 -> SpriteID.DIGIT_9;
            case 0 -> SpriteID.DIGIT_0;
            default -> throw new IllegalArgumentException("Illegal digit value " + digit);
        });
    }

    public void drawBar(Color outlineColor, Color barColor, double width, double y) {
        requireNonNull(outlineColor);
        requireNonNull(barColor);
        ctx().save();
        ctx().scale(scaling(), scaling());
        ctx().setFill(outlineColor);
        ctx().fillRect(0, y, width, TS);
        ctx().setFill(barColor);
        ctx().fillRect(0, y + 1, width, TS - 2);
        ctx().restore();
    }

    public void drawClapperBoard(Clapperboard clapperboard) {
        requireNonNull(clapperboard);
        if (!clapperboard.isVisible()) return;
        clapperboard.sprite().ifPresent(sprite -> {
            double numberX = clapperboard.x() + 8, numberY = clapperboard.y() + 18; // baseline
            ctx().setImageSmoothing(false);
            drawSpriteScaledCenteredAt(sprite, clapperboard.x() + HTS, clapperboard.y() + HTS);
            // over-paint number from sprite sheet
            ctx().save();
            ctx().scale(scaling(), scaling());
            ctx().setFill(backgroundColorPy.get());
            ctx().fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx().restore();

            ctx().setFont(clapperboard.font());
            ctx().setFill(nesPaletteColor(0x20));
            ctx().fillText(String.valueOf(clapperboard.number()), scaled(numberX), scaled(numberY));
            if (clapperboard.isTextVisible()) {
                double textX = clapperboard.x() + sprite.width(), textY = clapperboard.y() + 2;
                ctx().fillText(clapperboard.text(), scaled(textX), scaled(textY));
            }
        });
    }

    public void drawJoypadKeyBinding(JoypadKeyBinding binding) {
        requireNonNull(binding);
        ctx().setFont(Font.font("Sans", scaled(TS)));
        ctx().setStroke(Color.WHITE);
        ctx().strokeText(" [SELECT]=%s   [START]=%s   [BUTTON B]=%s   [BUTTON A]=%s".formatted(
                binding.key(JoypadButton.SELECT),
                binding.key(JoypadButton.START),
                binding.key(JoypadButton.B),
                binding.key(JoypadButton.A)
        ), 0, scaled(TS));
        ctx().strokeText(" [UP]=%s   [DOWN]=%s   [LEFT]=%s   [RIGHT]=%s".formatted(
                binding.key(JoypadButton.UP),
                binding.key(JoypadButton.DOWN),
                binding.key(JoypadButton.LEFT),
                binding.key(JoypadButton.RIGHT)
        ), 0, scaled(2*TS));
    }
}