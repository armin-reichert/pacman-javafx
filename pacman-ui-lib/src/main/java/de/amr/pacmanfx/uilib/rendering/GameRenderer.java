/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.actors.Actor;
import de.amr.pacmanfx.uilib.GameClock;
import de.amr.pacmanfx.uilib.assets.SpriteSheet;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public abstract class GameRenderer extends BaseRenderer {

    /**
     * Draws an actor (Pac-Man, ghost, moving bonus, etc.) if it is visible.
     *
     * @param actor the actor to draw
     * @param spriteSheet sprite sheet
     */
    public void drawActor(Actor actor, SpriteSheet<?> spriteSheet) {
        requireNonNull(actor);
        if (!actor.isVisible()) return;
        actor.animations()
                .map(animationManager -> animationManager.currentSprite(actor))
                .ifPresent(actorSprite -> drawSpriteCentered(actor.center(), actorSprite));
    }

    /**
     * Applies settings specific to the given game level to this renderer. This can be for example
     * the selection of a different color scheme which is specified in the level map. The default
     * implementation is empty such that subclasses that have no such hints can silently ignore it.
     *
     * @param gameContext the game context
     */
    public void applyLevelSettings(GameContext gameContext) {}

    /**
     * @param gameContext the game context
     * @param gameClock the game clock (used for blinking effects)
     * @param backgroundColor level background color
     * @param mazeBright if the maze is drawn as highlighted (flashing)
     * @param energizerBright if the blinking energizers are in their highlighted state
     */
    public abstract void drawGameLevel(GameContext gameContext, GameClock gameClock, Color backgroundColor, boolean mazeBright, boolean energizerBright);
}
