package com.westeros.servlets;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import frostillicus.xsp.servlet.AbstractXSPServlet;

public class HouseServlet extends AbstractXSPServlet {

	@Override
	protected void doService(HttpServletRequest req, HttpServletResponse res,
			FacesContext facesContext, ServletOutputStream out)
			throws Exception {

		res.setContentType("text/plain");

		String reqMethod = req.getMethod();

		if (reqMethod.equals("GET")) {

			HouseCollection.doGet(req, res, facesContext, out);

		} else if (reqMethod.equals("POST")) {

			HouseCollection.doPost(req, res, facesContext, out);

		} else if (reqMethod.equals("PUT")) {

			HouseCollection.doPut(req, res, facesContext, out);

		} else if (reqMethod.equals("DELETE")) {

			HouseCollection.doDelete(req, res, facesContext, out);

		} else {
			// unsupported request method
			HouseCollection.handleUnexpectedVerb(req, res, facesContext, out);
		}

	}

}
