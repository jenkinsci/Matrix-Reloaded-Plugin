package net.praqma.jenkins.plugin.reloaded;

import hudson.matrix.Combination;
import hudson.matrix.Layouter;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;
import hudson.model.Action;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 *
 * @author Lucie Votypkova
 */
public class MatrixProjectFilterAction implements Action{
    
    private MatrixProject project;
    
    public MatrixProjectFilterAction(MatrixProject project){
        this.project = project;
    }
    
    public List<String> getChecked() {
        List<String> checked = new ArrayList<String>();
        for(MatrixConfiguration config: project.getActiveConfigurations()){
            checked.add(config.getDisplayName());
        }
        return checked;
    }
    
    public MatrixProject getProject(){
        return project;
    }
    
    public Layouter getLayouter(){
        return project.getLayouter();
    }

    public String getDisplayName() {
        return "Set combination filter";
    }

    public String getIconFileName() {
        return Definitions.__ICON_FILE_NAME;
    }

    public String getUrlName() {
        return "filter";
    }
    
    public void doSetFilter(StaplerRequest request, StaplerResponse response) throws IOException, ServletException{
        StringBuilder builder = new StringBuilder();
        for(MatrixConfiguration configuration: project.getItems()){
            if(request.getParameter(configuration.getDisplayName())==null){
                if(builder.length()>0)
                    builder.append(" || ");
                builder.append("(").append(createFilterExpression(configuration.getCombination())).append( ")");
            }
        }
        if(builder.length()>0){
            project.setCombinationFilter("!(" + builder.toString() + ")");
            project.save();
        }
        response.forwardToPreviousPage(request);
    }
    
    public String createFilterExpression(Combination combination){
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String,String> e : combination.entrySet()) {
            if(builder.length()>1) builder.append(" && ");
            builder.append(e.getKey()).append("==\"").append(e.getValue()).append("\"");
        }
        return builder.toString();
    }
}
