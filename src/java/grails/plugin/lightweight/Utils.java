package grails.plugin.lightweight;

import com.google.common.io.ByteStreams;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {

    public static boolean deleteDir(final File dir) {
		if (!dir.exists()) {
			return true;
		}

		if (!dir.isDirectory()) {
			return false;
		}

		File[] files = dir.listFiles();
		if (files == null) {
			return false;
		}

		boolean result = true;
		for (File file : files) {
			if (file.isDirectory()) {
				if (!deleteDir(file)) {
					result = false;
				}
			}
			else {
				if (!file.delete()) {
					result = false;
				}
			}
		}

		if (!dir.delete()) {
			result = false;
		}

		return result;
	}

	public static void unzip(ZipEntry entry, ZipFile zipfile, File explodedDir) throws IOException {

		if (entry.isDirectory()) {
			new File(explodedDir, entry.getName()).mkdirs();
			return;
		}

		File outputFile = new File(explodedDir, entry.getName());
		if (!outputFile.getParentFile().exists()) {
			outputFile.getParentFile().mkdirs();
		}

		BufferedInputStream inputStream = new BufferedInputStream(zipfile.getInputStream(entry));
		BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

		try {
			ByteStreams.copy(inputStream, outputStream);
		}
		finally {
			outputStream.close();
			inputStream.close();
		}
	}
}
