module "mongodb_collection_pec_notifications" {
  source = "git::https://github.com/pagopa/terraform-azurerm-v3.git//cosmosdb_mongodb_collection?ref=v7.39.0"

  name                = "PecNotification"
  resource_group_name = local.mongo_db.mongodb_rg_name

  cosmosdb_mongo_account_name  = local.mongo_db.cosmosdb_account_mongodb_name
  cosmosdb_mongo_database_name = "selcMsCore"

  indexes = [{
      keys   = ["_id"]
      unique = true
    },
    {
      keys   = ["moduleDayOfTheEpoch"]
      unique = false
    }
  ]

  lock_enable = true
}