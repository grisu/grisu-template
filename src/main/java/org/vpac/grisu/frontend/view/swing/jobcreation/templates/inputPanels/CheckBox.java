package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;

public class CheckBox extends AbstractInputPanel {

	public static final String CHECKED_VALUE = "checkedValue";
	public static final String UNCHECKED_VALUE = "uncheckedValue";
	private JCheckBox checkBox;
	private JLabel label;

	public CheckBox(String templateName, PanelConfig config)
			throws TemplateException {
		super(templateName, config);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(getCheckBox());
		if (displayHelpLabel()) {
			add(getHelpLabel());
		}

	}

	private JCheckBox getCheckBox() {
		if (checkBox == null) {
			checkBox = new JCheckBox(getPanelProperty(LABEL));
			checkBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent arg0) {

					if (!isInitFinished()) {
						return;
					}

					try {
						setValue(bean, getValueAsString());
					} catch (TemplateException e) {
						e.printStackTrace();
					}

				}
			});
		}
		return checkBox;
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		Map<String, String> defaultProperties = new HashMap<String, String>();
		defaultProperties.put(LABEL, null);
		defaultProperties.put(DEFAULT_VALUE, "false");
		defaultProperties.put(CHECKED_VALUE, "true");
		defaultProperties.put(UNCHECKED_VALUE, "false");
		return defaultProperties;

	}

	@Override
	protected String getValueAsString() {

		if (getCheckBox().isSelected()) {
			return getPanelProperty(CHECKED_VALUE);
		} else {
			return getPanelProperty(UNCHECKED_VALUE);
		}
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
			throws TemplateException {

	}

	@Override
	void setInitialValue() throws TemplateException {

		boolean checked = false;
		try {
			if (StringUtils.isNotBlank(getDefaultValue())) {
				if (getDefaultValue().equals(getPanelProperty(CHECKED_VALUE))) {
					getCheckBox().setSelected(true);
				} else if (getDefaultValue().equals(
						getPanelProperty(UNCHECKED_VALUE))) {
					getCheckBox().setSelected(false);
				}
			}
		} catch (Exception e) {
			throw new TemplateException("Can't parse initial checkbox value: "
					+ e.getLocalizedMessage());
		}

		getCheckBox().setSelected(checked);

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}

	}
}
