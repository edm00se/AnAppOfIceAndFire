package org.openntf.xsp.model;

import javax.faces.context.FacesContext;

import org.apache.commons.beanutils.PropertyUtils;

import com.ibm.commons.util.StringUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;

public abstract class AbstractSmartDocumentModel extends AbstractDocumentMapModel {
	private static final long serialVersionUID = 1L;
	private boolean editMode;

	public AbstractSmartDocumentModel() {
		super();
		String action = ExtLibUtil.readParameter(FacesContext.getCurrentInstance(), "action");
		if (StringUtil.isEmpty(getUnid()) || "edit".equalsIgnoreCase(action)) {
			edit();
		}
	}

	public void edit() {
		setEditMode(true);
	}

	@Override
	public Class<?> getType(final Object key) {
		Class<?> result = null;
		try {
			result = PropertyUtils.getPropertyType(this, key.toString());
		} catch (Throwable t) {
			result = super.getType(key);
		}
		return result;
	}

	protected <T> T getTypedValue(final String name, final Class<T> type) {
		return type.cast(getValues().get(name));
	}

	@Override
	public Object getValue(final Object key) {
		Object result = null;
		try {
			result = PropertyUtils.getProperty(this, key.toString());
		} catch (Throwable t) {
			result = super.getValue(key);
		}
		return result;
	}

	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public boolean isReadOnly(final Object key) {
		boolean result = !isEditMode() || super.isReadOnly(key);
		try {
			if (!result) {
				result = PropertyUtils.getWriteMethod(PropertyUtils.getPropertyDescriptor(this, key.toString())) == null;
			}
		} catch (Throwable t) {
			result = super.isReadOnly(key);
		}
		return result;
	}

	public void setEditMode(final boolean editMode) {
		this.editMode = editMode;
	}

	@Override
	public void setValue(final Object key, final Object value) {
		try {
			PropertyUtils.setProperty(this, key.toString(), value);
		} catch (Throwable t) {
			super.setValue(key, value);
		}
	}
}
