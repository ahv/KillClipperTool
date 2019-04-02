package killclipper.model;

import killclipper.ClipWork;
import killclipper.Killboard;

public class ClipWorkModel {
    
    // TODO: These two could go to the settings file / Settings class
    public static int preceedingSeconds = 5;
    public static int trailingSeconds = 5;
    //
    
    public static ClipWork clipWork;

    public static void generate(Killboard killboard) {
        clipWork = new ClipWork(killboard, preceedingSeconds, trailingSeconds);
    }
}
