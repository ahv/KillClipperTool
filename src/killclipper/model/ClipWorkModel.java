package killclipper.model;

import java.io.IOException;
import killclipper.ClipWork;
import killclipper.Killboard;
import killclipper.Settings;
import killclipper.Video;

public class ClipWorkModel {
    
    public static ClipWork clipWork;

    public static void generate(Killboard killboard, Video video) throws IOException {
        Settings s = SettingsModel.getSettings();
        clipWork = new ClipWork(killboard, video, s.getPreceedingSeconds(), s.getTrailingSeconds());
    }
}
