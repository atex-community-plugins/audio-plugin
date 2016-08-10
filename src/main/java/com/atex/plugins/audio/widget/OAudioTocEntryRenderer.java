package com.atex.plugins.audio.widget;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.atex.onecms.content.Content;
import com.atex.onecms.content.ContentId;
import com.atex.onecms.content.ContentManager;
import com.atex.onecms.content.ContentResult;
import com.atex.onecms.content.ContentVersionId;
import com.atex.onecms.content.IdUtil;
import com.atex.onecms.content.Subject;
import com.atex.plugins.audio.AudioContentDataBean;
import com.atex.plugins.baseline.widget.OContentListEntryBasePolicyWidget;
import com.polopoly.cm.app.orchid.widget.OContentIcon;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;
import com.polopoly.orchid.widget.OWidget;

public class OAudioTocEntryRenderer extends OContentListEntryBasePolicyWidget implements MetadataWidgetRenderer {

	private static final long serialVersionUID = 1L;

    private static final int DEFAULT_LEAD_WIDTH = 180;
    private AudioContentDataBean contentData;
    private OContentIcon _icon;
    
    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        
        ContentPolicy policy = (ContentPolicy) getPolicy();
        _icon = new OContentIcon();
        _icon.initContent((Policy) policy, getContentSession().getPolicyCMServer());
        addAndInitChild(oc, _icon);
        
        CmClient cmClient = (CmClient) oc.getApplication().getApplicationComponent(CmClientBase.DEFAULT_COMPOUND_NAME);
        ContentManager contentManager = cmClient.getContentManager();
        ContentId contentId = IdUtil.fromPolicyContentId(getPolicy().getContentId());
        ContentVersionId versionId = contentManager.resolve(contentId, Subject.NOBODY_CALLER);
        ContentResult<AudioContentDataBean> result = contentManager
                .get(versionId, null, AudioContentDataBean.class, null, Subject.NOBODY_CALLER);
        Content<AudioContentDataBean> content = result.getContent();
        contentData = content.getContentData();
    }

    @Override
    protected void renderEntryHeader(final Device device, final OrchidContext oc) throws OrchidException, IOException {
        device.println("<div class='toolbox-right'>");
        renderToolbox(device, oc);
        device.println("</div>");
    }

    @Override
    protected void renderEntryBody(final Device device, final OrchidContext oc) throws OrchidException, IOException {
        device.println("<div class='clearfix'>");
        device.print("<span style='vertical-align:text-bottom;'>");
    	renderIcon(device, oc);
    	device.print("</span>");
        contentLink.render(oc);
        
        if (contentData.getDuration() != null && (contentData.getDuration().toString()).length() > 0) {
        	
        	long seconds = (contentData.getDuration()).longValue();

        	String length = String.format("%02d:%02d:%02d", 
        			TimeUnit.SECONDS.toHours(seconds),
        			TimeUnit.SECONDS.toMinutes(seconds),
        		    TimeUnit.SECONDS.toSeconds(seconds) - 
        		    TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
        	
        	device.println("("
                        + WidgetUtil.abbreviate(length, DEFAULT_LEAD_WIDTH)
                        + ")");
        }
        if (contentData.getDescription() != null && (contentData.getDescription()).length() > 0) {
        	device.println("<p>"
        				+ "<span style='font-style:italic'>Description: </span>"
                        + WidgetUtil.abbreviate(contentData.getDescription(), DEFAULT_LEAD_WIDTH)
                        + "</p>");
        }
        if (contentData.getInCue() != null && (contentData.getInCue()).length() > 0) {
        	device.println("<p>"
        				+ "<span style='font-style:italic'>In Cue: </span>"
                        + WidgetUtil.abbreviate(contentData.getInCue(), DEFAULT_LEAD_WIDTH)
                        + "</p>");
        }
        if (contentData.getOutCue() != null && (contentData.getOutCue()).length() > 0) {
        	device.println("<p>"
        				+ "<span style='font-style:italic'>Out Cue: </span>"
                        + WidgetUtil.abbreviate(contentData.getOutCue(), DEFAULT_LEAD_WIDTH)
                        + "</p>");
        }
        device.println("</div>");
    }

    @Override
    protected void renderEntryFooter(final Device device, final OrchidContext oc)
        throws OrchidException, IOException {
        // Just empty
    }
    
    protected void renderIcon(final Device device, final OrchidContext oc) throws OrchidException, IOException {
        _icon.render(oc);
    }

	@Override
	public void setMetadataWidget(OWidget metadataWidget) {
		// TODO Auto-generated method stub
	}

}
