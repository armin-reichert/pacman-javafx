/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.model.actors;

import de.amr.pacmanfx.model.GameLevel;

public interface ActorSpeedControl {

    /**
     * Returns Pac‑Man's bonus speed for the given level.
     *
     * @param level the current level
     * @return the bonus speed multiplier
     */
    float bonusSpeed(GameLevel level);

    /**
     * Returns the speed of the given ghost for the given level.
     *
     * @param level the current level
     * @param ghost the ghost whose speed is requested
     * @return the ghost's speed multiplier
     */
    float ghostSpeed(GameLevel level, Ghost ghost);

    /**
     * Returns the ghost's speed while attacking.
     *
     * @param level the current level
     * @param ghost a ghost
     * @return the ghost's speed (pixels per frame) while attacking
     */
    float ghostSpeedAttacking(GameLevel level, Ghost ghost);

    /**
     * Returns the speed of frightened ghosts.
     *
     * @param level the current level
     * @return the speed of frightened ghosts (pixels per frame)
     */
    float ghostSpeedFrightened(GameLevel level);

    /**
     * Returns the ghost's speed inside a tunnel for the given level.
     *
     * @param levelNumber the level number
     * @return the ghost's tunnel speed (pixels per frame)
     */
    float ghostSpeedTunnel(int levelNumber);


    /**
     * Returns Pac‑Man's normal speed for the given level.
     *
     * @param level the current level
     * @return Pac‑Man's speed multiplier
     */
    float pacSpeed(GameLevel level);

    /**
     * Returns Pac‑Man's speed while he has power (after eating a power pellet).
     *
     * @param level the current level
     * @return Pac‑Man's powered‑up speed multiplier
     */
    float pacSpeedWhenHasPower(GameLevel level);
}
