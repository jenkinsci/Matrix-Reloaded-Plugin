package net.praqma.jenkins.plugin.reloaded;

import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.model.Job;
import hudson.security.Permission;
import hudson.views.ListViewColumn;
import hudson.views.ListViewColumnDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;

public class MatrixReloadedColumn extends ListViewColumn {

  @DataBoundConstructor
  public MatrixReloadedColumn() {
    super();
  }

  public boolean isMatrixJob(Job job) {
    if (job.hasPermission(Permission.READ) && job instanceof MatrixProject) {
      return true;
    }

    return false;
  }

  @Extension
  public static class DescriptorImpl extends ListViewColumnDescriptor {

    public DescriptorImpl() {
    }

    @Override
    public String getDisplayName() {
      return "Matrix Reloaded";
    }

    @Override
    public boolean shownByDefault() {
      return false;
    }

  }

}
