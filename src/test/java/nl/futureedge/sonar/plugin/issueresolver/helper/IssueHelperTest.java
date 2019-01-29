package nl.futureedge.sonar.plugin.issueresolver.helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.Assert;

import org.sonar.api.server.ws.LocalConnector;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Common.Severity;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.client.issue.SearchWsRequest;

import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKeyExtension;
import nl.futureedge.sonar.plugin.issueresolver.ws.MockRequest;

public class IssueHelperTest {

	private static final Logger LOGGER = Loggers.get(IssueHelperTest.class);

	private int count = 0;

	@Test
	public void TenThousandIssues() throws IOException {


		// Request
		final MockRequest request = new MockRequest();
		request.setParam("fromProjectKey", "from-project-key");

		// 14 January 2019 - target branch
		request.setParam("fromProject-branch", "from-project-branch");

		request.setParam("preview", "false");
		request.setParam("skipAssign", "false");
		request.setParam("skipComments", "false");

		int pageSize = 100;
		int nbPages = 100;
		int total = nbPages * pageSize;

		Map<String, String> localRequestBaseParamsToCheckPageOne= new HashMap<String, String>();

		// Local call (first page)
		localRequestBaseParamsToCheckPageOne.put("projectKeys", "from-project-key");
		localRequestBaseParamsToCheckPageOne.put("additionalFields", "comments");
		// 14th January 2019 - add hash
		//localRequestBaseParamsToCheckPageOne.put("additionalFields", "hash");
		localRequestBaseParamsToCheckPageOne.put("statuses", "CONFIRMED,REOPENED,RESOLVED");
		// page id and page size
		localRequestBaseParamsToCheckPageOne.put("p", String.valueOf(1));
		localRequestBaseParamsToCheckPageOne.put("ps", String.valueOf(pageSize));


		Issues.SearchWsResponse.Builder localRequestResponsePageOne = Issues.SearchWsResponse.newBuilder();

		int pageIndex = 1;
		total = 100;
		// Response
		localRequestResponsePageOne.setPaging(Common.Paging.newBuilder().setTotal(total).setPageIndex(pageIndex).setPageSize(pageSize));

		localRequestResponsePageOne.addComponents(Issues.Component.newBuilder()
				.setKey("nl.future-edge.sonarqube.plugins:myBranch:sonar-issueresolver-plugin:pom.xml")
				.setLongName("pom.xml"));


		for (int i = 0 ; i < pageSize ; i++) {

			localRequestResponsePageOne.addIssues(Issues.Issue.newBuilder()
					.setKey("TenThousandIssues-" + String.valueOf(i))
					.setRule("xml:IllegalTabCheck")
					.setComponent("nl.future-edge.sonarqube.plugins:myBranch:sonar-issueresolver-plugin:pom.xml")
					.setHash("hash")
					.setTextRange(Common.TextRange.newBuilder().setStartLine(4).setStartOffset(0))
					.setResolution("FALSE-POSITIVE")
					.setStatus("RESOLVED")
					.setMessage("message")
					.setCreationDate("14/01/2019")
					.setSeverity(Severity.INFO));
		}


		request.mockLocalRequest("api/issues/search", localRequestBaseParamsToCheckPageOne, localRequestResponsePageOne.build().toByteArray());

		LocalConnector localConnector = request.localConnector();

		// Loop through all issues of the current project with the provided target branch
		final SearchWsRequest searchIssuesRequest = SearchHelper.findIssuesForExport("from-project-key" , "from-project-branch");

		// issue from the FROM SOURCE project branch

		IssueHelper.forEachIssue(localConnector, searchIssuesRequest, (searchIssuesResponse, issue) -> {

			// issue 
			final IssueKeyExtension issueKey = IssueKeyExtension.fromIssue(issue, searchIssuesResponse.getComponentsList());
			//LOGGER.debug("Try to match issue: {}", issueKey);
			count = count + 1;

		});

		LOGGER.debug("Number of Issues: {}", count);
		Assert.assertEquals(100, count);

	}
}


