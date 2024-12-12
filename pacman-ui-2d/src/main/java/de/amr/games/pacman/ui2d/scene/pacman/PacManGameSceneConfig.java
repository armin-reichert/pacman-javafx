/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.scene.pacman;

import de.amr.games.pacman.lib.tilemap.WorldMap;
import de.amr.games.pacman.model.GameLevel;
import de.amr.games.pacman.ui2d.GameAssets2D;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.rendering.GameSpriteSheet;
import de.amr.games.pacman.ui2d.scene.common.*;
import de.amr.games.pacman.ui2d.util.AssetStorage;
import de.amr.games.pacman.ui2d.util.ResourceManager;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static de.amr.games.pacman.lib.Globals.checkNotNull;
import static de.amr.games.pacman.ui2d.GameAssets2D.PFX_PACMAN;
import static de.amr.games.pacman.ui2d.util.Ufx.imageBackground;

public class PacManGameSceneConfig implements GameSceneConfig {

    private final AssetStorage assets;
    private final PacManGameSpriteSheet spriteSheet;
    private final Map<String, GameScene> scenesByID = new HashMap<>();

    public PacManGameSceneConfig(AssetStorage assets) {
        this.assets = checkNotNull(assets);
        loadAssets(() -> GameAssets2D.class);

        spriteSheet = new PacManGameSpriteSheet(assets.get(PFX_PACMAN + ".spritesheet"));

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
    public PacManGameRenderer createRenderer(Canvas canvas) {
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
        assets.store(PFX_PACMAN + ".scene_background",         imageBackground(rm.loadImage("graphics/pacman_wallpaper.png")));

        assets.store(PFX_PACMAN + ".spritesheet",              rm.loadImage("graphics/pacman/pacman_spritesheet.png"));
        assets.store(PFX_PACMAN + ".flashing_maze",            rm.loadImage("graphics/pacman/maze_flashing.png"));

        assets.store(PFX_PACMAN + ".startpage.image1",         rm.loadImage("graphics/pacman/f1.jpg"));
        assets.store(PFX_PACMAN + ".startpage.image2",         rm.loadImage("graphics/pacman/f2.jpg"));
        assets.store(PFX_PACMAN + ".startpage.image3",         rm.loadImage("graphics/pacman/f3.jpg"));

        assets.store(PFX_PACMAN + ".helpButton.icon",          rm.loadImage("graphics/icons/help-blue-64.png"));
        assets.store(PFX_PACMAN + ".icon",                     rm.loadImage("graphics/icons/pacman.png"));

        assets.store(PFX_PACMAN + ".color.game_over_message",  Color.RED);
        assets.store(PFX_PACMAN + ".color.ready_message",      Color.YELLOW);

        // Clips
        assets.store(PFX_PACMAN + ".audio.bonus_eaten",        rm.loadAudioClip("sound/pacman/eat_fruit.mp3"));
        assets.store(PFX_PACMAN + ".audio.credit",             rm.loadAudioClip("sound/pacman/credit.wav"));
        assets.store(PFX_PACMAN + ".audio.extra_life",         rm.loadAudioClip("sound/pacman/extend.mp3"));
        assets.store(PFX_PACMAN + ".audio.ghost_eaten",        rm.loadAudioClip("sound/pacman/eat_ghost.mp3"));
        assets.store(PFX_PACMAN + ".audio.sweep",              rm.loadAudioClip("sound/common/sweep.mp3"));

        // Media player sounds
        assets.store(PFX_PACMAN + ".audio.game_ready",         rm.url("sound/pacman/game_start.mp3"));
        assets.store(PFX_PACMAN + ".audio.game_over",          rm.url("sound/common/game-over.mp3"));
        assets.store(PFX_PACMAN + ".audio.intermission",       rm.url("sound/pacman/intermission.mp3"));
        assets.store(PFX_PACMAN + ".audio.pacman_death",       rm.url("sound/pacman/pacman_death.wav"));
        assets.store(PFX_PACMAN + ".audio.pacman_munch",       rm.url("sound/pacman/munch.wav"));
        assets.store(PFX_PACMAN + ".audio.pacman_power",       rm.url("sound/pacman/ghost-turn-to-blue.mp3"));
        assets.store(PFX_PACMAN + ".audio.level_complete",     rm.url("sound/common/level-complete.mp3"));
        assets.store(PFX_PACMAN + ".audio.siren.1",            rm.url("sound/pacman/siren_1.mp3"));
        assets.store(PFX_PACMAN + ".audio.siren.2",            rm.url("sound/pacman/siren_2.mp3"));
        assets.store(PFX_PACMAN + ".audio.siren.3",            rm.url("sound/pacman/siren_3.mp3"));
        assets.store(PFX_PACMAN + ".audio.siren.4",            rm.url("sound/pacman/siren_4.mp3"));
        assets.store(PFX_PACMAN + ".audio.ghost_returns",      rm.url("sound/pacman/retreating.mp3"));
    }
}