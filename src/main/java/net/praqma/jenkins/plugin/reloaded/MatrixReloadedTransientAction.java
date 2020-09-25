package net.praqma.jenkins.plugin.reloaded;

import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;

import java.util.Collection;
import java.util.Collections;

@Extension
public class MatrixReloadedTransientAction extends TransientProjectActionFactory {

  @Override
  public Collection<? extends Action> createFor(AbstractProject target) {
    if (target instanceof MatrixProject) {
      return Collections.singleton(new TransientMatrixReloadedProjectAction(target));
    } else {
      return Collections.emptyList();
    }
  }

}
