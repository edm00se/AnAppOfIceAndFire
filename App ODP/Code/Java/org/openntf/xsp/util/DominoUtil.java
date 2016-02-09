package org.openntf.xsp.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lotus.domino.Base;
import lotus.domino.Database;
import lotus.domino.DateRange;
import lotus.domino.DateTime;
import lotus.domino.Document;
import lotus.domino.DocumentCollection;
import lotus.domino.Item;
import lotus.domino.MIMEEntity;
import lotus.domino.MIMEHeader;
import lotus.domino.NoteCollection;
import lotus.domino.Session;
import lotus.domino.Stream;

import com.ibm.xsp.extlib.util.ExtLibUtil;

/*
 * Nearly all of this class borrows heavily from:
 * https://github.com/OpenNTF/org.openntf.domino/blob/master/org.openntf.domino/src/org/openntf/domino/utils/DominoUtils.java
 */
public class DominoUtil {
	@SuppressWarnings("unchecked")
	public static void incinerate(final Object... objects) {
		for (Object eachObject : objects) {
			if (eachObject != null) {
				if (eachObject instanceof Base) {
					Base dominoObject = (Base) eachObject;
					try {
						dominoObject.recycle();
					} catch (Exception e) {
					}
				} else if (eachObject instanceof Map) {
					Set<Map.Entry> entries = ((Map) eachObject).entrySet();
					for (Map.Entry<?, ?> entry : entries) {
						incinerate(entry.getKey(), entry.getValue());
					}
				} else if (eachObject instanceof Collection) {
					Iterator i = ((Collection) eachObject).iterator();
					while (i.hasNext()) {
						Object obj = i.next();
						incinerate(obj);
					}
				} else if (eachObject.getClass().isArray()) {
					try {
						Object[] objs = (Object[]) eachObject;
						for (Object ao : objs) {
							incinerate(ao);
						}
					} catch (Throwable t) {
						// who cares?
					}
				}
			}
		}
	}
	
