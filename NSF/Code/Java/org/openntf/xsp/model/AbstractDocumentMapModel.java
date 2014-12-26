package org.openntf.xsp.model;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import javax.faces.context.FacesContext;

import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NoteCollection;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.openntf.xsp.util.DominoUtil;
import org.openntf.xsp.util.LogUtil;

import com.ibm.commons.util.StringUtil;
import com.ibm.jscript.debug.ArrayListUtil;
import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.ibm.xsp.model.domino.wrapped.DominoDocument;

public abstract class AbstractDocumentMapModel extends AbstractMapModel {
	public static int MAX_NATIVE_VECTOR_SIZE = 255;
	private static final long serialVersionUID = 1L;
	private String unid;

	public AbstractDocumentMapModel() {
		String documentId = ExtLibUtil.readParameter(FacesContext.getCurrentInstance(), "id");
		if (StringUtil.isNotEmpty(documentId)) {
			load(documentId);
		}
	}

	protected abstract String getFormName();

	/*
	 * Borrowed heavily from
	 * https://github.com/OpenNTF/org.openntf.domino/blob/master/org.openntf.domino/src/org/openntf/domino/impl/Document.java
	 */
	private Object getItemValueMIME(final Document parent, final String name) {
		Object resultObj = null;
		try {
			Session session = ExtLibUtil.getCurrentSession();
			boolean convertMime = session.isConvertMIME();
			session.setConvertMIME(false);
			MIMEEntity entity = parent.getMIMEEntity(name);
			MIMEHeader contentType = entity.getNthHeader("Content-Type");
			if (contentType != null
					&& (contentType.getHeaderVal().equals("application/x-java-serialized-object") || contentType.getHeaderVal().equals(
							"application/x-java-externalized-object"))) {
				// Then it's a MIMEBean
				resultObj = DominoUtil.restoreState(parent, name);
			}
			session.setConvertMIME(convertMime);
		} catch (Throwable t) {
			LogUtil.trace(t);
		}
		return resultObj;
	}

	public String getUnid() {
		return unid;
	}

