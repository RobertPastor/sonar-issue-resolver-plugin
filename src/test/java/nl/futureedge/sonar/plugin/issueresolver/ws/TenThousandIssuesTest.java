package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Common.Severity;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.Issues.Issue;

public class TenThousandIssuesTest {

	private static final Logger LOGGER = Loggers.get(TenThousandIssuesTest.class);



	@Test
	public void TenThousandIssues() throws IOException {


		// Request
		final MockRequest request = new MockRequest();
		request.setParam("fromProjectKey", "base-project-key");
		request.setParam("projectKey", "my-project-key");

		// 14 January 2019 - target branch
		request.setParam("fromProject-branch", "release-IBB1");
		request.setParam("toProject-branch", "master");

		request.setParam("preview", "false");
		request.setParam("skipAssign", "false");
		request.setParam("skipComments", "false");

		// Local call (first page)
		final Map<String, String> localRequestBaseParamsToCheckPageOne = new HashMap<>();
		localRequestBaseParamsToCheckPageOne.put("projectKeys", "base-project-key");
		localRequestBaseParamsToCheckPageOne.put("additionalFields", "comments");
		// 14th January 2019 - add hash
		//localRequestBaseParamsToCheckPageOne.put("additionalFields", "hash");
		localRequestBaseParamsToCheckPageOne.put("statuses", "CONFIRMED,REOPENED,RESOLVED");
		// page id and page size
		localRequestBaseParamsToCheckPageOne.put("p", "1");
		localRequestBaseParamsToCheckPageOne.put("ps", "100");

		//final Issues.SearchWsResponse.Builder localRequestBaseResponsePageOne = Issues.SearchWsResponse.newBuilder();
		//localRequestBaseResponsePageOne.setPaging(Common.Paging.newBuilder().setTotal(3).setPageIndex(1).setPageSize(2));

		// total number of issues and number of pages
		//Issues.SearchWsResponse.Builder builder = Issues.SearchWsResponse.newBuilder().setTotal(20000).setPs(100);

		final Issues.SearchWsResponse.Builder localRequestResponsePageOne = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageOne.setPaging(Common.Paging.newBuilder().setTotal(10001).setPageIndex(1).setPageSize(100));
		
		localRequestResponsePageOne.addComponents(Issues.Component.newBuilder()
				.setKey("nl.future-edge.sonarqube.plugins:myBranch:sonar-issueresolver-plugin:pom.xml")
				.setLongName("pom.xml"));

		for (int i = 0; i < 100; i++) {
			localRequestResponsePageOne.addIssues(Issues.Issue.newBuilder().setKey("TenThousandIssues").setRule("xml:IllegalTabCheck")
					.setComponent("nl.future-edge.sonarqube.plugins:myBranch:sonar-issueresolver-plugin:pom.xml")
					.setHash("hash")
					.setTextRange(Common.TextRange.newBuilder().setStartLine(4).setStartOffset(0))
					.setResolution("FALSE-POSITIVE")
					.setStatus("RESOLVED")
					.setMessage("message")
					.setCreationDate("14/01/2019")
					.setSeverity(Severity.INFO));
		}

		Issues.SearchWsResponse searchWsResponse = localRequestResponsePageOne.build();
		//Issues.SearchWsResponse searchWsResponse = builder.build();
		//for (int i = 0; i < 100; i++) {

		//	MockResponse mockResponse = new MockResponse().setStatus(200).setBody(toBuffer(searchWsResponse));
		//	mockResponse = mockResponse.setHeader("Content-Type", "application/x-protobuf");

		//}
		//MockResponse mockLastResponse = new MockResponse().setStatus(400).setBody("{\"errors\":[{\"msg\":\"Can return only the first 10000 results. 10100th result asked.\"}]}"));

		request.mockLocalRequest("api/issues/search", localRequestBaseParamsToCheckPageOne, localRequestResponsePageOne.build().toByteArray());

		// Response
		final MockResponse response = new MockResponse();

		// Execute
		final UpdateAction subject = new UpdateAction();
		subject.handle(request, response);

		LOGGER.debug(new String(response.result(), "UTF-8"));

		// Validate
		final String result = new String(response.result(), "UTF-8");


	}

}


