/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.Stork;
import de.amr.pacmanfx.core.spriteanim.SpriteAnimation;
import de.amr.pacmanfx.tengenmspacman.entities.clapperboard.TengenMsPacMan_ClapperboardAnimationSystem;
import de.amr.pacmanfx.tengenmspacman.sprites.SpriteID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_AnimationID;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_SpriteSheet;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.FacingSprite;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import java.util.Arrays;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer {

    // These arrays must be sorted!
    private static final int[] GHOST_POINTS = { 200, 400, 800, 1600 };
    private static final int[] BONUS_POINTS = { 100, 200, 500, 700, 1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000 };

    private final ActorSpriteAnimController animSystem;

    public TengenMsPacMan_ActorRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);
        this.animSystem = requireNonNull(animSystem);
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return TengenMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void render(Object r, long tick) {
        if (!(r instanceof GameEntity actor)) {
            return;
        }

        if (!actor.isVisible()) return;

        final Vector2f center = actor.pos().bodyCenter();
        switch (actor) {
            case Bonus bonus -> drawSpriteCentered(computeSprite(bonus), center);
            case BonusPoints points -> drawSpriteCentered(computeSprite(points), center);
            case Ghost ghost -> drawSpriteCentered(computeSprite(ghost), center);
            case GhostPoints points -> drawSpriteCentered(computeSprite(points), center);
            case Pac pac -> drawFacingSpriteCentered(computeSprite(pac), center);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case Stork stork -> drawStork(stork);
            default -> drawSpriteCentered(animSystem.currentSprite(actor), center);
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
        return new FacingSprite(spriteOrDefault(spriteSheet().findSpriteSequence(spriteArrayID), frame), dir);
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
            return spriteOrDefault(sprites, animSystem.currentFrame(ghost));
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
            case EDIBLE -> spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN, INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }

    private RectShort computeSprite(BonusPoints bonusPoints) {
        final int index = Arrays.binarySearch(BONUS_POINTS, bonusPoints.points().number());
        return index >= 0 ? spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES)[index] : RectShort.NULL_RECTANGLE;
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        TengenMsPacMan_ClapperboardAnimationSystem.sprite(clapperboard).ifPresent(sprite -> {
            double numberX = clapperboard.pos().x() + 8, numberY = clapperboard.pos().y() + 18; // baseline
            drawSpriteCentered(sprite, clapperboard.pos().bodyCenter());
            // over-paint number from sprite sheet
            ctx.save();
            ctx.scale(scaling(), scaling());
            ctx.setFill(backgroundColor());
            ctx.fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx.restore();

            ctx.setFont(arcadeFont8());
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
            // Sprite sheet has no stork without bag under its beak so we over-paint the bag
            ctx.setFill(backgroundColor());
            ctx.fillRect(scaled(stork.pos().x() - 13), scaled(stork.pos().y() + 3), scaled(8), scaled(10));
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
}