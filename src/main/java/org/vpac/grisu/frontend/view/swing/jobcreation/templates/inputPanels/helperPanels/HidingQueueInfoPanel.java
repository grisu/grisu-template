package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels.helperPanels;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class HidingQueueInfoPanel extends JPanel {
	private JCheckBox chckbxShowQueueInformation;
	private QueueInfoPanel queueInfoPanel;

	/**
	 * Create the panel.
	 */
	public HidingQueueInfoPanel() {
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC, }));
		add(getChckbxShowQueueInformation(), "2, 2");
		add(getQueueInfoPanel(), "2, 4, fill, fill");

	}

	private JCheckBox getChckbxShowQueueInformation() {
		if (chckbxShowQueueInformation == null) {
			chckbxShowQueueInformation = new JCheckBox("Show queue information");
			chckbxShowQueueInformation.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {

					if (chckbxShowQueueInformation.isSelected()) {
						getQueueInfoPanel().setVisible(true);
					} else {
						getQueueInfoPanel().setVisible(false);
					}

				}
			});
		}
		return chckbxShowQueueInformation;
	}

	private QueueInfoPanel getQueueInfoPanel() {
		if (queueInfoPanel == null) {
			queueInfoPanel = new QueueInfoPanel();
			queueInfoPanel.setVisible(false);
		}
		return queueInfoPanel;
	}

	public void setLoading(boolean loading) {

		getChckbxShowQueueInformation().setEnabled(!loading);
		getQueueInfoPanel().setLoading(loading);

	}
}
