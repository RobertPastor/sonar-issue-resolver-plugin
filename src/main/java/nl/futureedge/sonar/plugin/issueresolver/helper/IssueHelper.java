package nl.futureedge.sonar.plugin.issueresolver.helper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import org.sonar.api.server.ws.LocalConnector;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarqube.ws.Issues.Comment;

import org.sonarqube.ws.Issues.Issue;
import org.sonarqube.ws.Issues.SearchWsResponse;
import org.sonarqube.ws.WsMeasures.ComponentTreeWsResponse;
import org.sonarqube.ws.WsMeasures.Component; 

import org.sonarqube.ws.client.PostRequest;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.WsConnector;
import org.sonarqube.ws.client.WsRequest;
import org.sonarqube.ws.client.WsResponse;
import org.sonarqube.ws.client.issue.SearchWsRequest;
import org.sonarqube.ws.client.measure.ComponentTreeWsRequest;

import nl.futureedge.sonar.plugin.issueresolver.issues.IssueData;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKey;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKeyExtension;
import nl.futureedge.sonar.plugin.issueresolver.ws.ImportResult;

/**
 * Issue functionality.
 */
public final class IssueHelper {

	private static final Logger LOGGER = Loggers.get(IssueHelper.class);

	private static final String PATH_TRANSITION = "api/issues/do_transition";
	private static final String PATH_ASSIGN = "api/issues/assign";
	private static final String PATH_ADD_COMMENT = "api/issues/add_comment";

	private static final String PARAM_ISSUE = "issue";
	private static final String PARAM_TRANSITION = "transition";
	private static final String PARAM_ASSIGNEE = "assignee";
	private static final String PARAM_TEXT = "text";

	// Error 400 on api/issues/search : {"errors":[{"msg":"Can return only the first 10000 results. 10100th result asked."}]}
	private static final int MAX_SEARCH_ISSUES = 10000;

	private static int issuesCount = 0;
	private static Component lastDirectory ;

	private IssueHelper() {
	}

	public static void forEachDirectory( final LocalConnector localConnector, final ComponentTreeWsRequest searchDirectoryRequest, 
			final BiConsumer<ComponentTreeWsResponse, Component> consumer) {

		// Loop through all directories of the project
		final WsClient wsClient = WsClientFactories.getLocal().newClient(localConnector);

		boolean doNextPage = true;
		while (doNextPage) {

			LOGGER.debug("Listing directory for project {}; page {}", searchDirectoryRequest.getComponent() , searchDirectoryRequest.getPage());

			final ComponentTreeWsResponse searchDirectoriesResponse = wsClient.measures().componentTree(searchDirectoryRequest);
			for (final Component directory : searchDirectoriesResponse.getComponentsList() ) {
				consumer.accept(searchDirectoriesResponse, directory);
			}

			int nbDirectories = searchDirectoriesResponse.getPaging().getTotal();
			LOGGER.debug("nb directories= "+ nbDirectories);

			// condition to query the next page
			doNextPage = searchDirectoriesResponse.getPaging().getTotal() > (searchDirectoriesResponse.getPaging().getPageIndex() * searchDirectoriesResponse.getPaging().getPageSize());

			searchDirectoryRequest.setPage(searchDirectoriesResponse.getPaging().getPageIndex() + 1);
			searchDirectoryRequest.setPageSize(searchDirectoriesResponse.getPaging().getPageSize());
		}
	}

