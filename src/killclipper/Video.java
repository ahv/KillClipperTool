package killclipper;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.bramp.ffmpeg.probe.FFmpegFormat;
import org.apache.commons.io.FilenameUtils;

public class Video {

    private File file;
    private FFmpegFormat format;
    private long startTimestamp;
    private long endTimestamp;

    public Video(File file) {
        this.file = file;
        if (!file.exists()) {
            System.out.println("Could not find file: " + file.getAbsolutePath());
        }
        try {
            format = Clipper.probe(file.getAbsolutePath()).getFormat();
            // TODO: Accept other filename formats
            DateTimeFormatter fileNameDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH-mm-ss Z");
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(FilenameUtils.removeExtension(file.getName()), fileNameDateFormat);
            ZonedDateTime videoStartTime = offsetDateTime.atZoneSameInstant(ZoneOffset.UTC);
            startTimestamp = Instant.from(videoStartTime).getEpochSecond();
            endTimestamp = startTimestamp + (long) Math.ceil(format.duration);
        } catch (IOException ex) {
            Logger.getLogger(Video.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String path() {
        return file.getPath();
    }

    public long getStartTimestamp() {
        return startTimestamp;
    }

    public long getEndTimeStamp() {
        return endTimestamp;
    }
    
    public long getDuration() {
        return endTimestamp - startTimestamp;
    }

    public void correctTimeBy(long seconds) {
        System.out.println(String.format("Correcting videotime by %s seconds.", seconds));
        startTimestamp += seconds;
        endTimestamp += seconds;
    }
    
    @Override
    public String toString(){
        
        return String.format("Path: %s\nStart Timestamp: %s\nEnd Timestamp: %s\nDuration: %s\nBitrate: %s\nTags: %s",
                path(), getStartTimestamp(), getEndTimeStamp(), getDuration(),
                format.bit_rate, format.tags);
    }
}
