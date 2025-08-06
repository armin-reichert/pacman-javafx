/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui._3d.GameLevel3D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.ui.GameUI.DEFAULT_ACTION_BINDINGS;
import static de.amr.pacmanfx.ui.PacManGames_GameActions.*;

/**
 * The 3D play scene of Tengen Ms. Pac-Man.
 *
 * <p>Differs slightly from the Arcade version, e.g. some action bindings use the "Joypad" keys
 * and additional information not available in the Arcade games (difficulty, maze category etc.) is displayed.
 */
public class TengenMsPacMan_PlayScene3D extends PlayScene3D {

    public TengenMsPacMan_PlayScene3D(GameUI ui) {
        super(ui);
    }

    @Override
    protected GameLevel3D createGameLevel3D() {
        return new TengenMsPacMan_GameLevel3D(ui);
    }

    @Override
    protected void setActionBindings() {
        var config = ui.<TengenMsPacMan_UIConfig>theConfiguration();
        // if demo level, allow going back to options screen
        if (gameContext().optGameLevel().isPresent() && gameContext().theGameLevel().isDemoLevel()) {
            actionBindings.use(config.ACTION_QUIT_DEMO_LEVEL, config.actionBindings);
        } else {
            setPlayerSteeringActionBindings();
            actionBindings.use(ACTION_CHEAT_ADD_LIVES, DEFAULT_ACTION_BINDINGS);
            actionBindings.use(ACTION_CHEAT_EAT_ALL_PELLETS, DEFAULT_ACTION_BINDINGS);
            actionBindings.use(ACTION_CHEAT_ENTER_NEXT_LEVEL, DEFAULT_ACTION_BINDINGS);
            actionBindings.use(ACTION_CHEAT_KILL_GHOSTS, DEFAULT_ACTION_BINDINGS);
            // Tengen only:
            actionBindings.use(config.ACTION_TOGGLE_PAC_BOOSTER, config.actionBindings);
        }
        actionBindings.use(ACTION_PERSPECTIVE_PREVIOUS, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_PERSPECTIVE_NEXT, DEFAULT_ACTION_BINDINGS);
        actionBindings.use(ACTION_TOGGLE_DRAW_MODE, DEFAULT_ACTION_BINDINGS);

        actionBindings.updateKeyboard(ui.theKeyboard());
    }

    @Override
    protected void setPlayerSteeringActionBindings() {
        var config = ui.<TengenMsPacMan_UIConfig>theConfiguration();
        actionBindings.use(ACTION_STEER_UP,    config.actionBindings);
        actionBindings.use(ACTION_STEER_DOWN,  config.actionBindings);
        actionBindings.use(ACTION_STEER_LEFT,  config.actionBindings);
        actionBindings.use(ACTION_STEER_RIGHT, config.actionBindings);
    }

    @Override
    protected void updateHUD() {
        final Score score = gameContext().theGame().score(), highScore = gameContext().theGame().highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else {
            scores3D.showTextForScore(ui.theAssets().text("score.game_over"), Color.web(NES_Palette.color(0x16)));
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }
}