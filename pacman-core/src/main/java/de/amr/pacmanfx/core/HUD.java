package de.amr.pacmanfx.core;

import de.amr.basics.QuerySet;
import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.*;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.level.MessageType;

import java.util.List;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

public class HUD {

    private boolean visible;

    private final QuerySet<GameEntity> entities = new QuerySet<>();

    private final LevelCounter levelCounter;
    private final LivesCounter livesCounter;
    private final Score gameScore;
    private final Score highScore;
    private final MessageView messageView;
    private final CreditDisplay creditDisplay;

    public HUD(String variantName) {
        levelCounter = new LevelCounter();
        livesCounter = new LivesCounter();
        messageView = new MessageView();
        gameScore = new Score(Score.Type.GAME_SCORE);
        highScore = ScoreSystem.createHighScore(variantName);
        creditDisplay = new CreditDisplay();

        gameScore.pos().set(TS, TS);
        highScore.show();
        highScore.pos().set(14 * TS, TS);
        creditDisplay.pos().set(2 * TS, 36 * TS);

        entities.addAll(levelCounter, livesCounter, messageView, gameScore, highScore, creditDisplay);
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
        return creditDisplay;
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

    public MessageView messageView() {
        return messageView;
    }

    public void clearMessage() {
        messageView.data().setMessageType(MessageType.NO_MESSAGE);
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
