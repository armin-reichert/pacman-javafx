/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.systems;

import de.amr.basics.ui.assets.AssetMap;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.entities.hud.score.Score;
import de.amr.pacmanfx.tengenmspacman.rendering.NES_Palette;
import de.amr.pacmanfx.ui.entities3D.score.Score3DSystem;
import de.amr.pacmanfx.ui.entities3D.score.ScoresView;
import javafx.scene.paint.Color;

import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_Score3DSystem extends Score3DSystem {

    public TengenMsPacMan_Score3DSystem() {
    }

    @Override
    public void updateScoresView(ScoresView scoresView, Score gameScore, Score highScore, TranslationManager translationManager, AssetMap assets) {
        requireNonNull(scoresView);
        requireNonNull(gameScore);
        requireNonNull(highScore);
        requireNonNull(translationManager);
        requireNonNull(assets);

        if (gameScore.data().isEnabled()) {
            scoresView.showScore(gameScore.data().points(), gameScore.data().levelNumber());
        } else {
            scoresView.showTextForScore(translationManager.translate("score.game_over"), Color.valueOf(NES_Palette.rgb(0x16)));
        }
        // Always show high score
        scoresView.showHighScore(highScore.data().points(), highScore.data().levelNumber());
    }
}
