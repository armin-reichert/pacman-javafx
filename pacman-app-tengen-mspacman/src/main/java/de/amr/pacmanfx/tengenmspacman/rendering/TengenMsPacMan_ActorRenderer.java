/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.scenes.Clapperboard;
import de.amr.pacmanfx.tengenmspacman.scenes.Stork;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID.*;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID.ANIM_PAC_MAN_WAVING_HAND;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer, ActorRenderer {

    public TengenMsPacMan_ActorRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;

        switch (actor) {
            case Bonus bonus -> drawBonus(bonus);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case Ghost ghost -> drawGhost(ghost);
            case Pac pac -> drawPac(pac);
            case Stork stork -> drawStork(stork);
            default -> drawSpriteCentered(actor.center(), actor.animations().currentSprite(actor));
        }
    }

    private void drawGhost(Ghost ghost) {
        drawSpriteCentered(ghost.center(), computeGhostSprite(ghost));
    }

    private RectShort computeGhostSprite(Ghost ghost) {
        final AnimationManager animations = ghost.animations();
        if (animations.isSelected(Ghost.AnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = TengenMsPacMan_GhostAnimations.ghostNormalSprites(spriteSheet(), ghost.personality(), ghost.wishDir());
            return sprites[animations.frameIndex()];
        }
        else if (animations.isSelected(Ghost.AnimationID.GHOST_EYES)) {
            final RectShort[] sprites = TengenMsPacMan_GhostAnimations.ghostEyesSprites(spriteSheet(), ghost.wishDir());
            return sprites[animations.frameIndex()];
        }
        else {
            return animations.currentSprite(ghost);
        }
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case INACTIVE -> {}
            case EDIBLE -> drawBonusSprite(bonus.center().plus(0, bonus.verticalElongation()),
                spriteSheet().sprites(SpriteID.BONUS_SYMBOLS), bonus.symbol());
            case EATEN  -> drawBonusSprite(bonus.center(),
                spriteSheet().sprites(SpriteID.BONUS_VALUES),
                // Note: sprite sheet has bonus values in wrong order!
                TengenMsPacMan_UIConfig.bonusValueSpriteIndex(bonus.symbol()));
        }
    }

    private void drawBonusSprite(Vector2f center, RectShort[] sprites, int index) {
        if (0 <= index && index < sprites.length) {
            drawSpriteCentered(center, sprites[index]);
        } else {
            throw new IllegalArgumentException("Illegal bonus symbol index: %d".formatted(index));
        }
    }

    private void drawPac(Pac pac) {
        final AnimationManager animations = pac.animations();
        if (animations.isSelected(Pac.AnimationID.PAC_DYING)) {
            if (animations instanceof SpriteAnimationMap<?> sam) {
                drawPacDyingSpriteAnimation(pac, sam);
            }
            return;
        }
        final int frame = animations.frameIndex();
        RectShort sprite;
        if (animations.isSelected(Pac.AnimationID.PAC_MUNCHING)) {
            sprite = spriteSheet().sprites(SpriteID.MS_PAC_MUNCHING)[frame];
        }
        else if (animations.isSelected(ANIM_MS_PAC_MAN_BOOSTER)) {
            sprite = spriteSheet().sprites(SpriteID.MS_PAC_MUNCHING_BOOSTER)[frame];
        }
        else if (animations.isSelected(ANIM_MS_PAC_MAN_TURNING_AWAY)) {
            sprite = spriteSheet().sprites(SpriteID.MS_PAC_TURNING_AWAY)[frame];
        }
        else if (animations.isSelected(ANIM_MS_PAC_MAN_WAVING_HAND)) {
            sprite = spriteSheet().sprites(SpriteID.MS_PAC_WAVING_HAND)[frame];
        }
        else if (animations.isSelected(ANIM_PAC_MAN_MUNCHING)) {
            sprite = spriteSheet().sprites(SpriteID.MR_PAC_MUNCHING)[frame];
        }
        else if (animations.isSelected(ANIM_PAC_MAN_TURNING_AWAY)) {
            sprite = spriteSheet().sprites(SpriteID.MR_PAC_TURNING_AWAY)[frame];
        }
        else if (animations.isSelected(ANIM_PAC_MAN_WAVING_HAND)) {
            sprite = spriteSheet().sprites(SpriteID.MR_PAC_WAVING_HAND)[frame];
        }
        else {
            sprite = animations.currentSprite(pac);
        }
        final Vector2f center = pac.center().scaled(scaling());
        drawSpriteCenteredRotatedByDir(center, pac.moveDir(), sprite);
    }

    // Simulates dying animation by providing the right direction for each animation frame
    private void drawPacDyingSpriteAnimation(Pac pac, SpriteAnimationMap<?> sam) {
        final Direction dir = switch (sam.frameIndex()) {
            case 0, 4, 8  -> Direction.DOWN;
            case 1, 5, 9  -> Direction.LEFT;
            case 2, 6, 10 -> Direction.UP;
            case 3, 7     -> Direction.RIGHT;
            default       -> Direction.UP; // end position frame 11...
        };
        drawSpriteCenteredRotatedByDir(pac.center().scaled(scaling()), dir, sam.currentSprite(pac));
    }

    // There are only left-pointing Ms. Pac-Man sprites in the sprite sheet, so we rotate and mirror in the renderer
    private void drawSpriteCenteredRotatedByDir(Vector2f center, Direction dir, RectShort sprite) {
        ctx.save();
        ctx.translate(center.x(), center.y());
        switch (dir) {
            case LEFT  -> {}
            case UP    -> ctx.rotate(90);
            case RIGHT -> ctx.scale(-1, 1);
            case DOWN  -> {
                ctx.scale(-1, 1);
                ctx.rotate(-90);
            }
        }
        drawSpriteCentered(0, 0, sprite);
        ctx.restore();
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        requireNonNull(clapperboard);
        if (!clapperboard.isVisible()) return;
        clapperboard.sprite().ifPresent(sprite -> {
            double numberX = clapperboard.x() + 8, numberY = clapperboard.y() + 18; // baseline
            drawSpriteCentered(clapperboard.center(), sprite);
            // over-paint number from sprite sheet
            ctx.save();
            ctx.scale(scaling(), scaling());
            ctx.setFill(backgroundColor());
            ctx.fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx.restore();

            ctx.setFont(arcadeFont8());
            ctx.setFill(nesColor(0x20));
            ctx.fillText(String.valueOf(clapperboard.number()), scaled(numberX), scaled(numberY));
            if (clapperboard.isTextVisible()) {
                double textX = clapperboard.x() + sprite.width(), textY = clapperboard.y() + 2;
                ctx.fillText(clapperboard.text(), scaled(textX), scaled(textY));
            }
        });
    }

    private void drawStork(Stork stork) {
        drawSpriteCentered(stork.center(), stork.animations().currentSprite(stork));
        if (stork.isBagReleasedFromBeak()) {
            // Sprite sheet has no stork without bag under its beak so we over-paint the bag
            ctx.setFill(backgroundColor());
            ctx.fillRect(scaled(stork.x() - 13), scaled(stork.y() + 3), scaled(8), scaled(10));
        }
    }
}