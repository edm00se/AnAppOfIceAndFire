package com.westeros.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.VariableResolver;

import lotus.domino.Database;
import lotus.domino.NotesException;
import lotus.domino.Session;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewEntryCollection;
import lotus.domino.ViewNavigator;

import com.ibm.domino.xsp.module.nsf.NotesContext;
import com.ibm.xsp.application.DesignerApplicationEx;
import com.ibm.xsp.component.UIViewRootEx;
import com.ibm.xsp.designer.context.XSPContext;

/**
 * Utils helper Class for various methods; Managed Bean capable.
 * 
 * Includes the Java version of DbColumn and DbLookup from XSnippets. src:
 * http:/
 * /openntf.org/XSnippets.nsf/snippet.xsp?id=pure-java-version-of-dblookup-
 * dbcolumn-with-cache-sort-and-unique
 * 
 * Also added the com.timtripcony.util.JSFUtil class, with modification by Paul
 * Withers.
 * 
 */
@SuppressWarnings("unchecked")
public class Utils implements Serializable {

	private static final long serialVersionUID = -5577520390584674902L;

	/**
	 * Dbcolumn - Java version of @Dbcolumn sort = false unique = false cache =
	 * false
	 * 
	 * @param db
	 * @param viewname
	 * @param columnnr
	 * @return ArrayList<String> with lookup results
	 * @throws NotesException
	 */
	public static ArrayList<String> Dbcolumn(final Database db,
			final String viewname, final int columnNr) throws NotesException {
		return DbcolumnCore(db, viewname, columnNr, new Params());
	}

	/**
	 * Dbcolumn - Java version of @Dbcolumn uses Params object
	 * 
	 * @param db
	 * @param viewname
	 * @param columnNr
	 * @param params
	 * @return ArrayList<String> with lookup results
	 * @throws NotesException
	 */
	public static ArrayList<String> Dbcolumn(final Database db,
			final String viewname, final int columnNr, final Params params)
			throws NotesException {
		return DbcolumnCore(db, viewname, columnNr, params);
	}

	/**
	 * Dbcolumn - Java version of @Dbcolumn sort = false unique = false cache =
	 * false
	 * 
	 * @param server
	 * @param dbpath
	 * @param viewname
	 * @param columnNr
	 * @return ArrayList<String> with lookup results
	 * @throws NotesException
	 */
	public static ArrayList<String> Dbcolumn(final String server,
			final String dbpath, final String viewname, final int columnNr)
			throws NotesException {
		Database db = getSession().getDatabase(server, dbpath);
		return DbcolumnCore(db, viewname, columnNr, new Params());
	}

	/**
	 * Dbcolumn - Java version of @Dbcolumn uses Params object
	 * 
	 * @param server
	 * @param dbpath
	 * @param viewname
	 * @param columnNr
	 * @param params
	 * @return ArrayList<String> with lookup results
	 * @throws NotesException
	 */
	public static ArrayList<String> Dbcolumn(final String server,
			final String dbpath, final String viewname, final int columnNr,
			final Params params) throws NotesException {
		Database db = getSession().getDatabase(server, dbpath);
		return DbcolumnCore(db, viewname, columnNr, params);
	}

	/**
	 * DbcolumnCore - private core function of @dbcolumn
	 * 
	 * @param db
	 * @param viewname
	 * @param columnnr
	 * @param params
	 * @return
	 * @throws NotesException
	 */
	private static ArrayList<String> DbcolumnCore(final Database db,
			final String viewname, final int columnNr, final Params params)
			throws NotesException {
		// generate cachekey
		String cachekey = Utils.replaceAll("dbc_" + db.getFilePath() + "_"
				+ viewname + "_" + String.valueOf(columnNr), " ", "_");

		// caching = if the results are already cached, get them
		if (params.isCache()) {
			if (Utils.sessionScope().containsKey(cachekey)) {
				return (ArrayList<String>) Utils.sessionScope().get(cachekey);
			}
		}

		View vw = db.getView(viewname);
		ViewNavigator vwnav = null;
		ViewEntry vwentry = null;
		ViewEntry vwentrytmp = null;

		// all results will be added to this
		ArrayList<String> results = new ArrayList<String>();

		// disable autoupdate
		vw.setAutoUpdate(false);

		// create viewnavigator
		if (params.getCategory().equals("")) {
			vwnav = vw.createViewNav();
		} else {
			vwnav = vw.createViewNavFromCategory(params.getCategory());
		}

		// setting buffer for fast view retrieval
		vwnav.setBufferMaxEntries(400);

		// peform lookup
		vwentry = vwnav.getFirst();
		while (vwentry != null) {
			results.add(vwentry.getColumnValues().elementAt(columnNr)
					.toString());

			// Get entry and go recycle
			vwentrytmp = vwnav.getNext(vwentry);
			vwentry.recycle();
			vwentry = vwentrytmp;

		}

		// sorting
		if (params.isSort()) {
			results = Utils.sort(results);
		}
		// unique
		if (params.isUnique()) {
			results = Utils.unique(results);
		}
		// caching
		if (params.isCache()) {
			Utils.sessionScope().put(cachekey, results);
		}

		// enable autoupdate
		vw.setAutoUpdate(true);

		return results;
	}

