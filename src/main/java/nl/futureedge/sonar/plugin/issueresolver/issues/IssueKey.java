package nl.futureedge.sonar.plugin.issueresolver.issues;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.sonar.api.utils.text.JsonWriter;
import org.sonarqube.ws.Issues.Component;
import org.sonarqube.ws.Issues.Issue;

import nl.futureedge.sonar.plugin.issueresolver.json.JsonReader;

/**
 * Issue key; used to match issues.
 */
public class IssueKey {

	protected static final String NAME_LONG_NAME = "longName";
	protected static final String NAME_RULE = "rule";
	//17th January 2019 - use has instead of line
	protected static final String NAME_LINE = "line";
	protected static final String NAME_HASH = "hash";
	protected static final String NAME_MESSAGE = "message";

	protected String longName;
	protected String rule;
	protected int line;
	protected String hash;
	protected String message;

	/**
	 * Constructor.
	 * 
	 * * @param longName = component
	 *            longName
	 * @param rule
	 *            rule
	 * @param line
	 *            line
	 * @param hash
	 *            hash
	 * @param message
	 *            message
	 */
	public IssueKey(final String longName, final String rule, final int line, final String hash, final String message) {
		
		this.longName = longName;
		this.rule = rule;
		this.line = line;
		this.hash = hash;
		this.message = message;
	}

	/**
	 * Construct key from search.
	 * 
	 * @param issue
	 *            issue from search
	 * @return issue key
	 */
	public static IssueKey fromIssue(final Issue issue, List<Component> components) {
		
		final Component component = findComponent(components, issue.getComponent());
		return new IssueKey(component.getLongName(), issue.getRule(), issue.getTextRange().getStartLine(), issue.getHash() , issue.getMessage());
		
	}

	/*
	 * why is this method as no private attribute used
	 */
	protected static Component findComponent(final List<Component> components, final String key) {
		for (final Component component : components) {
			if (key.equals(component.getKey())) {
				return component;
			}
		}

		throw new IllegalStateException("Component of issue not found");
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
	public static IssueKey read(final JsonReader reader) throws IOException {
		return new IssueKey(
				
				reader.prop(NAME_LONG_NAME), 
				reader.prop(NAME_RULE),
				reader.propAsInt(NAME_LINE), 
				reader.prop(NAME_HASH), 
				reader.prop(NAME_MESSAGE));
	}

	/**
	 * Write key to export data.
	 * 
	 * @param writer
	 *            json writer
	 */
	public void write(final JsonWriter writer) {
		
		// warning - order is important see IssueKeyTest
		writer.prop(NAME_LONG_NAME, longName);
		writer.prop(NAME_RULE, rule);
		writer.prop(NAME_LINE, line);
		writer.prop(NAME_HASH, hash);
		writer.prop(NAME_MESSAGE, message);

	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37).append(this.longName).append(this.rule).append(this.line).toHashCode();
		
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

	@Override
	public String toString() {
		return "IssueKey [longName=" + this.longName + ", rule=" + this.rule + ", line=" + this.line + "]";
	}

}
