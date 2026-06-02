package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.Bonus;

public class TengenMsPacMan_GameRules implements GameRules {
    @Override
    public int pointsForPellet() {
        return 10;
    }

    @Override
    public int pointsForEnergizer() {
        return 50;
    }

    @Override
    public int pointsForBonus(Bonus bonus) {
        return 0;
    }
}
