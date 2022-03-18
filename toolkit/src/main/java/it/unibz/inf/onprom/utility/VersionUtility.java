/*
 * onprom-toolkit
 *
 * VersionUtility.java
 *
 * Copyright (C) 2016-2019 Free University of Bozen-Bolzano
 *
 * This product includes software developed under
 * KAOS: Knowledge-Aware Operational Support project
 * (https://kaos.inf.unibz.it).
 *
 * Please visit https://onprom.inf.unibz.it for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.unibz.inf.onprom.utility;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vdurmont.semver4j.Semver;
import it.unibz.inf.onprom.ui.form.InformationDialog;
import it.unibz.inf.onprom.ui.utility.UIUtility;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

public class VersionUtility {
    private static final Logger logger = LoggerFactory.getLogger(VersionUtility.class.getSimpleName());
    private static boolean isUpdating = false;

    public synchronized static String getVersion() {
        try {
            String version = null;
            File f = new File("pom.xml");
            if (f.exists() && !f.isDirectory()) {
                MavenXpp3Reader reader = new MavenXpp3Reader();
                Model model = reader.read(new FileReader("pom.xml"));
                version = model.getVersion();
            }
            // read from manifest file
            if (version == null) {
                version = VersionUtility.class.getPackage().getImplementationVersion();
            }
            return version;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            InformationDialog.display(e.getMessage());
            return null;
        }
    }

    private synchronized static int asMB(long value) {
        return (int) value / (1024 * 1024);
    }

    private synchronized static void downloadLatestJar(Pair<String, String> downloadInformation) {
        try {
            String fileName = downloadInformation.getLeft();
            String downloadUrl = downloadInformation.getRight();

            URL fileURL = new URL(downloadInformation.getRight());
            int contentLength = fileURL.openConnection().getContentLength();
            int sizeInMb = asMB(contentLength);

            if (UIUtility.confirm("There is a new version of onprom: " + fileName + " (" + sizeInMb + "MB). Would like to download this version?", "Update Confirmation")) {
                File file = new File(fileName);
                int fileSize = asMB(file.length());
                if (!file.exists() || UIUtility.confirm("The file " + fileName + "(" + fileSize + "MB) exists, do you want to overwrite?")) {
                    logger.debug("Downloading from " + downloadUrl + " to " + fileName + " file");
                    logger.debug("Size of the file is " + sizeInMb + "MB");
                    ProgressMonitor progressMonitor = new ProgressMonitor(null, "", "", 0, contentLength);
                    ReadableByteChannel rbc = new CallbackByteChannel(
                            Channels.newChannel(fileURL.openStream()),
                            contentLength,
                            (rbc1, progress) -> {
                                progressMonitor.setProgress((int) rbc1.getReadSoFar());
                                if (progressMonitor.isCanceled()) {
                                    UIUtility.info("Download is cancelled!");
                                    if (file.exists() && file.length() < contentLength && file.delete()) {
                                        logger.debug("Deleted incomplete file");
                                    }
                                    try {
                                        rbc1.close();
                                    } catch (IOException e) {
                                        logger.error(e.getMessage(), e);
                                        InformationDialog.display(e.getMessage());
                                    }
                                }
                            }
                    );
                    FileOutputStream fos = new FileOutputStream(downloadInformation.getLeft());
                    fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                    if (progressMonitor.isCanceled()) {
                        UIUtility.info("<p>Didn't finish the downloading the new version of onprom: " + downloadInformation.getLeft() + "</p>");
                        logger.debug("Download is cancelled");
                    } else {
                        UIUtility.info("<p>Finished downloading the new version of onprom: " + downloadInformation.getLeft() + "</p><p>Please use the new version by double clicking the downloaded jar file.</p>");
                        logger.debug("Download is completed");
                    }
                    progressMonitor.close();
                    fos.close();
                    rbc.close();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            InformationDialog.display(e.getMessage());
        }
    }

    private synchronized static JsonNode getLatestRelease() {
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL("https://api.github.com/repos/onprom/onprom/releases/latest").openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            JsonNode jsonNode = (new ObjectMapper().readTree(IOUtils.toString(conn.getInputStream(), StandardCharsets.UTF_8)));
            conn.disconnect();
            return jsonNode;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            InformationDialog.display(e.getMessage());
            return null;
        }
    }

    private synchronized static String releaseTagName(JsonNode release) {
        if (release != null) {
            String tagName = release.get("tag_name").asText();
            if (tagName.toLowerCase().contains("v")) {
                return tagName.replace('v', ' ').replace('V', ' ');
            }
        }
        return null;
    }

    private synchronized static String releaseHtmlUrl(JsonNode release) {
        return release.get("html_url").asText();
    }

    private synchronized static String releaseString(JsonNode release) {
        return "The latest available release is <a href=" + releaseHtmlUrl(release) + " target=\"_blank\">" + releaseTagName(release) + "</a>";
    }

    private synchronized static Pair<String, String> getDownloadInformation(JsonNode release) {
        JsonNode drNode = release.path("assets");
        Iterator<JsonNode> itr = drNode.elements();
        while (itr.hasNext()) {
            JsonNode temp = itr.next();
            String name = temp.get("name").asText().trim().toLowerCase();
            if (name.matches("onprom-toolkit-(.*).jar")) {
                return new ImmutablePair<>(name, temp.get("browser_download_url").asText());
            }
        }
        return null;
    }

    public synchronized static String checkVersion() {
        try {
            if (!isUpdating) {
                String currentVersion = getVersion();
                JsonNode latestRelease = getLatestRelease();

                if (currentVersion != null && latestRelease != null) {
                    boolean newVersionExists = new Semver(releaseTagName(latestRelease)).isGreaterThan(currentVersion);
                    Pair<String, String> downloadInformation = getDownloadInformation(latestRelease);
                    if (newVersionExists) {
                        if (downloadInformation != null) {
                            isUpdating = true;
                            downloadLatestJar(downloadInformation);
                            isUpdating = false;
                        }
                    }
                    String isLatest = newVersionExists ? "an old" : "a new";
                    return "<p>You are using " + isLatest + " version " + currentVersion + "</p><p>" + releaseString(latestRelease) + "</p>";
                }
            } else {
                UIUtility.info("Already downloading an update");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "An error occured when retrieving the version";
    }
}
