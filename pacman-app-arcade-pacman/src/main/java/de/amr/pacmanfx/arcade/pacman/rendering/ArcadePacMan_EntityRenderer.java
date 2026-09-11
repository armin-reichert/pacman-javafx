/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.Energizer;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.entities.hud.comp.HUD_Style;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.MessageViewRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Arrays;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_EntityRenderer extends BaseRenderer implements SpriteRenderer {

    // These arrays must be sorted!
    private static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    private static final int[] BONUS_POINTS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

    private final ActorSpriteAnimController animController;
    private final MessageViewRenderer messageViewRenderer;

    public ArcadePacMan_EntityRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);
        this.animController = requireNonNull(animController);

        messageViewRenderer = new MessageViewRenderer(canvas, ArcadePacMan_RenderConfig.MESSAGE_TEXTS);
        messageViewRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        messageViewRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Renderable r, long tick) {
        requireNonNull(r);
        if (r instanceof GameEntity gameEntity) {
            if (gameEntity.isVisible()) {
                ctx.save();
                ctx.setImageSmoothing(true);
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
            case Pac pac -> drawSpriteCentered(computeSprite(pac), center);
            case Ghost ghost -> drawSpriteCentered(computeSprite(ghost), center);
            case GhostPoints points -> drawSpriteCentered(computeSprite(points), center);
            case Bonus bonus -> drawSpriteCentered(computeSprite(bonus), center);
            case BonusPoints bonusPoints -> drawSpriteCentered(computeSprite(bonusPoints), center);
            case MessageView messageView -> messageViewRenderer.renderMessageView(messageView);
            case Energizer energizer -> drawEnergizer(energizer);
            case LevelCounter levelCounter -> drawLevelCounter(levelCounter);
            case LivesCounter livesCounter -> drawLivesCounter(livesCounter);
            case Score score -> {
                if (score.type() == Score.Type.GAME_SCORE) {
                    drawGameScore(score);
                } else {
                    drawHighScore(score);
                }
            }
            case CreditDisplay creditDisplay -> drawCreditDisplay(creditDisplay);
            case TextDisplay textDisplay -> super.render(textDisplay, tick);
            default -> {}
        }
    }

    private RectShort computeSprite(Pac pac) {
        if (animController.isSelected(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING)) {
            final Direction dir = pac.worldNavigation().moveDir();
            final RectShort[] sprites = spriteSheet().pacMunchingSprites(dir);
            return SpriteSheet.spriteOrDefault(sprites, animController.currentFrame(pac));
        }
        else {
            return animController.currentSprite(pac);
        }
    }

    private RectShort computeSprite(Ghost ghost) {
        if (animController.isSelected(ghost, CommonSpriteAnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.worldNavigation().wishDir());
            return SpriteSheet.spriteOrDefault(sprites, animController.currentFrame(ghost));
        }
        if (animController.isSelected(ghost, CommonSpriteAnimationID.GHOST_EYES)) {
            return spriteSheet().ghostEyesSprite(ghost.worldNavigation().wishDir());
        }
        final RectShort sprite = animController.currentSprite(ghost);
        if (sprite == null) {
            throw new IllegalStateException("No sprite could be computed for ghost %s".formatted(ghost));
        }
        return sprite;
    }

    private RectShort computeSprite(BonusPoints bonusPoints) {
        final int index = Arrays.binarySearch(BONUS_POINTS, bonusPoints.points().number());
        return index >= 0 ? spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES)[index] : RectShort.NULL_RECTANGLE;
    }

    private RectShort computeSprite(GhostPoints ghostPoints) {
        final int index = Arrays.binarySearch(GHOST_POINTS, ghostPoints.points().number());
        return index >= 0 ? spriteSheet().findSpriteSequence(SpriteID.GHOST_NUMBERS)[index] : RectShort.NULL_RECTANGLE;
    }

    //TODO: decouple symbol code from index in sprite array
    private RectShort computeSprite(Bonus bonus) {
        return switch (bonus.state().enumValue()) {
            case EDIBLE   -> SpriteSheet.spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN, INACTIVE  -> RectShort.NULL_RECTANGLE;
        };
    }

    private void drawEnergizer(Energizer energizer) {
        if (!energizer.on()) {
            final double size = scaled(9);
            ctx.save();
            ctx.setFill(backgroundColor());
            ctx.fillRect(scaled(energizer.pos().x() - 0.5), scaled(energizer.pos().y() - 0.5), size, size);
            ctx.restore();
        }
    }

    // --- HUD ---

    private void drawCreditDisplay(CreditDisplay creditDisplay) {
        final HUD_Style style = creditDisplay.reqComp(HUD_Style.class);
        final int credit = creditDisplay.data().credit();
        final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());
        final String text = style.creditTextFormat().formatted(credit);
        final float baseline = creditDisplay.pos().y();
        fillText(text, ARCADE_WHITE, scaledFont, creditDisplay.pos().x(), baseline);
    }

    private void drawGameScore(Score score) {
        final HUD_Style style = score.reqComp(HUD_Style.class);
        final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());
        drawScoreText(score, style.scoreText(), scaledFont, style.scoreTextColor());

    }

    private void drawHighScore(Score score) {
        final HUD_Style style = score.reqComp(HUD_Style.class);
        final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());
        final boolean disabled = !score.data().isEnabled();
        final Color color = disabled ? style.scoreTextColorDisabled() : style.scoreTextColor();
        drawScoreText(score, style.highScoreText(), scaledFont, color);
    }

    private void drawScoreText(Score score, String title, Font font, Color color) {
        final float x = score.pos().x();
        final float y = score.pos().y();
        fillText(title, color, font, x, y);
        fillText("%7s".formatted("%02d".formatted(score.data().points())), color, font, x, y + TS + 1);
        if (score.data().points() != 0) {
            fillText("L" + score.data().levelNumber(), color, font, x + tilesPx(8), y + TS + 1);
        }
    }

    private void drawLivesCounter(LivesCounter livesCounter) {
        final HUD_Style style = livesCounter.reqComp(HUD_Style.class);
        final float x = livesCounter.pos().x();
        final float y = livesCounter.pos().y();

        final int numLives = livesCounter.data().numLives();

        final int numLivesShown = livesCounter.data().numLivesShown();
        for (int i = 0; i < numLivesShown; ++i) {
            drawSprite(style.livesCounterSymbolSprite(), x + i * 2 * TS, y, true);
        }

        if (numLives > livesCounter.data().maxLivesShown()) {
            final Font font = Font.font("Serif", FontWeight.BOLD, scaled(8));
            fillText("%d".formatted(numLives), ARCADE_YELLOW, font, x - 14, y + TS);
        }
    }

    private void drawLevelCounter(LevelCounter levelCounter) {
        final HUD_Style style = levelCounter.reqComp(HUD_Style.class);
        final float y = levelCounter.pos().y();
        float x = levelCounter.pos().x();
        for (int symbolCode : levelCounter.data().symbolCodes()) {
            drawSprite(style.bonusSymbolSprites()[symbolCode], x, y, true);
            x -= tilesPx(2); // symbols are drawn from right to left
        }
    }
}