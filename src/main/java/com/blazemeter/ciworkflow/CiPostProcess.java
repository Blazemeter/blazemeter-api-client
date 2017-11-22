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

public class CiPostProcess {

    public final Master master;

    public final boolean jtl;

    public final boolean junit;

    public final String junitPath;

    public final String jtlPath;

    public final String workspaceDir;

    public CiPostProcess(Master master, boolean jtl,
                         boolean junit, String junitPath,
                         String jtlPath, String workspaceDir) {
        this.master = master;
        this.jtl = jtl;
        this.junit = junit;
        this.junitPath = junitPath;
        this.jtlPath = jtlPath;
        this.workspaceDir = workspaceDir;
    }

    /**
    Executes post-process after test was finished on server
    */
    public BuildResult execute() {
        if (this.junit) {
            saveJunit();
        }
        if (this.jtl) {
            saveJtl();
        }
        printSummary();
        return null;
    }

    /**
    Saves junit report to hdd;
    */
    public void saveJunit() {

    }

    /**
    Saves jtl report to hdd;
    */
    public void saveJtl() {

    }

    /**
    Prints summary to browser;
    */
    public void printSummary(){

    }
}
