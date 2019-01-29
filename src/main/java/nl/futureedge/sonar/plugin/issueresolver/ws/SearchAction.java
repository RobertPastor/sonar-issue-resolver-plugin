package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonar.api.server.ws.LocalConnector;
import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.server.ws.WebService.NewController;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarqube.ws.WsMeasures.Component;
import org.sonarqube.ws.client.WsClient;
import org.sonarqube.ws.client.WsClientFactories;
import org.sonarqube.ws.client.issue.SearchWsRequest;
import org.sonarqube.ws.client.measure.ComponentTreeWsRequest;

import nl.futureedge.sonar.plugin.issueresolver.helper.IssueHelper;
import nl.futureedge.sonar.plugin.issueresolver.helper.SearchHelper;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueData;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKey;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKeyExtension;

public class SearchAction implements IssueResolverWsAction {

	// allows to search issues looping through the directories to avoid 10K issues search api limitations
	public static final String ACTION = "search";
	
	// param key of the project
	public static final String PARAM_PROJECT_KEY = "projectKey";
	
	// 14 January 2019 - target-branch - name of the input in the web page
	public static final String PARAM_BRANCH = "branch";

	private static final Logger LOGGER = Loggers.get(SearchAction.class);
	
	private int issuesCount = 0;

	@Override
	public void define(NewController controller) {		
		
		LOGGER.debug("Defining search action ...");
		
		final NewAction action = controller.createAction(ACTION)
				.setDescription("Searchissues looping through directories to avoid 10k issues.")
				.setResponseExample(getClass().getResource("/response-examples/export.json")).setHandler(this)
				.setPost(false);

		action.createParam(PARAM_PROJECT_KEY).setDescription("Project to seach issues from")
				.setExampleValue("my-project").setRequired(true);
		
		action.createParam(PARAM_BRANCH).setDescription("Branch to export issues from")
				.setExampleValue("my-branch").setRequired(true);
		
		LOGGER.debug("Search action defined");
		
	}

	@Override
	public void handle(final Request request, final Response response) throws Exception {

		LocalConnector localConnector = request.localConnector();
		final WsClient wsClient = WsClientFactories.getLocal().newClient(localConnector);

		// search request 
		String projectKey = request.mandatoryParam(PARAM_PROJECT_KEY);
		String branchName = request.mandatoryParam(PARAM_BRANCH);
		final ComponentTreeWsRequest searchDirectoryRequest = SearchHelper.getDirectoryList(projectKey, branchName);

		List<Component> directoryList = new ArrayList<Component>();
		// retrieve all directories
		IssueHelper.forEachDirectory(localConnector, searchDirectoryRequest, (searchDirectoryResponse, directory) -> {

			LOGGER.debug("directory= " + directory.getDescription() + " - " + directory.getQualifier() );
			LOGGER.debug("directory= " + directory.getKey() + " - " + directory.getName() + " - " + directory.getPath() );

			directoryList.add(directory);
		});
		
		// record the number of directories
		LOGGER.debug("number of directories= " + directoryList.size());

		Iterator<Component> iter = directoryList.iterator();
		// loop through the directories
		while (iter.hasNext()) {
			
			Component directory = iter.next();

			// Loop through all issues of the current project with the provided target branch
			final SearchWsRequest searchIssuesRequest = SearchHelper.findIssuesForImport(projectKey , branchName, directory);

			// issue from the FROM TARGET project branch
			boolean moreThanTenThousandIssues = IssueHelper.forEachIssue(localConnector, searchIssuesRequest, (searchIssuesResponse, issue) -> {

				LOGGER.info("------------------------------------ " + issue.getKey() + " ----------------------------");

				issuesCount = issuesCount + 1;
				
				// issue 
				final IssueKey issueKey = IssueKey.fromIssue(issue, searchIssuesResponse.getComponentsList());
				final IssueData issueData = IssueData.fromIssue(issue);

					
				
			});


		}

		
	}

	
}
