/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.rendering;

import de.amr.basics.math.Direction;
import de.amr.basics.math.RectShort;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.ecs.systems.ActorSpriteAnimController;
import de.amr.pacmanfx.core.entities.Bonus;
import de.amr.pacmanfx.core.entities.CommonSpriteAnimationID;
import de.amr.pacmanfx.core.entities.Ghost;
import de.amr.pacmanfx.core.entities.Pac;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;

import static java.util.Objects.requireNonNull;

public class ArcadePacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer, ActorRenderer {

    private final ActorSpriteAnimController animSystem;

    public ArcadePacMan_ActorRenderer(ActorSpriteAnimController animSystem, Canvas canvas) {
        super(canvas);
        this.animSystem = requireNonNull(animSystem);
    }

    @Override
    public ArcadePacMan_SpriteSheet spriteSheet() {
        return ArcadePacMan_SpriteSheet.instance();
    }

    @Override
    public void drawActor(GameEntity actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;
        drawSpriteCentered(computeSprite(animSystem, actor), actor.pos().bodyCenter());
    }

    private RectShort computeSprite(ActorSpriteAnimController animSystem, GameEntity actor) {
        return switch (actor) {
            case Pac pac -> computePacSprite(animSystem, pac);
            case Ghost ghost -> computeGhostSprite(animSystem, ghost);
            case Bonus bonus -> computeBonusSprite(bonus);
            default -> animSystem.currentSprite(actor);
        };
    }

    private RectShort computePacSprite(ActorSpriteAnimController animSystem, Pac pac) {
        if (animSystem.isSelected(pac, CommonSpriteAnimationID.PAC_MOUTH_MOVING)) {
            final Direction dir = pac.worldNavigation().moveDir();
            final RectShort[] sprites = spriteSheet().pacMunchingSprites(dir);
            return spriteOrDefault(sprites, animSystem.currentFrame(pac));
        }
        else {
            return animSystem.currentSprite(pac);
        }
    }

    private RectShort computeGhostSprite(ActorSpriteAnimController animSystem, Ghost ghost) {
        if (animSystem.isSelected(ghost, CommonSpriteAnimationID.GHOST_NORMAL)) {
            final RectShort[] sprites = spriteSheet().ghostNormalSprites(ghost.personality(), ghost.worldNavigation().wishDir());
            return spriteOrDefault(sprites, animSystem.currentFrame(ghost));
        }
        else if (animSystem.isSelected(ghost, CommonSpriteAnimationID.GHOST_EYES)) {
            return spriteSheet().ghostEyesSprite(ghost.worldNavigation().wishDir());
        }
        else {
            return animSystem.currentSprite(ghost);
        }
    }

    private RectShort computeBonusSprite(Bonus bonus) {
        //TODO: decouple symbol code from index in sprite array
        return switch (bonus.bonusState()) {
            case EDIBLE   -> spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_SYMBOLS), bonus.data().symbolCode());
            case EATEN    -> spriteOrDefault(spriteSheet().findSpriteSequence(SpriteID.BONUS_VALUES),  bonus.data().symbolCode());
            case INACTIVE -> RectShort.NULL_RECTANGLE;
        };
    }
}