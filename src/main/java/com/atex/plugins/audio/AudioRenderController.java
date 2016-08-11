package com.atex.plugins.audio;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.atex.onecms.content.ContentFileInfo;
import com.atex.onecms.content.FilesAspectBean;
import com.atex.plugins.audio.util.DomainBuilder;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.cm.servlet.RequestPreparator;
import com.polopoly.cm.servlet.URLBuilder;
import com.polopoly.model.Model;
import com.polopoly.model.ModelPathUtil;
import com.polopoly.render.RenderRequest;
import com.polopoly.siteengine.dispatcher.ControllerContext;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.mvc.RenderControllerBase;

public class AudioRenderController extends RenderControllerBase {

    private static final Logger LOG = Logger.getLogger(AudioRenderController.class.getName());

    @Override
    public void populateModelBeforeCacheKey(final RenderRequest request,
								            final TopModel topModel,
								            final ControllerContext context) {

    	super.populateModelBeforeCacheKey(request, topModel, context);

        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        final Model localModel = topModel.getLocal();

        final PolicyCMServer cmServer = getCmClient(context).getPolicyCMServer();

        try {
            final AudioPolicy audio = (AudioPolicy) cmServer.getPolicy(context.getContentId());

            final URI uri = new DomainBuilder()
                    .setTopModel(topModel)
                    .setRequest(httpServletRequest)
                    .build();
            final String domain = (uri != null ? uri.toASCIIString() : "");
            if (uri != null) {
                ModelPathUtil.set(localModel, "domain", domain);
            }

            final URLBuilder urlBuilder = RequestPreparator.getURLBuilder(httpServletRequest);
            FilesAspectBean aspect = audio.getFiles();

            for (ContentFileInfo f : aspect.getFiles().values()) {

                final String audioFileUrl = urlBuilder.createFileUrl(context.getContentId(), f.getFilePath(), httpServletRequest);
                ModelPathUtil.set(localModel, "audioFile", domain + audioFileUrl);
                ModelPathUtil.set(localModel, "audioType", "audio/mpeg");
            }

        } catch (Exception e) {
            LOG.log(Level.WARNING, "Error", e);
        }

    }

}
