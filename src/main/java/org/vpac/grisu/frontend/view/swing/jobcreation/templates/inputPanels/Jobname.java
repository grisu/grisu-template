package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.netbeans.validation.api.Validator;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.validators.JobnameValidator;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class Jobname extends AbstractInputPanel {

	static final String REPLACEMENT_CHARACTERS = "\\s|;|'|\"|,|\\$|\\?|#";

	public static String JOBNAME_CALC_METHOD_KEY = "jobnameCalcMethod";

	private JTextField jobnameTextField;

	private final String autoJobnameMethod = null;

	public Jobname(String name, PanelConfig config) throws TemplateException {

		super(name, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getJobnameTextField(), "2, 2, fill, fill");

		Validator<String> val = new JobnameValidator();
		config.addValidator(val);
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		Map<String, String> defaultProperties = new HashMap<String, String>();

		defaultProperties.put(TITLE, "Jobname");
		// defaultProperties.put(DEFAULT_VALUE, "gridJob");
		defaultProperties.put(JOBNAME_CALC_METHOD_KEY, "uniqueNumber");

		return defaultProperties;
	}

	private JTextField getJobnameTextField() {
		if (jobnameTextField == null) {
			jobnameTextField = new JTextField();
			jobnameTextField.setColumns(10);
			jobnameTextField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyReleased(KeyEvent e) {
					try {
						String input = jobnameTextField.getText();
						int index = jobnameTextField.getCaretPosition();
						input = input.replaceAll(REPLACEMENT_CHARACTERS, "_");
						jobnameTextField.setText(input.trim());
						jobnameTextField.setCaretPosition(index);

						setValue("jobname", jobnameTextField.getText());
					} catch (TemplateException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}

			});
		}
		return jobnameTextField;
	}

	@Override
	public JTextComponent getTextComponent() {
		return getJobnameTextField();
	}

	@Override
	protected String getValueAsString() {
		return getJobnameTextField().getText();
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

		if ("jobname".equals(e.getPropertyName())) {
			String newJobname = (String) e.getNewValue();
			getJobnameTextField().setText(newJobname);
		}

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
			throws TemplateException {

	}

	@Override
	void setInitialValue() throws TemplateException {

		String defaultValue = getPanelProperty(DEFAULT_VALUE);
		if (StringUtils.isNotBlank(defaultValue)) {
			String sugJobname = getUserEnvironmentManager()
					.calculateUniqueJobname(defaultValue);
			setValue("jobname", sugJobname);
		}

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {
		// TODO Auto-generated method stub

	}
}
