package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.server.ws.WebService.NewController;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.text.JsonWriter;

import nl.futureedge.sonar.plugin.issueresolver.helper.IssueHelper;
import nl.futureedge.sonar.plugin.issueresolver.helper.SearchHelper;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueData;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKey;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKeyExtension;

/**
 * Update action.
 */
public final class UpdateAction implements IssueResolverWsAction {

	public static final String ACTION = "update";
	// name of the input in the tabUpdate that contains the to Project Key -> destination project key
	public static final String PARAM_TO_PROJECT_KEY = "projectKey";
	public static final String PARAM_FROM_PROJECT_KEY = "fromProjectKey";
	
	// 14 January 2019 - target branch
	public static final String PARAM_TO_PROJECT_BRANCH = "toProject-branch";
	public static final String PARAM_FROM_PROJECT_BRANCH = "fromProject-branch";
		
	public static final String PARAM_PREVIEW = "preview";
	public static final String PARAM_SKIP_ASSIGN = "skipAssign";
	public static final String PARAM_SKIP_COMMENTS = "skipComments";

	private static final String BOOLEAN_FALSE = "false";
	private static final List<String> BOOLEAN_VALUES = Arrays.asList("true", BOOLEAN_FALSE);

	private static final Logger LOGGER = Loggers.get(UpdateAction.class);

	@Override
	public void define(final NewController controller) {
		
		LOGGER.debug("Defining update action ...");
		final NewAction action = controller.createAction(ACTION)
				.setDescription("Update issues from in one project based on another.")
				.setResponseExample(getClass().getResource("/response-examples/import.json")).setHandler(this)
				.setPost(true);

		action.createParam(PARAM_TO_PROJECT_KEY).setDescription("Project to resolve issues in")
				.setExampleValue("my-project").setRequired(true);
		action.createParam(PARAM_FROM_PROJECT_KEY).setDescription("Project to read issues from")
				.setExampleValue("my-other-project").setRequired(true);
		
		// 14 January 2019 - add  branch
		action.createParam(PARAM_TO_PROJECT_BRANCH).setDescription("To Project Branch to synchronise in")
				.setExampleValue("master");
		// 14 January 2019 - add  branch
		action.createParam(PARAM_FROM_PROJECT_BRANCH).setDescription("From Project Branch to import from")
				.setExampleValue("release-IBB1");
		
		action.createParam(PARAM_PREVIEW).setDescription("If import should be a preview")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		
		action.createParam(PARAM_SKIP_ASSIGN).setDescription("If assignment should be skipped")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		
		action.createParam(PARAM_SKIP_COMMENTS).setDescription("If comments should be skipped")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		LOGGER.debug("Update action defined");
	}

	@Override
	public void handle(final Request request, final Response response) {
		
		LOGGER.info("Handle issue resolver update request ...");
		final ImportResult importResult = new ImportResult();
		
		// Read issues from origin project - FROM PROJECT - reference data- read-only same as JSON file in import
		final Map<IssueKeyExtension, IssueData> issues = new HashMap<>();
		
		try {
			// Issues for export -> means read-only reference source issues (from the source project branch)
			IssueHelper.forEachIssue(request.localConnector(),
					SearchHelper.findIssuesForExport(request.mandatoryParam(PARAM_FROM_PROJECT_KEY) , request.mandatoryParam(PARAM_FROM_PROJECT_BRANCH) ),
					(searchIssuesResponse, issue) -> {
						
						// issues will contain reference data
						issues.put(IssueKeyExtension.fromIssue(issue, searchIssuesResponse.getComponentsList()),
								IssueData.fromIssue(issue));
						
						importResult.registerIssue();
					});
			
			LOGGER.info("Read " + importResult.getNbIssues() + " issues");

			// add to project branch as key for the modification
			IssueHelper.resolveIssues(request.localConnector(), importResult,
					request.mandatoryParamAsBoolean(PARAM_PREVIEW), request.mandatoryParamAsBoolean(PARAM_SKIP_ASSIGN),
					request.mandatoryParamAsBoolean(PARAM_SKIP_COMMENTS), request.mandatoryParam(PARAM_TO_PROJECT_KEY),
					request.mandatoryParam(PARAM_TO_PROJECT_BRANCH),
					issues);
			
			// Sent result
			final JsonWriter responseWriter = response.newJsonWriter();
			importResult.write(responseWriter);
			responseWriter.close();
			LOGGER.debug("Issue Resolver update request done");
			
		} catch (Exception e) {
			
			LOGGER.info("Error while Updating request parameters " + e.getLocalizedMessage());
			
			final JsonWriter responseWriter = response.newJsonWriter();
			responseWriter.beginObject();
			responseWriter.prop("error", e.getLocalizedMessage());
			responseWriter.endObject();
			responseWriter.close();
		}
	}
}
