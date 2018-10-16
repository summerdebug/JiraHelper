package com.jirahelper.ui;

import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.jirahelper.StartApplication;
import com.jirahelper.service.JiraQueryExecutor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

/**
 * Windows containing text box for entering JiraQueryExecutor password and OK button to run {@link
 * JiraQueryExecutor#execute(JiraRestClient)} method.
 */
public class EnterPasswordWindow {

    private static final int ENTER_KEY_CODE = 10;
    private static final String TITLE = "JiraHelper";
    private static final int WIDTH = 300;
    private static final int HEIGHT = 100;
    private static JTextField passwordField;
    private static JButton okButton;

    public static void showWindow() {
        JFrame window = new JFrame(TITLE);
        window.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        buildWindowContent(window);
        positionWindowOnScreen(window);
        displayWindow(window);
    }

    private static void buildWindowContent(JFrame window) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(createPasswordPanel());
        panel.add(createOkButton());

        window.getContentPane().add(panel);
    }

    private static JPanel createPasswordPanel() {
        JPanel panel = new JPanel();

        JLabel label = new JLabel("password");
        passwordField = new JPasswordField(15);

        passwordField.addKeyListener(new EnterKeyHandler());

        panel.add(label);
        panel.add(passwordField);

        return panel;
    }

    private static JButton createOkButton() {
        okButton = new JButton("OK");
        okButton.addActionListener(new OkActionListener());
        return okButton;
    }

    private static void positionWindowOnScreen(JFrame window) {
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation(dim.width / 2 - window.getSize().width / 2, dim.height / 2 - window.getSize().height / 2);
    }

    private static void displayWindow(JFrame window) {
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.pack();
        window.setVisible(true);
    }

    private static class EnterKeyHandler implements KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == ENTER_KEY_CODE) {
                okButton.doClick();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }
    }

    private static class OkActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            JiraRestClient restClient = getRestClient();

            if (restClient != null) {
                JiraQueryExecutor.execute(restClient);
            } else {
                System.out.println("Error. restClient is null.");
            }

            System.exit(0);
        }

        private JiraRestClient getRestClient() {
            System.out.println("Getting restClient.");
            JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
            try {
                return factory.createWithBasicHttpAuthentication(
                        new URI(StartApplication.JIRA_URL), StartApplication.LOGIN, passwordField.getText());
            } catch (URISyntaxException e) {
                System.out.println(e.getMessage());
                return null;
            }
        }
    }
}
