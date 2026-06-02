package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.Bonus;

public interface GameRules {

    int pointsForPellet();

    int pointsForEnergizer();

    int pointsForBonus(Bonus bonus);
}
