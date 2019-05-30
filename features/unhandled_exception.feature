Feature: Reporting Unhandled Exceptions

Scenario: Test Unhandled Kotlin Exception without Session
    When I run "UnhandledExceptionScenario"
    And I relaunch the app
    And I wait to receive a request
    And the request is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the payload field "events" is an array with 1 elements
    And the exception "errorClass" equals "java.lang.RuntimeException"
    And the exception "message" equals "UnhandledExceptionScenario"
    And the event "session" is null
    And the payload field "events.0.device.cpuAbi" is a non-empty array

Scenario: Test Unhandled Java Exception with Session
    When I run "UnhandledExceptionJavaScenario"
    And I relaunch the app
    And I wait to receive a request
    And the request is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the payload field "events" is an array with 1 elements
    And the exception "errorClass" equals "java.lang.RuntimeException"
    And the exception "message" equals "UnhandledExceptionJavaScenario"

Scenario: Test handled Kotlin Exception with Session
    When I run "UnhandledExceptionSessionScenario"
    And I relaunch the app
    And I wait to receive a request
    And the request is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the exception "errorClass" equals "java.lang.RuntimeException"
    And the exception "message" equals "UnhandledExceptionSessionScenario"
    And the event "session" is not null
    And the event "session.id" is not null
    And the event "session.startedAt" is not null
    And the event "session.events" is not null
    And the payload field "events.0.session.events.handled" equals 0
    And the payload field "events.0.session.events.unhandled" equals 1

Scenario: Test cached Unhandled Exception with Session sends
    When I run "ReportCacheScenario"
    And I wait for 2 seconds
    Then I should receive no requests

    And I relaunch the app
    And I wait to receive a request

    And the request is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the payload field "events" is an array with 1 elements
    And the exception "errorClass" equals "java.lang.RuntimeException"
    And the exception "message" equals "ReportCacheScenario"
