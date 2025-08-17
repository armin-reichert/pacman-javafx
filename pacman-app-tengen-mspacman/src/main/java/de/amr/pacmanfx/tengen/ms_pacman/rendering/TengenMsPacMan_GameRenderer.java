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
import de.amr.pacmanfx.lib.tilemap.WorldMap;
import de.amr.pacmanfx.model.*;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.model.*;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.Stork;
import de.amr.pacmanfx.ui._2d.GameRenderer;
import de.amr.pacmanfx.ui.input.JoypadKeyBinding;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.tinylog.Logger;

import static de.amr.pacmanfx.Globals.HTS;
import static de.amr.pacmanfx.Globals.TS;
import static de.amr.pacmanfx.model.actors.CommonAnimationID.ANIM_PAC_DYING;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesPaletteColor;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public class TengenMsPacMan_GameRenderer extends GameRenderer {

    private final ObjectProperty<Color> backgroundColor = new SimpleObjectProperty<>(Color.BLACK);
    private final TengenMsPacMan_UIConfig uiConfig;

    public TengenMsPacMan_GameRenderer(TengenMsPacMan_UIConfig uiConfig, Canvas canvas) {
        this.uiConfig = requireNonNull(uiConfig);
        setCanvas(canvas);
        ctx().setImageSmoothing(false);
    }

    public ObjectProperty<Color> backgroundColorProperty() { return backgroundColor; }

    public Color backgroundColor() { return backgroundColor.get(); }

    @Override
    public void applyLevelSettings(GameLevel gameLevel) {
        WorldMap worldMap = gameLevel.worldMap();
        int numFlashes = gameLevel.data().numFlashes();
        if (!worldMap.hasConfigValue(TengenMsPacMan_UIConfig.RECOLORED_MAZE_PROPERTY)) {
            ColoredMazeSpriteSet recoloredMaze = uiConfig.createRecoloredMaze(worldMap, numFlashes);
            worldMap.setConfigValue(TengenMsPacMan_UIConfig.RECOLORED_MAZE_PROPERTY, recoloredMaze);
            Logger.info("Created recolored maze for game level #{} ({} flash colors: {})",
                gameLevel.number(), numFlashes, recoloredMaze.mazeSprite());
        }
    }

    @Override
    public void drawHUD(GameContext gameContext, HUDData data, Vector2f sceneSize, long tick) {
        requireNonNull(data);

        if (!data.isVisible()) return;

        Font font = uiConfig.theUI().assets().arcadeFont(TS);
        TengenMsPacMan_GameModel game = gameContext.game();
        if (data.isScoreVisible()) {
            drawScores(game, tick, nesPaletteColor(0x20), font);
        }
        if (data.isLivesCounterVisible()) {
            drawLivesCounter(data.theLivesCounter(), game.lifeCount(), TS(2), sceneSize.y() - TS);
        }
        if (data.isLevelCounterVisible()) {
            var hudData = (TengenMsPacMan_HUDData) data;
            TengenMsPacMan_LevelCounter levelCounter = hudData.theLevelCounter();
            float x = sceneSize.x() - TS(2), y = sceneSize.y() - TS;
            drawLevelCounter(levelCounter.displayedLevelNumber(), levelCounter, x, y);
        }
    }

    public void drawScores(TengenMsPacMan_GameModel game, long tick, Color color, Font font) {
        ctx().save();
        ctx().scale(scaling(), scaling());
        ctx().setFill(color);
        ctx().setFont(font);
        if (tick % 60 < 30) {
            ctx().fillText("1UP", TS(4), TS(1));
        }
        ctx().fillText("HIGH SCORE", TS(11), TS(1));
        ctx().fillText("%6d".formatted(game.scoreManager().score().points()), TS(2), TS(2));
        ctx().fillText("%6d".formatted(game.scoreManager().highScore().points()), TS(13), TS(2));
        ctx().restore();
    }

    public void drawLivesCounter(LivesCounter livesCounter, int lifeCount, float x, float y) {
        RectShort sprite = uiConfig.spriteSheet().sprite(SpriteID.LIVES_COUNTER_SYMBOL);
        for (int i = 0; i < livesCounter.visibleLifeCount(); ++i) {
            drawSprite(uiConfig.spriteSheet().sourceImage(), sprite, x + TS(i * 2), y, true);
        }
        if (lifeCount > livesCounter.maxLivesDisplayed()) {
            Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            fillText("(%d)".formatted(lifeCount), nesPaletteColor(0x28), font, x + TS(10), y + TS);
        }
    }

    public void drawLevelCounter(int levelNumber, LevelCounter levelCounter, float x, float y) {
        if (levelNumber != 0) {
            drawLevelNumberBox(levelNumber, 0, y); // left box
            drawLevelNumberBox(levelNumber, x, y); // right box
        }
        RectShort[] symbolSprites = uiConfig.spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
        x -= TS(2);
        // symbols are drawn from right to left!
        for (byte symbol : levelCounter.symbols()) {
            drawSprite(uiConfig.spriteSheet().sourceImage(), symbolSprites[symbol], x, y, true);
            x -= TS(2);
        }
    }

    // this is also used by the 3D scene
    public void drawLevelNumberBox(int number, double x, double y) {
        TengenMsPacMan_SpriteSheet spriteSheet = uiConfig.spriteSheet();
        drawSprite(spriteSheet.sourceImage(), spriteSheet.sprite(SpriteID.LEVEL_NUMBER_BOX), x, y, true);
        int tens = number / 10, ones = number % 10;
        if (tens > 0) {
            drawSprite(uiConfig.spriteSheet().sourceImage(), spriteSheet.digitSprite(tens), x + 2, y + 2, true);
        }
        drawSprite(uiConfig.spriteSheet().sourceImage(), spriteSheet.digitSprite(ones), x + 10, y + 2, true);
    }

    @Override
    public void drawActor(Actor actor, Image spriteSheetImage) {
        requireNonNull(actor);
        if (actor.isVisible()) {
            switch (actor) {
                case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
                case Bonus bonus -> drawMovingBonus(bonus);
                case Pac pac -> drawAnyKindOfPac(pac);
                case Stork stork -> {
                    super.drawActor(stork, spriteSheetImage);
                    if (stork.isBagReleasedFromBeak()) {
                        hideStorkBag(stork);
                    }
                }
                default -> super.drawActor(actor, spriteSheetImage);
            }
        }
    }

    public void drawMovingBonus(Bonus bonus) {
        if (bonus.state() == BonusState.INACTIVE) return;
        ctx().save();
        ctx().translate(0, bonus.jumpHeight());
        switch (bonus.state()) {
            case EDIBLE -> {
                RectShort sprite = uiConfig.spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS)[bonus.symbol()];
                drawSpriteCentered(bonus.center(), uiConfig.spriteSheet().sourceImage(), sprite);
            }
            case EATEN  -> {
                RectShort sprite = uiConfig.spriteSheet().spriteSequence(SpriteID.BONUS_VALUES)[bonus.symbol()];
                drawSpriteCentered(bonus.center(), uiConfig.spriteSheet().sourceImage(), sprite);
            }
        }
        ctx().restore();
    }

    private void drawAnyKindOfPac(Pac pac) {
        pac.animations().map(SpriteAnimationManager.class::cast).ifPresent(spriteAnimations -> {
            SpriteAnimation spriteAnimation = spriteAnimations.currentAnimation();
            if (spriteAnimation == null) {
                Logger.error("No sprite animation found for {}", pac);
                return;
            }
            if (ANIM_PAC_DYING.equals(spriteAnimations.selectedID())) {
                drawPacDyingAnimation(pac, spriteAnimation);
            } else {
                drawActorSprite(pac, pac.moveDir(), spriteAnimation.currentSprite());
            }
        });
    }

    // Simulates dying animation by providing the right direction for each animation frame
    private void drawPacDyingAnimation(Pac pac, SpriteAnimation animation) {
        Direction dir = Direction.DOWN;
        if (animation.frameIndex() < 11) {
            dir = switch (animation.frameIndex() % 4) {
                case 1 -> Direction.LEFT;
                case 2 -> Direction.UP;
                case 3 -> Direction.RIGHT;
                default -> Direction.DOWN; // start with DOWN
            };
        }
        drawActorSprite(pac, dir, animation.currentSprite());
    }

    // There are only left-pointing Ms. Pac-Man sprites in the sprite sheet, so we rotate and mirror in the renderer
    private void drawActorSprite(MovingActor actor, Direction dir, RectShort sprite) {
        Vector2f center = actor.center().scaled(scaling());
        ctx().save();
        ctx().translate(center.x(), center.y());
        switch (dir) {
            case LEFT  -> {}
            case UP    -> ctx().rotate(90);
            case RIGHT -> ctx().scale(-1, 1);
            case DOWN  -> { ctx().scale(-1, 1); ctx().rotate(-90); }
        }
        drawSpriteCentered(0, 0, uiConfig.spriteSheet().sourceImage(), sprite);
        ctx().restore();
    }

    // Sprite sheet has no stork without bag under its beak so we over-paint the bag
    private void hideStorkBag(Stork stork) {
        ctx().setFill(backgroundColor());
        ctx().fillRect(scaled(stork.x() - 13), scaled(stork.y() + 3), scaled(8), scaled(10));
    }

    @Override
    public void drawLevel(
        GameContext gameContext,
        Color backgroundColor,
        boolean mazeHighlighted,
        boolean energizerHighlighted,
        long tick)
    {
        TengenMsPacMan_GameModel game = gameContext.game();
        GameLevel gameLevel = gameContext.gameLevel();
        int mapNumber = gameLevel.worldMap().getConfigValue("mapNumber");
        applyLevelSettings(gameLevel);
        ColoredMazeSpriteSet recoloredMaze =  gameLevel.worldMap().getConfigValue(TengenMsPacMan_UIConfig.RECOLORED_MAZE_PROPERTY);
        if (game.mapCategory() == MapCategory.STRANGE && mapNumber == 15) {
            int spriteIndex = mazeAnimationSpriteIndex(tick);
            drawLevelWithMaze(gameContext, gameLevel,
                recoloredMaze.mazeSprite().image(),
                uiConfig.nonArcadeMapsSpriteSheet()
                        .spriteSequence(NonArcadeMapsSpriteSheet.MazeID.MAZE32_ANIMATED)[spriteIndex]);
        } else {
            drawLevelWithMaze(gameContext, gameLevel, recoloredMaze.mazeSprite().image(), recoloredMaze.mazeSprite().sprite());
        }
    }

    /*
       Strange map #15 (maze #32): psychedelic animation:
       Frame pattern: (00000000 11111111 22222222 11111111)+, numFrames = 4, frameDuration = 8
     */
    private int mazeAnimationSpriteIndex(long tick) {
        long block = (tick % 32) / 8;
        return (int) (block < 3 ? block : 1);
    }

    public void drawLevelWithMaze(GameContext gameContext, GameLevel level, Image mazeImage, RectShort mazeSprite) {
        var tengenGame = (TengenMsPacMan_GameModel) gameContext.game();
        ctx().setImageSmoothing(false);
        if (!tengenGame.optionsAreInitial()) {
            drawGameOptions(tengenGame.mapCategory(), tengenGame.difficulty(), tengenGame.pacBooster(),
                level.worldMap().numCols() * HTS, TS(2) + HTS);
        }
        int x = 0, y = GameLevel.EMPTY_ROWS_OVER_MAZE * TS;
        ctx().drawImage(mazeImage,
            mazeSprite.x(), mazeSprite.y(), mazeSprite.width(), mazeSprite.height(),
            scaled(x), scaled(y), scaled(mazeSprite.width()), scaled(mazeSprite.height())
        );
        overPaintActorSprites(level);
        drawFood(level);
    }

    private void drawFood(GameLevel gameLevel) {
        requireNonNull(gameLevel);
        ctx().save();
        ctx().scale(scaling(), scaling());
        ColoredMazeSpriteSet recoloredMaze =  gameLevel.worldMap().getConfigValue(TengenMsPacMan_UIConfig.RECOLORED_MAZE_PROPERTY);
        Color pelletColor = Color.web(recoloredMaze.mazeSprite().colorScheme().pelletColorRGB());
        drawPellets(gameLevel, pelletColor);
        drawEnergizers(gameLevel, pelletColor);
        ctx().restore();
    }

    private void drawPellets(GameLevel gameLevel, Color pelletColor) {
        gameLevel.worldMap().tiles().filter(gameLevel::isFoodPosition).filter(not(gameLevel::isEnergizerPosition)).forEach(tile -> {
            ctx().setFill(backgroundColor());
            fillSquareAtTileCenter(tile, 4);
            if (!gameLevel.tileContainsEatenFood(tile)) {
                // draw pellet using the right color
                ctx().setFill(pelletColor);
                fillSquareAtTileCenter(tile, 2);
            }
        });
    }

    private void drawEnergizers(GameLevel level, Color pelletColor) {
        double size = TS;
        double offset = 0.5 * HTS;
        level.worldMap().tiles().filter(level::isEnergizerPosition).forEach(tile -> {
            ctx().setFill(backgroundColor());
            fillSquareAtTileCenter(tile, TS + 2);
            if (!level.tileContainsEatenFood(tile) && level.blinking().isOn()) {
                ctx().setFill(pelletColor);
                // draw pixelated "circle"
                double cx = tile.x() * TS, cy = tile.y() * TS;
                ctx().fillRect(cx + offset, cy, HTS, size);
                ctx().fillRect(cx, cy + offset, size, HTS);
                ctx().fillRect(cx + 1, cy + 1, size - 2, size - 2);
            }
        });
    }

    public void drawLevelMessage(GameLevel level, Vector2f position, Font font) {
        requireNonNull(level);
        requireNonNull(position);
        requireNonNull(font);

        if (level.messageType() == GameLevel.MESSAGE_NONE) return;

        NES_ColorScheme nesColorScheme = level.worldMap().getConfigValue("nesColorScheme");
        float x = position.x(), y = position.y() + TS;
        switch (level.messageType()) {
            case GameLevel.MESSAGE_READY
                -> fillTextCentered("READY!", uiConfig.localAssetColor("color.ready_message"), font, x, y);
            case GameLevel.MESSAGE_GAME_OVER -> {
                Color color = level.isDemoLevel()
                    ? Color.web(nesColorScheme.strokeColorRGB())
                    : uiConfig.localAssetColor("color.game_over_message");
                fillTextCentered("GAME OVER", color, font, x, y);
            }
            case GameLevel.MESSAGE_TEST
                -> fillTextCentered("TEST L%02d".formatted(level.number()), nesPaletteColor(0x28), font, x, y);
        }
    }

    private void overPaintActorSprites(GameLevel level) {
        House house = level.house().orElse(null);
        if (house == null) {
            Logger.error("No house exists in game level!");
            return;
        }

        double margin = scaled(1), halfMargin = 0.5f * margin;
        double s = scaled(TS);

        // Over-paint area at house bottom where the ghost sprites are shown in map
        var inHouseArea = new Rectangle2D(
            halfMargin + s * (house.minTile().x() + 1),
            halfMargin + s * (house.minTile().y() + 2),
            s * (house.sizeInTiles().x() - 2) - margin,
            s * 2 - margin
        );

        ctx().setFill(backgroundColor());
        ctx().fillRect(inHouseArea.getMinX(), inHouseArea.getMinY(), inHouseArea.getWidth(), inHouseArea.getHeight());

        // Now the actor sprites outside the house. Be careful not to over-paint nearby obstacle edges!
        Vector2i pacTile = level.worldMap().getTerrainTileProperty("pos_pac", Vector2i.of(14, 26));
        overPaintActorSprite(pacTile, margin);

        Vector2i redGhostTile = level.worldMap().getTerrainTileProperty("pos_ghost_1_red", Vector2i.of(13, 14));
        overPaintActorSprite(redGhostTile, margin);
    }

    private void overPaintActorSprite(Vector2i tile, double margin) {
        double halfMargin = 0.5f * margin;
        double overPaintSize = scaled(2 * TS) - margin;
        ctx().fillRect(
            halfMargin + scaled(tile.x() * TS),
            halfMargin + scaled(tile.y() * TS - HTS),
            overPaintSize, overPaintSize);
    }

    public void drawGameOptions(MapCategory category, Difficulty difficulty, PacBooster booster, double centerX, double y) {
        TengenMsPacMan_SpriteSheet spriteSheet = uiConfig.spriteSheet();
        drawSpriteCentered(centerX, y, spriteSheet.sourceImage(), spriteSheet.sprite(SpriteID.INFO_FRAME));
        RectShort categorySprite = switch (requireNonNull(category)) {
            case BIG     -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_BIG);
            case MINI    -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_MINI);
            case STRANGE -> spriteSheet.sprite(SpriteID.INFO_CATEGORY_STRANGE);
            case ARCADE  -> RectShort.ZERO;
        };
        drawSpriteCentered(centerX + TS(4.5), y, spriteSheet.sourceImage(), categorySprite);
        RectShort difficultySprite = switch (requireNonNull(difficulty)) {
            case EASY   -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_EASY);
            case HARD   -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_HARD);
            case CRAZY  -> spriteSheet.sprite(SpriteID.INFO_DIFFICULTY_CRAZY);
            case NORMAL -> RectShort.ZERO;
        };
        drawSpriteCentered(centerX, y, spriteSheet.sourceImage(), difficultySprite);
        if (requireNonNull(booster) != PacBooster.OFF) {
            drawSpriteCentered(centerX - TS(6), y, spriteSheet.sourceImage(), spriteSheet.sprite(SpriteID.INFO_BOOSTER));
        }
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

    private void drawClapperBoard(Clapperboard clapperboard) {
        requireNonNull(clapperboard);
        if (!clapperboard.isVisible()) return;
        clapperboard.sprite().ifPresent(sprite -> {
            double numberX = clapperboard.x() + 8, numberY = clapperboard.y() + 18; // baseline
            drawSpriteCentered(clapperboard.center(), uiConfig.spriteSheet().sourceImage(), sprite);
            // over-paint number from sprite sheet
            ctx().save();
            ctx().scale(scaling(), scaling());
            ctx().setFill(backgroundColor());
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
        ctx().save();
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
        ctx().restore();
    }
}