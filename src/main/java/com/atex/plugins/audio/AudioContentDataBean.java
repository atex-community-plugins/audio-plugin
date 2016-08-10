package com.atex.plugins.audio;

import com.atex.onecms.content.aspects.annotations.AspectDefinition;

@AspectDefinition
public class AudioContentDataBean {

	private String title;
	private String description;
    private String byline;
	private String fileName;
	private Double duration;
	private String inCue;
	private String outCue;

	public String getTitle() {
		return title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

    public String getByline() {
        return byline;
    }

    public void setByline(final String byline) {
        this.byline = byline;
    }

    public String getInCue() {
		return inCue;
	}

	public void setInCue(final String inCue) {
		this.inCue = inCue;
	}

	public String getOutCue() {
		return outCue;
	}

	public void setOutCue(final String outCue) {
		this.outCue = outCue;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public Double getDuration() {
		return duration;
	}

	public void setDuration(final Double duration) {
		this.duration = duration;
	}

}
