/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui2d.dashboard;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.lib.timer.TickTimer;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui2d.GameContext;
import de.amr.games.pacman.ui2d.util.SpriteAnimationCollection;

import java.util.function.BiFunction;
import java.util.function.Supplier;

import static de.amr.games.pacman.lib.timer.TickTimer.ticksToString;

/**
 * @author Armin Reichert
 */
public class InfoBoxActorInfo extends InfoBox {

    public void init(GameContext context) {
        super.init(context);

        labelledValue("Pac Name", pacInfo((game, pac) -> pac.name()));
        labelledValue("Movement", pacInfo(this::movementInfo));
        labelledValue("Tile",     pacInfo(this::locationInfo));
        labelledValue("Power", () -> {
            TickTimer powerTimer = context.game().powerTimer();
            return powerTimer.isRunning()
                ? "Remaining: %s".formatted(ticksToString(powerTimer.remaining()))
                : "No Power";
        });
        emptyRow();
        ghostInfo(GameModel.RED_GHOST);
        emptyRow();
        ghostInfo(GameModel.PINK_GHOST);
        emptyRow();
        ghostInfo(GameModel.CYAN_GHOST);
        emptyRow();
        ghostInfo(GameModel.ORANGE_GHOST);
    }

    private Supplier<String> pacInfo(BiFunction<GameModel, Pac, String> fnPacInfo) {
        return ifLevelPresent(level -> fnPacInfo.apply(context.game(), context.game().pac()));
    }

    private String locationInfo(GameModel game, Creature guy) {
        Vector2i tile = guy.tile();
        Vector2f offset = guy.offset();
        return "(%2d,%2d)+(%2.0f,%2.0f)%s".formatted(
            tile.x(), tile.y(),
            offset.x(), offset.y(),
            guy.isNewTileEntered() ? " NEW" : "");
    }

    private String movementInfo(GameModel game, Creature guy) {
        var speed = guy.velocity().length() * context.gameClock().getActualFrameRate();
        var blocked = !guy.moveInfo().moved;
        var reverseText = guy.gotReverseCommand() ? "REV!" : "";
        return blocked
            ? "BLOCKED!"
            : "%.2fpx/s %s (%s)%s".formatted(speed, guy.moveDir(), guy.wishDir(), reverseText);
    }

    private void ghostInfo(byte ghostID) {
        labelledValue(ghostColorName(ghostID) + " Ghost", fnGhostInfo(this::ghostNameAndState, ghostID));
        labelledValue("Animation",      fnGhostInfo(this::ghostAnimation, ghostID));
        labelledValue("Movement",       fnGhostInfo(this::movementInfo, ghostID));
        labelledValue("Tile",           fnGhostInfo(this::locationInfo, ghostID));
    }

    private Supplier<String> fnGhostInfo(
        BiFunction<GameModel, Ghost, String> fnGhostInfo, // (game, ghost) -> info text about this ghost
        byte ghostID)
    {
        return ifLevelPresent(level -> fnGhostInfo.apply(context.game(), context.game().ghost(ghostID)));
    }

    private String ghostColorName(byte ghostID) {
        return switch (ghostID) {
            case GameModel.RED_GHOST -> "Red";
            case GameModel.PINK_GHOST -> "Pink";
            case GameModel.CYAN_GHOST -> "Cyan";
            case GameModel.ORANGE_GHOST -> "Orange";
            default -> "";
        };
    }

    private String ghostNameAndState(GameModel game, Ghost ghost) {
        String name = ghost.name();
        if (ghost.id() == GameModel.RED_GHOST && game.cruiseElroyState() > 0) {
            name = "Elroy" + game.cruiseElroyState();
        }
        return String.format("%s (%s)", name, ghostState(game, ghost));
    }

    private String ghostAnimation(GameModel game, Ghost ghost) {
        if (ghost.animations().isEmpty()) {
            return InfoText.NO_INFO;
        }
        SpriteAnimationCollection sa = (SpriteAnimationCollection) ghost.animations().get();
        return sa.currentAnimationName() != null ? sa.currentAnimationName() : InfoText.NO_INFO;
    }

    private String ghostState(GameModel game, Ghost ghost) {
        var stateText = ghost.state() != null ? ghost.state().name() : "undefined";
        if (ghost.state() == GhostState.HUNTING_PAC) {
            stateText = game.isScatterPhase(game.huntingPhaseIndex()) ? "Scattering" : "Chasing";
        }
        return stateText;
    }
}