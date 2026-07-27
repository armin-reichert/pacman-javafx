package de.amr.pacmanfx.core.model.component.ghost;

import de.amr.pacmanfx.core.model.actors.GhostState;
import de.amr.pacmanfx.core.model.component.EntityComponent;

import static java.util.Objects.requireNonNull;

public class GhostStateComponent implements EntityComponent {

    private GhostState state;

    public GhostStateComponent() {
        state = GhostState.LOCKED;
    }

    public GhostState state() {
        return state;
    }

    public void setState(GhostState state) {
        this.state = requireNonNull(state);
    }

    @Override
    public void reset() {
        state = GhostState.LOCKED;
    }
}
