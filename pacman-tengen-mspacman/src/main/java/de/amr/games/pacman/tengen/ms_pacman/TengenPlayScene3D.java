/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.tengen.ms_pacman;

import de.amr.games.pacman.event.GameEvent;
import de.amr.games.pacman.lib.nes.NES_ColorScheme;
import de.amr.games.pacman.lib.nes.NES_JoypadButton;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.model.Score;
import de.amr.games.pacman.model.ScoreManager;
import de.amr.games.pacman.ui2d.action.GameActions2D;
import de.amr.games.pacman.ui3d.GameActions3D;
import de.amr.games.pacman.ui3d.level.Bonus3D;
import de.amr.games.pacman.ui3d.scene3d.PlayScene3D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

import static de.amr.games.pacman.ui2d.action.GameActions2D.bindCheatActions;
import static de.amr.games.pacman.ui2d.action.GameActions2D.bindFallbackPlayerControlActions;
import static de.amr.games.pacman.ui2d.input.Keyboard.alt;

public class TengenPlayScene3D extends PlayScene3D {

    @Override
    public void bindGameActions() {
        bind(GameActions3D.PREV_PERSPECTIVE, alt(KeyCode.LEFT));
        bind(GameActions3D.NEXT_PERSPECTIVE, alt(KeyCode.RIGHT));
        if (context.game().isDemoLevel()) {
            bind(MsPacManGameTengenActions.QUIT_DEMO_LEVEL, context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_START));
        }
        else {
            bind(GameActions2D.PLAYER_UP,    context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_UP));
            bind(GameActions2D.PLAYER_DOWN,  context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_DOWN));
            bind(GameActions2D.PLAYER_LEFT,  context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_LEFT));
            bind(GameActions2D.PLAYER_RIGHT, context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_RIGHT));
            bind(MsPacManGameTengenActions.TOGGLE_PAC_BOOSTER,
                context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_A),
                context.currentJoypadKeyBinding().key(NES_JoypadButton.BTN_B));
            bindFallbackPlayerControlActions(this);
            bindCheatActions(this);
            bindFallbackPlayerControlActions(this);
            bindCheatActions(this);
        }
        registerGameActionKeyBindings(context.keyboard());
    }

    @Override
    protected void updateScores() {
        ScoreManager manager = context.game().scoreManager();
        Score score = manager.score(), highScore = manager.highScore();

        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
        if (manager.isScoreEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else { // when score is disabled, show text "game over"
            String assetKeyPrefix = context.gameConfiguration().assetKeyPrefix();
            Color color = context.assets().color(assetKeyPrefix + ".color.game_over_message");
            if (context.gameVariant() == GameVariant.MS_PACMAN_TENGEN) {
                WorldMap worldMap = context.level().world().map();
                NES_ColorScheme nesColorScheme = worldMap.getConfigValue("nesColorScheme");
                color = Color.valueOf(nesColorScheme.fillColor());
            }
            scores3D.showTextAsScore(GAME_OVER_TEXT, color);
        }
    }

    @Override
    public void onBonusActivated(GameEvent event) {
        context.level().bonus().ifPresent(bonus -> level3D.replaceBonus3D(bonus, context.gameConfiguration().spriteSheet()));
        context.sound().playBonusBouncingSound();
    }

    @Override
    public void onBonusEaten(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::showEaten);
        context.sound().stopBonusBouncingSound();
        context.sound().playBonusEatenSound();
    }

    @Override
    public void onBonusExpired(GameEvent event) {
        level3D.bonus3D().ifPresent(Bonus3D::onBonusExpired);
        context.sound().stopBonusBouncingSound();
    }
}