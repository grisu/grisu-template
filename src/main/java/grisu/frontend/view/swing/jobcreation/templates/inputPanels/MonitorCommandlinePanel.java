package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.job.JobSubmissionObjectImpl;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MonitorCommandlinePanel extends AbstractInputPanel {
	private JTextField textField;

	public MonitorCommandlinePanel(String name, PanelConfig config)
	throws TemplateException {

		super(name, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getTextField(), "2, 2, fill, fill");
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();
		defaultProperties.put(NAME, "Name");
		defaultProperties.put(TITLE, "Commandline");
		defaultProperties.put(DEFAULT_VALUE, "n/a");

		return defaultProperties;
	}

	@Override
	public JTextComponent getTextComponent() {
		return getTextField();
	}

	private JTextField getTextField() {
		if (textField == null) {
			textField = new JTextField();
			textField.setEditable(false);
			textField.setColumns(10);
		}
		return textField;
	}

	@Override
	protected String getValueAsString() {
		return null;
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

		if ("commandline".equals(e.getPropertyName())) {
			final String newJobname = (String) e.getNewValue();
			getTextField().setText(newJobname);
		}

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties) {

	}

	@Override
	void setInitialValue() throws TemplateException {
		getTextField().setText(getJobSubmissionObject().getCommandline());
	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {
		// TODO Auto-generated method stub

	}
}
