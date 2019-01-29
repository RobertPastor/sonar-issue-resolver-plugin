package nl.futureedge.sonar.plugin.issueresolver.ws;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKey;
import nl.futureedge.sonar.plugin.issueresolver.issues.IssueKeyExtension;

/**
 * Import result.
 */
public final class ImportResult {

	private boolean preview = false;
	
	private int nbIssues = 0;
	private int duplicateKeys = 0;
	private int matchedIssues = 0;
	private List<String> matchFailures = new ArrayList<>();
	private int transitionedIssues = 0;
	private List<String> transitionFailures = new ArrayList<>();
	private int assignedIssues = 0;
	private List<String> assignFailures = new ArrayList<>();
	private int commentedIssues = 0;
	private List<String> commentFailures = new ArrayList<>();
	
	// 25 January 2019 - temporary 
	private boolean moreThanTenThousandIssues = false;
	
	// improve results -> show issues that are matching
	private List<IssueKeyExtension> issuesKeysExtensions = new ArrayList<IssueKeyExtension>();
	
	// number of directories
	private int numberOfDirectories = 0;
	// number of issues in the target project branch
	private int numberOfTargetIssues = 0;

	public void setPreview(final boolean preview) {
		this.preview = preview;
	}

	public void registerIssue() {
		nbIssues++;
	}

	public int getNbIssues() {
		return nbIssues;
	}

	public void registerDuplicateKey() {
		duplicateKeys++;
	}

	public int getDuplicateKeys() {
		return duplicateKeys;
	}

	public void registerMatchedIssue() {
		matchedIssues++;
	}

	public void registerMatchFailure(String failure) {
		matchFailures.add(failure);
	}

	public void registerTransitionedIssue() {
		transitionedIssues++;
	}

	public void registerTransitionFailure(String failure) {
		transitionFailures.add(failure);
	}

	public void registerAssignedIssue() {
		assignedIssues++;
	}

	public void registerAssignFailure(String failure) {
		assignFailures.add(failure);
	}

	public void registerCommentedIssue() {
		commentedIssues++;
	}

	public void registerCommentFailure(String failure) {
		commentFailures.add(failure);
	}

	public void write(final JsonWriter writer) {
		
		// warning - these are keys used by the javascript in src/main/resources/static
		writer.beginObject();
		writer.prop("preview", preview);
		
		// number of directories in the target project branch
		writer.prop("nbDirectories", this.numberOfDirectories);
		
		// number of issues read in the target project branch
		writer.prop("targetIssues", this.numberOfTargetIssues);
		
		// number of issues read in JSON file or in SOURCE project branch
		writer.prop("issues", this.nbIssues);
		writer.prop("duplicateKeys", this.duplicateKeys);
		writer.prop("matchedIssues", this.matchedIssues);
		writer.prop("tenThousand", this.moreThanTenThousandIssues);
		
		// name of the key followed by an array
		writer.name("matchFailures");
		writer.beginArray();
		writer.values(matchFailures);
		writer.endArray();	
		
		writer.prop("transitionedIssues", transitionedIssues);
		
		writer.name("transitionFailures");
		writer.beginArray();
		writer.values(transitionFailures);
		writer.endArray();
		
		writer.prop("assignedIssues", assignedIssues);
		
		writer.name("assignFailures");
		writer.beginArray();
		writer.values(assignFailures);
		writer.endArray();
		
		writer.prop("commentedIssues", commentedIssues);
		
		writer.name("commentFailures");
		writer.beginArray();
		writer.values(commentFailures);
		writer.endArray();
		
		// write matching issues
		// Warning : this name (matchingIssues) is used in the result.js to build the results HTML table
		writer.name("matchingIssues");
		writer.beginArray();
		Iterator<IssueKeyExtension> iter = issuesKeysExtensions.iterator();
		
		while(iter.hasNext()) {
			writer.beginObject();
			IssueKeyExtension issueKey = iter.next();
			issueKey.write(writer, true);
			writer.endObject();
		}
		writer.endArray();
		
		writer.endObject();
	}

	public void recordToBeModifiedIssue(final IssueKeyExtension issueKey) {
		this.issuesKeysExtensions.add(issueKey);
	}

	public void recordMoreThanTenThousandIssues(final boolean moreThanTenThousandIssues) {
		this.moreThanTenThousandIssues = moreThanTenThousandIssues;
		
	}

	public void setNumberOfDirectories(int size) {
		this.numberOfDirectories = size;
		
	}

	public void recordNumberOfTargetIssues(int issuesCount) {
		this.numberOfTargetIssues = issuesCount;		
	}
}