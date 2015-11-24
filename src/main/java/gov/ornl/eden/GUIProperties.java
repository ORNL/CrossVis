package gov.ornl.eden;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class GUIProperties extends Properties {
	private static final long serialVersionUID = 1L;
	private final static Logger logger = Logger.getLogger(GUIProperties.class);

	public static final String LAST_SERVER_ADDRESS = "LAST_SERVER_ADDRESS";
	public static final String LAST_SERVER_PORT = "LAST_SERVER_PORT";

	// The property file name
	protected static final String FILENAME = "eden.properties";

	// Home directory for the Gryffin workspace
	public static String gryffinHome;

	// Name of the property file
	private static String configFile;

	public GUIProperties() {
		super();

		File file = new File(gryffinHome, GUIProperties.FILENAME);
		try {
			if (file.exists()) {
				configFile = file.getCanonicalPath();
			} else {
				// create new property file in Gryffin home directory
				configFile = file.getCanonicalPath();
				file.createNewFile();
				logger.info("New property file created at '"
						+ file.getCanonicalPath() + "'.");
			}
		} catch (IOException ioe) {
			configFile = null;
			logger.error("Problem encountered while constructing GryffinProperties object: \n"
					+ ioe.fillInStackTrace());
		}
	}

	public static String getGryffinHome() {
		return gryffinHome;
	}

	public static void loadResources(String rootPath) {
		gryffinHome = rootPath;
	}

	// Load user properties from property file
	public void load() throws IOException {
		if (configFile == null) {
			return;
		}

		FileInputStream fis = new FileInputStream(configFile);
		load(fis);
		fis.close();
	}

	public Object setProperty(String key, String value) {
		logger.debug("Called with key='" + key + "' and value='" + value + "'.");
		return super.setProperty(key, value);
	}

	// Save user properties into property file
	public void save() {
		logger.debug("Saving property file (" + configFile + ")");
		if (configFile == null) {
			return;
		}

		try {
			FileOutputStream fos = new FileOutputStream(configFile);
			store(fos, "Properties modified on ");
			fos.close();
		} catch (Exception e) {
		}
	}

	public static String getPropertyFilename() {
		return configFile;
	}
}
