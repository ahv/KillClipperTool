package killclipper;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.progress.Progress;
import net.bramp.ffmpeg.progress.ProgressListener;

public class ClipWork {

    private final ArrayList<Span> clipSpans;
    private final ArrayList<ClipJob> clipJobs;

    public ClipWork(Killboard killboard, Video video, int preceedingSeconds, int trailingSeconds) {
        int maximumSecondsBetweenKills = preceedingSeconds + trailingSeconds;
        clipSpans = new ArrayList<>();
        int index = 0;
        while (index < killboard.size()) {
            long clipStart = killboard.entries.get(index).timestamp;

            Killboard.Entry pe = killboard.entries.get(index);
            index++;
            while (index < killboard.size()) {
                pe = killboard.entries.get(index - 1);
                Killboard.Entry ne = killboard.entries.get(index);
                if (ne.timestamp - pe.timestamp > maximumSecondsBetweenKills) {
                    break;
                }
                index++;
            }
            clipSpans.add(new Span(clipStart - preceedingSeconds, pe.timestamp + trailingSeconds));
        }
        
        clipJobs = new ArrayList<>();
        int i = 0;
        for (Span span : clipSpans) {
            clipJobs.add(new ClipJob (video.path(), "clip_" + i, (int) (span.getStartTimestamp() - video.getStartTimestamp()), (int) span.getDurationSeconds()));
            i++;
        }
    }

    public ArrayList<Span> getSpans() {
        return clipSpans;
    }

    public void start() {
        for (ClipJob cj : clipJobs) {
            cj.start();
        }
    }

    public class Span {

        private final long startTimeStamp;
        private final long endTimeStamp;

        public Span(long startTimeStamp, long endTimeStamp) {
            this.startTimeStamp = startTimeStamp;
            this.endTimeStamp = endTimeStamp;
        }

        public long getStartTimestamp() {
            return startTimeStamp;
        }

        public long getEndTimestamp() {
            return endTimeStamp;
        }
        
        public long getDurationSeconds() {
            return endTimeStamp - startTimeStamp;
        }

        @Override
        public String toString() {
            return String.format("Span: %s -- %s (%s)", startTimeStamp, endTimeStamp, getDurationSeconds());
        }
    }
    
       // TODO: Probably should be in the ClipWork object
    public class ClipJob implements ProgressListener {

        private final FFmpegBuilder builder;
        private final FFmpegJob job;
        private final String clipName;
        private final int clipDurationSeconds;
        

        public ClipJob(String videoPath, String outputClipName, int videoStartOffsetSeconds, int clipDurationSeconds) {
            this.clipDurationSeconds = clipDurationSeconds;
            this.clipName = outputClipName;
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
            job = Clipper.instance.executor.createJob(builder, this);
        }

        private FFmpegBuilder getBuilder() {
            return builder;
        }

        public FFmpegJob getJob() {
            return job;
        }
        
        public void start() {
            job.run();
        }

        @Override
        public void progress(Progress progress) {
            double percentage = (progress.out_time_ns*100) / clipDurationSeconds;
            System.out.println("Clip " + clipName + " progress: " + percentage);
        }
    }
}
