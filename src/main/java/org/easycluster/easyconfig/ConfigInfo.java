package org.easycluster.easyconfig;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ConfigInfo implements Serializable {

	private static final long	serialVersionUID	= 1L;

	private String				dataId;
	private String				group;
	private String				configureString;

	public String getDataId() {
		return dataId;
	}

	public void setDataId(String dataId) {
		this.dataId = dataId;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getConfigureString() {
		return configureString;
	}

	public void setConfigureString(String configureString) {
		this.configureString = configureString;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.dataId);
		builder.append(this.group);
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ConfigInfo)) {
			return false;
		}
		ConfigInfo rhs = (ConfigInfo) obj;
		return new EqualsBuilder().append(dataId, rhs.dataId).append(group, rhs.group).isEquals();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
