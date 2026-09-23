/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.ecs.systems.ActorSpriteAnimController;
import de.amr.basics.ui.spriteanim.CommonSpriteAnimationID;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.Energizer;
import de.amr.pacmanfx.core.entities.actor.bonus.Bonus;
import de.amr.pacmanfx.core.entities.actor.ghost.Ghost;
import de.amr.basics.ui.entities.hud.levelCounter.LevelCounter;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.basics.ui.entities.hud.score.Score;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.basics.ui.entities.props.bonuspoints.BonusPoints;
import de.amr.basics.ui.entities.props.ghostpoints.GhostPoints;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.assets.SpriteSheet;
import de.amr.basics.ui.entities.hud.HUD_Style;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.ArcadeColor;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Arrays;
import java.util.Optional;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.tilesPx;
import static java.util.Objects.requireNonNull;

public class ArcadePacMan_VariantRenderer extends BaseRenderer {

    // These arrays must be sorted!
    private static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    private static final int[] BONUS_POINTS = { 100, 300, 500, 700, 1000, 2000, 3000, 5000 };

    private final ArcadePacMan_SpriteSheet spriteSheet = ArcadePacMan_SpriteSheet.instance();
    private final ActorSpriteAnimController animController;

    public ArcadePacMan_VariantRenderer(ActorSpriteAnimController animController, Canvas canvas) {
        super(canvas);
        this.animController = requireNonNull(animController);
    }

    @Override
    public Optional<SpriteSheet<?>> optSpriteSheet() {
        return Optional.of(spriteSheet);
    }

    @Override
    public void render(Renderable r, long tick) {
        requireNonNull(r);
        switch (r) {
            case GameEntityView rge -> renderGameEntity(rge.entity(), tick);
            case GameEntity gameEntity ->    renderGameEntity(gameEntity, tick);
            default -> super.render(r, tick);
        }
    }

    @Override
    protected void renderGameEntity(GameEntity gameEntity, long tick) {
        if (!gameEntity.isVisible()) return;

        ctx.save();
        ctx.setImageSmoothing(true);
        final Vector2f center = gameEntity.pos().bodyCenter();
        switch (gameEntity) {
            case Bonus bonus -> drawSpriteCentered(computeSprite(bonus), center);
            case BonusPoints bonusPoints -> drawSpriteCentered(computeSprite(bonusPoints), center);
            case Energizer energizer -> draw(energizer);
            case Ghost ghost -> drawSpriteCentered(computeSprite(ghost), center);
            case GhostPoints points -> drawSpriteCentered(computeSprite(points), center);
            case LevelCounter levelCounter -> draw(levelCounter);
            case LivesCounter livesCounter -> draw(livesCounter);
            case Pac pac -> drawSpriteCentered(computeSprite(pac), center);
            case Score score -> draw(score);
            default -> super.renderGameEntity(gameEntity, tick);
        }
        ctx.restore();
    }

    private RectShort computeSprite(Pac pac) {
        if (animController.isSelected(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING)) {
            final Direction dir = pac.worldNavigation().moveDir();
            final RectShort[] sprites = spriteSheet.pacMunchingSprites(dir);
            return SpriteSheet.spriteOrDefault(sprites, animController.currentFrame(pac));
        }
        else {
            return animController.currentSprite(pac);
        }
    }

    private RectShort computeSprite(Ghost ghost) {
        if (animController.isSelected(ghost, CommonSpriteAnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet.ghostNormalSprites(ghost.personality(), ghost.worldNavigation().wishDir());
            return SpriteSheet.spriteOrDefault(sprites, animController.currentFrame(ghost));
        }
        if (animController.isSelected(ghost, CommonSpriteAnimationID.GHOST_EYES)) {
            return spriteSheet.ghostEyesSprite(ghost.worldNavigation().wishDir());
        }
        final RectShort sprite = animController.currentSprite(ghost);
        if (sprite == null) {
            throw new IllegalStateException("No sprite could be computed for ghost %s".formatted(ghost));
        }
        return sprite;
    }

    private RectShort computeSprite(BonusPoints bonusPoints) {
        final int index = Arrays.binarySearch(BONUS_POINTS, bonusPoints.points().number());
        return index >= 0 ? spriteSheet.findSpriteSequence(SpriteID.BONUS_VALUES)[index] : RectShort.NULL_RECTANGLE;
    }

    private RectShort computeSprite(GhostPoints ghostPoints) {
        final int index = Arrays.binarySearch(GHOST_POINTS, ghostPoints.points().number());
        return index >= 0 ? spriteSheet.findSpriteSequence(SpriteID.GHOST_NUMBERS)[index] : RectShort.NULL_RECTANGLE;
    }

    //TODO: decouple symbol code from index in sprite array
    private RectShort computeSprite(Bonus bonus) {
        return switch (bonus.state().enumValue()) {
            case EDIBLE   -> SpriteSheet.spriteOrDefault(spriteSheet.findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN, INACTIVE  -> RectShort.NULL_RECTANGLE;
        };
    }

    private void draw(Score score) {
        final HUD_Style style = score.reqComp(HUD_Style.class);
        final Font scaledFont = Ufx.scaleFontBy(style.scoreTextFont(), scaling());
        final boolean disabled = !score.data().isEnabled();
        final Color color = disabled ? style.scoreTextColorDisabled() : style.scoreTextColor();
        drawScoreText(score, score.type() == Score.Type.GAME_SCORE ? style.scoreText() : style.highScoreText(), scaledFont, color);
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

    private void draw(Energizer energizer) {
        if (!energizer.on()) {
            final double size = scaled(9);
            ctx.save();
            ctx.setFill(backgroundColor());
            ctx.fillRect(scaled(energizer.pos().x() - 0.5), scaled(energizer.pos().y() - 0.5), size, size);
            ctx.restore();
        }
    }

    private void draw(LivesCounter livesCounter) {
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
            fillText("%d".formatted(numLives), ArcadeColor.YELLOW.color(), font, x - 14, y + TS);
        }
    }

    private void draw(LevelCounter levelCounter) {
        final HUD_Style style = levelCounter.reqComp(HUD_Style.class);
        final float y = levelCounter.pos().y();
        float x = levelCounter.pos().x();
        for (int symbolCode : levelCounter.data().symbolCodes()) {
            drawSprite(style.bonusSymbolSprites()[symbolCode], x, y, true);
            x -= tilesPx(2); // symbols are drawn from right to left
        }
    }
}