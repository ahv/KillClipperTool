package killclipper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;
import killclipper.model.SettingsModel;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

public class Clipper {

    public static Clipper instance = new Clipper();
    private final String FFMPEG_PATH = Paths.get("").toAbsolutePath().toString() + "\\ffmpeg";
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

    public static void Combine(String pathToCliplist) {
        System.out.println("Doing combined clip job");
        //ffmpeg -f concat -safe 0 -i clips.txt -c copy combined.mp4
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(pathToCliplist +"\\cliplist.txt")
                .overrideOutputFiles(true)
                .addExtraArgs("-safe", "0")
                .setFormat("concat")
                .addOutput(SettingsModel.getSettings().getVideoOutputRootPath() + "\\combined.mp4")
                .setVideoCodec("copy")
                .setAudioCodec("copy")
                .setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                .done();
        Clipper.instance.executor.createJob(builder).run();
        System.out.println("Combined clip done");
    }

    public FFmpegProbeResult probe(String path) throws IOException {
        return ffprobe.probe(path);
    }
}
