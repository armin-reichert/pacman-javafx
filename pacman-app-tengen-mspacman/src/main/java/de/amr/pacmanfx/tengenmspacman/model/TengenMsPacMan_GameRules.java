package de.amr.pacmanfx.tengenmspacman.model;

import de.amr.pacmanfx.model.GameRules;
import de.amr.pacmanfx.model.actors.Bonus;

public class TengenMsPacMan_GameRules implements GameRules {

    private final TengenMsPacMan_GameModel gameModel;

    public TengenMsPacMan_GameRules(TengenMsPacMan_GameModel gameModel) {
        this.gameModel = gameModel;
    }

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

    /*
     * See https://tcrf.net/Ms._Pac-Man_(NES,_Tengen):
     *
     * Humorously, instead of adding a check to disable multiple extra lives,
     * the "Arcade" maze set sets the remaining 3 extra life scores to over 970,000 points,
     * a score normally unachievable without cheat codes, since all maze sets end after 32 stages.
     * This was most likely done to simulate the Arcade game only giving one extra life per game.
     */
    @Override
    public boolean isExtraLifeAwarded(int oldScore, int newScore) {
        if (gameModel.mapCategory() == MapCategory.ARCADE) {
            return crossedScoreLine(oldScore, newScore, 10_000)
                || crossedScoreLine(oldScore, newScore, 970_000)
                || crossedScoreLine(oldScore, newScore, 980_000)
                || crossedScoreLine(oldScore, newScore, 990_000);
        }
        else {
            return crossedScoreLine(oldScore, newScore, 10_000)
                || crossedScoreLine(oldScore, newScore, 50_000)
                || crossedScoreLine(oldScore, newScore, 100_000)
                || crossedScoreLine(oldScore, newScore, 300_000);
        }
    }
}
