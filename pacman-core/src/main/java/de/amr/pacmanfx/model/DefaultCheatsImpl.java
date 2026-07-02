package de.amr.pacmanfx.model;

import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.score.Score;

public class DefaultCheatsImpl extends GameCheats {

    @Override
    public void update(GameLevel level) {
        if (level.isDemoLevel() || !level.gameModel().isPlaying()) {
            return;
        }
        final Pac pac = level.entities().pac();
        pac.immuneProperty().set(isPacImmune());
        pac.usingAutopilotProperty().set(isPacUsingAutopilot());
        if (isPacImmune() || isPacUsingAutopilot()) {
            notifyCheatUsed();
        }
    }

    public void handleCheatDetected(Score highScore) {
        if (highScore != null) {
            highScore.setEnabled(false);
        }
    }
}
