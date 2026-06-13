/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.arcade.pacman.scenes;

import de.amr.basics.math.Vector2i;
import de.amr.pacmanfx.arcade.pacman.Arcade_Actions;
import de.amr.pacmanfx.arcade.pacman.ArcadePacMan_UIConfig;
import de.amr.pacmanfx.gamestate.GameState;
import de.amr.pacmanfx.gamestate.GameStateID;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.ArcadePacMan_AnimationID;
import de.amr.pacmanfx.model.actors.Pac;
import de.amr.pacmanfx.model.level.GameLevel;
import de.amr.pacmanfx.ui.d2.GameScene2D;
import de.amr.pacmanfx.ui.d2.LevelCompletedAnimation;
import de.amr.pacmanfx.ui.game.Game;
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

    public Arcade_PlayScene2D(Game game) {
        super(game);
        setGameEventHandler(new Arcade_PlayScene2DGameEventHandler(this));
    }

    @Override
    public void onTick(long tick) {
        gameContext().optCurrentLevel().ifPresent(level -> {
            updateLivesCounter(gameState(), gameModel(), level.entities().pac());
            optSoundEffects().ifPresent(sfx -> sfx.playAmbientGameLevelSound(gameContext(), level));
        });
    }

    @Override
    public void handleQuit(Game game) {
        if (gameModel().isPlaying()) {
            gameContext().optCurrentLevel().ifPresent(level -> gameModel().onGameOver(gameContext(), level));
        }
        gameModel().cheats().clear();
        gameModel().lives().setCount(0);
        onDeactivate();
    }

    @Override
    public Optional<ContextMenu> supplyContextMenu() {
        final TranslationManager translations = game().ui().translations();
        final var contextMenu = new ContextMenu();

        addLocalizedTitleItem(contextMenu, translations, "pacman");

        addLocalizedCheckBox(contextMenu, translations, gameModel().cheats().pacUsingAutopilotProperty(), "autopilot").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                game().cheatActions().ACTION_ACTIVATE_AUTOPILOT.execute(game());
            } else {
                game().cheatActions().ACTION_DEACTIVATE_AUTOPILOT.execute(game());
            }
        });

        addLocalizedCheckBox(contextMenu, translations, gameModel().cheats().pacImmuneProperty(), "immunity").setOnAction(e -> {
            final var checkBox = (CheckMenuItem) e.getSource();
            if (checkBox.isSelected()) {
                game().cheatActions().ACTION_ACTIVATE_IMMUNITY.execute(game());
            } else {
                game().cheatActions().ACTION_DEACTIVATE_IMMUNITY.execute(game());
            }
        });

        addSeparator(contextMenu);

        addLocalizedCheckBox(contextMenu, translations, game().ui().settings().mutedProperty, "muted");

        addLocalizedActionItem(contextMenu, game(), translations, game().commonActions().ACTION_QUIT, "quit");

        return Optional.of(contextMenu);
    }

    @Override
    public void onEnteredFrom3DScene() {
        gameContext().optCurrentLevel().ifPresent(this::acceptGameLevel);
    }

    public void setLevelCompletedAnimation(LevelCompletedAnimation levelCompletedAnimation) {
        this.levelCompletedAnimation = levelCompletedAnimation;
    }

    // Expose animation to scene renderer
    public Optional<LevelCompletedAnimation> optLevelCompletedAnimation() {
        return Optional.ofNullable(levelCompletedAnimation);
    }

    @Override
    public void acceptGameLevel(GameLevel level) {
        final Vector2i terrainSize = level.worldMap().terrainLayer().sizeInPixel();
        unscaledWidthProperty().set(terrainSize.x());
        unscaledHeightProperty().set(terrainSize.y());

        if (level.isDemoLevel()) {
            acceptDemoLevel();
        } else {
            acceptNormalLevel(level);
        }
    }

    // Called from game event handler
    public void resetActorAnimations(GameLevel level) {
        final Pac pac = level.entities().pac();
        pac.animations().select(ArcadePacMan_AnimationID.PAC_MUNCHING);
        pac.animations().resetSelected();
        level.entities().ghosts().forEach(ghost -> {
            ghost.animations().select(ArcadePacMan_AnimationID.GHOST_NORMAL);
            ghost.animations().resetSelected();
        });
    }

    private void acceptNormalLevel(GameLevel level) {
        actionBindings().registerAllBindings(game().commonActions().steeringActionBindings);
        actionBindings().registerAllBindings(game().commonActions().cheatActionBindings);

        Logger.info(actionBindings());

        game().ui().sounds().setEnabled(true);

        Logger.info("Game scene {} accepted game level #{}", getClass().getSimpleName(), level.number());
    }

    private void acceptDemoLevel() {
        final Arcade_Actions actions = game().ui().extensions()
            .getExtension(ArcadePacMan_UIConfig.EXT_ARCADE_ACTIONS, Arcade_Actions.class);

        actionBindings().registerAllBindings(actions.GAME_START_ACTION_BINDINGS);
        Logger.info(actionBindings());

        game().ui().sounds().setEnabled(false);

        Logger.info("Game scene {} accepted demo level", getClass().getSimpleName());
    }

    // While Pac-Man is not yet visible on game/level start, an additional lives symbol more is shown in the counter
    private void updateLivesCounter(GameState gameState, GameModel gameModel, Pac pac) {
        final boolean oneMore = GameStateID.GAME_OR_LEVEL_STARTING.identifies(gameState) && !pac.isVisible();
        final int livesToDisplay = gameModel.lives().count() - 1 + (oneMore ? 1 : 0);
        final int livesDisplayed = Math.clamp(livesToDisplay, 0, gameModel.hud().maxLivesDisplayed());
        gameModel.hud().setVisibleLifeCount(livesDisplayed);
    }
}