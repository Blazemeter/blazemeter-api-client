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
import com.blazemeter.api.logging.UserNotifier;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CiPostProcess {

    public final boolean isDownloadJtl;

    public final boolean isDownloadJunit;

    public final String junitPath;

    public final String jtlPath;

    public final String workspaceDir;

    public final UserNotifier notifier;

    public CiPostProcess(boolean isDownloadJtl,
                         boolean isDownloadJunit, String junitPath,
                         String jtlPath, String workspaceDir, UserNotifier logger) {
        this.isDownloadJtl = isDownloadJtl;
        this.isDownloadJunit = isDownloadJunit;
        this.junitPath = junitPath;
        this.jtlPath = jtlPath;
        this.workspaceDir = workspaceDir;
        this.notifier = logger;
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
                    result = errorsFailed(errors) ? BuildResult.FAILED : BuildResult.ERROR;
                }
            }
            if (result.equals(BuildResult.SUCCESS)) {
                notifier.notifyAbout("No errors/failures while validating CIStatus: setting " + result.name());
            }
        } catch (IOException e) {
            notifier.notifyAbout("Error while getting CI status from server " + e);
        }
        return result;
    }

    public boolean errorsFailed(JSONArray errors) {
        boolean errorsFailed = false;
        try {
            int l = errors.size();
            for (int i = 0; i < l; i++) {
                errorsFailed = errors.getJSONObject(i).getInt("code") == 0 |
                        errors.getJSONObject(i).getInt("code") == 70404;
                return errorsFailed;
            }
        } catch (JSONException je) {
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
            File junitFile = createJunitFile(junitPath + File.separator + master.getId() + ".xml",
                    workspaceDir);
            Files.write(Paths.get(junitFile.toURI()), junitReport.getBytes());
        } catch (Exception e) {
            notifier.notifyAbout("Failed to save junit report from master = " + master.getId() + " to disk.");
        }
    }

    private File createJunitFile(String junitPath, String workspaceDir) throws Exception {
        File junitFile = new File(junitPath);
        try {
            junitFile.createNewFile();
        } catch (IOException e) {
            junitFile = new File(workspaceDir);
            junitFile.createNewFile();
        }
        return junitFile;
    }

    /**
     * Saves jtl report to hdd;
     */
    public void saveJtl(Master master) {
        try {
            List<Session> sessions = master.getSessions();
            for (Session s : sessions) {
                s.getJTLReport();
            }
        } catch (IOException e) {
            notifier.notifyAbout("Failed to get junit report from master = " + master.getId());
        }

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
                this.notifier.notifyAbout("Trying to get  functional summary from server, attempt# " + retries);
                summary = master.getFunctionalReport();
            } catch (IOException e) {
                this.notifier.notifyAbout("Failed to get functional summary for master " + e);
            }
            if (summary != null && summary.size() > 0) {
                this.notifier.notifyAbout("Got functional report from server");
                return summary;
            } else {
                try {
                    this.notifier.notifyAbout("Trying to get  aggregate summary from server, attempt# " + retries);
                    summary = master.getSummary();
                } catch (Exception e) {
                    this.notifier.notifyAbout("Failed to get aggregate summary for master " + e);
                }
                if (summary != null && summary.size() > 0) {
                    this.notifier.notifyAbout("Got aggregated report from server");
                    return summary;
                }
            }
            Thread.sleep(5000);
            retries++;
        }
        return summary;
    }
}
