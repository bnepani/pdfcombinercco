package com.appirio.report;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapPanelOrderPreferences {

	private Map<String, MapPanelOrderPreference> mapPanelOrderPreferences;

	public void addMapPanelOrderPreference(MapPanelOrderPreference mapPanelOrderPreference) {

		this.getMapPanelOrderPreferences().put(mapPanelOrderPreference.getFlight() + " " + mapPanelOrderPreference.getPanel(), mapPanelOrderPreference);
	}

	public Map<String, MapPanelOrderPreference> getMapPanelOrderPreferences() {

		if(this.mapPanelOrderPreferences == null) {
			this.mapPanelOrderPreferences = new LinkedHashMap<String, MapPanelOrderPreference>();
		}

		return this.mapPanelOrderPreferences;
	}
}
