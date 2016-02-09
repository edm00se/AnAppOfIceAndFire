package org.openntf.xsp.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.ibm.xsp.model.DataObject;

public abstract class AbstractMapModel implements Serializable, DataObject {
	private static final long serialVersionUID = 1L;
	private Map<Object, Object> values;

	public Class<?> getType(final Object key) {
		Class<?> result = null;
		if (getValues().containsKey(key)) {
			Object value = getValues().get(key);
			if (value != null) {
				result = value.getClass();
			}
		}
		return result;
	}

	public Object getValue(final Object key) {
		return getValues().get(key);
	}

	protected Map<Object, Object> getValues() {
		if (values == null) {
			values = new HashMap<Object, Object>();
		}
		return values;
	}

	public boolean isReadOnly(final Object key) {
		return false;
	}

	public void setValue(final Object key, final Object value) {
		getValues().put(key, value);
	}

}
