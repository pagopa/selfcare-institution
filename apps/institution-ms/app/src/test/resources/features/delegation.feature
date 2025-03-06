Feature: Delegation

# POST /delegations

  @RemovePairOfMockInstitutionAfterScenario
  @RemoveCreatedDelegationAfterScenario
  Scenario: Successfully create delegation
    Given User login with username "j.doe" and password "test"
    And A pair of mock institutions with id "123" and "456"
    And The following request body:
      """
        {
          "from": "123",
          "to": "456",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 201
    And The response body contains:
      | institutionId       | 123                   |
      | institutionName     | From Institution      |
      | institutionRootName | From Root Institution |
      | type                | AOO                   |
      | productId           | prod-io               |
      | brokerId            | 456                   |
      | brokerName          | To Institution        |
      | status              | ACTIVE                |
    And The response body contains field "id"
    And The response body contains field "createdAt"
    And The response body contains field "updatedAt"
    And The delegation flag for institution "123" is false on db
    And The delegation flag for institution "456" is true on db
    And The delegation from institution "123" to institution "456" was saved to db successfully

  Scenario: Institution (FROM) not found while creating delegation
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "from": "123",
          "to": "fc5466e5-df00-4800-9ad5-aa2e7d9344f9",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 404
    And The response body contains:
      | status | 404                                                                                 |
      | detail | Cannot find Institution using institutionId 123 and externalInstitutionId UNDEFINED |

  Scenario: Institution (TO) not found while creating delegation
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "from": "fc5466e5-df00-4800-9ad5-aa2e7d9344f9",
          "to": "456",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 404
    And The response body contains:
      | status | 404                                                                                 |
      | detail | Cannot find Institution using institutionId 456 and externalInstitutionId UNDEFINED |

  @RemovePairOfMockInstitutionAfterScenario
  @RemoveCreatedDelegationAfterScenario
  Scenario: Conflict while creating delegation
    Given User login with username "j.doe" and password "test"
    And A pair of mock institutions with id "123" and "456"
    And A mock delegation of type EA with productId "prod-pagopa" for institution with id "123" and "456" with status ACTIVE
    And The following request body:
      """
        {
          "from": "123",
          "to": "456",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-pagopa",
          "type": "EA",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 409
    And The response body contains:
      | status | 409                                                                   |
      | detail | Delegation with parameters [from, to, productId, type] already exists |

  @RemovePairOfMockInstitutionAfterScenario
  @RemoveCreatedDelegationAfterScenario
  Scenario: Successfully enable deleted delegation while creating delegation
    Given User login with username "j.doe" and password "test"
    And A pair of mock institutions with id "123" and "456"
    And A mock delegation of type EA with productId "prod-pagopa" for institution with id "123" and "456" with status DELETED
    And The following request body:
      """
        {
          "from": "123",
          "to": "456",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-pagopa",
          "type": "EA",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 201
    And The response body contains:
      | institutionId       | 123                   |
      | institutionName     | From Institution      |
      | institutionRootName | From Root Institution |
      | type                | EA                    |
      | productId           | prod-pagopa           |
      | brokerId            | 456                   |
      | brokerName          | To Institution        |
      | status              | ACTIVE                |
    And The response body contains field "id"
    And The delegation flag for institution "123" is false on db
    And The delegation flag for institution "456" is true on db
    And The delegation from institution "123" to institution "456" was saved to db successfully

  Scenario: Bad request while creating delegation with missing from field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "from": "",
          "to": "456",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

  Scenario: Bad request while creating delegation with missing to field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "from": "123",
          "to": "",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

  Scenario: Bad request while creating delegation with missing institutionFromName field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "from": "123",
          "to": "456",
          "institutionFromName": "",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

  Scenario: Bad request while creating delegation with missing institutionToName field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "from": "123",
          "to": "456",
          "institutionFromName": "From Institution",
          "institutionToName": "",
          "productId": "prod-io",
          "type": "AOO",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

  Scenario: Bad request while creating delegation with missing productId field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "from": "123",
          "to": "456",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "",
          "type": "AOO",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

  Scenario: Bad request while creating delegation with missing type field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "from": "123",
          "to": "456",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "institutionFromRootName": "From Root Institution"
        }
      """
    When I send a POST request to "/delegations"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

# POST /delegations/from-taxcode



# GET /delegations



# DELETE /delegations/{delegationId}


