# JiraHelper

## Purpose
Execute JQL queries against Jira from IDE. Result is printed to IDE console.

## Usage
- In `StartApplication` class specify Jira URL and login.
- In `execute` method of `JiraQueryExecutor` uncomment `printTicket` method call.
- Specify existing Jira ticket number in `printTicket` parameter.
- Run `main` method of `StartApplication` class.
- In opened window enter password and click OK.
- Ticket information wilt be printed to IDE console.
- Try other commented methods in `JiraQueryExecutor.execute`.
