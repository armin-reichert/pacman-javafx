/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.pacman;

import de.amr.games.pacman.arcade.Resources;
import de.amr.games.pacman.controller.GameState;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.assets.AssetStorage;
import de.amr.games.pacman.ui.assets.GameSpriteSheet;
import de.amr.games.pacman.ui.assets.ResourceManager;
import de.amr.games.pacman.ui.assets.WorldMapColoring;
import de.amr.games.pacman.ui.lib.Ufx;
import de.amr.games.pacman.ui.scene.BootScene;
import de.amr.games.pacman.ui.scene.GameConfiguration;
import de.amr.games.pacman.ui.scene.GameScene;
import de.amr.games.pacman.ui.scene.PlayScene2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class PacManGameConfiguration implements GameConfiguration {
    private final AssetStorage assets;
    private final PacManGameSpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public PacManGameConfiguration() {
        assets = new AssetStorage();
        loadAssets(() -> Resources.class);
        spriteSheet = new PacManGameSpriteSheet(assets.get(assetKeyPrefix() + ".spritesheet"));
        set("BootScene",   new BootScene());
        set("IntroScene",  new IntroScene());
        set("StartScene",  new StartScene());
        set("PlayScene2D", new PlayScene2D());
        set("CutScene1",   new CutScene1());
        set("CutScene2",   new CutScene2());
        set("CutScene3",   new CutScene3());
    }

    @Override
    public AssetStorage assets() {
        return assets;
    }

    @Override
    public String assetKeyPrefix() {
        return "pacman";
    }

    @Override
    public void set(String id, GameScene gameScene) {
        scenesByID.put(id, gameScene);
    }

    @Override
    public GameScene get(String id) {
        return scenesByID.get(id);
    }

    @Override
    public Stream<GameScene> gameScenes() {
        return scenesByID.values().stream();
    }

    @Override
    public PacManGameRenderer createRenderer(AssetStorage assets, Canvas canvas) {
        return new PacManGameRenderer(assets, spriteSheet, canvas);
    }

    @Override
    public WorldMapColoring worldMapColoring(WorldMap worldMap) {
        return PacManGameRenderer.WORLDMAP_COLORING;
    }

    @Override
    public GameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case GameState.BOOT               -> "BootScene";
            case GameState.SETTING_OPTIONS    -> "StartScene";
            case GameState.INTRO              -> "IntroScene";
            case GameState.INTERMISSION       -> "CutScene" + context.level().intermissionNumber();
            case GameState.TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
            default                 -> "PlayScene2D";
        };
        return get(sceneID);
    }

    @Override
    public void createActorAnimations(GameLevel level) {
        level.pac().setAnimations(new PacAnimations(spriteSheet));
        level.ghosts().forEach(ghost -> ghost.setAnimations(new GhostAnimations(spriteSheet, ghost.id())));
    }

    private void loadAssets(ResourceManager rm) {
        assets.store(assetKeyPrefix() + ".scene_background",         Ufx.imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));

        assets.store(assetKeyPrefix() + ".spritesheet",              rm.loadImage("graphics/pacman_spritesheet.png"));
        assets.store(assetKeyPrefix() + ".flashing_maze",            rm.loadImage("graphics/maze_flashing.png"));

        assets.store(assetKeyPrefix() + ".startpage.image1",         rm.loadImage("graphics/f1.jpg"));
        assets.store(assetKeyPrefix() + ".startpage.image2",         rm.loadImage("graphics/f2.jpg"));
        assets.store(assetKeyPrefix() + ".startpage.image3",         rm.loadImage("graphics/f3.jpg"));

        assets.store(assetKeyPrefix() + ".helpButton.icon",          rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.store(assetKeyPrefix() + ".icon",                     rm.loadImage("graphics/icons/pacman.png"));

        assets.store(assetKeyPrefix() + ".color.game_over_message",  Color.RED);
        assets.store(assetKeyPrefix() + ".color.ready_message",      Color.YELLOW);

        // Clips
        assets.store(assetKeyPrefix() + ".audio.bonus_eaten",        rm.loadAudioClip("sound/eat_fruit.mp3"));
        assets.store(assetKeyPrefix() + ".audio.credit",             rm.loadAudioClip("sound/credit.wav"));
        assets.store(assetKeyPrefix() + ".audio.extra_life",         rm.loadAudioClip("sound/extend.mp3"));
        assets.store(assetKeyPrefix() + ".audio.ghost_eaten",        rm.loadAudioClip("sound/eat_ghost.mp3"));
        assets.store(assetKeyPrefix() + ".audio.sweep",              rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        assets.store(assetKeyPrefix() + ".audio.game_ready",         rm.url("sound/game_start.mp3"));
        assets.store(assetKeyPrefix() + ".audio.game_over",          rm.url("sound/common/game-over.mp3"));
        assets.store(assetKeyPrefix() + ".audio.intermission",       rm.url("sound/intermission.mp3"));
        assets.store(assetKeyPrefix() + ".audio.pacman_death",       rm.url("sound/pacman_death.wav"));
        assets.store(assetKeyPrefix() + ".audio.pacman_munch",       rm.url("sound/munch.wav"));
        assets.store(assetKeyPrefix() + ".audio.pacman_power",       rm.url("sound/ghost-turn-to-blue.mp3"));
        assets.store(assetKeyPrefix() + ".audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        assets.store(assetKeyPrefix() + ".audio.siren.1",            rm.url("sound/siren_1.mp3"));
        assets.store(assetKeyPrefix() + ".audio.siren.2",            rm.url("sound/siren_2.mp3"));
        assets.store(assetKeyPrefix() + ".audio.siren.3",            rm.url("sound/siren_3.mp3"));
        assets.store(assetKeyPrefix() + ".audio.siren.4",            rm.url("sound/siren_4.mp3"));
        assets.store(assetKeyPrefix() + ".audio.ghost_returns",      rm.url("sound/retreating.mp3"));
    }
}