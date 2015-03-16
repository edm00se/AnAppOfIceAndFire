package com.westeros.servlets;

import java.lang.reflect.Field;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.Document;
import lotus.domino.Session;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.reflect.FieldUtils;

import com.google.gson.Gson;
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
	
	/**
	 * GET method of actions to take against the Houses Servlet, for
	 * the specified House, by UNID.
	 * 
	 * @param unid String
	 * @param req HttpServletRequest
	 * @param res HttpServletResponse
	 * @param facesContext FacesContext
	 * @param out ServletOutputStream
	 * @throws Exception
	 */
	public static void doGet(String unid, HttpServletRequest req, HttpServletResponse res,
			FacesContext facesContext, ServletOutputStream out)
			throws Exception {
		
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
	 * @throws Exception
	 */
	public static void doPut(String unid, HttpServletRequest req, HttpServletResponse res,
					FacesContext facesContext, ServletOutputStream out)
	throws Exception {
		
		// GET existing
		HouseModel exHouse = new HouseModel();
		exHouse.load(unid);
		
		StringBuilder sb = new StringBuilder();
		String s;
		while ((s = req.getReader().readLine()) != null) {
			sb.append(s);
		}
		
		// Reflection of the received JSON into an object via com.ibm.commons
		/*
		 * HouseModel nwHouseInfo = (HouseModel) JsonParser.fromJson(JsonJavaFactory.instanceEx, sb.toString());
		 */
		
		// Reflection of the received JSON into an object via GSON
		Gson g = new Gson();
		HouseModel nwHouseInfo = g.fromJson(sb.toString(), HouseModel.class);
		
		// compare/update
		
		/**
		 * Getting the Fields from the new obj using Apache Commons FieldUtils.
		 * http://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/reflect/FieldUtils.html#getAllFields(java.lang.Class)
		 */
		Field[] fields = FieldUtils.getAllFields(nwHouseInfo.getClass());
		
		
		// this is where we can validate before committing
		/**
		 * Setting the Values..
		 */
		for (Field field : fields) {
			String curFldName = field.getName();
			if( BeanUtils.getProperty(nwHouseInfo, curFldName) !=
				BeanUtils.getProperty(exHouse, curFldName)) {
				// if the properties are not equal, the new one is an update
				BeanUtils.setProperty(exHouse, curFldName,
								BeanUtils.getProperty(nwHouseInfo, curFldName));
			}
		}
		
		// done setting new values back into the existing object
		exHouse.save();
		
		// respond with anything?
		// out.print();
		
		res.setStatus(200);
		
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
	 * @throws Exception
	 */
	public static void doDelete(String unid, HttpServletRequest req, HttpServletResponse res,
		FacesContext facesContext, ServletOutputStream out)
		throws Exception {
		
		Session s = (Session) facesContext.getApplication().getVariableResolver().resolveVariable(facesContext, "session");
		Document houseDoc = s.getCurrentDatabase().getDocumentByUNID(unid);
		houseDoc.remove(true);
		
		res.setStatus(200);
		
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
					ServletOutputStream out) throws Exception {
		res.setStatus(405);
		res.addHeader("Allow", "GET, PUT, DELETE");
	}
	
}
