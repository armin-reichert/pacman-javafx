package de.amr.pacmanfx.core.model.component.ghost;

import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.component.EntityComponent;

import static java.util.Objects.requireNonNull;

public class GhostStateComponent implements EntityComponent {

    public static final GhostState DEFAULT_STATE = GhostState.LOCKED;

    private GhostState state;

    public GhostStateComponent() {
        state = DEFAULT_STATE;
    }

    public GhostState state() {
        return state;
    }

    public void setState(GhostState state) {
        this.state = requireNonNull(state);
    }

    @Override
    public void reset() {
        state = DEFAULT_STATE;
    }
}
