package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.io.IOException;

import org.junit.Test;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import org.junit.Assert;


public class ExportFileNameTest {

	private static final Logger LOGGER = Loggers.get(ExportFileNameTest.class);

	@Test
	public void test() throws IOException {
		
		String fileNameEnd = ExportAction.getCurrentDateTime();
		LOGGER.info(fileNameEnd);
		Assert.assertTrue("file name end contains h for hour separator", fileNameEnd.indexOf("h")>0);
		// assert file name should contain THREE occurrences of a DASH
		int dashCount = fileNameEnd.length() - fileNameEnd.replaceAll("-", "").length();
		LOGGER.info("number of dash= "+ dashCount); 
		Assert.assertTrue("file name contains THREE occurrences of the DASH character", dashCount == 3);
	}

}
