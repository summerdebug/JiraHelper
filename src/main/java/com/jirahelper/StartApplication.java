package com.jirahelper;

import com.jirahelper.ui.EnterPasswordWindow;

public class StartApplication {

    public static final String JIRA_URL = "https://provide_jira_url_here";
    public static final String LOGIN = "provide_jira_login_here";

    // Entry point to start the application.
    public static void main(String[] args) {
        EnterPasswordWindow.showWindow();
    }
}
