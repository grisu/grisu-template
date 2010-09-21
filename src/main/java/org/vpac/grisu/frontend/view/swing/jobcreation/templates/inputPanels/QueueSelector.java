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

import org.apache.commons.lang.StringUtils;
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

	private String lastApplication = null;

	private Thread loadThread;

	private String lastSubLoc = null;

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

					System.out.println(subLoc);
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
	protected void jobPropertyChanged(PropertyChangeEvent e) {

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

		if (Constants.COMMANDLINE_KEY.equals(e.getPropertyName())) {
			final String temp = getJobSubmissionObject().getApplication();
			if (temp == null) {
				if (lastApplication == null) {
					lastApplication = temp;
					return;
				}
			} else {
				if (lastApplication == null || temp.equals(lastApplication)) {
					lastApplication = temp;
					return;
				}
			}
		}
		loadQueues();
	}

	private void loadQueues() {

		if (loadThread != null && loadThread.isAlive()) {
			// I know, I know. But I think it's ok in this case.
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
			return;
		}
		String applicationName = job.getApplication();
		if (StringUtils.isBlank(applicationName)) {
			applicationName = Constants.GENERIC_APPLICATION_NAME;
		}
		final ApplicationInformation ai = GrisuRegistryManager.getDefault(
				getServiceInterface()).getApplicationInformation(
				applicationName);

		if (Thread.interrupted()) {
			return;
		}

		currentQueues = ai.getAllSubmissionLocationsAsGridResources(
				getJobSubmissionObject().getJobSubmissionPropertyMap(),
				GrisuRegistryManager.getDefault(getServiceInterface())
						.getUserEnvironmentManager().getCurrentFqan());

		if (Thread.interrupted()) {
			return;
		}

		if (currentQueues == null) {
			return;
		}

		setLoading(false);
		queueModel.removeAllElements();
		boolean containsOld = false;
		for (final GridResource gr : currentQueues) {
			if (gr.equals(oldSubLoc)) {
				containsOld = true;
			}
			SwingUtilities.invokeLater(new Thread() {

				@Override
				public void run() {
					queueModel.addElement(gr);
				}

			});

		}
		if (containsOld) {
			final GridResource temp = oldSubLoc;
			SwingUtilities.invokeLater(new Thread() {

				@Override
				public void run() {
					queueModel.setSelectedItem(temp);
				}

			});
		}
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
					queueModel.addElement("Calculating...");
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
