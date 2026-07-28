/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.spriteanim.SpriteAnimation;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.tengenmspacman.gamescene.Clapperboard;
import de.amr.pacmanfx.tengenmspacman.gamescene.Stork;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer, ActorRenderer {

    private final SpriteAnimSystem animSystem;

    public TengenMsPacMan_ActorRenderer(SpriteAnimSystem animSystem, Canvas canvas) {
        super(canvas);
        this.animSystem = requireNonNull(animSystem);
    }

    @Override
    public SpriteAnimSystem animSystem() {
        return animSystem;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.visibility().isVisible()) return;

        final Vector2f center = WorldMovementSystem.computeCenter(actor);
        switch (actor) {
            case Bonus bonus -> drawSpriteCentered(computeBonusSprite(bonus), center);
            case Ghost ghost -> drawSpriteCentered(computeGhostSprite(ghost), center);
            case Pac pac -> drawFacingSpriteCentered(computePacSprite(pac), center);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case Stork stork -> drawStork(stork);
            default -> drawSpriteCentered(animSystem.currentSprite(actor), center);
        }
    }

    private RectShort computeGhostSprite(Ghost ghost) {
        if (animSystem.isSelected(ghost, CommonAnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.worldNavigation().wishDir());
            return spriteOrDefault(sprites, animSystem.currentFrame(ghost));
        }
        if (animSystem.isSelected(ghost, CommonAnimationID.GHOST_EYES)) {
            return spriteSheet().ghostEyesSprite(ghost.worldNavigation().wishDir());
        }
        else {
            return animSystem.currentSprite(ghost);
        }
    }

    private FacingSprite computePacSprite(Pac pac) {
        final int frame = animSystem.currentFrame(pac);
        final Direction dir = pac.worldMovement().moveDir();
        return switch (animSystem.selectedAnimationID(pac)) {
            case null -> throw new IllegalStateException("Could not determine Pac-sprite, no animation selected");
            case CommonAnimationID.PAC_DYING    -> computePacDyingSprite(pac);
            case CommonAnimationID.PAC_MUNCHING -> facingSprite(SpriteID.MS_PAC_MUNCHING, frame, dir);
            case TengenMsPacMan_AnimationID.MS_PAC_MAN_BOOSTER -> facingSprite(SpriteID.MS_PAC_MUNCHING_BOOSTER, frame, dir);
            case TengenMsPacMan_AnimationID.MS_PAC_MAN_TURNING_AWAY -> facingSprite(SpriteID.MS_PAC_TURNING_AWAY, frame, dir);
            case TengenMsPacMan_AnimationID.MS_PAC_MAN_WAVING_HAND -> facingSprite(SpriteID.MS_PAC_WAVING_HAND, frame, dir);
            case TengenMsPacMan_AnimationID.MR_PAC_MAN_MUNCHING -> facingSprite(SpriteID.MR_PAC_MUNCHING, frame, dir);
            case TengenMsPacMan_AnimationID.MR_PAC_MAN_TURNING_AWAY -> facingSprite(SpriteID.MR_PAC_TURNING_AWAY, frame, dir);
            case TengenMsPacMan_AnimationID.MR_PAC_MAN_WAVING_HAND -> facingSprite(SpriteID.MR_PAC_WAVING_HAND, frame, dir);
            default -> new FacingSprite(animSystem.currentSprite(pac), pac.worldMovement().moveDir());
        };
    }

    private FacingSprite facingSprite(SpriteID spriteArrayID, int frame, Direction dir) {
        return new FacingSprite(spriteOrDefault(spriteSheet().findSprites(spriteArrayID), frame), dir);
    }

    // Dying animation is realized by providing a sprite facing to the corresponding direction for each animation frame
    private FacingSprite computePacDyingSprite(Pac pac) {
        final var dyingAnimation = animSystem.animation(pac, CommonAnimationID.PAC_DYING);
        if (dyingAnimation instanceof SpriteAnimation spriteAnimation) {
            final Direction facingDir = switch (spriteAnimation.frame()) {
                case 0, 4, 8  -> Direction.DOWN;
                case 1, 5, 9  -> Direction.LEFT;
                case 2, 6, 10 -> Direction.UP;
                case 3, 7     -> Direction.RIGHT;
                default       -> Direction.UP; // end position from frame 11 on
            };
            return new FacingSprite(spriteAnimation.sprite(), facingDir);
        } else {
            throw new IllegalArgumentException("No sprite animation set for Pac-Man dying");
        }
    }

    private RectShort computeBonusSprite(Bonus bonus) {
        return switch (bonus.state()) {
            case EDIBLE -> spriteOrDefault(spriteSheet().findSprites(SpriteID.BONUS_SYMBOLS), bonus.symbolCode());
            // Note: sprite sheet has bonus values in wrong order!
            case EATEN -> spriteOrDefault(spriteSheet().findSprites(SpriteID.BONUS_VALUES),
                TengenMsPacMan_RenderConfig.bonusValueSpriteIndex(bonus.symbolCode()));
            case INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        clapperboard.sprite().ifPresent(sprite -> {
            double numberX = clapperboard.position().x + 8, numberY = clapperboard.position().y + 18; // baseline
            drawSpriteCentered(sprite, WorldMovementSystem.computeCenter(clapperboard));
            // over-paint number from sprite sheet
            ctx.save();
            ctx.scale(scaling(), scaling());
            ctx.setFill(backgroundColor());
            ctx.fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx.restore();

            ctx.setFont(arcadeFont8());
            ctx.setFill(NES_Palette.color(0x20));
            ctx.fillText(String.valueOf(clapperboard.number()), scaled(numberX), scaled(numberY));
            if (clapperboard.isTextVisible()) {
                double textX = clapperboard.position().x + sprite.width(), textY = clapperboard.position().y + 2;
                ctx.fillText(clapperboard.text(), scaled(textX), scaled(textY));
            }
        });
    }

    private void drawStork(Stork stork) {
        drawSpriteCentered(animSystem.currentSprite(stork), WorldMovementSystem.computeCenter(stork));
        if (stork.isBagReleasedFromBeak()) {
            // Sprite sheet has no stork without bag under its beak so we over-paint the bag
            ctx.setFill(backgroundColor());
            ctx.fillRect(scaled(stork.position().x - 13), scaled(stork.position().y + 3), scaled(8), scaled(10));
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