	/**
	 * Dblookup - Java version of @Dblookup uses Params object
	 * 
	 * @param db
	 * @param viewname
	 * @param params
	 * @return ArrayList<String> with lookup results
	 * @throws NotesException
	 */
	public static ArrayList<String> Dblookup(final Database db,
			final String viewname, final Params params) throws NotesException {
		return DblookupCore(db, viewname, params);
	}

	/**
	 * Dblookup - Java version of @Dblookup sort = false unique = false cache =
	 * false
	 * 
	 * @param db
	 * @param viewname
	 * @param params
	 * @return ArrayList<String> with lookup results
	 * @throws NotesException
	 */
	public static ArrayList<String> Dblookup(final String server,
			final String dbpath, final String viewname, final String key,
			final String returnfield) throws NotesException {
		Database db = getSession().getDatabase(server, dbpath);

		Params params = new Params();
		params.setKey(key);
		params.setReturnfield(returnfield);

		return DblookupCore(db, viewname, params);
	}

	/**
	 * Dblookup - Java version of @Dblookup sort = false unique = false cache =
	 * false
	 * 
	 * @param db
	 * @param viewname
	 * @param key
	 * @param returnfield
	 * @return ArrayList<String> with lookup results
	 * @throws NotesException
	 */
	public static ArrayList<String> Dblookup(final Database db,
			final String viewname, final String key, final String returnfield)
			throws NotesException {
		Params params = new Params();
		params.setKey(key);
		params.setReturnfield(returnfield);

		return DblookupCore(db, viewname, params);
	}

	/**
	 * DblookupCore - private core function of @dblookup
	 * 
	 * @param db
	 * @param viewname
	 * @param params
	 * @return
	 * @throws NotesException
	 */
	private static ArrayList<String> DblookupCore(final Database db,
			final String viewname, final Params params) throws NotesException {
		// generate cachekey
		String cachekey = Utils.replaceAll("dbl_" + db.getFilePath() + "_"
				+ viewname + "_" + params.getKey() + "_"
				+ params.getReturnfield(), " ", "_");

		// caching = if the results are already cached, get them
		if (params.isCache()) {
			if (Utils.sessionScope().containsKey(cachekey)) {
				return (ArrayList<String>) Utils.sessionScope().get(cachekey);
			}
		}

		View vw = db.getView(viewname);
		ViewEntryCollection vwcoll = null;
		ViewEntry vwentry = null;
		ViewEntry vwentrytmp = null;

		// all results will be added to this
		ArrayList<String> results = new ArrayList<String>();

		// disable autoupdate
		vw.setAutoUpdate(false);

		// create viewnavigator
		if (params.getKey().equals("")) {
			vwcoll = vw.getAllEntries();
		} else {
			vwcoll = vw.getAllEntriesByKey(params.getKey(), true);
		}

		// peform lookup
		vwentry = vwcoll.getFirstEntry();
		while (vwentry != null) {
			if (Utils.isNumber(params.getReturnfield())) {
				// return field is column number
				Object values = vwentry.getColumnValues().elementAt(
						Integer.parseInt(params.getReturnfield()));
				if (values instanceof String) {
					results.add(values.toString());
				} else {
					results.addAll((Vector<String>) values);
				}
			} else {
				// return field is fieldname, only add if it exist on document
				if (vwentry.getDocument().hasItem(params.getReturnfield()) == true) {
					results.addAll(vwentry.getDocument().getItemValue(
							params.getReturnfield()));
				}
			}

			// Get entry and go recycle
			vwentrytmp = vwcoll.getNextEntry(vwentry);
			vwentry.recycle();
			vwentry = vwentrytmp;
		}

		// sorting
		if (params.isSort()) {
			results = Utils.sort(results);
		}
		// unique
		if (params.isUnique()) {
			results = Utils.unique(results);
		}
		// caching
		if (params.isCache()) {
			Utils.sessionScope().put(cachekey, results);
		}

		// enable autoupdate
		vw.setAutoUpdate(true);

		return results;
	}

	/**
	 * Params object is a helper class for passing params to DbColumn and
	 * DbLookup functions
	 * 
	 * @author Ferry Kranenburg 2013
	 * 
	 * 
	 */
	public static class Params implements Serializable {
		private static final long serialVersionUID = 610176852544247762L;
		private boolean sort = false;
		private boolean cache = false;
		private boolean unique = false;
		private String category = "";
		private String returnfield = "";
		private String key = "";

		// constuctor
		public Params() {
		}

