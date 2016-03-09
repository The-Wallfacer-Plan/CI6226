package models.core;

import com.google.common.base.MoreObjects;

public class LIndexOption {
    final boolean stemming;
    final boolean ignoreCase;
    final String swDict;

    public LIndexOption(boolean stemming, boolean ignoreCase, String swDict) {
        this.stemming = stemming;
        this.ignoreCase = ignoreCase;
        this.swDict = swDict;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("stemming", stemming)
                .add("ignoreCase", ignoreCase)
                .add("swDict", swDict)
                .toString();
    }
}
