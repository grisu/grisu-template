package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.job.JobDescription;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.builtin.Validators;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class Email extends AbstractInputPanel {
	private JCheckBox startsCheckBox;
	private JCheckBox chckbxfinishes;
	private JTextField textField;
	private final Validator checkBoxValidator;

	public Email(String name, PanelConfig config) throws TemplateException {

		super(name, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, }));
		add(getStartsCheckBox(), "2, 2");
		add(getChckbxfinishes(), "4, 2");
		add(getTextField(), "2, 4, 3, 1, fill, default");

		checkBoxValidator = new Validator<String>() {

			public boolean validate(Problems arg0, String arg1, String arg2) {

				if (!getChckbxfinishes().isSelected()
						&& !getStartsCheckBox().isSelected()) {
					return true;
				} else {
					return Validators.EMAIL_ADDRESS.validate(arg0, arg1, arg2);
				}
			}
		};
		config.addValidator(checkBoxValidator);
	}

	private JCheckBox getChckbxfinishes() {
		if (chckbxfinishes == null) {
			chckbxfinishes = new JCheckBox("...finishes");
			chckbxfinishes.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {

					try {
						setValue("email_on_job_finish",
								chckbxfinishes.isSelected());
						if (getChckbxfinishes().isSelected()
								|| getStartsCheckBox().isSelected()) {
							getTextField().setText(getTextField().getText());
						} else {
							setValue("email_address", "");
						}
					} catch (final TemplateException e) {
						myLogger.error(e);
					}

				}
			});
		}
		return chckbxfinishes;
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();

		defaultProperties.put(TITLE, "Send email when job...");

		return defaultProperties;
	}

	private JCheckBox getStartsCheckBox() {
		if (startsCheckBox == null) {
			startsCheckBox = new JCheckBox("...starts");
			startsCheckBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {

					try {
						setValue("email_on_job_start",
								startsCheckBox.isSelected());
						if (getChckbxfinishes().isSelected()
								|| getStartsCheckBox().isSelected()) {
							getTextField().setText(getTextField().getText());
						} else {
							setValue("email_address", "");
						}
					} catch (final TemplateException e) {
						myLogger.error(e);
					}

				}
			});
		}
		return startsCheckBox;
	}

	@Override
	public JTextComponent getTextComponent() {
		return getTextField();
	}

	private JTextField getTextField() {
		if (textField == null) {
			textField = new JTextField();
			textField.setColumns(10);
			textField.addKeyListener(new KeyAdapter() {

				@Override
				public void keyReleased(KeyEvent e) {
					try {
						if (getChckbxfinishes().isSelected()
								|| getStartsCheckBox().isSelected()) {
							setValue("email_address", textField.getText());
						} else {
							setValue("email_address", "");
						}
					} catch (final TemplateException e1) {
						myLogger.error(e1);
					}
				}

			});
		}
		return textField;
	}

	@Override
	protected String getValueAsString() {
		return null;
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {
	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
			throws TemplateException {

	}

	@Override
	void setInitialValue() {

		try {
			setValue("email_on_job_finish", chckbxfinishes.isSelected());
		} catch (TemplateException e1) {
			myLogger.error("Can't set email_on_job_finish value", e1);
		}

		try {
			setValue("email_on_job_start", startsCheckBox.isSelected());
		} catch (TemplateException e1) {
			myLogger.error("Can't set email_on_job_start value", e1);
		}

		if (getChckbxfinishes().isSelected()
				|| getStartsCheckBox().isSelected()) {
			try {
				setValue("email_address", textField.getText());
			} catch (TemplateException e) {
				myLogger.error("Can't set email_address value", e);
			}
		} else {
			try {
				setValue("email_address", "");
			} catch (TemplateException e) {
				myLogger.error("Can't set email_address value", e);
			}
		}

	}

	@Override
	protected void templateRefresh(JobDescription jobObject) {
		System.out.println("TEMPLATE REFRESH");
	}
}
