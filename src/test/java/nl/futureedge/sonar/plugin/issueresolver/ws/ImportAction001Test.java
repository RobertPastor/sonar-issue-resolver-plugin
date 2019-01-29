package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.internal.apachecommons.io.IOUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonarqube.ws.Common;
import org.sonarqube.ws.Issues;
import org.sonarqube.ws.WsMeasures;
import org.sonarqube.ws.Common.Severity;

import nl.futureedge.sonar.plugin.issueresolver.helper.IssueHelper;

public class ImportAction001Test {
	
	private static final Logger LOGGER = Loggers.get(ImportAction001Test.class);

	private final static int sonarIssuesSearchPageSize = 500;

	@Test
	public void test() throws IOException {
		// Request
		final MockRequest request = new MockRequest();
		request.setParam("projectKey", "my-project-key");
		// 14 January 2019 - target branch
		request.setParam("target-branch", "release-IBB10");
		
		request.setParam("preview", "false");
		request.setParam("skipAssign", "false");
		request.setParam("skipComments", "false");
		request.setPart("data",
				new ByteArrayInputStream(removeInvalidJsonComments(
						IOUtils.toString(ImportAction001Test.class.getResourceAsStream("ImportActionTest-request.json")))
								.getBytes("UTF-8")),
				"resolved-issues.json");

		// Local call - Search - First page
		final Map<String, String> localRequestParamsToCheckPageOne = new HashMap<>();
		localRequestParamsToCheckPageOne.put("projectKeys", "my-project-key");
		localRequestParamsToCheckPageOne.put("additionalFields", "comments");
		localRequestParamsToCheckPageOne.put("p", "1");
		localRequestParamsToCheckPageOne.put("ps", String.valueOf(sonarIssuesSearchPageSize));

		final Issues.SearchWsResponse.Builder localRequestResponsePageOne = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageOne.setPaging(Common.Paging.newBuilder().setTotal(9).setPageIndex(1).setPageSize(6));
		
		// MATCHED ISSUE (NO ACTION - same status and same resolution)
		localRequestResponsePageOne
				.addIssues(Issues.Issue.newBuilder()
						.setKey("TotaleAndereKey4")
						.setRule("xml:IllegalTabCheck")
						.setComponent("nl.future-edge.sonarqube.plugins:myBranch:sonar-issueresolver-plugin:pom.xml")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(4).setStartOffset(0))

						.setHash("hash0")
						.setMessage("message")
						
						.setStatus("RESOLVED")
						.setResolution("FALSE-POSITIVE")
						.setSeverity(Severity.INFO)

						.setAssignee("admin")

						.setCreationDate("14/01/2019")
						.setComments(Issues.Comments.newBuilder().addComments(Issues.Comment.newBuilder().setMarkdown("Comment one") ) ) );

		
		// UNMATCHED ISSUE
		localRequestResponsePageOne
			.addIssues(Issues.Issue.newBuilder()
				.setKey("TotaleAndereKey14")
				.setRule("xml:IllegalTabCheck")
				.setComponent("nl.future-edge.sonarqube.plugins:myBranch:sonar-issueresolver-plugin:pom.xml")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(14).setStartOffset(0))
				.setHash("hash1")
				.setMessage("message")
				
				.setStatus("OPEN")
				.setSeverity(Severity.INFO)
				
				.setAssignee("admin")
				.setCreationDate("14/01/2019")
				
