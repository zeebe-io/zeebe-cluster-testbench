package io.zeebe.clustertestbench.testdriver.api;

public interface CamundaCloudAuthenticationDetails {

	String VARIABLE_KEY = "authenticationDetails";

	String getAudience();

	String getAuthorizationURL();

	String getClientId();

	String getClientSecret();

	String getContactPoint();

}