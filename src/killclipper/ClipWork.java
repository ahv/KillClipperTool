package killclipper;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleDoubleProperty;
import killclipper.model.MediaModel;
import killclipper.model.SettingsModel;
import net.bramp.ffmpeg.job.FFmpegJob;
import net.bramp.ffmpeg.progress.Progress;

// TODO: This class has gotten a little bloated, break it up
public class ClipWork {

    private final Video video;
    private final ArrayList<Span> clipSpans;
    private final ArrayList<ClipJob> clipJobs;
    private final ConcurrentLinkedDeque<ClipJob> queuedClipJobs = new ConcurrentLinkedDeque<>();
    private final ClipJobDoneLambda clipJobDoneFunction = (ClipJob doneJob) -> {
        queuedClipJobs.remove(doneJob);
        if (queuedClipJobs.size() > 0) {
            startNextClipJob();
        } else {
            allClipJobsDoneEvent();
        }
    };
    private interface ClipJobDoneLambda {
        abstract void execute(ClipJob clipJob);
    }

    private void allClipJobsDoneEvent() {
        System.out.println("ALL CLIP JOBS DONE!");
    }

    public ClipWork(Killboard killboard, Video video, int preceedingSeconds, int trailingSeconds) throws IOException {
        this.video = video;
        clipSpans = createClipSpans(killboard, preceedingSeconds, trailingSeconds);
        clipJobs = new ArrayList<>();
        clipJobs.addAll(createClipJobs(clipSpans));
        if (SettingsModel.getSettings().isCreateCombinedVideo()) {
            CombineClipJob combineJob = createCombineJob();
            clipJobs.add(combineJob);
        }
    }

    private ArrayList<Span> createClipSpans(Killboard killboard, int preceedingSeconds, int trailingSeconds) {
        ArrayList<Span> spans = new ArrayList<>();
        int maximumSecondsBetweenKills = preceedingSeconds + trailingSeconds;
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
            spans.add(new Span(clipStart - preceedingSeconds, pe.timestamp + trailingSeconds));
        }
        return spans;
    }

    public ArrayList<Span> getSpans() {
        return clipSpans;
    }

    public List<ClipJob> getClipJobs() {
        return (List<ClipJob>) clipJobs;
    }

    public void startWork() {
        for (ClipJob cj : clipJobs) {
            queuedClipJobs.add(cj);
        }
        startNextClipJob();
    }

    private void startNextClipJob() {
        queuedClipJobs.pop().start(clipJobDoneFunction);
    }

    private ArrayList<SingleClipJob> createClipJobs(ArrayList<Span> spans) {
        ArrayList<SingleClipJob> jobs = new ArrayList<>();
        int i = 0;
        for (Span span : spans) {
            jobs.add(new SingleClipJob("clip_" + i, span));
            i++;
        }
        return jobs;
    }
    
    private CombineClipJob createCombineJob() throws IOException {
        return new CombineClipJob("combined");
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

        public void start(ClipJobDoneLambda expression) {
            new Thread(() -> {
                job.run();
                expression.execute(this);
            }).start();
        }
    }

    public class SingleClipJob extends ClipJob {

        private final int clipDurationSeconds;

        public SingleClipJob(String clipName, Span span) {
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
            //Files.deleteIfExists(Paths.get("", "cliplist.txt"));
        }

        @Override
        public int getClipDurationSeconds() {
            return this.clipDurationSeconds;
        }
    }
}
