/**
 * Copyright 2017 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blazemeter.ciworkflow;

import com.blazemeter.api.explorer.Master;
import com.blazemeter.api.explorer.Session;
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Set of actions taken after test on server was over.
 * Can fetch reports from server(aggregate,functional,jtl,junit,validate ci-status)
 */
public class CiPostProcess {

    protected final boolean isDownloadJtl;

    protected final boolean isDownloadJunit;

    protected final String junitPath;

    protected final String jtlPath;

    protected final String workspaceDir;

    private final UserNotifier notifier;

    private final Logger logger;

    public CiPostProcess(boolean isDownloadJtl, boolean isDownloadJunit,
                         String junitPath, String jtlPath, String workspaceDir,
                         UserNotifier notifier, Logger logger) {
        this.isDownloadJtl = isDownloadJtl;
        this.isDownloadJunit = isDownloadJunit;
        this.junitPath = junitPath;
        this.jtlPath = jtlPath;
        this.workspaceDir = workspaceDir;
        this.notifier = notifier;
        this.logger = logger;
    }

    /**
     * Executes post-process after test was finished on server
     *
     * @return BuildResult
     */
    public BuildResult execute(Master master) throws InterruptedException {
        BuildResult r = validateCiStatus(master);
        if (this.isDownloadJunit) {
            saveJunit(master);
        }
        if (this.isDownloadJtl) {
            saveJtl(master);
        }
        JSONObject summary = downloadSummary(master);
        this.notifier.notifyAbout(summary.toString());
        return r;
    }

    public BuildResult validateCiStatus(Master master) {
        BuildResult result = BuildResult.SUCCESS;
        try {
            JSONArray failures = null;
            JSONObject cis = master.getCIStatus();
            if (cis.has("failures")) {
                failures = cis.getJSONArray("failures");
                if (failures.size() > 0) {
                    notifier.notifyAbout("Having failures " + failures.toString());
                    result = BuildResult.FAILED;
                    notifier.notifyAbout("Setting ci-status = " + result.name());
                    return result;
                }
            }
            JSONArray errors = null;
            if (cis.has("errors")) {
                errors = cis.getJSONArray("errors");
                if (errors.size() > 0) {
                    notifier.notifyAbout("Having errors " + errors.toString());
                    logger.error("Having errors " + errors.toString());
                    result = errorsFailed(errors) ? BuildResult.FAILED : BuildResult.ERROR;
                }
            }
            if (result.equals(BuildResult.SUCCESS)) {
                notifier.notifyAbout("No errors/failures while validating CIStatus: setting " + result.name());
                logger.info("No errors/failures while validating CIStatus: setting " + result.name());
            }
        } catch (IOException e) {
            notifier.notifyAbout("Error while getting CI status from server " + e.getMessage());
            logger.error("Error while getting CI status from server ", e);
        }
        return result;
    }

    public boolean errorsFailed(JSONArray errors) {
        try {
            int l = errors.size();
            for (int i = 0; i < l; i++) {
                boolean errorsFailed = errors.getJSONObject(i).getInt("code") == 0 |
                        errors.getJSONObject(i).getInt("code") == 70404;
                return errorsFailed;
            }
        } catch (JSONException je) {
            notifier.notifyAbout("Failed get errors from json: " + errors.toString() + " " + je);
            logger.error("Failed get errors from json: " + errors.toString(), je);
            return false;
        }
        return false;
    }

    /**
     * Saves junit report to hdd;
     */
    public void saveJunit(Master master) {
        try {
            String junitReport = master.getJUnitReport();
            String junitFileName = master.getId() + ".xml";
            File junitFile = createJunitFile(junitPath + File.separator + junitFileName,
                    workspaceDir + File.separator + junitFileName);
            Files.write(Paths.get(junitFile.toURI()), junitReport.getBytes());
        } catch (Exception e) {
            notifier.notifyAbout("Failed to save junit report from master = " + master.getId() + " to disk.");
            logger.error("Failed to save junit report from master = " + master.getId() + " to disk.", e);
        }
    }


