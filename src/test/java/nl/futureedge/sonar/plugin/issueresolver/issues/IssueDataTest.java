package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Common.Severity;
import org.sonarqube.ws.Issues.Comment;
import org.sonarqube.ws.Issues.Comments;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

public class IssueDataTest {

	@Test
	public void test() throws IOException {
		final Issue issue = ReflectionTestUtils.build(Issue.class, "status_", "RESOLVED", "resolution_", "FALSE-POSITIVE",
				"assignee_", "admin", "line_", 63, "message_", "message", "creationDate_", "14/01/2019", "severity_", Severity.INFO.getNumber(),
				"comments_",
				ReflectionTestUtils.build(Comments.class, "comments_",
						Arrays.asList(ReflectionTestUtils.build(Comment.class, "markdown_", "Comment one"),
								ReflectionTestUtils.build(Comment.class, "markdown_", "Comment two"))));

		final IssueData data = IssueData.fromIssue(issue);
		Assert.assertEquals("RESOLVED", data.getStatus());
		Assert.assertEquals("FALSE-POSITIVE", data.getResolution());
		Assert.assertEquals("admin", data.getAssignee());
		// 14th January 2019 - line is now moved to Issue Data
		//Assert.assertEquals(63, data.getLine());
		//Assert.assertEquals("message", data.getMessage());
		Assert.assertEquals("14/01/2019", data.getCreationDate());
		Assert.assertEquals(Severity.INFO , data.getSeverity());
		
		Assert.assertEquals(Arrays.asList("Comment one", "Comment two"), data.getComments());

		final String json;
		try (final StringWriter writer = new StringWriter()) {
			final JsonWriter jsonWriter = JsonWriter.of(writer);
			jsonWriter.beginObject();
			data.write(jsonWriter);
			jsonWriter.endObject();
			jsonWriter.close();

			json = writer.toString();
		}
		Assert.assertEquals("{\"status\":\"RESOLVED\",\"resolution\":\"FALSE-POSITIVE\",\"assignee\":\"admin\",\"creationDate\":\"14/01/2019\",\"severity\":\"INFO\",\"comments\":[\"Comment one\",\"Comment two\"]}", json);

		final IssueData readData;
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes("UTF-8"));
				final JsonReader reader = new JsonReader(bais)) {
			reader.beginObject();
			readData = IssueData.read(reader);
			reader.endObject();
		}
		
		Assert.assertEquals("RESOLVED", readData.getStatus());
		Assert.assertEquals("FALSE-POSITIVE", readData.getResolution());
		Assert.assertEquals("admin", readData.getAssignee());
		// 14th January 2019 - export hash
		//Assert.assertEquals(63, readData.getLine());
		//Assert.assertEquals("message", readData.getMessage());
		Assert.assertEquals("14/01/2019", readData.getCreationDate());
		Assert.assertEquals(Severity.INFO, readData.getSeverity());
		
		Assert.assertEquals(Arrays.asList("Comment one", "Comment two"), readData.getComments());
	}
}
