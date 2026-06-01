/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.tengenmspacman;

import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.test.CutScenesTestState;
import de.amr.pacmanfx.tengenmspacman.scenes.*;
import de.amr.pacmanfx.ui.AppContext;
import de.amr.pacmanfx.ui.gamescene.AbstractGameSceneConfig;
import de.amr.pacmanfx.ui.gamescene.CommonSceneID;
import de.amr.pacmanfx.ui.gamescene.GameScene;

import static de.amr.pacmanfx.tengenmspacman.model.TengenMsPacMan_GameState.*;
import static de.amr.pacmanfx.ui.GameUI_Constants.PROPERTY_3D_ENABLED;
import static java.util.Objects.requireNonNull;

public class TengenMsPacMan_GameSceneConfig extends AbstractGameSceneConfig {

    private final AppContext context;

    public TengenMsPacMan_GameSceneConfig(AppContext context) {
        this.context = requireNonNull(context);
    }

    @Override
    public boolean sceneDecorationRequested(GameScene gameScene) {
        requireNonNull(gameScene);
        return false;
    }

    @Override
    protected GameScene createGameScene(SceneID sceneID) {
        requireNonNull(sceneID);
        return switch (sceneID) {
            case CommonSceneID.BOOT_SCENE -> new TengenMsPacMan_BootScene(context);
            case CommonSceneID.INTRO_SCENE -> new TengenMsPacMan_IntroScene(context);
            case CommonSceneID.START_SCENE -> new TengenMsPacMan_OptionsScene(context);
            case TengenMsPacMan_UIConfig.TengenSceneID.HALL_OF_FAME -> new TengenMsPacMan_CreditsScene(context);
            case CommonSceneID.PLAY_SCENE_2D -> new TengenMsPacMan_PlayScene2D(context);
            case CommonSceneID.PLAY_SCENE_3D -> new TengenMsPacMan_PlayScene3D(context);
            case CommonSceneID.CUTSCENE_1 -> new TengenMsPacMan_CutScene1(context);
            case CommonSceneID.CUTSCENE_2 -> new TengenMsPacMan_CutScene2(context);
            case CommonSceneID.CUTSCENE_3 -> new TengenMsPacMan_CutScene3(context);
            case CommonSceneID.CUTSCENE_4 -> new TengenMsPacMan_CutScene4(context);
            default -> throw new IllegalArgumentException("Illegal scene ID: " + sceneID);
        };
    }

    @Override
    protected SceneID determineSceneID(Game game) {
        return switch (game.flow().state()) {
            case BOOT -> CommonSceneID.BOOT_SCENE;
            case PREPARING_GAME_START -> CommonSceneID.START_SCENE;
            case SHOWING_HALL_OF_FAME -> TengenMsPacMan_UIConfig.TengenSceneID.HALL_OF_FAME;
            case INTRO -> CommonSceneID.INTRO_SCENE;
            case INTERMISSION -> resolveCutSceneID(game);
            case CutScenesTestState<?> testState -> AbstractGameSceneConfig.cutSceneID(testState.testedCutSceneNumber);
            default -> PROPERTY_3D_ENABLED.get() ? CommonSceneID.PLAY_SCENE_3D : CommonSceneID.PLAY_SCENE_2D;
        };
    }
}
