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
import de.amr.pacmanfx.uilib.rendering.SpriteRendererMixin;
import javafx.scene.canvas.Canvas;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.nesColor;
import static de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_AnimationID.*;
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
            case Bonus bonus -> drawBonus(bonus);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case Ghost ghost -> drawSpriteCentered(computeGhostSprite(ghost), ghost.center());
            case Pac pac -> drawPac(pac);
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
            sprite = animations.currentSprite();
        }
        drawSpriteCenteredRotatedByDir(sprite, pac.center().scaled(scaling()), pac.moveDir());
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
        drawSpriteCenteredRotatedByDir(sam.currentSprite(), pac.center().scaled(scaling()), dir);
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> {
                final int index = bonus.symbol(); //TODO decouple
                final RectShort[] sprites = spriteSheet().sprites(SpriteID.BONUS_SYMBOLS);
                // The Up-Down animation of the moving bonus changes the center of drawing
                final Vector2f center = bonus.center().plus(0, bonus.verticalElongation());
                drawSpriteCentered(sprites[index], center);
            }
            case EATEN  -> {
                // Note: sprite sheet has bonus values in wrong order!
                final int index = TengenMsPacMan_UIConfig.bonusValueSpriteIndex(bonus.symbol());
                final RectShort[] sprites = spriteSheet().sprites(SpriteID.BONUS_VALUES);
                drawSpriteCentered(sprites[index], bonus.center());
            }
            case INACTIVE -> {}
        }
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