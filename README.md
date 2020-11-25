# AzimDP
A maven plugin to generate list of dependencies in form of json file

Usage example:
```xml
<plugin>
  <groupId>com.github.Azim</groupId>
  <artifactId>azimdp-maven-plugin</artifactId>
  <version>1.0.1</version>
  <configuration>
    <!--defines if plugin should check the versions of dependencies too, you might want to set that to true if you are using jitpack-->
    <ignoreVersions>true</ignoreVersions>
    <!--defines if the resulting .json file should have indentation-->
    <beautify>true</beautify>
    <!--defines a path within final jar of the generated json file (including filename)-->
    <path>AzimDP.json</path>
    <!--exclude the dependencies you know are already present, for example spigot api and libby-->
    <excludes>
      <exclude>groupId:artifactId:version</exclude>
      <exclude>com.github.Azim.libby:libby-bukkit:ignored</exclude>
    </excludes>
  </configuration>
  <executions>
    <execution>
      <goals>
        <goal>generate-dependency-list</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```
