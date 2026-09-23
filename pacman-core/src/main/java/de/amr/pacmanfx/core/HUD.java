package de.amr.pacmanfx.core;

import de.amr.basics.QuerySet;
import de.amr.basics.ui.ecs.GameEntity;
import de.amr.basics.ui.entities.hud.levelCounter.LevelCounter;
import de.amr.basics.ui.entities.hud.livescounter.LivesCounter;
import de.amr.basics.ui.entities.hud.score.Score;
import de.amr.basics.ui.entities.props.textdisplay.TextDisplay;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.entities.hud.ScoreSystem;

import java.util.List;
import java.util.stream.Stream;

import static de.amr.pacmanfx.core.model.world.map.WorldMap.TS;

public class HUD {

    private final LevelCounter levelCounter = new LevelCounter();
    private final LivesCounter livesCounter = new LivesCounter();
    private final TextDisplay creditDisplay = new TextDisplay();
    private final Score gameScore = new Score(Score.Type.GAME_SCORE);
    private Score highScore;

    private final QuerySet<GameEntity> additionalEntities = new QuerySet<>();

    private List<Renderable> renderables = List.of();

    public HUD() {
    }

    public HUD(String variantName) {
        creditDisplay.setName("Credits");
        creditDisplay.pos().set(2 * TS, 36 * TS);

        gameScore.pos().set(TS, TS);

        highScore = ScoreSystem.createHighScore(variantName);
        highScore.pos().set(14 * TS, TS);
        highScore.show();

        updateRenderables();
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

    public Stream<Renderable> renderables() {
        return renderables.stream();
    }

    public void updateRenderables() {
        renderables = Ufx.streamOf(
            levelCounter,
            livesCounter,
            creditDisplay,
            gameScore,
            highScore,
            additionalEntities.all()
        )
            .map(e -> new GameEntityView((GameEntity) e, RenderingLayer.HUD, 0))
            .map(Renderable.class::cast)
            .toList();
    }

    public TextDisplay creditDisplay() {
        return creditDisplay;
    }

    public void addAdditionalEntities(GameEntity... entities) {
        additionalEntities.addAll(entities);
        updateRenderables();
    }

    public QuerySet<GameEntity> additionalEntities() {
        return additionalEntities;
    }
}