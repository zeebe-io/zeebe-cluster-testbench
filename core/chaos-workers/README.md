# Description

The chaos-workers are register for a specific job-type and run chaos experiments against a specific cluster plan.

The docker image:
 
  * bases on a python image
  * installs kubectl, chaos toolkit, git and other dependencies to run chaos experiments
  * needs access to the gke cluster via a service account token
  * starts a zeebe job worker via zbctl on running the image 
  
On building the docker image a service account token needs to be given, which is used to access the different gke cluster.
When running the image a zeebe job worker is started, as environment variables the auth details to the testbench have to been set.

The job worker runs a job handler script. This handler extracts from the given variables all necessary informations like auth details of the targeting cluster but also which cluster plan is it.
Regarding of the cluster plan different chaos experiments are executed. The job is completed with a `PASSED` or `FAILED` testResult variable.