	@SuppressWarnings("unchecked")
	public void load(final String unid) {
		setUnid(unid);
		Document doc = null;
		MIMEEntity entity = null;
		Item item = null;
		try {
			if (StringUtil.isNotEmpty(getUnid())) {
				doc = ExtLibUtil.getCurrentDatabase().getDocumentByUNID(getUnid());
				DominoDocument wrappedDoc = DominoDocument.wrap(doc.getParentDatabase().getFilePath(), // databaseName
						doc, // Document
						null, // computeWithForm
						null, // concurrencyMode
						false, // allowDeletedDocs
						null, // saveLinksAs
						null // webQuerySaveAgent
						);
				for (Object eachItem : doc.getItems()) {
					if (eachItem instanceof Item) {
						item = (Item) eachItem;
						String itemName = item.getName();
						// certainly not a comprehensive list:
						ArrayList<String> ignoreList = ArrayListUtil
								.stringToArrayList("MIME_Version;$NoteHasNativeMIME;$MIMETrack;$UpdatedBy;$Revisions;$REF;$Conflict");
						if (!ignoreList.contains(itemName)) {
							entity = doc.getMIMEEntity(itemName);
							if (entity != null) {
								setValue(itemName, getItemValueMIME(doc, itemName));
							} else {
								setValue(itemName, wrappedDoc.getValue(itemName));
							}
						}
						DominoUtil.incinerate(item);
					}
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			DominoUtil.incinerate(item, entity, doc);
		}
	}

	protected boolean postSave() {
		return true;
	}

	protected boolean querySave() {
		return true;
	}

	/*
	 * Borrowed heavily from
	 * https://github.com/OpenNTF/org.openntf.domino/blob/master/org.openntf.domino/src/org/openntf/domino/impl/Document.java
	 */
	@SuppressWarnings("unchecked")
	protected Item replaceItemValue(final Document parent, final String itemName, final Object value) {
		Item result = null;
		boolean isNonSummary = false;
		try {
			String noteid_ = parent.getNoteID();
			if (value instanceof Collection || value instanceof Object[]) {
				if (value instanceof Collection) {
					if (((Collection) value).size() > MAX_NATIVE_VECTOR_SIZE) {
						throw new IllegalArgumentException();
					}
				} else if (value instanceof Object[]) {
					if (((Object[]) value).length > MAX_NATIVE_VECTOR_SIZE) {
						throw new IllegalArgumentException();
					}
				}
				Vector<Object> resultList = new Vector<Object>();
				Class<?> objectClass = null;
				long totalStringSize = 0;
				Collection<?> listValue = value instanceof Collection ? (Collection<?>) value : Arrays.asList((Object[]) value);
				for (Object valNode : listValue) {
					Object domNode = DominoUtil.toDominoFriendly(valNode);
					if (domNode != null) {
						if (objectClass == null) {
							objectClass = domNode.getClass();
						} else {
							if (!objectClass.equals(domNode.getClass())) {
								throw new IllegalArgumentException();
							}
						}
						if (domNode instanceof String) {
							totalStringSize += ((String) domNode).length();
							if (totalStringSize > 14000) {
								isNonSummary = true;
							}
							// Escape to serializing if there's too much text data
							// Leave fudge room for multibyte? This is clearly not the best way to do it
							if (totalStringSize > 60000) {
								throw new IllegalArgumentException();
							}
						}
						resultList.add(domNode);
					}
				}
				// If it ended up being something we could store, make note of the original class instead of the list class
				if (!listValue.isEmpty()) {
					// valueClass = (listValue).get(0).getClass();
					MIMEEntity mimeChk = parent.getMIMEEntity(itemName);
					if (mimeChk != null) {
						mimeChk.remove();
					}
				} else {
					throw new IllegalArgumentException();
				}
				result = parent.replaceItemValue(itemName, resultList);
				if (isNonSummary) {
					result.setSummary(false);
				}
				DominoUtil.incinerate(resultList);
			} else {
				Object domNode = DominoUtil.toDominoFriendly(value);
				if (domNode instanceof String && ((String) domNode).length() > 60000) {
					throw new IllegalArgumentException();
				}
				try {
					MIMEEntity mimeChk = parent.getMIMEEntity(itemName);
					if (mimeChk != null) {
						mimeChk.remove();
					}
					result = parent.replaceItemValue(itemName, domNode);
					if (isNonSummary) {
						result.setSummary(false);
					}
				} catch (NotesException nativeError) {
					LogUtil.debug("Native error occured when replacing " + itemName + " item on doc " + noteid_ + " with a value of type "
							+ (domNode == null ? "null" : domNode.getClass().getName()) + " of value " + String.valueOf(domNode));
				}
				DominoUtil.incinerate(domNode);
			}
		} catch (IllegalArgumentException iae) {
			// Then try serialization
			try {
				if (value instanceof Serializable) {
					DominoUtil.saveState((Serializable) value, parent, itemName);
					// result = null;
				} else if (value instanceof DocumentCollection) {
					// NoteIDs would be faster for this and, particularly,
					// NoteCollection, but it should be replica-friendly
					DocumentCollection docs = (DocumentCollection) value;
					String[] unids = new String[docs.getCount()];
					int index = 0;
					Document doc = docs.getFirstDocument();
					while (doc != null) {
						unids[index++] = doc.getUniversalID();
						doc = DominoUtil.next(doc, docs);
					}
					Map<String, String> headers = new HashMap<String, String>(1);
					headers.put("X-Original-Java-Class", "org.openntf.domino.DocumentCollection");
					DominoUtil.saveState(unids, parent, itemName, true, headers);
				} else if (value instanceof NoteCollection) {
					// Maybe it'd be faster to use .getNoteIDs - I'm not sure how
					// the performance compares
					NoteCollection notes = (NoteCollection) value;
					String[] unids = new String[notes.getCount()];
					String noteid = notes.getFirstNoteID();
					int index = 0;
					while (noteid != null && !noteid.isEmpty()) {
						unids[index++] = notes.getUNID(noteid);
						noteid = notes.getNextNoteID(noteid);
					}
					Map<String, String> headers = new HashMap<String, String>(1);
					headers.put("X-Original-Java-Class", value.getClass().getCanonicalName());
					DominoUtil.saveState(unids, parent, itemName, true, headers);
				} else {
					// Check to see if it's a StateHolder
					try {
						Class<?> stateHolderClass = Class.forName("javax.faces.component.StateHolder");
						if (stateHolderClass.isInstance(value)) {
							Class<?> facesContextClass = Class.forName("javax.faces.context.FacesContext");
							Method getCurrentInstance = facesContextClass.getMethod("getCurrentInstance");
							Method saveState = stateHolderClass.getMethod("saveState", facesContextClass);
							Serializable state = (Serializable) saveState.invoke(value, getCurrentInstance.invoke(null));
							Map<String, String> headers = new HashMap<String, String>();
							headers.put("X-Storage-Scheme", "StateHolder");
							headers.put("X-Original-Java-Class", value.getClass().getName());
							DominoUtil.saveState(state, parent, itemName, true, headers);
							result = null;
						} else {
							// Well, we tried.
							throw iae;
						}
					} catch (ClassNotFoundException cnfe) {
						throw iae;
					} catch (Throwable t) {
						LogUtil.trace(t);
					}
				}
			} catch (Throwable t) {
				LogUtil.trace(t);
			}
		} catch (Throwable t) {
			LogUtil.trace(t);
		}
		return result;
	}

	public boolean save() {
		boolean result = false;
		if (querySave()) {
			Document doc = null;
			try {
				if (StringUtil.isEmpty(getUnid())) {
					doc = ExtLibUtil.getCurrentDatabase().createDocument();
					setUnid(doc.getUniversalID());
					doc.replaceItemValue("Form", getFormName());
				} else {
					doc = ExtLibUtil.getCurrentDatabase().getDocumentByUNID(getUnid());
				}
				for (Entry<Object, Object> entry : getValues().entrySet()) {
					String itemName = entry.getKey().toString();
					Item item = replaceItemValue(doc, itemName, entry.getValue());
					DominoUtil.incinerate(item);
				}
				if (doc.save()) {
					result = postSave();
				}
			} catch (Throwable t) {
				t.printStackTrace();
			} finally {
				DominoUtil.incinerate(doc);
			}
		}
		return result;
	}

	public void setUnid(final String unid) {
		this.unid = unid;
	}
}