	/**
	 * Loop over issues.
	 * 
	 * @param localConnector
	 *            local connector
	 * @param searchIssuesRequest
	 *            search request
	 * @param consumer
	 *            callback called for each issues
	 */
	public static boolean forEachIssue(final LocalConnector localConnector, final SearchWsRequest searchIssuesRequest,
			final BiConsumer<SearchWsResponse, Issue> consumer) {

		boolean moreThanTenThousandIssues = false;

		// Loop through all issues of the project
		final WsClient wsClient = WsClientFactories.getLocal().newClient(localConnector);

		boolean doNextPage = true;
		while (doNextPage) {

			LOGGER.debug("Listing issues for project {}; page {}", searchIssuesRequest.getProjectKeys(), searchIssuesRequest.getPage());

			final SearchWsResponse searchIssuesResponse = wsClient.issues().search(searchIssuesRequest);
			for (final Issue issue : searchIssuesResponse.getIssuesList()) {
				consumer.accept(searchIssuesResponse, issue);
			}

			// condition to query the next page
			//doNextPage = searchIssuesResponse.getPaging().getTotal() > (searchIssuesResponse.getPaging().getPageIndex() * searchIssuesResponse.getPaging().getPageSize());
			doNextPage = Math.min(MAX_SEARCH_ISSUES ,  searchIssuesResponse.getPaging().getTotal()) > (searchIssuesResponse.getPaging().getPageIndex() * searchIssuesResponse.getPaging().getPageSize());

			// sonar does not return more than 10.000 issues
			int maxPages = ( MAX_SEARCH_ISSUES / (searchIssuesResponse.getPaging().getPageSize() + 1)) + 1;
			int nbPages = ( searchIssuesResponse.getPaging().getTotal() / (searchIssuesResponse.getPaging().getPageSize() + 1)) + 1;
			if (nbPages > maxPages) {
				LOGGER.warn("Max issues over 10.000 -> " + searchIssuesResponse.getPaging().getTotal());
				moreThanTenThousandIssues = true;
			}

			searchIssuesRequest.setPage(searchIssuesResponse.getPaging().getPageIndex() + 1);
			searchIssuesRequest.setPageSize(searchIssuesResponse.getPaging().getPageSize());
		}
		return moreThanTenThousandIssues;
	}

	/**
	 * Resolve issues => modifiy issues in project with project Key and target branch
	 * 
	 * @param localConnector
	 *            local connector
	 * @param importResult
	 *            result in the target project branch
	 * @param preview
	 *            true if issues should not be actually resolved.
	 * @param skipAssign
	 *            if true, no assignments will be done
	 * @param skipComments
	 *            if true, no comments will be added
	 * @param projectKey
	 *            project key
	 * @param issues
	 *            issues from the input reference branch (can be the JSON input file)
	 */
	public static void resolveIssues(final LocalConnector localConnector, final ImportResult importResult,
			final boolean preview, final boolean skipAssign, final boolean skipComments, final String projectKey,
			final String targetBranch,
			final Map<IssueKey, IssueData> sourceIssues) {
		
		issuesCount = 0;

		// Read issues from project, match and resolve
		importResult.setPreview(preview);

		final WsClient wsClient = WsClientFactories.getLocal().newClient(localConnector);

		// search request 
		final ComponentTreeWsRequest searchDirectoryRequest = SearchHelper.getDirectoryList(projectKey, targetBranch);

		List<Component> directoryList = new ArrayList<Component>();
		// retrieve all directories
		forEachDirectory(localConnector, searchDirectoryRequest, (searchDirectoryResponse, directory) -> {

			LOGGER.debug("directory= " + directory.getDescription() + " - " + directory.getQualifier() );
			LOGGER.debug("directory= " + directory.getKey() + " - " + directory.getName() + " - " + directory.getPath() );

			directoryList.add(directory);
		});
		
		// record the number of directories
		importResult.setNumberOfDirectories(directoryList.size());
		LOGGER.debug("number of directories= " + directoryList.size());

		Iterator<Component> iter = directoryList.iterator();
		// loop through the directories
		while (iter.hasNext()) {
			
			Component directory = iter.next();

			// Loop through all issues of the current project with the provided target branch
			final SearchWsRequest searchIssuesRequest = SearchHelper.findIssuesForImport(projectKey , targetBranch, directory);

			// issue from the FROM TARGET project branch
			boolean moreThanTenThousandIssues = forEachIssue(localConnector, searchIssuesRequest, (searchIssuesResponse, issue) -> {

				LOGGER.info("------------------------------------ " + issue.getKey() + " ----------------------------");

				issuesCount = issuesCount + 1;
				// issue 
				final IssueKey issueKey = IssueKey.fromIssue(issue, searchIssuesResponse.getComponentsList());
				final IssueKeyExtension issueKeyExtension = IssueKeyExtension.fromIssue(issue, searchIssuesResponse.getComponentsList());

				LOGGER.debug("Try to match issue: {}", issueKey);

				// Match between issues (reference read-only) from data
				// if removes is OK then data contains the data of the matching issue
				final IssueData data = sourceIssues.remove(issueKey);

				// check if there is data from the JSON file or from the origin project (projectKey)
				if (data != null) {

					LOGGER.info("issues are matching= " + issueKey.toString()); 
					// set PLUS ONE matched issue
					importResult.registerMatchedIssue();

					// Handle issue, if data is found
					handleTransition(wsClient.wsConnector(), issue, data.getStatus(), data.getResolution() , preview, importResult, issueKeyExtension);

					// conditional modification
					if (!skipAssign) {
						handleAssignee(wsClient.wsConnector(), issue, data.getAssignee(), preview, importResult, issueKeyExtension);
					}
					if (!skipComments) {
						handleComments(wsClient.wsConnector(), issue, data.getComments(), preview, importResult, issueKeyExtension);
					}

					// import result - store issue that can be modified - target branch
					importResult.recordToBeModifiedIssue(issueKeyExtension);
				}
			});

			// record more than ten Thousand issues
			importResult.recordMoreThanTenThousandIssues(moreThanTenThousandIssues);
		}

		LOGGER.debug("number of issues= " + issuesCount );
		importResult.recordNumberOfTargetIssues(issuesCount);
	}

