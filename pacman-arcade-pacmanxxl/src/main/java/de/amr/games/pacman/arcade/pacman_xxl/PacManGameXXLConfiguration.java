/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman_xxl;

import de.amr.games.pacman.arcade.Resources;
import de.amr.games.pacman.arcade.pacman.*;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.assets.AssetStorage;
import de.amr.games.pacman.ui2d.assets.GameSpriteSheet;
import de.amr.games.pacman.ui2d.assets.ResourceManager;
import de.amr.games.pacman.ui2d.assets.WorldMapColoring;
import de.amr.games.pacman.ui2d.scene.*;
import javafx.scene.canvas.Canvas;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui2d.lib.Ufx.imageBackground;

public class PacManGameXXLConfiguration implements GameConfiguration {

    private final PacManGameSpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public PacManGameXXLConfiguration(AssetStorage assets) {
        loadAssets(() -> Resources.class, assets);
        spriteSheet = new PacManGameSpriteSheet(assets.get(assetKeyPrefix() + ".spritesheet"));
        setGameScene("BootScene",   new ArcadeBootScene());
        setGameScene("IntroScene",  new IntroScene());
        setGameScene("StartScene",  new StartScene());
        setGameScene("PlayScene2D", new ArcadePlayScene2D());
        setGameScene("CutScene1",   new CutScene1());
        setGameScene("CutScene2",   new CutScene2());
        setGameScene("CutScene3",   new CutScene3());
    }

    @Override
    public String assetKeyPrefix() {
        return "pacman_xxl";
    }

    @Override
    public void setGameScene(String id, GameScene gameScene) {
        scenesByID.put(id, gameScene);
    }

    @Override
    public GameScene getGameScene(String id) {
        return scenesByID.get(id);
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public GameScene2D createPiPScene(GameContext context, Canvas canvas) {
        var gameScene = new ArcadePlayScene2D();
        gameScene.setGameContext(context);
        gameScene.setGameRenderer(createRenderer(context.assets(), canvas));
        return gameScene;
    }

    @Override
    public PacManGameXXLRenderer createRenderer(AssetStorage assets, Canvas canvas) {
        return new PacManGameXXLRenderer(assets, spriteSheet, canvas);
    }

    @Override
    @SuppressWarnings("unchecked")
    public WorldMapColoring worldMapColoring(WorldMap worldMap) {
        return new WorldMapColoring((Map<String, String>) worldMap.getConfigValue("colorMap"));
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case BOOT -> "BootScene";
            case SETTING_OPTIONS -> "StartScene";
            case INTRO -> "IntroScene";
            case INTERMISSION -> "CutScene" + context.level().intermissionNumber();
            case TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default -> "PlayScene2D";
        };
        return getGameScene(sceneID);
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }

    private void loadAssets(ResourceManager rm, AssetStorage assets) {
        assets.store(assetKeyPrefix() + ".scene_background",     imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));

        assets.store(assetKeyPrefix() + ".icon",                 rm.loadImage("graphics/icons/pacman.png"));
        assets.store(assetKeyPrefix() + ".helpButton.icon",      rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.store(assetKeyPrefix() + ".startpage.image1",     rm.loadImage("graphics/pacman_xxl_logo.jpg"));

        assets.store(assetKeyPrefix() + ".spritesheet",          rm.loadImage("graphics/pacman_spritesheet.png"));

        // Clips
        assets.store(assetKeyPrefix() + ".audio.bonus_eaten",    rm.loadAudioClip("sound/eat_fruit.mp3"));
        assets.store(assetKeyPrefix() + ".audio.credit",         rm.loadAudioClip("sound/credit.wav"));
        assets.store(assetKeyPrefix() + ".audio.extra_life",     rm.loadAudioClip("sound/extend.mp3"));
        assets.store(assetKeyPrefix() + ".audio.ghost_eaten",    rm.loadAudioClip("sound/eat_ghost.mp3"));
        assets.store(assetKeyPrefix() + ".audio.sweep",          rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        assets.store(assetKeyPrefix() + ".audio.game_ready",     rm.url("sound/game_start.mp3"));
        assets.store(assetKeyPrefix() + ".audio.game_over",      rm.url("sound/common/game-over.mp3"));
        assets.store(assetKeyPrefix() + ".audio.intermission",   rm.url("sound/intermission.mp3"));
        assets.store(assetKeyPrefix() + ".audio.pacman_death",   rm.url("sound/pacman_death.wav"));
        assets.store(assetKeyPrefix() + ".audio.pacman_munch",   rm.url("sound/munch.wav"));
        assets.store(assetKeyPrefix() + ".audio.pacman_power",   rm.url("sound/ghost-turn-to-blue.mp3"));
        assets.store(assetKeyPrefix() + ".audio.level_complete", rm.url("sound/common/level-complete.mp3"));
        assets.store(assetKeyPrefix() + ".audio.siren.1",        rm.url("sound/siren_1.mp3"));
        assets.store(assetKeyPrefix() + ".audio.siren.2",        rm.url("sound/siren_2.mp3"));
        assets.store(assetKeyPrefix() + ".audio.siren.3",        rm.url("sound/siren_3.mp3"));
        assets.store(assetKeyPrefix() + ".audio.siren.4",        rm.url("sound/siren_4.mp3"));
        assets.store(assetKeyPrefix() + ".audio.ghost_returns",  rm.url("sound/retreating.mp3"));
    }
}