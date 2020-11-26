package icu.azim;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import java.util.ArrayList;
import java.util.List;

public class AzimDPExtension {
    final Property<Boolean> ignoreVersions;
    final Property<Boolean> beautify;
    final Property<String> path;
    final ListProperty<String> exclude;

    @javax.inject.Inject
    public AzimDPExtension(ObjectFactory objects) {
        ignoreVersions = objects.property(Boolean.class);
        beautify = objects.property(Boolean.class);
        path = objects.property(String.class);
        exclude = objects.listProperty(String.class);
    }

    //all the getters with default values in case it's run without any configuration
    public List<String> getExclude(){
        return exclude.getOrElse(new ArrayList<>());
    }
    public boolean isIgnoreVersions(){
        return ignoreVersions.getOrElse(true);
    }
    public boolean isBeautify(){
        return beautify.getOrElse(true);
    }
    public String getPath(){
        return path.getOrElse("AzimDP.json");
    }
}
