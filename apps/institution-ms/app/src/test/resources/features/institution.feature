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

  @RemoveInstitutionIdAfterScenario
  Scenario: Successfully create raw institution
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "123456789"
      }
      """
    When I send a POST request to "/institutions"
    Then The status code is 201
    And The response body contains:
      | taxCode  | 123456789      |
      | origin   | SELC           |
      | originId | SELC_123456789 |
    And The response body contains field "id"

  @RemoveInstitutionIdAfterScenario
  Scenario: Successfully create and update raw institution
    # CREATE
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "123456789"
      }
      """
    When I send a POST request to "/institutions"
    Then The status code is 201
    And The response body contains:
      | taxCode  | 123456789      |
      | origin   | SELC           |
      | originId | SELC_123456789 |
    And The response body contains field "id"
    And The response body doesn't contain field "supportEmail"
    # UPDATE
    Given User login with username "j.doe" and password "test"
    And The following request body:
      """
      {
        "taxCode": "123456789",
        "supportEmail": "test@pec.it"
      }
      """
    When I send a POST request to "/institutions"
    Then The status code is 201
    And The response body contains:
      | taxCode      | 123456789      |
      | origin       | SELC           |
      | originId     | SELC_123456789 |
      | supportEmail | test@pec.it    |
    And The response body contains field "id"

# POST /institutions/insert/{externalId} (deprecated)

# POST /institutions/pg

# GET /institutions/{id}/products

  Scenario: Successfully get products related to an institution
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | f94c0589-b07e-4ee7-a509-fda5fe91faa2 |
    When I send a GET request to "/institutions/{id}/products"
    Then The status code is 200
    And The response body contains the list "products" of size 4
    And The response body contains at path "products.id" the following list of values in any order:
      | prod-io      |
      | prod-pn      |
      | prod-idpay   |
      | prod-interop |
    And The response body contains at path "products.state" the following list of values in any order:
      | ACTIVE  |
      | PENDING |
      | PENDING |
      | PENDING |

  Scenario: Successfully get products related to an institution with states
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | f94c0589-b07e-4ee7-a509-fda5fe91faa2 |
    And The following query params:
      | states | PENDING,DELETED |
    When I send a GET request to "/institutions/{id}/products"
    Then The status code is 200
    And The response body contains the list "products" of size 3
    And The response body contains at path "products.id" the following list of values in any order:
      | prod-pn      |
      | prod-interop |
      | prod-idpay   |
    And The response body contains at path "products.state" the following list of values in any order:
      | PENDING |
      | PENDING |
      | PENDING |

  Scenario: Get products related to an institution not found
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | 123 |
    When I send a GET request to "/institutions/{id}/products"
    Then The status code is 404
    And The response body contains:
      | detail | Cannot find Institution using institutionId 123 and externalInstitutionId UNDEFINED |

  Scenario: Get (not found) products related to an institution
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | f94c0589-b07e-4ee7-a509-fda5fe91faa2 |
    And The following query params:
      | states | DELETED |
    When I send a GET request to "/institutions/{id}/products"
    Then The status code is 404
    And The response body contains:
      | detail | Products not found for institution having internalId f94c0589-b07e-4ee7-a509-fda5fe91faa2 |

  Scenario: Get products related to an institution with invalid state in request
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | f94c0589-b07e-4ee7-a509-fda5fe91faa2 |
    And The following query params:
      | states | ACTIVE,XXX,PENDING |
    When I send a GET request to "/institutions/{id}/products"
    Then The status code is 400

# PUT /institutions/{id}

# POST /institutions/{id}/onboarding

# DELETE /institutions/{id}/products/{productId}

# GET /institutions/{id}/geotaxonomies

  Scenario: Successfully get geotaxonomies
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | c9a50656-f345-4c81-84be-5b2474470544 |
    When I send a GET request to "/institutions/{id}/geotaxonomies"
    Then The status code is 200
    And The response body contains the list "" of size 1
    And The response body contains:
      | [0].code       | 058091 |
      | [0].istat_code | 058091 |
      | [0].enabled    | false  |

  Scenario: Get geotaxonomies with non existent institution
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | 123 |
    When I send a GET request to "/institutions/{id}/geotaxonomies"
    Then The status code is 404
    And The response body contains:
      | detail | Cannot find Institution using institutionId 123 and externalInstitutionId UNDEFINED |

  Scenario: Get geotaxonomies with institution without geotaxonomies
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | f94c0589-b07e-4ee7-a509-fda5fe91faa2 |
    When I send a GET request to "/institutions/{id}/geotaxonomies"
    Then The status code is 500
    And The response body contains:
      | detail | GeographicTaonomies for institution f94c0589-b07e-4ee7-a509-fda5fe91faa2 not found |

  Scenario: Get geotaxonomies with institution with non existent geotax code
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | 067327d3-bdd6-408d-8655-87e8f1960046 |
    When I send a GET request to "/institutions/{id}/geotaxonomies"
    Then The status code is 500
    And The response body contains:
      | detail | Error on retrieve geographic taxonomy code: 123 |

