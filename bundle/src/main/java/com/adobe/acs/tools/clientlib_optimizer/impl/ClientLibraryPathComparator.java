package com.adobe.acs.tools.clientlib_optimizer.impl;

import java.util.Comparator;

import com.day.cq.widget.ClientLibrary;

public class ClientLibraryPathComparator implements Comparator<ClientLibrary> {

	@Override
	public int compare(ClientLibrary o1, ClientLibrary o2) {
		return o1.getPath().compareToIgnoreCase(o2.getPath());
	}
}