	public static Document next(final Document current, final DocumentCollection collection) {
		Document result = null;
		try {
			result = collection.getNextDocument(current);
		} catch (Throwable t) {
			LogUtil.trace(t);
		} finally {
			incinerate(current);
		}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static Object restoreState(final Document doc, final String itemName) throws Throwable {
		Session session = ExtLibUtil.getCurrentSession();
		boolean convertMime = session.isConvertMime();
		session.setConvertMime(false);
		Object result = null;
		Stream mimeStream = session.createStream();
		MIMEEntity entity = doc.getMIMEEntity(itemName);
		if (entity == null) {
			return null;
		}
		entity.getContentAsBytes(mimeStream);
		ByteArrayOutputStream streamOut = new ByteArrayOutputStream();
		mimeStream.getContents(streamOut);
		mimeStream.recycle();
		byte[] stateBytes = streamOut.toByteArray();
		ByteArrayInputStream byteStream = new ByteArrayInputStream(stateBytes);
		ObjectInputStream objectStream;
		if (entity.getHeaders().toLowerCase().contains("content-encoding: gzip")) {
			GZIPInputStream zipStream = new GZIPInputStream(byteStream);
			objectStream = new ObjectInputStream(zipStream);
		} else {
			objectStream = new ObjectInputStream(byteStream);
		}
		// There are three potential storage forms: Externalizable,
		// Serializable, and StateHolder, distinguished by type or header
		if (entity.getContentSubType().equals("x-java-externalized-object")) {
			Class<Externalizable> externalizableClass = (Class<Externalizable>) Class.forName(entity.getNthHeader("X-Java-Class")
							.getHeaderVal());
			Externalizable restored = externalizableClass.newInstance();
			restored.readExternal(objectStream);
			result = restored;
		} else {
			Object restored = objectStream.readObject();
			// But wait! It might be a StateHolder object or Collection!
			MIMEHeader storageScheme = entity.getNthHeader("X-Storage-Scheme");
			MIMEHeader originalJavaClass = entity.getNthHeader("X-Original-Java-Class");
			if ((storageScheme != null) && storageScheme.getHeaderVal().equals("StateHolder")) {
				Class<?> facesContextClass = Class.forName("javax.faces.context.FacesContext");
				Method getCurrentInstance = facesContextClass.getMethod("getCurrentInstance");
				Class<?> stateHoldingClass = Class.forName(originalJavaClass.getHeaderVal());
				Method restoreStateMethod = stateHoldingClass.getMethod("restoreState", facesContextClass, Object.class);
				result = stateHoldingClass.newInstance();
				restoreStateMethod.invoke(result, getCurrentInstance.invoke(null), restored);
			} else if ((originalJavaClass != null) && originalJavaClass.getHeaderVal().endsWith(".domino.DocumentCollection")) {
				/*
				 * WARNING: Nathan says this bit might still be buggy...
				 */
				try {
					String[] unids = (String[]) restored;
					Database db = doc.getParentDatabase();
					DocumentCollection docCollection = db.createDocumentCollection();
					for (String unid : unids) {
						docCollection.addDocument(db.getDocumentByUNID(unid));
					}
					result = docCollection;
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if ((originalJavaClass != null) && originalJavaClass.getHeaderVal().endsWith(".domino.NoteCollection")) {
				String[] unids = (String[]) restored;
				Database db = doc.getParentDatabase();
				NoteCollection noteCollection = db.createNoteCollection(false);
				for (String unid : unids) {
					noteCollection.add(db.getDocumentByUNID(unid));
				}
				result = noteCollection;
			} else {
				result = restored;
			}
		}
		entity.recycle();
		session.setConvertMime(convertMime);
		return result;
	}
	
	public static void saveState(final Serializable object, final Document doc, final String itemName) throws Throwable {
		saveState(object, doc, itemName, true, null);
	}
	
	public static void saveState(final Serializable object, final Document doc, final String itemName, final boolean compress,
					final Map<String, String> headers) throws Throwable {
		if (object == null) {
			System.out.println("Ignoring attempt to save MIMEBean value of null");
			return;
		}
		Session session = ExtLibUtil.getCurrentSession();
		boolean convertMime = session.isConvertMime();
		session.setConvertMime(false);
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream objectStream = compress ? new ObjectOutputStream(new GZIPOutputStream(byteStream)) : new ObjectOutputStream(
						byteStream);
		String contentType = null;
		// Prefer externalization if available
		if (object instanceof Externalizable) {
			((Externalizable) object).writeExternal(objectStream);
			contentType = "application/x-java-externalized-object";
		} else {
			objectStream.writeObject(object);
			contentType = "application/x-java-serialized-object";
		}
		objectStream.flush();
		objectStream.close();
		Stream mimeStream = session.createStream();
		MIMEEntity previousState = doc.getMIMEEntity(itemName);
		MIMEEntity entity = null;
		if (previousState == null) {
			Item itemChk = doc.getFirstItem(itemName);
			if (itemChk != null) {
				itemChk.remove();
			}
			entity = doc.createMIMEEntity(itemName);
		} else {
			entity = previousState;
		}
		byte[] bytes = byteStream.toByteArray();
		ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
		mimeStream.setContents(byteIn);
		entity.setContentFromBytes(mimeStream, contentType, MIMEEntity.ENC_NONE);
		MIMEHeader javaClass = entity.getNthHeader("X-Java-Class");
		if (javaClass == null) {
			javaClass = entity.createHeader("X-Java-Class");
		}
		try {
			javaClass.setHeaderVal(object.getClass().getName());
		} catch (Throwable t) {
			t.printStackTrace();
		}
		MIMEHeader contentEncoding = entity.getNthHeader("Content-Encoding");
		if (compress) {
			if (contentEncoding == null) {
				contentEncoding = entity.createHeader("Content-Encoding");
			}
			contentEncoding.setHeaderVal("gzip");
			contentEncoding.recycle();
		} else {
			if (contentEncoding != null) {
				contentEncoding.remove();
				contentEncoding.recycle();
			}
		}
		javaClass.recycle();
		if (headers != null) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				MIMEHeader paramHeader = entity.getNthHeader(entry.getKey());
				if (paramHeader == null) {
					paramHeader = entity.createHeader(entry.getKey());
				}
				paramHeader.setHeaderVal(entry.getValue());
				paramHeader.recycle();
			}
		}
		entity.recycle();
		mimeStream.recycle();
		session.setConvertMime(convertMime);
	}
	
	protected static Vector<Object> toDominoFriendly(final Collection<?> values, final Document context) throws IllegalArgumentException {
		java.util.Vector<Object> result = new java.util.Vector<Object>();
		for (Object value : values) {
			result.add(toDominoFriendly(value));
		}
		return result;
	}
	
	public static Object toDominoFriendly(final Object value) {
		/*
		 * A weak subset of the auto-conversion done by the OpenNTF Domino API:
		 * http://github.com/OpenNTF/org.openntf.domino/blob/master/org.openntf.domino
		 */
		if (value == null) {
			return "";
		}
		if ((value instanceof DateTime) || (value instanceof DateRange) || (value instanceof Item)) {
			// already safe to store
			return value;
		} else if ((value instanceof Integer) || (value instanceof Double) || (value instanceof String)) {
			// already safe to store
			return value;
		} else if (value instanceof Boolean) {
			return (Boolean) value ? "1" : "0";
		}
		// Now for the illegal-but-convertible types
		if (value instanceof Number) {
			return ((Number) value).doubleValue();
		} else if (value instanceof Date) {
			Session session = ExtLibUtil.getCurrentSession();
			try {
				return session.createDateTime((Date) value);
			} catch (Throwable t) {
				LogUtil.trace(t);
				return null;
			}
		} else if (value instanceof Calendar) {
			Session session = ExtLibUtil.getCurrentSession();
			try {
				return session.createDateTime((Calendar) value);
			} catch (Throwable t) {
				LogUtil.trace(t);
				return null;
			}
		} else if (value instanceof CharSequence) {
			return value.toString();
		}
		throw new IllegalArgumentException("Cannot convert to Domino friendly from type " + value.getClass().getName());
	}
}
