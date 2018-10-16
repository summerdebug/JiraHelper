package com.jirahelper.service;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class JiraQueryExecutor {

    /**
     * Execute queries against Jira. Uncomment any invocation and provide actual values to try it. Check methods java
     * docs for details. Output is printed to {@link System#out}.
     */
    public static void execute(JiraRestClient client) {
        System.out.println("Start querying Jira.");

        // printTicket(client, "PROJECT-1234");

        // printTicketsDistributionByAssignees(client, "Put initial JQL here.");

        // saveTicketsToFile(client, "Put initial JQL here.", "/data/tickets.txt");

        // printCountOfMatchingTickets(client, "Put initial JQL here.");
    }

    /**
     * Prints information about the ticket.
     */
    static void printTicket(JiraRestClient client, String ticket) {
        System.out.println("Requesting issue: " + ticket);
        IssueRestClient issueClient = client.getIssueClient();
        Promise<Issue> issuePromise = issueClient.getIssue(ticket);
        waitResponse(issuePromise);
        System.out.println("Response received.");

        try {
            System.out.println("Issue: " + issuePromise.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows distribution of the matching ticket among team members.
     */
    static void printTicketsDistributionByAssignees(JiraRestClient client, String jql) {
        List<String> tickets = findTickets(client, jql);
        Map<String, List<String>> userTickets = getDeveloperTicketsMap(client, tickets);
        printDeveloperTicketsMap(userTickets);
    }

    private static Map<String, List<String>> getDeveloperTicketsMap(JiraRestClient client, List<String> tickets) {
        Map<String, List<String>> developerTickets = new HashMap<>();
        int i = 0;
        for (String ticket : tickets) {
            Issue issue = getTicket(client, ticket);
            BasicUser assignee = issue.getAssignee();

            String displayName = assignee != null ? assignee.getDisplayName() : "Unassigned";
            if (!developerTickets.containsKey(displayName)) {
                developerTickets.put(displayName, new ArrayList<>());
            }
            developerTickets.get(displayName).add(ticket);
            System.out.println("Tickets left: " + (tickets.size() - i++));
        }
        return developerTickets;
    }

    private static void printDeveloperTicketsMap(Map<String, List<String>> developerTicketsMap) {
        for (String developer : developerTicketsMap.keySet()) {
            List<String> tickets = developerTicketsMap.get(developer);
            if (tickets == null) {
                tickets = new ArrayList<>();
            }

            System.out.println(developer + ": " + tickets.size() + (tickets.size() > 0 ? " tickets: " : " No tickets"));
            for (String ticket : tickets) {
                System.out.print(ticket + ", ");
            }
        }
    }

    /**
     * Store tickets information to a file.
     */
    static void saveTicketsToFile(JiraRestClient client, String jql, String fileName) {
        try {
            createFile(fileName);
            FileWriter writer = new FileWriter(fileName);

            List<String> tickets = findTickets(client, jql);

            int counter = 1;
            for (String ticket : tickets) {
                Issue issue = getTicket(client, ticket);
                printTicket(writer, counter++, ticket, issue);
            }

            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createFile(String fileName) throws IOException {
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
        file.createNewFile();
    }

    private static List<String> findTickets(JiraRestClient client, String jql) {
        return findTickets(client, jql, 100);
    }

    private static List<String> findTickets(JiraRestClient client, String jql, int max) {
        System.out.println("Start search. jql: " + jql);
        SearchRestClient searchClient = client.getSearchClient();
        Promise<SearchResult> searchResult = searchClient.searchJql(jql, /*max results*/max, /*start index*/0);
        waitResponse(searchResult);

        List<String> tickets = new ArrayList<>();
        try {
            SearchResult resultReceived = searchResult.get();
            Iterable<BasicIssue> issues = resultReceived.getIssues();
            for (BasicIssue issue : issues) {
                tickets.add(issue.getKey());
            }
            System.out.println("issueList size: " + tickets.size());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return tickets;
    }

    private static void waitResponse(Promise<?> issuePromise) {
        System.out.print("Waiting response from server.");
        while (!issuePromise.isDone()) {
            try {
                Thread.sleep(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.print(".");
        }
        System.out.println();
    }

    private static Issue getTicket(JiraRestClient client, String ticket) {
        System.out.println("Requesting issue: " + ticket);
        IssueRestClient issueClient = client.getIssueClient();
        Promise<Issue> issuePromise = issueClient.getIssue(ticket);
        waitResponse(issuePromise);
        System.out.println("Response received.");

        Issue result = null;
        try {
             result = issuePromise.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static void printTicket(FileWriter writer, int counter, String ticket, Issue issue) throws IOException {
        String description = issue.getDescription();
        String assignee = issue.getAssignee().getDisplayName();
        String status = issue.getStatus().getName();

        writer.write("\n___________________________________________________________");
        writer.write("\nTicket-" + counter + ": " + ticket);
        writer.write("\nStatus: " + status);
        writer.write("\nAssignee: " + assignee);
        writer.write("\nDescription:\n" + description);
    }

    /**
     * Prints count of tickets, matching the filter.
     */
    static void printCountOfMatchingTickets(JiraRestClient client, String jql) {
        System.out.println("Start search. jql: " + jql);
        SearchRestClient searchClient = client.getSearchClient();
        Promise<SearchResult> searchResult = searchClient.searchJql(jql);
        waitResponse(searchResult);

        try {
            System.out.println("Found issues count: " + searchResult.get().getTotal());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }
}
