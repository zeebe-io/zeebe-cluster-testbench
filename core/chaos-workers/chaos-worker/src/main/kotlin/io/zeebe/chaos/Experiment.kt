package io.zeebe.chaos

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class Experiment(
    var title: String,
    @JsonProperty("steady-state-hypothesis")
    var steadyState: Hypothesis,
    var method: List<Method>,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Hypothesis(
    var title: String?,
    var probes: List<Probe>
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Probe(
    var name: String,
    var provider: Provider
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Method(
    var type: String,
    var name: String,
    var provider: Provider
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Provider(
    var type: String,
    var path: String,
    var timeout: Long
)
