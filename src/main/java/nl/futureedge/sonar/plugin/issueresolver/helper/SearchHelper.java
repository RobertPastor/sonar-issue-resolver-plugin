package nl.futureedge.sonar.plugin.issueresolver.helper;

import java.util.Arrays;
import java.util.Collections;

import org.sonarqube.ws.WsMeasures.Component;
import org.sonarqube.ws.client.issue.SearchWsRequest;
import org.sonarqube.ws.client.measure.ComponentTreeWsRequest;

/**
 * Search functionality.
 */
public final class SearchHelper {
	
	private final static int sonarIssuesSearchPageSize = 500;
	private final static int sonarDirectoriesSearchPageSize = 500;

	private SearchHelper() {
	}

	/**
	 * Create search request for resolved issues.
	 * 
	 * @param projectKey
	 *            project key
	 * @return search request
	 */
	public static SearchWsRequest findIssuesForExport(final String projectKey, String branchName) {

		final SearchWsRequest searchIssuesRequest = new SearchWsRequest();
		searchIssuesRequest.setProjectKeys(Collections.singletonList(projectKey));
		searchIssuesRequest.setAdditionalFields(Collections.singletonList("comments"));

		// 14 January 2019 - additional field for a branch
		if (branchName.length()>0) {
			searchIssuesRequest.setBranch(branchName);
		}

		searchIssuesRequest.setStatuses(Arrays.asList("CONFIRMED", "REOPENED", "RESOLVED"));
		searchIssuesRequest.setPage(1);
		searchIssuesRequest.setPageSize(sonarIssuesSearchPageSize);
		return searchIssuesRequest;
	}

	/**
	 * create an issues search request using a project key, a branch and a directory
	 * @param projectKey
	 * @param branchName
	 * @param directory
	 * @return
	 */
	public static SearchWsRequest findIssuesForImport(final String projectKey, String branchName, Component directory) {

		final SearchWsRequest searchIssuesRequest = new SearchWsRequest();
		
		searchIssuesRequest.setProjectKeys(Collections.singletonList(projectKey));
		searchIssuesRequest.setAdditionalFields(Collections.singletonList("comments"));
		
		// filtering by directory to avoid 10k issues limit
		searchIssuesRequest.setComponentKeys(Collections.singletonList(directory.getKey()));

		// 14 January 2019 - additional field for a branch
		if (branchName.length()>0) {
			searchIssuesRequest.setBranch(branchName);
		}

		searchIssuesRequest.setPage(1);
		searchIssuesRequest.setPageSize(sonarIssuesSearchPageSize);
		return searchIssuesRequest;
	}

	/**
	 * used to retrieve more than 10K violations
	 * @param projectKey
	 * @return
	 */
	public static ComponentTreeWsRequest getDirectoryList(final String projectKey, String branchName) {

		final ComponentTreeWsRequest searchDirectoriesRequest = new ComponentTreeWsRequest();
		searchDirectoriesRequest.setComponent(projectKey);
		searchDirectoriesRequest.setMetricKeys(Collections.singletonList("violations"));
		searchDirectoriesRequest.setQualifiers(Collections.singletonList("DIR"));

		// 14 January 2019 - additional field for a branch
		if (branchName.length()>0) {
			searchDirectoriesRequest.setBranch(branchName);
		}

		searchDirectoriesRequest.setPage(1);
		searchDirectoriesRequest.setPageSize(sonarDirectoriesSearchPageSize);
		return searchDirectoriesRequest;	
	}

}
