package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.jcommons.constants.Constants;
import grisu.model.FqanEvent;
import grisu.model.GrisuRegistryManager;
import grisu.model.info.ApplicationInformation;
import grisu.model.info.dto.Version;
import grisu.model.job.JobDescription;

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

	private synchronized void changeJobApplicationVersion(Version version) {

		if (lockVersion) {
			return;
		}
		try {
			if ((version == null) || version.equals(Version.ANY_VERSION)) {

				if (lastVersionEmpty) {
					return;
				}

				setValue("applicationVersion", Version.ANY_VERSION.getVersion());
				lastVersionEmpty = true;
			} else {
				setValue("applicationVersion", version.getVersion());
				lastVersionEmpty = false;
			}
		} catch (TemplateException e1) {
			myLogger.error(e1);
		}
	}

	private JComboBox getComboBox() {
		if (comboBox == null) {
			comboBox = new JComboBox(versionModel);
			comboBox.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxx");
			comboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {

					if (!isInitFinished()) {
						return;
					}

					if (ItemEvent.SELECTED == e.getStateChange()) {
						final Version version = (Version) versionModel
								.getSelectedItem();

						new Thread() {
							@Override
							public void run() {
								changeJobApplicationVersion(version);

							}
						}.start();
					}

				}
			});
		}
		return comboBox;
	}

	@Override
	protected String getValueAsString() {
		Version temp = (Version) getComboBox().getSelectedItem();
		if (temp == null) {
			return "";
		} else {
			return temp.getVersion();
		}
	}

	@Override
	protected synchronized void jobPropertyChanged(PropertyChangeEvent e) {

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

	public synchronized void onEvent(FqanEvent arg0) {
		
		if ( getJobSubmissionObject() == null ) {
			return;
		}

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

		final Version lastVersion = (Version) versionModel.getSelectedItem();
		lockUI(true);

		if (StringUtils.isBlank(app)
				|| Constants.GENERIC_APPLICATION_NAME.equals(app)) {

			SwingUtilities.invokeLater(new Thread() {
				@Override
				public void run() {
					versionModel.removeAllElements();
					versionModel.addElement(Version.ANY_VERSION);
				}
			});
			lockVersion = false;
			lockUI(false);
			changeJobApplicationVersion(Version.ANY_VERSION);
			return;
		}

		// if (Thread.interrupted()) {
		// lockUI(false);
		// lockVersion = false;
		// return;
		// }

		ApplicationInformation info = GrisuRegistryManager.getDefault(
				getServiceInterface()).getApplicationInformation(app);

		final Set<Version> allVersions = info
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
					System.out.println("ZERO");
					versionModel.addElement(Version.ANY_VERSION);
				} else if (allVersions.size() == 1) {
					System.out.println("ONE");
					versionModel.addElement(allVersions.iterator().next());
				} else if ((allVersions.size() == 2)
						&& allVersions.contains(Version.ANY_VERSION)) {
					java.util.Iterator<Version> it = allVersions.iterator();
					while (it.hasNext()) {
						Version v = it.next();
						if (!Version.ANY_VERSION.equals(v)) {
							versionModel.addElement(v);
							break;
						}
					}
				} else {
					if (allVersions.size() > 1) {
						versionModel.addElement(Version.ANY_VERSION);
					}
					for (Version version : allVersions) {
						if (versionModel.getIndexOf(version) < 0) {
							versionModel.addElement(version);
						}
					}
				}

				if ((lastVersion != null)
						&& (versionModel.getIndexOf(lastVersion) >= 0)) {
					versionModel.setSelectedItem(lastVersion);
				} else {
					versionModel.setSelectedItem(versionModel.getElementAt(0));
				}

			}
		});

		lockUI(false);
		lockVersion = false;
		changeJobApplicationVersion((Version) versionModel.getSelectedItem());
	}

	@Override
	void setInitialValue() throws TemplateException {
		final String defaultValue = getPanelProperty(DEFAULT_VALUE);
		// X.p("xxx" + defaultValue);
		if (StringUtils.isNotBlank(defaultValue)) {
			changeJobApplicationVersion(new Version(defaultValue));
		}
	}

	private synchronized void setProperApplicationVersion(final String app) {

		final String currentFqan = getUserEnvironmentManager().getCurrentFqan();

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
				myLogger.error(e);
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
	protected void templateRefresh(JobDescription jobObject) {

	}
}
