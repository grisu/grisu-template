package org.vpac.grisu.frontend.view.swing;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JMenuItem;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.TemplateManager;
import org.vpac.grisu.control.exceptions.NoSuchTemplateException;
import org.vpac.grisu.frontend.view.swing.jobcreation.JobCreationPanel;
import org.vpac.grisu.frontend.view.swing.jobcreation.TemplateJobCreationPanel;
import org.vpac.grisu.frontend.view.swing.settings.SettingsDialog;
import org.vpac.grisu.model.GrisuRegistryManager;
import org.vpac.security.light.Init;

public class GrisuTemplateApp extends GrisuApplicationWindow implements
		PropertyChangeListener {

	static final Logger myLogger = Logger.getLogger(GrisuTemplateApp.class
			.getName());

	public static void main(String[] args) {

		Init.initBouncyCastle();

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {

					final GrisuApplicationWindow appWindow = new GrisuTemplateApp();
					appWindow.setVisible(true);

				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});

	}

	private JMenuItem settingsItem;

	private final SettingsDialog dialog;

	private final GrisuMenu menu;

	private TemplateManager tm;

	public GrisuTemplateApp() {
		super();
		dialog = new SettingsDialog(this.getFrame());

		menu = new GrisuMenu(this.getFrame());

		menu.getToolsMenu().add(getSettingsItem());

		getFrame().setJMenuBar(menu);
	}

	private JobCreationPanel createFixedPanel(String panelClassName) {

		try {

			Class panelClass = null;

			if (panelClassName.contains(".")) {
				panelClass = Class.forName(panelClassName);
			} else {
				panelClass = Class
						.forName("org.vpac.grisu.frontend.view.swing.jobcreation.createJobPanels."
								+ panelClassName);
			}

			final JobCreationPanel panel = (JobCreationPanel) panelClass
					.newInstance();

			return panel;

		} catch (final Exception e) {
			e.printStackTrace();
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

	private JMenuItem getSettingsItem() {
		if (settingsItem == null) {
			settingsItem = new JMenuItem("Settings");
			settingsItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {

					dialog.setVisible(true);

				}
			});
		}
		return settingsItem;
	}

	@Override
	public void initOptionalStuff(ServiceInterface si) {

		menu.setServiceInterface(si);
		dialog.setServiceInterface(si);
		tm = GrisuRegistryManager.getDefault(si).getTemplateManager();
		tm.addTemplateManagerListener(this);
	}

	public void propertyChange(PropertyChangeEvent evt) {

		if (getServiceInterface() == null) {
			myLogger.info("No serviceInterface. Not updateing template list.");
			return;
		}

		refreshJobCreationPanels();

	}

}