				.setComments(Issues.Comments.newBuilder().addComments(Issues.Comment.newBuilder().setMarkdown("Comment one") ) ));

		// MATCHED ISSUE (CONFIRM; NO ASSIGN)
		localRequestResponsePageOne
			.addIssues(Issues.Issue.newBuilder()
					.setKey("TotaleAndereKey55")
					.setRule("squid:S3776")
					.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
					.setTextRange(Common.TextRange.newBuilder().setStartLine(55).setStartOffset(16))
					
					.setHash("hash2")
					.setMessage("message")

					.setStatus("OPEN")
					.setSeverity(Severity.INFO)
					
					.setAssignee("admin")
					.setCreationDate("14/01/2019")
					 );

					
		// MATCHED ISSUE (UNCONFIRM)
		localRequestResponsePageOne
				.addIssues(Issues.Issue.newBuilder()
						.setKey("TotaleAndereKey56")
						.setRule("squid:S3776")
						.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(56).setStartOffset(16))

						.setHash("hash3")
						.setMessage("message")

						.setStatus("CONFIRMED")
						.setSeverity(Severity.INFO)
						
						.setAssignee("unknown")
						.setCreationDate("14/01/2019")
						 );


		// MATCHED ISSUE (REOPEN)
		localRequestResponsePageOne.addIssues(Issues.Issue.newBuilder()
				.setKey("TotaleAndereKey57")
				.setRule("squid:S3776")
				.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(57).setStartOffset(16))

				.setHash("hash4")
				.setMessage("message")

				.setStatus("RESOLVED")
				.setSeverity(Severity.INFO)
				
				.setAssignee("admin")

				.setCreationDate("14/01/2019")
				 );


		// MATCHED ISSUE (RESOLVED FIXED)
		localRequestResponsePageOne.addIssues(Issues.Issue.newBuilder()
				.setKey("TotaleAndereKey58")
				.setRule("squid:S3776")
				.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(58).setStartOffset(16))

				.setHash("hash5")
				.setMessage("message")

				.setStatus("OPEN")
				.setSeverity(Severity.INFO) 
				
				.setAssignee("")
				.setCreationDate("14/01/2019")
				);
		
		localRequestResponsePageOne.addComponents(Issues.Component.newBuilder()
				.setKey("nl.future-edge.sonarqube.plugins:myBranch:sonar-issueresolver-plugin:pom.xml")
				.setLongName("pom.xml"));
		
		localRequestResponsePageOne.addComponents(Issues.Component.newBuilder()
				.setKey("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setLongName("src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java"));
		
		request.mockLocalRequest("api/issues/search", localRequestParamsToCheckPageOne,
				localRequestResponsePageOne.build().toByteArray());

		// Local call - Search - Second page
		final Map<String, String> localRequestParamsToCheckPageTwo = new HashMap<>();
		localRequestParamsToCheckPageTwo.put("projectKeys", "my-project-key");
		localRequestParamsToCheckPageTwo.put("additionalFields", "comments");

		localRequestParamsToCheckPageTwo.put("p", "2");
		localRequestParamsToCheckPageTwo.put("ps", "6");

		final Issues.SearchWsResponse.Builder localRequestResponsePageTwo = Issues.SearchWsResponse.newBuilder();
		localRequestResponsePageTwo.setPaging(Common.Paging.newBuilder().setTotal(9).setPageIndex(2).setPageSize(6));
		
		// MATCHED ISSUE (RESOLVE FALSE-POSITIVE)
		localRequestResponsePageTwo
			.addIssues(Issues.Issue.newBuilder()
				.setKey("TotaleAndereKey59")
				.setRule("squid:S3776")
				.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setTextRange(Common.TextRange.newBuilder().setStartLine(59).setStartOffset(16))

				.setHash("hash6")
				.setMessage("message")
				
				.setStatus("REOPENED")
				.setSeverity(Severity.INFO)
				
				.setAssignee("")
				.setCreationDate("14/01/2019")
				 );


		// MATCHED ISSUE (RESOLVE WONTFIX; ADD COMMENT)
		localRequestResponsePageTwo
				.addIssues(Issues.Issue.newBuilder()
						.setKey("TotaleAndereKey60")
						.setRule("squid:S3776")
						.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(60).setStartOffset(16))

						.setHash("hash7")
						.setMessage("message")

						.setStatus("CONFIRMED")
						.setSeverity(Severity.INFO)
						
						.setAssignee("admin")
						.setCreationDate("14/01/2019")
						
						.setComments(Issues.Comments.newBuilder().addComments(Issues.Comment.newBuilder().setMarkdown("Comment one") ) ));


		// MATCHED ISSUE (MATCH FAILURE)
		localRequestResponsePageTwo
				.addIssues(Issues.Issue.newBuilder()
						.setKey("TotaleAndereKey61")
						.setRule("squid:S3776")
						.setComponent("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
						.setTextRange(Common.TextRange.newBuilder().setStartLine(61).setStartOffset(16))

						.setHash("hash8")
						.setMessage("message")
						
						.setStatus("RESOLVED")
						.setResolution("WONTFIX")
						.setSeverity(Severity.INFO)
						
						.setAssignee("admin")
						.setCreationDate("14/01/2019")
						 );


		localRequestResponsePageTwo.addComponents(Issues.Component.newBuilder()
				.setKey("nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:myBranch:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java")
				.setLongName("src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java"));
		
		request.mockLocalRequest("api/issues/search", localRequestParamsToCheckPageTwo,
				localRequestResponsePageTwo.build().toByteArray());

		// Local call - MATCHED ISSUE (NO ACTION; ASSIGN) - Assign
		final Map<String, String> localRequestParamsAssign4 = new HashMap<>();
		localRequestParamsAssign4.put("issue", "TotaleAndereKey4");
		localRequestParamsAssign4.put("assignee", "admin");

		request.mockLocalRequest("api/issues/assign", localRequestParamsAssign4,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (CONFIRM; NO ASSIGN)
		final Map<String, String> localRequestParamsTransition55 = new HashMap<>();
		localRequestParamsTransition55.put("issue", "TotaleAndereKey55");
		localRequestParamsTransition55.put("transition", "confirm");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition55,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (UNCONFIRM; REASSIGN)
		final Map<String, String> localRequestParamsTransition56 = new HashMap<>();
		localRequestParamsTransition56.put("issue", "TotaleAndereKey56");
		localRequestParamsTransition56.put("transition", "unconfirm");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition56,
				Issues.Operation.newBuilder().build().toByteArray());

		final Map<String, String> localRequestParamsAssign56 = new HashMap<>();
		localRequestParamsAssign56.put("issue", "TotaleAndereKey56");
		localRequestParamsAssign56.put("assignee", "unknown");

		// 400 is here an HTTP response
		request.mockLocalRequest("api/issues/assign", localRequestParamsAssign56, 400,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (REOPEN)
		final Map<String, String> localRequestParamsTransition57 = new HashMap<>();
		localRequestParamsTransition57.put("issue", "TotaleAndereKey57");
		localRequestParamsTransition57.put("transition", "reopen");

		// 400 is here an HTTP response
		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition57, 400,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (RESOLVE FIXED)
		final Map<String, String> localRequestParamsTransition58 = new HashMap<>();
		localRequestParamsTransition58.put("issue", "TotaleAndereKey58");
		localRequestParamsTransition58.put("transition", "resolve");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition58,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (RESOLVE FALSE-POSITIVE)
		final Map<String, String> localRequestParamsTransition59 = new HashMap<>();
		localRequestParamsTransition59.put("issue", "TotaleAndereKey59");
		localRequestParamsTransition59.put("transition", "falsepositive");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition59,
				Issues.Operation.newBuilder().build().toByteArray());

		// Local call - MATCHED ISSUE (RESOLVE WONTFIX; ADD COMMENT)
		final Map<String, String> localRequestParamsTransition60 = new HashMap<>();
		localRequestParamsTransition60.put("issue", "TotaleAndereKey60");
		localRequestParamsTransition60.put("transition", "wontfix");

		request.mockLocalRequest("api/issues/do_transition", localRequestParamsTransition60,
				Issues.Operation.newBuilder().build().toByteArray());

		final Map<String, String> localRequestParamsAddComment60a = new HashMap<>();
		localRequestParamsAddComment60a.put("issue", "TotaleAndereKey60");
		localRequestParamsAddComment60a.put("text", "Comment two");

		request.mockLocalRequest("api/issues/add_comment", localRequestParamsAddComment60a,
				Issues.Operation.newBuilder().build().toByteArray());

		final Map<String, String> localRequestParamsAddComment60b = new HashMap<>();
		localRequestParamsAddComment60b.put("issue", "TotaleAndereKey60");
		localRequestParamsAddComment60b.put("text", "Comment three");

		request.mockLocalRequest("api/issues/add_comment", localRequestParamsAddComment60b, 
				Issues.Operation.newBuilder().build().toByteArray());

		//================================================
		
		final Map<String, String> localRequestBaseParamsToCheckPageOneBis = new HashMap<>();
		localRequestBaseParamsToCheckPageOneBis.put("component", "my-project-key");
		localRequestBaseParamsToCheckPageOneBis.put("metricKeys", "violations");
		localRequestBaseParamsToCheckPageOneBis.put("qualifiers", "DIR");
		
		final WsMeasures.ComponentTreeWsResponse.Builder localRequestBaseResponsePageOneBis = WsMeasures.ComponentTreeWsResponse.newBuilder() ;
		localRequestBaseResponsePageOneBis.addComponents(WsMeasures.Component.newBuilder().setKey("component"));
		
		request.mockLocalRequest("api/measures/component_tree" , localRequestBaseParamsToCheckPageOneBis, 
				localRequestBaseResponsePageOneBis.build().toByteArray());
		
		//==============================================================
		// Response
		final MockResponse response = new MockResponse();

		// Execute
		final ImportAction subject = new ImportAction();
		subject.handle(request, response);

		//request.validateNoMoreLocalRequests();

		// Validate
		final String result = new String(response.result(), "UTF-8");
		LOGGER.info(result);
	
		Assert.assertEquals(
				"{\"preview\":false," 
				+ "\"nbDirectories\":1,\"targetIssues\":9,"
				+ "\"issues\":10,\"duplicateKeys\":1,"
				+ "\"matchedIssues\":8,"
				+ "\"tenThousand\":false,"
				+ "\"matchFailures\":[\"Could not determine transition for issue with key 'TotaleAndereKey61'; current status is 'RESOLVED' and resolution is 'WONTFIX'; wanted status is 'RESOLVED' and resolution is 'FALSE-POSITIVE'\"],"
				+ "\"transitionedIssues\":5,\"transitionFailures\":[\"Could not transition issue with key 'TotaleAndereKey57' using transition 'reopen'\"],"
				+ "\"assignedIssues\":0,\"assignFailures\":[],"
				+ "\"commentedIssues\":1,\"commentFailures\":[],"
				
				+ "\"matchingIssues\":["
				+ "{\"longName\":\"pom.xml\",\"rule\":\"xml:IllegalTabCheck\",\"line\":4,\"hash\":\"hash0\",\"message\":\"message\",\"status\":\"RESOLVED\",\"resolution\":\"FALSE-POSITIVE\",\"severity\":\"INFO\",\"transitioned\":false,\"assigned\":false,\"commented\":false},"
				+ "{\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"rule\":\"squid:S3776\",\"line\":55,\"hash\":\"hash2\",\"message\":\"message\",\"status\":\"OPEN\",\"resolution\":\"\",\"severity\":\"INFO\",\"transitioned\":true,\"assigned\":false,\"commented\":false},"
				+ "{\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"rule\":\"squid:S3776\",\"line\":56,\"hash\":\"hash3\",\"message\":\"message\",\"status\":\"CONFIRMED\",\"resolution\":\"\",\"severity\":\"INFO\",\"transitioned\":true,\"assigned\":false,\"commented\":false},"
				
				+ "{\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"rule\":\"squid:S3776\",\"line\":57,\"hash\":\"hash4\",\"message\":\"message\",\"status\":\"RESOLVED\",\"resolution\":\"\",\"severity\":\"INFO\",\"transitioned\":false,\"assigned\":false,\"commented\":false},"
				+ "{\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"rule\":\"squid:S3776\",\"line\":58,\"hash\":\"hash5\",\"message\":\"message\",\"status\":\"OPEN\",\"resolution\":\"\",\"severity\":\"INFO\",\"transitioned\":true,\"assigned\":false,\"commented\":false},"
				+ "{\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"rule\":\"squid:S3776\",\"line\":59,\"hash\":\"hash6\",\"message\":\"message\",\"status\":\"REOPENED\",\"resolution\":\"\",\"severity\":\"INFO\",\"transitioned\":true,\"assigned\":false,\"commented\":false},"
				+ "{\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"rule\":\"squid:S3776\",\"line\":60,\"hash\":\"hash7\",\"message\":\"message\",\"status\":\"CONFIRMED\",\"resolution\":\"\",\"severity\":\"INFO\",\"transitioned\":true,\"assigned\":false,\"commented\":true},"
				+ "{\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"rule\":\"squid:S3776\",\"line\":61,\"hash\":\"hash8\",\"message\":\"message\",\"status\":\"RESOLVED\",\"resolution\":\"WONTFIX\",\"severity\":\"INFO\",\"transitioned\":false,\"assigned\":false,\"commented\":false}"
				+ "]"
				+ "}",
				result);
		
	}

	private String removeInvalidJsonComments(String json) {
		String result = json.replaceAll("(?m)//.*$", "");
		System.out.println(result);
		return result;
	}

	
	//@Test(expected=IllegalStateException.class)
	/*@Test
	public void invalidData() throws IOException {
		final MockRequest request = new MockRequest();
		request.setParam("projectKey", "my-project-key");
		request.setParam("target-branch", "release-IBB10");
		request.setParam("preview", "false");
		request.setPart("data", new ByteArrayInputStream("{\"version\":1}".getBytes("UTF-8")), "resolved-issues.json");

		// Response
		final MockResponse response = new MockResponse();

		// Execute
		final ImportAction subject = new ImportAction();
		subject.handle(request, response);
	}
	
	*/
	
	
	
}
