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
import com.westeros.model.HouseModel;

/**
 * The Collection related /houses servlet actions. This class
 * exposes a GET method for the collection and a POST method,
 * for creating a new document.
 * 
 * @author Eric McCormick, @edm00se
 *
 */
public class HouseCollection {
	
	private static String colAllowMethods = "GET, POST";
	
	/**
	 * GET method of actions to take against the Houses Servlet, across the
	 * implementations.
	 * 
	 * 
	 * 
	 * @api {get} /houses/:id Request House entry
	 * @apiName Houses
	 * @apiGroup Houses
	 *
	 * @apiParam {Number} id Users unique ID.
	 *
	 * @apiSuccess {Boolean} editMode  Edit mode status of the House record.
	 * @apiSuccess {String} unid  UNID of the House record.
	 * @apiSuccess {String} values.form  Form of the House record.
	 * @apiSuccess {String} values.coatOfArms  Coat of Arms the House record.
	 * @apiSuccess {String} values.currentLord  Current Lord of the House record.
	 * @apiSuccess {String} values.description  Description of the House record.
	 * @apiSuccess {String} values.heir  Heir of the House record.
	 * @apiSuccess {String} values.name  Name of the House record.
	 * @apiSuccess {String} values.overlord  Overlord of the House record.
	 * @apiSuccess {String} values.region  Region of the House record.
	 * @apiSuccess {String} values.seat  Seat of the House record.
	 * @apiSuccess {String} values.title  Title of the House record.
	 * @apiSuccess {String} values.words  Words of the House record.
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
			ArrayList<HashMap<String,String>> dataAr = new ArrayList<HashMap<String,String>>();
			
			Database db = Utils.getCurrentDatabase();
			View vw = db.getView("houses");
			ViewNavigator nav = vw.createViewNav();
			@SuppressWarnings("unchecked")
			Vector<String> colNames = vw.getColumnNames();
			int numCols = colNames.size();
			ViewEntry ent = nav.getFirstDocument();
			while( ent != null ) {
				
				@SuppressWarnings("unchecked")
				Vector<String> colVals = ent.getColumnValues();
				HashMap<String,String> curOb = new HashMap<String,String>();
				
				/*
				curOb.put("name", colVals.get(0));
				curOb.put("description", colVals.get(1));
				curOb.put("words", colVals.get(2));
				curOb.put("unid", colVals.get(3));
				 */
				for( int i=0; i<numCols; i++ ) {
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
			/*
			 * out.println(JsonGenerator.toJson(JsonJavaFactory.instanceEx,
			 * myResponse));
			 */
			
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
	 * POST method of actions to take against the Houses Servlet, across the
	 * implementations.
	 * 
	 * @api {post} /houses/:id Create House entry
	 * @apiName Houses
	 * @apiGroup Houses
	 *
	 * @apiParam {Number} id Users unique ID.
	 *
	 * @apiSuccess {Boolean} editMode  Edit mode status of the House record.
	 * @apiSuccess {String} unid  UNID of the House record.
	 * @apiSuccess {String} values.form  Form of the House record.
	 * @apiSuccess {String} values.coatOfArms  Coat of Arms the House record.
	 * @apiSuccess {String} values.currentLord  Current Lord of the House record.
	 * @apiSuccess {String} values.description  Description of the House record.
	 * @apiSuccess {String} values.heir  Heir of the House record.
	 * @apiSuccess {String} values.name  Name of the House record.
	 * @apiSuccess {String} values.overlord  Overlord of the House record.
	 * @apiSuccess {String} values.region  Region of the House record.
	 * @apiSuccess {String} values.seat  Seat of the House record.
	 * @apiSuccess {String} values.title  Title of the House record.
	 * @apiSuccess {String} values.words  Words of the House record.
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
			JsonJavaObject tmpNwHouse = (JsonJavaObject) JsonParser.fromJson(factory, reqStr);
			Iterator<String> it = tmpNwHouse.getJsonProperties();
			HouseModel nwHouse = new HouseModel();
			nwHouse.setEditMode(true);
			while( it.hasNext() ) {
				String curProp = it.next();
				String curVal = tmpNwHouse.getAsString(curProp);
				nwHouse.setValue(curProp, curVal);
				it.remove();
			}
			 */
			
			// GSON way
			Gson g = new Gson();
			/*
			 * Direct reflection to the HouseModel breaks, as it
			 * extends AbstractSmartDocumentModel.
			 * 
			 * This hacks around that issue, as I know that the House
			 * model is really a bunch of String key to String value pairs.
			 * The AbstractSmartDocumentModel class basically adds some helper
			 * methods to wrap a Map<String,Object> (representing the Notes
			 * Document's Field to Value nature) with things like an edit
			 * property, load (by unid) method, and save (for the obvious).
			 */
			Map<String,Object> tmpNwHouse = g.fromJson(reqStr, HashMap.class);
			HouseModel nwHouse = new HouseModel();
			nwHouse.setEditMode(true);
			for (Map.Entry<String, Object> pair : tmpNwHouse.entrySet()) {
				String curProp = pair.getKey();
				Object curVal = pair.getValue();
				nwHouse.setValue(curProp, curVal);
			}
			
			//HouseModel nwHouse = g.fromJson(reqStr, HouseModel.class);
			
			boolean svSuccess = nwHouse.save();
			if(svSuccess) {
				unid = nwHouse.getUnid();
				res.setStatus(201);
				res.addHeader("Location", "/xsp/houses/"+unid);
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
	 * Method of actions to take against the Houses Servlet, across the
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
