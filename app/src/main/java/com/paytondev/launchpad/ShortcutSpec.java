package com.paytondev.launchpad;

final class ShortcutSpec {
    final String title;
    final String subtitle;
    final String component;
    final String action;
    final boolean launcher;

    private ShortcutSpec(String title, String subtitle, String component, String action, boolean launcher) {
        this.title = title; this.subtitle = subtitle; this.component = component;
        this.action = action; this.launcher = launcher;
    }

    static final ShortcutSpec PROJECTION = component("Phone Projection", "Connect your phone",
        "com.daudio.app.phoneprojection/.settings.ui.DeviceConnectionSettings");
    static final ShortcutSpec NEMESIS = new ShortcutSpec("NEMESIS", "Open NEMESIS Manager",
        "com.nemesis.manager/.MainActivity", "android.intent.action.MAIN", false);
    static final ShortcutSpec WIFI = new ShortcutSpec("Wi-Fi Settings", "Manage wireless connections",
        null, "android.settings.WIFI_SETTINGS", false);

    private static ShortcutSpec component(String title, String subtitle, String component) {
        return new ShortcutSpec(title, subtitle, component, "android.intent.action.MAIN", true);
    }

    static boolean mayLaunch(boolean exists, boolean exported, boolean enabled,
                             boolean appEnabled, boolean permitted) {
        return exists && exported && enabled && appEnabled && permitted;
    }
}
