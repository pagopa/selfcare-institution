locals {
  prefix  = "selc"
  project = "selc-${var.env_short}"

  container_app_environment_name = "${local.project}-${var.cae_name}"
  ca_resource_group_name         = "${local.project}-container-app${var.suffix_increment}-rg"

  mongo_db = {
    mongodb_rg_name               = "${local.prefix}-${var.env_short}-cosmosdb-mongodb-rg",
    cosmosdb_account_mongodb_name = "${local.prefix}-${var.env_short}-cosmosdb-mongodb-account"
  }
}