package com.westeros.servlets;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.Session;

import org.apache.commons.io.IOUtils;
import org.openntf.xsp.util.DominoUtil;

import com.google.gson.Gson;
import com.westeros.app.Utils;
import com.westeros.model.HouseModel;

/**
 * The document related /houses/{:unid} servlet actions. This class
 * exposes a GET method for the existing document, a PUT method
 * for updating an existing document, and a DELETE method, for
 * deleting an existing document.
 * 
 * @author Eric McCormick, @edm00se
 *
 */
public class HouseRecord {
	
	private static String recAllowedMethods = "GET, PUT, DELETE";
	
	/**
	 * GET method of actions to take against the Houses Servlet, for
	 * the specified House, by UNID.
	 * 
	 * @param unid String
	 * @param req HttpServletRequest
	 * @param res HttpServletResponse
	 * @param facesContext FacesContext
	 * @param out ServletOutputStream
	 * @throws IOException
	 */
	public static void doGet(String unid, HttpServletRequest req, HttpServletResponse res,
					FacesContext facesContext, ServletOutputStream out) throws IOException {
		
		try {
			
			// create a House model object in memory and load its contents
			HouseModel myHouse = new HouseModel();
			myHouse.load(unid);
			
			//return the contents in JSON to the OutputStream
			
			// com.ibm.commons way
			/*
			 * out.print(JsonGenerator.toJson(JsonJavaFactory.instanceEx,
			 * myHouse));
			 */
			
			// GSON way
			Gson g = new Gson();
			out.print( g.toJson(myHouse) );
			
			res.setStatus(200);
			res.addHeader("Allow", recAllowedMethods);
			
		} catch(Exception e) {
			res.setStatus(500);
			res.addHeader("Allow", recAllowedMethods);
			Map<String,Object> errOb = new HashMap<String,Object>();
			errOb.put("error", true);
			errOb.put("errorMsg", e.toString());
			Gson g = new Gson();
			out.print(g.toJson(errOb));
		}
	}
	
	/**
	 * PUT method of actions to take against the Houses Servlet, for
	 * the specified House, by UNID.
	 * 
	 * @param unid String
	 * @param req HttpServletRequest
	 * @param res HttpServletResponse
	 * @param facesContext FacesContext
	 * @param out ServletOutputStream
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void doPut(String unid, HttpServletRequest req, HttpServletResponse res,
					FacesContext facesContext, ServletOutputStream out) throws IOException {
		
		try {
			
			// GET existing
			HouseModel exHouse = new HouseModel();
			exHouse.load(unid);
			exHouse.setEditMode(true);
			
			ServletInputStream is = req.getInputStream();
			String reqStr = IOUtils.toString(is);
			
			Gson g = new Gson();
			
			// setting the keys/values into the tmpNwHouse Map
			Map<String, String> tmpNwHouse = g.fromJson(reqStr, HashMap.class);
			// compare/update
			for(Map.Entry<String, String> pair : tmpNwHouse.entrySet()) {
				String curProp = pair.getKey();
				String curVal = pair.getValue();
				if( exHouse.getValue(curProp) != curVal ) {
					exHouse.setValue(curProp, curVal);
				}
			}
			
			// done setting new values back into the existing object
			boolean success = exHouse.save();
			
			if( success ) {
				res.setStatus(200);
			}else {
				res.setStatus(400);
				if( tmpNwHouse!=null ) {
					out.println("{\"error\":true,\"errorMsg\":\"WTF?\",\"receivedData\":\""+tmpNwHouse.toString()+"\"}");
				}else {
					out.println("{\"error\":true,\"errorMsg\":\"WTF?\"}");
				}
			}
			
			res.addHeader("Allow", recAllowedMethods);
			
		} catch(Exception e) {
			res.setStatus(500);
			res.addHeader("Allow", recAllowedMethods);
			Map<String,Object> errOb = new HashMap<String,Object>();
			errOb.put("error", true);
			errOb.put("errorMsg", e.toString());
			Gson g = new Gson();
			out.print(g.toJson(errOb));
		}
		
	}
	
	/**
	 * DELETE method of actions to take against the Houses Servlet, for
	 * the specified House, by UNID.
	 * 
	 * @param unid String
	 * @param req HttpServletRequest
	 * @param res HttpServletResponse
	 * @param facesContext FacesContext
	 * @param out ServletOutputStream
	 * @throws IOException
	 */
	public static void doDelete(String unid, HttpServletRequest req, HttpServletResponse res,
					FacesContext facesContext, ServletOutputStream out) throws IOException {
		
		Session s = (Session) facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, "session");
		Document houseDoc;
		try {
			houseDoc = s.getCurrentDatabase().getDocumentByUNID(unid);
			boolean success = houseDoc.remove(true);
			houseDoc.recycle();
			if( success ) {
				res.setStatus(200);
			}else {
				res.setStatus(400);
			}
			res.addHeader("Allow", recAllowedMethods);
		} catch (NotesException e) {
			res.setStatus(500);
			Gson g = new Gson();
			Map<String,Object> errData = new HashMap<String,Object>();
			errData.put("error", true);
			errData.put("errorMessage", e.toString());
			errData.put("stackTrace", e.getStackTrace());
			out.print(g.toJson(errData));
		}
		
	}
	
	/**
	 * Method of actions to take against the Houses Servlet, across the
	 * implementations, for unexpected VERB.
	 * 
	 * @param req
	 * @param res
	 * @param facesContext
	 * @param out
	 * @throws Exception
	 */
	public static void handleUnexpectedVerb(HttpServletRequest req,
					HttpServletResponse res, FacesContext facesContext,
					ServletOutputStream out) {
		res.setStatus(405); // method not allowed
		res.addHeader("Allow", recAllowedMethods);
	}
	
	public static void handleRecordNotFound(HttpServletRequest req,
					HttpServletResponse res, FacesContext facesContext,
					ServletOutputStream out) {
		res.setStatus(404); // not found
		res.addHeader("Allow", recAllowedMethods);
	}
	
	/**
	 * @param unid String
	 * @return boolean
	 */
	public static boolean isValidUnid(String unid) {
		boolean status = false;
		Database db = Utils.getCurrentDatabase();
		Document tmpDoc = null;
		try {
			tmpDoc = db.getDocumentByUNID(unid);
			if((tmpDoc != null) && tmpDoc.isValid() && !tmpDoc.isDeleted()) {
				status = true;
			}
		} catch (NotesException e) {}
		DominoUtil.incinerate(db, tmpDoc);
		return status;
		
	}
	
}
