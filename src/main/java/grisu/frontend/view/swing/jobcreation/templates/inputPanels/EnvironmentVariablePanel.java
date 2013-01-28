package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.job.JobDescription;
import grisu.utils.StringHelpers;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JScrollPane;

import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class EnvironmentVariablePanel extends AbstractInputPanel {



	private JComboBox keyCombo;
	private JComboBox valueCombo;
	private JScrollPane scrollPane;
	private JButton btnAdd;
	private JList list;

	private DefaultComboBoxModel keyModel = new DefaultComboBoxModel();
	private DefaultComboBoxModel valueModel = new DefaultComboBoxModel();

	private DefaultListModel envModel = new DefaultListModel();
	private JButton btnRemove;

	public EnvironmentVariablePanel(String templateName, PanelConfig config)
			throws TemplateException {
		super(templateName, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(54dlu;default):grow"),
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getKeyCombo(), "2, 2, fill, default");
		add(getValueCombo(), "4, 2, fill, default");
		add(getBtnAdd(), "6, 2");
		add(getScrollPane(), "2, 4, 3, 1, fill, fill");
		add(getBtnRemove(), "6, 4, default, top");
	}

	private void addVar(String key, String value) throws TemplateException {

		if (StringUtils.isBlank(key)) {
			return;
		}
		if (StringUtils.isBlank(value)) {
			removeVar(key);
			return;
		}
		String item = key + "=" + value;

		int index = -1;
		for (int i=0; i<envModel.getSize(); i++) {
			if (((String) envModel.get(i)).startsWith(key + "=")) {
				index = i;
				break;
			}
		}

		if ( index >= 0 ) {
			envModel.remove(index);
			envModel.add(index, item);
		} else {
			envModel.addElement(item);
		}
		addHistoryValue(item);
		setValue("environmentVariables", getValue());
	}

	private JButton getBtnAdd() {
		if (btnAdd == null) {
			btnAdd = new JButton("Add");
			btnAdd.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					String key = (String) getKeyCombo().getSelectedItem();
					if (StringUtils.isBlank(key)) {
						myLogger.debug("No key - nothing added");
						return;
					}

					String value = (String) getValueCombo().getSelectedItem();
					if (StringUtils.isBlank(value)) {
						try {
							removeVar(key);
						} catch (TemplateException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} else {
						try {
							addVar(key, value);
						} catch (TemplateException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

				}
			});
		}
		return btnAdd;
	}

	private JButton getBtnRemove() {
		if (btnRemove == null) {
			btnRemove = new JButton("Remove");
			btnRemove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {

					while (getList().getSelectedIndices().length > 0) {
						int[] sel = getList().getSelectedIndices();
						envModel.remove(sel[0]);
					}


				}
			});
		}
		return btnRemove;
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();
		defaultProperties.put(TITLE, "Environment Variables");
		defaultProperties.put(NAME, "env_vars");
		defaultProperties.put(HISTORY_ITEMS, "50");
		return defaultProperties;
	}

	public Map<String, String> getHistoryEnvVars() {

		List<String> hist = getHistoryValues();
		Map<String, String> result = Maps.newTreeMap();
		for (String all : hist) {
			int index = all.indexOf("=");
			String key = all.substring(0, index);
			String value = all.substring(index + 1);
			result.put(key, value);
		}

		return result;

	}

	private JComboBox getKeyCombo() {
		if (keyCombo == null) {
			keyCombo = new JComboBox(keyModel);
			keyCombo.setEditable(true);
		}
		return keyCombo;
	}

	private JList getList() {
		if (list == null) {
			list = new JList(envModel);
		}
		return list;
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getList());
		}
		return scrollPane;
	}

	private Map<String, String> getValue() {

		Map<String, String> items = Maps.newLinkedHashMap();
		for (int i = 0; i < envModel.size(); i++) {
			String item = (String) (envModel.getElementAt(i));
			int index = item.indexOf("=");
			String key = item.substring(0, index);
			String value = item.substring(index + 1);
			items.put(key, value);
		}
		return items;

	}

	@Override
	protected String getValueAsString() {

		return StringHelpers.mapToString(getValue());

	}

	private JComboBox getValueCombo() {
		if (valueCombo == null) {
			valueCombo = new JComboBox(valueModel);
			valueCombo.setEditable(true);
		}
		return valueCombo;
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

		if ("environmentVariables".equals(e.getPropertyName())) {
			Map<String, String> vars = (Map<String, String>) e.getNewValue();
			myLogger.debug("New env variables: "
					+
					StringHelpers.mapToString(vars));
			// envModel.removeAllElements();
			// for (String key : vars.keySet()) {
			// addValue(key, vars.get(key));
			// }
		}

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
			throws TemplateException {

		keyModel.removeAllElements();
		valueModel.removeAllElements();

		keyModel.addElement("");
		valueModel.addElement("");

		Map<String, String> all = getHistoryEnvVars();
		for (String key : all.keySet()) {
			keyModel.addElement(key);
		}
		for (String value : Sets.newTreeSet(all.values())) {
			valueModel.addElement(value);
		}

	}

	private void removeVar(String key) throws TemplateException {
		if (StringUtils.isBlank(key)) {
			return;
		}

		int index = -1;
		for (int i=0; i<envModel.getSize(); i++) {
			if (((String) envModel.get(i)).startsWith(key + "=")) {
				index = i;
				break;
			}
		}

		if ( index >= 0 ) {
			envModel.remove(index);
		}

		setValue("environmentVariables", getValue());

	}

	@Override
	void setInitialValue() throws TemplateException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void templateRefresh(JobDescription jobObject) {
		// TODO Auto-generated method stub

	}
}
