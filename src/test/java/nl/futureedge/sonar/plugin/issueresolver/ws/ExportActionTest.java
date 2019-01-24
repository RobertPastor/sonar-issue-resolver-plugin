package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Common.Severity;
import org.sonarqube.ws.Issues;

public class ExportActionTest {

	@Test
	public void test() throws IOException {
		// Request
		final MockRequest request = new MockRequest();
		request.setParam("projectKey", "my-project-key");
		request.setParam("branch", "my-branch");

		// Local call (first page)
		final Map<String, String> localRequestParamsToCheckPageOne = new HashMap<>();
		localRequestParamsToCheckPageOne.put("projectKeys", "my-project-key");
		localRequestParamsToCheckPageOne.put("additionalFields", "comments");
		//localRequestParamsToCheckPageOne.put("target-branch", "my-branch");
		localRequestParamsToCheckPageOne.put("statuses", "CONFIRMED,REOPENED,RESOLVED");
		localRequestParamsToCheckPageOne.put("p", "1");
		localRequestParamsToCheckPageOne.put("ps", "100");

		final Issues.SearchWsResponse.Builder localRequestResponsePageOne = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageOne.setPaging(Common.Paging.newBuilder().setTotal(2).setPageIndex(1).setPageSize(1));
		
		// 14th January 2019 - add hash message creation date and severity
		localRequestResponsePageOne
				.addIssues(Issues.Issue.newBuilder()
						.setKey("AVrdUwSCGyMCMhQpQjBw").setRule("xml:IllegalTabCheck")
						.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:pom.xml")
						.setHash("hash")
						.setAssignee("admin")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(4).setStartOffset(63))
						.setResolution("FALSE-POSITIVE")
						.setStatus("RESOLVED")
						.setMessage("message").setCreationDate("14/01/2019").setSeverity(Severity.INFO));
		
		localRequestResponsePageOne.addComponents(Issues.Component.newBuilder()
				.setKey("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:pom.xml").setLongName("pom.xml"));
		
		request.mockLocalRequest("api/issues/search", localRequestParamsToCheckPageOne,
				localRequestResponsePageOne.build().toByteArray());

		// Local call (second page)
		final Map<String, String> localRequestParamsToCheckPageTwo = new HashMap<>();
		localRequestParamsToCheckPageTwo.put("projectKeys", "my-project-key");
		localRequestParamsToCheckPageTwo.put("additionalFields", "comments");
		//localRequestParamsToCheckPageOne.put("branch", "branch");
		localRequestParamsToCheckPageTwo.put("statuses", "CONFIRMED,REOPENED,RESOLVED");
		localRequestParamsToCheckPageTwo.put("p", "2");
		localRequestParamsToCheckPageTwo.put("ps", "1");

		final Issues.SearchWsResponse.Builder localRequestResponsePageTwo = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageTwo.setPaging(Common.Paging.newBuilder().setTotal(2).setPageIndex(2).setPageSize(1));
		localRequestResponsePageTwo
				.addIssues(Issues.Issue.newBuilder()
						.setKey("AVrdUwS9GyMCMhQpQjBx")
						.setRule("squid:S3776")
						.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
						.setHash("hash")
						.setAssignee("admin")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(64).setStartOffset(16))
						.setResolution("WONTFIX")
						.setStatus("RESOLVED")
						.setMessage("message")
						.setCreationDate("14/01/2019")
						.setSeverity(Severity.INFO));
		
		localRequestResponsePageTwo.addComponents(Issues.Component.newBuilder()
				.setKey("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setLongName("src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java"));
		
		request.mockLocalRequest("api/issues/search", localRequestParamsToCheckPageTwo,
				localRequestResponsePageTwo.build().toByteArray());

		// Response
		final MockResponse response = new MockResponse();

		// Execute
		final ExportAction subject = new ExportAction();
		subject.handle(request, response);

		// Validate
		final String result = new String(response.result(), "UTF-8");
		DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		Assert.assertEquals(
				"{\"version\":1,\"projectKey\":\"my-project-key\",\"exportDate\":\"" + df.format(new Date()) + "\",\"branch\":\"my-branch\",\"issues\":["
				+ "{\"longName\":\"pom.xml\",\"rule\":\"xml:IllegalTabCheck\",\"line\":0,\"hash\":\"hash\",\"message\":\"message\",\"status\":\"RESOLVED\",\"resolution\":\"FALSE-POSITIVE\",\"assignee\":\"admin\",\"creationDate\":\"14/01/2019\",\"severity\":\"INFO\",\"comments\":[]},"
				+ "{\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"rule\":\"squid:S3776\",\"line\":0,\"hash\":\"hash\",\"message\":\"message\",\"status\":\"RESOLVED\",\"resolution\":\"WONTFIX\",\"assignee\":\"admin\",\"creationDate\":\"14/01/2019\",\"severity\":\"INFO\",\"comments\":[]}"
				+ "]}",
				result);
	}
}
