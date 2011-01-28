package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.job.JobSubmissionObjectImpl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JRadioButton;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class JobType extends AbstractInputPanel implements ActionListener {

	private static final long serialVersionUID = 1L;
	private JRadioButton singleRadioBox;
	private JRadioButton threadedRadioBox;
	private JRadioButton mpiRadioBox;

	private final static String SINGLE = "Single";
	private final static String THREADED = "Threaded";
	private final static String MPI = "MPI";

	private int lastMultiCpuValue = 2;

	private String currentActionCommand;

	private final ButtonGroup group = new ButtonGroup();

	public JobType(String name, PanelConfig config) throws TemplateException {

		super(name, config);

		final String orientation = config.getProperties().get("orientation");
		if (!StringUtils.isBlank(orientation) && "Y_AXIS".equals(orientation)) {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		} else {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		}
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("69px"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("93px"),
				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
				ColumnSpec.decode("50px:grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("23px"),
				FormFactory.RELATED_GAP_ROWSPEC, }));

		add(getSingleRadioBox(), "2, 2, left, top");
		add(getThreadedRadioBox(), "4, 2, left, top");
		add(getMpiRadioBox(), "6, 2, left, top");

		group.add(getSingleRadioBox());
		group.add(getThreadedRadioBox());
		group.add(getMpiRadioBox());

	}

	public void actionPerformed(ActionEvent e) {

		currentActionCommand = e.getActionCommand();

		if (SINGLE.equals(currentActionCommand)) {
			try {
				if (getJobSubmissionObject().getCpus() > 1) {
					lastMultiCpuValue = getJobSubmissionObject().getCpus();
				}
				setValue("cpus", 1);
				setValue("force_single", Boolean.TRUE);
			} catch (final TemplateException e1) {
				e1.printStackTrace();
			}
		} else if (THREADED.equals(currentActionCommand)) {
			try {
				if (getJobSubmissionObject().getCpus() == 1) {
					setValue("cpus", lastMultiCpuValue);
				}
				setValue("force_single", Boolean.TRUE);
				setValue("hostCount", 1);
			} catch (final TemplateException e1) {
				e1.printStackTrace();
			}
		} else if (MPI.equals(currentActionCommand)) {
			try {
				setValue("force_mpi", Boolean.TRUE);
			} catch (final TemplateException e1) {
				e1.printStackTrace();
			}
		} else {
			throw new RuntimeException("Command: " + currentActionCommand
					+ " can not be parsed to jobtype.");
		}

	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();
		defaultProperties.put("orientation", "X_AXIS");
		defaultProperties.put(TITLE, "Jobtype");

		return defaultProperties;
	}

	private JRadioButton getMpiRadioBox() {
		if (mpiRadioBox == null) {
			mpiRadioBox = new JRadioButton("MPI");
			mpiRadioBox.setActionCommand(MPI);
			mpiRadioBox.addActionListener(this);
		}
		return mpiRadioBox;
	}

	private JRadioButton getSingleRadioBox() {
		if (singleRadioBox == null) {
			singleRadioBox = new JRadioButton("Single");
			singleRadioBox.setActionCommand(SINGLE);
			singleRadioBox.addActionListener(this);
		}
		return singleRadioBox;
	}

	private JRadioButton getThreadedRadioBox() {
		if (threadedRadioBox == null) {
			threadedRadioBox = new JRadioButton("Threaded");
			threadedRadioBox.setActionCommand(THREADED);
			threadedRadioBox.addActionListener(this);
		}
		return threadedRadioBox;
	}

	@Override
	protected String getValueAsString() {
		return currentActionCommand;
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

		final String propertyName = e.getPropertyName();

		if ("force_single".equals(propertyName)) {
			if ((Boolean) e.getNewValue()) {
				if ("MPI".equals(currentActionCommand)) {
					if (getJobSubmissionObject().getCpus() > 1) {
						getThreadedRadioBox().setSelected(true);
					} else {
						getSingleRadioBox().setSelected(true);
					}
				}
			}
		} else if ("force_mpi".equals(propertyName)) {
			if ((Boolean) e.getNewValue()) {
				getMpiRadioBox().setSelected(true);
			}
		} else if ("cpus".equals(propertyName)) {
			if (SINGLE.equals(currentActionCommand)
					|| THREADED.equals(currentActionCommand)) {
				final int cpus = getJobSubmissionObject().getCpus();
				if (cpus == 1) {
					getSingleRadioBox().setSelected(true);
				} else {
					getThreadedRadioBox().setSelected(true);
				}
			}
		}

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
	throws TemplateException {

	}

	@Override
	void setInitialValue() throws TemplateException {

		String last = null;
		if (useHistory()) {
			last = getDefaultValue();
		}

		if (StringUtils.isBlank(last)) {
			final int cpus = getJobSubmissionObject().getCpus();
			if (cpus == 1) {
				getSingleRadioBox().setSelected(true);
			} else {
				getMpiRadioBox().setSelected(true);
			}
		} else {
			if (SINGLE.equals(last)) {
				getSingleRadioBox().setSelected(true);
			} else if (THREADED.equals(last)) {
				getThreadedRadioBox().setSelected(true);
			} else if (MPI.equals(last)) {
				getMpiRadioBox().setSelected(true);
			} else {
				myLogger.error("Value: " + last
						+ " not a valid value for Jobtype.");
			}
		}

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}

	}
}
