package com.westeros.servlets;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.xsp.extlib.util.ExtLibUtil;

public class HouseCollection {

	/**
	 * GET method of actions to take against the Example Servlet, across the
	 * implementations.
	 * 
	 * @param req
	 * @param res
	 * @param facesContext
	 * @param out
	 *            ServletOutputStream
	 * @throws Exception
	 */
	public static void doGet(HttpServletRequest req, HttpServletResponse res,
			FacesContext facesContext, ServletOutputStream out)
			throws Exception {

		try {
			res.setStatus(200); // OK
			out.println(req.getMethod());
			@SuppressWarnings("unchecked")
			Map<String, String> param = (Map<String, String>) ExtLibUtil
					.resolveVariable(facesContext, "param");
			out.println("pathInfo: " + req.getPathInfo());
			out.println("contextPath: " + req.getContextPath());
			List<String> routeParm = Arrays.asList(req.getPathInfo().split(
					"/(.*?)"));
			if (routeParm.size() > 3) {
				for (int i = 3; i < routeParm.size(); i++) {
					// nsf/xsp/servlet is the base,
					// so the fourth is the first routeParm
					out.println("routeParm: " + routeParm.get(i));
				}
			} else {
				out.println("routeParm: " + "none");
			}
			out.println("param: " + param);
			out.println("\nHeaders:\n");
			@SuppressWarnings("unchecked")
			Enumeration<String> headerNames = req.getHeaderNames();
			while (headerNames.hasMoreElements()) {
				String headerName = headerNames.nextElement();
				out.print(headerName + ":");
				String headerValue = req.getHeader(headerName);
				out.print(" " + headerValue);
				out.println("\n");
			}
		} catch (Exception e) {
			out.println(e.toString());
		}
	}

	/**
	 * POST method of actions to take against the Example Servlet, across the
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

		res.setStatus(200);
		out.println(req.getMethod());
		@SuppressWarnings("unchecked")
		Map<String, String> param = (Map<String, String>) ExtLibUtil
				.resolveVariable(facesContext, "param");
		out.println("pathInfo: " + req.getPathInfo());
		out.println("param: " + param);
	}

	/**
	 * PUT method of actions to take against the Example Servlet, across the
	 * implementations.
	 * 
	 * @param req
	 * @param res
	 * @param facesContext
	 * @param out
	 * @throws Exception
	 */
	public static void doPut(HttpServletRequest req, HttpServletResponse res,
			FacesContext facesContext, ServletOutputStream out)
			throws Exception {

		res.setStatus(200);
		out.println(req.getMethod());
		@SuppressWarnings("unchecked")
		Map<String, String> param = (Map<String, String>) ExtLibUtil
				.resolveVariable(facesContext, "param");
		out.println("pathInfo: " + req.getPathInfo());
		out.println("param: " + param);
	}

	/**
	 * DELETE method of actions to take against the Example Servlet, across the
	 * implementations.
	 * 
	 * @param req
	 * @param res
	 * @param facesContext
	 * @param out
	 * @throws Exception
	 */
	public static void doDelete(HttpServletRequest req,
			HttpServletResponse res, FacesContext facesContext,
			ServletOutputStream out) throws Exception {

		res.setStatus(200);
		out.println(req.getMethod());
		@SuppressWarnings("unchecked")
		Map<String, String> param = (Map<String, String>) ExtLibUtil
				.resolveVariable(facesContext, "param");
		out.println("pathInfo: " + req.getPathInfo());
		out.println("param: " + param);
	}

	/**
	 * Method of actions to take against the Example Servlet, across the
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
		res.addHeader("Allow", "GET, POST, PUT, DELTE");
	}
}
