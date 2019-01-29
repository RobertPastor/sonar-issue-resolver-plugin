package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.text.JsonWriter;
import org.sonar.wsclient.issue.Issues;
import org.sonarqube.ws.Common.Severity;
import org.sonarqube.ws.Issues.Comment;
import org.sonarqube.ws.Issues.Comments;
import org.sonarqube.ws.Issues.Component;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;
import nl.futureedge.sonar.plugin.issueresolver.ws.ImportAction;

public class IssueDataTest {
	
	private static final Logger LOGGER = Loggers.get(IssueDataTest.class);


	@Test
	public void test() throws IOException {
		final Issue issue = ReflectionTestUtils.build(Issue.class, "status_", "RESOLVED", "resolution_", "FALSE-POSITIVE",
				"severity_", Severity.INFO.getNumber(),
				"assignee_", "admin", "line_", 63, "message_", "message", "creationDate_", "14/01/2019", 
				"component_", "nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java",
				"comments_",
				ReflectionTestUtils.build( Comments.class, "comments_",
						Arrays.asList(ReflectionTestUtils.build(Comment.class, "markdown_", "Comment one"),
								ReflectionTestUtils.build(Comment.class, "markdown_", "Comment two") )  ) );
		
		final Component component = ReflectionTestUtils.build(Component.class, 
				"key_", "nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java",
				"longName_", "src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java");
		final List<Component> components = Arrays.asList(component);

		final IssueKey issueKey = IssueKey.fromIssue(issue, components);
		
		final IssueData data = IssueData.fromIssue(issue);
		LOGGER.info(data.toString());
		
		Assert.assertEquals("RESOLVED", data.getStatus());
		
		Assert.assertEquals("admin", data.getAssignee());
		Assert.assertEquals("14/01/2019", data.getCreationDate());
		Assert.assertEquals(Arrays.asList("Comment one", "Comment two"), data.getComments());

		final String json;
		try (final StringWriter writer = new StringWriter()) {
			final JsonWriter jsonWriter = JsonWriter.of(writer);
			jsonWriter.beginObject();
			issueKey.write(jsonWriter);
			data.write(jsonWriter);
			jsonWriter.endObject();
			jsonWriter.close();

			json = writer.toString();
		}
		LOGGER.info(json);
		
		Assert.assertEquals("{" 
			+	"\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\",\"rule\":\"\",\"line\":0,\"hash\":\"\",\"message\":\"message\","
			+   "\"status\":\"RESOLVED\",\"resolution\":\"FALSE-POSITIVE\",\"severity\":\"INFO\","
			+	"\"assignee\":\"admin\",\"creationDate\":\"14/01/2019\",\"comments\":[\"Comment one\",\"Comment two\"]}", json);

		final IssueData readData;
		
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes("UTF-8"));
				final JsonReader reader = new JsonReader(bais)) {
			reader.beginObject();
			IssueKey.read(reader);
			readData = IssueData.read(reader);
			reader.endObject();
		}
		
		Assert.assertEquals("admin", readData.getAssignee());
		Assert.assertEquals("14/01/2019", readData.getCreationDate());
		Assert.assertEquals(Arrays.asList("Comment one", "Comment two"), readData.getComments());
	}
}
