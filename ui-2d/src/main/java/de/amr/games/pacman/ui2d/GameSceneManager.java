/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.scene.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class GameSceneManager {

    private final Map<GameVariant, Map<GameSceneID, GameScene>> gameScenesForVariant = new EnumMap<>(GameVariant.class);

    public void createGameScenes(GameVariant variant) {
        switch (variant) {
            case MS_PACMAN ->
                gameScenesForVariant.put(variant, new EnumMap<>(Map.of(
                    GameSceneID.BOOT_SCENE,   new BootScene(),
                    GameSceneID.INTRO_SCENE,  new MsPacManIntroScene(),
                    GameSceneID.CREDIT_SCENE, new MsPacManCreditScene(),
                    GameSceneID.PLAY_SCENE,   new PlayScene2D(),
                    GameSceneID.CUT_SCENE_1,  new MsPacManCutScene1(),
                    GameSceneID.CUT_SCENE_2,  new MsPacManCutScene2(),
                    GameSceneID.CUT_SCENE_3,  new MsPacManCutScene3()
                )));
            case PACMAN, PACMAN_XXL ->
                gameScenesForVariant.put(variant, new EnumMap<>(Map.of(
                    GameSceneID.BOOT_SCENE,   new BootScene(),
                    GameSceneID.INTRO_SCENE,  new PacManIntroScene(),
                    GameSceneID.CREDIT_SCENE, new PacManCreditScene(),
                    GameSceneID.PLAY_SCENE,   new PlayScene2D(),
                    GameSceneID.CUT_SCENE_1,  new PacManCutScene1(),
                    GameSceneID.CUT_SCENE_2,  new PacManCutScene2(),
                    GameSceneID.CUT_SCENE_3,  new PacManCutScene3()
                )));
        }
    }

    public void putGameScene(GameScene gameScene, GameVariant variant, GameSceneID sceneID) {
        gameScenesForVariant.get(variant).put(sceneID, gameScene);
    }

    public Stream<GameScene> gameScenes(GameVariant variant) {
        return gameScenesForVariant.get(variant).values().stream();
    }

    public Stream<GameScene2D> gameScenes2D(GameVariant variant) {
        return gameScenesForVariant.get(variant).values().stream()
            .filter(GameScene2D.class::isInstance)
            .map(GameScene2D.class::cast);
    }

    public GameScene gameScene(GameVariant variant, GameSceneID sceneID) {
        return gameScenesForVariant.get(variant).get(sceneID);
    }

    public boolean isGameSceneRegisteredAs(GameScene gameScene, GameVariant variant, GameSceneID sceneID) {
        return gameScene(variant, sceneID) == gameScene;
    }
}
