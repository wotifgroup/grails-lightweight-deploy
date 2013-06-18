package grails.plugin.lightweightdeploy;

import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a deployable War file.
 */
public class War {
    private static final Logger logger = LoggerFactory.getLogger(War.class);

    private File directory;

    /**
     * Creates a deployable war file, given a directory to work in.
     *
     * @param workDir The work directory, where the war contents can be exploded.
     * @throws IOException If there was an error exploding the war.
     */
    public War(File workDir) throws IOException {
		File target = new File(workDir, "lightweight-war");
		Utils.deleteDir(target);
		target.mkdirs();
		this.directory = extractWar(target);

        deleteOnShutdown();
    }

    public File getDirectory() {
        return directory;
    }

    protected File extractWar(File workDir) throws IOException {
        String filePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        logger.info("Exploding jar at: " + filePath);
        FileInputStream fileInputStream = new FileInputStream(new File(filePath));
		return extractWar(fileInputStream, File.createTempFile("embedded", ".war", workDir).getAbsoluteFile());
	}

	protected File extractWar(InputStream embeddedWarfile, File destinationWarfile) throws IOException {
		destinationWarfile.getParentFile().mkdirs();
		destinationWarfile.deleteOnExit();
		ByteStreams.copy(embeddedWarfile, new FileOutputStream(destinationWarfile));
		return explode(destinationWarfile);
	}

	protected File explode(File war) throws IOException {
		String basename = war.getName();
		int index = basename.lastIndexOf('.');
		if (index > -1) {
			basename = basename.substring(0, index);
		}
		File explodedDir = new File(war.getParentFile(), basename + "-exploded-" + System.currentTimeMillis());

		ZipFile zipfile = new ZipFile(war);
		for (Enumeration<? extends ZipEntry> e = zipfile.entries(); e.hasMoreElements(); ) {
			Utils.unzip(e.nextElement(), zipfile, explodedDir);
		}
		zipfile.close();

		return explodedDir;
	}

	protected void deleteOnShutdown() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				Utils.deleteDir(getDirectory());
			}
		});
	}
}
