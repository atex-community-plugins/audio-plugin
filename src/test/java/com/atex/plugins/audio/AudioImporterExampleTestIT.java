package com.atex.plugins.audio;

import java.io.ByteArrayInputStream;

import javax.inject.Inject;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.polopoly.cm.ContentId;
import com.polopoly.cm.ExternalContentId;
import com.polopoly.cm.policy.PolicyCMServer;
import com.polopoly.testnj.TestNJRunner;
import com.polopoly.util.StringUtil;

/**
 * A simple audio import example.
 *
 * @author mnova
 */
@Ignore
@RunWith(TestNJRunner.class)
public class AudioImporterExampleTestIT {

    private static final ContentId AUDIO_TEMPLATE_ID = new ExternalContentId("com.atex.plugins.audio.Audio");
    private static final ContentId DEP_ID = new ExternalContentId("PolopolyPost.d");
    private static final int REQUEST_TIMEOUT = 30 * 1000;

    @Inject
    private PolicyCMServer cmServer;

    @Test
    public void testImportContent() throws Exception {

        final String title = "test Title";
        final String description = "description";
        final String byline = "byline";
        final String url = "http://cdn46.castfire.com/audio/522/3444/25318/2932333/2016-08-11acs_2016-08-10-232133-7770-0-644-0.64k.mp3";

        AudioPolicy audio = (AudioPolicy) cmServer.createContent(1, DEP_ID, AUDIO_TEMPLATE_ID);
        final String fullURL = url;
        final String audioURL;
        final int qmIdx = fullURL.indexOf('?');
        if (qmIdx > 0) {
             audioURL = fullURL.substring(0, qmIdx);
        } else {
            audioURL = fullURL;
        }
        Connection.Response response = Jsoup
                .connect(audioURL)
                .timeout(REQUEST_TIMEOUT)
                .ignoreContentType(true)
                .execute();
        byte[] clipBytes = response.bodyAsBytes();
        String extension = FilenameUtils.getExtension(audioURL);
        if (StringUtil.isEmpty(extension)) {
            // If for some reason extension is missing, assume jpg
            extension = "mp3";
        }
        String audioName = FilenameUtils.getBaseName(audioURL) + "." + extension;
        audio.importFile(audioName, new ByteArrayInputStream(clipBytes));

        final AudioContentDataBean audioBean = new AudioContentDataBean();
        audioBean.setTitle(title);
        audioBean.setDescription(description);
        audioBean.setByline(byline);
        audioBean.setFileName(audioName);
        audioBean.setDuration(60.0);
        audio.setContentData(audioBean);

        //processMetadata(podcastItem, categories,audio);
        cmServer.commitContent(audio);

        System.out.println(audio.getContentId().getContentIdString());
    }

}
