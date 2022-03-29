# Private Libraries

Command to add a library/jar to the repository is the following:

```bash
mvn install:install-file -DlocalRepositoryPath=path_to_this_repository -DgroupId=group_id_of_package -DartifactId=name_of_package -Dversion=version_of_package -Dpackaging=jar -Dfile=jar_file_with_path
```

Examples:

```bash
mvn install:install-file -DlocalRepositoryPath=./onprom/libraries/ -DgroupId=deckfour -DartifactId=spex -Dversion=1.0 -Dpackaging=jar -Dfile=Spex.jar
mvn install:install-file -DlocalRepositoryPath=./onprom/libraries/ -DgroupId=deckfour -DartifactId=openxes -Dversion=1.0 -Dpackaging=jar -Dfile=OpenXES-20170216.jar
mvn install:install-file -DlocalRepositoryPath=./onprom/libraries/ -DgroupId=processmining -DartifactId=prom -Dversion=6.6 -Dpackaging=jar -Dfile=/home/tahir/ProM/ProM66.jar
```

After updating jars or adding new jars, it must be commited and pushed to the remote repository.

## How to add to your pom

It is already part of parent project but it is also possible to use in other modules by adding the following:

```xml

<repositories>
    ...
    <repository>
        <id>libraries</id>
        <url>file://${basedir}/../libraries</url>
    </repository>
    ...
</repositories>
```