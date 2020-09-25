package net.praqma.jenkins.plugin.reloaded;

import hudson.model.Item;
import hudson.model.Job;
import hudson.model.RootAction;

public class TransientMatrixReloadedProjectAction implements RootAction {

  private Item owner;

  /**
   * Standard constructor.
   *
   * @param owner the "owner" of this action. Used to render the side panel.
   */
  public TransientMatrixReloadedProjectAction(Item owner) {
    this.owner = owner;
  }

  public String getDisplayName() {
    return Definitions.__DISPLAY_NAME;
  }

  public String getIconFileName() {
    return Definitions.__ICON_FILE_NAME;
  }

  public String getUrlName() {
    Job project = (Job) owner;
    if (project == null) {
      return null;
    }

    if (project.getLastBuild() != null) {
      return "lastBuild/" + Definitions.__URL_NAME;
    }

    return null;
  }

}
