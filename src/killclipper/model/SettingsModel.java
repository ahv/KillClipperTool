package killclipper.model;

import killclipper.Settings;

public class SettingsModel {

    private static Settings settings;

    public static Settings getSettings() {
        if (settings == null) {
            settings = Settings.loadOrCreate();
        }
        return settings;
    }
}