	/* ************** ********** ************** */
	/* ************** TRANSITION ************** */
	/* ************** ********** ************** */

	private static void handleTransition(final WsConnector wsConnector, final Issue issue, final String status, final String resolution, final boolean preview, final ImportResult importResult, IssueKeyExtension issueKey) {

		LOGGER.info("source - status= " + status  + " - resolution= " + resolution );
		LOGGER.info("target - status= " + issue.getStatus() + " - resolution= " + issue.getResolution());
		final String transition = determineTransition(issue.getKey(), issue.getStatus(), issue.getResolution(), status , resolution  , importResult);
		
		if (transition != null) {
			
			LOGGER.info("transition is= " + transition);
			// there is a valid transition
			if (preview == false) {
				// transition the issue
				boolean localTransitionResult = transitionIssue(wsConnector, issue.getKey(), transition, importResult);
				issueKey.setTransitioned(localTransitionResult);
				if (localTransitionResult) {
					importResult.registerTransitionedIssue();
				}
			} else {
				// the issue would have been transitioned
				issueKey.setTransitioned(true);			
				importResult.registerTransitionedIssue();
			}
			
		}
	}

	private static String determineTransition(final String issue, final String currentStatus,
			final String currentResolution, final String wantedStatus, final String wantedResolution,
			final ImportResult importResult) {

		final String transition;
		if (TransitionHelper.noAction(currentStatus, currentResolution, wantedStatus, wantedResolution)) {
			transition = null;
		} else if (TransitionHelper.shouldConfirm(currentStatus, wantedStatus)) {
			transition = "confirm";
		} else if (TransitionHelper.shouldUnconfirm(currentStatus, wantedStatus)) {
			transition = "unconfirm";
		} else if (TransitionHelper.shouldReopen(currentStatus, wantedStatus)) {
			transition = "reopen";
		} else if (TransitionHelper.shouldResolveFixed(currentStatus, wantedStatus, wantedResolution)) {
			transition = "resolve";
		} else if (TransitionHelper.shouldResolveFalsePositive(currentStatus, wantedStatus, wantedResolution)) {
			transition = "falsepositive";
		} else if (TransitionHelper.shouldReopen(currentStatus, wantedStatus, wantedResolution)) {
			transition = "wontfix";
		} else {
			importResult.registerMatchFailure("Could not determine transition for issue with key '" + issue
					+ "'; current status is '" + currentStatus + "' and resolution is '" + currentResolution
					+ "'; wanted status is '" + wantedStatus + "' and resolution is '" + wantedResolution + "'");
			transition = null;
		}
		return transition;
	}

