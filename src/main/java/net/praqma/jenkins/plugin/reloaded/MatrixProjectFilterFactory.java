package net.praqma.jenkins.plugin.reloaded;

import hudson.Extension;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.TransientProjectActionFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Lucie Votypkova
 */
@Extension
public class MatrixProjectFilterFactory extends TransientProjectActionFactory{

    @Override
    public Collection<? extends Action> createFor(AbstractProject ap) {
        List<Action> actions = new ArrayList<Action>();
        if(ap instanceof MatrixProject){
            actions.add(new MatrixProjectFilterAction((MatrixProject)ap));
        }
        return actions;
    }
    
}
