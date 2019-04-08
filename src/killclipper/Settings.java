package killclipper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.CheckBox;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement
public class Settings {

    private String videoSourceRootPath;
    private String videoOutputRootPath;

    private int preceedingSeconds;
    private int trailingSeconds;
    private boolean createCombinedVideo;

    @XmlElementWrapper
    @XmlElement(name = "character")
    private final ArrayList<PlayerCharacter> characters;

    private Settings() {
        videoSourceRootPath = Paths.get("").toAbsolutePath().toString();
        videoOutputRootPath = Paths.get("").toAbsolutePath().toString();
        characters = new ArrayList<>();
        createCombinedVideo = false;
        preceedingSeconds = 5;
        trailingSeconds = 5;
    }

    public void save() {
        Settings.save(this);
    }

    public String getVideoOutputRootPath() {
        return videoOutputRootPath;
    }

    @XmlElement
    public void setVideoOutputRootPath(String videoOutputRootPath) {
        this.videoOutputRootPath = videoOutputRootPath;
    }

    public String getVideoSourceRootPath() {
        return videoSourceRootPath;
    }

    public ArrayList<PlayerCharacter> getCharacters() {
        return characters;
    }

    @XmlElement
    public void setVideoSourceRootPath(String videoSourcePath) {
        this.videoSourceRootPath = videoSourcePath;
    }

    public void setCreateCombinedVideo(boolean option) {
        this.createCombinedVideo = option;
    }

    public void setPreceedingSeconds(int seconds) {
        this.preceedingSeconds = seconds;
    }

    public boolean isCreateCombinedVideo() {
        return createCombinedVideo;
    }

    public void setTrailingSeconds(int seconds) {
        this.trailingSeconds = seconds;
    }

    public int getPreceedingSeconds() {
        return preceedingSeconds;
    }

    public int getTrailingSeconds() {
        return trailingSeconds;
    }

    @XmlRootElement
    public static class PlayerCharacter {

        private SimpleStringProperty name;
        private SimpleStringProperty id;
        private SimpleBooleanProperty enabled;
        @XmlTransient
        private CheckBox enabledCheckBox;

        public PlayerCharacter(String name, String id) {
            this.name = new SimpleStringProperty(name);
            this.id = new SimpleStringProperty(id);
            this.enabled = new SimpleBooleanProperty(true);
            this.enabledCheckBox = new CheckBox();
            this.enabledCheckBox.setSelected(true);
            Bindings.bindBidirectional(enabled, enabledCheckBox.selectedProperty());
        }

        public PlayerCharacter() {
            this.name = new SimpleStringProperty("undefined");
            this.id = new SimpleStringProperty("0");
            this.enabled = new SimpleBooleanProperty(false);
            this.enabledCheckBox = new CheckBox();
            this.enabledCheckBox.setSelected(false);
            Bindings.bindBidirectional(enabled, enabledCheckBox.selectedProperty());
        }

        public String getName() {
            return name.get();
        }

        public void setName(String name) {
            this.name.set(name);
        }

        public String getId() {
            return id.get();
        }

        public void setId(String id) {
            this.id.set(id);
        }

        public boolean isEnabled() {
            return enabled.get();
        }

        public void setEnabled(boolean enabled) {
            this.enabled.set(enabled);
        }
        
        public CheckBox getEnabledCheckBox() {
            return enabledCheckBox;
        }

        @Override
        public String toString() {
            return String.format("PlayerCharacter name: %s, id: %s, enabled: %s", getName(), getId(), isEnabled());

        }
    }

    // -------------------------------------------------------------------------
    //<editor-fold defaultstate="collapsed" desc="Static">
    public static final Path PATH = Paths.get("", "settings.xml").toAbsolutePath();
    private static final JAXBContext JAXB_CONTEXT = initJAXBContext();
    private static final File SETTINGS_FILE = new File(PATH.toString());

    private static JAXBContext initJAXBContext() {
        try {
            return JAXBContext.newInstance(Settings.class);
        } catch (JAXBException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static Settings loadOrCreate() {
        if (SETTINGS_FILE.exists()) {
            System.out.println("Loading settings file.");
            try {
                Unmarshaller jaxbUnmarshaller = JAXB_CONTEXT.createUnmarshaller();
                return (Settings) jaxbUnmarshaller.unmarshal(SETTINGS_FILE);
            } catch (JAXBException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        System.out.println("Settings file not found (" + PATH + "), creating a new file.");
        Settings settings = new Settings();
        save(settings);
        return settings;
    }

    private static void save(Settings settings) {
        try (BufferedWriter writer = Files.newBufferedWriter(PATH)) {
            writer.write(settingsAsXML(settings));
        } catch (IOException ex) {
            System.out.println("Couldn't create a new settings file!");
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static String settingsAsXML(Settings settings) {
        try {
            Marshaller jaxbMarshaller = JAXB_CONTEXT.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter sw = new StringWriter();
            jaxbMarshaller.marshal(settings, sw);
            String xmlContent = sw.toString();
            return xmlContent;
        } catch (JAXBException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
//</editor-fold>
}
