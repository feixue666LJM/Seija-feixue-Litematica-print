package com.seija.printer.mixin;

/** Retained source-level marker for integrations that used the old text widget accessor. */
public interface WTextBoxAccessor {
    default int getCursor() {
        return 0;
    }
}
