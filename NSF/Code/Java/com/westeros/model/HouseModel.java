package com.westeros.model;

import org.openntf.xsp.model.AbstractSmartDocumentModel;

/**
 * 
 * This is the primary House Model, for how to structure a House, be it great or
 * small, in the Seven Kingdoms of Westeros... or at least this application.
 * 
 * @author Eric McCormik, @edm00se
 * 
 */
@SuppressWarnings("unused")
public class HouseModel extends AbstractSmartDocumentModel {
	private static final long serialVersionUID = 1L;

	private String name;
	private String description;
	private String coatOfArms;
	private String words;
	private String seat;
	private String currentLord;
	private String region;
	private String title;
	private String heir;
	private String overlord;

	@Override
	protected String getFormName() {
		return "house";
	}

	@Override
	public void load(final String unid) {
		super.load(unid);
	}

	@Override
	protected boolean querySave() {
		return true;
	}
}
