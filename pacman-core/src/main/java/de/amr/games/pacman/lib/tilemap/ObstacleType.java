package de.amr.games.pacman.lib.tilemap;

import java.util.Set;
import java.util.stream.Stream;

/**
 * The encoding depends on the byte values in interface {@link Tiles}!
 */
public enum ObstacleType {
    CROSS_SHAPE("dfdgegfdfege",
            "dcfdgecgfcdfegce",
            "dfbdgbegfdbfebge",
            "dcfbdgbecgfcdbfebgce"),

    F_SHAPE,

    H_SHAPE("dgefdgbfegdfeb", "dcgfdegfcedfge",
            "dgbecfbdgbfebgcdbfeb", "dcgfcdbecgfcedcfbgce"),

    L_SHAPE(
            "dgegfceb", "dcgfdfeb", "dcgbfege", "dfdgbfce", // 8 segments
            "dcgbfegce", "dcfdgbfce", "dcgfcdfeb", "dgecgfceb", // 9 segments
            "dgbegfceb", "dcgfdbfeb", "dcgbfebge", "dfbdgbfce", // 9 segments
            "dcgbfebgce", "dcfbdgbfce", "dgbecgfceb", "dcgfcdbfeb" // 10 segments
    ),

    O_SHAPE("dgfe", "dcgfce", "dgbfeb", "dcgbfceb"),

    S_SHAPE("dcfdgbfcdfeb", "dgecgbfegceb", "dcgbegfcebge", "dfbdcgfdbfce",
            "dcfbdgbfcdbfeb", "dgbecgbfebgceb", "dcgbecgfcebgce", "dcfbdcgfcdbfce"),

    T_SHAPE("dgbecgfcdbfeb", "dcfbdgbfebgce", "dcgfcdbfebgce", "dcfbdgbecgfce"),

    U_SHAPE(
            "dcgbfcedfge", "dcgfdegfceb", "dcgbfegdfeb", "dgefdgbfceb",
            "dcgbfcedcfgce", "dcgfcdecgfceb", "dcgbfebgdbfeb", "dgbefbdgbfceb",
            "dcgbfcedcfbgce", "dcgfcdbecgfceb", "dcgbfebgcdbfeb", "dgbecfbdgbfceb"
    ),

    ANY;

    ObstacleType(String... encodingStrings) {
        encodings = Set.of(encodingStrings);
    }

    private final Set<String> encodings;

    public Stream<String> encodings() {
        return encodings.stream();
    }

    public boolean matches(String encoding) {
        return encodings.contains(encoding);
    }
}
