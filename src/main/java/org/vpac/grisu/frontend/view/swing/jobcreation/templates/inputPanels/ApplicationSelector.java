package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

import org.apache.commons.lang.StringUtils;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import org.vpac.grisu.model.GrisuRegistryManager;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;

import au.org.arcs.jcommons.constants.Constants;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ApplicationSelector extends AbstractInputPanel {
	private JComboBox comboBox;
	private final DefaultComboBoxModel appModel = new DefaultComboBoxModel();

	public ApplicationSelector(String templateName, PanelConfig config)
			throws TemplateException {
		super(templateName, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(103dlu;default):grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getComboBox(), "2, 2, fill, default");
		// TODO Auto-generated constructor stub
	}

	private JComboBox getComboBox() {
		if (comboBox == null) {
			comboBox = new JComboBox(appModel);
			comboBox.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {

					if (ItemEvent.SELECTED == e.getStateChange()) {
						String app = (String) appModel.getSelectedItem();
						try {
							setValue("application", app);
						} catch (TemplateException e1) {
							e1.printStackTrace();
						}
					}

				}
			});
		}
		return comboBox;
	}

	@Override
	protected String getValueAsString() {
		return (String) getComboBox().getSelectedItem();
	}

	@Override
	protected void jobPropertyChanged(PropertyChangeEvent e) {

		// Thread.dumpStack();
		if (Constants.EXECUTABLE_KEY.equals(e.getPropertyName())) {
			String cmdln = (String) e.getNewValue();
			if (StringUtils.isBlank(cmdln)) {
				return;
			}
			String exe = JobSubmissionObjectImpl.extractExecutable(cmdln);
			String[] appPackages = GrisuRegistryManager
					.getDefault(getServiceInterface()).getResourceInformation()
					.getApplicationPackageForExecutable(exe);

			setApplicationPackages(appPackages);
			return;

		} else if (Constants.APPLICATIONNAME_KEY.equals(e.getPropertyName())) {

			String app = (String) e.getNewValue();
			if (StringUtils.isNotBlank(app)) {
				appModel.setSelectedItem(app);
			}
		}

	}

	@Override
	protected void preparePanel(Map<String, String> panelProperties)
			throws TemplateException {
		// TODO Auto-generated method stub

	}

	private void setApplicationPackages(String[] appPackages) {

		appModel.removeAllElements();

		if ((appPackages == null) || (appPackages.length == 0)) {
			appModel.addElement("n/a");
		} else {
			for (String app : appPackages) {
				appModel.addElement(app);
			}
		}
	}

	@Override
	void setInitialValue() throws TemplateException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {
		// TODO Auto-generated method stub

	}
}
