package io.zeebe.clustertestbench.testdriver.impl;

import java.util.List;
import java.util.Map;

import io.zeebe.clustertestbench.testdriver.api.TestReport;

public class TestReportDTO implements TestReport {

	private TestResult testResult;
	private List<String> failureMessages;
	private int failureCount;
	private Map<String, Object> metaData;
	private long startTime;
	private long endTime;
	private long timeOfFirstFailure;

	@Override
	public TestResult getTestResult() {
		return testResult;
	}

	@Override
	public List<String> getFailureMessages() {
		return failureMessages;
	}

	@Override
	public int getFailureCount() {
		return failureCount;
	}

	@Override
	public Map<String, Object> getMetaData() {
		return metaData;
	}

	@Override
	public long getStartTime() {
		return startTime;
	}

	@Override
	public long getEndTime() {
		return endTime;
	}

	@Override
	public long getTimeOfFirstFailure() {
		return timeOfFirstFailure;
	}

	public void setTestResult(TestResult testResult) {
		this.testResult = testResult;
	}

	public void setFailureMessages(List<String> failureMessages) {
		this.failureMessages = failureMessages;
	}

	public void setFailureCount(int failureCount) {
		this.failureCount = failureCount;
	}

	public void setMetaData(Map<String, Object> metaData) {
		this.metaData = metaData;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setTimeOfFirstFailure(long timeOfFirstFailure) {
		this.timeOfFirstFailure = timeOfFirstFailure;
	}

}
