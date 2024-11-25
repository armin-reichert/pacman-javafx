/*
Copyright (c) 2021-2024 Armin Reichert (MIT License)
See file LICENSE in repository root directory for details.
*/
package de.amr.games.pacman.lib.tilemap;

import de.amr.games.pacman.lib.Direction;
import de.amr.games.pacman.lib.Vector2i;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * @author Armin Reichert
 */
public class TileMapPath implements Iterable<Vector2i> {

    private final Vector2i startTile;
    private final List<Vector2i> vectors = new ArrayList<>();

    public TileMapPath(Vector2i startTile) {
        this.startTile = Objects.requireNonNull(startTile);
    }

    public Vector2i startTile() {
        return startTile;
    }

    public int size() {
        return vectors.size();
    }

    public void add(Direction dir) {
        Objects.requireNonNull(dir);
        vectors.add(dir.vector());
    }

    public Vector2i vector(int i) {
        return vectors.get(i);
    }

    @Override
    public Iterator<Vector2i> iterator() {
        return vectors.iterator();
    }
}