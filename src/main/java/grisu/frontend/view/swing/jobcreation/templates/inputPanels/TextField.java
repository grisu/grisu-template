package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.job.JobDescription;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class TextField extends AbstractInputPanel {
	private JTextField textField;

	public TextField(String name, PanelConfig config) throws TemplateException {

		super(name, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormSpecs.RELATED_GAP_COLSPEC, FormSpecs.DEFAULT_COLSPEC,
				FormSpecs.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC, FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC, }));
		if (displayHelpLabel()) {
			add(getTextField(), "2, 2, fill, fill");
			add(getHelpLabel(), "4, 2");
		} else {
			add(getTextField(), "2, 2, 3, 1, fill, fill");
		}

	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();

		return defaultProperties;
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

						// if ( StringUtils.isBlank(bean) ) {
						// return;
						// }

						setValue(bean, textField.getText());
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
		return getTextField().getText();
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
			throws TemplateException {

		// if ( StringUtils.isBlank(bean) ) {
		// return;
		// }

	}

	@Override
	void setInitialValue() throws TemplateException {

		final String defaultValue = getPanelProperty(DEFAULT_VALUE);
		if (StringUtils.isNotBlank(defaultValue)) {
			getTextField().setText(defaultValue);
			setValue(bean, defaultValue);
		} else {
			setValue(bean, "");
		}
	}

	@Override
	protected void templateRefresh(JobDescription jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}
	}
}
