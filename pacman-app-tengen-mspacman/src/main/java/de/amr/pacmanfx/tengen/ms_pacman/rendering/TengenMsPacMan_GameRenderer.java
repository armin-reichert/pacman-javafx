/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.lib.Vector2i;
import de.amr.pacmanfx.lib.nes.JoypadButton;
import de.amr.pacmanfx.lib.nes.NES_ColorScheme;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengen.ms_pacman.model.*;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.Stork;
import de.amr.pacmanfx.ui.GameUI_Config;
import de.amr.pacmanfx.ui.PacManGames_Assets;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.ui.input.JoypadKeyBinding;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.lib.UsefulFunctions.tiles_to_px;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static de.amr.pacmanfx.tengen.ms_pacman.model.TengenMsPacMan_MapRepository.strangeMap15Sprite;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_GameRenderer extends GameRenderer {

    public static Color blueShadedColor(long tick) {
        // Blue color, changing from dark blue to brighter blue.
        // Cycles through palette indices 0x01, 0x11, 0x21, 0x31, each 16 ticks.
        int i = (int) (tick % 64) / 16;
        return nesPaletteColor(0x01 + i * 0x10);
    }

    private final ObjectProperty<Color> backgroundColorProperty = new SimpleObjectProperty<>(Color.BLACK);

    private TengenMsPacMan_SpriteSheet spriteSheet;
    private TengenMsPacMan_MapRepository mapRepository;
    private ColoredMazeSpriteSet mazeSpriteSet;

    public TengenMsPacMan_GameRenderer(
        PacManGames_Assets assets,
        TengenMsPacMan_SpriteSheet spriteSheet,
        TengenMsPacMan_MapRepository mapRepository,
        Canvas canvas)
    {
        super(assets);
        this.ctx = requireNonNull(canvas).getGraphicsContext2D();
        this.ctx.setImageSmoothing(false);
        this.spriteSheet = requireNonNull(spriteSheet);
        this.mapRepository = requireNonNull(mapRepository);
    }

    @Override
    public void dispose() {
        spriteSheet = null;
        mapRepository = null;
        backgroundColorProperty.unbind();
        scalingProperty.unbind();
    }

    @Override
    public Optional<SpriteSheet<?>> optSpriteSheet() {
        return Optional.of(spriteSheet);
    }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColorProperty; }

    public Color backgroundColor() { return backgroundColorProperty.get(); }

    //TODO check cases where colored map set is not initialized properly
    public void ensureRenderingHintsAreApplied(GameLevel level) {
        if (mazeSpriteSet == null) {
            applyRenderingHints(level);
        }
    }

    public ColoredMazeSpriteSet mazeConfig() {
        return mazeSpriteSet;
    }

    @Override
    public void applyRenderingHints(GameLevel level) {
        mazeSpriteSet = mapRepository.createMazeSpriteSet(level.worldMap(), level.data().numFlashes());
        Logger.info("Created maze sprite set ({} flash colors: {})", level.data().numFlashes(),
            mazeSpriteSet.colorSchemedMazeSprite());
    }

    @Override
    public void drawHUD(GameContext gameContext, HUD hud, Vector2f sceneSize, long tick) {
        requireNonNull(hud);

        if (!hud.isVisible()) return;

        var theGame = (TengenMsPacMan_GameModel) gameContext.theGame();

        if (hud.isScoreVisible()) {
            drawScores(theGame, tick, nesPaletteColor(0x20), assets().arcadeFont(8));
        }

        if (hud.isLivesCounterVisible()) {
            drawLivesCounter(hud.theLivesCounter(), theGame.lifeCount(), 2 * TS, sceneSize.y() - TS);
        }

        if (hud.isLevelCounterVisible()) {
            if (hud instanceof TengenMsPacMan_HUD tengenHUD) {
                TengenMsPacMan_LevelCounter levelCounter = tengenHUD.theLevelCounter();
                float x = sceneSize.x() - 2 * TS, y = sceneSize.y() - TS;
                drawLevelCounter(levelCounter.displayedLevelNumber(), levelCounter, x, y);
            }
        }
    }

    public void drawScores(TengenMsPacMan_GameModel game, long tick, Color color, Font font) {
        ctx.save();
        ctx.scale(scaling(), scaling());
        ctx.setFill(color);
        ctx.setFont(font);
        if (tick % 60 < 30) {
            ctx.fillText("1UP", tiles_to_px(4), tiles_to_px(1));
        }
        ctx.fillText("HIGH SCORE", tiles_to_px(11), tiles_to_px(1));
        ctx.fillText("%6d".formatted(game.score().points()), tiles_to_px(2), tiles_to_px(2));
        ctx.fillText("%6d".formatted(game.highScore().points()), tiles_to_px(13), tiles_to_px(2));
        ctx.restore();
    }

    public void drawLivesCounter(LivesCounter livesCounter, int lifeCount, float x, float y) {
        RectShort sprite = spriteSheet.sprite(SpriteID.LIVES_COUNTER_SYMBOL);
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
        RectShort[] symbolSprites = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS);
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

    private RectShort digitSprite(int digit) {
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
                case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
                case Bonus bonus -> drawMovingBonus(bonus);
                case Pac pac                   -> drawAnyKindOfPac(pac);
                case Stork stork -> {
                    drawAnimatedActor(stork);
                    if (stork.isBagReleasedFromBeak()) {
                        hideStorkBag(stork);
                    }
                }
                default                        -> super.drawActor(actor);
            }
        }
    }

    public void drawMovingBonus(Bonus bonus) {
        if (bonus.state() == BonusState.INACTIVE) return;
        ctx.save();
        ctx.translate(0, bonus.jumpHeight());
        switch (bonus.state()) {
            case EDIBLE -> {
                RectShort sprite = spriteSheet.spriteSeq(SpriteID.BONUS_SYMBOLS)[bonus.symbol()];
                drawActorSpriteCentered(bonus, sprite);
            }
            case EATEN  -> {
                RectShort sprite = spriteSheet.spriteSeq(SpriteID.BONUS_VALUES)[bonus.symbol()];
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
            if (spriteAnimations.selectedAnimationID().equals(ANIM_PAC_DYING)) {
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
                drawMovingActor(pac, dir, (RectShort) animation.currentSprite());
            } else {
                drawMovingActor(pac, pac.moveDir(), (RectShort) animation.currentSprite());
            }
        });
    }

    private void drawMovingActor(MovingActor actor, Direction dir, RectShort spriteLookingLeft) {
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

    //TODO change sprite sheet to include stork without bag in beak?
    private void hideStorkBag(Stork stork) {
        ctx.setFill(backgroundColor());
        ctx.fillRect(scaled(stork.x() - 13), scaled(stork.y() + 3), scaled(8), scaled(10));
    }

    public void drawVerticalSceneBorders() {
        double width = ctx.getCanvas().getWidth(), height = ctx.getCanvas().getHeight();
        ctx.setLineWidth(0.5);
        ctx.setStroke(Color.grayRgb(50));
        ctx.strokeLine(0.5, 0, 0.5, height);
        ctx.strokeLine(width - 0.5, 0, width - 0.5, height);
    }

    @Override
    public void drawLevel(
        GameContext gameContext,
        GameLevel level,
        Color backgroundColor,
        boolean mazeHighlighted,
        boolean energizerHighlighted,
        long tick)
    {
        var tengenGame = (TengenMsPacMan_GameModel) gameContext.theGame();
        int mapNumber = level.worldMap().getConfigValue("mapNumber");
        RectShort mazeSprite = tengenGame.mapCategory() == MapCategory.STRANGE && mapNumber == 15
            ? strangeMap15Sprite(tick) // Strange map #15: psychedelic animation
            : mazeSpriteSet.colorSchemedMazeSprite().sprite();
        drawLevelWithMaze(gameContext, level, mazeSpriteSet.colorSchemedMazeSprite().image(), mazeSprite);
    }

    public void drawLevelWithMaze(GameContext gameContext, GameLevel level, Image mazeImage, RectShort mazeSprite) {
        var tengenGame = (TengenMsPacMan_GameModel) gameContext.theGame();
        ctx.setImageSmoothing(false);
        if (!tengenGame.optionsAreInitial()) {
            drawGameOptions(tengenGame.mapCategory(), tengenGame.difficulty(), tengenGame.pacBooster(),
                level.worldMap().numCols() * HTS, tiles_to_px(2) + HTS);
        }
        int x = 0, y = GameLevel.EMPTY_ROWS_OVER_MAZE * TS;
        ctx.drawImage(mazeImage,
            mazeSprite.x(), mazeSprite.y(), mazeSprite.width(), mazeSprite.height(),
            scaled(x), scaled(y), scaled(mazeSprite.width()), scaled(mazeSprite.height())
        );
        overPaintActorSprites(level);
        drawFood(level);
    }

    private void drawFood(GameLevel level) {
        requireNonNull(level);
        ctx.save();
        ctx.scale(scaling(), scaling());
        Color pelletColor = Color.web(mazeSpriteSet.colorSchemedMazeSprite().colorScheme().pelletColorRGB());
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

    public void drawLevelMessage(GameUI_Config config, GameLevel level, Vector2f position, Font font) {
        requireNonNull(level);
        requireNonNull(position);
        requireNonNull(font);
        if (level.messageType() == GameLevel.MESSAGE_NONE) return;

        float x = position.x(), y = position.y() + TS;
        String ans = config.assetNamespace();
        switch (level.messageType()) {
            case GameLevel.MESSAGE_READY
                -> fillTextAtScaledCenter("READY!", assets.color(ans + ".color.ready_message"), font, x, y);
            case GameLevel.MESSAGE_GAME_OVER -> {
                Color color = assets.color(ans + ".color.game_over_message");
                if (level.isDemoLevel()) {
                    NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
                    color = Color.web(nesColorScheme.strokeColorRGB());
                }
                fillTextAtScaledCenter("GAME OVER", color, font, x, y);
            }
            case GameLevel.MESSAGE_TEST
                -> fillTextAtScaledCenter("TEST L%02d".formatted(level.number()), nesPaletteColor(0x28), font, x, y);
        }
    }

    private void overPaintActorSprites(GameLevel level) {
        House house = level.house().orElse(null);
        if (house == null) {
            Logger.error("No house exists in game level!");
            return;
        }

        float margin = scaled(1), halfMargin = 0.5f * margin;
        float s = scaled(TS);

        // Over-paint area at house bottom where the ghost sprites are shown in map
        var inHouseArea = new Rectangle2D(
            halfMargin + s * (house.minTile().x() + 1),
            halfMargin + s * (house.minTile().y() + 2),
            s * (house.sizeInTiles().x() - 2) - margin,
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
        RectShort categorySprite = switch (requireNonNull(category)) {
            case BIG     -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_BIG);
            case MINI    -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_MINI);
            case STRANGE -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_STRANGE);
            case ARCADE  -> RectShort.ZERO;
        };
        drawSpriteScaledCenteredAt(categorySprite, centerX + tiles_to_px(4.5), y);
        RectShort difficultySprite = switch (requireNonNull(difficulty)) {
            case EASY   -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_EASY);
            case HARD   -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_HARD);
            case CRAZY  -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_CRAZY);
            case NORMAL -> RectShort.ZERO;
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

    public void drawJoypadKeyBinding(JoypadKeyBinding binding) {
        ctx.save();
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
        ctx.restore();
    }
}