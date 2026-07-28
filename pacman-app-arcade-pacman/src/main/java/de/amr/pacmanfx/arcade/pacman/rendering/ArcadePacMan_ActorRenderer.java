/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.model.actors.*;
import de.amr.pacmanfx.core.model.systems.common.WorldMovementSystem;
import de.amr.pacmanfx.core.model.systems.spriteanim.SpriteAnimSystem;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer, ActorRenderer {

    private final SpriteAnimSystem animSystem;

    public ArcadePacMan_ActorRenderer(SpriteAnimSystem animSystem, Canvas canvas) {
        super(canvas);
        this.animSystem = requireNonNull(animSystem);
    }

    @Override
    public SpriteAnimSystem animSystem() {
        return animSystem;
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.visibility().isVisible()) return;
        drawSpriteCentered(computeSprite(animSystem, actor), WorldMovementSystem.computeCenter(actor));
    }

    private RectShort computeSprite(SpriteAnimSystem animSystem, Actor actor) {
        return switch (actor) {
            case Pac pac -> computePacSprite(animSystem, pac);
            case Ghost ghost -> computeGhostSprite(animSystem, ghost);
            case Bonus bonus -> computeBonusSprite(bonus);
            default -> animSystem.currentSprite(actor);
        };
    }

    private RectShort computePacSprite(SpriteAnimSystem animSystem, Pac pac) {
        if (animSystem.isSelected(pac, CommonAnimationID.PAC_MUNCHING)) {
            final Direction dir = pac.worldNavigation().moveDir();
            final RectShort[] sprites = spriteSheet().pacMunchingSprites(dir);
            return spriteOrDefault(sprites, animSystem.currentFrame(pac));
        }
        else {
            return animSystem.currentSprite(pac);
        }
    }

    private RectShort computeGhostSprite(SpriteAnimSystem animSystem, Ghost ghost) {
        if (animSystem.isSelected(ghost, CommonAnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.worldNavigation().wishDir());
            return spriteOrDefault(sprites, animSystem.currentFrame(ghost));
        }
        else if (animSystem.isSelected(ghost, CommonAnimationID.GHOST_EYES)) {
            return spriteSheet().ghostEyesSprite(ghost.worldNavigation().wishDir());
        }
        else {
            return animSystem.currentSprite(ghost);
        }
    }

    private RectShort computeBonusSprite(Bonus bonus) {
        //TODO: decouple symbol code from index in sprite array
        return switch (bonus.state()) {
            case EDIBLE   -> spriteOrDefault(spriteSheet().findSprites(SpriteID.BONUS_SYMBOLS), bonus.symbolCode());
            case EATEN    -> spriteOrDefault(spriteSheet().findSprites(SpriteID.BONUS_VALUES),  bonus.symbolCode());
            case INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }
}