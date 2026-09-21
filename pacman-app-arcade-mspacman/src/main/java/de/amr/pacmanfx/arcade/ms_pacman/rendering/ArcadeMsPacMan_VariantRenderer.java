/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.arcade.ms_pacman.props.Heart;
import de.amr.pacmanfx.arcade.ms_pacman.props.clapperboard.ClapperboardAnimationSystem;
import de.amr.pacmanfx.arcade.ms_pacman.gamescene.introscene.MarqueeRenderer;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.comp.SpriteAnimationComp;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.pacmanfx.core.entities.hud.levelCounter.LevelCounter;
import de.amr.pacmanfx.core.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.entities.hud.score.Score;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.arcade.ms_pacman.props.bag.Bag;
import de.amr.pacmanfx.core.entities.props.bonuspoints.BonusPoints;
import de.amr.pacmanfx.core.entities.props.clapperboard.Clapperboard;
import de.amr.pacmanfx.core.entities.props.ghostpoints.GhostPoints;
import de.amr.pacmanfx.core.entities.props.marquee.Marquee;
import de.amr.pacmanfx.core.entities.props.stork.Stork;
import de.amr.pacmanfx.core.rendering.Renderable;
import de.amr.pacmanfx.core.rendering.RenderableGameEntity;
import de.amr.pacmanfx.core.spriteanim.CommonSpriteAnimationID;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import de.amr.pacmanfx.uilib.entities.hud.HUD_Style;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Arrays;
import java.util.Optional;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;
import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_YELLOW;
import static java.util.Objects.requireNonNull;

/**
 * Implements the rendering for all actor types occurring in the Arcade Ms. Pac-Man game.
 */
public class ArcadeMsPacMan_VariantRenderer extends BaseRenderer {

    // These arrays must be sorted!
    private static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    private static final int[] BONUS_POINTS = { 100, 200, 500, 700, 1000, 2000, 5000 };

    private final ArcadeMsPacMan_SpriteSheet spriteSheet = ArcadeMsPacMan_SpriteSheet.instance();
    private final ActorSpriteAnimController animController;
    private final MarqueeRenderer marqueeRenderer;

