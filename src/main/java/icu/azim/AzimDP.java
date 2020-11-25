package icu.azim;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Set;

@Mojo(name = "generate-dependency-list", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME, requiresOnline = true)
public class AzimDP extends AbstractMojo {

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    MavenProject project;

    @Parameter(property = "excludes")
    String[] excludes;

    @Parameter(property = "ignoreVersions", defaultValue = "true", required = true)
    Boolean ignoreVersions;

    @Parameter(property = "path", defaultValue = "AzimDP.json", required = true)
    String path;

    @Parameter(property = "beautify", defaultValue = "false", required = true)
    Boolean beautify;

    public void execute()  {
        Set<Artifact> artifacts = project.getArtifacts();
        getLog().info("Total number of transitive dependencies: "+project.getArtifacts().size());
        JsonArray toSave = new JsonArray();
        for(Artifact a:artifacts){
            boolean skipped = false;
            for(String trail:a.getDependencyTrail()){
                String[]pieces = trail.split(":");
                for(String excluded:excludes){
                    String[]excludedPieces = excluded.split(":");
                    if(pieces[0].equals(excludedPieces[0])&&pieces[1].equals(excludedPieces[1])&&(ignoreVersions||pieces[3].equals(excludedPieces[2]))){
                        skipped = true;
                        break;
                    }
                }
                if(skipped) break;
            }
            if(skipped){
                getLog().info("Skipping "+artifactToString(a)+" as it's parent is excluded");
                continue;
            }
            JsonObject artifactData = new JsonObject();
            artifactData.addProperty("groupId",a.getGroupId());
            artifactData.addProperty("artifactId",a.getArtifactId());
            artifactData.addProperty("version",a.getVersion());
            toSave.add(artifactData);
            getLog().info("Included "+artifactToString(a));
        }
        File dir = new File(project.getBuild().getDirectory(),"classes");
        dir.mkdirs();

        File result = new File(dir, path);
        if(result.getParentFile()!=null)
            result.getParentFile().mkdirs();

        try {
            FileWriter writer = new FileWriter(result);
            Gson gson = beautify?new GsonBuilder().setPrettyPrinting().create() : new Gson();
            gson.toJson(toSave, writer);
            writer.flush();
            writer.close();
            getLog().info("Total number of dependencies written to file: "+toSave.size());
        } catch (Exception e) {
            getLog().warn(e);
        }

    }

    private String artifactToString(Artifact a){
        return a.getGroupId()+":"+a.getArtifactId()+":"+a.getVersion()+":"+a.getScope();
    }
}
