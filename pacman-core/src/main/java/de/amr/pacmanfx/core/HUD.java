package de.amr.pacmanfx.core;

import de.amr.pacmanfx.core.ecs.GameEntity;
import de.amr.pacmanfx.core.entities.LevelCounter;
import de.amr.pacmanfx.core.entities.LivesCounter;
import de.amr.pacmanfx.core.entities.MessageView;
import de.amr.pacmanfx.core.entities.Score;
import de.amr.pacmanfx.core.entities.score.system.ScoreSystem;
import de.amr.pacmanfx.core.level.MessageType;

import java.util.List;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

public class HUD {

    private boolean visible;
    private boolean creditVisible;

    private final LevelCounter levelCounter;
    private final LivesCounter livesCounter;
    private final Score gameScore;
    private final Score highScore;
    private final MessageView messageView;

    public HUD(String variantName) {
        levelCounter = new LevelCounter();
        livesCounter = new LivesCounter();
        messageView = new MessageView();
        gameScore = new Score(Score.Type.GAME_SCORE);
        highScore = ScoreSystem.createHighScore(variantName);

        gameScore.pos().set(TS, TS);
        highScore.show();
        highScore.pos().set(14 * TS, TS);
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

    public boolean isCreditVisible() {
        return creditVisible;
    }

    public void showCredit() {
        creditVisible = true;
    }

    public void hideCredit() {
        creditVisible = false;
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

    public List<GameEntity> entities() {
        return List.of(levelCounter, livesCounter, gameScore, highScore, messageView);
    }
}
