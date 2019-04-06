package killclipper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.ProgressListener;

public class Clipper {

    private final static String STATIC_FFMPEG_PATH = Paths.get("").toAbsolutePath().toString() + "\\ffmpeg";
    private static FFmpeg ffmpeg;
    private static FFprobe probe;
    private static FFmpegExecutor executor;

    public static void initialize() throws IOException {
        ffmpeg = new FFmpeg(STATIC_FFMPEG_PATH + "\\ffmpeg.exe");
        probe = new FFprobe(STATIC_FFMPEG_PATH + "\\ffprobe.exe");
        executor = new FFmpegExecutor(ffmpeg, probe);

    }

    public static FFmpegJob createClipJob(String videoFilePath, String outputFilePath, int startOffsetSeconds, int durationSeconds, ProgressListener progressListener) {
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(videoFilePath)
                .overrideOutputFiles(true)
                .setStartOffset(startOffsetSeconds, TimeUnit.SECONDS)
                .addOutput(outputFilePath)
                .setDuration(durationSeconds, TimeUnit.SECONDS)
                .setVideoCodec("copy")
                .setAudioCodec("copy")
                .done();
        return Clipper.executor.createJob(builder, progressListener);
    }

    public static FFmpegJob createCombineJob(String clipListFilePath, String outputFilePath, ProgressListener progressListener) {
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(clipListFilePath)
                .overrideOutputFiles(true)
                .addExtraArgs("-safe", "0")
                .setFormat("concat")
                .addOutput(outputFilePath)
                .setVideoCodec("copy")
                .setAudioCodec("copy")
                .done();
        return Clipper.executor.createJob(builder, progressListener);
    }

    public static FFmpegProbeResult probe(String path) throws IOException {
        return probe.probe(path);
    }
}