	private static boolean transitionIssue(final WsConnector wsConnector, final String issue, final String transition,
			final ImportResult importResult) {

		// use sonar api - the issue to transition
		final WsRequest request = new PostRequest(PATH_TRANSITION).setParam(PARAM_ISSUE, issue)
				.setParam(PARAM_TRANSITION, transition);

		final WsResponse response = wsConnector.call(request);

		boolean responseIsSuccessFul =  response.isSuccessful();
		if (responseIsSuccessFul ==  false) {
			LOGGER.debug("Failed to transition issue: " + response.content());
			importResult.registerTransitionFailure(	"Could not transition issue with key '" + issue + "' using transition '" + transition + "'");
		} else {
			// register the issue in the result 
			LOGGER.debug("transition issue is OK : " + response.content());
		}
		return responseIsSuccessFul;
	}

	/* ************** ******** ************** */
	/* ************** ASSIGNEE ************** */
	/* ************** ******** ************** */

	private static void handleAssignee(final WsConnector wsConnector, final Issue issue, final String assignee,
			final boolean preview, final ImportResult importResult, IssueKeyExtension issueKey) {

		LOGGER.debug("Handle assignee '{}' for issue with key '{}' - current assignee is '{}' ", assignee, issue.getKey(), issue.getAssignee());
		final String currentAssignee = issue.getAssignee() == null ? "" : issue.getAssignee();
		if (assignee.length()==0) {
			LOGGER.warn("cannot assignee an empty assignee");
		}
		if (!currentAssignee.equals(assignee) && (assignee.length()>0)) {
			if (!preview) {
				boolean assignSuccessFul = assignIssue(wsConnector, issue.getKey(), assignee, importResult);
				issueKey.setAssigned(assignSuccessFul);
				if (assignSuccessFul) {
					importResult.registerAssignedIssue();
				}
			} else {
				issueKey.setAssigned(true);
				importResult.registerAssignedIssue();
			}
		}
	}

	private static boolean assignIssue(final WsConnector wsConnector, final String issue, final String assignee,
			final ImportResult importResult) {

		LOGGER.debug("Assigning '{}' for issue with key '{}'", assignee, issue);
		final WsRequest request = new PostRequest(PATH_ASSIGN).setParam(PARAM_ISSUE, issue).setParam(PARAM_ASSIGNEE,
				assignee);
		final WsResponse response = wsConnector.call(request);

		boolean responseIsSuccessful = response.isSuccessful();
		if (!responseIsSuccessful) {
			LOGGER.debug("Failed to assign issue: " + response.content());
			importResult.registerAssignFailure(
					"Could not assign issue with key '" + issue + "' to user '" + assignee + "'");
		}
		return responseIsSuccessful;
	}

	/* ************** ******* ************** */
	/* ************** COMMENT ************** */
	/* ************** ******* ************** */

	private static void handleComments(final WsConnector wsConnector, final Issue issue, final List<String> comments,
			final boolean preview, final ImportResult importResult, IssueKeyExtension issueKey) {
		boolean commentAdded = false;
		for (final String comment : comments) {
			// check if comment is already existing
			if (!alreadyContainsComment(issue.getComments().getCommentsList(), comment)) {
				commentAdded = true;
				if (!preview) {
					issueKey.setCommented(addComment(wsConnector, issue.getKey(), comment, importResult));
				} else {
					issueKey.setCommented(true);
				}
			}
		}

		if (commentAdded) {
			importResult.registerCommentedIssue();
		}
	}

	private static boolean alreadyContainsComment(final List<Comment> currentComments, final String comment) {
		for (Comment currentComment : currentComments) {
			if (currentComment.getMarkdown().equals(comment)) {
				return true;
			}
		}
		return false;
	}

	private static boolean addComment(final WsConnector wsConnector, final String issue, final String text,
			final ImportResult importResult) {

		final WsRequest request = new PostRequest(PATH_ADD_COMMENT).setParam(PARAM_ISSUE, issue).setParam(PARAM_TEXT, text);
		final WsResponse response = wsConnector.call(request);

		boolean responseIsSuccessful = response.isSuccessful();
		if (!responseIsSuccessful) {
			LOGGER.debug("Failed to add comment to issue: " + response.content());
			importResult.registerCommentFailure("Could not add comment to issue with key '" + issue + "'");
		}
		return responseIsSuccessful;
	}

}
