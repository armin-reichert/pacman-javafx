/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.GameContext;
import de.amr.games.pacman.ui.assets.AssetStorage;
import de.amr.games.pacman.ui.assets.ResourceManager;
import de.amr.games.pacman.ui.assets.WorldMapColoring;
import de.amr.games.pacman.ui.scene.BootScene;
import de.amr.games.pacman.ui.scene.GameConfiguration;
import de.amr.games.pacman.ui.scene.GameScene;
import de.amr.games.pacman.ui.scene.PlayScene2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.ui.lib.Ufx.imageBackground;

public class MsPacManGameConfiguration implements GameConfiguration {

    private final AssetStorage assets;
    private final MsPacManGameSpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public MsPacManGameConfiguration() {
        assets = new AssetStorage();
        loadAssets(() -> MsPacManGameConfiguration.class);
        spriteSheet = new MsPacManGameSpriteSheet(assets.get(assetKeyPrefix() + ".spritesheet"));

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
        return "ms_pacman";
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
    public WorldMapColoring worldMapColoring(WorldMap worldMap) {
        return MsPacManGameRenderer.WORLD_MAP_COLORINGS.get(worldMap.getConfigValue("colorMapIndex"));
    }

    @Override
    public MsPacManGameRenderer createRenderer(AssetStorage assets, Canvas canvas) {
        return new MsPacManGameRenderer(assets, spriteSheet, canvas);
    }

    @Override
    public MsPacManGameSpriteSheet spriteSheet() {
        return spriteSheet;
    }

    @Override
    public GameScene selectGameScene(GameContext context) {
        String sceneID = switch (context.gameState()) {
            case BOOT               -> "BootScene";
            case SETTING_OPTIONS    -> "StartScene";
            case INTRO              -> "IntroScene";
            case INTERMISSION       -> "CutScene" + context.level().intermissionNumber();
            case TESTING_CUT_SCENES -> "CutScene" + context.gameState().<Integer>getProperty("intermissionTestNumber");
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
        assets.store(assetKeyPrefix() + ".scene_background",              imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));

        assets.store(assetKeyPrefix() + ".spritesheet",                   rm.loadImage("graphics/mspacman_spritesheet.png"));
        assets.store(assetKeyPrefix() + ".flashing_mazes",                rm.loadImage("graphics/mazes_flashing.png"));

        assets.store(assetKeyPrefix() + ".startpage.image1",              rm.loadImage("graphics/f1.jpg"));
        assets.store(assetKeyPrefix() + ".startpage.image2",              rm.loadImage("graphics/f2.jpg"));

        assets.store(assetKeyPrefix() + ".helpButton.icon",               rm.loadImage("graphics/icons/help-red-64.png"));
        assets.store(assetKeyPrefix() + ".icon",                          rm.loadImage("graphics/icons/mspacman.png"));
        assets.store(assetKeyPrefix() + ".logo.midway",                   rm.loadImage("graphics/midway_logo.png"));

        assets.store(assetKeyPrefix() + ".ghost.0.color.normal.dress",    Color.valueOf(Arcade.Palette.RED));
        assets.store(assetKeyPrefix() + ".ghost.1.color.normal.dress",    Color.valueOf(Arcade.Palette.PINK));
        assets.store(assetKeyPrefix() + ".ghost.2.color.normal.dress",    Color.valueOf(Arcade.Palette.CYAN));
        assets.store(assetKeyPrefix() + ".ghost.3.color.normal.dress",    Color.valueOf(Arcade.Palette.ORANGE));

        assets.store(assetKeyPrefix() + ".color.game_over_message",       Color.valueOf(Arcade.Palette.RED));
        assets.store(assetKeyPrefix() + ".color.ready_message",           Color.valueOf(Arcade.Palette.YELLOW));
        assets.store(assetKeyPrefix() + ".color.clapperboard",            Color.valueOf(Arcade.Palette.WHITE));

        // Clips
        assets.store(assetKeyPrefix() + ".audio.bonus_eaten",             rm.loadAudioClip("sound/Fruit.mp3"));
        assets.store(assetKeyPrefix() + ".audio.credit",                  rm.loadAudioClip("sound/credit.wav"));
        assets.store(assetKeyPrefix() + ".audio.extra_life",              rm.loadAudioClip("sound/ExtraLife.mp3"));
        assets.store(assetKeyPrefix() + ".audio.ghost_eaten",             rm.loadAudioClip("sound/Ghost.mp3"));
        assets.store(assetKeyPrefix() + ".audio.sweep",                   rm.loadAudioClip("sound/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store(assetKeyPrefix() + ".audio.bonus_bouncing",          rm.url("sound/Fruit Bounce.mp3"));
        assets.store(assetKeyPrefix() + ".audio.game_ready",              rm.url("sound/Start.mp3"));
        assets.store(assetKeyPrefix() + ".audio.game_over",               rm.url("sound/game-over.mp3"));
        assets.store(assetKeyPrefix() + ".audio.intermission.1",          rm.url("sound/Act_1_They_Meet.mp3"));
        assets.store(assetKeyPrefix() + ".audio.intermission.2",          rm.url("sound/Act_2_The_Chase.mp3"));
        assets.store(assetKeyPrefix() + ".audio.intermission.3",          rm.url("sound/Act_3_Junior.mp3"));
        assets.store(assetKeyPrefix() + ".audio.level_complete",          rm.url("sound/level-complete.mp3"));
        assets.store(assetKeyPrefix() + ".audio.pacman_death",            rm.url("sound/Died.mp3"));
        assets.store(assetKeyPrefix() + ".audio.pacman_munch",            rm.url("sound/munch.wav"));
        assets.store(assetKeyPrefix() + ".audio.pacman_power",            rm.url("sound/ScaredGhost.mp3"));
        assets.store(assetKeyPrefix() + ".audio.siren.1",                 rm.url("sound/GhostNoise1.wav"));
        assets.store(assetKeyPrefix() + ".audio.siren.2",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store(assetKeyPrefix() + ".audio.siren.3",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store(assetKeyPrefix() + ".audio.siren.4",                 rm.url("sound/GhostNoise1.wav"));// TODO
        assets.store(assetKeyPrefix() + ".audio.ghost_returns",           rm.url("sound/GhostEyes.mp3"));
    }
}