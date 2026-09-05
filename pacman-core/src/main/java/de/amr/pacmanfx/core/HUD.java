package de.amr.pacmanfx.core;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.CreditDisplay;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

public class HUD {

    private boolean visible;

    private final QuerySet<GameEntity> entities = new QuerySet<>();

    public HUD(String variantName) {
        final var levelCounter = new LevelCounter();
        final var livesCounter = new LivesCounter();
        final var creditDisplay = new CreditDisplay();
        final var gameScore = new Score(Score.Type.GAME_SCORE);
        final var highScore = ScoreSystem.createHighScore(variantName);

        creditDisplay.pos().set(2 * TS, 36 * TS);

        gameScore.pos().set(TS, TS);
        highScore.pos().set(14 * TS, TS);
        highScore.show();

        entities.addAll(levelCounter, livesCounter, gameScore, highScore, creditDisplay);
    }

    public boolean isVisible() {
        return visible;
    }

    public void show() {
        visible = true;
    }

    public void hide() {
        visible = false;
    }

    public CreditDisplay creditDisplay() {
        return entities.theOne(CreditDisplay.class);
    }

    public LevelCounter levelCounter() {
        return entities.theOne(LevelCounter.class);
    }

    public LivesCounter livesCounter() {
        return entities.theOne(LivesCounter.class);
    }

    public Score gameScore() {
        return entities.selectWhere(Score.class, score -> score.type() == Score.Type.GAME_SCORE).findFirst().orElseThrow();
    }

    public Score highScore() {
        return entities.selectWhere(Score.class, score -> score.type() == Score.Type.HIGH_SCORE).findFirst().orElseThrow();
    }

    public QuerySet<GameEntity> entities() {
        return entities;
    }

    public void addEntity(GameEntity entity) {
        entities.add(entity);
    }

    public void removeEntity(GameEntity entity) {
        entities.remove(entity);
    }
}
