/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamescene.playscene;

import de.amr.basics.InfoMap;
import de.amr.basics.math.RectShort;
import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.assets.TranslationManager;
import de.amr.basics.ui.ecs.system.ActorSpriteAnimController;
import de.amr.basics.ui.rendering.GameEntityView;
import de.amr.basics.ui.rendering.Renderable;
import de.amr.basics.ui.rendering.RenderingLayer;
import de.amr.basics.ui.spriteanim.SpriteAnimationContainer;
import de.amr.basics.util.Ufx;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.GameSession;
import de.amr.pacmanfx.core.HUD;
import de.amr.pacmanfx.core.entities.actor.pac.Pac;
import de.amr.pacmanfx.core.entities.world.Door;
import de.amr.pacmanfx.core.entities.world.DoorDataComp;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.event.base.GameEventListener;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.core.model.world.map.TerrainLayer;
import de.amr.pacmanfx.core.model.world.map.WorldMap;
import de.amr.pacmanfx.core.model.world.map.WorldMapConfigKey;
import de.amr.pacmanfx.engine.GameVariantRenderConfig;
import de.amr.pacmanfx.engine.GameVariantRuntime;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_Actions;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameExtension;
import de.amr.pacmanfx.tengenmspacman.config.TengenMsPacMan_UISettings;
import de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderInfoKey;
import de.amr.pacmanfx.tengenmspacman.sprites.ColorSchemedMapSprite;
import de.amr.pacmanfx.tengenmspacman.sprites.MapImageSet;
import de.amr.pacmanfx.tengenmspacman.sprites.NonArcadeMapsSpriteSheet;
import de.amr.pacmanfx.tengenmspacman.sprites.TengenMsPacMan_MapRepository;
import de.amr.pacmanfx.ui.action.CommonGameActions;
import de.amr.pacmanfx.ui.gamescene.common.AbstractGameScene;
import de.amr.pacmanfx.ui.gamescene.d2.FlashingState;
import de.amr.pacmanfx.ui.gamescene.d2.GameLevelView;
import de.amr.pacmanfx.ui.gamescene.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.viewmodel.Game2DSettingsVM;
import de.amr.pacmanfx.uilib.view2d.LevelRenderInfoKey;
import de.amr.pacmanfx.uilib.view2d.RenderingSurface;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.layout.StackPane;
import org.tinylog.Logger;

import java.util.Optional;
import java.util.stream.Stream;

import static de.amr.basics.TileDimension.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptionValues;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_HEIGHT;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH;
import static de.amr.pacmanfx.tengenmspacman.gamescene.SceneDisplay.SCROLLING;
import static de.amr.pacmanfx.tengenmspacman.sprites.NonArcadeMapsSpriteSheet.MapID.MAP32_ANIMATED;
import static de.amr.pacmanfx.ui.views.ContextMenuSupport.*;

/**
 * Tengen Ms. Pac-Man play scene, uses vertical scrolling by default to accommodate to NES screen size.
 */
public class TengenMsPacMan_PlayScene2D extends AbstractGameScene {

    // The indent such that the scene content appears horizontally centered
    public static int OFFSET_X = 2 * TS;

    // Additional 2 tiles below world map for HUD display
    public static final int EXTRA_SPACE_BELOW_MAP = 2 * TS;

    // In Tengen Ms.Pac-Man, all maps are 28 tiles wide. The NES screen width however is 32 tiles,
    // so 16 pixels on each side are clipped and the map is horizontally centered inside the available space.
    // (One additional pixel is clipped on the right side to hide noise caused by the spritesheet image.)
    public static final RectShort CLIP_RECT = new RectShort(
        OFFSET_X, 0,
        NES_SCREEN_WIDTH - 2 * OFFSET_X - 1, Short.MAX_VALUE
    );

    private final StackPane rootPane = new StackPane();

    private LevelCompletedAnimation levelCompletedAnimation;

    private final GameEventHandler eventHandler = new GameEventHandler(this);

