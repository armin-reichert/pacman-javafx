/*
 * Copyright (c) 2021-2026 Armin Reichert (MIT License)
 */
package de.amr.pacmanfx.core;

import de.amr.pacmanfx.flow.GameFlow;
import de.amr.pacmanfx.model.AbstractGameModel;
import de.amr.pacmanfx.model.GameModel;
import de.amr.pacmanfx.model.actors.CollisionStrategy;
import de.amr.pacmanfx.simulation.HuntingStepResult;
import javafx.beans.property.StringProperty;

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

    /**
     * The property holding the name (identifier) of the currently selected game variant.
     *
     * @return the observable property representing the selected game variant name
     */
    StringProperty gameVariantNameProperty();

    /**
     * Selects the game variant with the given name.
     * <p>
     * If no game with the specified name exists, an {@link IllegalArgumentException} is thrown.
     *
     * @param variantName the name of the game variant to select (must not be {@code null})
     * @throws IllegalArgumentException if no game with the given name is registered
     */
    default void select(String variantName) {
        requireNonNull(variantName);
        if (hasGameForVariantName(variantName)) {
            gameVariantNameProperty().set(variantName);
        }
        else {
            throw new IllegalArgumentException("Game with name '" + variantName + "' not found");
        }
    }

    /**
     * Returns the name (identifier) of the currently selected game variant.
     *
     * @return the current game variant name
     */
    default String gameVariantName() {
        return gameVariantNameProperty().get();
    }

    /**
     * Checks whether a game variant with the given name is registered.
     *
     * @param name the game variant name to check
     * @return {@code true} if a game with that name exists, {@code false} otherwise
     */
    boolean hasGameForVariantName(String name);

    /**
     * Returns the game model associated with the specified variant name.
     *
     * @param variantName the name of the game variant
     * @param <T>         the expected type of the game model
     * @return the game model for the given variant name
     * @throws ClassCastException if the registered game model cannot be cast to the expected type
     */
    <T extends GameModel> T gameForVariant(String variantName);

    /**
     * Returns the game model of the currently selected game variant.
     *
     * @param <T> the expected type of the game model
     * @return the game model of the active game variant
     * @throws ClassCastException if the registered game model type does not match the expected type
     */
    <T extends AbstractGameModel> T gameModel();

    default GameFlow gameFlow() {
        return gameModel().flow();
    }

    /**
     * @return the game clock driving the simulation
     */
    GameClock gameClock();

    /**
     * Returns the coin mechanism associated with the game box.
     * <p>
     * The coin mechanism is responsible for tracking inserted credits and
     * determining whether a game can be started.
     *
     * @return the coin mechanism
     */
    CoinMechanism coinMechanism();

    void setCollisionStrategy(CollisionStrategy collisionStrategy);

    Boolean isCollisionDoubleChecked();

    CollisionStrategy collisionStrategy();

    void setCollisionDoubleChecked(boolean doubleChecked);

    void startNewHuntingStep();

    HuntingStepResult huntingResult();
}
