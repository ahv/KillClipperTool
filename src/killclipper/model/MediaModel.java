package killclipper.model;

import javafx.scene.media.Media;
import killclipper.Video;

public class MediaModel {
    private static Media media;
    private static Video video;

    public static Media getMedia() {
        return media;
    }

    public static void setMedia(Media media) {
        MediaModel.media = media;
    }

    public static Video getVideo() {
        return video;
    }

    public static void setVideo(Video video) {
        MediaModel.video = video;
    }
    
}
