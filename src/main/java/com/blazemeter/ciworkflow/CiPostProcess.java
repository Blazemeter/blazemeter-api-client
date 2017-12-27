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

    protected final UserNotifier notifier;

    protected final Logger logger;

    public CiPostProcess(boolean isDownloadJtl, boolean isDownloadJunit, String jtlPath,
                         String junitPath, String workspaceDir,
                         UserNotifier notifier, Logger logger) {
        this.isDownloadJtl = isDownloadJtl;
        this.isDownloadJunit = isDownloadJunit;
        this.jtlPath = jtlPath;
        this.junitPath = junitPath;
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
        try {
            JSONObject ciStatus = master.getCIStatus();
            BuildResult result = validateCiStatus(ciStatus);
            boolean hasReports = checkErrorCode(ciStatus);
            if (hasReports) {
                if (isDownloadJunit) {
                    saveJunit(master);
                }
                if (isDownloadJtl) {
                    saveJTL(master);
                }
                JSONObject summary = downloadSummary(master);
                notifier.notifyInfo(summary.toString());
            }
            return result;
        } catch (Exception e) {
            notifier.notifyError("Error while getting CI status from server " + e.getMessage());
            logger.error("Error while getting CI status from server ", e);
            return BuildResult.FAILED;
        }
    }

    protected boolean checkErrorCode(JSONObject ciStatus) {
        if (ciStatus.has("errors")) {
            JSONArray errors = ciStatus.getJSONArray("errors");
            if (!errors.isEmpty()) {
                for (int i = 0; i < errors.size(); i++) {
                    if (errors.getJSONObject(i).getInt("code") == 70404) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    public BuildResult validateCiStatus(JSONObject ciStatus) {
        BuildResult result = checkFailsAndError(ciStatus);

        if (result.equals(BuildResult.SUCCESS)) {
            notifier.notifyInfo("No errors/failures while validating CIStatus: setting " + result.name());
            logger.info("No errors/failures while validating CIStatus: setting " + result.name());
        }
        return result;
    }

    protected BuildResult checkFailsAndError(JSONObject ciStatus) {
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

    protected BuildResult notifyAboutErrors(JSONArray errors) {
        notifier.notifyWarning("Having errors " + errors.toString());
        logger.error("Having errors " + errors.toString());
        return isErrorsFailed(errors) ? BuildResult.FAILED : BuildResult.ERROR;
    }

    protected BuildResult notifyAboutFailed(JSONArray failures) {
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
            if (StringUtils.isBlank(junitReport)) {
                notifier.notifyWarning("Got empty junit report from server");
            } else {
                File junitReportDir = mkdirs(workspaceDir, junitPath);
                File junitFile = createReportFile(junitReportDir, master.getId() + ".xml");
                notifier.notifyInfo("Saving junit report " + junitFile.getAbsolutePath());
                Files.write(Paths.get(junitFile.toURI()), junitReport.getBytes());
            }
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
            File jtlReportsDir = mkdirs(workspaceDir, jtlPath, false);
            for (Session session : master.getSessions()) {
                for (int i = 1; i < 6; i++) {
                    String reportUrl = session.getJTLReport();
                    if (reportUrl != null) {
                        URL url = new URL(reportUrl);
                        File reportDir = new File(jtlReportsDir, session.getId());
                        reportDir.mkdirs();
                        boolean isSuccess = downloadAndUnzipJTL(url, reportDir);
                        notifier.notifyInfo("Saving jtl report " + reportDir.getAbsolutePath());
                        if (!isSuccess) {
                            logger.error("Failed to download & unzip jtl-report from " + url.getPath());
                        }
                        break;
                    } else {
                        if (i == 5) {
                            notifier.notifyWarning("Failed to get JTL ZIP for session id=" + session.getId());
                            break;
                        }
                        Thread.sleep(5000);
                    }
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
    public boolean downloadAndUnzipJTL(URL url, File reportDir) {
        try {
            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(30000);
            InputStream inputStream = connection.getInputStream();
            unzipJTL(inputStream, reportDir);
            return true;
        } catch (Exception e) {
            notifier.notifyWarning("Unable to get JTL zip for sessionId=" + url.getPath() + " : check server for test artifacts " + e);
            logger.error("Unable to get JTL zip for sessionId=" + url.getPath() + " : check server for test artifacts ", e);
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
            File report = new File(reportDir, zipEntry.getName());
            FileOutputStream fos = new FileOutputStream(report);
            int len;
            while ((len = zis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            if (report.exists() && report.getName().equals("sample.jtl")) {
                report.renameTo(new File(reportDir, "bm-kpis.jtl"));
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

    public static File createReportFile(File parent, String reportName) throws IOException {
        File result = new File(parent, reportName);
        result.createNewFile();
        return result;
    }

    public static File mkdirs(String workspaceDir, String userPath) throws IOException {
        return mkdirs(workspaceDir, userPath, true);
    }

    public static File mkdirs(String workspaceDir, String userPath, boolean doMkdirs) throws IOException {
        if (StringUtils.isBlank(userPath) || !new File(userPath).isAbsolute()) {
            File workspace = (workspaceDir == null) ? createTmpDir() : new File(workspaceDir);
            File result = StringUtils.isBlank(userPath) ? workspace : new File(workspace, userPath);
            if (doMkdirs) {
                result.mkdirs();
            }
            return result;
        } else {
            File userFile = new File(userPath);
            if (doMkdirs) {
                userFile.mkdirs();
            }
            return userFile;
        }
    }

    public static File createTmpDir() throws IOException {
        final File temp = File.createTempFile("bzm_tmp", Long.toString(System.nanoTime()));

        if (!(temp.delete())) {
            throw new IOException("Could not delete temp file: " + temp.getAbsolutePath());
        }

        if (!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }

        return (temp);
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
