package killclipper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.SimpleDoubleProperty;
import killclipper.ClipWork.ClipJob;
import killclipper.model.MediaModel;
import killclipper.model.SettingsModel;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.progress.Progress;

// TODO: This class has gotten a little bloated, break it up
public class ClipWork {

    private final Video video;
    private final ArrayList<TimeSpan> clipSpans;
    private final ArrayList<ClipJob> clipJobs;

    public ClipWork(Video video, ArrayList<TimeSpan> timeSpans) throws IOException {
        this.video = video;
        clipSpans = timeSpans;
        clipJobs = new ArrayList<>();
        clipJobs.addAll(createClipJobs(clipSpans));
        if (SettingsModel.getSettings().isCreateCombinedVideo()) {
            CombineClipJob combineJob = createCombineJob();
            clipJobs.add(combineJob);
        }

    }

    private ArrayList<SingleClipJob> createClipJobs(ArrayList<TimeSpan> spans) {
        ArrayList<SingleClipJob> jobs = new ArrayList<>();
        int i = 0;
        for (TimeSpan span : spans) {
            jobs.add(new SingleClipJob("clip_" + i, span));
            i++;
        }
        return jobs;
    }

    private CombineClipJob createCombineJob() throws IOException {
        return new CombineClipJob("combined");
    }

    public ArrayList<TimeSpan> getSpans() {
        return clipSpans;
    }

    public ArrayList<ClipJob> getClipJobs() {
        return clipJobs;
    }

    public interface ClipJobDoneLambda {

        abstract void execute(ClipWork.ClipJob clipJob);
    }

    public abstract class ClipJob {

        String clipName;
        public SimpleDoubleProperty progress;
        FFmpegJob job;

        private ClipJob(String clipName) {
            this.clipName = clipName;
            this.progress = new SimpleDoubleProperty(0);
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

        public abstract int getClipDurationSeconds();

        public void start(ClipJobDoneLambda expression) throws IOException {
            new Thread(() -> {
                job.run();
                expression.execute(this);
            }).start();
        }
    }

    public class SingleClipJob extends ClipJob {

        private final int clipDurationSeconds;

        public SingleClipJob(String clipName, TimeSpan span) {
            super(clipName);
            int videoStartOffsetSeconds = (int) (span.getStartTimestamp() - MediaModel.getVideo().getStartTimestamp());
            this.clipDurationSeconds = (int) span.getDurationSeconds();
            // TODO: Support for other file formats
            String outputFilePath = SettingsModel.getSettings().getVideoOutputRootPath() + "\\" + clipName + ".mp4";
            this.job = Clipper.createClipJob(MediaModel.getVideo().path(), outputFilePath, videoStartOffsetSeconds, clipDurationSeconds, (Progress prgrs) -> {
                if (prgrs.isEnd()) {
                    setProgress(1);
                } else {
                    long elapsedSeconds = TimeUnit.SECONDS.convert(prgrs.out_time_ns, TimeUnit.NANOSECONDS);
                    double percentage = ((double) elapsedSeconds / (double) clipDurationSeconds);
                    setProgress(percentage);
                }
            });
        }

        @Override
        public int getClipDurationSeconds() {
            return clipDurationSeconds;
        }
    }

    private class CombineClipJob extends ClipJob {

        private final int clipDurationSeconds;

        // WARNING: Can cause some strange output if not used correctly
        // AKA first create normal span-based clips and then create the combined clip once after that
        public CombineClipJob(String clipName) throws IOException {
            super(clipName);
            FileWriter fileWriter = new FileWriter("cliplist.txt");
            PrintWriter printWriter = new PrintWriter(fileWriter);
            String clipOutputPath = SettingsModel.getSettings().getVideoOutputRootPath();
            // TODO: Support for other fileformats
            String fileFormat = "mp4";
            int ts = 0;
            for (ClipJob cj : clipJobs) {
                printWriter.printf("file '%s\\%s.%s'\n", clipOutputPath, cj.clipName, fileFormat);
                ts += ((SingleClipJob) cj).getClipDurationSeconds();
            }
            clipDurationSeconds = ts;
            printWriter.close();
            fileWriter.close();
            this.job = Clipper.createCombineJob(Main.workingDirectory + "\\cliplist.txt", clipOutputPath + "\\combined." + fileFormat, (prgrs) -> {
                if (prgrs.isEnd()) {
                    setProgress(1);
                } else {
                    long elapsedSeconds = TimeUnit.SECONDS.convert(prgrs.out_time_ns, TimeUnit.NANOSECONDS);
                    double percentage = ((double) elapsedSeconds / (double) clipDurationSeconds);
                    setProgress(percentage);
                }
            });
        }

        @Override
        public int getClipDurationSeconds() {
            return this.clipDurationSeconds;
        }
    }
}
