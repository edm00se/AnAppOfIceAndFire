package com.westeros.servlets;

import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HouseServletAlt extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.GenericServlet#destroy()
	 */
	@Override
	public void destroy() {

	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) {
		HttpServletRequest _req = req;
		HttpServletResponse _res = res;
		PrintWriter out = null;
		try {
			_res.setContentType("text/plain");
			out = _res.getWriter();
			out.println("Servlet Running");
			out.println("You executed a " + _req.getMethod().toString()
					+ " request.");
		} catch (Exception e) {
			if (out != null) {
				out.println("Sorry, an error occurred...");
			}
		} finally {
			out.close();
		}
	}

}
