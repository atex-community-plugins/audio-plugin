package com.atex.plugins.audio.util;

import static com.google.common.base.Preconditions.checkNotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;

import com.polopoly.cm.client.CMException;
import com.polopoly.siteengine.model.TopModel;
import com.polopoly.siteengine.structure.Alias;
import com.polopoly.siteengine.structure.Site;

/**
 * Allow you to build a domain string.
 *
 * @author mnova
 */
public class DomainBuilder {

    private static final Logger LOG = Logger.getLogger(DomainBuilder.class.getName());

    private Site site = null;
    private HttpServletRequest request = null;

    public DomainBuilder setTopModel(final TopModel model) {
        checkNotNull(model);
        if (model.getContext() != null && model.getContext().getSite() != null) {
            site = model.getContext().getSite().getBean();
        }
        return this;
    }

    public DomainBuilder setSite(final Site site) {
        checkNotNull(site);
        this.site = site;
        return this;
    }

    public DomainBuilder setRequest(final HttpServletRequest request) {
        checkNotNull(request);
        this.request = request;
        return this;
    }

    public URI build() {
        if (site == null && request == null) {
            throw new RuntimeException("site and request are null!");
        }
        URI uri = null;
        if (site != null) {
            try {
                final Alias alias = site.getMainAlias();
                if (alias != null) {
                    final String url = alias.getUrl();
                    if (url != null) {
                        uri = new URI(url);
                    }
                }
            } catch (CMException e) {
                LOG.log(Level.SEVERE, "cannot get alias: " + e.getMessage(), e);
            } catch (URISyntaxException e) {
                LOG.log(Level.SEVERE, "cannot parse url: " + e.getMessage(), e);
            }
        }
        if (uri == null && request != null) {
            // prepare the domain
            final StringBuilder sb = new StringBuilder();
            sb.append(request.getScheme());
            sb.append("://");
            sb.append(request.getServerName());
            int port = request.getServerPort();
            if (port != 80) {
                sb.append(":");
                sb.append(port);
            }
            try {
                uri = new URI(sb.toString());
            } catch (URISyntaxException e) {
                LOG.log(Level.SEVERE, "cannot parse url: " + e.getMessage(), e);
            }
        }
        return uri;
    }
}
