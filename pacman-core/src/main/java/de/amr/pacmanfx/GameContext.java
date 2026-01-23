/*
Copyright (c) 2021-2026 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.pacmanfx;

import de.amr.pacmanfx.model.CoinMechanism;
import de.amr.pacmanfx.model.Game;
import de.amr.pacmanfx.model.GameVariant;
import javafx.beans.property.StringProperty;

import java.io.File;

import static java.util.Objects.requireNonNull;

/**
 * Provides access to the current game variant, its associated game model,
 * and shared game-box components such as the coin mechanism.
 * <p>
 * A {@code GameContext} acts as a central lookup for selecting and retrieving
 * game variants by name. Implementations are responsible for maintaining the
 * registry of available game variants.
 */
public interface GameContext {

    static File highScoreFile(String gameVariantName) {
        requireNonNull(gameVariantName);
        final String fileName = "highscore-%s.xml".formatted(gameVariantName).toLowerCase();
        return new File(GameBox.HOME_DIR, fileName);
    }

    static File highScoreFile(GameVariant gameVariant) {
        return highScoreFile(gameVariant.name());
    }

    /**
     * The property holding the name (identifier) of the currently selected game variant.
     *
     * @return the observable property representing the selected game variant name
     */
    StringProperty gameVariantNameProperty();

    /**
     * Returns the name (identifier) of the currently selected game variant.
     *
     * @return the current game variant name
     */
    default String gameVariantName() {
        return gameVariantNameProperty().get();
    }

    /**
     * Selects the game variant with the given name.
     * <p>
     * If no game with the specified name exists, an {@link IllegalArgumentException} is thrown.
     *
     * @param name the name of the game variant to select (must not be {@code null})
     * @throws IllegalArgumentException if no game with the given name is registered
     */
    default void selectGameByName(String name) {
        requireNonNull(name);
        if (hasGameWithName(name)) {
            gameVariantNameProperty().set(name);
        }
        else {
            throw new IllegalArgumentException("Game with name '" + name + "' not found");
        }
    }

    /**
     * Checks whether a game variant with the given name is registered.
     *
     * @param name the game variant name to check
     * @return {@code true} if a game with that name exists, {@code false} otherwise
     */
    boolean hasGameWithName(String name);

    /**
     * Returns the game model associated with the specified variant name.
     *
     * @param variantName the name of the game variant
     * @param <T>         the expected type of the game model
     * @return the game model for the given variant name
     * @throws ClassCastException if the registered game model cannot be cast to the expected type
     */
    <T extends Game> T gameByVariantName(String variantName);

    /**
     * Returns the game model of the currently selected game variant.
     *
     * @param <T> the expected type of the game model
     * @return the game model of the active game variant
     * @throws ClassCastException if the registered game model type does not match the expected type
     */
    <T extends Game> T currentGame();

    /**
     * Returns the coin mechanism associated with the game box.
     * <p>
     * The coin mechanism is responsible for tracking inserted credits and
     * determining whether a game can be started.
     *
     * @return the coin mechanism
     */
    CoinMechanism coinMechanism();
}
