package com.westeros.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.faces.context.FacesContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lotus.domino.Database;
import lotus.domino.View;
import lotus.domino.ViewEntry;
import lotus.domino.ViewNavigator;

import org.apache.commons.io.IOUtils;

import com.google.gson.Gson;
import com.westeros.app.Utils;
import com.westeros.model.CharacterModel;

/**
 * The Collection related /characters servlet actions. This class
 * exposes a GET method for the collection and a POST method,
 * for creating a new document.
 * 
 * @author Eric McCormick, @edm00se
 *
 */
public class CharacterCollection {
	
	private static String colAllowMethods = "GET, POST";
	
	/**
	 * GET method of actions to take against the Characters Servlet, across the
	 * implementations.
	 * 
	 * @param req
	 * @param res
	 * @param facesContext
	 * @param out ServletOutputStream
	 * @throws IOException
	 * @throws Exception
	 */
	public static void doGet(HttpServletRequest req, HttpServletResponse res,
					FacesContext facesContext, ServletOutputStream out) throws IOException {
		
		try {
			
			// the HashMap will represent the main JSON object
			HashMap<String, Object> myResponse = new HashMap<String, Object>();
			// the ArrayList will contain the JSON Array's data elements
			// which is another HashMap
			ArrayList<HashMap<String,Object>> dataAr = new ArrayList<HashMap<String,Object>>();
			
			Database db = Utils.getCurrentDatabase();
			View vw = db.getView("characters");
			ViewNavigator nav = vw.createViewNav();
			@SuppressWarnings("unchecked")
			Vector<String> colNames = vw.getColumnNames();
			int numCols = colNames.size();
			ViewEntry ent = nav.getFirstDocument();
			while( ent != null ) {
				
				@SuppressWarnings("unchecked")
				Vector colVals = ent.getColumnValues();
				HashMap<String,Object> curOb = new HashMap<String,Object>();
				
				for( int i=0; i<numCols; i++ ) {
					//handle Vector output
					curOb.put(colNames.get(i), colVals.get(i));
				}
				
				dataAr.add(curOb);
				
				ViewEntry tmpEnt = nav.getNext(ent);
				ent.recycle();
				ent = tmpEnt;
			}
			
			myResponse.put("dataAr", dataAr);
			myResponse.put("error", false);
			
			// IBM commons way of toJson
			//out.println(JsonGenerator.toJson(JsonJavaFactory.instanceEx,myResponse));
			
			
			// GSON way of toJson
			Gson g = new Gson();
			out.print(g.toJson(myResponse));
			res.setStatus(200); // OK
			res.addHeader("Allow", colAllowMethods);
			
		} catch (Exception e) {
			res.setStatus(500); // your Intarwebs are clogged
			res.addHeader("Allow", colAllowMethods);
			out.print( "{error: true, errorMessage: \""+e.toString()+"\"}" );
			//out.print(e.toString());
		}
	}
	
	/**
	 * POST method of actions to take against the Characters Servlet, across the
	 * implementations.
	 * 
	 * @param req
	 * @param res
	 * @param facesContext
	 * @param out
	 * @throws IOException
	 */
	@SuppressWarnings("unchecked")
	public static void doPost(HttpServletRequest req, HttpServletResponse res,
					FacesContext facesContext, ServletOutputStream out) throws IOException {
		try {
			String unid;
			
			ServletInputStream is = req.getInputStream();
			// not that I'm using it, but the ServletRequestWrapper
			// can be quite helpful
			// ServletRequestWrapper srw = new ServletRequestWrapper(req);
			String reqStr = IOUtils.toString(is);
			
			// com.ibm.commons way
			/*
			JsonJavaFactory factory = JsonJavaFactory.instanceEx;
			JsonJavaObject tmpNwChar = (JsonJavaObject) JsonParser.fromJson(factory, reqStr);
			Iterator<String> it = tmpNwChar.getJsonProperties();
			CharacterModel nwChar = new CharacterModel();
			nwChar.setEditMode(true);
			while( it.hasNext() ) {
				String curProp = it.next();
				String curVal = tmpNwChar.getAsString(curProp);
				nwChar.setValue(curProp, curVal);
				it.remove();
			}
			 */
			
			// GSON way
			Gson g = new Gson();
			/*
			 * Direct reflection to the CharacterModel breaks, as it
			 * extends AbstractSmartDocumentModel.
			 * 
			 * This hacks around that issue, as I know that the Character
			 * model is really a bunch of String key to String value pairs.
			 * The AbstractSmartDocumentModel class basically adds some helper
			 * methods to wrap a Map<String,Object> (representing the Notes
			 * Document's Field to Value nature) with things like an edit
			 * property, load (by unid) method, and save (for the obvious).
			 */
			Map<String,Object> tmpNwChar = g.fromJson(reqStr, HashMap.class);
			CharacterModel nwChar = new CharacterModel();
			nwChar.setEditMode(true);
			for (Map.Entry<String, Object> pair : tmpNwChar.entrySet()) {
				String curProp = pair.getKey();
				String curVal = (String) pair.getValue();
				nwChar.setValue(curProp, curVal);
			}
			
			//CharacterModel nwChar = g.fromJson(reqStr, CharacterModel.class);
			
			boolean svSuccess = nwChar.save();
			if(svSuccess) {
				unid = nwChar.getUnid();
				res.setStatus(201);
				res.addHeader("Location", "/xsp/characters/"+unid);
			} else {
				res.setStatus(400);
			}
			
			
			res.addHeader("Allow", colAllowMethods);
			
		}catch(Exception e) {
			HashMap<String,Object> errOb = new HashMap<String,Object>();
			errOb.put("error", true);
			errOb.put("errorMessage",e.toString());
			
			res.setStatus(500);
			res.addHeader("Allow", colAllowMethods);
			Gson g = new Gson();
			out.print(g.toJson(errOb));
		}
		
	}
	
	/**
	 * Method of actions to take against the Characters Servlet, across the
	 * implementations, for unexpected VERB.
	 * 
	 * @param req
	 * @param res
	 * @param facesContext
	 * @param out
	 */
	public static void handleUnexpectedVerb(HttpServletRequest req,
					HttpServletResponse res, FacesContext facesContext,
					ServletOutputStream out) {
		res.setStatus(405);
		res.addHeader("Allow", "GET, POST");
	}
	
}
