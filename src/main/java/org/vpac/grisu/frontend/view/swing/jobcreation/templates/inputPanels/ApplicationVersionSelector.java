package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;
import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import org.vpac.grisu.model.FqanEvent;
import org.vpac.grisu.model.GrisuRegistryManager;
import org.vpac.grisu.model.info.ApplicationInformation;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;

import au.org.arcs.jcommons.constants.Constants;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ApplicationVersionSelector extends AbstractInputPanel implements
		EventSubscriber<FqanEvent> {
	private JComboBox comboBox;
	private final DefaultComboBoxModel versionModel = new DefaultComboBoxModel();

	private boolean lastVersionEmpty = false;
	private boolean lockVersion = false;

	private String lastFqan = null;
	private String lastApplication = null;

	private Thread appVersionThread = null;

	public ApplicationVersionSelector(String templateName, PanelConfig config)
			throws TemplateException {
		super(templateName, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(103dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getComboBox(), "2, 2, fill, default");

		EventBus.subscribe(FqanEvent.class, this);

	}

	private void changeJobApplicationVersion(String version) {

		if (lockVersion) {
			return;
		}
		try {
			if (StringUtils.isBlank(version)
					|| Constants.NO_VERSION_INDICATOR_STRING.equals(version)
					|| "n/a".equals(version)) {

				if (lastVersionEmpty) {
					return;
				}

				setValue("applicationVersion",
						Constants.NO_VERSION_INDICATOR_STRING);
				lastVersionEmpty = true;
			}

			setValue("applicationVersion", version);
			lastVersionEmpty = false;
		} catch (TemplateException e1) {
			e1.printStackTrace();
		}
	}

	private JComboBox getComboBox() {
		if (comboBox == null) {
			comboBox = new JComboBox(versionModel);
			comboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {

					if (ItemEvent.SELECTED == e.getStateChange()) {
						String version = (String) versionModel
								.getSelectedItem();

						changeJobApplicationVersion(version);
					}

				}
			});
		}
		return comboBox;
	}

	@Override
	protected String getValueAsString() {
		return (String) getComboBox().getSelectedItem();
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

		if (!isInitFinished()) {
			return;
		}

		if (Constants.APPLICATIONNAME_KEY.equals(e.getPropertyName())) {
			final String app = (String) e.getNewValue();
			if (StringUtils.isBlank(app)) {
				return;
			}
			setProperApplicationVersion(app);
			return;

		}

	}

	private void lockUI(final boolean lock) {

		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				getComboBox().setEnabled(!lock);
				// if (lock) {
				// versionModel.setSelectedItem("Searching...");
				// } else {
				// versionModel.removeElement("Searching...");
				// }
			}
		});

	}

	public void onEvent(FqanEvent arg0) {

		setProperApplicationVersion(getJobSubmissionObject().getApplication());
	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
			throws TemplateException {
		// TODO Auto-generated method stub

	}

	private synchronized void queryApplicationVersion(final String app,
			final String fqan) {

		lockVersion = true;

		final String lastVersion = (String) versionModel.getSelectedItem();
		lockUI(true);

		if (StringUtils.isBlank(app)
				|| Constants.GENERIC_APPLICATION_NAME.equals(app)) {

			versionModel.removeAllElements();
			versionModel.addElement("n/a");
			lockVersion = false;
			lockUI(false);
			changeJobApplicationVersion(Constants.NO_VERSION_INDICATOR_STRING);
			return;
		}

		// if (Thread.interrupted()) {
		// lockUI(false);
		// lockVersion = false;
		// return;
		// }

		ApplicationInformation info = GrisuRegistryManager.getDefault(
				getServiceInterface()).getApplicationInformation(app);

		final Set<String> allVersions = info
				.getAllAvailableVersionsForFqan(fqan);

		// if (Thread.interrupted()) {
		// lockUI(false);
		// lockVersion = false;
		// return;
		// }

		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {

				versionModel.removeAllElements();

				if (allVersions.size() == 0) {
					versionModel.addElement("n/a");
				} else {
					if (allVersions.size() > 1) {
						versionModel
								.addElement(Constants.NO_VERSION_INDICATOR_STRING);
					}
					for (String version : allVersions) {
						versionModel.addElement(version);
					}
				}

				if (StringUtils.isNotBlank(lastVersion)
						&& (versionModel.getIndexOf(lastVersion) >= 0)) {
					versionModel.setSelectedItem(lastVersion);
					System.out.println("Last version: " + lastVersion);
				} else {
					versionModel.setSelectedItem(versionModel.getElementAt(0));
					System.out.println("First entry version: "
							+ versionModel.getElementAt(0));
				}

			}
		});

		lockUI(false);
		lockVersion = false;
		changeJobApplicationVersion((String) versionModel.getSelectedItem());
	}

	@Override
	void setInitialValue() throws TemplateException {

	}

	private void setProperApplicationVersion(final String app) {

		final String currentFqan = getUserEnvironmentManager().getCurrentFqan();
		System.out.println(app + "/" + lastApplication);
		System.out.println(currentFqan + "/" + lastFqan);

		if (app == null) {
			if ((lastApplication == null) && currentFqan.equals(lastFqan)) {
				return;
			}
		} else if (app.equals(lastApplication) && currentFqan.equals(lastFqan)) {
			return;
		}

		lastApplication = app;
		lastFqan = currentFqan;

		if ((appVersionThread != null) && appVersionThread.isAlive()) {
			appVersionThread.interrupt();
			try {
				appVersionThread.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		appVersionThread = new Thread() {
			@Override
			public void run() {

				queryApplicationVersion(app, currentFqan);

			}
		};

		appVersionThread.start();

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

	}
}
