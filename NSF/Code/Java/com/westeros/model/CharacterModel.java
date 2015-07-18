package com.westeros.model;

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
		return true;
	}
}
