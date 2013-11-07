package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.jcommons.utils.WalltimeUtils;
import grisu.model.job.JobDescription;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class WalltimeVertical extends AbstractInputPanel {

	private JComboBox amountComboBox;

	private JComboBox unitComboBox;
	private final DefaultComboBoxModel amountModel = new DefaultComboBoxModel();

	private final DefaultComboBoxModel unitModel = new DefaultComboBoxModel();

	public WalltimeVertical(String name, PanelConfig config) throws TemplateException {

		super(name, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormSpecs.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("55dlu"),
				FormSpecs.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,
				FormSpecs.DEFAULT_ROWSPEC,
				FormSpecs.RELATED_GAP_ROWSPEC,}));
		add(getAmountComboBox(), "2, 2, fill, fill");
		add(getUnitComboBox(), "2, 4, fill, fill");
	}

	private JComboBox getAmountComboBox() {
		if (amountComboBox == null) {
			amountComboBox = new JComboBox();
			amountComboBox.setEditable(true);
			amountComboBox.setModel(amountModel);
			// amountComboBox.setSelectedIndex(0);
			amountComboBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {

					final String amount = (String) (getAmountComboBox()
							.getSelectedItem());
					final String unit = (String) (getUnitComboBox()
							.getSelectedItem());

					if (StringUtils.isBlank(amount)
							|| StringUtils.isBlank(unit)) {
						return;
					}

					int walltimeInSeconds = -1;
					try {
						walltimeInSeconds = WalltimeUtils
								.convertHumanReadableStringIntoSeconds(new String[] {
										amount, unit });
					} catch (final Exception e1) {
						myLogger.debug("Can't parse " + amount + ",  " + unit
								+ ": " + e1.getLocalizedMessage());
						return;
					}

					try {
						setValue("walltimeInSeconds", walltimeInSeconds);
					} catch (final TemplateException e1) {
						myLogger.error(e1);
					}
				}
			});
		}
		return amountComboBox;
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();
		defaultProperties.put(TITLE, "Walltime");
		defaultProperties.put("defaultAmountList",
				"1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,30,45");
		defaultProperties.put("defaultAmount", "1");
		defaultProperties.put("defaultUnitList", "minutes,hours,days,weeks");
		defaultProperties.put("defaultUnit", "hours");

		return defaultProperties;
	}

	private JComboBox getUnitComboBox() {
		if (unitComboBox == null) {
			unitComboBox = new JComboBox();
			unitComboBox.setModel(unitModel);
			// unitComboBox.setSelectedIndex(1);
			unitComboBox.addItemListener(new ItemListener() {

				public void itemStateChanged(ItemEvent e) {

					final int walltimeInSeconds = WalltimeUtils
							.convertHumanReadableStringIntoSeconds(new String[] {
									(String) (getAmountComboBox()
											.getSelectedItem()),
											(String) (getUnitComboBox()
													.getSelectedItem()) });
					try {
						setValue("walltimeInSeconds", walltimeInSeconds);
					} catch (final TemplateException e1) {
						myLogger.error(e1);
					}
				}
			});
		}
		return unitComboBox;
	}

	@Override
	protected String getValueAsString() {
		final String amount = (String) getAmountComboBox().getSelectedItem();
		final String unit = (String) getUnitComboBox().getSelectedItem();
		try {
			final Integer secs = WalltimeUtils
					.convertHumanReadableStringIntoSeconds(new String[] {
							amount, unit });
			return secs.toString();
		} catch (final Exception e) {
			return null;
		}

	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

		if ("walltimeInSeconds".equals(e.getPropertyName())) {

			final String[] humanReadable = WalltimeUtils
					.convertSecondsInHumanReadableString((Integer) (e
							.getNewValue()));
			amountModel.setSelectedItem(humanReadable[0]);
			unitModel.setSelectedItem(humanReadable[1]);
		}

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties) {

		final String[] amounts = panelProperties.get("defaultAmountList")
				.split(",");
		amountModel.removeAllElements();
		for (final String amount : amounts) {
			try {
				final Integer a = Integer.parseInt(amount);
				amountModel.addElement(amount);
			} catch (final Exception e) {
				myLogger.error("Can't add amount " + amount
						+ " to WalltimePanel: " + e.getLocalizedMessage());
			}
		}

		unitModel.removeAllElements();
		final String[] units = panelProperties.get("defaultUnitList")
				.split(",");
		for (final String unit : units) {
			if ("minutes,hours,days,weeks".indexOf(unit) >= 0) {
				unitModel.addElement(unit);
			} else {
				myLogger.error("Can't add unit " + unit
						+ " to WalltimePanel. Not a valid unitname.");
			}
		}

	}

	@Override
	void setInitialValue() throws TemplateException {

		String defaultAmount = null;
		String defaultUnit = null;
		if (useHistory()) {
			final String defValue = getDefaultValue();

			try {
				final String[] humanreadable = WalltimeUtils
						.convertSecondsInHumanReadableString(Integer
								.parseInt(defValue));
				if ((humanreadable != null) && (humanreadable.length == 2)) {
					defaultAmount = humanreadable[0];
					defaultUnit = humanreadable[1];
				}
			} catch (final Exception e) {
				myLogger.debug("Can't parse history value for walltime: "
						+ e.getLocalizedMessage());
			}

		}

		if (StringUtils.isBlank(defaultAmount)) {
			defaultAmount = getPanelProperty("defaultAmount");
		}
		if (StringUtils.isNotBlank(defaultAmount)) {
			try {
				final Integer a = Integer.parseInt(defaultAmount);
				amountModel.setSelectedItem(defaultAmount);
			} catch (final Exception e) {
				myLogger.error("Can't set amount " + defaultAmount
						+ " as default to WalltimePanel: "
						+ e.getLocalizedMessage());
			}
		}

		if (StringUtils.isBlank(defaultUnit)) {
			defaultUnit = getPanelProperty("defaultUnit");
		}

		if (StringUtils.isNotBlank(defaultUnit)
				&& ("minutes,hours,days,weeks".indexOf(defaultUnit) >= 0)) {
			unitModel.setSelectedItem(defaultUnit);
		} else {
			myLogger.error("Can't set unit " + defaultUnit
					+ " as default to WalltimePanel. Not a valid unitname.");
		}

	}

	@Override
	protected void templateRefresh(JobDescription jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}
	}
}
