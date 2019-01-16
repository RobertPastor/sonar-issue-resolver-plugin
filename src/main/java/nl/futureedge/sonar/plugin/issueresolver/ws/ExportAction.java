package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.server.ws.WebService.NewController;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Issues.Component;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.helper.IssueHelper;
import nl.futureedge.sonar.plugin.issueresolver.helper.SearchHelper;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueData;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKey;



/**
 * Export action.
 */
public class ExportAction implements IssueResolverWsAction {

	public static final String ACTION = "export";
	public static final String PARAM_PROJECT_KEY = "projectKey";
	
	// 14 January 2019 - target-branch - name of the input in the web page
	public static final String PARAM_TARGET_BRANCH = "target-branch";

	private static final Logger LOGGER = Loggers.get(ExportAction.class);
	
	private static String stripNonAscii(String str) {
		
		StringBuffer buff = new StringBuffer();
        char chars[] = str.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if ((65 <= chars[i] && chars[i] <= 90) || (97 <= chars[i] && chars[i] <= 122)) {
                buff.append(chars[i]);
            }
        }
        return buff.toString();
	}


	@Override
	public void define(final NewController controller) {
		
		LOGGER.debug("Defining export action ...");
		
		final NewAction action = controller.createAction(ACTION)
				.setDescription("Export resolved issues with the status false positive or won't fix.")
				.setResponseExample(getClass().getResource("/response-examples/export.json")).setHandler(this)
				.setPost(false);

		action.createParam(PARAM_PROJECT_KEY).setDescription("Project to export issues from")
				.setExampleValue("my-project").setRequired(true);
		
		action.createParam(PARAM_TARGET_BRANCH).setDescription("Branch to export issues from")
				.setExampleValue("my-branch").setRequired(true);
		
		LOGGER.debug("Export action defined");
	}

	@Override
	public void handle(final Request request, final Response response) {
		
		LOGGER.debug("Handle issueresolver export request");
		String fileName = stripNonAscii(request.mandatoryParam(PARAM_PROJECT_KEY));
		LOGGER.info("file Name= " + fileName);
		
		// header of the response
		response.setHeader("Content-Disposition", "attachment; filename=\"resolved-issues.json\"");

		final JsonWriter responseWriter = response.newJsonWriter();
		writeStart(responseWriter, request.mandatoryParam(PARAM_PROJECT_KEY), request.param(PARAM_TARGET_BRANCH));

		IssueHelper.forEachIssue(request.localConnector(),
				SearchHelper.findIssuesForExport(request.mandatoryParam(PARAM_PROJECT_KEY) , request.param(PARAM_TARGET_BRANCH) ), (searchIssuesResponse,
						issue) -> writeIssue(responseWriter, issue, searchIssuesResponse.getComponentsList()));

		writeEnd(responseWriter);
		responseWriter.close();
		LOGGER.debug("Issue Resolver export request done");
	}

	private void writeStart(final JsonWriter writer, String projectKey, String projectBranch) {
		writer.beginObject();
		writer.prop("version", 1);
		writer.prop("projectKey", projectKey);
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		writer.prop("exportDate", df.format(new Date()));
		if (projectBranch.length()>0) {
			writer.prop("branch", projectBranch);
		}
		writer.name("issues");
		writer.beginArray();
	}

	private void writeIssue(final JsonWriter writer, final Issue issue, List<Component> components) {
		writer.beginObject();

		final IssueKey key = IssueKey.fromIssue(issue, components);
		key.write(writer);

		final IssueData data = IssueData.fromIssue(issue);
		data.write(writer);

		writer.endObject();
	}

	private void writeEnd(final JsonWriter writer) {
		writer.endArray();
		writer.endObject();
		writer.close();
	}

}
