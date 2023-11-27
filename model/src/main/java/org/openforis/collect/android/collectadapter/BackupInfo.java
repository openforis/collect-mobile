package org.openforis.collect.android.collectadapter;

import org.openforis.collect.Collect;
import org.openforis.collect.utils.Dates;
import org.openforis.commons.versioning.Version;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Properties;

/**
 * 
 * @author S. Ricci
 *
 */
public class BackupInfo {

	private static final String TIMESTAMP_PROP = "timestamp";
	private static final String DATE_PROP = "date";
	private static final String COLLECT_VERSION_PROP = "collect_version";
	private static final String COLLECT_MOBILE_VERSION_PROP = "collect_mobile_version";
	private Version collectVersion;
	private Version collectMobileVersion;
	private Date timestamp;

	public BackupInfo(String collectMobileVersion) {
		this.collectVersion = Collect.VERSION;
		this.collectMobileVersion = collectMobileVersion == null ? null : new Version(collectMobileVersion);
		this.timestamp = new Date();
	}
	
	public void store(OutputStream os) throws IOException {
		Properties props = toProperties();
		props.store(os, null);
	}
	
	protected Properties toProperties() {
		Properties props = new Properties();
		props.setProperty(COLLECT_VERSION_PROP, collectVersion.toString());
		props.setProperty(COLLECT_MOBILE_VERSION_PROP, collectMobileVersion.toString());
		props.setProperty(TIMESTAMP_PROP, Dates.formatDateTime(timestamp));
		return props;
	}

	public static BackupInfo parse(InputStream is) throws IOException {
		Properties props = new Properties();
		props.load(is);
		return parse(props);
	}
	
	protected static BackupInfo parse(Properties props) {
		BackupInfo info = new BackupInfo(null);
		info.collectVersion = new Version(props.getProperty(COLLECT_VERSION_PROP));
		info.collectMobileVersion = new Version(props.getProperty(COLLECT_MOBILE_VERSION_PROP));
		String timestampString = props.getProperty(TIMESTAMP_PROP);
		if ( timestampString == null ) {
			info.timestamp = Dates.parseDate(props.getProperty(DATE_PROP));
		} else {
			info.timestamp = Dates.parseDateTime(timestampString);
		}
		return info;
	}
	public Date getTimestamp() {
		return timestamp;
	}
	
	public void setTimestamp(Date date) {
		this.timestamp = date;
	}
	
	public Version getCollectVersion() {
		return collectVersion;
	}

	public void setCollectVersion(Version collectVersion) {
		this.collectVersion = collectVersion;
	}

	public Version getCollectMobileVersion() {
		return collectMobileVersion;
	}

	public void setCollectMobileVersion(Version collectMobileVersion) {
		this.collectMobileVersion = collectMobileVersion;
	}
}
