package org.vpac.grisu.frontend.view.swing.jobcreation.templates.inputPanels.helperPanels;

import java.awt.BorderLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class QueueInfoPanel extends JPanel {
	private JScrollPane scrollPane;
	private JTextArea txtrNa;

	/**
	 * Create the panel.
	 */
	public QueueInfoPanel() {
		setLayout(new BorderLayout(0, 0));
		add(getScrollPane(), BorderLayout.CENTER);

	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getTxtrNa());
		}
		return scrollPane;
	}

	private JTextArea getTxtrNa() {
		if (txtrNa == null) {
			txtrNa = new JTextArea();
			txtrNa.setText("n/a");
		}
		return txtrNa;
	}

	public void setLoading(boolean loading) {

		// if (loading) {
		// SwingUtilities.invokeLater(new Thread() {
		//
		// @Override
		// public void run() {
		// // getTxtrNa().setText("n/a");
		// }
		//
		// });
		// }
		getTxtrNa().setEnabled(!loading);

	}
}
