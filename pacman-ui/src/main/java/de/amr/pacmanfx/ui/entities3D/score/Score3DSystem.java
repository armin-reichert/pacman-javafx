/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.ui.entities3D.score;

import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.entities.hud.score.Score;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import de.amr.pacmanfx.ui.gamescene.playscene.PlayScene3D;

import static de.amr.basics.Tile.TS;
import static java.util.Objects.requireNonNull;

public class Score3DSystem {

    public void replaceScoresView(PlayScene3D playScene3D, Score gameScore, Score highScore, TranslationManager translationManager) {
        playScene3D.optComponent(ScoresView.class).ifPresent(scoresView ->  {
            playScene3D.assertView3D().root().getChildren().remove(scoresView.root());
            playScene3D.removeComponent(ScoresView.class);
        });

        if (!gameScore.hasComponent(ScoreView.class)) {
            gameScore.setComponent(ScoreView.class, new ScoreView());
        }
        final ScoreView gameScoreView = gameScore.assertComponent(ScoreView.class);
        gameScoreView.titleDisplay().setText(translationManager.translate("score.score"));

        if (!highScore.hasComponent(ScoreView.class)) {
            highScore.setComponent(ScoreView.class, new ScoreView());
        }
        final ScoreView highScoreView = highScore.assertComponent(ScoreView.class);
        highScoreView.titleDisplay().setText(translationManager.translate("score.high_score"));

        final var scoresView = new ScoresView(gameScoreView, highScoreView);
        scoresView.setFont(GlobalFonts.ARCADE.font(8));

        playScene3D.assertView3D().root().getChildren().add(scoresView.root());

        //scoresView.textOpacity.bind(scoreOpacity);

        // Scores must always face towards viewer, independent of current perspective:
        scoresView.root().rotationAxisProperty().bind(playScene3D.assertView3D().camera().rotationAxisProperty());
        scoresView.root().rotateProperty().bind(playScene3D.assertView3D().camera().rotateProperty());

        // Scores are shown slightly "behind" and over game level from viewer's perspective
        scoresView.root().translateXProperty().bind(playScene3D.assertView3D().level3DHolder().translateXProperty().add(TS));
        scoresView.root().translateYProperty().bind(playScene3D.assertView3D().level3DHolder().translateYProperty().subtract(4.5 * TS));
        scoresView.root().translateZProperty().bind(playScene3D.assertView3D().level3DHolder().translateZProperty().subtract(4.5 * TS));

        playScene3D.setComponent(ScoresView.class, scoresView);
    }

    public void updateScoresView(ScoresView scoresView, Score gameScore, Score highScore, TranslationManager translationManager, AssetMap assets) {
        requireNonNull(scoresView);
        requireNonNull(gameScore);
        requireNonNull(highScore);
        requireNonNull(translationManager);
        requireNonNull(assets);

        // If score is disabled, show "GAME OVER" text instead
        if (gameScore.data().isEnabled()) {
            scoresView.showScore(gameScore.data().points(), gameScore.data().levelNumber());
        } else {
            scoresView.showTextForScore(translationManager.translate("score.game_over"), assets.color("color.game_over_message"));
        }

        // High score is always visible
        scoresView.showHighScore(highScore.data().points(), highScore.data().levelNumber());
    }
}
