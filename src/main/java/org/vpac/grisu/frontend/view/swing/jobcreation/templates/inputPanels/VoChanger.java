package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

import org.vpac.grisu.control.ServiceInterface;
import org.vpac.grisu.control.exceptions.TemplateException;
import org.vpac.grisu.frontend.view.swing.DefaultFqanChangePanel;
import org.vpac.grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import org.vpac.grisu.model.job.JobSubmissionObjectImpl;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class VoChanger extends AbstractInputPanel {

	private DefaultFqanChangePanel fqanChangePanel = null;
	private JLabel label;

	public VoChanger(String templateName, PanelConfig config)
			throws TemplateException {
		super(templateName, config);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC, }));
	}

	public DefaultFqanChangePanel getFqanChangePanel() {

		if (fqanChangePanel == null) {
			fqanChangePanel = new DefaultFqanChangePanel();
			try {
				fqanChangePanel.setServiceInterface(getServiceInterface());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return fqanChangePanel;
	}

	@Override
	protected Map<String, String> getDefaultPanelProperties() {

		Map<String, String> defaultProperties = new HashMap<String, String>();

		return defaultProperties;

	}

	@Override
	public void setServiceInterface(ServiceInterface si) {
		super.setServiceInterface(si);
		add(getFqanChangePanel(), "2, 2");

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
	void setInitialValue() throws TemplateException {

	}

	@Override
	protected void templateRefresh(JobSubmissionObjectImpl jobObject) {

	}

}
