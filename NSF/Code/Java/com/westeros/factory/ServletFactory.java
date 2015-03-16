package com.westeros.factory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import com.ibm.designer.runtime.domino.adapter.ComponentModule;
import com.ibm.designer.runtime.domino.adapter.IServletFactory;
import com.ibm.designer.runtime.domino.adapter.ServletMatch;

public class ServletFactory implements IServletFactory {
	private static final Map<String, String> servletClasses = new HashMap<String, String>();
	private static final Map<String, String> servletNames = new HashMap<String, String>();
	private ComponentModule module;
	
	public void init(ComponentModule module) {
		servletClasses.put("houses", "com.westeros.servlets.HouseServlet");
		servletNames.put("houses", "Houses of Westeros");
		
		servletClasses.put("althouses", "com.westeros.servlets.HouseServletAlt");
		servletNames.put("althouses", "Alt Houses of Westeros");
		
		this.module = module;
	}
	
	public ServletMatch getServletMatch(String contextPath, String path)
	throws ServletException {
		try {
			String servletPath = "";
			// iterate the servletNames map
			Iterator<Map.Entry<String, String>> it = servletNames.entrySet()
			.iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pairs = it.next();
				if (path.contains("/" + pairs.getKey())) {
					String pathInfo = path;
					return new ServletMatch(getWidgetServlet(pairs.getKey()),
									servletPath, pathInfo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Servlet getWidgetServlet(String key) throws ServletException {
		return module.createServlet(servletClasses.get(key), servletNames
						.get(key), null);
	}
	
}
