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
	private static final String NAME_ASSIGNEE = "assignee";
	private static final String NAME_COMMENTS = "comments";
	// 14th January 2019 - use hash
	private static final String NAME_HASH = "hash";
	private static final String NAME_MESSAGE = "message";
	private static final String NAME_CREATION_DATE = "creationDate";
	private static final String NAME_SEVERITY = "severity";

	private final String status;
	private final String resolution;
	private final String assignee;
	private final String hash;
	private final String message;
	private final String creationDate;
	private final Severity severity;
	
	private final List<String> comments;

	/**
	 * Constructor.
	 * 
	 * @param status
	 *            status
	 * @param resolution
	 *            resolution
	 * @param assignee
	 *            assignee
	 * @param hash
	 * 			  hash
	 * @param message
	 * 			  message
	 * @param creationDate
	 * 			  creationDate
	 * @param severity
	 * 			  severity
	 * @param comments
	 *            comments
	 */
	private IssueData(final String status, final String resolution, final String assignee, 
			final String hash, final String message, final String creationDate, final Severity severity,
			final List<String> comments) {
		this.status = status;
		this.resolution = resolution;
		this.assignee = assignee;
		// 14th January 2019 - export hash and message and creation Date
		this.hash= hash;
		this.message = message;
		this.creationDate = creationDate;
		this.severity = severity;
		
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

		return new IssueData(issue.getStatus(), issue.getResolution(), issue.getAssignee(), 
				issue.getHash(), issue.getMessage(), issue.getCreationDate(), issue.getSeverity(),
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
		return new IssueData(reader.prop(NAME_STATUS), reader.prop(NAME_RESOLUTION), reader.prop(NAME_ASSIGNEE), reader.prop(NAME_HASH),
				reader.prop(NAME_MESSAGE), reader.prop(NAME_CREATION_DATE), Severity.valueOf(reader.prop(NAME_SEVERITY)),
				reader.propValues(NAME_COMMENTS));
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
		writer.prop(NAME_ASSIGNEE, assignee);
		// 14th January 2019 - export hash
		writer.prop(NAME_HASH, hash);
		writer.prop(NAME_MESSAGE, message);
		writer.prop(NAME_CREATION_DATE, creationDate);
		writer.prop(NAME_SEVERITY, severity.name());

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
	 * Comments (markdown format).
	 * 
	 * @return list of comments
	 */
	public List<String> getComments() {
		return comments;
	}
	
	/**
	 * Hash.
	 * 
	 * @return hash
	 */
	public String getHash() {
		return hash;
	}
	
	/**
	 * Message.
	 * 
	 * @return message
	 */
	public String getMessage() {
		return message;
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
	 * Severity.
	 * 
	 * @return severity
	 */
	public Severity getSeverity() {
		return severity;
	}

}
