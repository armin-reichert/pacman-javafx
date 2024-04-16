/*
Copyright (c) 2021-2023 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.model.actors;

import de.amr.games.pacman.model.GameModel;

/**
 * @author Armin Reichert
 */
public interface Bonus {

    byte STATE_INACTIVE = 0;
    byte STATE_EDIBLE = 1;
    byte STATE_EATEN = 2;

    default String stateName() {
        return switch (state()) {
            case STATE_EATEN -> "eaten";
            case STATE_EDIBLE -> "edible";
            case STATE_INACTIVE -> "inactive";
            default -> throw new IllegalStateException("Unknown bonus state: " + state());
        };
    }

    /**
     * @return Entity representing this bonus in the world.
     */
    Entity entity();

    /**
     * @return the symbol of this bonus.
     */
    byte symbol();

    /**
     * @return points earned for eating this bonus
     */
    int points();

    /**
     * @return state of the bonus
     */
    byte state();

    /**
     * Updates the bonus state.
     *
     * @param game current game variant
     */
    void update(GameModel game);

    /**
     * Changes the bonus state to STATE_INACTIVE.
     */
    void setInactive();

    /**
     * Changes the bonus state to STATE_EATEN.
     *
     * @param ticks how long the bonus stays in eaten state
     */
    void setEaten(long ticks);

    /**
     * Changes the bonus state to STATE_EDIBLE.
     *
     * @param ticks how long the bonus stays in edible state
     */
    void setEdible(long ticks);
}