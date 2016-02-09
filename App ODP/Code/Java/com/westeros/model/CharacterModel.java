package com.westeros.model;

import java.util.ArrayList;

import org.openntf.xsp.model.AbstractSmartDocumentModel;

/**
 * 
 * This is the primary Character Model, for how to structure a Character, be
 * they famous or infamous, in the Seven Kingdoms of Westeros... or at least
 * this application.
 * 
 * @author Eric McCormik, @edm00se
 * 
 */
@SuppressWarnings("unused")
public class CharacterModel extends AbstractSmartDocumentModel {
	private static final long serialVersionUID = 1L;
	
	private String charFirstName;
	private String charLastName;
	private String actFirstName;
	private String actLastName;
	private String title;
	private String house;
	private String home;
	private ArrayList<String> abilities;
	private ArrayList<String> parents;
	private ArrayList<String> siblings;
	private ArrayList<String> children;
	private String spouse;
	
	@Override
	protected String getFormName() {
		return "character";
	}
	
	@Override
	public void load(final String unid) {
		super.load(unid);
	}
	
	@Override
	protected boolean querySave() {
		boolean success = false;
		// VALIDATION REQUIREMENTS HERE!!!
		boolean cond = true;
		if( cond ) {
			success = true;
		}
		return success;
	}
}
