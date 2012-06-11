package grisu.frontend.view.swing.jobcreation.templates;

import grisu.frontend.control.login.LoginManager;
import grisu.frontend.model.events.ApplicationEventListener;
import grisu.frontend.view.swing.WindowSaver;
import grisu.frontend.view.swing.login.LoginPanel;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

public class TemplateCreator {

	static final Logger myLogger = Logger.getLogger(TemplateCreator.class
			.getName());

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					final TemplateCreator window = new TemplateCreator();
					window.frame.setVisible(true);
				} catch (final Exception e) {
					myLogger.error(e);
				}
			}
		});
	}

	private JFrame frame;

	/**
	 * Create the application.
	 */
	public TemplateCreator() {

		LoginManager.initEnvironment();

		new ApplicationEventListener();

		final Toolkit tk = Toolkit.getDefaultToolkit();
		tk.addAWTEventListener(WindowSaver.getInstance(),
				AWTEvent.WINDOW_EVENT_MASK);

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (final Exception e) {
			myLogger.error(e);
		}

		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final TemplateTestFrame templateframe = new TemplateTestFrame();

		final LoginPanel lp = new LoginPanel(templateframe, null);
		frame.getContentPane().add(lp, BorderLayout.CENTER);
	}

}
