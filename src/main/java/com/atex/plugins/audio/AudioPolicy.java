package com.atex.plugins.audio;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.atex.onecms.content.AspectedPolicy;
import com.atex.onecms.content.ContentFileInfo;
import com.atex.onecms.content.FilesAspectBean;
import com.atex.onecms.content.metadata.MetadataInfo;
import com.polopoly.application.Application;
import com.polopoly.application.IllegalApplicationStateException;
import com.polopoly.cm.app.Resource;
import com.polopoly.cm.app.inbox.InboxFlags;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CMRuntimeException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.metadata.Metadata;
import com.polopoly.metadata.MetadataAware;
import com.polopoly.metadata.util.MetadataUtil;
import com.polopoly.util.StringUtil;

public class AudioPolicy extends AspectedPolicy<AudioContentDataBean> implements Resource,
                                                                                 MetadataAware {

    private static final Logger LOGGER = Logger.getLogger(AudioPolicy.class.getName());
    private static final String RESOURCE_TYPE = "audio";
    private static final String FILE_PATH = "/polopoly_fs/%s!/%s";
    private static final String DURATION_NOT_AVAILABLE = "N/A";
    
    public AudioPolicy(final CmClient cmClient, final Application application) throws IllegalApplicationStateException {
        super(cmClient, application);
    }

    @Override
    public String getName() throws CMException {
    	if (super.getName() == null) {
            FilesAspectBean files = getFiles();
    		if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
    			for (String file: files.getFiles().keySet()) {
    				return file;
    			}
    		}
    	}
        return super.getName();
    }

    public FilesAspectBean getFiles() throws CMException {
        return (FilesAspectBean) getAspect(FilesAspectBean.ASPECT_NAME);
    }

    @Override
    public void importFile(final String path, final InputStream data) throws CMException, IOException {
        super.importFile(path, data);

    }

    @Override
    public void deleteFile(final String path) throws CMException, IOException {
        super.deleteFile(path);
    }

    public String getDurationStr() throws CMException {
        AudioContentDataBean audioContentDataBean = getContentData();
        if (audioContentDataBean != null) {
            return getDurationStr(audioContentDataBean.getDuration(), true);
        }

        return DURATION_NOT_AVAILABLE;
    }

    @Override
    public void postCreateSelf() throws CMException {
        super.postCreateSelf();

        new InboxFlags().setShowInInbox(this, true);
    }

    @Override
    public void preCommitSelf() throws CMException {
    	AudioContentDataBean audioContentDataBean = getContentData();

    	if (audioContentDataBean != null) {
    		
    		FilesAspectBean files = getFiles();
    		if (files != null && files.getFiles() != null && !files.getFiles().isEmpty()) {
    			for (String file: files.getFiles().keySet()) {

    				if (StringUtil.isEmpty(audioContentDataBean.getTitle())) {
	    				String title = String.format("%s (%s)", file,  getDurationStr(audioContentDataBean.getDuration(), false));
	    				audioContentDataBean.setTitle(title);
    				}
    			}
    		}
    	} 
        super.preCommitSelf();
    }

    private String getDurationStr(Double duration, boolean includeHours) {

        if (duration == null) {
            return DURATION_NOT_AVAILABLE;
        }
    	
    	int hours = (int) Math.floor(duration/360);
    	int minutes = (int) Math.floor((duration - hours*360)/60);
    	int seconds = (int) Math.floor(duration - hours*360 - minutes*60);
    	//Double miliseconds = Math.floor((duration % 1)*1000);;
    	
    	if (hours != 0 || includeHours) {		
    		return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    	} else {
    		return String.format("%02d:%02d", minutes, seconds);
    	}
    }

    
	@Override
	public Map<String, String> getResourceData() throws CMException {
		Map<String, String> map = new HashMap<>();
        map.put(Resource.FIELD_RESOURCE_TYPE, RESOURCE_TYPE);
        FilesAspectBean files = getFiles();
        
        try {
	        	if (files != null) {
			        for (String file: files.getFiles().keySet()) {
						ContentFileInfo contentFileInfo = files.getFiles().get(file);
						String path = contentFileInfo.getFilePath();
				        map.put(Resource.FIELD_CONTENT_FILE_PATH, String.format(FILE_PATH, getContentId().getContentIdString(), path));
						break;
					}
	        	}
	    } catch (Exception e) {
	    	LOGGER.logp(Level.WARNING, CLASS, "getResourceData",
	                "Failed to create resource data for " + getContentId(), e);
	    }
        
        return map;
	}

    @Override
    public Metadata getMetadata()  {
        try {
            MetadataInfo metadata = (MetadataInfo) getAspect(MetadataInfo.ASPECT_NAME);
            if (metadata != null) {
                return metadata.getMetadata();
            }
        } catch (CMException e) {
        }

        return new Metadata();
    }

    @Override
    public void setMetadata(final Metadata metadata) {
        try {
            MetadataInfo metadataInfo= new MetadataInfo();
            metadataInfo.setMetadata(metadata);
            metadataInfo.setTaxonomyIds(MetadataUtil.getTaxonomyIds(this));
            setAspect(MetadataInfo.ASPECT_NAME, metadataInfo);
        } catch (CMException e) {
            throw new CMRuntimeException("Unable to set metadata", e);
        }
    }

}
