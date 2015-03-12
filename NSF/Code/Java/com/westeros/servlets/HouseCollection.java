package com.westeros.servlets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;

import com.google.gson.Gson;
import com.westeros.app.Utils;

/**
 * The Collection related /houses servlet actions. This class
 * exposes a GET method for the collection and a POST method,
 * for creating a new document.
 * 
 * @author Eric McCormick, @edm00se
 *
 */
public class HouseCollection {
	
	/**
	 * GET method of actions to take against the Houses Servlet, across the
	 * implementations.
	 * 
	 * @param req
	 * @param res
	 * @param facesContext
	 * @param out ServletOutputStream
	 * @throws Exception
	 */
	public static void doGet(HttpServletRequest req, HttpServletResponse res,
					FacesContext facesContext, ServletOutputStream out)
	throws Exception {
		
		try {
			
			// the HashMap will represent the main JSON object
			HashMap<String, Object> myResponse = new HashMap<String, Object>();
			// the ArrayList will contain the JSON Array's data elements
			// which is another HashMap
			ArrayList<HashMap<String,String>> dataAr = new ArrayList<HashMap<String,String>>();
			
			Database db = Utils.getCurrentDatabase();
			View vw = db.getView("houses");
			ViewNavigator nav = vw.createViewNav();
			ViewEntry ent = nav.getFirstDocument();
			while( ent != null ) {
				
				@SuppressWarnings("unchecked")
				Vector<String> colVals = ent.getColumnValues();
				HashMap<String,String> curOb = new HashMap<String,String>();
				curOb.put("name", colVals.get(0));
				curOb.put("description", colVals.get(1));
				curOb.put("words", colVals.get(2));
				dataAr.add(curOb);
				
				ViewEntry tmpEnt = nav.getNext(ent);
				ent.recycle();
				ent = tmpEnt;
			}
			
			myResponse.put("dataAr", dataAr);
			myResponse.put("error", false);
			
			// IBM commons way of toJson
			/*
			 * out.println(JsonGenerator.toJson(JsonJavaFactory.instanceEx,
			 * myResponse));
			 */
			
			// GSON way of toJson
			Gson g = new Gson();
			out.print(g.toJson(myResponse));
			
			res.setStatus(200); // OK
			
		} catch (Exception e) {
			res.setStatus(500); // your Intarwebs are clogged
			out.print( "{error: true, errorMessage: \""+e.toString()+"\"}" );
			//out.print(e.toString());
		}
	}
	
	/**
	 * POST method of actions to take against the Houses Servlet, across the
	 * implementations.
	 * 
	 * @param req
	 * @param res
	 * @param facesContext
	 * @param out
	 * @throws Exception
	 */
	public static void doPost(HttpServletRequest req, HttpServletResponse res,
					FacesContext facesContext, ServletOutputStream out)
	throws Exception {
		
		// TODO: creation
		String unid = "";
		
		res.setStatus(201);
		res.addHeader("Location", "/houses/"+unid);
		
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
		// res.addHeader("Allow", "GET, POST, PUT, DELTE");
		res.addHeader("Allow", "GET, POST");
	}
}
