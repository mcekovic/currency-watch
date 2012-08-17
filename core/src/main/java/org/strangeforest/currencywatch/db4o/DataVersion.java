package org.strangeforest.currencywatch.db4o;

import com.finsoft.util.*;

public class DataVersion implements Comparable<DataVersion> {

	private int version;

	public static final int CURRENT_VERSION = 3;

	public DataVersion() {}

	public DataVersion(int version) {
		this.version = version;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}


	// Object Methods

	@Override public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof DataVersion)) return false;
		DataVersion version = (DataVersion)o;
		return this.version == version.version;
	}

	@Override public int hashCode() {
		return version;
	}

	@Override public String toString() {
		return String.valueOf(version);
	}

	@Override public int compareTo(DataVersion v) {
		return PrimitiveUtil.compare(version, v.version);
	}
}
