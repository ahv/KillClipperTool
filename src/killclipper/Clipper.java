package killclipper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.probe.FFmpegProbeResult;

public class Clipper {

    public static Clipper instance = new Clipper();
    private final String FFMPEG_PATH = "D:\\Software\\ffmpeg-20190122-d92f06e-win64-static\\bin";
    private FFmpeg ffmpeg;
    private FFprobe ffprobe;
    private FFmpegExecutor executor;

    private ConcurrentLinkedDeque<ClipJob> clipJobQueue;
    private List<ClipJob> runningClipJobs;

    private Clipper() {
        try {
            ffmpeg = new FFmpeg(FFMPEG_PATH + "\\ffmpeg.exe");
            ffprobe = new FFprobe(FFMPEG_PATH + "\\ffprobe.exe");
            executor = new FFmpegExecutor(ffmpeg, ffprobe);
            clipJobQueue = new ConcurrentLinkedDeque<>();
            runningClipJobs = Collections.synchronizedList(new ArrayList());
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public FFmpegProbeResult probe(String path) throws IOException {
        return ffprobe.probe(path);
    }

    // TODO: will overwrite jobs if used twice before doing a startClipWork, reworkkerino
    public void queueClibJobs(Video video, ClipWork clipWork) {
        int i = 0;
        for (ClipWork.Span span : clipWork.getSpans()) {
            queueClibJob(video, "clip_" + i, (int) (span.getStartTimestamp() - video.getStartTimestamp()), (int) span.getDurationSeconds());
            i++;
        }
    }

    private void queueClibJob(Video video, String fileName, int clipStart, int clipDuration) {
        clipJobQueue.add(new ClipJob(video.path(), fileName, clipStart, clipDuration));
    }

    public void startClipWork() {
        while (!clipJobQueue.isEmpty()) {
            ClipJob cj = clipJobQueue.pop();
            runningClipJobs.add(cj);
            executor.createJob(cj.builder).run();
        }
    }

    public class ClipJob {

        private final FFmpegBuilder builder;

        public ClipJob(String videoPath, String outputClipName, int videoStartOffsetSeconds, int clipDurationSeconds) {
            builder = new FFmpegBuilder()
                    .setInput(videoPath)
                    .overrideOutputFiles(true)
                    .setStartOffset(videoStartOffsetSeconds, TimeUnit.SECONDS)
                    .addOutput(outputClipName + ".mp4")
                    .setDuration(clipDurationSeconds, TimeUnit.SECONDS)
                    .setVideoCodec("copy")
                    .setAudioCodec("copy")
                    //.setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    .done();
        }

        public FFmpegBuilder getBuilder() {
            return builder;
        }
    }
}
