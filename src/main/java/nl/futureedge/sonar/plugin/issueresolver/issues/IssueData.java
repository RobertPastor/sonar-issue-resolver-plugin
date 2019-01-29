package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Issues.Comment;
import org.sonarqube.ws.Issues.Issue;
import org.sonarqube.ws.Common.Severity;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

/**
 * Issue data; used to resolve issues.
 */
public final class IssueData {


	private static final String NAME_STATUS = "status";
	private static final String NAME_RESOLUTION = "resolution";
	private static final String NAME_SEVERITY = "severity";

	private static final String NAME_ASSIGNEE = "assignee";
	private static final String NAME_CREATION_DATE = "creationDate";
	private static final String NAME_COMMENTS = "comments";

	private final String status;
	private final String resolution;
	private final Severity severity;

	private final String assignee;
	private final String creationDate;
	private final List<String> comments;

	
	public String toString() {
		
		return NAME_STATUS + " - " + this.status + " - " + NAME_RESOLUTION + " - " + this.resolution;
	}
	/**
	 * Constructor.
	 * 

	 * @param assignee
	 *            assignee

	 * @param creationDate
	 * 			  creationDate

	 * @param comments
	 *            comments
	 */
	private IssueData(final String status, final String resolution, final Severity severity, final String assignee, 
			final String creationDate, 
			final List<String> comments) {

		this.status = status;
		this.resolution = resolution;
		this.severity = severity;

		this.assignee = assignee;
		this.creationDate = creationDate;
		this.comments = comments;
	}

	/**
	 * Construct data from search.
	 * 
	 * Reads the markdown format of comments.
	 * 
	 * @param issue
	 *            issue from search
	 * @return issue data
	 */
	public static IssueData fromIssue(final Issue issue) {

		final List<String> comments = new ArrayList<>();

		for (final Comment comment : issue.getComments().getCommentsList()) {
			comments.add(comment.getMarkdown());
		}

		return new IssueData( issue.getStatus(), issue.getResolution(), issue.getSeverity(), issue.getAssignee(), 
				issue.getCreationDate(), 
				comments);
	}

	/**
	 * Construct data from export data.
	 * 
	 * @param reader
	 *            json reader
	 * @return issue data
	 * @throws IOException
	 *             IO errors in underlying json reader
	 */
	public static IssueData read(final JsonReader reader) throws IOException {

		// 21st January 2019 - line and message are moved to IssueKey
		return new IssueData(
				reader.prop(NAME_STATUS), 
				reader.prop(NAME_RESOLUTION), 
				Severity.valueOf(reader.prop(NAME_SEVERITY)),
				
				reader.prop(NAME_ASSIGNEE), 
				reader.prop(NAME_CREATION_DATE),
				reader.propValues(NAME_COMMENTS)
				);		
	}

	/**
	 * Write data to export data.
	 * 
	 * @param writer
	 *            json writer
	 */
	public void write(final JsonWriter writer) {

		writer.prop(NAME_STATUS, status);
		writer.prop(NAME_RESOLUTION, resolution);
		writer.prop(NAME_SEVERITY, this.severity.name());

		writer.prop(NAME_ASSIGNEE, assignee);
		
		writer.prop(NAME_CREATION_DATE, creationDate);

		// tag name followed by an array
		writer.name(NAME_COMMENTS);
		writer.beginArray();
		writer.values(comments);
		writer.endArray();
	}

	/**
	 * Status.
	 * 
	 * @return status
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Resolution.
	 * 
	 * @return resolution
	 */
	public String getResolution() {
		return resolution;
	}

	/**
	 * Assignee.
	 * 
	 * @return assignee
	 */
	public String getAssignee() {
		return assignee;
	}

	/**
	 * CreationDate.
	 * 
	 * @return CreationDate
	 */
	public String getCreationDate() {
		return creationDate;
	}

	/**
	 * Comments (markdown format).
	 * 
	 * @return list of comments
	 */
	public List<String> getComments() {
		return comments;
	}


}
