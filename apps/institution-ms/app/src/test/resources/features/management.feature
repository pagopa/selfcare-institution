Feature: Management

  Scenario: Successfully retrieve institutions in bulk
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "partyIdentifiers": [
            "0b56686d-3e25-4851-86c8-b9ba0d4fe301",
            "0b56686d-3e25-4851-86c8-b9ba0d4fe301",
            "067327d3-bdd6-408d-8655-87e8f1960046",
            "123"
        ]
      }
      """
    When I send a POST request to "/bulk/institutions"
    And I convert the response to BulkInstitutions
    Then The status code is 200
    And The institutions found are:
      | 0b56686d-3e25-4851-86c8-b9ba0d4fe301 |
      | 067327d3-bdd6-408d-8655-87e8f1960046 |
    And The institutions not found are:
      | 123 |

  Scenario: Bad request on null list
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {}
      """
    When I send a POST request to "/bulk/institutions"
    Then The status code is 400

  Scenario: Empty lists
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "partyIdentifiers": []
      }
      """
    When I send a POST request to "/bulk/institutions"
    And I convert the response to BulkInstitutions
    Then The status code is 200
    And No institutions in found
    And No institutions in notFound

  Scenario: Unauthorized
    Given A bad jwt token
    And The following request body:
      """
      {
        "partyIdentifiers": []
      }
      """
    When I send a POST request to "/bulk/institutions"
    Then The status code is 401