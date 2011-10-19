package grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.model.job.JobSubmissionObjectImpl;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang.StringUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class Spinner extends AbstractInputPanel {
	private static final String MAX_VALUE = "max";

	private static final String MIN_VALUE = "min";
	private static final String STEP_VALUE = "step";

	private JSpinner spinner;

	private final SpinnerListModel model = new SpinnerListModel();

	public Spinner(String templateName, PanelConfig config)
			throws TemplateException {
		super(templateName, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getSpinner(), "2, 2, fill, top");
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		final Map<String, String> defaultProperties = new HashMap<String, String>();
		return defaultProperties;

	}

	private JSpinner getSpinner() {
		if (spinner == null) {
			spinner = new JSpinner(model);
			spinner.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					String value = (String) spinner.getModel().getValue();
					try {
						setValue(bean, value);
					} catch (TemplateException e1) {
						myLogger.error(e1);
					}
				}
			});
		}
		return spinner;
	}

	@Override
	protected String getValueAsString() {
		return (String) getSpinner().getValue();
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
			throws TemplateException {

		Set<String> modelList = null;
		modelList = new LinkedHashSet<String>();

		final String prefills = panelProperties.get(PREFILLS);
		if (StringUtils.isNotBlank(prefills)) {

			for (final String value : prefills.split(",")) {
				modelList.add(value);
			}
		} else {

			String max = getPanelProperty(MAX_VALUE);
			if (StringUtils.isBlank(max)) {
				max = "100";
			}
			String min = getPanelProperty(MIN_VALUE);
			if (StringUtils.isBlank(min)) {
				min = "0";
			}
			String step = getPanelProperty(STEP_VALUE);
			if (StringUtils.isBlank(step)) {
				step = "1";
			}

			int maxI;
			try {
				maxI = Integer.parseInt(max);
			} catch (final NumberFormatException e) {
				throw new TemplateException(e);
			}
			int minI;
			try {
				minI = Integer.parseInt(min);
			} catch (final NumberFormatException e) {
				throw new TemplateException(e);
			}
			int stepI;
			try {
				stepI = Integer.parseInt(step);
			} catch (final NumberFormatException e) {
				throw new TemplateException(e);
			}

			for (int i = minI; i <= maxI; i = i + stepI) {
				modelList.add(new Integer(i).toString());
			}

		}

		if (useHistory()) {
			for (final String value : getHistoryValues()) {
				if (StringUtils.isNotBlank(value)) {
					modelList.add(value);
				}
			}
		}

		model.setList(new LinkedList<String>(modelList));

		// boolean isEditable = true;
		// try {
		// if (panelProperties.get(IS_EDITABLE) != null) {
		// isEditable = Boolean.parseBoolean(panelProperties
		// .get(IS_EDITABLE));
		// }
		// } catch (Exception e) {
		// throw new TemplateException("Can't parse \"editable\" value: "
		// + panelProperties.get(IS_EDITABLE));
		// }

	}

	@Override
	void setInitialValue() throws TemplateException {
		final String value = getDefaultValue();
		if (StringUtils.isNotBlank(value) && !"empty".equals(value)) {
			getSpinner().setValue(value);
		}
	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

		if (useHistory()) {
			addValueToHistory();
		}
	}
}
