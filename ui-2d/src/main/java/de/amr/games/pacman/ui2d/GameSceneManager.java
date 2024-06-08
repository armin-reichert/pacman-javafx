/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d;

import de.amr.games.pacman.model.GameVariant;
import de.amr.games.pacman.ui2d.scene.*;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Armin Reichert
 */
public class GameSceneManager {

    public static final String BOOT_SCENE    = "boot";
    public static final String INTRO_SCENE   = "intro";
    public static final String CREDIT_SCENE  = "credit";
    public static final String PLAY_SCENE    = "play";
    public static final String PLAY_SCENE_3D = "play3D";
    public static final String CUT_SCENE_1   = "cut1";
    public static final String CUT_SCENE_2   = "cut2";
    public static final String CUT_SCENE_3   = "cut3";

    private final Map<GameVariant, Map<String, GameScene>> gameScenesForVariant = new EnumMap<>(GameVariant.class);

    public void createGameScenes(GameVariant variant) {
        switch (variant) {
            case MS_PACMAN ->
                gameScenesForVariant.put(variant, new HashMap<>(Map.of(
                    BOOT_SCENE,   new BootScene(),
                    INTRO_SCENE,  new MsPacManIntroScene(),
                    CREDIT_SCENE, new MsPacManCreditScene(),
                    PLAY_SCENE,   new PlayScene2D(),
                    CUT_SCENE_1,  new MsPacManCutScene1(),
                    CUT_SCENE_2,  new MsPacManCutScene2(),
                    CUT_SCENE_3,  new MsPacManCutScene3()
                )));
            case PACMAN, PACMAN_XXL ->
                gameScenesForVariant.put(variant, new HashMap<>(Map.of(
                    BOOT_SCENE,   new BootScene(),
                    INTRO_SCENE,  new PacManIntroScene(),
                    CREDIT_SCENE, new PacManCreditScene(),
                    PLAY_SCENE,   new PlayScene2D(),
                    CUT_SCENE_1,  new PacManCutScene1(),
                    CUT_SCENE_2,  new PacManCutScene2(),
                    CUT_SCENE_3,  new PacManCutScene3()
                )));
        }
    }

    public void putGameScene(GameScene gameScene, GameVariant variant, String sceneID) {
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

    public GameScene gameScene(GameVariant variant, String sceneID) {
        return gameScenesForVariant.get(variant).get(sceneID);
    }

    public boolean isGameSceneRegisteredAs(GameScene gameScene, GameVariant variant, String sceneID) {
        return gameScene(variant, sceneID) == gameScene;
    }
}