    public ArcadeMsPacMan_VariantRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);
        this.animController = requireNonNull(animController);

        this.marqueeRenderer = new MarqueeRenderer(canvas);
        marqueeRenderer.backgroundColorProperty().bind(backgroundColorProperty());
        marqueeRenderer.scalingProperty().bind(scalingProperty());
    }

    @Override
    public Optional<SpriteSheet<?>> optSpriteSheet() {
        return Optional.of(spriteSheet);
    }

    @Override
    public void render(Renderable r, long tick) {
        switch (r) {
            case RenderableGameEntity rge -> renderGameEntity(rge.gameEntity(), tick);
            // This case will disappear:
            case GameEntity gameEntity -> renderGameEntity(gameEntity, tick);
            default -> super.render(r, tick);
        }
    }

    @Override
    protected void renderGameEntity(GameEntity gameEntity, long tick) {
        if (!gameEntity.isVisible()) return;

        ctx.save();
        final Vector2f center = gameEntity.pos().bodyCenter();
        switch (gameEntity) {
            case Pac pac                   -> drawSpriteCentered(computeSprite(pac),    center);
            case Ghost ghost               -> drawSpriteCentered(computeSprite(ghost),  center);
            case GhostPoints points        -> drawSpriteCentered(computeSprite(points), center);
            case Bonus bonus               -> drawSpriteCentered(computeSprite(bonus),  center);
            case BonusPoints points        -> drawSpriteCentered(computeSprite(points), center);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case Heart heart               -> drawSpriteCentered(computeSprite(heart), center);
            case Marquee marquee           -> marqueeRenderer.renderMarquee(marquee, tick);
            case LevelCounter levelCounter -> drawLevelCounter(levelCounter);
            case LivesCounter livesCounter -> drawLivesCounter(livesCounter);
            case Score score -> {
                if (score.type() == Score.Type.GAME_SCORE) {
                    drawGameScore(score);
                } else {
                    drawHighScore(score);
                }
            }
            case Bag bag -> drawSpriteCentered(computeSprite(bag), center);
            case Stork stork -> drawSpriteCentered(computeSprite(stork), center);
            default -> super.renderGameEntity(gameEntity, tick);
        }
        ctx.restore();
    }

    private RectShort computeSprite(Ghost ghost) {
        RectShort sprite;
        if (animController.isSelected(ghost, CommonSpriteAnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet.ghostNormalSprites(ghost.personality(), ghost.worldNavigation().wishDir());
            sprite = SpriteSheet.spriteOrDefault(sprites, animController.currentFrame(ghost));
        }
        else if (animController.isSelected(ghost, CommonSpriteAnimationID.GHOST_EYES)) {
            sprite = spriteSheet.ghostEyesSprite(ghost.worldNavigation().wishDir());
        }
        else {
            sprite = animController.currentSprite(ghost);
        }
        if (sprite == null) {
            throw new IllegalStateException("No sprite could be computed for ghost %s".formatted(ghost));
        }
        return sprite;
    }

    private RectShort computeSprite(Pac pac) {
        RectShort sprite;
        if (animController.isSelected(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING)) {
            final RectShort[] sprites = spriteSheet.msPacManMunchingSprites(pac.worldNavigation().moveDir());
            sprite = SpriteSheet.spriteOrDefault(sprites, animController.currentFrame(pac));
        }
        else if (animController.isSelected(pac, CommonSpriteAnimationID.MR_PAC_MAN_MUNCHING)) {
            final RectShort[] sprites = spriteSheet.mrPacManMunchingSprites(pac.worldNavigation().moveDir());
            sprite = SpriteSheet.spriteOrDefault(sprites, animController.currentFrame(pac));
        }
        else {
            sprite = animController.currentSprite(pac);
        }
        if (sprite == null) {
            throw new IllegalStateException("Could not determine Pac sprite");
        }
        return sprite;
    }

    // TODO decouple symbol code from sprite index
    private RectShort computeSprite(Bonus bonus) {
        return switch (bonus.state().enumValue()) {
            case EDIBLE -> SpriteSheet.spriteOrDefault(spriteSheet.findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN, INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }

    private RectShort computeSprite(BonusPoints bonusPoints) {
        final int index = Arrays.binarySearch(BONUS_POINTS, bonusPoints.points().number());
        return index >= 0 ? spriteSheet.findSpriteSequence(SpriteID.BONUS_VALUES)[index] : RectShort.NULL_RECTANGLE;
    }

    private RectShort computeSprite(GhostPoints ghostPoints) {
        final int index = Arrays.binarySearch(GHOST_POINTS, ghostPoints.points().number());
        return index >= 0 ? spriteSheet.findSpriteSequence(SpriteID.GHOST_NUMBERS)[index] : RectShort.NULL_RECTANGLE;
    }

    private RectShort computeSprite(Bag bag) {
        return bag.reqComp(SpriteAnimationComp.class).spriteAnimations().currentSprite();
    }

    private RectShort computeSprite(Heart heart) {
        return heart.reqComp(SpriteAnimationComp.class).spriteAnimations().currentSprite();
    }

    private RectShort computeSprite(Stork stork) {
        return stork.reqComp(SpriteAnimationComp.class).spriteAnimations().currentSprite();
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        ClapperboardAnimationSystem.sprite(clapperboard).ifPresent(sprite -> {
            drawSpriteCentered(sprite, clapperboard.pos().bodyCenter());

            final Font arcade8 = Ufx.deriveFont(GlobalFonts.ARCADE.font(), scaled(8));
            // Draw number and title
            final String number = clapperboard.inscription().number();
            final String text = clapperboard.inscription().text();
            final double numberX = scaled(clapperboard.pos().x() + sprite.width() - 25);
            final double textX = scaled(clapperboard.pos().x() + sprite.width());
            final double y = scaled(clapperboard.pos().y() + 18);
            ctx.setFont(arcade8);
            ctx.setFill(ARCADE_WHITE);
            ctx.fillText(number, numberX, y);
            ctx.fillText(text, textX, y);
        });
    }

    // --- HUD ---

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