    public TengenMsPacMan_PlayScene2D() {
        view2D().setRenderingSurface(new RenderingSurface());
        view2D().unscaledWidthProperty().set(NES_SCREEN_WIDTH);
        view2D().unscaledHeightProperty().set(NES_SCREEN_HEIGHT);
        view2D().setClipRect(CLIP_RECT);
    }

    @Override
    protected void onEngineConnected() {
        final Game2DSettingsVM settings = viewModel().common2DSettings();
        rootPane.backgroundProperty().bind(settings.canvasBackgroundColorProperty().map(Background::fill));
    }

    @Override
    public Optional<GameEventListener> optGameEventHandler() {
        return Optional.of(eventHandler);
    }

    @Override
    public Stream<Renderable> renderables() {
        final Vector2f offset = new Vector2f(OFFSET_X, 0);
        return game().session().optLevel()
            .map(level -> Ufx.<Renderable>streamOf(
                createGameLevelView(level, offset, game().session().thisFrame().tick()),
                createEntityViews(level,offset))
            )
            .orElse(Stream.empty());
    }

    @Override
    public void onEnteredFrom3DScene() {
        final GameSession session = game().session();

        final HUD hud = session.hud();
        hud.levelCounter().show();
        hud.livesCounter().show();
        session.setHudVisible(true);

        session.optLevel().ifPresent(level -> onAcceptGameLevel(session, level));
    }

    @Override
    public void onActivate() {
        final GameSession session = game().session();
        final HUD hud = session.hud();

        hud.gameScore().show();
        hud.levelCounter().show();
        hud.livesCounter().show();
        session.setHudVisible(true);
    }

    @Override
    public void onDeactivate() {
    }

    @Override
    public void onTick(GameContext game) {
        final GameSession session = game.session();
        session.optLevel().ifPresent(level -> {
            ensureActorAnimationsCreated(level, gameOptionValues(session).boosterEnabled());
            optSoundEffects().ifPresent(soundEffects -> {
                soundEffects.setEnabled(!session.isAttractMode());
                soundEffects.playAmbientGameLevelSound(game(), level);
            });
        });
    }

    @Override
    public void onQuit() {
        onDeactivate();
        gameFlow().enterGameState(game(), CommonGameStateID.GAME_OVER);
    }

    @Override
    public Optional<ContextMenu> optContextMenu() {
        final var uiSettings = uiSettings();

        final TranslationManager translations = engine().translationManager();
        final SceneDisplay displayMode = uiSettings.playSceneDisplay.get();
        final var contextMenu = new ContextMenu();

        final RadioMenuItem miScaledToFit = addLocalizedRadioButton(contextMenu, translations, "context_menu.scaled_to_fit");
        miScaledToFit.setSelected(displayMode == SceneDisplay.SCALED_TO_FIT);
        miScaledToFit.setOnAction(_ -> uiSettings.playSceneDisplay.set(SceneDisplay.SCALED_TO_FIT));

        final RadioMenuItem miScrolling = addLocalizedRadioButton(contextMenu, translations, "context_menu.scrolling");
        miScrolling.setSelected(displayMode == SCROLLING);
        miScrolling.setOnAction(_ -> uiSettings.playSceneDisplay.set(SCROLLING));

        final ToggleGroup toggleGroup = new ToggleGroup();
        miScaledToFit.setToggleGroup(toggleGroup);
        miScrolling.setToggleGroup(toggleGroup);

        addLocalizedTitleItem(contextMenu, translations, "context_menu.pacman");
        addLocalizedCheckBox(contextMenu, translations, game().session().cheats().pacUsingAutopilotProperty(), "context_menu.autopilot");
        addLocalizedCheckBox(contextMenu, translations, game().session().cheats().pacImmuneProperty(), "context_menu.immunity");
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, translations, engine().ui().viewModel().muteProperty(), "context_menu.muted");
        addLocalizedActionItem(engine(), contextMenu, translations, CommonGameActions.instance().gameFlowActions().actionQuit(), "context_menu.quit");

