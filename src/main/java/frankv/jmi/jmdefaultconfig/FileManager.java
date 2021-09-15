package frankv.jmi.jmdefaultconfig;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import frankv.jmi.JMI;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File versionFile = new File(System.getProperty("user.dir") + "/journeymap/defaultconfig.json");

    public static Version readConfigVersion() throws IOException {
        Version version;
        try (FileReader fileReader = new FileReader(versionFile)) {
            version = GSON.fromJson(fileReader, Version.class);
            if (version == null) {
                throw new NullPointerException();
            }

        } catch (JsonParseException | NullPointerException | IOException e) {
            version = new Version(-1);
            versionFile.getParentFile().mkdirs();
            try (FileWriter fileWriter = new FileWriter(versionFile)) {
                GSON.toJson(version, fileWriter);
            }
        }
        return version;
    }

    private static void updateVersionFile(int newVersion) throws IOException {
        Version v;
        v = new Version(newVersion);
        FileWriter fileWriter = new FileWriter(versionFile);
        GSON.toJson(v, fileWriter);
    }

    public static void writeJMDefaultConfig(int newVersion) {
        File source = new File(System.getProperty("user.dir") + "/config/jmdefaultconfig");
        File dest = new File(System.getProperty("user.dir") + "/journeymap/");

        if (!source.exists() || !source.isDirectory()) {
            JMI.LOGGER.error("No default config found.");
            return;
        }

        JMI.LOGGER.info("Writing default configs for Journeymap...");
        try {
            updateVersionFile(newVersion);
            FileUtils.copyDirectory(source, dest);
            JMI.LOGGER.info("Journeymap configs updated.");
        } catch (IOException e) {
            JMI.LOGGER.error("Failed to write default configs " + e);
        }
    }
}