package de.amr.pacmanfx.core;

import de.amr.basics.QuerySet;
import de.amr.basics.ecs.GameEntity;
import de.amr.basics.ui.entities.hud.levelCounter.LevelCounter;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.basics.ui.entities.hud.score.Score;
import de.amr.basics.ui.entities.props.textdisplay.TextView;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.hud.ScoreSystem;

import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

public class HUD {

    private final LevelCounter levelCounter = new LevelCounter();
    private final LivesCounter livesCounter = new LivesCounter();
    private final TextView creditDisplay = new TextView();
    private final Score gameScore = new Score(Score.Type.GAME_SCORE);
    private Score highScore;

    private final QuerySet<GameEntity> additionalEntities = new QuerySet<>();

    public HUD() {
    }

    public HUD(String variantName) {
        creditDisplay.setName("Credits");
        creditDisplay.pos().set(2 * TS, 36 * TS);

        gameScore.pos().set(TS, TS);

        highScore = ScoreSystem.createHighScore(variantName);
        highScore.pos().set(14 * TS, TS);
        highScore.show();
    }

    public LevelCounter levelCounter() {
        return levelCounter;
    }

    public LivesCounter livesCounter() {
        return livesCounter;
    }

    public Score gameScore() {
        return gameScore;
    }

    public Score highScore() {
        return highScore;
    }

    public TextView creditDisplay() {
        return creditDisplay;
    }

    public void addAdditionalEntities(GameEntity... entities) {
        additionalEntities.addAll(entities);
    }

    public Stream<GameEntity> allEntities() {
        return Ufx.streamOf(
            levelCounter,
            livesCounter,
            creditDisplay,
            gameScore,
            highScore,
            additionalEntities.all()
        );
    }

    public QuerySet<GameEntity> additionalEntities() {
        return additionalEntities;
    }
}