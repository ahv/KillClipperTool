package killclipper.model;

import java.io.IOException;
import java.util.ArrayList;
import killclipper.ClipWork;
import killclipper.Killboard;
import killclipper.Settings;
import killclipper.TimeSpan;
import killclipper.Video;

public class ClipWorkModel {
    
    public static ClipWork clipWork;

    public static void generate(Killboard killboard, Video video) throws IOException {
        Settings s = SettingsModel.getSettings();
        ArrayList<TimeSpan> spans = TimeSpan.createSpansFor(killboard, s.getPreceedingSeconds(), s.getTrailingSeconds());
        clipWork = new ClipWork(video, spans);
    }
}
