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
import org.sonarqube.ws.Common.TextRange;
import org.sonarqube.ws.Issues.Component;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

public class IssueKeyExtensionTest {
	
	private static final Logger LOGGER = Loggers.get(IssueKeyTest.class);

	@Test
	public void test() throws IOException {
		final Issue issue = ReflectionTestUtils.build(Issue.class, "rule_", "test:rule001", "component_",
				"nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java",
				"textRange_", ReflectionTestUtils.build(TextRange.class, "startLine_", 13, "startOffset_", 65),
				"hash_", "hash", "message_", "message");
		
		final Component component = ReflectionTestUtils.build(Component.class, 
				"key_", "nl.future-edge.sonarqube.plugins:sonar-issueresolver-plugin:src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java",
				"longName_", "src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java");
		
		final List<Component> components = Arrays.asList(component);

		final IssueKeyExtension key = IssueKeyExtension.fromIssue(issue, components);

		final String json;
		try (final StringWriter writer = new StringWriter()) {
			final JsonWriter jsonWriter = JsonWriter.of(writer);
			
			jsonWriter.beginObject();
			// Issue Key is writing file Path, rule, line, hash and message
			key.write(jsonWriter, false);
			jsonWriter.endObject();
			jsonWriter.close();

			json = writer.toString();
		}
		
		LOGGER.info(json);
		
		Assert.assertEquals(
				"{\"longName\":\"src/main/java/nl/futureedge/sonar/plugin/issueresolver/issues/IssueKey.java\","
				+ "\"rule\":\"test:rule001\",\"line\":13,\"hash\":\"hash\",\"message\":\"message\","
				+ "\"status\":\"\",\"resolution\":\"\",\"severity\":\"INFO\""
				+ "}",
				json);

		LOGGER.info("json is as expected");
		
		final IssueKeyExtension readKey;
		try (final ByteArrayInputStream bais = new ByteArrayInputStream(json.getBytes("UTF-8"));
				final JsonReader reader = new JsonReader(bais)) {
			reader.beginObject();
			readKey = IssueKeyExtension.read(reader);
			reader.endObject();
		}

		Assert.assertTrue("same hash code", (key.hashCode() == readKey.hashCode()) );
		LOGGER.info("same hash code");

		Assert.assertFalse("key is not null" , key.equals(null));
		LOGGER.info("key is not null");

		Assert.assertTrue("same keys using equals" , key.equals(readKey));
		LOGGER.info("same keys using equals");

		Assert.assertFalse(key.equals(new Object()));
	}
}
