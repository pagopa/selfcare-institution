Feature: Institution

# GET /institutions

# POST /institutions/from-ipa

# POST /institutions/from-anac

  @RemoveInstitutionIdAfterScenario
  Scenario: Successfully create institution from ANAC
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "06068501219"
      }
      """
    When I send a POST request to "/institutions/from-anac"
    Then The status code is 201
    And The response body contains:
      | taxCode  | 06068501219 |
      | origin   | ANAC        |
      | originId | 06068501219 |
    And The response body contains field "id"

  Scenario: TaxCode not found from ANAC
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "123456789"
      }
      """
    When I send a POST request to "/institutions/from-anac"
    Then The status code is 404

  @RemoveInstitutionIdAfterScenario
  Scenario: Successfully create and update institution from ANAC
    # CREATE
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "06068501219"
      }
      """
    When I send a POST request to "/institutions/from-anac"
    Then The status code is 201
    And The response body contains:
      | taxCode   | 06068501219 |
      | origin    | ANAC        |
      | originId  | 06068501219 |
    And The response body contains field "id"
    And The response body doesn't contain field "supportEmail"
    # UPDATE
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "06068501219",
        "supportEmail": "test@pec.it"
      }
      """
    When I send a POST request to "/institutions/from-anac"
    Then The status code is 201
    And The response body contains:
      | taxCode       | 06068501219 |
      | origin        | ANAC        |
      | originId      | 06068501219 |
      | supportEmail  | test@pec.it |
    And The response body contains field "id"

# POST /institutions/from-ivass

  @RemoveInstitutionIdAfterScenario
  Scenario: Successfully create institution from IVASS
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "originId": "A044S"
      }
      """
    When I send a POST request to "/institutions/from-ivass"
    Then The status code is 201
    And The response body contains:
      | taxCode  | 00409920584 |
      | origin   | IVASS       |
      | originId | A044S       |
    And The response body contains field "id"

  Scenario: originId not found from IVASS
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "originId": "A1234"
      }
      """
    When I send a POST request to "/institutions/from-ivass"
    Then The status code is 404

  @RemoveInstitutionIdAfterScenario
  Scenario: Successfully create and update institution from IVASS
    # CREATE
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "originId": "A044S"
      }
      """
    When I send a POST request to "/institutions/from-ivass"
    Then The status code is 201
    And The response body contains:
      | taxCode  | 00409920584 |
      | origin   | IVASS       |
      | originId | A044S       |
    And The response body contains field "id"
    And The response body doesn't contain field "supportEmail"
    # UPDATE
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "originId": "A044S",
        "supportEmail": "test@pec.it"
      }
      """
    When I send a POST request to "/institutions/from-ivass"
    Then The status code is 201
    And The response body contains:
      | taxCode      | 00409920584 |
      | origin       | IVASS       |
      | originId     | A044S       |
      | supportEmail | test@pec.it |
    And The response body contains field "id"

# POST /institutions/from-pda (Search first on IPA and if not found search on INFOCAMERE)

# POST /institutions/from-infocamere

  @RemoveInstitutionIdAfterScenario
  Scenario: Successfully create institution from INFOCAMERE
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "01501320442"
      }
      """
    When I send a POST request to "/institutions/from-infocamere"
    Then The status code is 201
    And The response body contains:
      | taxCode  | 01501320442 |
      | origin   | INFOCAMERE  |
      | originId | 01501320442 |
    And The response body contains field "id"

  @RemoveInstitutionIdAfterScenario
  Scenario: taxCode not found from INFOCAMERE
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "123456789"
      }
      """
    When I send a POST request to "/institutions/from-infocamere"
    Then The status code is 201
    And The response body contains:
      | taxCode  | 123456789 |
      | origin   | ADE       |
      | originId | 123456789 |
    And The response body contains field "id"

  @RemoveInstitutionIdAfterScenario
  Scenario: Successfully create and update institution from INFOCAMERE
    # CREATE
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "01501320442"
      }
      """
    When I send a POST request to "/institutions/from-infocamere"
    Then The status code is 201
    And The response body contains:
      | taxCode  | 01501320442 |
      | origin   | INFOCAMERE  |
      | originId | 01501320442 |
    And The response body contains field "id"
    And The response body doesn't contain field "description"
    # UPDATE
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "01501320442",
        "description": "test description"
      }
      """
    When I send a POST request to "/institutions/from-infocamere"
    Then The status code is 201
    And The response body contains:
      | taxCode     | 01501320442      |
      | origin      | INFOCAMERE       |
      | originId    | 01501320442      |
      | description | test description |
    And The response body contains field "id"

# POST /institutions/{externalId} (deprecated)

# POST /institutions

# POST /institutions/insert/{externalId} (deprecated)

# POST /institutions/pg

# GET /institutions/{id}/products

# PUT /institutions/{id}

# POST /institutions/{id}/onboarding

# DELETE /institutions/{id}/products/{productId}

# GET /institutions/{id}/geotaxonomies

# GET /institutions/{id}

# GET /institutions/onboardings

# POST /institutions/onboarded/{productId}

# PUT /institutions/{institutionId}/created-at

# GET /institutions/products/{productId}

# GET /institutions/{productId}/brokers/{institutionType}
