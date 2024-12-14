/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.arcade.ms_pacman;

import de.amr.games.pacman.lib.arcade.Arcade;
import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui.*;
import de.amr.games.pacman.ui.assets.AssetStorage;
import de.amr.games.pacman.ui.assets.ResourceManager;
import de.amr.games.pacman.ui.assets.WorldMapColoring;
import de.amr.games.pacman.ui.scene.BootScene;
import de.amr.games.pacman.ui.scene.GameScene;
import de.amr.games.pacman.ui.scene.GameSceneConfig;
import de.amr.games.pacman.ui.scene.PlayScene2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui.lib.Ufx.imageBackground;

public class MsPacManGameSceneConfig implements GameSceneConfig {

    private final AssetStorage assets;
    private final MsPacManGameSpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public MsPacManGameSceneConfig(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        loadAssets(this::getClass); //TODO fixme
        spriteSheet = new MsPacManGameSpriteSheet(assets.get(GameContext.PFX_MS_PACMAN + ".spritesheet"));

        set("BootScene",   new BootScene());
        set("IntroScene",  new IntroScene());
        set("StartScene",  new StartScene());
        set("PlayScene2D", new PlayScene2D());
        set("CutScene1",   new CutScene1());
        set("CutScene2",   new CutScene2());
        set("CutScene3",   new CutScene3());
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
    public MsPacManGameRenderer createRenderer(Canvas canvas) {
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
        assets.store(GameContext.PFX_MS_PACMAN + ".scene_background",              imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));

        assets.store(GameContext.PFX_MS_PACMAN + ".spritesheet",                   rm.loadImage("graphics/mspacman/mspacman_spritesheet.png"));
        assets.store(GameContext.PFX_MS_PACMAN + ".flashing_mazes",                rm.loadImage("graphics/mspacman/mazes_flashing.png"));

        assets.store(GameContext.PFX_MS_PACMAN + ".startpage.image1",              rm.loadImage("graphics/mspacman/f1.jpg"));
        assets.store(GameContext.PFX_MS_PACMAN + ".startpage.image2",              rm.loadImage("graphics/mspacman/f2.jpg"));

        assets.store(GameContext.PFX_MS_PACMAN + ".helpButton.icon",               rm.loadImage("graphics/icons/help-red-64.png"));
        assets.store(GameContext.PFX_MS_PACMAN + ".icon",                          rm.loadImage("graphics/icons/mspacman.png"));
        assets.store(GameContext.PFX_MS_PACMAN + ".logo.midway",                   rm.loadImage("graphics/mspacman/midway_logo.png"));

        assets.store(GameContext.PFX_MS_PACMAN + ".ghost.0.color.normal.dress",    Color.valueOf(Arcade.Palette.RED));
        assets.store(GameContext.PFX_MS_PACMAN + ".ghost.1.color.normal.dress",    Color.valueOf(Arcade.Palette.PINK));
        assets.store(GameContext.PFX_MS_PACMAN + ".ghost.2.color.normal.dress",    Color.valueOf(Arcade.Palette.CYAN));
        assets.store(GameContext.PFX_MS_PACMAN + ".ghost.3.color.normal.dress",    Color.valueOf(Arcade.Palette.ORANGE));

        assets.store(GameContext.PFX_MS_PACMAN + ".color.game_over_message",       Color.valueOf(Arcade.Palette.RED));
        assets.store(GameContext.PFX_MS_PACMAN + ".color.ready_message",           Color.valueOf(Arcade.Palette.YELLOW));
        assets.store(GameContext.PFX_MS_PACMAN + ".color.clapperboard",            Color.valueOf(Arcade.Palette.WHITE));

        // Clips
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.bonus_eaten",             rm.loadAudioClip("sound/mspacman/Fruit.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.credit",                  rm.loadAudioClip("sound/mspacman/credit.wav"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.extra_life",              rm.loadAudioClip("sound/mspacman/ExtraLife.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.ghost_eaten",             rm.loadAudioClip("sound/mspacman/Ghost.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.sweep",                   rm.loadAudioClip("sound/common/sweep.mp3"));

        // Audio played by MediaPlayer
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.bonus_bouncing",          rm.url("sound/mspacman/Fruit Bounce.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.game_ready",              rm.url("sound/mspacman/Start.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.game_over",               rm.url("sound/common/game-over.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.intermission.1",          rm.url("sound/mspacman/Act_1_They_Meet.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.intermission.2",          rm.url("sound/mspacman/Act_2_The_Chase.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.intermission.3",          rm.url("sound/mspacman/Act_3_Junior.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.level_complete",          rm.url("sound/common/level-complete.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.pacman_death",            rm.url("sound/mspacman/Died.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.pacman_munch",            rm.url("sound/mspacman/munch.wav"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.pacman_power",            rm.url("sound/mspacman/ScaredGhost.mp3"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.siren.1",                 rm.url("sound/mspacman/GhostNoise1.wav"));
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.siren.2",                 rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.siren.3",                 rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.siren.4",                 rm.url("sound/mspacman/GhostNoise1.wav"));// TODO
        assets.store(GameContext.PFX_MS_PACMAN + ".audio.ghost_returns",           rm.url("sound/mspacman/GhostEyes.mp3"));
    }
}