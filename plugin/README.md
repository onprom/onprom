ProM plugins module for OnProM tool.

# Notes
- After generating JAR using maven (by `mvn clean install` command), it is possible to use plugin with ProM by copying generated JAR (`onprom-plugin.jar`) to the `ProM66_lib` folder.
- This generated JAR contains all dependencies defined in `pom.xml`, except `ProM` related dependencies.