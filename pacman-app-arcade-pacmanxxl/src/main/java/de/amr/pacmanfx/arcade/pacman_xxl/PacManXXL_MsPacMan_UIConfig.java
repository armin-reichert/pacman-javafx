/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.arcade.pacman_xxl;

import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_Factory3D;
import de.amr.pacmanfx.arcade.ms_pacman.ArcadeMsPacMan_UIConfig;
import de.amr.pacmanfx.arcade.ms_pacman.model.ArcadeMsPacMan_GameModel;
import de.amr.pacmanfx.arcade.ms_pacman.rendering.*;
import de.amr.pacmanfx.arcade.ms_pacman.scenes.*;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_BootScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.rendering.Arcade_PlayScene2D_Renderer;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_BootScene2D;
import de.amr.pacmanfx.arcade.pacman.scenes.Arcade_PlayScene2D;
import de.amr.pacmanfx.lib.math.RectShort;
import de.amr.pacmanfx.model.actors.Ghost;
import de.amr.pacmanfx.model.world.WorldMap;
import de.amr.pacmanfx.model.world.WorldMapColorScheme;
import de.amr.pacmanfx.model.world.WorldMapConfigKey;
import de.amr.pacmanfx.ui.GameSceneConfig;
import de.amr.pacmanfx.ui.GameUI;
import de.amr.pacmanfx.ui.UIConfig;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.GameScene2D_Renderer;
import de.amr.pacmanfx.ui.d2.HeadsUpDisplay_Renderer;
import de.amr.pacmanfx.ui.sound.GameSoundEffects;
import de.amr.pacmanfx.ui.sound.SoundID;
import de.amr.pacmanfx.ui.sound.SoundManager;
import de.amr.pacmanfx.uilib.animation.SpriteAnimationManager;
import de.amr.pacmanfx.uilib.assets.AssetMap;
import de.amr.pacmanfx.uilib.assets.ResourceManager;
import de.amr.pacmanfx.uilib.rendering.ActorRenderer;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.image.Image;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.ResourceBundle;

import static de.amr.pacmanfx.uilib.rendering.ArcadePalette.ARCADE_RED;

public class PacManXXL_MsPacMan_UIConfig implements UIConfig, ResourceManager {

    @Override
    public Class<?> resourceRootClass() {
        return ArcadeMsPacMan_UIConfig.class;
    }

    private final AssetMap assets = new AssetMap();
    private final ArcadeMsPacMan_Factory3D factory3D = new ArcadeMsPacMan_Factory3D();
    private final GameSceneConfig gameSceneConfig = new PacManXXL_MsPacMan_GameSceneConfig();
    private GameSoundEffects soundEffects;

    public PacManXXL_MsPacMan_UIConfig() {}

    @Override
    public void init(GameUI ui) {
        Logger.info("Load assets of UI configuration {}", getClass().getSimpleName());
        loadAssets();
        initSound(ui.soundManager());
    }

    @Override
    public void dispose() {
        Logger.info("Dispose UI configuration {}:", getClass().getSimpleName());
        disposeAssets();
        gameSceneConfig.dispose();
    }

    @Override
    public GameSceneConfig gameSceneConfig() {
        return gameSceneConfig;
    }

    @Override
    public AssetMap assets() {
        return assets;
    }

    @Override
    public ArcadeMsPacMan_Factory3D factory3D() {
        return factory3D;
    }

    private void loadAssets() {
        assets.clear();
        assets.set("app_icon", loadImage("graphics/icons/mspacman.png"));
        assets.set("logo.midway", loadImage("graphics/midway_logo.png"));
        assets.set("color.game_over_message", ARCADE_RED);
        assets.setLocalizedTexts(ResourceBundle.getBundle("de.amr.pacmanfx.arcade.pacman_xxl.localized_texts_ms_pacman"));
    }

