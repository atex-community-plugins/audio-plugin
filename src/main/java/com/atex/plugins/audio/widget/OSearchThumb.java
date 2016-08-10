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
import com.atex.plugins.audio.AudioPolicy;
import com.polopoly.cm.app.orchid.widget.OContentIcon;
import com.polopoly.cm.app.search.widget.OSearchThumbBase;
import com.polopoly.cm.client.CMException;
import com.polopoly.cm.client.CmClient;
import com.polopoly.cm.client.CmClientBase;
import com.polopoly.cm.policy.ContentPolicy;
import com.polopoly.cm.policy.Policy;
import com.polopoly.orchid.OrchidException;
import com.polopoly.orchid.context.Device;
import com.polopoly.orchid.context.OrchidContext;

@SuppressWarnings("serial")
public class OSearchThumb extends OSearchThumbBase {

    private static final int DEFAULT_LEAD_WIDTH = 180;
    private static final int DEFAULT_WIDGET_WIDTH = 180;
    private AudioPolicy article;
    private AudioContentDataBean contentData;
    private String name;    
    private OContentIcon _icon;

    protected int getWidth() {
        return DEFAULT_WIDGET_WIDTH;
    }

    @Override
    public void initSelf(final OrchidContext oc) throws OrchidException {
        super.initSelf(oc);
        try {
            article = (AudioPolicy) getPolicy();
            name = article.getName();
            if (name == null || "".equals(name)) {
                name = article.getContentId().getContentId().getContentIdString();
            }

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
        } catch (CMException e) {
            throw new OrchidException(e);
        }
    }

    @Override
    protected void renderThumb(final OrchidContext oc) throws IOException, OrchidException {
        Device device = oc.getDevice();
        device.print("<span style='float:left'>");
        renderIcon(device, oc);
        device.print("</span>");
        device.print("<h2>" + name + "</h2>");

        if (contentData != null && contentData.getDuration() != null && (contentData.getDuration().toString()).length() > 0) {
        	
        	long seconds = (contentData.getDuration()).longValue();

        	String result = String.format("%02d:%02d:%02d", 
        			TimeUnit.SECONDS.toHours(seconds),
        			TimeUnit.SECONDS.toMinutes(seconds),
        		    TimeUnit.SECONDS.toSeconds(seconds) - 
        		    TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(seconds)));
        	
        	device.println("<p class='lead'>("
                        + WidgetUtil.abbreviate(result, DEFAULT_LEAD_WIDTH)
                        + ")</p>");
        }
        
        if (contentData != null && contentData.getInCue() != null && (contentData.getInCue()).length() > 0) {
        	device.println("<p class='lead' style='padding-left:6px;text-indent:-6px;'>"
        				+ "<span style='font-style:italic'>In Cue: </span>"
                        + WidgetUtil.abbreviate(contentData.getInCue(), DEFAULT_LEAD_WIDTH)
                        + "</p>");
        }
        if (contentData != null && contentData.getOutCue() != null && (contentData.getOutCue()).length() > 0) {
        	device.println("<p class='lead' style='padding-left:6px;text-indent:-6px;'>"
        				+ "<span style='font-style:italic'>Out Cue: </span>"
                        + WidgetUtil.abbreviate(contentData.getOutCue(), DEFAULT_LEAD_WIDTH)
                        + "</p>");
        }
    }

    @Override
    protected String getCSSClass() {
        return "custom-search-article";
    }

    protected void renderIcon(final Device device, final OrchidContext oc) throws OrchidException, IOException {
        _icon.render(oc);
    }
}
