package killclipper.controller;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;
import javafx.scene.shape.Line;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;
import killclipper.ApiCaller;
import killclipper.Killboard;
import killclipper.Killboard.Entry;
import killclipper.Main;
import killclipper.Settings;
import killclipper.Video;
import killclipper.model.ClipWorkModel;
import killclipper.model.MediaModel;
import killclipper.model.SettingsModel;

public class SyncController implements Initializable  {

    @FXML private StackPane mediaPane;
    @FXML private MediaView mediaView;
    private MediaPlayer mediaPlayer;
    
    
    @FXML private Pane killTimelinePane;
    private Killboard killboard;
    
    // TODO: This bool is a little awkward, any way to get rid of it?
    private boolean clickedOnScrubber = false;
    @FXML private Slider scrubber;
    @FXML private Button playPauseButton;
    @FXML private Button previousKillButton;
    @FXML private Button nextKillButton;
    @FXML private Button seekBackButton;
    @FXML private Button seekForwardButton;
    @FXML private Button stepBackButton;
    @FXML private Button stepForwardButton;
    
    @FXML private Button previousSyncTargetButton;
    @FXML private Button nextSyncTargetButton;
    @FXML private Button syncButton;
    @FXML private Label syncTargetLabel;
    private int syncTargetIndex;
    @FXML private Label syncTimeLabel;
    
    @FXML private Button settingsButton;
    @FXML private Button generateButton;
    
    @FXML private Button fullscreenButton;
    
    @FXML
    void handlePlayPauseAction(ActionEvent event) {
        if (mediaPlayer.getStatus().equals(Status.PAUSED) || mediaPlayer.getStatus().equals(Status.READY)) {
            mediaPlayer.play();
            playPauseButton.setText("||");
        } else {
            mediaPlayer.pause();
            playPauseButton.setText(">");
        }   
    }
    
    //<editor-fold defaultstate="collapsed" desc="ActionHandlers">
    @FXML
            void handleStepBackAction(ActionEvent event) {
                mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() - 0.25));
            }
            
            @FXML
            void handleStepForwardAction(ActionEvent event) {
                mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() + 0.25));
            }
            
            @FXML
            void handleSeekBackAction(ActionEvent event) {
                mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() - 2));
            }
            
            @FXML
            void handleSeekForwardAction(ActionEvent event) {
                mediaPlayer.seek(Duration.seconds(mediaPlayer.getCurrentTime().toSeconds() + 2));
            }
            
            @FXML
            void handlePreviousKillAction(ActionEvent event) {
                long videoStartTimestamp = MediaModel.getVideo().getStartTimestamp();
                long playerCurrentTime = (long) mediaPlayer.getCurrentTime().toSeconds();
                long previousKillTimestamp = killboard.lastTimestampBefore(videoStartTimestamp + playerCurrentTime);
                long seekTo = previousKillTimestamp - videoStartTimestamp;
                mediaPlayer.seek(Duration.seconds(seekTo));
            }
            
            // TODO: Scrubber won't move when using these before having hit the play button once
            @FXML
            void handleNextKillAction(ActionEvent event) {
                long videoStartTimestamp = MediaModel.getVideo().getStartTimestamp();
                long playerCurrentTime = (long) mediaPlayer.getCurrentTime().toSeconds();
                long nextKillTimestamp = killboard.nextTimestampAfter(videoStartTimestamp + playerCurrentTime);
                long seekTo = nextKillTimestamp - videoStartTimestamp;
                mediaPlayer.seek(Duration.seconds(seekTo));
            }
            
            @FXML
            void handlePreviousSyncTargetAction(ActionEvent event) {
                syncTargetIndex = (syncTargetIndex - 1) <= 0 ? 0 : syncTargetIndex -1;
                String syncTargetName = killboard.entries.get(syncTargetIndex).character.name.first;
                syncTargetLabel.setText("Target: " + syncTargetName);
            }
            
            @FXML
            void handleNextSyncTargetAction(ActionEvent event) {
                syncTargetIndex = (syncTargetIndex + 1) > killboard.entries.size() ? syncTargetIndex : syncTargetIndex + 1;
                String syncTargetName = killboard.entries.get(syncTargetIndex).character.name.first;
                syncTargetLabel.setText("Target: " + syncTargetName);
            }
            
            @FXML
            void handleSyncAction(ActionEvent event) {
                int videoTime = (int) mediaPlayer.getCurrentTime().toSeconds();
                long killOffset = killboard.entries.get(syncTargetIndex).timestamp - MediaModel.getVideo().getStartTimestamp();
                long correction = killOffset - videoTime;
                syncTimeLabel.setText("Sync: " + correction + " seconds");
                MediaModel.getVideo().correctTimeBy(correction);
                
                
                // TODO: Redraw killtimeline also on window resize
                redrawKillTimeline();
            }
            
            @FXML
            void handleShowSettingsAction(ActionEvent event) throws IOException {
                Main.popupView("ClipworkSettingsView");
            }
            
            @FXML
            void handleGenerateClipworkAction(ActionEvent event) throws IOException {
                mediaPlayer.pause();
                DirectoryChooser directoryChooser = new DirectoryChooser();
                File dir = directoryChooser.showDialog(Main.mainStage);
                if (dir == null) return;
                SettingsModel.getSettings().setVideoOutputRootPath(dir.getAbsolutePath());
                ClipWorkModel.generate(killboard, MediaModel.getVideo());
                Main.popupView("ClipWorkProgressView");
            }
            
            @FXML
            void handleFullscreenAction (ActionEvent event) {
                Main.mainStage.setFullScreen(!Main.mainStage.isFullScreen());
                redrawKillTimeline();
            }
