package killclipper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

public class Clipper {

    public static Clipper instance = new Clipper();
    //private final String FFMPEG_PATH = "F:\\Netbeans Projects\\KillClipperTool\\ffmpeg";
    private final String FFMPEG_PATH = Paths.get("").toAbsolutePath().toString();
    private FFmpeg ffmpeg;
    private FFprobe ffprobe;
    public FFmpegExecutor executor;

    private Clipper() {
        try {
            ffmpeg = new FFmpeg(FFMPEG_PATH + "\\ffmpeg.exe");
            ffprobe = new FFprobe(FFMPEG_PATH + "\\ffprobe.exe");
            executor = new FFmpegExecutor(ffmpeg, ffprobe);
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FFmpegProbeResult probe(String path) throws IOException {
        return ffprobe.probe(path);
    }
}
