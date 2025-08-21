/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.uilib.rendering;

import de.amr.pacmanfx.GameContext;
import de.amr.pacmanfx.model.actors.Actor;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

public abstract class GameLevelRenderer extends BaseRenderer {

    public GameLevelRenderer(Canvas canvas) {
        super(canvas);
    }

    /**
     * Draws an actor (Pac-Man, ghost, moving bonus, etc.) if it is visible.
     *
     * @param actor the actor to draw
     */
    public abstract void drawActor(Actor actor);

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
     * @param backgroundColor level background color
     * @param mazeBright if the maze is drawn as highlighted (flashing)
     * @param energizerBright if the blinking energizers are in their highlighted state
     */
    public abstract void drawGameLevel(GameContext gameContext, Color backgroundColor, boolean mazeBright, boolean energizerBright);
}
