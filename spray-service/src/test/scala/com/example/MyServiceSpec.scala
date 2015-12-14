package com.example

import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class MyServiceSpec extends Specification with Specs2RouteTest with MyService {
  def actorRefFactory = system
  
  "MyService" should {

    "return an empty response for GET requests to the /reverse?q path" in {
      Get("/reverse?q") ~> route ~> check {
        responseAs[String] must be_== ("")
      }
    }

    "return a reversed string response for GET requests to the /reverse?q=123 path" in {
      Get("/reverse?q=123") ~> route ~> check {
        responseAs[String] must be_== ("321")
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> route ~> check {
        handled must beFalse
      }
    }

    "return a NotFound error for Get requests without 'q' param to the /reverse path" in {
      Get("/reverse") ~> sealRoute(route) ~> check {
        status === NotFound
        responseAs[String] === "Request is missing required query parameter 'q'"
      }
    }

    "return a MethodNotAllowed error for PUT requests to the /reverse path" in {
      Put("/reverse") ~> sealRoute(route) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}
