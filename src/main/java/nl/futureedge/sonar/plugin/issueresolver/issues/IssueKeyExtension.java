package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Common.Severity;
import org.sonarqube.ws.Issues.Component;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

public class IssueKeyExtension extends IssueKey {

	// transitioned has two meaning: in preview mode it means ready to transition, otherwise it means transitioned (done with response = 200)
	private static final String NAME_transitioned = "transitioned";
	private static final String NAME_assigned = "assigned";
	private static final String NAME_commented = "commented";

	private static final String NAME_STATUS = "status";
	private static final String NAME_RESOLUTION = "resolution";
	private static final String NAME_SEVERITY = "severity";

	private boolean transitioned = false;
	private boolean assigned = false;
	private boolean commented = false;

	private String status = "";
	private String resolution = "";
	private final Severity severity;


	/**
	 * Constructor 
	 * 
	 * @param longName
	 * @param rule
	 * @param line
	 * @param hash
	 * @param message
	 */
	public IssueKeyExtension(final String longName, final String rule, final int line, 
			final String hash, final String message, final String status, final String resolution, final Severity severity) {

		super(longName, rule, line, hash, message);

		this.transitioned = false;
		this.assigned = false;
		this.commented = false;

		this.status = status;
		this.resolution = resolution;
		this.severity = severity;

	}

	/**
	 * Construct key from search.
	 * 
	 * @param issue
	 *            issue from search
	 * @return issue key
	 */
	public static IssueKeyExtension fromIssue(final Issue issue, List<Component> components) {

		final Component component = findComponent(components, issue.getComponent());
		return new IssueKeyExtension(component.getLongName(), issue.getRule(), issue.getTextRange().getStartLine() , 
				issue.getHash() , issue.getMessage() , issue.getStatus() , issue.getResolution(), issue.getSeverity());

	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != getClass()) {
			return false;
		}
		final IssueKey that = (IssueKey) obj;
		// specific CASE for line = 0 or hash is empty
		//if (  (this.line == 0) || (this.hash.length() == 0) ) {
		//	return false;
		//}
		return new EqualsBuilder()
				.append(this.longName, that.longName)
				.append(this.rule, that.rule)
				.append(this.line, that.line)
				.isEquals();
	}

	/**
	 * Construct key from export data.
	 * 
	 * @param reader
	 *            json reader
	 * @return issue key
	 * @throws IOException
	 *             IO errors in underlying json reader
	 */
	public static IssueKeyExtension read(final JsonReader reader) throws IOException {

		// order of the tags must be the same as the writer
		return new IssueKeyExtension(
				
				reader.prop(NAME_LONG_NAME), 
				reader.prop(NAME_RULE),
				reader.propAsInt(NAME_LINE), 
				
				reader.prop(NAME_HASH), 
				reader.prop(NAME_MESSAGE),
				
				// from here writer of data
				reader.prop(NAME_STATUS),
				reader.prop(NAME_RESOLUTION),
				Severity.valueOf(reader.prop(NAME_SEVERITY))
				);
	}

	/**
	 * Write key to export data.
	 * 
	 * @param writer
	 *            json writer
	 * @param b 
	 */
	public void write(final JsonWriter writer, boolean b) {

		// warning - order is important see IssueKeyTest

		writer.prop(NAME_LONG_NAME, this.longName);
		writer.prop(NAME_RULE, this.rule);
		writer.prop(NAME_LINE, this.line);
		writer.prop(NAME_HASH, this.hash);
		writer.prop(NAME_MESSAGE, this.message);

		writer.prop(NAME_STATUS, this.status);
		writer.prop(NAME_RESOLUTION, this.resolution);
		writer.prop(NAME_SEVERITY, this.severity.name());

		if (b) {
			writer.prop(NAME_transitioned, this.transitioned);
			writer.prop(NAME_assigned, this.assigned);
			writer.prop(NAME_commented, this.commented);
		}

	}

	/**
	 * @return the transitioned
	 */
	public boolean isTransitioned() {
		return this.transitioned;
	}

	/**
	 * @param transitioned the transitioned to set
	 */
	public void setTransitioned(boolean transitioned) {
		this.transitioned = transitioned;
	}

	/**
	 * @return the assigned
	 */
	public boolean isAssigned() {
		return this.assigned;
	}

	/**
	 * @param assigned the assigned to set
	 */
	public void setAssigned(boolean assigned) {
		this.assigned = assigned;
	}

	/**
	 * @return the commented
	 */
	public boolean isCommented() {
		return this.commented;
	}

	/**
	 * @param commented the commented to set
	 */
	public void setCommented(boolean commented) {
		this.commented = commented;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @return the resolution
	 */
	public String getResolution() {
		return resolution;
	}

	/**
	 * @param resolution the resolution to set
	 */
	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

}
