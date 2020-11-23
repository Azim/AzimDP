# AzimDP
A maven plugin to generate list of dependencies in form of json file "AzimDP.json"

Usage example:
```xml
<plugin>
  <groupId>icu.azim</groupId>
  <artifactId>azimdp-maven-plugin</artifactId>
  <version>1.0.0</version>
  <configuration>
    <ignoreVersions>true</ignoreVersions>
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
