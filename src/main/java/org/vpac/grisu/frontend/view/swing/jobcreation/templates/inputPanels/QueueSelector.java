package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.bushe.swing.event.EventBus;
import org.bushe.swing.event.EventSubscriber;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels.helperPanels.HidingQueueInfoPanel;
import org.vpac.grisu.model.FqanEvent;
import org.vpac.grisu.model.GrisuRegistryManager;
import org.vpac.grisu.model.info.ApplicationInformation;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;

import au.org.arcs.jcommons.constants.Constants;
import au.org.arcs.jcommons.interfaces.GridResource;
import au.org.arcs.jcommons.utils.SubmissionLocationHelpers;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class QueueSelector extends AbstractInputPanel implements
		EventSubscriber<FqanEvent> {
	private JLabel lblQueue;
	private JComboBox queueComboBox;
	private JPanel panel;
	private HidingQueueInfoPanel hidingQueueInfoPanel;

	private final DefaultComboBoxModel queueModel = new DefaultComboBoxModel();

	private SortedSet<GridResource> currentQueues = null;

	private String lastApplication = Constants.GENERIC_APPLICATION_NAME;
	private String lastVersion = Constants.NO_VERSION_INDICATOR_STRING;
	private String lastFqan = null;

	private Thread loadThread;

	private String lastSubLoc = null;

	private boolean interrupted = false;

	public QueueSelector(String templateName, PanelConfig config)
			throws TemplateException {
		super(templateName, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getLblQueue(), "2, 2, right, default");
		add(getQueueComboBox(), "4, 2, fill, default");
		// add(getHidingQueueInfoPanel(), "2, 4, 3, 1, fill, fill");

		EventBus.subscribe(FqanEvent.class, this);
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {
		final Map<String, String> defaultProperties = new HashMap<String, String>();

		return defaultProperties;
	}

	private HidingQueueInfoPanel getHidingQueueInfoPanel() {
		if (hidingQueueInfoPanel == null) {
			hidingQueueInfoPanel = new HidingQueueInfoPanel();
		}
		return hidingQueueInfoPanel;
	}

	private JLabel getLblQueue() {
		if (lblQueue == null) {
			lblQueue = new JLabel("Submit to:");
		}
		return lblQueue;
	}

	private JComboBox getQueueComboBox() {
		if (queueComboBox == null) {
			queueComboBox = new JComboBox(queueModel);
			queueComboBox
					.setPrototypeDisplayValue("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
			queueComboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {

					GridResource gr;
					try {
						gr = (GridResource) (queueModel.getSelectedItem());
						if (gr == null) {
							return;
						}
					} catch (final Exception ex) {
						return;
					}
					final String subLoc = SubmissionLocationHelpers
							.createSubmissionLocationString(gr);

					if (subLoc.equals(lastSubLoc)) {
						return;
					}
					lastSubLoc = subLoc;

					try {
						System.out.println("Setting: " + subLoc);
						setValue("submissionLocation", subLoc);
					} catch (final TemplateException e1) {
						e1.printStackTrace();
					}
				}
			});
		}
		return queueComboBox;
	}

	@Override
	protected String getValueAsString() {
		return null;
	}

	@Override
	protected synchronized void jobPropertyChanged(PropertyChangeEvent e) {

		if (!isInitFinished()) {
			return;
		}

		final String[] possibleBeans = new String[] {
				Constants.COMMANDLINE_KEY, Constants.APPLICATIONNAME_KEY,
				Constants.APPLICATIONVERSION_KEY, Constants.FORCE_MPI_KEY,
				Constants.FORCE_SINGLE_KEY, Constants.HOSTCOUNT_KEY,
				Constants.MEMORY_IN_B_KEY, Constants.NO_CPUS_KEY,
				Constants.WALLTIME_IN_MINUTES_KEY };

		boolean reloadQueues = false;
		for (final String bean : possibleBeans) {
			if (bean.equals(e.getPropertyName())) {
				reloadQueues = true;
				break;
			}
		}

		if (!reloadQueues) {
			return;
		}

		// if (Constants.COMMANDLINE_KEY.equals(e.getPropertyName())
		// || Constants.APPLICATIONNAME_KEY.equals(e.getPropertyName())
		// || Constants.APPLICATIONVERSION_KEY.equals(e.getPropertyName())) {
		//
		//
		//
		// }

		System.out.println("LOading queues.");
		loadQueues();
	}

	private void loadQueues() {

		String tempApp = getJobSubmissionObject().getApplication();
		String tempVers = getJobSubmissionObject().getApplicationVersion();

		String currentFqan = getUserEnvironmentManager().getCurrentFqan();

		// if (!interrupted) {

		if (tempApp == null) {

			if ((lastApplication == null) && tempVers.equals(lastVersion)
					&& currentFqan.equals(lastFqan)) {
				return;
			}

		} else {

			if (tempApp.equals(lastApplication) && tempVers.equals(lastVersion)
					&& currentFqan.equals(lastFqan)) {
				return;
			}
		}
		// }

		lastApplication = tempApp;
		lastVersion = tempVers;
		lastFqan = currentFqan;
		System.out.println("App: " + lastApplication);
		System.out.println("Version: " + lastVersion);
		System.out.println("Fqan: " + lastFqan);

		if ((loadThread != null) && loadThread.isAlive()) {
			loadThread.interrupt();
		}

		loadThread = new Thread() {
			@Override
			public void run() {
				loadQueuesIntoComboBox();
			}

		};

		loadThread.start();

	}

	private synchronized void loadQueuesIntoComboBox() {
		GridResource oldSubLoc = null;

		try {
			oldSubLoc = (GridResource) queueModel.getSelectedItem();
		} catch (final Exception e) {
			// doesn't matter
		}
		setLoading(true);
		final JobSubmissionObjectImpl job = getJobSubmissionObject();
		if (job == null) {
			setLoading(false);
			return;
		}
		String applicationName = job.getApplication();
		if (applicationName == null) {
			applicationName = Constants.GENERIC_APPLICATION_NAME;
		}

		final ApplicationInformation ai = GrisuRegistryManager.getDefault(
				getServiceInterface()).getApplicationInformation(
				applicationName);

		// if (Thread.interrupted()) {
		// System.out.println("interrupted 1");
		// setLoading(false);
		// interrupted = true;
		// return;
		// }

		currentQueues = ai.getAllSubmissionLocationsAsGridResources(
				getJobSubmissionObject().getJobSubmissionPropertyMap(),
				GrisuRegistryManager.getDefault(getServiceInterface())
						.getUserEnvironmentManager().getCurrentFqan());

		// if (Thread.interrupted()) {
		// System.out.println("Interrupted 2");
		// interrupted = true;
		// return;
		// }

		if ((currentQueues == null) || (currentQueues.size() == 0)) {

			SwingUtilities.invokeLater(new Thread() {
				@Override
				public void run() {
					queueModel.removeAllElements();
					queueModel
							.addElement("No location available for selected values");
					setLoading(false);
				}
			});
			return;
		}

		setLoading(false);

		// if (Thread.interrupted()) {
		// System.out.println("interrupted 3");
		// interrupted = true;
		// return;
		// }

		queueModel.removeAllElements();
		boolean containsOld = false;
		for (final GridResource gr : currentQueues) {
			if (gr.equals(oldSubLoc)) {
				containsOld = true;
			}
			queueModel.addElement(gr);
		}
		if (containsOld) {
			final GridResource temp = oldSubLoc;
			queueModel.setSelectedItem(temp);
		}
		interrupted = false;
	}

	public void onEvent(FqanEvent arg0) {

		loadQueues();

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
			throws TemplateException {

	}

	@Override
	void setInitialValue() throws TemplateException {

		loadQueues();

	}

	private void setLoading(final boolean loading) {

		SwingUtilities.invokeLater(new Thread() {
			@Override
			public void run() {
				if (loading) {
					queueModel.removeAllElements();
					queueModel.addElement("Searching...");
				} else {
					queueModel.removeElement("Searching...");
				}

				getQueueComboBox().setEnabled(!loading);
				getHidingQueueInfoPanel().setLoading(loading);
			}
		});
	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}

	}
}
