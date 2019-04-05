package killclipper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.SimpleDoubleProperty;
import killclipper.model.SettingsModel;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.progress.Progress;

public class ClipWork {

    private final ArrayList<Span> clipSpans;
    private final ArrayList<ClipJob> clipJobs;
    private final Video video;

    public ClipWork(Killboard killboard, Video video, int preceedingSeconds, int trailingSeconds) {
        this.video = video;
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
            clipJobs.add(new ClipJob("clip_" + i, (int) (span.getStartTimestamp() - video.getStartTimestamp()), (int) span.getDurationSeconds()));
            i++;
        }
    }

    public ArrayList<Span> getSpans() {
        return clipSpans;
    }

    public List<ClipJob> getClipJobs() {
        return clipJobs;
    }

    // TODO: Have x ClipJobs running at the same time; start the next one as one finishes,
    // once all are done call a allJobsDone listener, set a jobs done listener as start parameter perhaps
    private ConcurrentLinkedDeque<ClipJob> queuedClipJobs = new ConcurrentLinkedDeque<>();
    private ClipJobDoneLambda clipJobDoneFunction = (ClipJob doneJob) -> {
        queuedClipJobs.remove(doneJob);
        if (queuedClipJobs.size() > 0) {
            startNextClipJob();
        } else {
            // TODO: All jobs done
            System.out.println("ALL CLIP JOBS DONE!");
        }
    };

    // TODO: Listener for all jobs done as parameter
    public void startWork() {
        for (ClipJob cj : clipJobs) {
            queuedClipJobs.add(cj);
        }
        // TODO: Start multiple jobs simultaneously?
        startNextClipJob();
    }

    private void startNextClipJob() {
        queuedClipJobs.pop().start(clipJobDoneFunction);
    }

    private interface ClipJobDoneLambda {

        abstract void execute(ClipJob clipJob);
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

    public class ClipJob {

        private final String clipName;
        private final int clipDurationSeconds;
        private final FFmpegJob job;
        private final SimpleDoubleProperty progress;

        public ClipJob(String outputClipName, int videoStartOffsetSeconds, int clipDurationSeconds) {
            this.clipDurationSeconds = clipDurationSeconds;
            this.clipName = outputClipName;
            this.progress = new SimpleDoubleProperty(0.0);
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(video.path())
                    .overrideOutputFiles(true)
                    .setStartOffset(videoStartOffsetSeconds, TimeUnit.SECONDS)
                    .addOutput(SettingsModel.getSettings().getVideoOutputRootPath() + "\\" + outputClipName + ".mp4")
                    .setDuration(clipDurationSeconds, TimeUnit.SECONDS)
                    .setVideoCodec("copy")
                    .setAudioCodec("copy")
                    //.setStrict(FFmpegBuilder.Strict.EXPERIMENTAL)
                    .done();
            job = Clipper.instance.executor.createJob(builder, (Progress prgrs) -> {
                long elapsedSeconds = TimeUnit.SECONDS.convert(prgrs.out_time_ns, TimeUnit.NANOSECONDS);
                double percentage = ((double) elapsedSeconds / (double) clipDurationSeconds);
                //System.out.format("Elapsed: %s of %s (%s)", elapsedSeconds, clipDurationSeconds, percentage);
                setProgress(percentage);
            });
        }

        public double getProgress() {
            return progress.get();
        }

        public SimpleDoubleProperty getProgressProperty() {
            return progress;
        }

        public void setProgress(double percent) {
            progress.set(percent);
        }

        public String getClipName() {
            return clipName;
        }

        public int getClipDurationSeconds() {
            return clipDurationSeconds;
        }

        public void start(ClipJobDoneLambda expression) {
            new Thread(() -> {
                job.run();
                setProgress(1.0);
                expression.execute(this);
            }).start();
        }

    }
}
