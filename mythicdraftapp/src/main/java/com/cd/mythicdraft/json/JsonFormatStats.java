package com.cd.mythicdraft.json;

import java.io.Serializable;
import java.util.List;

public class JsonFormatStats implements Serializable {

	private static final long serialVersionUID = 1L;

	private JsonFormat format;
	private List<JsonFormatPickStats> formatPickStats;
	public JsonFormat getFormat() {
		return format;
	}
	public void setFormat(JsonFormat format) {
		this.format = format;
	}
	public List<JsonFormatPickStats> getFormatPickStats() {
		return formatPickStats;
	}
	public void setFormatPickStats(List<JsonFormatPickStats> formatPickStats) {
		this.formatPickStats = formatPickStats;
	}
}
