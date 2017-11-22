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
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import java.io.IOException;

public class CiPostProcess {

    public final boolean isDownloadJtl;

    public final boolean isDownloadJunit;

    public final String junitPath;

    public final String jtlPath;

    public final String workspaceDir;

    public final Logger logger;

    public CiPostProcess(boolean isDownloadJtl,
                         boolean isDownloadJunit, String junitPath,
                         String jtlPath, String workspaceDir, Logger logger) {
        this.isDownloadJtl = isDownloadJtl;
        this.isDownloadJunit = isDownloadJunit;
        this.junitPath = junitPath;
        this.jtlPath = jtlPath;
        this.workspaceDir = workspaceDir;
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
        this.logger.info(summary.toString());
        return r;
    }

    public BuildResult validateCiStatus(Master master) {
        BuildResult result = BuildResult.SUCCESS;
        try {
            JSONObject cis = master.getCIStatus();
            JSONArray failures = cis.getJSONArray("failures");
            JSONArray errors = cis.getJSONArray("errors");
            if (errors.size() > 0) {
                logger.info("Having errors " + errors.toString());
                result = errorsFailed(errors) ? BuildResult.FAILED : BuildResult.ERROR;
            }
            if (failures.size() > 0) {
                logger.info("Having failures " + failures.toString());
                result = BuildResult.FAILED;
                logger.info("Setting ci-status = " + result.name());
                return result;
            }
            if (result.equals(BuildResult.SUCCESS)) {
                logger.info("No errors/failures while validating CIStatus: setting " + result.name());
            }
        } catch (IOException e) {
            logger.error("Error while getting CI status from server ", e);
        }
        return result;
    }

    private boolean errorsFailed(JSONArray errors) {
        int l = errors.size();
        for (int i = 0; i < l; i++) {
            try {
                return errors.getJSONObject(i).getInt("code") == 0 |
                        errors.getJSONObject(i).getInt("code") == 70404;
            } catch (JSONException je) {
                return false;
            }
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
     * Prints summary to browser;
     */
    public JSONObject downloadSummary(Master master) throws InterruptedException {
        JSONObject summary = null;
        int retries = 1;
        try {
            while (retries < 5 && summary == null) {
                this.logger.info("Trying to get  test report from server, attempt# " + retries);
                summary = master.getFunctionalReport();
                if (summary != null) {
                    this.logger.info("Got functional report from server");
                    break;
                } else {
                    summary = master.getSummary();
                    if (summary != null) {
                        this.logger.info("Got aggregated report from server");
                        break;
                    }
                }
                Thread.sleep(5000);
                retries++;
            }

        } catch (IOException e) {
            this.logger.error("Failed to get summary for master ", e);
        }
        return summary;
    }
}