//</editor-fold>
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Initializing SyncViewController.");
        Main.mainStage.setResizable(true);
        
        // TODO: Refactor into MainView, implement threading, error messages
        fetchKillboard();
        
        initializeMediaPlayer();
        
        // TODO: Listeners probably need to be removed on view switch
        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) ->
            redrawKillTimeline();
        Main.mainStage.widthProperty().addListener(stageSizeListener);
        Main.mainStage.heightProperty().addListener(stageSizeListener);
        
        // TODO: Refactor setting sync target
        syncTargetIndex = 0;
        String syncTargetName = killboard.entries.get(syncTargetIndex).character.name.first;
        syncTargetLabel.setText("Target: " + syncTargetName);
        //
    }

    private void initializeMediaPlayer() {
        mediaPlayer = new MediaPlayer(MediaModel.getMedia());
        mediaView.setMediaPlayer(mediaPlayer);
        mediaView.fitWidthProperty().bind(mediaPane.widthProperty());
        mediaView.fitHeightProperty().bind(mediaPane.heightProperty());
        
        scrubber.setMax(MediaModel.getVideo().getDuration());
        mediaPlayer.currentTimeProperty().addListener((observable, oldValue, newValue) -> {
            if (!clickedOnScrubber) scrubber.setValue(newValue.toSeconds());
        });
        scrubber.setOnMousePressed((event) -> {
            clickedOnScrubber = true; 
        });
        scrubber.setOnMouseReleased((event) -> {
            mediaPlayer.seek(Duration.seconds(scrubber.getValue()));
            clickedOnScrubber = false;
        });
    }
    
    private void fetchKillboard() {
        Video video = MediaModel.getVideo();
        long startTimestamp = video.getStartTimestamp();
        long endTimestamp = video.getEndTimeStamp();
        killboard = new Killboard();
        for (Settings.PlayerCharacter pc : SettingsModel.getSettings().getCharacters()) {
            killboard.append(ApiCaller.getKillboard(pc.getId(), startTimestamp, endTimestamp));
        }
    }
    
    // TODO: Rework: only reposition them on the x axis on resize instead of KILLING ALL CHILDREN D:
    // referece is kept in killTimelinePane.getChildren()!
    private void redrawKillTimeline() {
        killTimelinePane.getChildren().clear();
        long paneWidth = (long) killTimelinePane.getWidth();
        float span = MediaModel.getVideo().getDuration();
        for (Entry e : killboard.entries) {
            float pos = e.timestamp - MediaModel.getVideo().getStartTimestamp();
            float spot = (long) ((pos/span) * paneWidth);
            
            // TODO: Add more detail; orange lines for teamkills, solid lines for headshot kills, dotted lines for other kills etc.
            Line line = new Line(spot, 10, spot, 23);
            line.setStyle(e.table_type.equals("kills") ? "-fx-stroke: green" : "-fx-stroke: red");
            
            killTimelinePane.getChildren().add(line);
        }
    }
}
