/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman.rendering;

import de.amr.pacmanfx.lib.math.Direction;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengenmspacman.scenes.Clapperboard;
import de.amr.pacmanfx.tengenmspacman.scenes.Stork;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationMap;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.bonusValueSpriteIndex;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID.*;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_ActorRenderer extends BaseRenderer implements SpriteRendererMixin, ActorRenderer {

    private record DirectedSprite(RectShort sprite, Direction dir) {}

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
            case Pac pac -> drawPac(pac);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case Stork stork -> drawStork(stork);
            default -> drawSpriteCentered(actor.animations().currentSprite(), actor.center());
        }
    }

    private RectShort computeGhostSprite(Ghost ghost) {
        final AnimationSet animations = ghost.animations();
        if (animations.isSelected(Ghost.AnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.wishDir());
            return sprites[animations.frameIndex()];
        }
        else if (animations.isSelected(Ghost.AnimationID.GHOST_EYES)) {
            final RectShort[] sprites = spriteSheet().ghostEyesSprites(ghost.wishDir());
            return sprites[animations.frameIndex()];
        }
        else {
            return animations.currentSprite();
        }
    }

    private void drawPac(Pac pac) {
        final AnimationSet animations = pac.animations();
        final int frame = animations.frameIndex();
        DirectedSprite sprite;
        if (animations.isSelected(Pac.AnimationID.PAC_DYING) && animations instanceof SpriteAnimationMap<?> sam) {
            sprite = new DirectedSprite(sam.currentSprite(), computePacDyingSpriteDir(pac));
        }
        else if (animations.isSelected(Pac.AnimationID.PAC_MUNCHING)) {
            sprite = new DirectedSprite(spriteSheet().sprites(SpriteID.MS_PAC_MUNCHING)[frame], pac.moveDir());
        }
        else if (animations.isSelected(ANIM_MS_PAC_MAN_BOOSTER)) {
            sprite = new DirectedSprite(spriteSheet().sprites(SpriteID.MS_PAC_MUNCHING_BOOSTER)[frame], pac.moveDir());
        }
        else if (animations.isSelected(ANIM_MS_PAC_MAN_TURNING_AWAY)) {
            sprite = new DirectedSprite(spriteSheet().sprites(SpriteID.MS_PAC_TURNING_AWAY)[frame], pac.moveDir());
        }
        else if (animations.isSelected(ANIM_MS_PAC_MAN_WAVING_HAND)) {
            sprite = new DirectedSprite(spriteSheet().sprites(SpriteID.MS_PAC_WAVING_HAND)[frame], pac.moveDir());
        }
        else if (animations.isSelected(ANIM_PAC_MAN_MUNCHING)) {
            sprite = new DirectedSprite(spriteSheet().sprites(SpriteID.MR_PAC_MUNCHING)[frame], pac.moveDir());
        }
        else if (animations.isSelected(ANIM_PAC_MAN_TURNING_AWAY)) {
            sprite = new DirectedSprite(spriteSheet().sprites(SpriteID.MR_PAC_TURNING_AWAY)[frame], pac.moveDir());
        }
        else if (animations.isSelected(ANIM_PAC_MAN_WAVING_HAND)) {
            sprite = new DirectedSprite(spriteSheet().sprites(SpriteID.MR_PAC_WAVING_HAND)[frame], pac.moveDir());
        }
        else {
            sprite = new DirectedSprite(animations.currentSprite(), pac.moveDir());
        }
        drawSpriteCenteredRotatedByDir(sprite.sprite(), pac.center().scaled(scaling()), sprite.dir());
    }

    // Dying animation is realized by providing a sprite direction for each animation frame
    private Direction computePacDyingSpriteDir(Pac pac) {
        final var dyingAnimation = pac.animations().animation(Pac.AnimationID.PAC_DYING);
        if (dyingAnimation instanceof SpriteAnimation spriteAnimation) {
            return switch (spriteAnimation.currentFrameIndex()) {
                case 0, 4, 8  -> Direction.DOWN;
                case 1, 5, 9  -> Direction.LEFT;
                case 2, 6, 10 -> Direction.UP;
                case 3, 7     -> Direction.RIGHT;
                default       -> Direction.UP; // end position frame 11...
            };
        }
        throw new IllegalArgumentException("No sprite animation set for Pac-Man dying");
    }

    private RectShort computeBonusSprite(Bonus bonus) {
        return switch (bonus.state()) {
            case EDIBLE -> spriteSheet().sprites(SpriteID.BONUS_SYMBOLS)[bonus.symbol()];
            // Note: sprite sheet has bonus values in wrong order!
            case EATEN -> spriteSheet().sprites(SpriteID.BONUS_VALUES)[bonusValueSpriteIndex(bonus.symbol())];
            case INACTIVE -> RectShort.ZERO;
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
}