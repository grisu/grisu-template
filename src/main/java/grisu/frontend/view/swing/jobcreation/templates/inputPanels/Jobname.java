package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.frontend.view.swing.jobcreation.templates.validators.JobnameValidator;
import grisu.model.job.JobSubmissionObjectImpl;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.netbeans.validation.api.Validator;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class Jobname extends AbstractInputPanel {


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

		final Validator<String> val = new JobnameValidator();
		config.addValidator(val);
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();

		defaultProperties.put(TITLE, "Jobname");
		// defaultProperties.put(DEFAULT_VALUE, "gridJob");
		defaultProperties
		.put(grisu.frontend.view.swing.jobcreation.widgets.Jobname.JOBNAME_CALC_METHOD_KEY,
				"uniqueNumber");

		return defaultProperties;
	}

	private JTextField getJobnameTextField() {
		if (jobnameTextField == null) {
			jobnameTextField = new JTextField();
			jobnameTextField.setColumns(10);
			jobnameTextField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyReleased(KeyEvent e) {
					int pos = jobnameTextField.getCaretPosition();
					try {
						String input = jobnameTextField.getText();
						final int index = jobnameTextField.getCaretPosition();
						input = input
								.replaceAll(
										grisu.frontend.view.swing.jobcreation.widgets.Jobname.REPLACEMENT_CHARACTERS,
										"_");
						jobnameTextField.setText(input.trim());
						jobnameTextField.setCaretPosition(index);

						setValue("jobname", jobnameTextField.getText());
					} catch (final TemplateException e1) {
						myLogger.error(e1);
					} finally {
						jobnameTextField.setCaretPosition(pos);
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
			final String newJobname = (String) e.getNewValue();
			getJobnameTextField().setText(newJobname);
		}

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
			throws TemplateException {

	}

	@Override
	void setInitialValue() throws TemplateException {

		final String defaultValue = getPanelProperty(DEFAULT_VALUE);
		if (StringUtils.isNotBlank(defaultValue)) {
			final String sugJobname = getUserEnvironmentManager()
					.calculateUniqueJobname(defaultValue);
			setValue("jobname", sugJobname);
		}

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

	}
}
