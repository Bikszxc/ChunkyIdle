package dev.bikzxc.chunkyidle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Common mod identity and logging shared by every loader. */
public final class ChunkyIdle {
    // Keep this in sync with `modId` in gradle.properties.
    public static final String MOD_ID = "chunkyidle";

    public static final Logger LOGGER = LoggerFactory.getLogger("ChunkyIdle");
    private static final Properties BUILD_METADATA = loadBuildMetadata();
    public static final String VERSION = BUILD_METADATA.getProperty("version", "unknown");
    public static final String AUTHORS = BUILD_METADATA.getProperty("authors", "Bikzxc");

    private ChunkyIdle() {
    }

    /** Called once by every loader's entry point, after that loader's own setup. */
    public static void init() {
        ChunkyIdleLog.info("ChunkyIdle is ready.");
    }

    private static Properties loadBuildMetadata() {
        Properties properties = new Properties();
        try (InputStream input = ChunkyIdle.class.getResourceAsStream("/chunkyidle-build.properties")) {
            if (input != null) properties.load(input);
        } catch (IOException exception) {
            LOGGER.error("[ChunkyIdle] Build information could not be read.", exception);
        }
        return properties;
    }
}
