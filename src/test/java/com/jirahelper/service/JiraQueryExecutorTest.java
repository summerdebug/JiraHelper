package com.jirahelper.service;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.jira.rest.client.IssueRestClient;
import com.atlassian.jira.rest.client.JiraRestClient;
import com.atlassian.jira.rest.client.SearchRestClient;
import com.atlassian.jira.rest.client.domain.BasicIssue;
import com.atlassian.jira.rest.client.domain.BasicStatus;
import com.atlassian.jira.rest.client.domain.BasicUser;
import com.atlassian.jira.rest.client.domain.Issue;
import com.atlassian.jira.rest.client.domain.SearchResult;
import com.atlassian.util.concurrent.Promise;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JiraQueryExecutorTest {

    private static final String TEST = "test";
    private static final String START = "Start search. jql: test";
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();
    @Mock
    private JiraRestClient jiraRestClient;
    @Mock
    private PrintStream printStream;
    @Mock
    private IssueRestClient issueRestClient;
    @Mock
    private Promise<Issue> issuePromise;
    @Mock
    private Issue issue;
    @Mock
    private SearchRestClient searchRestClient;
    @Mock
    private BasicUser basicUser;
    @Mock
    private Promise<SearchResult> searchResult;
    @Mock
    private SearchResult searchResultValue;
    @Mock
    private BasicIssue basicIssue;
    @Mock
    private BasicStatus basicStatus;

    @Test
    public void givenExecutorWhenPrintTicketThenSuccessful() throws Exception {
        // Arrange
        System.setOut(printStream);
        when(jiraRestClient.getIssueClient()).thenReturn(issueRestClient);
        when(issueRestClient.getIssue(TEST)).thenReturn(issuePromise);
        when(issuePromise.isDone()).thenReturn(true);
        when(issuePromise.get()).thenReturn(issue);

        // Act
        JiraQueryExecutor.printTicket(jiraRestClient, TEST);

        // Assert
        assertTrue(true);
        verify(printStream).println("Requesting issue: test");
        verify(printStream).println("Response received.");
        verify(printStream).println("Issue: issue");
    }

    @Test
    public void givenExecutorWhenPrintTicketsDistributionByAssigneesThenSuccessful()
            throws ExecutionException, InterruptedException {
        // Arrange
        System.setOut(printStream);
        when(jiraRestClient.getSearchClient()).thenReturn(searchRestClient);
        when(searchRestClient.searchJql(TEST, 100, 0)).thenReturn(searchResult);
        when(searchResult.isDone()).thenReturn(true);
        when(searchResult.get()).thenReturn(searchResultValue);
        List<BasicIssue> issues = new ArrayList<>();
        issues.add(basicIssue);
        when(searchResultValue.getIssues()).thenReturn(issues);
        when(basicIssue.getKey()).thenReturn(TEST);
        when(jiraRestClient.getIssueClient()).thenReturn(issueRestClient);
        when(issueRestClient.getIssue(TEST)).thenReturn(issuePromise);
        when(issuePromise.isDone()).thenReturn(true);
        when(issuePromise.get()).thenReturn(issue);
        when(issue.getAssignee()).thenReturn(basicUser);
        when(basicUser.getDisplayName()).thenReturn(TEST);

        // Act
        JiraQueryExecutor.printTicketsDistributionByAssignees(jiraRestClient, TEST);

        // Assert
        verify(printStream).println(START);
        verify(printStream).println("issueList size: 1");
        verify(printStream).println("Requesting issue: test");
        verify(printStream).println("Response received.");
        verify(printStream).println("Tickets left: 1");
    }

    @Test
    public void givenExecutorWhenSaveTicketsToFileThenSuccessful() throws Exception {
        // Arrange
        System.setOut(printStream);
        temporaryFolder.create();
        File file = temporaryFolder.newFile();
        when(jiraRestClient.getSearchClient()).thenReturn(searchRestClient);
        when(searchRestClient.searchJql(TEST, 100, 0)).thenReturn(searchResult);
        when(searchResult.isDone()).thenReturn(true);
        when(searchResult.get()).thenReturn(searchResultValue);
        List<BasicIssue> issues = new ArrayList<>();
        issues.add(basicIssue);
        when(searchResultValue.getIssues()).thenReturn(issues);
        when(basicIssue.getKey()).thenReturn(TEST);
        when(jiraRestClient.getIssueClient()).thenReturn(issueRestClient);
        when(issueRestClient.getIssue(TEST)).thenReturn(issuePromise);
        when(issuePromise.isDone()).thenReturn(true);
        when(issuePromise.get()).thenReturn(issue);
        when(issue.getAssignee()).thenReturn(basicUser);
        when(basicUser.getDisplayName()).thenReturn(TEST);
        when(issue.getDescription()).thenReturn(TEST);
        when(issue.getStatus()).thenReturn(basicStatus);
        when(basicStatus.getName()).thenReturn(TEST);

        // Act
        JiraQueryExecutor.saveTicketsToFile(jiraRestClient, TEST, file.getAbsolutePath());

        // Assert
        verify(printStream).println(START);
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        bufferedReader.skip(99);
        assertTrue(bufferedReader.readLine().contains(TEST));
    }

    @Test
    public void givenExecutorWhenPrintCountOfMatchingTicketsThenSuccessful()
            throws ExecutionException, InterruptedException {
        // Arrange
        System.setOut(printStream);
        when(jiraRestClient.getSearchClient()).thenReturn(searchRestClient);
        when(searchRestClient.searchJql(TEST)).thenReturn(searchResult);
        when(searchResult.isDone()).thenReturn(true);
        when(searchResult.get()).thenReturn(searchResultValue);
        when(searchResultValue.getTotal()).thenReturn(1);

        // Act
        JiraQueryExecutor.printCountOfMatchingTickets(jiraRestClient, TEST);

        // Assert
        verify(printStream).println(START);
        verify(printStream).println("Found issues count: 1");

        verify(jiraRestClient).getSearchClient();
        verify(searchRestClient).searchJql(TEST);
        verify(searchResult).isDone();
        verify(searchResult).get();
        verify(searchResultValue).getTotal();
    }
}
