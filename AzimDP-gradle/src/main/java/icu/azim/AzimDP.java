package icu.azim;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ResolvedDependency;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.file.CopySpec;
import org.gradle.api.tasks.Copy;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class AzimDP implements Plugin <Project>{
    @Override
    public void apply(Project project) {
        //get plugin settings
        AzimDPExtension settings = project.getExtensions().create("azimdp", AzimDPExtension.class);
        //register task
        project.task("generate-dependency-list").doLast(task->{
            //populate the list of dependencies we don't want to have
            List<ResolvedDependency> excluded = new ArrayList<>();
            task.getProject().getConfigurations().getByName("runtimeClasspath").getResolvedConfiguration().getFirstLevelModuleDependencies().forEach(resolvedDependency -> {
                if(shouldExclude(settings.getExclude(), settings.isIgnoreVersions(), resolvedDependency)){
                    System.out.println("Excluding "+resolvedToString(resolvedDependency)+" and it's dependencies");
                    excluded.addAll(flatten(resolvedDependency));
                }else{
                    System.out.println("Including "+resolvedToString(resolvedDependency)+" and it's dependencies");
                }
            });

            //add the ones we want to carry around into the json array
            JsonArray resultingArray = new JsonArray();
            task.getProject().getConfigurations().getByName("runtimeClasspath").getIncoming().getResolutionResult().getAllDependencies()
                    .stream().filter(d->!excluded.stream().anyMatch(ex->areSame(settings.isIgnoreVersions(), ex, d))).forEach(d->{
                JsonObject element = new JsonObject();
                String[] dependency = d.getRequested().toString().split(":");
                element.addProperty("groupId",dependency[0]);
                element.addProperty("artifactId",dependency[1]);
                element.addProperty("version",dependency[2]);
                resultingArray.add(element);
            });

            Path file = Paths.get(settings.getPath()); //relative file in target jar
            Path parent = file.getParent()!=null?file.getParent():Paths.get(""); //if it has parent directory we want to know that
            File result = new File(new File(project.getBuildDir(),"AzimDP"), settings.getPath()); //temporary folder for the json file
            if(result.getParentFile()!=null)
                result.getParentFile().mkdirs();

            try {
                FileWriter writer = new FileWriter(result);
                Gson gson = settings.isBeautify()?new GsonBuilder().setPrettyPrinting().create():new Gson(); //beautify if needed
                gson.toJson(resultingArray, writer);
                writer.flush(); //save the file
                writer.close();
                System.out.println("Total number of dependencies written to file: "+resultingArray.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

            //copy freshly generated file into target jar
            project.getTasks().named("processResources", Copy.class, t -> {
                CopySpec copyPluginDescriptors = t.getRootSpec().addChild();
                copyPluginDescriptors.into(parent.toString());
                copyPluginDescriptors.from(result.toString());
            });

        });
    }

    private boolean shouldExclude(List<String> toExclude, boolean ignoreVersion, ResolvedDependency dependency){
        for(String e:toExclude){
            String[] exclude = e.split(":");
            if(exclude[0].equals(dependency.getModuleGroup())&&exclude[1].equals(dependency.getModuleName())&&(ignoreVersion||exclude[2].equals(dependency.getModuleVersion()))){
                return true;
            }
        }
        return false;
    }

    private boolean areSame(boolean ignoreVersion, ResolvedDependency resolved, DependencyResult res){
        String[] requested = res.getRequested().toString().split(":");
        return requested[0].equals(resolved.getModuleGroup())&&requested[1].equals(resolved.getModuleName())&&(ignoreVersion||requested[2].equals(resolved.getModuleVersion()));
    }

    private String resolvedToString(ResolvedDependency d){
        return d.getModuleGroup()+":"+d.getModuleName()+":"+d.getModuleVersion();
    }

    @SuppressWarnings("unused")
    private void printChildren(ResolvedDependency resolvedDependency, String offset){
        resolvedDependency.getChildren().forEach(child->{
            System.out.println(offset+resolvedToString(child));
            printChildren(child, offset+"-");
        });
    }

    private List<ResolvedDependency> flatten(ResolvedDependency resolvedDependency){
        List<ResolvedDependency> result = new ArrayList<>();
        result.add(resolvedDependency);
        resolvedDependency.getChildren().forEach(d->result.addAll(flatten(d)));
        return result;
    }

}
