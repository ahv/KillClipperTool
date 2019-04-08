package killclipper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;
import net.bramp.ffmpeg.progress.ProgressListener;

public class Clipper {

    private final static String FFMPEG_PATH = Paths.get("").toAbsolutePath().toString() + "\\ffmpeg";
    private static FFmpeg FFMPEG;
    private static FFprobe PROBE;
    private static FFmpegExecutor EXECUTOR;
    private final static ConcurrentLinkedDeque<ClipWork.ClipJob> QUEUED_CLIP_JOBS = new ConcurrentLinkedDeque<>();
    private static Runnable onDoneCallback;

    public static void initialize() throws IOException {
        FFMPEG = new FFmpeg(FFMPEG_PATH + "\\ffmpeg.exe");
        PROBE = new FFprobe(FFMPEG_PATH + "\\ffprobe.exe");
        EXECUTOR = new FFmpegExecutor(FFMPEG, PROBE);

    }

    static FFmpegJob createClipJob(String videoFilePath, String outputFilePath, int startOffsetSeconds, int durationSeconds, ProgressListener progressListener) {
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(videoFilePath)
                .overrideOutputFiles(true)
                .setStartOffset(startOffsetSeconds, TimeUnit.SECONDS)
                .addOutput(outputFilePath)
                .setDuration(durationSeconds, TimeUnit.SECONDS)
                .setVideoCodec("copy")
                .setAudioCodec("copy")
                .done();
        return Clipper.EXECUTOR.createJob(builder, progressListener);
    }

    static FFmpegJob createCombineJob(String clipListFilePath, String outputFilePath, ProgressListener progressListener) {
        FFmpegBuilder builder = new FFmpegBuilder()
                .setInput(clipListFilePath)
                .overrideOutputFiles(true)
                .addExtraArgs("-safe", "0")
                .setFormat("concat")
                .addOutput(outputFilePath)
                .setVideoCodec("copy")
                .setAudioCodec("copy")
                .done();
        return Clipper.EXECUTOR.createJob(builder, progressListener);
    }

    public static FFmpegProbeResult probe(String path) throws IOException {
        return PROBE.probe(path);
    }

    // TODO: Shouldn't accept more jobs while already working,
    // introduce an Exception to throw?
    public static void startWork(ClipWork clipWork, Runnable onDone) throws IOException {
        Clipper.onDoneCallback = onDone;
        for (ClipWork.ClipJob cj : clipWork.getClipJobs()) {
            QUEUED_CLIP_JOBS.add(cj);
        }
        startNextClipJob();
    }

    private static void startNextClipJob() throws IOException {
        QUEUED_CLIP_JOBS.pop().start((ClipWork.ClipJob doneJob) -> {
            try {
            QUEUED_CLIP_JOBS.remove(doneJob);
                if (QUEUED_CLIP_JOBS.size() > 0) {
                    startNextClipJob();
                } else {
                    onAllClipJobsDone();
                }
            } catch (IOException ex) {
                Logger.getLogger(Clipper.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    private static void onAllClipJobsDone() throws IOException {
        Files.deleteIfExists(Paths.get("", "cliplist.txt"));
        onDoneCallback.run();
    }
}
