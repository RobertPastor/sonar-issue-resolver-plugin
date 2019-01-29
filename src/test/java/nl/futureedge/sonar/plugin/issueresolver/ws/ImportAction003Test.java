package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class ImportAction003Test {


	private static final Logger LOGGER = Loggers.get(ImportAction003Test.class);


	//@Test(expected=IllegalStateException.class)
	@Test
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

		final String result = new String(response.result(), "UTF-8");
		LOGGER.info(result);
	}

}
