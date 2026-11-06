/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */

package de.amr.pacmanfx.tengenmspacman.gamestate;

import de.amr.basics.math.Vector2f;
import de.amr.basics.math.Vector2i;
import de.amr.basics.ui.entities.props.messageview.MessageType;
import de.amr.basics.ui.entities.props.messageview.MessageView;
import de.amr.basics.ui.rendering.BaseRenderer;
import de.amr.pacmanfx.core.GameConstants;
import de.amr.pacmanfx.core.GameContext;
import de.amr.pacmanfx.core.SessionKey;
import de.amr.pacmanfx.core.entities.world.House;
import de.amr.pacmanfx.core.event.HighScoreAccessErrorEvent;
import de.amr.pacmanfx.core.gamestate.AbstractGameState;
import de.amr.pacmanfx.core.gamestate.CommonGameStateID;
import de.amr.pacmanfx.core.level.GameLevel;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GameSystems;
import de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_UIConfig;
import de.amr.pacmanfx.tengenmspacman.entities.messageview.MessageAnimationComp;
import de.amr.pacmanfx.tengenmspacman.model.MapCategory;
import de.amr.pacmanfx.tengenmspacman.rendering.TengenMsPacMan_RenderConfig;
import de.amr.pacmanfx.ui.assets.GlobalFonts;
import javafx.scene.text.Font;
import org.tinylog.Logger;

import java.io.IOException;

import static de.amr.basics.TileDimension.TS;
import static de.amr.pacmanfx.tengenmspacman.TengenMsPacMan_GamePlay.gameOptionValues;

public class Tengen_GameOverState extends AbstractGameState {

    public static final int GAME_OVER_MESSAGE_DELAY_SEC = 2;
    public static final int COUNTDOWN_AFTER_ANIMATION = 180;

    private long countdownAfter;
    private GameLevel level;

    public Tengen_GameOverState() {
        super(CommonGameStateID.GAME_OVER);
    }

    @Override
    public void doEnterState(GameContext game) {
        level = game.session().optLevel().orElseThrow();

        countdownAfter = 0;

        game.session().store(SessionKey.GAME_RUNNING, false);
        game.session().cheats().clear();

        try {
            game.systems().scoreSystem().saveHighScoreIfNeeded(game.session().hud().highScore());
        } catch (IOException e) {
            game.eventManager().publishEvent(new HighScoreAccessErrorEvent(e));
        }

        level.showMessage(MessageType.GAME_OVER);

        final MapCategory mapCategory = gameOptionValues(game.session()).mapCategory();
        if (!game.session().booleanValue(SessionKey.DEMO_LEVEL_MODE) && mapCategory != MapCategory.ARCADE) {
            final MessageView messageView = level.entitySet().theOne(MessageView.class);
            createAndStartMessageAnimation(messageView, game);
            stateTimer().restartIndefinitely(); // animation completion triggers state exit
        }
        else {
            stateTimer().restartTicks(game.session().value(SessionKey.GAME_OVER_STATE_TICKS, Long.class));
        }
    }

    @Override
    public void doUpdate(GameContext game, long globalTickCount, long stateTickCount) {
        if (countdownAfter > 0) {
            --countdownAfter;
            if (countdownAfter == 0) {
                stateTimer().expire();
            }
        }

        if (stateTimer().hasExpired()) {
            if (game.session().booleanValue(SessionKey.DEMO_LEVEL_MODE)) {
                game.flow().enterGameState(game, TengenMsPacMan_GameStateID.SHOWING_HALL_OF_FAME);
                return;
            }
            final boolean continueGame = TengenMsPacMan_GamePlay.checkGameContinuesOnGameOver(game.session());
            game.flow().enterGameState(game, continueGame ? CommonGameStateID.GAME_PREPARATION : CommonGameStateID.GAME_INTRO);
            return;
        }

        // Show animated game over message moving horizontally over scene and wrapping around
        final MessageView messageView = level.entitySet().theOne(MessageView.class);
        messageView.optComponent(MessageAnimationComp.class).ifPresent(messageAnimation -> {
            final var tSystems = (TengenMsPacMan_GameSystems) game.systems();
            if (messageAnimation.finished() && countdownAfter == 0) {
                countdownAfter = COUNTDOWN_AFTER_ANIMATION;
            } else {
                tSystems.messageAnimationSystem().update(messageView);
            }
        });
    }

    @Override
    public void onExit(GameContext game) {
        game.session().optLevel().orElseThrow().clearMessage();
        final MessageView messageView = level.entitySet().theOne(MessageView.class);
        messageView.removeComponent(MessageAnimationComp.class);
    }

    private void createAndStartMessageAnimation(MessageView messageView, GameContext game) {
        final MessageAnimationComp messageAnimation = new MessageAnimationComp();

        // Compute exact message size and wrap position at right border
        final Font font = GlobalFonts.ARCADE.font();
        final String gameOverText = TengenMsPacMan_RenderConfig.MESSAGE_TEXTS.get(MessageType.GAME_OVER);
        final double width = BaseRenderer.textWidth(gameOverText, font);
        final double wrapX = TengenMsPacMan_UIConfig.NES_SCREEN_WIDTH + 0.5 * width;

        messageAnimation.setWidth(width);
        messageAnimation.setWrapX(wrapX);
        messageAnimation.setDelayTicks(GAME_OVER_MESSAGE_DELAY_SEC * GameConstants.SIMULATION_FPS);
        messageView.setComponent(MessageAnimationComp.class, messageAnimation);

        Logger.info("Message animation bounds computed: width={}, wrapX={}", width, wrapX);

        final var tSystems = (TengenMsPacMan_GameSystems) game.systems();
        tSystems.messageAnimationSystem().start(
            messageView,
            computeMessageStartPosition(),
            GAME_OVER_MESSAGE_DELAY_SEC * GameConstants.SIMULATION_FPS
        );
    }

    private Vector2f computeMessageStartPosition() {
        final House house = level.entitySet().theOne(House.class);
        final Vector2i houseSize = house.sizeInTiles();
        // Compute center position under house
        return house.floorplan().minTile()
            .toVector2f()
            .plus(houseSize.x() * 0.5f, houseSize.y() + 1)
            .scaled(TS);
    }
}
