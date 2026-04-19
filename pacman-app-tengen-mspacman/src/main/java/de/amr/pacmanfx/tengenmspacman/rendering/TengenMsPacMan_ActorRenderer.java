/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.basics.spriteanim.SpriteAnimationSet;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengenmspacman.scenes.Clapperboard;
import de.amr.pacmanfx.tengenmspacman.scenes.Stork;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.bonusValueSpriteIndex;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.nesColor;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_ActorRenderer extends BaseRenderer implements SpriteRendererMixin, ActorRenderer {

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
            case Bonus bonus -> drawSpriteCentered(computeBonusSprite(bonus), bonus.center());
            case Ghost ghost -> drawSpriteCentered(computeGhostSprite(ghost), ghost.center());
            case Pac pac -> drawFacingSpriteCentered(computePacSprite(pac), pac.center());
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case Stork stork -> drawStork(stork);
            default -> drawSpriteCentered(actor.animations().currentSprite(), actor.center());
        }
    }

    private RectShort computeGhostSprite(Ghost ghost) {
        final SpriteAnimationSet animations = ghost.animations();
        if (animations.isSelected(ArcadePacMan_AnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.wishDir());
            return spriteOrDefault(sprites, animations.currentFrame());
        }
        if (animations.isSelected(ArcadePacMan_AnimationID.GHOST_EYES)) {
            return spriteSheet().ghostEyesSprite(ghost.wishDir());
        }
        else {
            return animations.currentSprite();
        }
    }

    private FacingSprite computePacSprite(Pac pac) {
        final SpriteAnimationSet animations = pac.animations();
        final int frame = animations.currentFrame();
        final Direction dir = pac.moveDir();
        return switch (animations.selectedAnimationID()) {
            case ArcadePacMan_AnimationID.PAC_DYING    -> computePacDyingSprite(pac);
            case ArcadePacMan_AnimationID.PAC_MUNCHING -> facingSprite(SpriteID.MS_PAC_MUNCHING, frame, dir);
            case TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER -> facingSprite(SpriteID.MS_PAC_MUNCHING_BOOSTER, frame, dir);
            case TengenMsPacMan_AnimationID.MS_PAC_MAN_TURNING_AWAY -> facingSprite(SpriteID.MS_PAC_TURNING_AWAY, frame, dir);
            case TengenMsPacMan_AnimationID.MS_PAC_MAN_WAVING_HAND -> facingSprite(SpriteID.MS_PAC_WAVING_HAND, frame, dir);
            case TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING -> facingSprite(SpriteID.MR_PAC_MUNCHING, frame, dir);
            case TengenMsPacMan_AnimationID.MR_PAC_MAN_TURNING_AWAY -> facingSprite(SpriteID.MR_PAC_TURNING_AWAY, frame, dir);
            case TengenMsPacMan_AnimationID.MR_PAC_MAN_WAVING_HAND -> facingSprite(SpriteID.MR_PAC_WAVING_HAND, frame, dir);
            default -> new FacingSprite(animations.currentSprite(), pac.moveDir());
        };
    }

    private FacingSprite facingSprite(SpriteID spriteArrayID, int frame, Direction dir) {
        return new FacingSprite(spriteOrDefault(spriteSheet().sprites(spriteArrayID), frame), dir);
    }

    // Dying animation is realized by providing a sprite facing to the corresponding direction for each animation frame
    private FacingSprite computePacDyingSprite(Pac pac) {
        final var dyingAnimation = pac.animations().animation(ArcadePacMan_AnimationID.PAC_DYING);
        if (dyingAnimation instanceof SpriteAnimation spriteAnimation) {
            final Direction facingDir = switch (spriteAnimation.currentFrame()) {
                case 0, 4, 8  -> Direction.DOWN;
                case 1, 5, 9  -> Direction.LEFT;
                case 2, 6, 10 -> Direction.UP;
                case 3, 7     -> Direction.RIGHT;
                default       -> Direction.UP; // end position from frame 11 on
            };
            return new FacingSprite(spriteAnimation.currentSprite(), facingDir);
        } else {
            throw new IllegalArgumentException("No sprite animation set for Pac-Man dying");
        }
    }

    private RectShort computeBonusSprite(Bonus bonus) {
        return switch (bonus.state()) {
            case EDIBLE -> spriteOrDefault(spriteSheet().sprites(SpriteID.BONUS_SYMBOLS), bonus.symbol());
            // Note: sprite sheet has bonus values in wrong order!
            case EATEN -> spriteOrDefault(spriteSheet().sprites(SpriteID.BONUS_VALUES), bonusValueSpriteIndex(bonus.symbol()));
            case INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        clapperboard.sprite().ifPresent(sprite -> {
            double numberX = clapperboard.x() + 8, numberY = clapperboard.y() + 18; // baseline
            drawSpriteCentered(sprite, clapperboard.center());
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
        drawSpriteCentered(stork.animations().currentSprite(), stork.center());
        if (stork.isBagReleasedFromBeak()) {
            // Sprite sheet has no stork without bag under its beak so we over-paint the bag
            ctx.setFill(backgroundColor());
            ctx.fillRect(scaled(stork.x() - 13), scaled(stork.y() + 3), scaled(8), scaled(10));
        }
    }

    // Assumes the unrotated sprite is facing left (like Ms. Pac-Man sprite in the current sprite sheet).
    // When facing up or down, Ms. Pac-Man top of head is on the right.
    private void drawFacingSpriteCentered(FacingSprite facingSprite, Vector2f centerUnscaled) {
        ctx().save();
        ctx().translate(centerUnscaled.x() * scaling(), centerUnscaled.y() * scaling());
        switch (facingSprite.facingDirection()) {
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
}