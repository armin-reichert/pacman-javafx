/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.rendering;

import de.amr.pacmanfx.lib.Direction;
import de.amr.pacmanfx.lib.RectShort;
import de.amr.pacmanfx.lib.Vector2f;
import de.amr.pacmanfx.model.actors.*;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.Clapperboard;
import de.amr.pacmanfx.tengen.ms_pacman.scenes.Stork;
import de.amr.pacmanfx.uilib.animation.SpriteAnimation;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import de.amr.pacmanfx.uilib.rendering.BaseRenderer;
import de.amr.pacmanfx.uilib.rendering.Renderer;
import javafx.scene.canvas.Canvas;
import org.tinylog.Logger;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig.nesColor;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_ActorRenderer extends BaseRenderer implements ActorRenderer, Renderer {

    private final TengenMsPacMan_UIConfig uiConfig;

    public TengenMsPacMan_ActorRenderer(Canvas canvas, TengenMsPacMan_UIConfig uiConfig) {
        super(canvas);
        this.uiConfig = uiConfig;
    }

    @Override
    public TengenMsPacMan_SpriteSheet spriteSheet() {
        return uiConfig.spriteSheet();
    }

    @Override
    public void drawActor(Actor actor) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;

        switch (actor) {
            case Bonus bonus -> drawBonus(bonus);
            case Clapperboard clapperboard -> drawClapperBoard(clapperboard);
            case Pac pac -> drawPac(pac);
            case Stork stork -> drawStork(stork);
            default -> drawCurrentSprite(actor);
        }
    }

    // There are only left-pointing Ms. Pac-Man sprites in the sprite sheet, so we rotate and mirror in the renderer
    private void drawMovingActorSprite(MovingActor actor, Direction dir, RectShort sprite) {
        Vector2f center = actor.center().scaled(scaling());
        ctx().save();
        ctx().translate(center.x(), center.y());
        switch (dir) {
            case LEFT  -> {}
            case UP    -> ctx().rotate(90);
            case RIGHT -> ctx().scale(-1, 1);
            case DOWN  -> { ctx().scale(-1, 1); ctx().rotate(-90); }
        }
        drawSpriteCentered(0, 0, sprite);
        ctx().restore();
    }

    private void drawCurrentSprite(Actor actor) {
        actor.optAnimationManager()
            .map(animations -> animations.currentSprite(actor))
            .ifPresent(sprite -> drawSpriteCentered(actor.center(), sprite));
    }

    private void drawBonus(Bonus bonus) {
        switch (bonus.state()) {
            case INACTIVE -> {}
            case EDIBLE -> {
                RectShort[] sprites = uiConfig.spriteSheet().spriteSequence(SpriteID.BONUS_SYMBOLS);
                int index = bonus.symbol();
                if (0 <= index && index < sprites.length) {
                    ctx().save();
                    ctx().translate(0, bonus.jumpHeight());
                    drawSpriteCentered(bonus.center(), sprites[index]);
                    ctx().restore();
                }
            }
            case EATEN  -> {
                RectShort[] sprites = uiConfig.spriteSheet().spriteSequence(SpriteID.BONUS_VALUES);
                int index = bonus.symbol();
                if (0 <= index && index < sprites.length) {
                    ctx().save();
                    ctx().translate(0, bonus.jumpHeight());
                    drawSpriteCentered(bonus.center(), sprites[index]);
                    ctx().restore();
                }
            }
        }
    }

    private void drawPac(Pac pac) {
        //TODO check if this is the way to do this
        pac.optAnimationManager().map(SpriteAnimationManager.class::cast).ifPresent(spriteAnimations -> {
            SpriteAnimation spriteAnimation = spriteAnimations.currentAnimation();
            if (spriteAnimation == null) {
                Logger.error("No sprite animation found for {}", pac);
                return;
            }
            if (CommonAnimationID.ANIM_PAC_DYING.equals(spriteAnimations.selectedID())) {
                drawPacDyingAnimation(pac, spriteAnimation);
            } else {
                drawMovingActorSprite(pac, pac.moveDir(), spriteAnimation.currentSprite());
            }
        });
    }

    // Simulates dying animation by providing the right direction for each animation frame
    private void drawPacDyingAnimation(Pac pac, SpriteAnimation animation) {
        Direction dir = Direction.DOWN;
        if (animation.frameIndex() < 11) {
            dir = switch (animation.frameIndex() % 4) {
                case 1 -> Direction.LEFT;
                case 2 -> Direction.UP;
                case 3 -> Direction.RIGHT;
                default -> Direction.DOWN; // start with DOWN
            };
        }
        drawMovingActorSprite(pac, dir, animation.currentSprite());
    }

    private void drawClapperBoard(Clapperboard clapperboard) {
        requireNonNull(clapperboard);
        if (!clapperboard.isVisible()) return;
        clapperboard.sprite().ifPresent(sprite -> {
            double numberX = clapperboard.x() + 8, numberY = clapperboard.y() + 18; // baseline
            drawSpriteCentered(clapperboard.center(), sprite);
            // over-paint number from sprite sheet
            ctx().save();
            ctx().scale(scaling(), scaling());
            ctx().setFill(backgroundColor());
            ctx().fillRect(numberX - 1, numberY - 8, 12, 8);
            ctx().restore();

            ctx().setFont(clapperboard.font());
            ctx().setFill(nesColor(0x20));
            ctx().fillText(String.valueOf(clapperboard.number()), scaled(numberX), scaled(numberY));
            if (clapperboard.isTextVisible()) {
                double textX = clapperboard.x() + sprite.width(), textY = clapperboard.y() + 2;
                ctx().fillText(clapperboard.text(), scaled(textX), scaled(textY));
            }
        });
    }

    private void drawStork(Stork stork) {
        drawCurrentSprite(stork);
        if (stork.isBagReleasedFromBeak()) {
            // Sprite sheet has no stork without bag under its beak so we over-paint the bag
            ctx().setFill(backgroundColor());
            ctx().fillRect(scaled(stork.x() - 13), scaled(stork.y() + 3), scaled(8), scaled(10));
        }
    }
}