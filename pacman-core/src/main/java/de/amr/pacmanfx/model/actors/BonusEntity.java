/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.model.actors;

public interface BonusEntity {

    enum BonusState { INACTIVE, EDIBLE, EATEN }

    /**
     * @return actor representing this bonus.
     */
    Actor actor();

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
    BonusState state();

    /**
     * Updates the bonus state.
     */
    void update();

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
    void setEdibleTicks(long ticks);
}