    private void initSound(SoundManager soundManager) {
        soundManager.registerMediaPlayer(SoundID.BONUS_ACTIVE,      url("sound/Fruit_Bounce.mp3"));
        soundManager.registerAudioClipURL(SoundID.BONUS_EATEN,      url("sound/Fruit.mp3"));
        soundManager.registerAudioClipURL(SoundID.COIN_INSERTED,    url("sound/credit.wav"));
        soundManager.registerAudioClipURL(SoundID.EXTRA_LIFE,       url("sound/ExtraLife.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_OVER,         url("sound/game-over.mp3"));
        soundManager.registerMediaPlayer(SoundID.GAME_READY,        url("sound/Start.mp3"));
        soundManager.registerAudioClipURL(SoundID.GHOST_EATEN,      url("sound/Ghost.mp3"));
        soundManager.registerMediaPlayer(SoundID.GHOST_RETURNS,     url("sound/GhostEyes.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_1,    url("sound/Act_1_They_Meet.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_2,    url("sound/Act_2_The_Chase.mp3"));
        soundManager.registerMediaPlayer(SoundID.INTERMISSION_3,    url("sound/Act_3_Junior.mp3"));
        soundManager.registerAudioClipURL(SoundID.LEVEL_CHANGED,    url("sound/sweep.mp3"));
        soundManager.registerMediaPlayer(SoundID.LEVEL_COMPLETE,    url("sound/level-complete.mp3"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_DEATH,     url("sound/Died.mp3"));
        soundManager.registerAudioClipURL(SoundID.PAC_MAN_MUNCHING, url("sound/munch.wav"));
        soundManager.registerMediaPlayer(SoundID.PAC_MAN_POWER,     url("sound/ScaredGhost.mp3"));

        soundManager.createSirenPlayer(
            url("sound/GhostNoise1.wav"),
            url("sound/GhostNoise2.wav"),
            url("sound/GhostNoise3.wav"),
            url("sound/GhostNoise4.wav")
        );

        soundEffects = new GameSoundEffects(soundManager);
        soundEffects.setMunchingSoundDelay((byte) 0);
        soundEffects.setSirenVolume(0.33f);
    }

    @Override
    public ArcadeMsPacMan_SpriteSheet spriteSheet() {
        return ArcadeMsPacMan_SpriteSheet.instance();
    }

    @Override
    public Rectangle2D spriteRegionForArcadeBootScene() {
        return new Rectangle2D(380, 0, 204, 208);
    }

    @Override
    public Optional<GameSoundEffects> soundEffects() {
        return Optional.ofNullable(soundEffects);
    }

    @Override
    public WorldMapColorScheme colorScheme(WorldMap worldMap) {
        return enhanceContrast(worldMap.getConfigValue(WorldMapConfigKey.COLOR_SCHEME));
    }

    @Override
    public GameScene2D_Renderer createGameSceneRenderer(GameScene2D gameScene2D, Canvas canvas) {
        final GameScene2D_Renderer renderer = switch (gameScene2D) {
            case Arcade_BootScene2D        ignored -> new Arcade_BootScene2D_Renderer(gameScene2D, canvas, spriteSheet(), spriteRegionForArcadeBootScene());
            case ArcadeMsPacMan_IntroScene ignored -> new ArcadeMsPacMan_IntroScene_Renderer(this, gameScene2D, canvas);
            case ArcadeMsPacMan_StartScene ignored -> new ArcadeMsPacMan_StartScene_Renderer(gameScene2D, canvas);
            case Arcade_PlayScene2D        ignored -> new Arcade_PlayScene2D_Renderer(gameScene2D, canvas, spriteSheet());
            case ArcadeMsPacMan_CutScene1  ignored -> new ArcadeMsPacMan_CutScene1_Renderer(this, gameScene2D, canvas);
            case ArcadeMsPacMan_CutScene2  ignored -> new ArcadeMsPacMan_CutScene2_Renderer(this, gameScene2D, canvas);
            case ArcadeMsPacMan_CutScene3  ignored -> new ArcadeMsPacMan_CutScene3_Renderer(this, gameScene2D, canvas);
            default -> throw new IllegalStateException("Unexpected value: " + gameScene2D);
        };
        return gameScene2D.adaptRenderer(renderer);
    }

    @Override
    public PacManXXL_MsPacMan_GameLevelRenderer createGameLevelRenderer(Canvas canvas) {
        return new PacManXXL_MsPacMan_GameLevelRenderer(canvas);
    }

    @Override
    public HeadsUpDisplay_Renderer createHUDRenderer(GameScene2D gameScene2D, Canvas canvas) {
        final var hudRenderer = new ArcadeMsPacMan_HeadsUpDisplayRenderer(canvas);
        hudRenderer.setImageSmoothing(true);
        gameScene2D.adaptRenderer(hudRenderer);
        return hudRenderer;
    }

    @Override
    public ActorRenderer createActorRenderer(Canvas canvas) {
        final var actorRenderer = new ArcadeMsPacMan_ActorRenderer(canvas);
        actorRenderer.setImageSmoothing(true);
        return actorRenderer;
    }

    @Override
    public Ghost createGhostWithAnimations(byte personality) {
        final Ghost ghost = ArcadeMsPacMan_GameModel.createGhost(personality);
        ghost.setAnimations(createGhostAnimations(personality));
        ghost.selectAnimation(Ghost.AnimationID.GHOST_NORMAL);
        return ghost;
    }

    @Override
    public SpriteAnimationManager<SpriteID> createGhostAnimations(byte personality) {
        return new ArcadeMsPacMan_GhostAnimations(personality);
    }

    @Override
    public SpriteAnimationManager<SpriteID> createPacAnimations() {
        return new ArcadeMsPacMan_PacAnimations();
    }

    @Override
    public Image killedGhostPointsImage(int killedIndex) {
        final RectShort[] numberSprites = spriteSheet().sprites(SpriteID.GHOST_NUMBERS);
        return spriteSheet().image(numberSprites[killedIndex]);
    }

    @Override
    public Image bonusSymbolImage(byte symbol) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.BONUS_SYMBOLS);
        return spriteSheet().image(sprites[symbol]);
    }

    @Override
    public Image bonusValueImage(byte symbol) {
        final RectShort[] sprites = spriteSheet().sprites(SpriteID.BONUS_VALUES);
        return spriteSheet().image(sprites[symbol]);
    }
}