        return Optional.of(contextMenu);
    }

    @Override
    public void onAcceptGameLevel(GameSession session, GameLevel level) {
        final WorldMap worldMap = level.worldMap();
        final TerrainLayer terrain = worldMap.terrainLayer();
        final Vector2i size = terrain.sizeInPixel();

        // Adapt scene size to map height
        view2D().unscaledHeightProperty().set(size.y() + EXTRA_SPACE_BELOW_MAP);

        // Store the maze sprite set with the correct colors for this level in the map configuration:
        if (!worldMap.hasConfigValue(TengenMsPacMan_RenderInfoKey.MAP_IMAGE_SET)) {
            final int numFlashes = 3;
            final MapImageSet mapImageSet = TengenMsPacMan_MapRepository.instance().createMapImageSet(worldMap, numFlashes);
            worldMap.setConfigValue(TengenMsPacMan_RenderInfoKey.MAP_IMAGE_SET, mapImageSet);
            Logger.info("Maze sprite set created: {}", mapImageSet);

            final House house = level.entitySet().entities().theOne(House.class);
            final var doorData = house.door().assertComponent(DoorDataComp.class);
            doorData.setColor(mapImageSet.mapImage().colorScheme().door());
            Logger.info("Door color set to {}", doorData.color());
        }

        if (session.isAttractMode()) {
            acceptDemoLevel();
        } else {
            acceptNormalLevel();
        }

        Logger.info(actionBindingsRegistry());
        Logger.info("Scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    // private area, do NOT enter!

    private TengenMsPacMan_Actions actions() {
        return runtime().extensionValue(TengenMsPacMan_GameExtension.EXT_ACTIONS, TengenMsPacMan_Actions.class);
    }

    private TengenMsPacMan_UISettings uiSettings() {
        return runtime().extensionValue(TengenMsPacMan_GameExtension.EXT_UI_SETTINGS, TengenMsPacMan_UISettings.class);
    }

    private void acceptNormalLevel() {
        soundManager().setEnabled(true); //TODO needed?

        // Pac-Man is steered using keys simulating the NES "Joypad" buttons ("START", "SELECT", "B", "A" etc.)
        actionBindingsRegistry().registerAllBindings(actions().steeringBindings());

        actionBindingsRegistry().registerAllBindings(CommonGameActions.instance().cheatActions().bindings());

        actionBindingsRegistry().selectAnyMatchingBinding(actions().actionTogglePlaySceneDisplayMode(), actions().localBindings());
        actionBindingsRegistry().selectAnyMatchingBinding(actions().actionTogglePacBooster(), actions().localBindings());
    }

    private void acceptDemoLevel() {
        soundManager().setEnabled(false); //TODO needed?
        actionBindingsRegistry().selectAnyMatchingBinding(actions().actionTogglePlaySceneDisplayMode(), actions().localBindings());
        actionBindingsRegistry().selectAnyMatchingBinding(actions().actionQuitDemoLevel(), actions().localBindings());
    }

    void playLevelCompleteAnimation(GameLevel level, int numFlashes) {
        levelCompletedAnimation = new LevelCompletedAnimation();
        levelCompletedAnimation.setOnFinished(() -> game().state().triggerTimeout());
        levelCompletedAnimation.play(level, numFlashes);
    }

    private void ensureActorAnimationsCreated(GameLevel level, boolean boosterEnabled) {
        final GameVariantRuntime variantConfig = engine().gameVariantManager().currentRuntime();
        final GameVariantRenderConfig renderConfig = variantConfig.uiConfig().renderConfig();
        final SpriteAnimationContainer animContainer = variantConfig.spriteAnimContainer();
        final ActorSpriteAnimController animController = variantConfig.playConfig().systems().actorSpriteAnimController();

        final Pac pac = level.entitySet().pac();
        if (animController.hasNoAnimations(pac)) {
            animController.setAnimations(pac, renderConfig.createPacAnimations(animContainer));
            eventHandler.resetPacAnimation(animController, boosterEnabled, pac);
        }

        level.entitySet().ghosts().forEach(ghost -> {
            if (animController.hasNoAnimations(ghost)) {
                animController.setAnimations(ghost, renderConfig.createGhostAnimations(animContainer, ghost.personality()));
                eventHandler.resetGhostAnimation(animController, ghost);
            }
        });
    }

    private GameLevelView createGameLevelView(GameLevel level, Vector2f offset, long tick) {
        final InfoMap renderInfo = InfoMap.create();
        final WorldMap worldMap = level.worldMap();

        final int mapNumber = worldMap.getConfigValue(WorldMapConfigKey.MAP_NUMBER);
        final MapCategory mapCategory = worldMap.getConfigValue(TengenMsPacMan_RenderInfoKey.MAP_CATEGORY);
        final MapImageSet mapImageSet = worldMap.getConfigValue(TengenMsPacMan_RenderInfoKey.MAP_IMAGE_SET);
        final MapImageSet imageSet = worldMap.getConfigValue(TengenMsPacMan_RenderInfoKey.MAP_IMAGE_SET);

        final var flashing = flashingState();
        final boolean highlighted = flashing != null && flashing.isHighlighted();
        final int flashingIndex = flashing != null ? flashing.flashingIndex() : -1;

        renderInfo.put(TengenMsPacMan_RenderInfoKey.MAP_CATEGORY, mapCategory);
        renderInfo.put(TengenMsPacMan_RenderInfoKey.MAP_IMAGE_SET, mapImageSet);

        renderInfo.put(LevelRenderInfoKey.SHOW_BRIGHT_MAZE, highlighted);
        renderInfo.put(LevelRenderInfoKey.FLASHING_INDEX, flashingIndex);

        if (highlighted) {
            final int imageIndex = Math.clamp(flashingIndex, 0, imageSet.flashingMapImages().size() - 1);
            final ColorSchemedMapSprite flashingMapImage = imageSet.flashingMapImages().get(imageIndex);
            renderInfo.put(LevelRenderInfoKey.MAZE_IMAGE, flashingMapImage.spriteSheetImage());
            renderInfo.put(LevelRenderInfoKey.MAZE_SPRITE, flashingMapImage.sprite());
        }
        else {
            renderInfo.put(LevelRenderInfoKey.MAZE_IMAGE, imageSet.mapImage().spriteSheetImage());
            if (mapCategory == MapCategory.STRANGE && mapNumber == 15) {
                final int spriteIndex = strangeMap15AnimationFrame(tick);
                renderInfo.put(LevelRenderInfoKey.MAZE_SPRITE,
                    NonArcadeMapsSpriteSheet.instance().findSpriteSequence(MAP32_ANIMATED)[spriteIndex]);
            } else {
                renderInfo.put(LevelRenderInfoKey.MAZE_SPRITE, imageSet.mapImage().sprite());
            }
        }

        return new GameLevelView(level, renderInfo, RenderingLayer.LEVEL, 0, offset);
    }

    private Stream<GameEntityView> createEntityViews(GameLevel level, Vector2f offset) {
        final GameVariantRenderConfig renderConfig = runtime().uiConfig().renderConfig();
        final Door door = level.entitySet().entities().theOne(House.class).door();
        return Ufx.streamOf(
            level.entitySet().all()
                .filter(entity -> entity != door)
                .map(renderConfig::createEntityView)
                .map(entityView -> entityView.newOffset(offset)),

            // Ghosts are drawn under the door, so lift door z index up!
            new GameEntityView(door, RenderingLayer.ACTORS, 100, offset)
        );
    }

    private FlashingState flashingState() {
        return levelCompletedAnimation == null ? null : levelCompletedAnimation.optFlashingState().orElse(null);
    }

    /**
     * Strange map #15 (maze #32) has a "psychedelic" animation:
     * Frame pattern: (00000000 11111111 22222222 11111111)+, numFrames = 4, frameDuration = 8
     */
    private static int strangeMap15AnimationFrame(long tick) {
        final long phase = (tick % 32) / 8;
        return (int) (phase < 3 ? phase : 1);
    }
}