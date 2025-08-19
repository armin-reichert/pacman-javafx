/*
Copyright (c) 2021-2025 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx.tengen.ms_pacman.scenes;

import de.amr.pacmanfx.lib.nes.NES_Palette;
import de.amr.pacmanfx.model.Score;
import de.amr.pacmanfx.model.ScoreManager;
import de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.ui._3d.GameLevel3D;
import de.amr.pacmanfx.ui._3d.PlayScene3D;
import de.amr.pacmanfx.ui.api.GameUI;
import javafx.scene.paint.Color;

import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_QUIT_DEMO_LEVEL;
import static de.amr.pacmanfx.tengen.ms_pacman.TengenMsPacMan_Actions.ACTION_TOGGLE_PAC_BOOSTER;
import static de.amr.pacmanfx.ui.CommonGameActions.*;

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
        var tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().actionBindings();
        // if demo level, allow going back to options screen
        if (gameContext().optGameLevel().isPresent() && gameContext().gameLevel().isDemoLevel()) {
            actionBindings.assign(ACTION_QUIT_DEMO_LEVEL, tengenActionBindings);
        } else {
            setPlayerSteeringActionBindings();
            actionBindings.assign(ACTION_CHEAT_ADD_LIVES, ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_EAT_ALL_PELLETS, ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_ENTER_NEXT_LEVEL, ui.actionBindings());
            actionBindings.assign(ACTION_CHEAT_KILL_GHOSTS, ui.actionBindings());
            actionBindings.assign(ACTION_TOGGLE_PAC_BOOSTER, tengenActionBindings);
        }
        actionBindings.assign(ACTION_PERSPECTIVE_PREVIOUS, ui.actionBindings());
        actionBindings.assign(ACTION_PERSPECTIVE_NEXT, ui.actionBindings());
        actionBindings.assign(ACTION_TOGGLE_DRAW_MODE, ui.actionBindings());

        actionBindings.installBindings(ui.keyboard());
    }

    @Override
    protected void setPlayerSteeringActionBindings() {
        var tengenActionBindings = ui.<TengenMsPacMan_UIConfig>currentConfig().actionBindings();
        actionBindings.assign(ACTION_STEER_UP,    tengenActionBindings);
        actionBindings.assign(ACTION_STEER_DOWN,  tengenActionBindings);
        actionBindings.assign(ACTION_STEER_LEFT,  tengenActionBindings);
        actionBindings.assign(ACTION_STEER_RIGHT, tengenActionBindings);
    }

    @Override
    protected void updateHUD() {
        ScoreManager scoreManager = gameContext().game().scoreManager();
        final Score score = scoreManager.score(), highScore = scoreManager.highScore();
        if (score.isEnabled()) {
            scores3D.showScore(score.points(), score.levelNumber());
        }
        else {
            scores3D.showTextForScore(ui.assets().translated("score.game_over"), Color.web(NES_Palette.color(0x16)));
        }
        // Always show high score
        scores3D.showHighScore(highScore.points(), highScore.levelNumber());
    }
}