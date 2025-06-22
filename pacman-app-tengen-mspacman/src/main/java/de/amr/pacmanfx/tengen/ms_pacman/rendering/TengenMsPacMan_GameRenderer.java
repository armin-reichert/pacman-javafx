/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.Sprite;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.GameLevel;
import de.amr.pacmanfx.model.HUD;
import de.amr.pacmanfx.model.LevelCounter;
import de.amr.pacmanfx.model.LivesCounter;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengen.ms_pacman.model.*;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.Stork;
import de.amr.pacmanfx.ui._2d.SpriteGameRenderer;
import de.amr.pacmanfx.ui.input.JoypadKeyBinding;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.*;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_MUNCHING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.NES_SIZE_PX;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_MapRepository.strangeMap15Sprite;
import static de.amr.pacmanfx.tengen.ms_pacman.rendering.TengenMsPacMan_PacAnimationMap.*;
import static de.amr.pacmanfx.ui.PacManGames.*;
import static de.amr.pacmanfx.ui.PacManGames_UI.PY_CANVAS_BG_COLOR;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_GameRenderer implements SpriteGameRenderer {

    public static Color blueShadedColor(long tick) {
        // Blue color, changing from dark blue to brighter blue.
        // Cycles through palette indices 0x01, 0x11, 0x21, 0x31, each 16 ticks.
        int i = (int) (tick % 64) / 16;
        return nesPaletteColor(0x01 + i * 0x10);
    }

    private final ObjectProperty<Color> backgroundColorPy = new SimpleObjectProperty<>(Color.BLACK);
    private final GraphicsContext ctx;
    private final TengenMsPacMan_SpriteSheet spriteSheet;
    private final FloatProperty scalingPy = new SimpleFloatProperty(1);
    private final TengenMsPacMan_MapRepository mapRepository;
    private ColoredMazeConfiguration mapConfiguration;

    public TengenMsPacMan_GameRenderer(TengenMsPacMan_SpriteSheet spriteSheet, TengenMsPacMan_MapRepository mapRepository, Canvas canvas) {
        this.ctx = requireNonNull(canvas).getGraphicsContext2D();
        this.spriteSheet = requireNonNull(spriteSheet);
        this.mapRepository = requireNonNull(mapRepository);
        ctx.setImageSmoothing(false);
    }

    @Override
    public GraphicsContext ctx() {
        return ctx;
    }

    @Override
    public FloatProperty scalingProperty() { return scalingPy; }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return spriteSheet;
    }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColorPy; }

    public Color backgroundColor() { return backgroundColorPy.get(); }

    //TODO check cases where colored map set is not initialized properly
    public void ensureRenderingHintsAreApplied(GameLevel level) {
        if (mapConfiguration == null) {
            applyRenderingHints(level);
        }
    }

    @Override
    public void applyRenderingHints(GameLevel level) {
        int flashCount = level.data().numFlashes();
        mapConfiguration = mapRepository.createMazeConfiguration(level.worldMap(), flashCount);
        Logger.info("Created map configuration with {} flash colors: {}", flashCount, mapConfiguration);
    }

    @Override
    public void drawHUD(HUD hud) {
        requireNonNull(hud);

        if (!hud.isVisible()) return;

        var theGame = (TengenMsPacMan_GameModel) theGame();
        Vector2f sceneSize = optGameLevel().map(GameLevel::worldSizePx).orElse(NES_SIZE_PX);

        if (hud.isScoreVisible()) {
            drawScores(theGame, nesPaletteColor(0x20), theAssets().arcadeFont(8));
        }

        if (hud.isLivesCounterVisible()) {
            drawLivesCounter(hud.livesCounter(), theGame.lifeCount(), 2 * TS, sceneSize.y() - TS);
        }

        if (hud.isLevelCounterVisible()) {
            if (hud instanceof TengenMsPacMan_HUD tengenHUD) {
                TengenMsPacMan_LevelCounter levelCounter = tengenHUD.levelCounter();
                float x = sceneSize.x() - 2 * TS, y = sceneSize.y() - TS;
                drawLevelCounter(levelCounter.displayedLevelNumber(), levelCounter, x, y);
            }
        }
    }

    public void drawScores(TengenMsPacMan_GameModel game, Color color, Font font) {
        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.setFill(color);
        ctx.setFont(font);
        if (theClock().tickCount() % 60 < 30) {
            ctx.fillText("1UP", tiles_to_px(4), tiles_to_px(1));
        }
        ctx.fillText("HIGH SCORE", tiles_to_px(11), tiles_to_px(1));
        ctx.fillText("%6d".formatted(game.score().points()), tiles_to_px(2), tiles_to_px(2));
        ctx.fillText("%6d".formatted(game.highScore().points()), tiles_to_px(13), tiles_to_px(2));
        ctx.restore();
    }

    public void drawLivesCounter(LivesCounter livesCounter, int lifeCount, float x, float y) {
        Sprite sprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);
        for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
            drawSpriteScaled(sprite, x + i * 2 * TS, y);
        }
        if (lifeCount > livesCounter.maxLivesDisplayed()) {
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            fillTextAtScaledPosition("(%d)".formatted(lifeCount), nesPaletteColor(0x28), font, x + TS * 10, y + TS);
        }
    }

    public void drawLevelCounter(int levelNumber, LevelCounter levelCounter, float x, float y) {
        if (levelNumber != 0) {
            drawLevelNumberBox(levelNumber, 0, y); // left box
            drawLevelNumberBox(levelNumber, x, y); // right box
        }
        Sprite[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
        x -= 2 * TS;
        // symbols are drawn from right to left!
        for (byte symbol : levelCounter.symbols()) {
            drawSpriteScaled(symbolSprites[symbol], x, y);
            x -= TS * 2;
        }
    }

    // this is also used by the 3D scene
    public void drawLevelNumberBox(int number, double x, double y) {
        drawSpriteScaled(spriteSheet.sprite(SpriteID.LEVEL_NUMBER_BOX), x, y);
        int tens = number / 10, ones = number % 10;
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

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (actor.isVisible()) {
            switch (actor) {
                case MovingBonus movingBonus -> drawMovingBonus(movingBonus);
                case Pac pac -> drawAnyKindOfPac(pac);
                case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
                case Stork stork -> drawStork(stork);
                default -> SpriteGameRenderer.super.drawActor(actor);
            }
        }
    }

    public void drawMovingBonus(MovingBonus bonus) {
        if (bonus.state() == Bonus.STATE_INACTIVE) return;
        ctx.save();
        ctx.translate(0, bonus.elongationY());
        switch (bonus.state()) {
            case Bonus.STATE_EDIBLE -> {
                Sprite sprite = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS)[bonus.symbol()];
                drawActorSpriteCentered(bonus, sprite);
            }
            case Bonus.STATE_EATEN  -> {
                Sprite sprite = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES)[bonus.symbol()];
                drawActorSpriteCentered(bonus, sprite);
            }
        }
        ctx.restore();
    }

    private void drawAnyKindOfPac(Pac pac) {
        if (!pac.isVisible()) {
            return;
        }
        pac.animationMap().map(SpriteAnimationMap.class::cast).ifPresent(spriteAnimations -> {
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
            }
        });
    }

    private void drawMovingActor(MovingActor actor, Direction dir, Sprite spriteLookingLeft) {
        Vector2f center = actor.center().scaled(scaling());
        ctx.save();
        ctx.translate(center.x(), center.y());
        switch (dir) {
            case UP    -> ctx.rotate(90);
            case LEFT  -> {}
            case RIGHT -> ctx.scale(-1, 1);
            case DOWN  -> { ctx.scale(-1, 1); ctx.rotate(-90); }
        }
        drawSpriteScaledCenteredAt(spriteLookingLeft, 0, 0);
        ctx.restore();
    }

    public void drawVerticalSceneBorders() {
        double width = ctx.getCanvas().getWidth(), height = ctx.getCanvas().getHeight();
        ctx.setLineWidth(0.5);
        ctx.setStroke(Color.grayRgb(50));
        ctx.strokeLine(0.5, 0, 0.5, height);
        ctx.strokeLine(width - 0.5, 0, width - 0.5, height);
    }

    @Override
    public void drawLevel(GameLevel level, Color backgroundColor, boolean mazeHighlighted, boolean energizerHighlighted) {
        requireNonNull(level);
        final var theGame = (TengenMsPacMan_GameModel) theGame();
        final int mapNumber = level.worldMap().getConfigValue("mapNumber");

        ctx.setImageSmoothing(false);

        if (!theGame.optionsAreInitial()) {
            drawGameOptions(theGame.mapCategory(), theGame.difficulty(), theGame.pacBooster(),
                level.worldMap().numCols() * HTS, tiles_to_px(2) + HTS);
        }

        Sprite mapSprite = theGame.mapCategory() == MapCategory.STRANGE && mapNumber == 15
            ? strangeMap15Sprite(theClock().tickCount()) // Strange map #15: psychedelic animation
            : mapConfiguration.mazeSprite().sprite();
        Image mapImage = mapConfiguration.mazeSprite().image();
        int y = GameLevel.EMPTY_ROWS_OVER_MAZE * TS;
        ctx.drawImage(mapImage,
            mapSprite.x(), mapSprite.y(), mapSprite.width(), mapSprite.height(),
            0, scaled(y), scaled(mapSprite.width()), scaled(mapSprite.height())
        );
        // The maze images show the ghosts and Ms. Pac-Man at their initial locations: must over-paint these
        overPaintActorSprites(level);

        drawFood(level);
    }

    public void drawFood(GameLevel level) {
        requireNonNull(level);
        if (mapConfiguration == null) {
            Logger.error("Draw food: no map set found");
            return;
        }
        ctx.save();
        ctx.scale(scaling(), scaling());
        Color pelletColor = Color.web(mapConfiguration.mazeSprite().colorScheme().pelletColor());
        drawPellets(level, pelletColor);
        drawEnergizers(level, pelletColor);
        ctx.restore();
    }

    public void drawHighlightedLevel(GameLevel level, int flashingIndex) {
        requireNonNull(level);
        double mapTop = GameLevel.EMPTY_ROWS_OVER_MAZE * TS;
        final var game = (TengenMsPacMan_GameModel) theGame();
        final ColorSchemedSprite mapImage = mapConfiguration.flashingMazeSprites().get(flashingIndex);
        final Sprite region = mapImage.sprite();
        if (!game.optionsAreInitial()) {
            drawGameOptions(game.mapCategory(), game.difficulty(), game.pacBooster(),
                    level.worldMap().numCols() * HTS, tiles_to_px(2) + HTS);
        }
        ctx.setImageSmoothing(false);
        ctx.drawImage(mapImage.image(),
            region.x(), region.y(), region.width(), region.height(),
            0, scaled(mapTop), scaled(region.width()), scaled(region.height())
        );
        overPaintActorSprites(level);

        // draw food to erase eaten food!
        ctx.save();
        ctx.scale(scaling(), scaling());
        Color pelletColor = Color.web(mapConfiguration.mazeSprite().colorScheme().pelletColor());
        drawPellets(level, pelletColor);
        drawEnergizers(level, pelletColor);
        ctx.restore();
    }

    private void drawPellets(GameLevel level, Color pelletColor) {
        level.worldMap().tiles().filter(level::isFoodPosition).filter(not(level::isEnergizerPosition)).forEach(tile -> {
            ctx.setFill(backgroundColor());
            fillSquareAtTileCenter(tile, 4);
            if (!level.tileContainsEatenFood(tile)) {
                // draw pellet using the right color
                ctx.setFill(pelletColor);
                fillSquareAtTileCenter(tile, 2);
            }
        });
    }

    private void drawEnergizers(GameLevel level, Color pelletColor) {
        double size = TS;
        double offset = 0.5 * HTS;
        level.worldMap().tiles().filter(level::isEnergizerPosition).forEach(tile -> {
            ctx.setFill(backgroundColor());
            fillSquareAtTileCenter(tile, TS + 2);
            if (!level.tileContainsEatenFood(tile) && level.blinking().isOn()) {
                ctx.setFill(pelletColor);
                // draw pixelated "circle"
                double cx = tile.x() * TS, cy = tile.y() * TS;
                ctx.fillRect(cx + offset, cy, HTS, size);
                ctx.fillRect(cx, cy + offset, size, HTS);
                ctx.fillRect(cx + 1, cy + 1, size - 2, size - 2);
            }
        });
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
                -> fillTextAtScaledCenter("READY!", theAssets().color(ans + ".color.ready_message"), font, x, y);
            case GameLevel.MESSAGE_GAME_OVER -> {
                Color color = theAssets().color(ans + ".color.game_over_message");
                if (level.isDemoLevel()) {
                    NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
                    color = Color.web(nesColorScheme.strokeColor());
                }
                fillTextAtScaledCenter("GAME OVER", color, font, x, y);
            }
            case GameLevel.MESSAGE_TEST
                -> fillTextAtScaledCenter("TEST L%02d".formatted(level.number()), nesPaletteColor(0x28), font, x, y);
        }
    }

    private void overPaintActorSprites(GameLevel level) {
        float margin = scaled(1), halfMargin = 0.5f * margin;
        float s = scaled(TS);

        // Over-paint area at house bottom where the ghost sprites are shown in map
        var inHouseArea = new Rectangle2D(
            halfMargin + s * (level.houseMinTile().x() + 1),
            halfMargin + s * (level.houseMinTile().y() + 2),
            s * (level.houseSizeInTiles().x() - 2) - margin,
            s * 2 - margin
        );

        ctx.setFill(backgroundColor());
        ctx.fillRect(inHouseArea.getMinX(), inHouseArea.getMinY(), inHouseArea.getWidth(), inHouseArea.getHeight());

        // Now the actor sprites outside the house. Be careful not to over-paint nearby obstacle edges!
        Vector2i pacTile = level.worldMap().getTerrainTileProperty("pos_pac", Vector2i.of(14, 26));
        overPaintActorSprite(pacTile, margin);

        Vector2i redGhostTile = level.worldMap().getTerrainTileProperty("pos_ghost_1_red", Vector2i.of(13, 14));
        overPaintActorSprite(redGhostTile, margin);
    }

    private void overPaintActorSprite(Vector2i tile, float margin) {
        float halfMargin = 0.5f * margin;
        float overPaintSize = scaled(2 * TS) - margin;
        ctx.fillRect(
            halfMargin + scaled(tile.x() * TS),
            halfMargin + scaled(tile.y() * TS - HTS),
            overPaintSize, overPaintSize);
    }

    public void drawGameOptions(MapCategory category, Difficulty difficulty, PacBooster booster, double centerX, double y) {
        drawSpriteScaledCenteredAt(spriteSheet.sprite(SpriteID.INFO_FRAME), centerX, y);
        Sprite categorySprite = switch (requireNonNull(category)) {
            case BIG     -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_BIG);
            case MINI    -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_MINI);
            case STRANGE -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_STRANGE);
            case ARCADE  -> Sprite.ZERO;
        };
        drawSpriteScaledCenteredAt(categorySprite, centerX + tiles_to_px(4.5), y);
        Sprite difficultySprite = switch (requireNonNull(difficulty)) {
            case EASY   -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_EASY);
            case HARD   -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_HARD);
            case CRAZY  -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_CRAZY);
            case NORMAL -> Sprite.ZERO;
        };
        drawSpriteScaledCenteredAt(difficultySprite, centerX, y);
        if (requireNonNull(booster) != PacBooster.OFF) {
            drawSpriteScaledCenteredAt(spriteSheet.sprite(SpriteID.INFO_BOOSTER), centerX - tiles_to_px(6), y);
        }
    }

    public void drawBar(Color outlineColor, Color barColor, double width, double y) {
        requireNonNull(outlineColor);
        requireNonNull(barColor);
        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.setFill(outlineColor);
        ctx.fillRect(0, y, width, TS);
        ctx.setFill(barColor);
        ctx.fillRect(0, y + 1, width, TS - 2);
        ctx.restore();
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        requireNonNull(clapperboard);
        if (!clapperboard.isVisible()) return;
        clapperboard.sprite().ifPresent(sprite -> {
            double numberX = clapperboard.x() + 8, numberY = clapperboard.y() + 18; // baseline
            drawSpriteScaledCenteredAt(sprite, clapperboard.x() + HTS, clapperboard.y() + HTS);
            // over-paint number from sprite sheet
            ctx.save();
            ctx.scale(scaling(), scaling());
            ctx.setFill(backgroundColor());
            ctx.fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx.restore();

            ctx.setFont(clapperboard.font());
            ctx.setFill(nesPaletteColor(0x20));
            ctx.fillText(String.valueOf(clapperboard.number()), scaled(numberX), scaled(numberY));
            if (clapperboard.isTextVisible()) {
                double textX = clapperboard.x() + sprite.width(), textY = clapperboard.y() + 2;
                ctx.fillText(clapperboard.text(), scaled(textX), scaled(textY));
            }
        });
    }

    //TODO maybe just extend sprite sheet to include stork without bag?
    private void drawStork(Stork stork) {
        SpriteGameRenderer.super.drawAnimatedActor(stork);
        if (stork.isBagReleasedFromBeak()) { // over-paint bag still hanging at beak
            ctx.setFill(PY_CANVAS_BG_COLOR.get());
            //TODO: clarify coordinate values
            ctx.fillRect(scaled(stork.x() - 13), scaled(stork.y() + 3), scaled(8), scaled(10));
        }
    }

    public void drawJoypadKeyBinding(JoypadKeyBinding binding) {
        requireNonNull(binding);
        ctx.setFont(Font.font("Sans", scaled(TS)));
        ctx.setStroke(Color.WHITE);
        ctx.strokeText(" [SELECT]=%s   [START]=%s   [BUTTON B]=%s   [BUTTON A]=%s".formatted(
                binding.key(JoypadButton.SELECT),
                binding.key(JoypadButton.START),
                binding.key(JoypadButton.B),
                binding.key(JoypadButton.A)
        ), 0, scaled(TS));
        ctx.strokeText(" [UP]=%s   [DOWN]=%s   [LEFT]=%s   [RIGHT]=%s".formatted(
                binding.key(JoypadButton.UP),
                binding.key(JoypadButton.DOWN),
                binding.key(JoypadButton.LEFT),
                binding.key(JoypadButton.RIGHT)
        ), 0, scaled(2*TS));
    }
}