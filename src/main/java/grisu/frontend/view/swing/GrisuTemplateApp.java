package grisu.frontend.view.swing;

import grisu.control.ServiceInterface;
import grisu.control.TemplateManager;
import grisu.control.exceptions.NoSuchTemplateException;
import grisu.frontend.control.login.LoginManager;
import grisu.frontend.view.swing.files.virtual.GridFileTreePanel;
import grisu.frontend.view.swing.jobcreation.JobCreationPanel;
import grisu.frontend.view.swing.jobcreation.TemplateJobCreationPanel;
import grisu.frontend.view.swing.settings.AdvancedTemplateClientSettingsPanel;
import grisu.frontend.view.swing.settings.ApplicationSubscribePanel;
import grisu.frontend.view.swing.utils.DefaultExceptionHandler;
import grisu.jcommons.utils.EnvironmentVariableHelpers;
import grisu.model.GrisuRegistryManager;
import grisu.settings.Environment;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * The main entry class for the template client.
 * 
 * @author markus
 * 
 */
public class GrisuTemplateApp extends GrisuApplicationWindow implements
PropertyChangeListener {

	static final Logger myLogger = Logger.getLogger(GrisuTemplateApp.class
			.getName());

	private static void configLogging() {
		// stop javaxws logging
		java.util.logging.LogManager.getLogManager().reset();
		java.util.logging.Logger.getLogger("root").setLevel(Level.ALL);

		String logback = "/etc/gricli/grisu-template.log.conf.xml";

		if (!new File(logback).exists() || (new File(logback).length() == 0)) {
			logback = Environment.getGrisuClientDirectory() + File.separator
					+ "grisu-template.log.conf.xml";
		}
		if (new File(logback).exists() && (new File(logback).length() > 0)) {

			LoggerContext lc = (LoggerContext) LoggerFactory
					.getILoggerFactory();

			try {
				JoranConfigurator configurator = new JoranConfigurator();
				configurator.setContext(lc);
				// the context was probably already configured by default
				// configuration
				// rules
				lc.reset();
				configurator.doConfigure(logback);
			} catch (JoranException je) {
				je.printStackTrace();
			}
			StatusPrinter.printInCaseOfErrorsOrWarnings(lc);

		}
	}

	public static void main(String[] args) throws Exception {
		myLogger.debug("Grisu template client. Starting...");
		GrisuTemplateApp app = new GrisuTemplateApp();
		app.run();
	}

	private TemplateManager tm;

	private final ApplicationSubscribePanel applicationSubscribePanel = new ApplicationSubscribePanel();

	public GrisuTemplateApp() throws Exception {
		super(new AdvancedTemplateClientSettingsPanel());


		// String environmentVariable = System
		// .getProperty("grisu.defaultApplications");
		// if (StringUtils.isBlank(environmentVariable)) {
		// environmentVariable = System.getProperty("grisu.createJobPanels");
		// if (StringUtils.isBlank(environmentVariable)) {
		// // only add that when no predefined applications
		// applicationSubscribePanel = new ApplicationSubscribePanel();
		// tabbedPane.addTab("Applications", null,
		// applicationSubscribePanel, null);
		// }
		// }
		addSettingsPanel("Applications", applicationSubscribePanel);
	}

	private JobCreationPanel createFixedPanel(String panelClassName) {

		try {

			Class panelClass = null;

			if (panelClassName.contains(".")) {
				panelClass = Class.forName(panelClassName);
			} else {
				panelClass = Class
						.forName("grisu.frontend.view.swing.jobcreation.createJobPanels."
								+ panelClassName);
			}

			final JobCreationPanel panel = (JobCreationPanel) panelClass
					.newInstance();

			return panel;

		} catch (final Exception e) {
			myLogger.error(e);
			return null;
		}
	}

	@Override
	public boolean displayAppSpecificMonitoringItems() {
		return true;
	}

	@Override
	public boolean displayBatchJobsCreationPane() {
		return true;
	}

	@Override
	public boolean displaySingleJobsCreationPane() {
		return true;
	}

	@Override
	public Set<String> getApplicationsToMonitor() {
		return null;
	}

	@Override
	public JobCreationPanel[] getJobCreationPanels() {

		if (getServiceInterface() == null) {
			return new JobCreationPanel[] {};
		}

		final List<JobCreationPanel> panels = new LinkedList<JobCreationPanel>();

		final String fixedPanels = System.getProperty("grisu.createJobPanels");
		if (StringUtils.isNotBlank(fixedPanels)) {

			for (final String panel : fixedPanels.split(",")) {

				final JobCreationPanel creationPanel = createFixedPanel(panel);
				if (creationPanel != null) {
					panels.add(creationPanel);
				}

			}

		}

		SortedSet<String> allTemplates = null;
		final String fixedTemplates = System
				.getProperty("grisu.defaultApplications");
		if (StringUtils.isNotBlank(fixedTemplates)) {
			myLogger.debug("Found defaultApplications: " + fixedTemplates);
			final String[] temp = fixedTemplates.split(",");
			allTemplates = new TreeSet<String>(Arrays.asList(temp));
		} else {
			myLogger.debug("Didn't find defaultApplications,");
			allTemplates = tm.getAllTemplateNames();
		}

		for (final String name : allTemplates) {
			try {
				final JobCreationPanel panel = new TemplateJobCreationPanel(
						name, tm.getTemplate(name));
				if (panel == null) {
					myLogger.warn("Can't find template " + name);
					continue;
				}
				panel.setServiceInterface(getServiceInterface());
				panels.add(panel);
			} catch (final NoSuchTemplateException e) {
				myLogger.warn("Can't find template " + name);
				continue;
			}
		}

		return panels.toArray(new JobCreationPanel[] {});
	}

	@Override
	public String getName() {

		final String name = System.getProperty("name");
		if (StringUtils.isNotBlank(name)) {
			return name;
		} else {
			return "Grisu template client";
		}
	}

	@Override
	public void initOptionalStuff(final ServiceInterface si) {

		new Thread() {
			@Override
			public void run() {
				GrisuRegistryManager.getDefault(si).getResourceInformation()
				.getAllApplications();
			}
		}.start();
		applicationSubscribePanel.setServiceInterface(si);

		tm = GrisuRegistryManager.getDefault(si).getTemplateManager();
		tm.addTemplateManagerListener(this);

		GridFileTreePanel.defaultRoots.clear();
		GridFileTreePanel.defaultRoots.put("Data Fabric",
				"grid://groups/nz/nesi//");
		GridFileTreePanel.defaultRoots
				.put(GridFileTreePanel.REMOTE_ALIAS, null);
		GridFileTreePanel.defaultRoots.put(GridFileTreePanel.LOCAL_ALIAS, null);
		addGroupFileListPanel(null, null);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		if (getServiceInterface() == null) {
			myLogger.info("No serviceInterface. Not updateing template list.");
			return;
		}

		refreshJobCreationPanels();

	}

	public void run() {

		configLogging();

		myLogger.debug("Starting template client.");

		Thread.currentThread().setName("main");

		LoginManager.setClientName("grisu-template");

		LoginManager.setClientVersion(grisu.jcommons.utils.Version
				.get("this-client"));

		EnvironmentVariableHelpers.loadEnvironmentVariablesToSystemProperties();

		Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler());

		LoginManager.initEnvironment();

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {

					// final GrisuApplicationWindow appWindow = new
					// GrisuTemplateApp();

					setVisible(true);

				} catch (final Exception e) {
					myLogger.error(e);
				}
			}
		});

	}

}
