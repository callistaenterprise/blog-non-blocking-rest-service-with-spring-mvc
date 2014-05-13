package basic

import com.excilys.ebi.gatling.core.Predef._
import com.excilys.ebi.gatling.http.Predef._
import com.excilys.ebi.gatling.jdbc.Predef._
import com.excilys.ebi.gatling.http.Headers.Names._
import akka.util.duration._
import bootstrap._

class SpringMvcAsynchSimulation extends Simulation {

  val rampUpTimeSecs = 60
  val testTimeSecs   = 360
  val noOfUsers      = 5000
	val minWaitMs      = 1000 milliseconds
  val maxWaitMs      = 3000 milliseconds

  val baseURL      = "http://localhost:9080"
  val baseName     = "spring-mvc-asynch"
  val requestName  = baseName + "-request"
  val scenarioName = baseName + "-scenario"
  val URI          = "/route-non-blocking?minMs=1000&maxMs=2000"
    
	val httpConf = httpConfig.baseURL(baseURL)

  val http_headers = Map(
    "Accept-Encoding" -> "gzip,deflate",
    "Content-Type" -> "text/json;charset=UTF-8",
		"Keep-Alive" -> "115")

	val scn = scenario(scenarioName)
    .during(testTimeSecs) { 		
      exec(
        http(requestName)
          .get(URI)
  				.headers(http_headers)
  				.check(status.is(200))
      )
      .pause(minWaitMs, maxWaitMs)
    }
  	setUp(scn.users(noOfUsers).ramp(rampUpTimeSecs).protocolConfig(httpConf))
}