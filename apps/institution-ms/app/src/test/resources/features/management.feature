@FeatureManagement
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
    Then The status code is 200
    And The response body contains the list "found" of size 2
    And The response body contains at path "found" the following list of objects in any order:
      | id                                   | externalId  | description           |
      | 067327d3-bdd6-408d-8655-87e8f1960046 | 99000870064 | comune di dernice     |
      | 0b56686d-3e25-4851-86c8-b9ba0d4fe301 | 15555555555 | Comune di Castel Test |
    And The response body contains at path "found" the following list of objects in any order:
      | products.prod-io.product | products.prod-io.status | products.prod-io.origin | products.prod-io.originId | products.prod-io.institutionType |
      | prod-io                  | ACTIVE                  | IPA                     | c_d277                    | PT                               |
      | prod-io                  | ACTIVE                  | SELC                    | PSP_15555555555           | PSP                              |
    And The response body contains at path "found" the following list of objects in any order:
      | products.prod-fd.product | products.prod-fd.status | products.prod-fd.origin | products.prod-fd.originId | products.prod-fd.institutionType |
      |                          |                         |                         |                           |                                  |
      | prod-fd                  | ACTIVE                  | SELC                    | PSP_15555555555           | PSP                              |
    And The response body contains at path "found" the following list of objects in any order:
      | products.prod-pagopa.product | products.prod-pagopa.status | products.prod-pagopa.origin | products.prod-pagopa.originId | products.prod-pagopa.institutionType |
      | prod-pagopa                  | ACTIVE                      | IPA                         | c_d277                        | PT                                   |
      | prod-pagopa                  | ACTIVE                      | SELC                        | PSP_15555555555               | PSP                                  |
    And The response body contains at path "found" the following list of objects in any order:
      | products.prod-interop.product | products.prod-interop.status | products.prod-interop.origin | products.prod-interop.originId | products.prod-interop.institutionType |
      | prod-interop                  | ACTIVE                       | IPA                          | c_d277                         | PT                                    |
      | prod-interop                  | ACTIVE                       | SELC                         | PSP_15555555555                | PSP                                   |
    And The response body contains the list "notFound" of size 1
    And The response body contains at path "notFound" the following list of values in any order:
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
    Then The status code is 200
    And The response body contains the list "found" of size 0
    And The response body contains the list "notFound" of size 0

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