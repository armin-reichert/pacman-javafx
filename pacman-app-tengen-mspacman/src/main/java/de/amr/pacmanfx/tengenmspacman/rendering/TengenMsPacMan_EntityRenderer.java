/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.door.comp.DoorDataComp;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimation;
import de.amr.pacmanfx.tengenmspacman.entities.GameOptionsDisplay;
import de.amr.pacmanfx.tengenmspacman.entities.LevelNumberDisplay;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.TengenMsPacMan_ClapperboardAnimationSystem;
import de.amr.pacmanfx.tengenmspacman.entities.gameoptionsdisplay.GameOptionsDataComp;
import de.amr.pacmanfx.tengenmspacman.model.BoosterMode;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.ui.GlobalFonts;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.entities.hud.comp.HUD_Style;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.FacingSprite;
import de.amr.pacmanfx.uilib.rendering.MessageViewRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Arrays;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_EntityRenderer extends BaseRenderer implements SpriteRenderer {

    // These arrays must be sorted!
    private static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    private static final int[] BONUS_POINTS = { 100, 200, 500, 700, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000 };

    private final ActorSpriteAnimController animSystem;
    private final MarqueeRenderer marqueeRenderer;
    private final MessageViewRenderer messageViewRenderer;

    public TengenMsPacMan_EntityRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);
        this.animSystem = requireNonNull(animSystem);

        marqueeRenderer = new MarqueeRenderer(canvas);
        marqueeRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        marqueeRenderer.scalingProperty().bind(scalingProperty());

        messageViewRenderer = new MessageViewRenderer(canvas, TengenMsPacMan_RenderConfig.MESSAGE_TEXTS);
        messageViewRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        messageViewRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Renderable r, long tick) {
        requireNonNull(r);
        if (r instanceof GameEntity gameEntity) {
            if (gameEntity.isVisible()) {
                ctx.save();
                ctx.setImageSmoothing(true);
                //TODO REMOVE! This does not belong here and is complete crap!
                ctx.translate(scaled(16), 0); // content indent of map
                renderGameEntity(gameEntity, tick);
                ctx.restore();
            }
        } else {
            super.render(r, tick);
        }
    }

    private void renderGameEntity(GameEntity gameEntity, long tick) {
        final Vector2f center = gameEntity.pos().bodyCenter();
        switch (gameEntity) {
            case Bonus bonus -> drawSpriteCentered(computeSprite(bonus), center);
            case BonusPoints points -> drawSpriteCentered(computeSprite(points), center);
            case Ghost ghost -> drawSpriteCentered(computeSprite(ghost), center);
            case GhostPoints points -> drawSpriteCentered(computeSprite(points), center);
            case Pac pac -> drawFacingSpriteCentered(computeSprite(pac), center);
            case MessageView messageView -> messageViewRenderer.renderMessageView(messageView);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case Stork stork -> drawStork(stork);
            case Marquee marquee -> drawMarquee(marquee, tick);
            case Door door -> drawDoor(door);
            case LevelCounter levelCounter -> drawLevelCounter(levelCounter);
            case LivesCounter livesCounter -> drawLivesCounter(livesCounter);
            case Score score -> {
                switch (score.type()) {
                    case GAME_SCORE -> drawGameScore(score, tick);
                    case HIGH_SCORE -> drawHighScore(score);
                }
            }
            case GameOptionsDisplay gameOptionsDisplay -> drawGameOptionsDisplay(gameOptionsDisplay);
            case LevelNumberDisplay levelNumberDisplay -> drawLevelNumberDisplay(levelNumberDisplay);
            case CreditDisplay _ -> { /* Not used in this game variant */}

            default -> {}
        }
    }

    private FacingSprite computeSprite(Pac pac) {
        final int frame = animSystem.currentFrame(pac);
        final Direction dir = pac.worldNavigation().moveDir();
        return switch (animSystem.selectedAnimationID(pac)) {
            case null -> facingSprite(SpriteID.MS_PAC_MUNCHING, frame, dir);
            case CommonSpriteAnimationID.PAC_DYING -> computePacDyingSprite(pac);
            case CommonSpriteAnimationID.PAC_MOUTH_MOVING -> facingSprite(SpriteID.MS_PAC_MUNCHING, frame, dir);
            case TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER -> facingSprite(SpriteID.MS_PAC_MUNCHING_BOOSTER, frame, dir);
            case TengenMsPacMan_AnimationID.MS_PAC_MAN_TURNING_AWAY -> facingSprite(SpriteID.MS_PAC_TURNING_AWAY, frame, dir);
            case TengenMsPacMan_AnimationID.MS_PAC_MAN_WAVING_HAND -> facingSprite(SpriteID.MS_PAC_WAVING_HAND, frame, dir);
            case TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING -> facingSprite(SpriteID.MR_PAC_MUNCHING, frame, dir);
            case TengenMsPacMan_AnimationID.MR_PAC_MAN_TURNING_AWAY -> facingSprite(SpriteID.MR_PAC_TURNING_AWAY, frame, dir);
            case TengenMsPacMan_AnimationID.MR_PAC_MAN_WAVING_HAND -> facingSprite(SpriteID.MR_PAC_WAVING_HAND, frame, dir);
            default -> new FacingSprite(animSystem.currentSprite(pac), pac.worldNavigation().moveDir());
        };
    }

    private FacingSprite facingSprite(SpriteID spriteArrayID, int frame, Direction dir) {
        return new FacingSprite(SpriteSheet.spriteOrDefault(spriteSheet().findSpriteSequence(spriteArrayID), frame), dir);
    }

    // Dying animation is realized by providing a sprite facing to the corresponding direction for each animation frame
    private FacingSprite computePacDyingSprite(Pac pac) {
        final var dyingAnimation = animSystem.animation(pac, CommonSpriteAnimationID.PAC_DYING);
        if (dyingAnimation instanceof SpriteAnimation spriteAnimation) {
            final Direction dir = switch (spriteAnimation.frame()) {
                case 0, 4, 8  -> Direction.DOWN;
                case 1, 5, 9  -> Direction.LEFT;
                case 2, 6, 10 -> Direction.UP;
                case 3, 7     -> Direction.RIGHT;
                default       -> Direction.UP; // end position from frame 11 on
            };
            return new FacingSprite(spriteAnimation.sprite(), dir);
        } else {
            throw new IllegalArgumentException("No sprite animation set for Pac-Man dying");
        }
    }

    private RectShort computeSprite(Ghost ghost) {
        if (animSystem.isSelected(ghost, CommonSpriteAnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.worldNavigation().wishDir());
            return SpriteSheet.spriteOrDefault(sprites, animSystem.currentFrame(ghost));
        }
        if (animSystem.isSelected(ghost, CommonSpriteAnimationID.GHOST_EYES)) {
            return spriteSheet().ghostEyesSprite(ghost.worldNavigation().wishDir());
        }
        else {
            return animSystem.currentSprite(ghost);
        }
    }

    private RectShort computeSprite(GhostPoints ghostPoints) {
        final int index = Arrays.binarySearch(GHOST_POINTS, ghostPoints.points().number());
        return index >= 0 ? spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS)[index] : RectShort.NULL_RECTANGLE;
    }

    private RectShort computeSprite(Bonus bonus) {
        return switch (bonus.state().enumValue()) {
            case EDIBLE -> SpriteSheet.spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN, INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }

    private RectShort computeSprite(BonusPoints bonusPoints) {
        final int index = Arrays.binarySearch(BONUS_POINTS, bonusPoints.points().number());
        return index >= 0 ? spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES)[index] : RectShort.NULL_RECTANGLE;
    }

    private void drawDoor(Door door) {
        final var data = door.reqComp(DoorDataComp.class);

        final double scaledTileSize = scaled(TS);
        final Vector2i leftDoorTile = data.leftTile();
        final double xMin = leftDoorTile.x() * scaledTileSize;
        final double yMin = leftDoorTile.y() * scaledTileSize + scaled(5); // 5 pixels down

        ctx.save();
        ctx.setFill(Color.valueOf(data.color()));
        ctx.fillRect(xMin, yMin, 2 * scaledTileSize, scaled(2));
        ctx.restore();
    }

    private void drawMarquee(Marquee marquee, long tick) {
        marqueeRenderer.render(marquee, tick);
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        TengenMsPacMan_ClapperboardAnimationSystem.sprite(clapperboard).ifPresent(sprite -> {
            final Font arcade8 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), scaled(8));
            double numberX = clapperboard.pos().x() + 8, numberY = clapperboard.pos().y() + 18; // baseline
            drawSpriteCentered(sprite, clapperboard.pos().bodyCenter());
            // over-paint number from sprite sheet
            ctx.save();
            ctx.scale(scaling(), scaling());
            ctx.setFill(backgroundColor());
            ctx.fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx.restore();

            ctx.setFont(arcade8);
            ctx.setFill(NES_Palette.color(0x20));

            final String number = String.valueOf(clapperboard.inscription().number());
            final String text = clapperboard.inscription().text();
            ctx.fillText(number, scaled(numberX), scaled(numberY));
            if (clapperboard.state().textVisible()) {
                double textX = clapperboard.pos().x() + sprite.width(), textY = clapperboard.pos().y() + 2;
                ctx.fillText(text, scaled(textX), scaled(textY));
            }
        });
    }

    private void drawStork(Stork stork) {
        drawSpriteCentered(animSystem.currentSprite(stork), stork.pos().bodyCenter());
        if (stork.isBagReleasedFromBeak()) {
            ctx.save();
            // Sprite sheet has no stork without bag under its beak so we over-paint the bag
            ctx.setFill(backgroundColor());
            ctx.fillRect(scaled(stork.pos().x() - 13), scaled(stork.pos().y() + 3), scaled(8), scaled(10));
            ctx.restore();
        }
    }

    // Assumes the unrotated sprite is facing left (like Ms. Pac-Man sprite in the current sprite sheet).
    // When facing up or down, Ms. Pac-Man top of head is on the right.
    private void drawFacingSpriteCentered(FacingSprite facingSprite, Vector2f centerUnscaled) {
        ctx().save();
        ctx().translate(centerUnscaled.x() * scaling(), centerUnscaled.y() * scaling());
        switch (facingSprite.facing()) {
            case LEFT  -> { /* sprite facing direction in sprite sheet */ }
            case UP    -> ctx().rotate(90);
            case RIGHT -> ctx().scale(-1, 1); // mirror at y-axis
            case DOWN  -> {
                ctx().scale(-1, 1); // mirror at y-axis
                ctx().rotate(-90); // rotate 90 degrees clockwise
            }
        }
        drawSpriteCentered(facingSprite.sprite(), 0, 0);
        ctx().restore();
    }

    // --- HUD ---

    private void drawGameOptionsDisplay(GameOptionsDisplay display) {
        final GameOptionsDataComp options = display.options();

        final RectShort mapCategorySprite = switch (options.mapCategory()) {
            case BIG     -> spriteSheet().findSprite(SpriteID.INFO_CATEGORY_BIG);
            case MINI    -> spriteSheet().findSprite(SpriteID.INFO_CATEGORY_MINI);
            case STRANGE -> spriteSheet().findSprite(SpriteID.INFO_CATEGORY_STRANGE);
            case ARCADE  -> null;
        };

        final RectShort difficultySprite = switch (options.difficulty()) {
            case EASY   -> spriteSheet().findSprite(SpriteID.INFO_DIFFICULTY_EASY);
            case HARD   -> spriteSheet().findSprite(SpriteID.INFO_DIFFICULTY_HARD);
            case CRAZY  -> spriteSheet().findSprite(SpriteID.INFO_DIFFICULTY_CRAZY);
            case NORMAL -> null;
        };

        final float centerX = display.pos().x();
        final float y = display.pos().y();

        drawSpriteCentered(spriteSheet().findSprite(SpriteID.INFO_FRAME), centerX, y);

        if (options.boosterMode() != BoosterMode.BOOSTER_OFF) {
            drawSpriteCentered(spriteSheet().findSprite(SpriteID.INFO_BOOSTER), centerX - tilesPx(5.5f), y);
        }
        if (difficultySprite != null) {
            drawSpriteCentered(difficultySprite, centerX, y);
        }
        if (mapCategorySprite != null) {
            drawSpriteCentered(mapCategorySprite, centerX + tilesPx(4.5f), y);
        }
    }

    private void drawGameScore(Score score, long tick) {
        final HUD_Style style = score.reqComp(HUD_Style.class);
        final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());
        // Blink frequency = 1Hz (30 ticks on, 30 ticks off)
        if (tick % 60 < 30) {
            fillText(style.scoreText(), style.scoreTextColor(), scaledFont, score.pos().x(), score.pos().y());
        }
        fillText("%6d".formatted(score.data().points()),
            style.scoreTextColor(), scaledFont, 2 * TS, score.pos().y() + TS);
    }

    private void drawHighScore(Score score) {
        final HUD_Style style = score.reqComp(HUD_Style.class);
        final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());
        final Color color = score.data().isEnabled() ? style.scoreTextColor(): style.scoreTextColorDisabled();
        fillText("HIGH SCORE", color, scaledFont, score.pos().x(), score.pos().y());
        fillText("%6d".formatted(score.data().points()), color, scaledFont,
            score.pos().x() + 2 * TS, score.pos().y() + TS
        );
    }

    private void drawLivesCounter(LivesCounter livesCounter) {
        final HUD_Style style = livesCounter.reqComp(HUD_Style.class);
        final float x = livesCounter.pos().x();
        final float y = livesCounter.pos().y();

        final int numLives      = livesCounter.data().numLives();
        final int numLivesShown = livesCounter.data().numLivesShown();
        final int maxLivesShown = livesCounter.data().maxLivesShown();

        for (int i = 0; i < numLivesShown; ++i) {
            drawSprite(style.livesCounterSymbolSprite(), x + i * tilesPx(2), y, true);
        }

        if (numLives > maxLivesShown) {
            final Font scaledFont = Font.font("Serif", FontWeight.BLACK, scaled(8));
            fillText("(%d)".formatted(numLives), NES_Palette.color(0x28), scaledFont, tilesPx(14), y + TS);
        }
    }

    private void drawLevelCounter(LevelCounter levelCounter) {
        float x = levelCounter.pos().x();
        float y = levelCounter.pos().y();

        // Symbols are drawn from right to left!
        final RectShort[] symbolSprites = spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS);
        for (int code : levelCounter.data().symbolCodes()) {
            if (0 <= code && code < symbolSprites.length) {
                drawSprite(symbolSprites[code], x, y, true);
            }
            x -= tilesPx(2);
        }
    }

    private void drawLevelNumberDisplay(LevelNumberDisplay display) {
        final float x = display.pos().x();
        final float y = display.pos().y();
        final int number = display.levelNumber().number();

        drawSprite(spriteSheet().findSprite(SpriteID.LEVEL_NUMBER_BOX), x, y, true);

        final int tens = number / 10;
        if (tens > 0) {
            final RectShort tensSprite = spriteSheet().findDigitSprite(number / 10);
            drawSprite(tensSprite, x + 2, y + 2, true);
        }

        final RectShort onesSprite = spriteSheet().findDigitSprite(number % 10);
        drawSprite(onesSprite, x + 10, y + 2, true);
    }

}