# Release Process

The release process is barebones and focused on what we need to use this tool internally.

## What we do

- Java artifacts are versioned according to [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html)
- This version is considered the version of this project
- We publish released versions of the Java artifacts to Maven
- We tag the sources in GitHub with that version and create a release in GitHub that summarizes the changes

## What we don't do

- We do not adhere to any well-defined release schedule
- We do not assign versions to shell script workers
- We do not automatically check for API changes (meaning we might miss a breaking change and assign a wrong semantic version)
- We do not upload either the JAR or the shell script artifacts to GitHUb releases
- We do not publish Docker containers with either the JAR or the shell script artifacts to a public repository
- We do not adjust the images in the Kubernetes deployment descriptor files to reference the correct version

## The Optimal Release Process

This section is to describe the optimal release process. It is here for interested parties who want to go beyond what is currently being provided.

## Relevant Release Artifacts

- JAR files for Testbench core and most of the testbench workers
- Shell scripts for Chaos experiment worker
- Docker containers for Java and shell workers
- Kubernetes deployment descriptor files for Java and shell workers

## Build Sequence

- Build JARs with Maven
- Upload JARs to MavenCentral
- Build testbench Docker container based on JARs
- Copy shell scripts for chaos experiments
- Build choas worker Docker container based on chaos worker shell scripts
- Change versions in Kubernetes deployment descriptors to reference the correct version of the Docker containers
- Run Maven release command for Java sources (thus increasing the version)
- Commit changes of Maven release command and push changes to GitHub
- Create tag and release in GitHub
- Zip, checksum and add JARs to GitHub release
- Zip, checksum and add chaos experiment scripts to GitHub release
- Zip, checksum and add Kubernetes deployment descriptors to GitHub release
