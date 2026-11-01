package com.paytondev.launchpad;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;

/** Reads the head unit's day/night display state. No vehicle Binder calls. */
final class DisplaySignals {
    // Ioniq 5 NEEV23 US, 2026-10-29: the Hyundai Day/Night display mode sets Android night mode
    // (UiModeManager.getNightMode() 1 in Day, 2 in Night) and the configuration follows it.
    static boolean night(Context context, Configuration configuration) {
        int night = configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        if (night != Configuration.UI_MODE_NIGHT_UNDEFINED) return night == Configuration.UI_MODE_NIGHT_YES;
        UiModeManager manager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        return manager != null && manager.getNightMode() == UiModeManager.MODE_NIGHT_YES;
    }

    private DisplaySignals() { }
}
