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

import javax.swing.JComboBox;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class Memory extends AbstractInputPanel {
	private JComboBox comboBox;

	private boolean userInput = true;

	private Long lastMemory = 1024L;

	public Memory(String name, PanelConfig config) throws TemplateException {

		super(name, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(24dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getComboBox(), "2, 2, fill, fill");
	}

	private JComboBox getComboBox() {
		if (comboBox == null) {
			comboBox = new JComboBox();
			comboBox.setEditable(true);

			comboBox.getEditor().getEditorComponent()
			.addKeyListener(new KeyAdapter() {

				@Override
				public void keyReleased(KeyEvent e) {

					try {

						Object o = getComboBox().getEditor().getItem();

						String currentValue = null;
						if (o instanceof String) {
							currentValue = (String) o;
						}
						else if(o instanceof Long){
							currentValue = ((Long) o).toString();
						}	 
						else {
							currentValue = ((Integer) o).toString();
						}

						if (StringUtils.isBlank(currentValue)) {
							getComboBox().getEditor().setItem("0");
							setValue("memory", 0);
							lastMemory = 0L;
							return;
						}
						Long memory = Long.parseLong(currentValue);
						lastMemory = memory;

					} catch (Exception ex) {
						getComboBox().getEditor().setItem(
								new String(lastMemory.toString()));
					}

				}
			});
			comboBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {

					if (!userInput) {
						return;
					}

					if (!isInitFinished()) {
						return;
					}

					try {
						if (ItemEvent.SELECTED == e.getStateChange()) {
							final Object v = getComboBox().getSelectedItem();
							Long value = -1L;
							if (v instanceof Long) {
								value = (Long) v;
							} else if (v instanceof String) {
								value = Long.parseLong((String) v);
							} else {
								myLogger.error("Can't parese memory combobox.");
								return;
							}

							try {
								setValue("memory", new Long(value * 1048576));
								lastMemory = value;
							} catch (final TemplateException e1) {
								myLogger.error(e1);
							}
						}
					} catch (Exception ex) {
						myLogger.debug(ex);
					}

				}
			});
		}
		return comboBox;
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();

		defaultProperties.put(TITLE, "Memory per core (in MB)");
		defaultProperties.put(DEFAULT_VALUE, "1024");
//		defaultProperties.put(PREFILLS, "1024,2048,4096,8192,16384");
		defaultProperties
				.put(PREFILLS,
						"1024,2048,4096,8192,16384,32768,65536,131072,262144,524288,1048576,2097152,4194304,8388608,16.77M,33.55M,67.10M,134.21M,268.43M,536.87M,1.07G,2.14G,4.29G,8.58G,17.17G,34.35G,68.71G,137.43G,274.87G,549.75G");

		return defaultProperties;
	}

	@Override
	protected String getValueAsString() {

		try {
			Long i = ((Long) (getComboBox().getSelectedItem()));
			i = i * 1048576;
			final String result = i.toString();
			return result;
		} catch (final Exception e) {
			myLogger.debug("Can't get value for panel " + getPanelName() + ": "
					+ e.getLocalizedMessage());
			return null;
		}

	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

		userInput = false;

		if ("memory".equals(e.getPropertyName())) {
			Long value = (Long) e.getNewValue();
			value = value / 1048576;
			getComboBox().setSelectedItem(value);
		}

		userInput = true;
	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties) {

		getComboBox().removeAllItems();

		for (final String key : panelProperties.keySet()) {
			try {
				if (PREFILLS.equals(key)) {
					userInput = false;
					for (final String item : panelProperties.get(PREFILLS)
							.split(",")) {
						getComboBox().addItem(Long.parseLong(item));
					}
					userInput = true;
				} else if (IS_EDITABLE.equalsIgnoreCase(key)) {
					getComboBox().setEditable(
							Boolean.parseBoolean(panelProperties.get(key)));
				}
			} catch (final Exception e) {
				myLogger.error(e);
			}
		}

	}

	@Override
	void setInitialValue() {

		final String def = getDefaultValue();
		if (StringUtils.isNotBlank(def)) {
			try {
				setValue("memory", Long.parseLong(def));
			} catch (final Exception e) {
				myLogger.error(e);
			}
		}

	}

	@Override
	protected void templateRefresh(JobDescription jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}

	}
}