# GET /institutions/{id}

  Scenario: Successfully get institution by id
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | c9a50656-f345-4c81-84be-5b2474470544 |
    When I send a GET request to "/institutions/{id}"
    Then The status code is 200
    And The response body contains:
      | id | c9a50656-f345-4c81-84be-5b2474470544 |
      | logo | test-logo-url/c9a50656-f345-4c81-84be-5b2474470544/logo.png |

  Scenario: Get institution by id not found
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | id | 123 |
    When I send a GET request to "/institutions/{id}"
    Then The status code is 404
    And The response body contains:
      | detail | Cannot find Institution using institutionId 123 and externalInstitutionId UNDEFINED |

# GET /institutions/{institutionId}/onboardings

  Scenario: Successfully get onboardings on institution
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | institutionId | 067327d3-bdd6-408d-8655-87e8f1960046 |
    When I send a GET request to "/institutions/{institutionId}/onboardings"
    Then The status code is 200

  Scenario: Successfully get onboardings on institution with productId
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | institutionId | c9a50656-f345-4c81-84be-5b2474470544 |
    And The following query params:
      | productId | prod-pagopa |
    When I send a GET request to "/institutions/{institutionId}/onboardings"
    Then The status code is 200
    And The response body contains the list "onboardings" of size 1
    And The response body contains:
      | onboardings[0].productId | prod-pagopa |
      | onboardings[0].status    | ACTIVE      |

  Scenario: Get onboardings on institution not found
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | institutionId | 123 |
    When I send a GET request to "/institutions/{institutionId}/onboardings"
    Then The status code is 200
    And The response body contains the list "onboardings" of size 0

  Scenario: Get onboardings on institution with productId not found
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | institutionId | c9a50656-f345-4c81-84be-5b2474470544 |
    And The following query params:
      | productId | xxx |
    When I send a GET request to "/institutions/{institutionId}/onboardings"
    Then The status code is 200
    And The response body contains the list "onboardings" of size 0

# POST /institutions/onboarded/{productId}

# PUT /institutions/{institutionId}/created-at

# GET /institutions/products/{productId}

  Scenario: Successfully get institutions with a productId
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | productId | prod-fd |
    When I send a GET request to "/institutions/products/{productId}"
    Then The status code is 200
    And The response body contains the list "items" of size 2
    And The response body contains:
      | items[0].onboardings.prod-fd.productId | prod-fd |
      | items[1].onboardings.prod-fd.productId | prod-fd |

  Scenario: Successfully get institutions with productId, page and size
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | productId | prod-io |
    And The following query params:
      | page | 1 |
      | size | 4 |
    When I send a GET request to "/institutions/products/{productId}"
    Then The status code is 200
    And The response body contains the list "items" of size 2

  Scenario: Get institutions with non existent productId
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | productId | prod-xxx |
    When I send a GET request to "/institutions/products/{productId}"
    Then The status code is 200
    And The response body contains the list "items" of size 0

# GET /institutions/{productId}/brokers/{institutionType}

  Scenario: Successfully get institutions brokers
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | productId       | prod-io |
      | institutionType | PA      |
    When I send a GET request to "/institutions/{productId}/brokers/{institutionType}"
    Then The status code is 200
    And The response body contains the list "" of size 2
    And The response body contains at path "taxCode" the following list of values in any order:
      | 85000870064 |
      | 00310810825 |

  Scenario: Get institutions brokers with bad productId
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | productId       | prod-xxx |
      | institutionType | PA       |
    When I send a GET request to "/institutions/{productId}/brokers/{institutionType}"
    Then The status code is 200
    And The response body contains the list "" of size 0

  Scenario: Get institutions brokers with bad institutionType
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | productId       | prod-io |
      | institutionType | 123     |
    When I send a GET request to "/institutions/{productId}/brokers/{institutionType}"
    Then The status code is 400

  Scenario: Get institutions brokers with missing institutionType
    Given User login with username "j.doe" and password "test"
    And The following path params:
      | productId       | prod-io |
      | institutionType | REC     |
    When I send a GET request to "/institutions/{productId}/brokers/{institutionType}"
    Then The status code is 200
    And The response body contains the list "" of size 0
