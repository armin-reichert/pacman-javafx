/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.action.CheatActions;
import de.amr.pacmanfx.ui.action.CommonActions;
import de.amr.pacmanfx.ui.app.AppConstants;
import de.amr.pacmanfx.ui.app.AppContext;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.uilib.assets.TranslationManager;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import org.tinylog.Logger;

import java.util.Optional;

import static de.amr.pacmanfx.ui.subviews.ContextMenuSupport.*;

/**
 * 2D play scene for Arcade game variants.
 */
public class Arcade_PlayScene2D extends GameScene2D {

    private LevelCompletedAnimation levelCompletedAnimation;

    public Arcade_PlayScene2D(AppContext context) {
        super(context);
        setGameEventHandler(new Arcade_PlayScene2DGameEventHandler(this));
    }

    public LevelCompletedAnimation levelCompletedAnimation() {
        return levelCompletedAnimation;
    }

    @Override
    public void onTick(long tick) {
        gameContext().optCurrentLevel().ifPresent(level -> {
            updateLivesCounter(level);
            appContext().currentSoundEffects().ifPresent(sfx -> {
                sfx.setEnabled(!level.isDemoLevel());
                sfx.playLevelRunningSound(gameContext(), level);
            });
        });
    }

    @Override
    public Optional<ContextMenu> supplyContextMenu() {
        final TranslationManager translations = appContext().ui().translations();
        final GameModel gameModel = gameContext().model();
        final var contextMenu = new ContextMenu();
        addLocalizedTitleItem(contextMenu, translations, "pacman");
        addLocalizedCheckBox(contextMenu, translations, gameModel.cheats().pacUsingAutopilotProperty(), "autopilot").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                CheatActions.ACTION_ACTIVATE_AUTOPILOT.executeIfEnabled(appContext());
            } else {
                CheatActions.ACTION_DEACTIVATE_AUTOPILOT.executeIfEnabled(appContext());
            }
        });
        addLocalizedCheckBox(contextMenu, translations, gameModel.cheats().pacImmuneProperty(), "immunity").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                CheatActions.ACTION_ACTIVATE_IMMUNITY.executeIfEnabled(appContext());
            } else {
                CheatActions.ACTION_DEACTIVATE_IMMUNITY.executeIfEnabled(appContext());
            }
        });
        addSeparator(contextMenu);
        addLocalizedCheckBox(contextMenu, translations, AppConstants.PROPERTY_MUTED, "muted");
        addLocalizedActionItem(contextMenu, appContext(), translations, CommonActions.ACTION_QUIT_GAME_SCENE, "quit");

        return Optional.of(contextMenu);
    }

    @Override
    public void onEnteredFrom3DScene() {
        appContext().currentGameContext().optCurrentLevel().ifPresent(this::acceptGameLevel);
    }

    // Others

    // Expose flashing animation state to renderer
    public Optional<LevelCompletedAnimation.FlashingState> optFlashingState() {
        return Optional.ofNullable(levelCompletedAnimation).flatMap(LevelCompletedAnimation::flashingState);
    }

    /**
     * If the 3D play scene is shown when the game level gets created, the onLevelCreated() method of this
     * scene is not called, so we have to accept the game level again when switching from the 3D scene to this one.
     */
    protected void acceptGameLevel(GameLevel level) {
        actionBindings().registerAllBindings(ArcadePacMan_UIConfig.GAME_START_ACTION_BINDINGS);
        if (!level.isDemoLevel()) {
            actionBindings().registerAllBindings(AppConstants.STEERING_ACTION_BINDINGS);
            actionBindings().registerAllBindings(AppConstants.CHEAT_ACTION_BINDINGS);
        }
        Logger.info(actionBindings());

        appContext().ui().sounds().setEnabled(!level.isDemoLevel()); //TODO is this needed?
        levelCompletedAnimation = new LevelCompletedAnimation(level, () -> gameContext().state().expire());

        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        unscaledWidthProperty().set(terrainSize.x());
        unscaledHeightProperty().set(terrainSize.y());

        Logger.info("Game scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    // Private

    // While Pac-Man is not yet visible on level start, one symbol more is shown in the lives counter
    private void updateLivesCounter(GameLevel level) {
        final GameModel gameModel = gameContext().model();
        final int additionalLives = GameStateID.GAME_OR_LEVEL_STARTING.identifies(gameContext().state())
            && !level.entities().pac().isVisible() ? 1 : 0;
        final int count = Math.clamp(gameModel.lives().count() - 1 + additionalLives, 0, gameModel.hud().maxLivesDisplayed());
        gameModel.hud().setVisibleLifeCount(count);
    }

    protected void resetActorAnimations(GameLevel level) {
        final Pac pac = level.entities().pac();
        pac.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pac.animations().resetSelected();
        level.entities().ghosts().forEach(ghost -> {
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.animations().resetSelected();
        });
    }
}