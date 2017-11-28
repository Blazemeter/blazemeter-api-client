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
import com.blazemeter.api.logging.Logger;
import com.blazemeter.api.logging.UserNotifier;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.io.IOException;

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
            saveJunit();
        }
        if (this.isDownloadJtl) {
            saveJtl();
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
    public void saveJunit() {

    }

    /**
     * Saves jtl report to hdd;
     */
    public void saveJtl() {

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
