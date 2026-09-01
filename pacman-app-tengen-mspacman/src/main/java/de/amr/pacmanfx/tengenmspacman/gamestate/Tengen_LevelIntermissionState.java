/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamestate;

import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;

import java.util.OptionalInt;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.setHUD_Option;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_HUD_Options.GAME_OPTIONS_VISIBLE;

public class Tengen_LevelIntermissionState extends AbstractGameState {

    public Tengen_LevelIntermissionState() {
        super(CommonGameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnterState(GameContext game) {
        final GameLevel level = session.level();
        final OptionalInt cutSceneNumber = rules.cutSceneAfterLevel(level.number());
        final boolean isLastCutScene = cutSceneNumber.isPresent() && cutSceneNumber.getAsInt() == rules.lastCutSceneNumber();

        if (isLastCutScene) {
            hud.hide();
        } else {
            hud.gameScore().hide();
            hud.levelCounter().show();
            hud.livesCounter().hide();
            setHUD_Option(session, GAME_OPTIONS_VISIBLE, false);
            hud.show();
        }
        timer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdateState(GameContext game, long globalTick, long stateTick) {
        if (timer().hasExpired()) {
            flow.enterGameState(game, session.isGameRunning() ? CommonGameStateID.GAME_LEVEL_TRANSITION : CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext game) {
        if (TengenMsPacMan_GamePlay.mapCategory(session) == MapCategory.ARCADE) {
            hud.hide();
        } else {
            setHUD_Option(session, GAME_OPTIONS_VISIBLE, false);
            hud.gameScore().show();
            hud.levelCounter().show();
            hud.livesCounter().hide();
            hud.show();
        }
    }
}
