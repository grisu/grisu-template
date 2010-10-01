package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang.StringUtils;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class TextField extends AbstractInputPanel {
	private JTextField textField;

	public TextField(String name, PanelConfig config) throws TemplateException {

		super(name, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
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
						// TODO Auto-generated catch block
						e1.printStackTrace();
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
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}
	}
}
