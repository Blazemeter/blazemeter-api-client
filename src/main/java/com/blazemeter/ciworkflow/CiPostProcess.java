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
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
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
    public BuildResult execute(Master master) {
        BuildResult result = validateCiStatus(master);
        if (isDownloadJunit) {
            saveJunit(master);
        }
        if (isDownloadJtl) {
            saveJTL(master);
        }
        JSONObject summary = downloadSummary(master);
        notifier.notifyInfo(summary.toString());
        return result;
    }

    public BuildResult validateCiStatus(Master master) {
        BuildResult result = BuildResult.ERROR;
        try {
            JSONObject ciStatus = master.getCIStatus();
            result = checkFailsAndError(ciStatus);
        } catch (Exception e) {
            notifier.notifyError("Error while getting CI status from server " + e.getMessage());
            logger.error("Error while getting CI status from server ", e);
        }

        if (result.equals(BuildResult.SUCCESS)) {
            notifier.notifyInfo("No errors/failures while validating CIStatus: setting " + result.name());
            logger.info("No errors/failures while validating CIStatus: setting " + result.name());
        }
        return result;
    }

    private BuildResult checkFailsAndError(JSONObject ciStatus) {
        if (ciStatus.has("failures")) {
            JSONArray failures = ciStatus.getJSONArray("failures");
            if (!failures.isEmpty()) {
                return notifyAboutFailed(failures);
            }
        }
        if (ciStatus.has("errors")) {
            JSONArray errors = ciStatus.getJSONArray("errors");
            if (!errors.isEmpty()) {
                return notifyAboutErrors(errors);
            }
        }
        return BuildResult.SUCCESS;
    }

    private BuildResult notifyAboutErrors(JSONArray errors) {
        notifier.notifyWarning("Having errors " + errors.toString());
        logger.error("Having errors " + errors.toString());
        return isErrorsFailed(errors) ? BuildResult.FAILED : BuildResult.ERROR;
    }

    private BuildResult notifyAboutFailed(JSONArray failures) {
        notifier.notifyInfo("Having failures " + failures.toString());
        notifier.notifyInfo("Setting ci-status = " + BuildResult.FAILED.name());
        return BuildResult.FAILED;
    }

    public boolean isErrorsFailed(JSONArray errors) {
        boolean errorsFailed = false;
        try {
            int l = errors.size();
            for (int i = 0; i < l; i++) {
                errorsFailed = errors.getJSONObject(i).getInt("code") == 0 ||
                        errors.getJSONObject(i).getInt("code") == 70404;
                if (!errorsFailed) {
                    return false;
                }
            }
        } catch (JSONException je) {
            notifier.notifyWarning("Failed get errors from json: " + errors.toString() + " " + je);
            logger.error("Failed get errors from json: " + errors.toString(), je);
        }
        return errorsFailed;
    }

    /**
     * Saves junit report to hdd;
     */
    public void saveJunit(Master master) {
        try {
            String junitReport = master.getJUnitReport();
            String junitFileName = master.getId() + ".xml";
            File junitReportDir = makeReportDir(junitPath);
            File junitFile = new File(junitReportDir, junitFileName);
            junitFile.createNewFile();
            notifier.notifyInfo("Saving junit report " + junitFile);
            Files.write(Paths.get(junitFile.toURI()), junitReport.getBytes());
        } catch (Exception e) {
            notifier.notifyWarning("Failed to save junit report from master = " + master.getId() + " to disk.");
            logger.error("Failed to save junit report from master = " + master.getId() + " to disk.", e);
        }
    }


    /**
     * Saves jtl report to hdd;
     */
    public void saveJTL(Master master) {
        try {
            for (Session s : master.getSessions()) {
                URL url = new URL(s.getJTLReport());
                boolean isSuccess = downloadAndUnzipJTL(url, jtlPath + File.separator + s.getId());
                if (!isSuccess) {
                    logger.error("Failed to download & unzip jtl-report from " + url.getPath());
                }
            }
        } catch (Exception e) {
            notifier.notifyWarning("Unable to get JTL ZIP from " + master.getId() + " " + e.getMessage());
            logger.error("Unable to get JTL ZIP from " + master.getId() + " ", e);
        }
    }

    /**
     * @param url - for download JTL report
     * @return true - if report has been successfully downloaded and unzip
     */
    public boolean downloadAndUnzipJTL(URL url, String jtlZipPath) {
        for (int i = 1; i < 4; i++) {
            try {
                int timeout = (int) (10000 * Math.pow(3, i - 1));
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(30000);
                InputStream inputStream = connection.getInputStream();
                File reportDir = makeReportDir(jtlZipPath);
                notifier.notifyInfo("Downloading jtl zip to " + reportDir);
                unzipJTL(inputStream, reportDir);
                return true;
            } catch (Exception e) {
                notifier.notifyWarning("Unable to get JTL zip for sessionId=" + url.getPath() + " : check server for test artifacts " + e);
                logger.error("Unable to get JTL zip for sessionId=" + url.getPath() + " : check server for test artifacts ", e);
            }
        }
        return false;
    }

    /**
     * Unzips archive with JTL from InputStream
     *
     * @param inputStream
     * @throws IOException
     */
    public void unzipJTL(InputStream inputStream, File reportDir) throws IOException {
        ZipInputStream zis = new ZipInputStream(inputStream);
        byte[] buffer = new byte[4096];
        ZipEntry zipEntry = zis.getNextEntry();
        while (zipEntry != null) {
            String fileName = zipEntry.getName();
            File f = new File(reportDir, fileName);
            FileOutputStream fos = new FileOutputStream(f);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            if (f.exists() && f.getName().equals("sample.jtl")) {
                f.renameTo(new File(reportDir, "bm-kpis.jtl"));
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
    public JSONObject downloadSummary(Master master) {
        JSONObject summary = getFunctionalReport(master);
        if (summary != null && !summary.isEmpty()) {
            notifier.notifyInfo("Got functional report from server");
            return summary;
        }

        summary = getSummary(master);
        if (summary != null && !summary.isEmpty()) {
            notifier.notifyInfo("Got aggregated report from server");
            return summary;
        }

        return new JSONObject();
    }

    protected JSONObject getSummary(Master master) {
        try {
            notifier.notifyInfo("Trying to get aggregate summary from server");
            return master.getSummary();
        } catch (Exception e) {
            notifier.notifyWarning("Failed to get aggregate summary for master " + e);
            logger.error("Failed to get aggregate summary for master ", e);
            return null;
        }
    }

    /**
     * Requests functional report from master
     *
     * @param master
     * @return
     */
    protected JSONObject getFunctionalReport(Master master) {
        try {
            notifier.notifyInfo("Trying to get functional summary from server");
            return master.getFunctionalReport();
        } catch (IOException e) {
            notifier.notifyWarning("Failed to get functional summary for master " + e);
            logger.error("Failed to get functional summary for master ", e);
            return null;
        }
    }

    public File makeReportDir(String reportDir) throws Exception {
        File f;
        File workspaceDir;
        String reportDirNoNull = reportDir.replace("null" + File.separator, "");
        if (this.workspaceDir == null) {
            workspaceDir = File.createTempFile(reportDirNoNull, reportDirNoNull);
        } else {
            workspaceDir = new File(this.workspaceDir);
        }
        if (StringUtils.isBlank(reportDirNoNull)) {
            f = workspaceDir;
        } else {
            f = new File(reportDirNoNull);
        }
        if (!f.isAbsolute()) {
            f = new File(workspaceDir, reportDirNoNull);
        }
        try {
            f.mkdirs();
        } catch (Exception e) {
            throw new Exception("Failed to find filepath to " + f.getAbsolutePath());
        } finally {
            if (!f.exists()) {
                f = new File(workspaceDir, reportDirNoNull);
                f.mkdirs();
            }
        }
        notifier.notifyInfo("Resolving path into " + f.getCanonicalPath());
        return f.getCanonicalFile();
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