		// full constructor for dbcolumn
		public Params(final boolean sort, final boolean cache,
				final boolean unique, final String category) {
			this.setSort(sort);
			this.setCache(cache);
			this.setUnique(unique);
			this.setCategory(category);
		}

		// full constructor for dblookup
		public Params(final boolean sort, final boolean cache,
				final boolean unique, final String key, final String returnfield) {
			this.setSort(sort);
			this.setCache(cache);
			this.setUnique(unique);
			this.setReturnfield(returnfield);
			this.setKey(key);
		}

		public void setSort(final boolean sort) {
			this.sort = sort;
		}

		public boolean isSort() {
			return sort;
		}

		public void setCache(final boolean cache) {
			this.cache = cache;
		}

		public boolean isCache() {
			return cache;
		}

		public void setUnique(final boolean unique) {
			this.unique = unique;
		}

		public boolean isUnique() {
			return unique;
		}

		public void setCategory(final String category) {
			this.category = category;
		}

		public String getCategory() {
			return category;
		}

		public void setReturnfield(final String field) {
			this.returnfield = field;
		}

		public String getReturnfield() {
			return returnfield;
		}

		public void setKey(final String key) {
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	private static Session getSession() {
		FacesContext context = FacesContext.getCurrentInstance();
		return (Session) context.getApplication().getVariableResolver()
		.resolveVariable(context, "session");
	}

	private static Map<String, Object> sessionScope() {
		FacesContext context = FacesContext.getCurrentInstance();
		return (Map<String, Object>) context.getApplication()
		.getVariableResolver().resolveVariable(context, "sessionScope");
	}

	private static ArrayList<String> sort(final ArrayList<String> input) {
		Collections.sort(input);
		return input;
	}

	private static ArrayList<String> unique(final ArrayList<String> input) {
		return new ArrayList<String>(new LinkedHashSet<String>(input));
	}

	private static String replaceAll(final String input,
			final String matchingStr, final String replacementStr) {
		return input.replaceAll(Pattern.quote(matchingStr), replacementStr);
	}

	private static boolean isNumber(final String input) {
		try {
			Integer.parseInt(input);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	/*
	 * <!-- Copyright 2010 Tim Tripcony Licensed under the Apache License,
	 * Version 2.0 (the "License"); you may not use this file except in
	 * compliance with the License. You may obtain a copy of the License at
	 * 
	 * http://www.apache.org/licenses/LICENSE-2.0
	 * 
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
	 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
	 * License for the specific language governing permissions and limitations
	 * under the License -->
	 */
	private static Session _signerSess;

	public static DesignerApplicationEx getApplication() {
		return (DesignerApplicationEx) getFacesContext().getApplication();
	}

	public static Map<String, Object> getApplicationScope() {
		return getServletContext().getApplicationMap();
	}

	public static Map<String, Object> getCompositeData() {
		return (Map<String, Object>) getVariableResolver().resolveVariable(
				getFacesContext(), "compositeData");
	}

	public static Database getCurrentDatabase() {
		return (Database) getVariableResolver().resolveVariable(
				getFacesContext(), "database");
	}

	public static Session getCurrentSession() {
		return (Session) getVariableResolver().resolveVariable(
				getFacesContext(), "session");
	}

	public static FacesContext getFacesContext() {
		return FacesContext.getCurrentInstance();
	}

	public static Map<String, Object> getRequestScope() {
		return getServletContext().getRequestMap();
	}

	public static ExternalContext getServletContext() {
		return getFacesContext().getExternalContext();
	}

	public static Map<String, Object> getSessionScope() {
		return getServletContext().getSessionMap();
	}

	private static VariableResolver getVariableResolver() {
		return getApplication().getVariableResolver();
	}

	public static UIViewRootEx getViewRoot() {
		return (UIViewRootEx) getFacesContext().getViewRoot();
	}

	public static Map<String, Object> getViewScope() {
		return getViewRoot().getViewMap();
	}

	public static XSPContext getXSPContext() {
		return XSPContext.getXSPContext(getFacesContext());
	}

	public static Object resolveVariable(final String variable) {
		return FacesContext.getCurrentInstance().getApplication()
		.getVariableResolver().resolveVariable(
				FacesContext.getCurrentInstance(), variable);
	}

	/**
	 * @since 3.0.0 Added by Paul Withers to get current session as signer
	 */
	public static Session getSessionAsSigner() {
		if (_signerSess == null) {
			_signerSess = NotesContext.getCurrent().getSessionAsSigner();
		} else {
			try {
				@SuppressWarnings("unused")
				boolean pointless = _signerSess.isOnServer();
			} catch (NotesException recycleSucks) {
				// our database object was recycled so we'll need to get it
				// again
				try {
					_signerSess = NotesContext.getCurrent()
					.getSessionAsSigner();
				} catch (Exception e) {

				}
			}
		}
		return _signerSess;
	}
}
