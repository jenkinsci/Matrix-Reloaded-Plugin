/*
 *  The MIT License
 *
 *  Copyright 2011, 2012 Praqma A/S.
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 */
package net.praqma.jenkins.plugin.reloaded;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.servlet.ServletException;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.jfree.util.Log;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.matrix.Combination;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixRun;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Cause;
import hudson.model.CauseAction;
import hudson.model.Hudson;
import hudson.model.ParametersAction;
import hudson.security.Permission;

/**
 * The Matrix Reloaded Action class. This enables the plugin to add the link
 * action to the side panel.
 *
 * @author wolfgang
 */
public class MatrixReloadedAction implements Action {

    private AbstractBuild<?, ?> build;
    private String checked = null;
    private static final Logger logger = Logger.getLogger(MatrixReloadedAction.class.getName());

    enum BuildType {

        MATRIXBUILD, MATRIXRUN, UNKNOWN
    }

    public MatrixReloadedAction() {
    }

    public MatrixReloadedAction(String checked) {
        this.checked = checked;
    }

    public String getDisplayName() {
        return Definitions.__DISPLAY_NAME;
    }

    public String getIconFileName() {
        return Definitions.__ICON_FILE_NAME;
    }

    public String getUrlName() {
        return Definitions.__URL_NAME;
    }

    public AbstractBuild<?, ?> getBuild() {
        return build;
    }

    public String getPrefix() {
        return Definitions.__PREFIX;
    }

    public String getChecked() {
        return this.checked;
    }
    
    public Permission getPermission(){
        return MatrixProject.BUILD;
    }

    public boolean combinationExists(AbstractBuild<?, ?> build, Combination c) {
        MatrixProject mp = null;

        if (build instanceof MatrixBuild) {
            mp = (MatrixProject) build.getProject();
        } else if (build instanceof MatrixRun) {
            mp = ((MatrixRun) build).getParentBuild().getProject();
        } else {
            Log.warn("Unable to determine matrix project");
            return false;
        }

        MatrixConfiguration mc = mp.getItem(c);

        /*
         * Verify matrix configuration
         */
        if (mc == null || !mc.isActiveConfiguration()) {
            return false;
        }

        return true;
    }

    public RebuildAction getRebuildAction(Map<String, String[]> formData) {
        logger.info("[MRP] The MATRIX RELOADED FORM has been submitted");

        
        RebuildAction raction = new RebuildAction();
        
        /*
         * Generate the parameters
         */
        Set<String> keys = formData.keySet();
        for (String key : keys) {

            /*
             * The special form field, providing information about the build we
             * decent from
             */
            if (key.equals(Definitions.__PREFIX + "NUMBER")) {
                String value = formData.get(key)[0];
                try {
                    raction.setBaseBuildNumber( Integer.parseInt(value) );
                    logger.info("[MRP] Build number is " + raction.getBaseBuildNumber());
                } catch (NumberFormatException w) {
                    /*
                     * If we can't parse the integer, the number is zero. This
                     * will either make the new run fail or rebuild it id
                     * rebuildIfMissing is set(not set actually)
                     */
                }

                continue;
            }

            /*
             * Check the fields of the form
             */
            if (key.startsWith(Definitions.__PREFIX)) {
                String[] vs = key.split(Definitions.__DELIMITER, 2);
                try {
                    if (vs.length > 1) {
                        logger.info("[MRP] adding " + key);
                        raction.addConfiguration( Combination.fromString(vs[1]), true );
                    }

                } catch (JSONException e) {
                    /*
                     * No-op, not the field we were looking for.
                     */
                }
            }
            //if the key is set we set the value on the build status for later on
            if (key.startsWith("forceDownstream")) {
                raction.setRebuildDownstream( true );
            }

        }
        
        return raction;
    }

    /**
     * This submits the action added by the run listener, onCompleted and will thus matrix reload a matrix build.
     * @param req
     * @param rsp
     * @throws ServletException
     * @throws IOException
     * @throws InterruptedException
     */
    public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp) throws ServletException,
            IOException, InterruptedException {
        Hudson.getInstance().checkPermission(getPermission());
        AbstractBuild<?, ?> mbuild = req.findAncestorObject(AbstractBuild.class);

        BuildType type;

        if (req.findAncestor(MatrixBuild.class) != null) {
            type = BuildType.MATRIXBUILD;
            build = mbuild;
        } else if (req.findAncestor(MatrixRun.class) != null) {
            type = BuildType.MATRIXRUN;
            build = ((MatrixRun) mbuild).getParentBuild();
        } else {
            type = BuildType.UNKNOWN;
        }

        Map map = req.getParameterMap();
        
        /*
        System.out.println("VALUES:");
        Set<String> keys = map.keySet();
        for (String key : keys) {
            System.out.print(key + ": ");
            for (String val : req.getParameterValues(key)) {
                System.out.print(val + "; ");
            }
            System.out.println();
        }
        */
        RebuildAction raction = getRebuildAction(map);
        
        
        /*
        * Get the parameters of the build, if any and add them to the build
        */
        ParametersAction pactions = build.getAction(ParametersAction.class);

        /*
         * Schedule the MatrixBuild
         */        
        Hudson.getInstance().getQueue().schedule(build.getProject(), 0, pactions, raction, new CauseAction(new Cause.UserCause()));
        
        /*
         * Depending on where the form was submitted, the number of levels to
         * direct
         */
        if (type.equals(BuildType.MATRIXRUN)) {
            rsp.sendRedirect("../../../");
        } else {
            rsp.sendRedirect("../../");
        }
    }
}
