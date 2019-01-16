package nl.futureedge.sonar.plugin.issueresolver.ws;

import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.server.ws.WebService;
import org.sonar.api.server.ws.WebService.Action;
import org.sonar.api.server.ws.WebService.Controller;

public class IssueResolverWebServiceTest {

	@Test
	public void test() {
		final IssueResolverWebService subject = new IssueResolverWebService(new ExportAction(), new ImportAction(),
				new UpdateAction());

		final WebService.Context context = new WebService.Context();
		Assert.assertEquals(0, context.controllers().size());
		subject.define(context);
		Assert.assertEquals(1, context.controllers().size());

		// Controller
		Controller controller = context.controller("api/issueresolver");
		Assert.assertNotNull(controller);
		Assert.assertEquals(3, controller.actions().size());

		// Export
		Action exportAction = controller.action("export");
		Assert.assertNotNull(exportAction);
		Assert.assertTrue(exportAction.handler() instanceof ExportAction);
		Assert.assertEquals(2, exportAction.params().size());
		Assert.assertNotNull(exportAction.param("projectKey"));
		Assert.assertNotNull(exportAction.param("target-branch"));

		// Import
		Action importAction = controller.action("import");
		Assert.assertNotNull(importAction);
		Assert.assertTrue(importAction.handler() instanceof ImportAction);
		
		// 14-January 2019 - number of parameters moved from 5 to 6 => add the project branch
		Assert.assertEquals(6, importAction.params().size());
		
		Assert.assertNotNull(importAction.param("projectKey"));
		Assert.assertNotNull(importAction.param("target-branch"));
		
		Assert.assertNotNull(importAction.param("preview"));
		Assert.assertNotNull(importAction.param("data"));
		Assert.assertNotNull(importAction.param("skipAssign"));
		Assert.assertNotNull(importAction.param("skipComments"));

		// Update
		Action updateAction = controller.action("update");
		Assert.assertNotNull(updateAction);
		Assert.assertTrue(updateAction.handler() instanceof UpdateAction);
		
		// 14 January 2019 - moved 5 params to 6 params - adding target-branch
		Assert.assertEquals(6, updateAction.params().size());
		Assert.assertNotNull(updateAction.param("fromProjectKey"));
		Assert.assertNotNull(updateAction.param("projectKey"));
		
		// 14 January 2019 - moved 5 params to 6 params - adding target-branch
		Assert.assertNotNull(updateAction.param("target-branch"));
		
		Assert.assertNotNull(updateAction.param("preview"));
		Assert.assertNotNull(updateAction.param("skipAssign"));
		Assert.assertNotNull(updateAction.param("skipComments"));
	}

}