    /**
     * Created file for saving junit report
     */
    public File createJunitFile(String junitPath, String workspaceJunitPath) throws Exception {
        File junitFile = new File(junitPath);
        try {
            junitFile.createNewFile();
        } catch (Exception e) {
            junitFile = new File(workspaceJunitPath);
            junitFile.createNewFile();
            notifier.notifyAbout("Failed to created a file " + junitPath);
            logger.error("Failed to created a file " + junitPath, e);
            notifier.notifyAbout("Junit report will be saved to " + workspaceJunitPath);
        }
        return junitFile;
    }

    /**
     * Saves jtl report to hdd;
     */
    public void saveJtl(Master master) {
        try {
            List<Session> sessions = master.getSessions();
            URL url;
            for (Session s : sessions) {
                url = new URL(s.getJTLReport());
                try {
                    downloadUnzip(url);
                } catch (Exception e) {
                    logger.error("Failed to download&unzip jtl-report from " + url.getPath(), e);
                }
            }
        } catch (Exception e) {
            notifier.notifyAbout("Unable to get JTLZIP from " + master.getId() + " " + e.getMessage());
            logger.error("Unable to get JTLZIP from " + master.getId() + " ", e);
        }
    }

    public void downloadUnzip(URL url) {
        for (int i = 1; i < 4; i++) {
            try {
                notifier.notifyAbout("Downloading JTLZIP from url=" + url.getPath() + " attemp # " + i);
                int conTo = (int) (10000 * Math.pow(3, i - 1));
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(conTo);
                connection.setReadTimeout(30000);
                InputStream is = connection.getInputStream();
                unzipjtl(is);
                break;
            } catch (Exception e) {
                notifier.notifyAbout("Unable to get JTLZIP for sessionId=" + url.getPath() + ":check server for test artifacts" + e);
            }
        }
    }

    public void unzipjtl(InputStream is) throws Exception {
        ZipInputStream zis = new ZipInputStream(is);
        byte[] buffer = new byte[1024];
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            String fileName = zipEntry.getName();
            File f = new File(fileName);
            FileOutputStream fos = new FileOutputStream(f);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            if (f.exists() && f.getName().equals("sample.jtl")) {
                f.renameTo(new File("bm-kpis.jtl"));
            }
            zipEntry = zis.getNextEntry();
        }
        zis.closeEntry();
        zis.close();
    }

    /**
     * Downloads summary.
     * It will be either functional or aggregate depending on server settings;
     */
    public JSONObject downloadSummary(Master master) throws InterruptedException {
        JSONObject summary = new JSONObject();
        int retries = 1;
        while (retries < 5) {
            try {
                notifier.notifyAbout("Trying to get  functional summary from server, attempt# " + retries);
                summary = master.getFunctionalReport();
            } catch (IOException e) {
                notifier.notifyAbout("Failed to get functional summary for master " + e);
            }
            if (summary != null && summary.size() > 0) {
                notifier.notifyAbout("Got functional report from server");
                return summary;
            } else {
                try {
                    notifier.notifyAbout("Trying to get  aggregate summary from server, attempt# " + retries);
                    summary = master.getSummary();
                } catch (Exception e) {
                    notifier.notifyAbout("Failed to get aggregate summary for master " + e);
                    logger.error("Failed to get aggregate summary for master ", e);
                }
                if (summary != null && summary.size() > 0) {
                    notifier.notifyAbout("Got aggregated report from server");
                    return summary;
                }
            }
            Thread.sleep(5000);
            retries++;
        }
        return summary;
    }

    public boolean isDownloadJtl() {
        return isDownloadJtl;
    }

    public boolean isDownloadJunit() {
        return isDownloadJunit;
    }

    public String getJunitPath() {
        return junitPath;
    }

    public String getJtlPath() {
        return jtlPath;
    }

    public String getWorkspaceDir() {
        return workspaceDir;
    }
}
