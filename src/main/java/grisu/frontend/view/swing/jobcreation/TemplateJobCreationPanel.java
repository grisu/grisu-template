package grisu.frontend.view.swing.jobcreation;

import grisu.control.ServiceInterface;
import grisu.control.exceptions.TemplateException;
import grisu.frontend.view.swing.jobcreation.templates.PanelConfig;
import grisu.frontend.view.swing.jobcreation.templates.TemplateHelpers;
import grisu.frontend.view.swing.jobcreation.templates.TemplateObject;
import grisu.frontend.view.swing.jobcreation.templates.inputPanels.AbstractInputPanel;
import grisu.model.GrisuRegistryManager;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;
import com.jgoodies.forms.layout.RowSpec;

public class TemplateJobCreationPanel extends JPanel implements
JobCreationPanel, PropertyChangeListener {

	static final Logger myLogger = Logger
			.getLogger(TemplateJobCreationPanel.class.getName());

	public static final String LOADING_PANEL = "loading";
	public static final String TEMPLATE_PANEL = "template";
	public static final String ERROR_PANEL = "error";

	public static String getStackTrace(Throwable t) {
		final StringWriter stringWritter = new StringWriter();
		final PrintWriter printWritter = new PrintWriter(stringWritter, true);
		t.printStackTrace(printWritter);
		printWritter.flush();
		stringWritter.flush();

		return stringWritter.toString();
	}

	private TemplateObject template;
	private final List<String> lines;
	private LinkedHashMap<String, PanelConfig> panelConfigs;
	private final CardLayout cardLayout = new CardLayout();
	private JPanel loadingPanel;
	private JProgressBar progressBar;
	private JLabel label;
	private JPanel currentTemplatePanel;
	private final String templateFileName;
	private JPanel errorPanel;
	private JScrollPane scrollPane;
	private JTextArea errorTextArea;

	private ServiceInterface si;

	public TemplateJobCreationPanel(String templateFileName, List<String> lines) {
		this.templateFileName = templateFileName;
		this.lines = lines;
		try {
			this.panelConfigs = TemplateHelpers.parseConfig(lines);
		} catch (final TemplateException e) {
			myLogger.error(e);
		}
		setLayout(cardLayout);
		add(getLoadingPanel(), LOADING_PANEL);
		add(getErrorPanel(), ERROR_PANEL);
	}

	@Override
	public boolean createsBatchJob() {
		return false;
	}

	@Override
	public boolean createsSingleJob() {
		return true;
	}

	private JPanel getErrorPanel() {
		if (errorPanel == null) {
			errorPanel = new JPanel();
			errorPanel.setLayout(new BorderLayout(0, 0));
			errorPanel.add(getScrollPane(), BorderLayout.CENTER);
		}
		return errorPanel;
	}

	private JTextArea getErrorTextArea() {
		if (errorTextArea == null) {
			errorTextArea = new JTextArea();
		}
		return errorTextArea;
	}

	private JLabel getLabel() {
		if (label == null) {
			label = new JLabel("Loading template...");
			label.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return label;
	}

	private JPanel getLoadingPanel() {
		if (loadingPanel == null) {
			loadingPanel = new JPanel();
			loadingPanel
			.setLayout(new FormLayout(new ColumnSpec[] {
					ColumnSpec.decode("24dlu"),
					ColumnSpec.decode("default:grow"),
					ColumnSpec.decode("24dlu"), }, new RowSpec[] {
					RowSpec.decode("4dlu:grow"),
					FormSpecs.DEFAULT_ROWSPEC,
					FormSpecs.RELATED_GAP_ROWSPEC,
					FormSpecs.DEFAULT_ROWSPEC,
					RowSpec.decode("4dlu:grow"), }));
			loadingPanel.add(getProgressBar(), "2, 2");
			loadingPanel.add(getLabel(), "2, 4, fill, default");
		}
		return loadingPanel;
	}

	@Override
	public JPanel getPanel() {
		return this;
	}

	@Override
	public String getPanelName() {

		if (template == null) {
			return "Template broken";
			// throw new IllegalStateException(
			// "No serviceinterface set yet. Can't determine panel name.");
		}
		return template.getTemplateName();
	}

	private JProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
			progressBar.setIndeterminate(true);
		}
		return progressBar;
	}

	private JScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new JScrollPane();
			scrollPane.setViewportView(getErrorTextArea());
		}
		return scrollPane;
	}

	@Override
	public String getSupportedApplication() {

		for (final PanelConfig config : panelConfigs.values()) {
			final String bean = config.getProperties().get(
					AbstractInputPanel.BEAN);
			if (AbstractInputPanel.APPLICATION.equals(bean)) {
				final String app = config.getProperties().get(
						AbstractInputPanel.DEFAULT_VALUE);
				if (StringUtils.isNotBlank(app)) {
					return app;
				} else {
					break;
				}
			}
		}
		return "generic";
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {

		// if (si != null) {
		// if ("localTemplateNames".equals(arg0.getPropertyName())) {
		// if (((List<String>) arg0.getNewValue())
		// .contains(templateFileName)) {
		// setServiceInterface(si);
		// }
		//
		// }
		// }
	}

	@Override
	public synchronized void setServiceInterface(ServiceInterface si) {
		
		if ( this.si == si ) {
			
			return;
			
		}

		this.si = si;

		try {
			if (currentTemplatePanel != null) {
				remove(currentTemplatePanel);
			}

			GrisuRegistryManager.getDefault(si).getTemplateManager()
			.addTemplateManagerListener(this);

			template = TemplateHelpers.parseAndCreateTemplatePanel(si,
					templateFileName, lines);
			currentTemplatePanel = new TemplateWrapperPanel(template);


			add(currentTemplatePanel, TEMPLATE_PANEL);
			// add(currentTemplatePanel, TEMPLATE_PANEL);
			cardLayout.show(this, TEMPLATE_PANEL);
		} catch (final Exception e) {
			myLogger.error(e);
			getErrorTextArea().setText(getStackTrace(e));
			cardLayout.show(this, ERROR_PANEL);
		}
	}
}
