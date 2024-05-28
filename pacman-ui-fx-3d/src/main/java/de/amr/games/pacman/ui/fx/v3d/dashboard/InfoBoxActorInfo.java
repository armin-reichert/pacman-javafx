/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.ui.fx.v3d.dashboard;

import de.amr.games.pacman.lib.Vector2f;
import de.amr.games.pacman.lib.Vector2i;
import de.amr.games.pacman.model.GameModel;
import de.amr.games.pacman.model.actors.Creature;
import de.amr.games.pacman.model.actors.Ghost;
import de.amr.games.pacman.model.actors.GhostState;
import de.amr.games.pacman.model.actors.Pac;
import de.amr.games.pacman.ui.fx.util.SpriteAnimations;
import de.amr.games.pacman.ui.fx.util.Theme;

import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * @author Armin Reichert
 */
public class InfoBoxActorInfo extends InfoBox {

    private Supplier<String> pacInfoIfPresent(BiFunction<GameModel, Pac, String> fnPacInfo) {
        return () -> context.game().level().isPresent()
            ? fnPacInfo.apply(context.game(), context.game().pac())
            : InfoText.NO_INFO;
    }

    private Supplier<String> ghostInfoIfPresent(
        BiFunction<GameModel, Ghost, String> fnGhostInfo, // (game, ghost) -> info text about this ghost
        byte ghostID)
    {
        return () -> context.game().level().isPresent()
            ? fnGhostInfo.apply(context.game(), context.game().ghost(ghostID))
            : InfoText.NO_INFO;
    }

    public InfoBoxActorInfo(Theme theme, String title) {
        super(theme, title);
        addPacInfo();
        emptyRow();
        addGhostInfo(GameModel.RED_GHOST);
        emptyRow();
        addGhostInfo(GameModel.PINK_GHOST);
        emptyRow();
        addGhostInfo(GameModel.CYAN_GHOST);
        emptyRow();
        addGhostInfo(GameModel.ORANGE_GHOST);
    }

    private void addPacInfo() {
        infoText("Pac Name", pacInfoIfPresent((game, pac) -> pac.name()));
        infoText("Movement", pacInfoIfPresent(this::movementInfo));
        infoText("Tile",     pacInfoIfPresent(this::locationInfo));
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
        var speed = guy.velocity().length();
        var blocked = !guy.lastMove().moved;
        var reverseText = guy.gotReverseCommand() ? "REV!" : "";
        return blocked
            ? "BLOCKED!"
            : "%.2fpx/s %s (%s)%s".formatted(speed, guy.moveDir(), guy.wishDir(), reverseText);
    }

    private void addGhostInfo(byte ghostID) {
        var color = switch (ghostID) {
            case GameModel.RED_GHOST -> "Red";
            case GameModel.PINK_GHOST -> "Pink";
            case GameModel.CYAN_GHOST -> "Cyan";
            case GameModel.ORANGE_GHOST -> "Orange";
            default -> "";
        };
        infoText(color + " Ghost", ghostInfoIfPresent(this::ghostNameAndState, ghostID));
        infoText("Animation",      ghostInfoIfPresent(this::ghostAnimation, ghostID));
        infoText("Movement",       ghostInfoIfPresent(this::movementInfo, ghostID));
        infoText("Tile",           ghostInfoIfPresent(this::locationInfo, ghostID));
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
        SpriteAnimations sa = (SpriteAnimations) ghost.animations().get();
        return sa.currentAnimationName() != null ? sa.currentAnimationName() : InfoText.NO_INFO;
    }

    private String ghostState(GameModel game, Ghost ghost) {
        var stateText = ghost.state() != null ? ghost.state().name() : "undefined";
        if (ghost.state() == GhostState.HUNTING_PAC) {
            stateText = game.currentHuntingPhaseName();
        }
        return stateText;
    }
}