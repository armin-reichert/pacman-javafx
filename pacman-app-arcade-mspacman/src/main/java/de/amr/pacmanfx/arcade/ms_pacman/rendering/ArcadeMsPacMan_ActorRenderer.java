/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.ms_pacman.rendering;

import de.amr.pacmanfx.arcade.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.lib.math.Vector2f;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.model.actors.Bonus;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.SpriteRenderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_WHITE;
import static java.util.Objects.requireNonNull;

public class ArcadeMsPacMan_ActorRenderer extends BaseRenderer implements SpriteRenderer, ActorRenderer {

    public ArcadeMsPacMan_ActorRenderer(Canvas canvas) {
        super(canvas);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;

        switch (actor) {
            case Ghost ghost -> drawGhost(ghost);
            case Pac pac -> drawPac(pac);
            case Bonus bonus -> drawBonus(bonus);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            default -> drawSpriteCentered(actor.center(), actor.animations().currentSprite(actor));
        }
    }

    private void drawGhost(Ghost ghost) {
        drawSpriteCentered(ghost.center(), ghost.animations().currentSprite(ghost));

    }

    private void drawPac(Pac pac) {
        final Object animationID = pac.animations().selectedAnimationID();
        final int frame = pac.animations().frameIndex();
        if (animationID == Pac.AnimationID.PAC_MUNCHING) {
            final RectShort[] sprites = switch (pac.moveDir()) {
                case RIGHT -> spriteSheet().sprites(SpriteID.MS_PACMAN_MUNCHING_RIGHT);
                case LEFT  -> spriteSheet().sprites(SpriteID.MS_PACMAN_MUNCHING_LEFT);
                case UP    -> spriteSheet().sprites(SpriteID.MS_PACMAN_MUNCHING_UP);
                case DOWN  -> spriteSheet().sprites(SpriteID.MS_PACMAN_MUNCHING_DOWN);
            };
            drawSpriteCentered(pac.center(), sprites[frame]);
        }
        else if (animationID == ArcadeMsPacMan_PacAnimations.AnimationID.PAC_MAN_MUNCHING) {
            final RectShort[] sprites = spriteSheet().sprites(switch (pac.moveDir()) {
                case RIGHT -> SpriteID.MR_PACMAN_MUNCHING_RIGHT;
                case LEFT  -> SpriteID.MR_PACMAN_MUNCHING_LEFT;
                case UP    -> SpriteID.MR_PACMAN_MUNCHING_UP;
                case DOWN  -> SpriteID.MR_PACMAN_MUNCHING_DOWN;
            });
            drawSpriteCentered(pac.center(), sprites[frame]);
        }
        else {
            drawSpriteCentered(pac.center(), pac.animations().currentSprite(pac));
        }
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.CLAPPERBOARD);
        final int spriteIndex = clapperboard.state();
        if (0 <= spriteIndex && spriteIndex < sprites.length) {
            final RectShort sprite = sprites[spriteIndex];
            drawSpriteCentered(clapperboard.center(), sprite);
            // Draw number and title
            final double numberX = scaled(clapperboard.x() + sprite.width() - 25);
            final double textX = scaled(clapperboard.x() + sprite.width());
            final double y = scaled(clapperboard.y() + 18);
            ctx.setFont(clapperboard.font());
            ctx.setFill(ARCADE_WHITE);
            ctx.fillText(clapperboard.number(), numberX, y);
            ctx.fillText(clapperboard.text(), textX, y);
        }
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case EDIBLE -> drawBonusSprite(bonus.center().plus(0, bonus.verticalElongation()),
                spriteSheet().sprites(SpriteID.BONUS_SYMBOLS), bonus.symbol());
            case EATEN -> drawBonusSprite(bonus.center(),
                spriteSheet().sprites(SpriteID.BONUS_VALUES), bonus.symbol());
            case INACTIVE -> {}
        }
    }

    private void drawBonusSprite(Vector2f center, RectShort[] sprites, int spriteIndex) {
        if (0 <= spriteIndex && spriteIndex < sprites.length) {
            drawSpriteCentered(center, sprites[spriteIndex]);
        } else {
            Logger.error("Cannot render bonus with symbol code {}", spriteIndex);
        }
    }
}