Feature: Reporting app version

Scenario: Test handled Android Exception
    When I run "BugsnagInitScenario"
    And I wait to receive a request
    Then the request is valid for the error reporting API version "4.0" for the "Android Bugsnag Notifier" notifier
    And the event "metaData.client.count" equals 1
