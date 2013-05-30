package grails.plugin.lightweight;

import com.google.common.base.Strings;
import com.google.common.io.ByteStreams;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.TagLibConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;

public class Launcher {

	private Configuration configuration;

	/**
	 * Start the server.
	 */
	public static void main(String[] args) throws IOException {
		final Launcher launcher = new Launcher(args);
		final File exploded = launcher.extractWar();
		launcher.deleteExplodedOnShutdown(exploded);
		launcher.start(exploded);
	}

	public Launcher(String[] args) throws IOException {
        String configYmlPath = args[0];
        System.out.println("Reading config from: " + configYmlPath);
		this.configuration = new Configuration(configYmlPath);
        System.out.println("Using configuration: " + this.configuration);
	}

	protected void start(File exploded) throws IOException {
		System.setProperty("org.eclipse.jetty.xml.XmlParser.NotValidating", "true");

		Server server = configureJetty(exploded);

		startJetty(server);
	}

	protected File extractWebdefaultXml() throws IOException {
		InputStream embeddedWebdefault = getClass().getClassLoader().getResourceAsStream("webdefault.xml");
		File temp = File.createTempFile("webdefault", ".war").getAbsoluteFile();
		temp.getParentFile().mkdirs();
		temp.deleteOnExit();
		copy(embeddedWebdefault, new FileOutputStream(temp));
		return temp;
	}

	protected Server configureJetty(File exploded) throws IOException {
		WebAppContext context = createStandardContext(exploded.getPath());

		if (this.configuration.isSsl()) {
			return configureHttpsServer(context);
		} else {
		    return configureHttpServer(context);
        }
	}

	protected void startJetty(Server server) {
		try {
			server.start();
			System.out.println("Startup complete. Server running on " + this.configuration.getPort());
		}
		catch (Exception e) {
			e.printStackTrace();
			System.err.println("Error loading Jetty: " + e.getMessage());
			System.exit(1);
		}
	}

	protected WebAppContext createStandardContext(String webappRoot) throws IOException {
		// Jetty requires a 'defaults descriptor' on the filesystem
		File webDefaults = extractWebdefaultXml();

		WebAppContext context = new WebAppContext(webappRoot, "/");

		setSystemProperty("java.naming.factory.url.pkgs", "org.eclipse.jetty.jndi");
		setSystemProperty("java.naming.factory.initial", "org.eclipse.jetty.jndi.InitialContextFactory");

		Class<?>[] configurationClasses = {
				WebInfConfiguration.class, WebXmlConfiguration.class, MetaInfConfiguration.class,
				FragmentConfiguration.class, EnvConfiguration.class, PlusConfiguration.class,
				JettyWebXmlConfiguration.class, TagLibConfiguration.class };
		org.eclipse.jetty.webapp.Configuration[] configurations = new org.eclipse.jetty.webapp.Configuration[configurationClasses.length];
		for (int i = 0; i < configurationClasses.length; i++) {
			configurations[i] = newConfigurationInstance(configurationClasses[i]);
		}

		context.setConfigurations(configurations);
		context.setDefaultsDescriptor(webDefaults.getPath());

		System.setProperty("TomcatKillSwitch.active", "true"); // workaround to prevent server exiting

		return context;
	}

	protected org.eclipse.jetty.webapp.Configuration newConfigurationInstance(Class<?> clazz) {
		 try {
			return (org.eclipse.jetty.webapp.Configuration) clazz.newInstance();
		}
		catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

    protected Server configureServer(WebAppContext context) {
		Server server = new Server();
		server.setHandler(context);
		return server;
    }

	protected Server configureHttpServer(WebAppContext context) {
		Server server = configureServer(context);

        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(this.configuration.getPort());
        setConnectors(server, connector);

		return server;
	}

	protected Server configureHttpsServer(WebAppContext context) throws IOException {
		Server server = configureServer(context);

        SslSelectChannelConnector sslConnector = new SslSelectChannelConnector();
        sslConnector.setPort(this.configuration.getPort());
        sslConnector.getSslContextFactory().setCertAlias(this.configuration.getKeyStoreAlias());
        sslConnector.getSslContextFactory().setKeyStore(this.configuration.getKeyStorePath());
        sslConnector.getSslContextFactory().setKeyStorePassword(this.configuration.getKeyStorePassword());

        setConnectors(server, sslConnector);

		return server;
	}

    protected void setConnectors(Server server, Connector... connectors) {
        for (Connector nextConnector : connectors) {
		    nextConnector.setMaxIdleTime(1000 * 60 * 60);
        }

        server.setConnectors(connectors);
    }

	protected void setSystemProperty(String name, String value) {
		if (!hasLength(System.getProperty(name))) {
			System.setProperty(name, value);
		}
	}

	protected File getWorkDir() {
		return new File(System.getProperty("java.io.tmpdir", ""));
	}

	protected File extractWar() throws IOException {
		File dir = new File(getWorkDir(), "standalone-war");
		deleteDir(dir);
		dir.mkdirs();
		return extractWar(dir);
	}

	protected File extractWar(File dir) throws IOException {
        String filePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath();
        System.out.println("Exploding jar at: " + filePath);
        FileInputStream fileInputStream = new FileInputStream(new File(filePath));
		return extractWar(fileInputStream, File.createTempFile("embedded", ".war", dir).getAbsoluteFile());
	}

	protected File extractWar(InputStream embeddedWarfile, File destinationWarfile) throws IOException {
		destinationWarfile.getParentFile().mkdirs();
		destinationWarfile.deleteOnExit();
		copy(embeddedWarfile, new FileOutputStream(destinationWarfile));
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
			unzip(e.nextElement(), zipfile, explodedDir);
		}
		zipfile.close();

		return explodedDir;
	}

	protected void unzip(ZipEntry entry, ZipFile zipfile, File explodedDir) throws IOException {

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
			copy(inputStream, outputStream);
		}
		finally {
			outputStream.close();
			inputStream.close();
		}
	}

	protected boolean hasLength(String s) {
        return !Strings.isNullOrEmpty(s);
	}

	protected void copy(InputStream in, OutputStream out) throws IOException {
        ByteStreams.copy(in, out);
	}

	// from DefaultGroovyMethods.deleteDir()
	protected boolean deleteDir(final File dir) {
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

	protected void deleteExplodedOnShutdown(final File exploded) {
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				deleteDir(exploded);
			}
		});
	}
}
