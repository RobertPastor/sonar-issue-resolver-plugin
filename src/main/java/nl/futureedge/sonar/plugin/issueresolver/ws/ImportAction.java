package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonar.api.server.ws.Request;
import org.sonar.api.server.ws.Request.Part;
import org.sonar.api.server.ws.Response;
import org.sonar.api.server.ws.WebService.NewAction;
import org.sonar.api.server.ws.WebService.NewController;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.text.JsonWriter;

import nl.futureedge.sonar.plugin.issueresolver.helper.*;
import nl.futureedge.sonar.plugin.issueresolver.issues.*;
import nl.futureedge.sonar.plugin.issueresolver.json.*;
import nl.futureedge.sonar.plugin.issueresolver.ws.*;;

/**
 * Import action.
 */
public final class ImportAction implements IssueResolverWsAction {

	public static final String ACTION = "import";
	public static final String PARAM_PROJECT_KEY = "projectKey";
	// 14 January 2019 - target branch
	public static final String PARAM_TARGET_BRANCH = "target-branch";
	public static final String PARAM_PREVIEW = "preview";
	public static final String PARAM_DATA = "data";
	public static final String PARAM_SKIP_ASSIGN = "skipAssign";
	public static final String PARAM_SKIP_COMMENTS = "skipComments";

	private static final String BOOLEAN_FALSE = "false";
	private static final List<String> BOOLEAN_VALUES = Arrays.asList("true", BOOLEAN_FALSE);

	private static final Logger LOGGER = Loggers.get(ImportAction.class);

	@Override
	public void define(final NewController controller) {
		
		LOGGER.debug("Defining import action ...");
		final NewAction action = controller.createAction(ACTION)
				.setDescription("Import issues that have exported with the export function.")
				.setResponseExample(getClass().getResource("/response-examples/import.json")).setHandler(this)
				.setPost(true);

		action.createParam(PARAM_PROJECT_KEY).setDescription("Project to import issues to")
				.setExampleValue("my-project").setRequired(true);
		
		// 14 January 2019 - add Target branch
		action.createParam(PARAM_TARGET_BRANCH).setDescription("Target Branch to import issues to")
				.setExampleValue("release-IBB1").setRequired(true);
		
		action.createParam(PARAM_PREVIEW).setDescription("If import should be a preview")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		
		action.createParam(PARAM_SKIP_ASSIGN).setDescription("If assignment should be skipped")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		
		action.createParam(PARAM_SKIP_COMMENTS).setDescription("If comments should be skipped")
				.setPossibleValues(BOOLEAN_VALUES).setDefaultValue(BOOLEAN_FALSE);
		
		// this is the data file parameter with issues to be matched
		action.createParam(PARAM_DATA).setDescription("Exported resolved issue data").setRequired(true);
		LOGGER.debug("Import action defined");
	}

	@Override
	public void handle(final Request request, final Response response) {
		
		LOGGER.info("Handle issue Resolver import request ...");
		final ImportResult importResult = new ImportResult();

		// Read issue data from request
		final Map<IssueKeyExtension, IssueData> issues;
		JsonWriter responseWriter;
		try {
			
			// retrieve the issues as available in the input JSON file
			issues = readIssues(request, importResult);
			
			LOGGER.info("Read " + importResult.getNbIssues() + " issues (having " + importResult.getDuplicateKeys() + " duplicate keys)");
			
			// resolve issues => modify issues in projet with project key and target branch
			IssueHelper.resolveIssues(request.localConnector(), importResult,
					request.mandatoryParamAsBoolean(PARAM_PREVIEW), request.mandatoryParamAsBoolean(PARAM_SKIP_ASSIGN),
					request.mandatoryParamAsBoolean(PARAM_SKIP_COMMENTS), request.mandatoryParam(PARAM_PROJECT_KEY),
					request.mandatoryParam(PARAM_TARGET_BRANCH),
					issues);

			// Send result
			responseWriter = response.newJsonWriter();
			importResult.write(responseWriter);
			responseWriter.close();
			
			
		} catch(IllegalStateException e) {
			
			LOGGER.info("Error while reading request parameters " + e.getLocalizedMessage());
			responseWriter = response.newJsonWriter();
			responseWriter.beginObject();
			responseWriter.prop("error", e.getLocalizedMessage());
			responseWriter.endObject();
			responseWriter.close();
		}
		
		LOGGER.debug("Issue Resolver import request done");
	}

	/* ************** READ ************** */
	/* ************** READ ************** */
	/* ************** READ ************** */

	private Map<IssueKeyExtension, IssueData> readIssues(final Request request, final ImportResult importResult)  {
		
		final Part data;
		try {
			data = request.mandatoryParamAsPart(PARAM_DATA);
			if (data == null) {
				LOGGER.info("Error while reading request parameters " + PARAM_DATA);
				throw new IllegalStateException("Mandatory Parameters Import File is missing");
			}
		} catch(Exception e) {
			LOGGER.info("Error while reading request parameters " + e.getLocalizedMessage());
			throw new IllegalStateException("Mandatory Parameters Import File is missing");
		}
		
		final Map<IssueKeyExtension, IssueData> issues;

		try (final JsonReader reader = new JsonReader(data.getInputStream())) {
			reader.beginObject();

			final int constantCurrentVersion = 1;
			// Version
			final int version = reader.propAsInt("version");
			switch (version) {
			case constantCurrentVersion:
				// 14 January 2019 - Robert PASTOR - add project key
				final String projectKey = reader.prop("projectKey");
				LOGGER.debug("project Key: " + projectKey);
				
				// 14 January 2019 - Robert PASTOR - add export Date
				final String exportDate = reader.prop("exportDate");
				LOGGER.debug("export Date: " + exportDate);
				
				// 17th January 2019 - Robert PASTOR - add branch name
				final String branchName = reader.prop("branch");
				LOGGER.debug("branch Name: " + branchName);
				
				// read and return the issues
				issues = readIssuesVersionOne(reader, importResult);
				break;
			default:
				throw new IllegalStateException("JSON header Error - Unknown version '" + version + "' - expected value was '" + constantCurrentVersion + "'");
			}
			reader.endObject();
		} catch (IOException ex) {
			throw new IllegalStateException("JSON Import - Unexpected error during data parse", ex);
		}
		return issues;
	}

	private Map<IssueKeyExtension, IssueData> readIssuesVersionOne(final JsonReader reader, final ImportResult importResult) throws IOException {
		
		final Map<IssueKeyExtension, IssueData> issues = new HashMap<IssueKeyExtension, IssueData>();

		reader.assertName("issues");
		reader.beginArray();
		while (reader.hasNext()) {
			
			reader.beginObject();
			final IssueKeyExtension key = IssueKeyExtension.read(reader);
			LOGGER.debug("Read issue: " + key);
			final IssueData data = IssueData.read(reader);
			importResult.registerIssue();

			if (issues.containsKey(key)) {
				importResult.registerDuplicateKey();
			} else {
				issues.put(key, data);
			}
			reader.endObject();
		}
		reader.endArray();

		return issues;
	}
}
