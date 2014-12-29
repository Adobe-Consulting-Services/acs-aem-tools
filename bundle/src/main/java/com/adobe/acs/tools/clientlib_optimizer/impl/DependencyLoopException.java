package com.adobe.acs.tools.clientlib_optimizer.impl;

public class DependencyLoopException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DependencyLoopException(String string) {
		super(string);
	}

}
