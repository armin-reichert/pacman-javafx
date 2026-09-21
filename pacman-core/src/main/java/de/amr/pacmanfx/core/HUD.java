package de.amr.pacmanfx.core;

import de.amr.basics.QuerySet;
import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.entities.props.textdisplay.TextDisplay;
import de.amr.pacmanfx.core.entities.hud.levelCounter.LevelCounter;
import de.amr.pacmanfx.core.entities.hud.livescounter.LivesCounter;
import de.amr.pacmanfx.core.entities.hud.score.Score;
import de.amr.pacmanfx.core.entities.hud.score.ScoreSystem;
import de.amr.basics.rendering.Renderable;

import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

public class HUD {

    private final QuerySet<GameEntity> entities = new QuerySet<>();

    public HUD() {}

    public HUD(String variantName) {
        final var levelCounter = new LevelCounter();
        final var livesCounter = new LivesCounter();
        final var creditDisplay = new TextDisplay();
        final var gameScore = new Score(Score.Type.GAME_SCORE);
        final var highScore = ScoreSystem.createHighScore(variantName);

        creditDisplay.setName("Credits");
        creditDisplay.pos().set(2 * TS, 36 * TS);

        gameScore.pos().set(TS, TS);

        highScore.pos().set(14 * TS, TS);
        highScore.show();

        entities.addAll(levelCounter, livesCounter, gameScore, highScore, creditDisplay);
    }

    public Stream<Renderable> renderables() {
        return entities.all()
            .filter(GameEntity::isVisible)
            .filter(Renderable.class::isInstance).map(Renderable.class::cast);
    }

    public TextDisplay creditDisplay() {
        return entities.ofTypeWhere(TextDisplay.class, e -> "Credits".equals(e.name())).findAny().orElseThrow();
    }

    public LevelCounter levelCounter() {
        return entities.theOne(LevelCounter.class);
    }

    public LivesCounter livesCounter() {
        return entities.theOne(LivesCounter.class);
    }

    public Score gameScore() {
        return entities.ofTypeWhere(Score.class, score -> score.type() == Score.Type.GAME_SCORE).findFirst().orElseThrow();
    }

    public Score highScore() {
        return entities.ofTypeWhere(Score.class, score -> score.type() == Score.Type.HIGH_SCORE).findFirst().orElseThrow();
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
