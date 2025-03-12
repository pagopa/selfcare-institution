@FeatureDelegation
Feature: Delegation

# POST /delegations

  @RemovePairOfMockInstitutionAfterScenario
  @RemoveCreatedDelegationAfterScenario
  Scenario: Successfully create delegation
    Given User login with username "j.doe" and password "test"
    And A pair of mock institutions with id "123","456" and taxcode "112233","445566" with subunitCode "",""
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
    And A pair of mock institutions with id "123","456" and taxcode "112233","445566" with subunitCode "",""
    And A mock delegation with id "123456" of type EA with productId "prod-pagopa" for institution with id "123" and "456" with status ACTIVE
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
    And A pair of mock institutions with id "123","456" and taxcode "112233","445566" with subunitCode "",""
    And A mock delegation with id "123456" of type EA with productId "prod-pagopa" for institution with id "123" and "456" with status DELETED
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

  @RemovePairOfMockInstitutionAfterScenario
  @RemoveCreatedDelegationAfterScenario
  Scenario: Successfully create delegation from taxcode
    Given User login with username "j.doe" and password "test"
    And A pair of mock institutions with id "123","456" and taxcode "112233","445566" with subunitCode "S1","S2"
    And The following request body:
      """
        {
          "fromTaxCode": "112233",
          "toTaxCode": "445566",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO",
          "fromSubunitCode": "S1",
          "toSubunitCode": "S2"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
    Then The status code is 201
    And The response body contains:
      | institutionId   | 123              |
      | institutionName | From Institution |
      | type            | AOO              |
      | productId       | prod-io          |
      | brokerId        | 456              |
      | brokerName      | To Institution   |
      | status          | ACTIVE           |
    And The response body contains field "id"
    And The response body contains field "createdAt"
    And The response body contains field "updatedAt"
    And The delegation flag for institution "123" is false on db
    And The delegation flag for institution "456" is true on db
    And The delegation from institution "123" to institution "456" was saved to db successfully

  Scenario: Institution (fromTaxCode) not found while creating delegation from taxcode
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "fromTaxCode": "123",
          "toTaxCode": "94076720658",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
    Then The status code is 404
    And The response body contains:
      | status | 404                                                                        |
      | detail | Cannot find Institution using taxCode fc5466e5-df00-4800-9ad5-aa2e7d9344f9 |

  Scenario: Institution (toTaxCode) not found while creating delegation from taxcode
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "fromTaxCode": "94076720658",
          "toTaxCode": "123",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
    Then The status code is 404
    And The response body contains:
      | status | 404                                        |
      | detail | Cannot find Institution using taxCode null |

  @RemovePairOfMockInstitutionAfterScenario
  @RemoveCreatedDelegationAfterScenario
  Scenario: Conflict while creating delegation from taxcode
    Given User login with username "j.doe" and password "test"
    And A pair of mock institutions with id "123","456" and taxcode "112233","445566" with subunitCode "S1","S2"
    And A mock delegation with id "123456" of type EA with productId "prod-pagopa" for institution with id "123" and "456" with status ACTIVE
    And The following request body:
      """
        {
          "fromTaxCode": "112233",
          "toTaxCode": "445566",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-pagopa",
          "type": "EA",
          "fromSubunitCode": "S1",
          "toSubunitCode": "S2"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
    Then The status code is 409
    And The response body contains:
      | status | 409                                                                   |
      | detail | Delegation with parameters [from, to, productId, type] already exists |

  @RemovePairOfMockInstitutionAfterScenario
  @RemoveCreatedDelegationAfterScenario
  Scenario: Successfully enable deleted delegation while creating delegation from taxcode
    Given User login with username "j.doe" and password "test"
    And A pair of mock institutions with id "123","456" and taxcode "112233","445566" with subunitCode "S1","S2"
    And A mock delegation with id "123456" of type EA with productId "prod-pagopa" for institution with id "123" and "456" with status DELETED
    And The following request body:
      """
        {
          "fromTaxCode": "112233",
          "toTaxCode": "445566",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-pagopa",
          "type": "EA",
          "fromSubunitCode": "S1",
          "toSubunitCode": "S2"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
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

  Scenario: Bad request while creating delegation from taxcode with missing fromTaxCode field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "toTaxCode": "445566",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

  Scenario: Bad request while creating delegation from taxcode with missing toTaxCode field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "fromTaxCode": "112233",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

  Scenario: Bad request while creating delegation from taxcode with missing institutionFromName field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "fromTaxCode": "112233",
          "toTaxCode": "445566",
          "institutionToName": "To Institution",
          "productId": "prod-io",
          "type": "AOO"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

  Scenario: Bad request while creating delegation from taxcode with missing institutionToName field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "fromTaxCode": "112233",
          "toTaxCode": "445566",
          "institutionFromName": "From Institution",
          "productId": "prod-io",
          "type": "AOO"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

  Scenario: Bad request while creating delegation from taxcode with missing productId field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "fromTaxCode": "112233",
          "toTaxCode": "445566",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "type": "AOO"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

  Scenario: Bad request while creating delegation from taxcode with missing type field
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
        {
          "fromTaxCode": "112233",
          "toTaxCode": "445566",
          "institutionFromName": "From Institution",
          "institutionToName": "To Institution",
          "productId": "prod-io"
        }
      """
    When I send a POST request to "/delegations/from-taxcode"
    Then The status code is 400
    And The response body contains:
      | status | 400              |
      | detail | INVALID ARGUMENT |

# GET /delegations

  Scenario: Successfully get delegations with institutionId,brokerId,productId,search
    Given User login with username "j.doe" and password "test"
    And The following query params:
      | institutionId | bf4dcdb6-f223-4996-bfbc-326b119dd101 |
      | brokerId      | c18d0cd5-e8a5-4f40-894b-f1f4252e1294 |
      | productId     | prod-io                              |
      | search        | Comune di Assisi                     |
    When I send a GET request to "/delegations"
    Then The status code is 200
    And The response body contains the list "" of size 2
    And The response body contains at path "" the following list of objects in any order:
      | id                                   | institutionId                        | institutionName  | institutionRootName | type | productId | taxCode     | institutionType | brokerId                             | brokerTaxCode | brokerType | brokerName           | status |
      | 88d04946-1b34-482c-8218-fda002b163cf | bf4dcdb6-f223-4996-bfbc-326b119dd101 | Comune di Assisi |                     | EA   | prod-io   | 00313820540 | PA              | c18d0cd5-e8a5-4f40-894b-f1f4252e1294 | 12399999890   | PT         | NTT Data             | ACTIVE |
      | 65a4c351-339b-44a2-bb74-cc650ba2413a | bf4dcdb6-f223-4996-bfbc-326b119dd101 | Comune di Assisi |                     | PT   | prod-io   | 00313820540 | PA              | c18d0cd5-e8a5-4f40-894b-f1f4252e1294 | 12399999890   | PT         | test pt senza geotax | ACTIVE |

  Scenario: Successfully get delegations with institutionId,order and pagination
    Given User login with username "j.doe" and password "test"
    And The following query params:
      | institutionId | bf4dcdb6-f223-4996-bfbc-326b119dd101 |
      | order         | DESC                                 |
      | page          | 1                                    |
      | size          | 10                                   |
    When I send a GET request to "/delegations"
    Then The status code is 200
    And The response body contains the list "" of size 1
    And The response body contains at path "" the following list of objects in any order:
      | id                                   | institutionId                        | institutionName  | institutionRootName | type | productId | taxCode     | institutionType | brokerId                             | brokerTaxCode | brokerType | brokerName | status |
      | 31d012c6-e7cf-4900-a243-b72bf416c522 | bf4dcdb6-f223-4996-bfbc-326b119dd101 | Comune di Assisi |                     | EA   | prod-io   | 00313820540 | PA              | cdaa3a10-8e4e-46ae-a365-c31a3f22b267 | 00017561486   | PT         | NTT Data   | ACTIVE |

  Scenario: Not found delegations
    Given User login with username "j.doe" and password "test"
    And The following query params:
      | institutionId | 123 |
    When I send a GET request to "/delegations"
    Then The status code is 200
    And The response body contains the list "" of size 0

  Scenario: Invalid request while getting delegations without institutionId and brokerId
    Given User login with username "j.doe" and password "test"
    When I send a GET request to "/delegations"
    Then The status code is 400
    And The response body contains:
      | status | 400                                          |
      | detail | institutionId or brokerId must not be null!! |

# DELETE /delegations/{delegationId}

  @RemovePairOfMockInstitutionAfterScenario
  @RemoveCreatedDelegationAfterScenario
  Scenario: Successfully delete delegation by id
    Given User login with username "j.doe" and password "test"
    And A pair of mock institutions with id "123","456" and taxcode "112233","445566" with subunitCode "S1","S2"
    And A mock delegation with id "123456" of type EA with productId "prod-pagopa" for institution with id "123" and "456" with status ACTIVE
    And The following path params:
      | delegationId | 123456 |
    When I send a DELETE request to "/delegations/{delegationId}"
    Then The status code is 204
    And The delegation flag for institution "123" is false on db
    And The delegation flag for institution "456" is false on db
    And The delegation with id "123456" is in state DELETED on db

  @RemovePairOfMockInstitutionAfterScenario
  Scenario: Not found delegationId while deleting delegation
    Given User login with username "j.doe" and password "test"
    And A pair of mock institutions with id "123","456" and taxcode "112233","445566" with subunitCode "S1","S2"
    And The following path params:
      | delegationId | 123456 |
    When I send a DELETE request to "/delegations/{delegationId}"
    Then The status code is 500
    And The response body contains:
      | status | 500                             |
      | detail | Error while deleting delegation |

  @RemoveCreatedDelegationAfterScenario
  Scenario: Not found institutions while deleting delegation
    Given User login with username "j.doe" and password "test"
    And A mock delegation with id "123456" without real institutions
    And The following path params:
      | delegationId | 123456 |
    When I send a DELETE request to "/delegations/{delegationId}"
    Then The status code is 204
    And The delegation with id "123456" is in state DELETED on db
