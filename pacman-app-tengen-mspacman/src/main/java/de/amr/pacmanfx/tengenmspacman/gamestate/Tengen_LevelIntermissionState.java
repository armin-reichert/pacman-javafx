/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamestate;

import de.amr.basics.ecs.GameEntity;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.entities.LevelNumberDisplay;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;

import java.util.OptionalInt;

import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptionValues;

public class Tengen_LevelIntermissionState extends AbstractGameState {

    public Tengen_LevelIntermissionState() {
        super(CommonGameStateID.GAME_LEVEL_INTERMISSION);
    }

    @Override
    public void onEnterState(GameContext game) {
        final GameLevel level = game.session().optLevel().orElseThrow();
        final OptionalInt cutSceneNumber = game.rules().cutSceneAfterLevel(level.number());
        final boolean isLastCutScene = cutSceneNumber.isPresent()
            && cutSceneNumber.getAsInt() == game.rules().lastCutSceneNumber();

        if (isLastCutScene) {
            game.session().store(SessionKey.HUD_VISIBLE, false);
        } else {
            game.session().hud().gameScore().hide();
            game.session().hud().levelCounter().show();
            game.session().hud().livesCounter().hide();
            game.session().hud().additionalEntities().elementsOfType(LevelNumberDisplay.class).forEach(GameEntity::hide);
            game.session().store(SessionKey.HUD_VISIBLE, true);
        }
        stateTimer().resetToIndefiniteDuration();
    }

    @Override
    public void onUpdate(GameContext game, long globalTick, long stateTick) {
        if (stateTimer().hasExpired()) {
            game.flow().enterGameState(game, game.session().booleanValue(SessionKey.GAME_RUNNING)
                ? CommonGameStateID.GAME_LEVEL_TRANSITION : CommonGameStateID.GAME_INTRO);
        }
    }

    @Override
    public void onExit(GameContext game) {
        if (gameOptionValues(game.session()).mapCategory() == MapCategory.ARCADE) {
            game.session().store(SessionKey.HUD_VISIBLE, false);
        } else {
            game.session().hud().additionalEntities().elementsOfType(LevelNumberDisplay.class).forEach(GameEntity::hide);
            game.session().hud().gameScore().show();
            game.session().hud().levelCounter().show();
            game.session().hud().livesCounter().hide();
            game.session().store(SessionKey.HUD_VISIBLE, true);
        }
    }
}
